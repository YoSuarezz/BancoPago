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
| **Application** | Domain | Interactors, UseCases, RulesValidators, `Rule`, DTOs, mappers, puertos de repo |
| **Infrastructure** | Application + Domain | Controllers, adapters R2DBC, seguridad, `GlobalExceptionHandler` |

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
| **Controller** | HTTP + `@Valid` → llama Interactor | Lógica de negocio, repos |
| **Interactor** | Map DTO→Domain; llama UseCase; map Domain→Response | Validar con repo, persistir |
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
CreatePersonInteractorImpl.createPerson(request)
  → PersonDTOMapper.toDomain(request)          // VOs validan aquí
  → CreatePersonUseCase.createPerson(domain)
      → CreatePersonRulesValidator.validate
            → UniqueDocumentRule (tipo+número)
            → UniqueEmailRule
      → PersonRepository.savePerson(domain)
  → PersonDTOMapper.toCreatePersonResponse(domain)
```

### Ejemplo real (`CreateAccount`)

```
CreateAccountInteractorImpl.createAccount(request)
  → CreateAccountCommand(ownerId, type)   // no Domain aún: falta número
  → CreateAccountUseCase.createAccount(command)
      → AccountNumberGenerator + new AccountDomain(...)
      → CreateAccountRulesValidator.validate
            → OwnerExistsRule
            → MaxAccountsPerOwnerRule (máx. 5)
            → UniqueAccountNumberRule
      → AccountRepository.saveAccount(domain)
  → AccountDTOMapper.toCreateAccountResponse(domain)
```

### Ejemplo real (`ChangeAccountStatus`)

```
ChangeAccountStatusInteractorImpl.changeAccountStatus(accountId, request)
  → ChangeAccountStatusCommand(accountId, operation)
  → ChangeAccountStatusUseCase.changeAccountStatus(command)
      → ChangeAccountStatusRulesValidator.validate
            → AllowedAccountStatusOperationRule   // política del UC (no OPERATE)
      → findAccountById → AccountNotFoundException si vacío
      → AccountDomain.block|unblock|close → save
  → AccountDTOMapper.toChangeAccountStatusResponse(domain)
```

Not-found se resuelve en el UseCase al cargar el Domain (mismo criterio que GetAccountBalance),
no con una Rule que hace `find` y descarta el agregado.

### Rules de aplicación actuales (Módulo 1)

| Use case | Rules |
|----------|--------|
| CreatePerson | `UniqueDocumentRule`, `UniqueEmailRule` |
| CreateAccount | `OwnerExistsRule`, `MaxAccountsPerOwnerRule`, `UniqueAccountNumberRule` |
| ChangeAccountStatus | `AllowedAccountStatusOperationRule` |

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

Invariantes puros recientes (no Rule): nombre máx. 100 chars en `PersonDomain`; `AccountDomain.close()` exige saldo cero.

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
getUserMessage() / getCode()  →  GlobalExceptionHandler → JSON al cliente
```

### ¿Cuándo nueva clase de excepción?

| Crear clase nueva | Reutilizar `InvalidAccountException` / `InvalidPersonException` |
|-------------------|------------------------------------------------------------------|
| Caso de negocio distinto y el HTTP/status importa por tipo | Varios códigos “datos inválidos” del mismo módulo |
| Ej: NotFound → 404, Duplicate → 409, Blocked → 409 | Ej: `NUMBER_EMPTY`, `TYPE_REQUIRED`, `CURRENCY_MISMATCH` vía `InvalidAccountException.create(AccountError.X, args)` |

**Ya existían (invariantes de dominio):**  
`InvalidAccountException`, `InvalidPersonException`, `AccountBlockedException`, `InsufficientBalanceException`, `InvalidAccountStateException`, `InvalidAmountException`, `UnsupportedAccountStatusOperationException` (operación no admitida por el UC de cambio de estado; distinto de `INVALID_STATE`).

**Se crearon para casos de aplicación/consulta (use cases):**  
`PersonNotFoundException`, `DuplicateDocumentException`, `AccountNotFoundException`, `DuplicateAccountException`.

No hace falta una excepción por cada entrada del enum: el enum tiene **todos** los mensajes; la clase tipada solo cuando el tipo de fallo importa.

### ¿Quién lanza qué?

| Origen | Ejemplo |
|--------|---------|
| VO / entidad (puro) | `new Email(...)` → `InvalidPersonException` + `PersonError.EMAIL_INVALID` |
| RulesValidator (estado) | documento ya existe → `DuplicateDocumentException` |
| UseCase (consulta / carga vacía) | `findById` empty → `AccountNotFoundException` (GetBalance y ChangeAccountStatus) |

---

## 5. Validación en tres niveles (resumen)

| Nivel | Dónde | Mecanismo | Ejemplo |
|-------|-------|-----------|---------|
| **1** | Controller / DTO Request | Jakarta Validation | Reject body malformado |
| **2** | Domain / VO / entidad | Compact constructor, métodos de negocio | `Email`, `Money`, `account.block()` |
| **3** | Application / RulesValidator | Consulta a repositorio | Documento único, owner existe |

---

## 6. Mapeo

| Dirección | Herramienta | Por qué |
|-----------|-------------|---------|
| Domain → Response | **MapStruct** (`abstract class @Mapper`) | Casi 1:1; valor real de MapStruct |
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
│   ├── primaryports/ ...
│   ├── secondaryports/ ...
│   └── usecase/
│       ├── Rule.java                 # opcional (reutilización rara)
│       ├── RulesValidator.java
│       └── {module}/
│           ├── impl/
│           └── rulesvalidator/(+ impl/)   # validaciones con repo aquí (sin carpeta rules/)
├── infrastructure/
└── crosscutting/
```

---

## 9. Documentos relacionados

| Documento | Para qué |
|-----------|----------|
| [`ARCHITECTURE_DECISIONS.md`](./ARCHITECTURE_DECISIONS.md) | Decisiones, convenciones, patrones detallados |
| [`IMPLEMENTATION_GUIDE.md`](./IMPLEMENTATION_GUIDE.md) | Guía paso a paso para nuevas funcionalidades |
| [`ROADMAP.md`](./ROADMAP.md) | Estado de módulos y subtareas |
| [`ISSUE_TEMPLATE.md`](./ISSUE_TEMPLATE.md) | Plantilla para issues de GitHub |
