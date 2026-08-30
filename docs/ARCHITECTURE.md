# Arquitectura — BancoPago

Documento vivo de la arquitectura del proyecto. Para decisiones detalladas ver
[`ARCHITECTURE_DECISIONS.md`](./ARCHITECTURE_DECISIONS.md). Para implementar un
módulo nuevo ver [`IMPLEMENTATION_GUIDE.md`](./IMPLEMENTATION_GUIDE.md).

---

## 1. Estilo

**Clean Architecture / Hexagonal (Puertos y Adaptadores)** + **Spring WebFlux** (reactivo).

Regla de dependencia: las capas externas dependen de las internas, nunca al revés.

```
Controller (infra primary adapter)
    → Interactor (primary port)          ← DTO ↔ Domain + delega
        → UseCaseImpl (aplicación)       ← Domain + RulesValidator + Repository
            → RulesValidator             ← solo reglas CON ESTADO (repos)
            → Repository (secondary port)
                → Adapter                ← Domain ↔ Entity
                → Domain (VOs, entidades, excepciones)
```

| Capa | Depende de | Contiene |
|------|------------|----------|
| **Domain** | Nada (Java puro) | Entidades, VOs, enums, excepciones, `*Error` |
| **Application** | Domain | Interactors, UseCases, RulesValidators, `Rule`, DTOs, mappers DTO, **puertos de repo** (`secondaryports/repository/`) |
| **Infrastructure** | Application + Domain | Controllers, adapters R2DBC (entity, mapper, Spring Data repo), seguridad, `GlobalExceptionHandler` |

---

## 2. Quién llama a quién (Interactor / UseCase / RulesValidator)

Este es el patrón central de la capa de aplicación. **No son ideas al aire**: está implementado así en el código.

```
Controller (infra)
    → Interactor (DTO ↔ Domain + delega)
        → UseCaseImpl (Domain only: RulesValidator + Repository + lógica dominio)
            → RulesValidator  (solo reglas CON ESTADO)
            → Repository      (Domain in/out; Entity solo en Adapter)
```

| Pieza | Qué hace | Qué NO hace |
|-------|----------|-------------|
| **Controller** | HTTP + `@Valid` → llama Interactor → `HttpResponses` / `SseEvents` | Lógica de negocio, repos, armar envelopes a mano |
| **Interactor** | Map DTO→Domain; llama UseCase; map Domain→Response (DTO puro) | Validar con repo, persistir, conocer `ApiResponse` / `ServerSentEvent` / HTTP |
| **UseCase** | RulesValidator → dominio → Repository (Domain) | Conocer DTOs ni Entity |
| **RulesValidator** | Validaciones con estado (repos) + excepciones tipadas | Reglas puras de VO; persistir |
| **Repository** | Persistencia / lectura reactiva (Domain) | Conocer DTOs ni HTTP |
| **Domain** | VOs, invariantes, `block()`/`deposit()` | Spring, R2DBC, “¿existe en BD?” |

### ¿Interactor y UseCase no son lo mismo?

En Clean Architecture clásica *Interactor* ≡ *UseCase*. Aquí se separan a propósito:

- `*Interactor` = contrato hacia el Controller + mapeo DTO↔Domain.
- `*UseCase` + `*UseCaseImpl` = lógica de aplicación sobre Domain.
- El Adapter R2DBC (no el UseCase) mapea Domain↔Entity.

### Ejemplo real (`CreatePerson`)

```
CreatePersonInteractorImpl.execute(request)
  → PersonDTOMapper.toPersonDomain(request)
  → CreatePersonUseCase.execute(domain)
      → CreatePersonRulesValidator.validate
            → UniqueDocumentRule (tipo+número)
            → UniqueEmailRule
      → PersonRepository.savePerson(domain)
  → PersonDTOMapper.toCreatePersonResponse(domain)
```

### Ejemplo real (`CreateAccount`)

```
CreateAccountInteractorImpl.execute(request)
  → AccountNumberGenerator → new AccountDomain(...)
  → CreateAccountUseCase.execute(domain)
      → CreateAccountRulesValidator.validate
            → OwnerExistsRule
            → MaxAccountsPerOwnerRule (máx. 5)
            → UniqueAccountTypePerOwnerRule (máx. 1 cuenta activa/bloqueada del mismo tipo)
            → UniqueAccountNumberRule
      → AccountRepository.saveAccount(domain)
  → AccountDTOMapper.toCreateAccountResponse(domain)
```

### Ejemplo real (`BlockAccount`)

```
BlockAccountInteractorImpl.execute(accountId)
  → BlockAccountUseCase.execute(accountId)
      → findAccountById → AccountNotFoundException si vacío
      → AccountDomain.block() → save
  → AccountDTOMapper.toAccountStatusResponse(domain)
```

Igual patrón para `UnblockAccount` (`unblock`) y `CloseAccount` (`close`). Transiciones inválidas → `InvalidAccountStateException` en dominio.

### REST (Módulo 1) — paths en inglés

| Método | Path | Interactor |
|--------|------|------------|
| POST | `/api/v1/persons` | `CreatePersonInteractor` → 201 |
| GET | `/api/v1/persons/{id}` | `GetPersonByIdInteractor` → 200 |
| POST | `/api/v1/accounts` | `CreateAccountInteractor` → 201 |
| GET | `/api/v1/accounts/{id}/balance` | `GetAccountBalanceInteractor` → 200 |
| GET | `/api/v1/accounts/{id}/balance/stream` | `StreamAccountBalanceInteractor` → SSE (`text/event-stream`) |
| GET | `/api/v1/accounts?ownerId={uuid}` | `ListAccountsByOwnerInteractor` → 200 |
| POST | `/api/v1/accounts/{id}/block` | `BlockAccountInteractor` → 200 |
| POST | `/api/v1/accounts/{id}/unblock` | `UnblockAccountInteractor` → 200 |
| POST | `/api/v1/accounts/{id}/close` | `CloseAccountInteractor` → 200 |

Respuesta de éxito: `ApiResponse<T>` (`data` + `messages`) vía `HttpResponses` (created/ok/okList). Mensajes de éxito en español vía `infrastructure/ResponseMessages`. SSE vía `SseEvents.map(flux, SseEvents.BALANCE)`. Errores: `GlobalExceptionHandler` → `{ code, message, messages }`.

**Contrato del Controller (delgado):**
```
return HttpResponses.created(interactor.execute(request), ResponseMessages.X);
return HttpResponses.ok(interactor.execute(id));
return HttpResponses.ok(interactor.execute(id), ResponseMessages.X);
return HttpResponses.okList(interactor.execute(ownerId));
return SseEvents.map(interactor.execute(id), SseEvents.BALANCE);
```

**Por qué el envoltorio NO va en el Interactor:** `ApiResponse`, `ResponseEntity` y `ServerSentEvent` son detalles del adaptador HTTP. El Interactor debe poder reutilizarse desde otro primary adapter (CLI, gRPC, mensaje) sin arrastrar Spring Web. Los helpers viven en `infrastructure/primaryadapters/adapter/response/`.

Responses tipados por subtipo: `CreatePersonResponse` / `GetPersonByIdResponse` son `sealed interface` → `CreateClientResponse` / `CreateEmployeeResponse` (y equivalentes Get). Campos ajenos al tipo no se serializan (`@JsonInclude(NON_NULL)`). `AccountStatusResponse` solo incluye `id`, `number`, `status`, `balance`.

No hay un `PATCH .../estado` único: bloqueo, desbloqueo y cierre son endpoints/use cases separados.

### Rules de aplicación actuales (Módulo 1)

| Use case | Rules |
|----------|--------|
| CreatePerson | `UniqueDocumentRule`, `UniqueEmailRule` |
| CreateAccount | `OwnerExistsRule`, `MaxAccountsPerOwnerRule`, `UniqueAccountTypePerOwnerRule`, `UniqueAccountNumberRule` |
| ListAccountsByOwner | (ninguna; lectura por owner) |
| StreamAccountBalance | `AccountExistsRule` (vía `StreamAccountBalanceRulesValidator`) |
| Block / Unblock / Close Account | (ninguna; invariantes en Domain + NotFound en UseCase) |

---

## 3. Dos tipos de reglas (puro vs con repositorio)

| Tipo | ¿Necesita BD? | Dónde vive | Cómo se expresa |
|------|---------------|------------|-----------------|
| **Pura** | No | **Dominio** | VOs, métodos de entidad, constructores |
| **Con estado / de aplicación** | Sí (o política del UC) | **Aplicación → `*Rule` + `*RuleImpl`** | Orquestadas por `*RulesValidatorImpl` (sin lógica inline) |

```
Nivel 1  Jakarta (@NotNull…)     →  request malformado
Nivel 2  Dominio (VO / entidad)  →  reglas PURAS
Nivel 3  RulesValidator + Rules  →  reglas con repo o política del use case
```

**Patrón:** cada validación con repo es interfaz `*Rule extends Rule<T>` + `*RuleImpl`; el `RulesValidator` las ejecuta en un solo `validate(...)`. El UseCase no conoce las rules individuales.

Invariantes puros recientes (no Rule): nombre máx. 100 chars en `PersonDomain`; `clientNumber` obligatorio en `ClientDomain`; `position` + `area` obligatorios en `EmployeeDomain`; `AccountDomain.close()` exige saldo cero.

| Ejemplo AgroSync | Equivalente BancoPago |
|------------------|----------------------|
| `PesoValidoRule` | VO |
| `IdentificadorExisteRule` (repo) | `OwnerExistsRule` en `rulesvalidator/rules/` |
| Validator que llama a las rules | `CreateAccountRulesValidatorImpl.validate(account)` |

---

## 4. Excepciones y mensajes — cuándo crear qué

Hay **dos piezas** distintas:

| Pieza | Rol | Ejemplo |
|-------|-----|---------|
| `*Error` (enum) | Catálogo de **mensajes** (templates en español) + código (`ACCOUNT_NOT_FOUND`) | `AccountError.NOT_FOUND`, `PersonError.DOCUMENT_ALREADY_EXISTS` |
| `*Exception` (clase) | Fallo **tipado** que el handler HTTP puede mapear (404, 409, 400…) | `AccountNotFoundException`, `DuplicateDocumentException` |

Flujo del mensaje:

```
AccountError.NOT_FOUND("No se encontró la cuenta con id %s")
        ↓
AccountNotFoundException.create(id)
        ↓  DomainException formatea el template con args
getUserMessage() / getCode()  →  GlobalExceptionHandler → `{ "code", "message", "messages" }`
```

Mapeo HTTP tipado: NotFound → 404; Duplicate* / Blocked / MaxAccounts / DuplicateAccountType → 409; Invalid* / InsufficientBalance → 400; fallback `DomainException` → 400; genérica → 500.

### ¿Cuándo nueva clase de excepción?

| Crear clase nueva | Reutilizar `InvalidAccountException` / `InvalidPersonException` |
|-------------------|------------------------------------------------------------------|
| Caso de negocio distinto y el HTTP/status importa por tipo | Varios códigos “datos inválidos” del mismo módulo |
| Ej: NotFound → 404, Duplicate → 409, Blocked → 409 | Ej: `NUMBER_EMPTY`, `TYPE_REQUIRED`, `CURRENCY_MISMATCH` vía `InvalidAccountException.create(AccountError.X, args)` |

**Ya existían (invariantes de dominio):**  
`InvalidAccountException`, `InvalidPersonException`, `AccountBlockedException`, `InsufficientBalanceException`, `InvalidAccountStateException`, `InvalidAmountException`.

**Se crearon para casos de aplicación/consulta (use cases):**  
`PersonNotFoundException`, `DuplicateDocumentException`, `AccountNotFoundException`, `DuplicateAccountException`, `DuplicateAccountTypeException`, `MaxAccountsExceededException`, `DuplicateEmailException`.

No hace falta una excepción por cada entrada del enum: el enum tiene **todos** los mensajes; la clase tipada solo cuando el tipo de fallo importa.

### ¿Quién lanza qué?

| Origen | Ejemplo |
|--------|---------|
| VO / entidad (puro) | `new Email(...)` → `InvalidPersonException` + `PersonError.EMAIL_INVALID` |
| RulesValidator (estado) | documento ya existe → `DuplicateDocumentException` |
| UseCase (consulta / carga vacía) | `findById` empty → `AccountNotFoundException` (GetBalance, Block/Unblock/Close) |

---

## 5. Validación en tres niveles (resumen)

| Nivel | Dónde | Mecanismo | Ejemplo |
|-------|-------|-----------|---------|
| **1** | Controller / DTO Request | Jakarta Validation | `@NotBlank`, `@Email`, `@Size(max=100)` en `CreatePersonRequest` |
| **2** | Domain / VO / entidad | Compact constructor, métodos de negocio | `Email`, `Money`, `clientNumber` requerido, `account.block()` |
| **3** | Application / RulesValidator | Consulta a repositorio | Documento único (tipo+número), owner existe, 1 tipo de cuenta por owner |

---

## 6. Mapeo

| Dirección | Herramienta | Por qué |
|-----------|-------------|---------|
| Domain → Response | **MapStruct** (`interface` o `abstract class @Mapper`) | Casi 1:1; valor real de MapStruct |
| Request → Domain (VOs/herencia) | Método concreto en `*DTOMapper` | Factory de aplicación; no forzar generación |
| Entity ↔ Domain | **Manual** (`@Component`) | VOs + `PersonDomain` abstracta |

Los Response DTOs **no** llevan `fromDomain()` — el mapeo vive en el `*DTOMapper` (Interactor).  
No hay Assembler/Factory como convención global: solo si un Response combina varios agregados o el `toDomain` crece demasiado.

---

## 7. Stack

| Pieza | Tecnología |
|-------|------------|
| Runtime | Java 21, Spring Boot 4 / WebFlux |
| Persistencia | R2DBC + PostgreSQL 16, Flyway |
| Caché / locks | Redis |
| Mapeo DTO | MapStruct |
| API docs | Springdoc OpenAPI |
| Tests | JUnit 5, Mockito, StepVerifier, Testcontainers |
| Frontend | Angular 18 |

---

## 8. Estructura de paquetes (backend)

```
com.bancopago.backend/
├── domain/
├── application/
│   ├── primaryports/ ...                 # dto, interactor, mapper (MapStruct DTO↔Domain)
│   ├── secondaryports/
│   │   └── repository/                   # interfaces reactivas (Domain in/out)
│   └── usecase/
│       ├── Rule.java
│       ├── RulesValidator.java
│       ├── UseCaseWithReturn.java    # base; UC concreto = interfaz vacía que la extiende
│       └── {module}/
│           ├── impl/                 # implementa execute(...)
│           └── rulesvalidator/
│               ├── impl/
│               └── rules/(+ impl/)
├── infrastructure/
│   ├── primaryadapters/
│   │   ├── controller/{module}/
│   │   └── adapter/response/         # ApiResponse, HttpResponses, SseEvents
│   ├── secondaryadapters/
│   │   └── r2dbc/
│   │       ├── entity/                   # AccountEntity, PersonEntity (@Table)
│   │       ├── mapper/                   # Entity↔Domain (manual)
│   │       ├── {module}/                 # *R2dbcAdapter + *R2dbcRepository
│   │       └── config/
│   ├── GlobalExceptionHandler.java
│   ├── ErrorResponse.java
│   └── ResponseMessages.java         # constantes de mensajes de éxito (español)
└── crosscutting/
```

**Persistencia Person (subtipos):** columnas `client_number`, `membership_date`, `position`, `area`, `cost_center`, `contract_type` (V2). Unicidad de documento = `(document_type, document_number)` (V3), alineada con `UniqueDocumentRule`. `document_number` es `VARCHAR(30)` (alineado con VO).

**SSE:** UseCase/Interactor emiten `Flux<DTO>`; el Controller nombra el evento con `SseEvents` (`BALANCE = "balance"`). Polling cada 5s en `StreamAccountBalanceUseCaseImpl` (pub/sub Redis → Módulo 2).

---

## 9. Documentos relacionados

| Documento | Para qué |
|-----------|----------|
| [`ARCHITECTURE_DECISIONS.md`](./ARCHITECTURE_DECISIONS.md) | Decisiones, convenciones, patrones detallados |
| [`IMPLEMENTATION_GUIDE.md`](./IMPLEMENTATION_GUIDE.md) | Guía paso a paso para nuevas funcionalidades |
| [`ROADMAP.md`](./ROADMAP.md) | Estado de módulos y subtareas |
| [`ISSUE_TEMPLATE.md`](./ISSUE_TEMPLATE.md) | Plantilla para issues de GitHub |
