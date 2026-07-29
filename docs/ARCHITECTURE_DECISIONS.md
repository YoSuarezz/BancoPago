# Decisiones de Arquitectura — BancoPago

*Versión: 2.0*
*Última actualización: Julio 2026*
*Idioma: Español (documentación), columnas BD en inglés, mensajes de error en español*

---

## 1. Estilo de Arquitectura

**Clean Architecture + Hexagonal (Ports & Adapters)** con stack reactivo (WebFlux + R2DBC).

### Regla de Dependencia

```
crosscutting  →  domain  →  application  →  infrastructure
     ↑                      ↓                     ↑
     └── helpers,      use cases,            REST, R2DBC,
         ErrorCode,     ports, DTOs,          Redis, config
         exceptions     mappers
```

- **domain** → Cero dependencias. Java 21 puro. Sin Spring, sin R2DBC, sin HTTP.
- **application** → Solo depende de domain.
- **infrastructure** → Implementa puertos de application + domain.
- **crosscutting** → Helpers, excepciones base, ErrorCode. Disponible para todas las capas.

### Elección del Stack Reactivo

| Aspecto | Elección | Por qué |
|---------|----------|---------|
| Framework web | Spring WebFlux | IO no bloqueante, soporte SSE, alta concurrencia |
| Acceso a BD | R2DBC (PostgreSQL) | SQL reactivo — sin hilos bloqueados en consultas |
| Documentación API | Springdoc OpenAPI | Compatible con reactivo |
| Migraciones BD | Flyway | SQL versionado y auditable |
| Caché / idempotencia | Redis Reactive | TTL, búsquedas rápidas |
| Tolerancia a fallos | Resilience4j Reactor | Circuit breaker, retry para flujos reactivos |
| Mapeo | MapStruct (Domain→Response) + manual (Request→Domain rico, Entity↔Domain) | MapStruct donde hay 1:1; manual con VOs/herencia |
| Testing | JUnit 5 + Mockito + StepVerifier + Testcontainers | Testing de streams reactivos |

---

## 2. Estructura del Proyecto

```
backend/src/main/java/com/bancopago/backend/
├── BackendApplication.java
│
├── crosscutting/                         # Infraestructura compartida
│   ├── exception/
│   │   ├── ErrorCode.java                # Interfaz: getCode(), getMessageTemplate()
│   │   ├── Layer.java                    # Enum: DOMAIN, APPLICATION, INFRASTRUCTURE
│   │   └── DomainException.java          # Excepción base abstracta
│   └── helpers/
│       ├── TextHelper.java               # applyTrim(), isBlank(), truncate()
│       └── ObjectHelper.java             # getDefault(), requireNonNull()
│
├── domain/                               # Lógica de negocio pura
│   ├── account/
│   │   ├── AccountError.java             # Códigos de error en español
│   │   ├── AccountDomain.java            # Entidad
│   │   ├── vo/
│   │   │   ├── AccountNumber.java        # Value Object (record)
│   │   │   └── Money.java                # Value Object (record)
│   │   └── exceptions/                   # Excepciones de dominio
│   ├── person/
│   │   ├── PersonError.java              # Códigos de error en español
│   │   ├── PersonDomain.java             # Entidad abstracta
│   │   ├── ClientDomain.java             # extends PersonDomain
│   │   ├── EmployeeDomain.java           # extends PersonDomain
│   │   ├── vo/
│   │   │   ├── Email.java                # Value Object (record)
│   │   │   └── DocumentNumber.java       # Value Object (record)
│   │   └── exceptions/
│   ├── enums/
│   │   ├── PersonType.java
│   │   ├── DocumentType.java
│   │   ├── AccountType.java
│   │   ├── AccountStatus.java
│   │   └── Currency.java
│   └── BaseDomain.java                   # Clase base UUID
│
├── application/                          # Casos de uso y puertos
│   ├── primaryports/
│   │   ├── dto/{module}/
│   │   │   ├── request/                  # Jakarta Validation
│   │   │   └── response/                 # sin fromDomain(); mapea el DTOMapper
│   │   ├── interactor/{module}/(+ impl/) # Puerto de entrada (Controller → aquí)
│   │   └── mapper/{module}/              # MapStruct DTO ↔ Domain
│   ├── secondaryports/
│   │   ├── entity/                       # Entidades R2DBC @Table
│   │   ├── repository/                   # Puertos de repositorio reactivos
│   │   └── mapper/                       # Mapeo manual Entity ↔ Domain
│   └── usecase/
│       ├── Rule.java                     # Regla granular (Mono<Void>)
│       ├── RulesValidator.java
│       ├── UseCaseWithReturn.java        # base genérica técnica
│       └── {module}/
│           ├── {UseCase}.java + impl/
│           └── rulesvalidator/(+ impl/ + rules/)  # RulesValidator; Rule en rules/
│
├── infrastructure/
│   ├── primaryadapters/
│   │   ├── controller/{module}/          # PersonController, AccountController (@Valid + ApiResponse)
│   │   └── adapter/response/             # Response / ApiResponse (envelope HTTP)
│   ├── secondaryadapters/
│   │   ├── config/                       # SecurityConfig (permitAll local; JWT después)
│   │   └── r2dbc/
│   │       ├── {module}/                 # Adapter + Spring Data repo
│   │       └── config/                   # Persistable callbacks, etc.
│   ├── GlobalExceptionHandler.java       # DomainException → HTTP status
│   ├── GlobalExceptionHandler.java       # mapeo DomainException → HTTP + ErrorResponse
│   ├── ErrorResponse.java                # { code, message, messages }
│   └── ResponseMessages.java             # constantes mensajes de éxito (español)
│
└── resources/
    └── db/migration/                     # Flyway SQL (V1 schema, V2 subtype fields, V3 document unique)
```

---

## 3. Patrones Clave

### 3.1 Value Objects (`record`)

Cada concepto de dominio que tiene validación o comportamiento es un **record de Java 21** en `domain/{module}/vo/`.

**Reglas:**
- El compact constructor valida todos los invariantes
- Usa `TextHelper.isBlank()` / `ObjectHelper.getDefault()` — nunca `if (x == null)` directo
- Los mensajes de error vienen del enum `{Module}Error` del módulo — nunca hardcodeados
- Operaciones de negocio como métodos del record (`Money.add()`, `Money.subtract()`)

```java
public record AccountNumber(String value) {
    public AccountNumber {
        value = TextHelper.applyTrim(value);
        if (TextHelper.isBlank(value))
            throw InvalidAccountException.create(AccountError.NUMBER_EMPTY);
    }
}
```

```java
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0)
            throw InvalidAmountException.create(amount);
        currency = ObjectHelper.getDefault(currency, Currency.COP);
    }
    public Money add(Money other) { /* chequeo de moneda + suma */ }
    public Money subtract(Money other) { /* chequeo de moneda + saldo + resta */ }
}
```

### 3.2 Manejo de Errores

**Enums de error por módulo** implementando `ErrorCode`, con **mensajes en español** (para el usuario):

```
crosscutting/exception/
├── ErrorCode.java            # getCode() + getMessageTemplate()
├── Layer.java                # DOMAIN, APPLICATION, INFRASTRUCTURE
└── DomainException.java      # Abstracto: ErrorCode + Layer + args
```

```java
// domain/account/AccountError.java
public enum AccountError implements ErrorCode {
    NUMBER_EMPTY("El número de cuenta no puede estar vacío"),
    INSUFFICIENT_BALANCE("Saldo insuficiente: actual=%s, requerido=%s");

    @Override public String getCode() { return "ACCOUNT_" + name(); }
    @Override public String getMessageTemplate() { return messageTemplate; }
}
```

**Jerarquía de excepciones (cada módulo tiene sus propias excepciones):**
```
DomainException
├── InvalidPersonException
├── InvalidAccountException
├── AccountBlockedException
├── InsufficientBalanceException
├── InvalidAccountStateException
└── InvalidAmountException
```

Cada excepción usa **constructor privado + método factory estático `create()`**:

```java
public class AccountBlockedException extends DomainException {
    private static final long serialVersionUID = 1L;

    private AccountBlockedException(UUID accountId) {
        super(AccountError.BLOCKED, Layer.DOMAIN, accountId);
    }
    public static AccountBlockedException create(UUID accountId) {
        return new AccountBlockedException(accountId);
    }
}
```

**`getUserMessage()`** provee el mensaje en español formateado para las respuestas de API.

### 3.3 Validación en Tres Niveles (puro vs con estado)

| Nivel | Capa | Mecanismo | Propósito |
|-------|------|-----------|-----------|
| **1** | Infraestructura (Controller) | Jakarta Validation (`@NotBlank`, `@Email`, `@NotNull`) | Rechazar entrada malformada antes de la lógica de negocio |
| **2** | Dominio (VO / entidad) — **reglas puras** | Compact constructor, métodos de negocio | Formato, rangos, transiciones (`block()`); **sin** repositorio |
| **3** | Aplicación (RulesValidator / `Rule<T>`) — **reglas con estado** | Acceso a repositorio | Unicidad, existencia (“¿está en BD?”) |

**Separación deliberada vs guías tipo AgroSync:** allí el `RulesValidator` llama tanto a `PesoValidoRule` (puro) como a `IdentificadorExisteRule` (repo), y las interfaces viven en `domain/.../rules/`. Aquí:

- Lo **puro** no se modela como `*Rule` en dominio: ya son VOs / métodos de entidad.
- Lo **con estado** vive solo en aplicación (`Rule` / `RulesValidator`). Nunca en `domain/`.
- El `RulesValidator` **no** revalida VOs; solo compone reglas con estado.

### 3.4 Mapeo (Estrategia Híbrida)

| Dirección | Herramienta | Razón |
|-----------|-------------|-------|
| **Domain → Response** | **MapStruct** (`abstract class @Mapper`) | Mapeo casi 1:1; renombres/`ignore`; crece con más endpoints |
| **Request → Domain** (VOs/herencia) | Método **concreto** en el mismo `*DTOMapper` | Actúa como factory de aplicación; MapStruct no genera bien Client/Employee + VOs |
| **Entity ↔ Domain** | **Manual** (`@Component`) | VOs, `PersonDomain` abstracta, enums como `String` |

**No** forzar MapStruct en Entity↔Domain ni en Request→Domain rico.  
**No** adoptar Assembler/Factory como convención global: solo si un Response junta varios agregados (`*ResponseAssembler`) o el `toPersonDomain` de Person crece demasiado (`PersonDomainFactory` en application).

**Ejemplo de mapper Entity (manual, maneja VOs):**
```java
public AccountDomain toAccountDomain(AccountEntity entity) {
    return new AccountDomain(
        entity.getId(),
        entity.getOwnerId(),
        new AccountNumber(entity.getAccountNumber()),
        mapAccountType(entity.getType()),
        new Money(entity.getBalance(), mapCurrency(entity.getCurrency())),
        mapAccountStatus(entity.getStatus())
    );
}
```

### 3.5 Patrón de Entidad (R2DBC)

```java
@Table("account")
public class AccountEntity implements Persistable<UUID> {
    @Id private UUID id;
    @Column("owner_id") private UUID ownerId;
    @Column("account_number") private String accountNumber;
    // ...

    public AccountEntity() {
        setId(UUID.randomUUID());
        setAccountNumber(TextHelper.EMPTY);
        // todos los campos inicializados con helpers
    }

    public static AccountEntity create(UUID id, UUID ownerId, ...) { ... }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = TextHelper.applyTrim(accountNumber);
    }
}
```

### 3.6 Patrón Interactor / Use Case / RulesValidator

```
Controller (@Valid DTO)
  → Interactor          map DTO → Domain; llama UseCase; map Domain → Response
    → UseCase           RulesValidator (estado) + lógica de dominio + Repository (Domain)
        → RulesValidator    solo validaciones CON ESTADO (repos)
        → Repository        puerto; Adapter hace Domain ↔ Entity
```

| Pieza | Responsabilidad |
|-------|-----------------|
| **Interactor** | DTO ↔ Domain (mapper) + delegar al UseCase. No valida con repo ni persiste. |
| **UseCase** | Recibe/devuelve **Domain**. Valida (RulesValidator) → opera → `repository.save/find`. |
| **RulesValidator** | Solo reglas con estado (unicidad, existencia). Inline con repos. |
| **Repository** | Puerto Domain in/out. **No** Entity en UseCase. |

**¿Por qué Interactor e UseCase parecen “duplicados”?**  
En Clean Architecture clásica *Interactor* y *UseCase* son el mismo concepto. Aquí se separan a propósito:

- `*Interactor` = contrato estable hacia afuera (Controller / adapters primarios).
- `*UseCase` + `*UseCaseImpl` = lógica de aplicación.
- `*InteractorImpl` es un adaptador delgado (hoy solo delega; mañana puede sumar tracing/métricas sin tocar el use case).

**Convención de este proyecto (patrón guía):** la interfaz concreta es **vacía** y solo extiende la base; el nombre de la clase expresa la operación; la impl implementa `execute`:

```java
public interface CreatePersonUseCase
        extends UseCaseWithReturn<PersonDomain, PersonDomain> {
}

@Service
public class CreatePersonUseCaseImpl implements CreatePersonUseCase {
    @Override
    public Mono<PersonDomain> execute(PersonDomain person) { ... }
}
```

Lo mismo aplica a Interactors con `InteractorWithReturn` (DTO o identidad in/out).  
Operaciones de estado de cuenta son use cases separados (`BlockAccount`, `UnblockAccount`, `CloseAccount`), cada uno con `UUID` de entrada.

### 3.6.1 RulesValidator orquesta `Rule`s con estado

Cada validación con repositorio = **interfaz** `extends Rule<T>` + **impl** con el repo. El `*RulesValidatorImpl` solo las ejecuta:

```java
// rules/OwnerExistsRule.java
public interface OwnerExistsRule extends Rule<UUID> {
}

// rules/impl/OwnerExistsRuleImpl.java
@Component
public class OwnerExistsRuleImpl implements OwnerExistsRule {
    private final PersonRepository personRepository;

    @Override
    public Mono<Void> validate(UUID ownerId) {
        return personRepository.findPersonById(ownerId)
            .switchIfEmpty(Mono.error(PersonNotFoundException.create(ownerId)))
            .then();
    }
}

// CreateAccountRulesValidatorImpl — solo orquesta
@Override
public Mono<Void> validate(AccountDomain account) {
    return ownerExistsRule.validate(account.getOwnerId())
        .then(maxAccountsPerOwnerRule.validate(account.getOwnerId()))
        .then(uniqueAccountTypePerOwnerRule.validate(account))
        .then(uniqueAccountNumberRule.validate(account.getNumber()));
}
```

Ubicación: `rulesvalidator/rules/` (interfaces) + `rules/impl/` (implementaciones). Nunca en `domain/`.

Rules Módulo 1: `UniqueDocumentRule`, `UniqueEmailRule`, `OwnerExistsRule`, `MaxAccountsPerOwnerRule`, `UniqueAccountTypePerOwnerRule`, `UniqueAccountNumberRule`.
Block/Unblock/Close no usan RulesValidator: cargan la cuenta, aplican el método de dominio y guardan.

Invariantes de dominio (Person): `clientNumber` obligatorio en `ClientDomain`; `position` + `area` obligatorios en `EmployeeDomain`.

**Pendiente (necesita definición de producto):** restricciones de `AccountType` por `PersonType`; operaciones sobre estado `SEIZED`; si cuentas `INACTIVE` cuentan al cupo de 5.

### 3.7 Patrón de Use Case (Reactivo) — ejemplo

```java
public interface CreateAccountUseCase
        extends UseCaseWithReturn<AccountDomain, AccountDomain> {
}

@Service
public class CreateAccountUseCaseImpl implements CreateAccountUseCase {

    private final AccountRepository accountRepository;
    private final CreateAccountRulesValidator rulesValidator;

    @Override
    public Mono<AccountDomain> execute(AccountDomain account) {
        return rulesValidator.validate(account)
            .then(Mono.defer(() -> accountRepository.saveAccount(account)));
    }
}
```

El número de cuenta se genera en el **Interactor** (arma `AccountDomain` completo) antes de `useCase.execute(domain)`.

### 3.8 MapStruct DTO (en el Interactor)

- El **Interactor** usa `*DTOMapper`: Request→Domain (si aplica) y Domain→Response.
- Domain→Response 1:1 puro → `interface` MapStruct (`AccountDTOMapper`).
- Request→Domain con herencia/VOs → `abstract class` con método concreto (`PersonDTOMapper.toPersonDomain`).
- CreateAccount: Interactor genera número + `new AccountDomain`; mapper de Account solo Domain→Response.
- El **UseCase** no importa DTOs ni mappers de DTO.
- Entity↔Domain sigue manual en el Adapter.

---

## 4. Convenciones

### 4.1 Idioma

| Contexto | Idioma | Ejemplo |
|----------|--------|---------|
| Código Java (clases, métodos, vars, entities) | Inglés | `AccountDomain`, `getAccountBalance()`, `ownerId` |
| Documentación (`.md` files) | **Español** | Todos los archivos .md |
| Nombres de tablas y columnas BD | Inglés | `person`, `account`, `account_number`, `document_type` |
| Valores de enums en BD / dominio | Inglés | `CLIENT`, `ACTIVE`, `SAVINGS` |
| Mensajes de error al usuario (`*Error`, excepciones) | Español | `"El número de cuenta no puede estar vacío"` |

### 4.2 Nomenclatura

| Elemento | Convención | Ejemplo |
|----------|------------|---------|
| Entidad de dominio | PascalCase + `Domain` | `AccountDomain` |
| Value Object | PascalCase (record) | `AccountNumber`, `Money` |
| Enum | PascalCase | `AccountStatus`, `AccountError` |
| Excepción | PascalCase + `Exception` | `AccountBlockedException` |
| Enum ErrorCode | PascalCase + `Error` | `AccountError` |
| Interfaz Use Case | PascalCase + `UseCase` | `CreateAccountUseCase` |
| Impl Use Case | PascalCase + `Impl` | `CreateAccountUseCaseImpl` |
| Interfaz RulesValidator | PascalCase + `RulesValidator` | `CreateAccountRulesValidator` |
| Repositorio (puerto entrada) | PascalCase + `Interactor` | `CreateAccountInteractor` |
| Repositorio (puerto salida) | PascalCase + `Repository` | `AccountRepository` |
| Entidad R2DBC | PascalCase + `Entity` | `AccountEntity` |
| Adaptador R2DBC | PascalCase + `Adapter` | `AccountR2dbcAdapter` |
| Controlador | PascalCase + `Controller` | `AccountController` |
| DTO Request | PascalCase + `Request` | `CreateAccountRequest` |
| DTO Response (caso de uso) | PascalCase + operación + `Response` | `CreateAccountResponse` |
| Envelope HTTP | `Response` / `ApiResponse` | `ApiResponse.of(dto)` / `ApiResponse.of(dto, ResponseMessages.X)` |
| Paquetes | minusculas.singular | `domain.account`, `application.usecase` |
| Métodos de repositorio (puerto) | `verb` + recurso + criterio | `savePerson`, `findAccountByNumber`, `existsPersonByDocument`, `findAccountsByOwnerId` |
| Métodos de controller | `verb` + recurso (+ criterio) | `createPerson`, `getAccountBalance`, `blockAccount` |
| Métodos Entity↔Domain mapper | `to{Resource}Entity` / `to{Resource}Domain` | `toAccountEntity`, `toPersonDomain` |
| Métodos DTO mapper | `to{Resource}Domain` / `to{UseCase}Response` | `toPersonDomain`, `toCreateAccountResponse` |
| Interfaces use case / interactor | PascalCase + operación | `CreateAccountUseCase`, `CreateAccountInteractor` |
| Método UseCase / Interactor | `execute` (contrato de la base) | `useCase.execute(domain)`, `interactor.execute(request)` |
| Métodos de dominio en la entidad | verbo corto (el receptor ya es el recurso) | `account.block()`, `account.close()` |
| Factory de excepciones / entities | `create(...)` | `PersonNotFoundException.create(id)` |
| Constantes | UPPER_SNAKE_CASE | `MAX_CONCURRENCY`, `DEFAULT_CURRENCY` |

### 4.2.1 Rutas HTTP (REST resource-oriented)

| Regla | Aplicación en BancoPago |
|-------|-------------------------|
| **Sustantivos** para recursos (no verbos en el path) | `/persons`, `/accounts` — no `/createPerson` |
| **Plural** para colecciones | `/api/v1/persons`, `/api/v1/accounts` |
| El **verbo HTTP** lleva la acción CRUD | `POST` crear, `GET` leer |
| Sub-recurso para consultas | `GET /accounts/{id}/balance` |
| Acción de negocio no-CRUD (transición de estado) | `POST /accounts/{id}/block` (mismo patrón para unblock/close) |
| Inglés, kebab/segmentos en minúscula | `/api/v1/...` |

No usar un único `PATCH /accounts/{id}/estado`: cada transición es un use case y un endpoint propios.

### 4.3 Estilo de Código

- **Sin Lombok.** Constructores, getters, setters manuales.
- **Excepciones:** constructor privado + static `create()`.
- **Entidades:** constructor por defecto antinull + factories estáticas `create()`.
- **Helpers:** `TextHelper.applyTrim()`, `ObjectHelper.getDefault()`, `ObjectHelper.requireNonNull()`.
- **MapStruct** preferente para Domain→Response. Request→Domain rico y Entity↔Domain: manual. Sin Assembler/Factory globales por defecto.
- **Sin `if (x == null)`** — usar `TextHelper.isBlank()` o `ObjectHelper.requireNonNull()`.
- **UseCase / Interactor:** interfaz vacía que extiende la base; impl con `execute`. Repos concretos mantienen métodos explícitos (`saveAccount`, `findAccountById`).
- **RulesValidator** concentra reglas con acceso a repositorio; el UseCase solo orquesta.
- **Comentarios:** solo cuando aportan el *porqué* (no narrar el código). En español.

---

## 5. Estrategia de Testing

| Capa | Herramienta | Enfoque |
|------|-------------|---------|
| Dominio | JUnit 5 | Reglas de negocio puras, sin mocks, sin Spring |
| Aplicación | JUnit 5 + Mockito + StepVerifier | Orquestación de use cases con puertos mockeados |
| Infraestructura | WebTestClient + Testcontainers | Endpoints REST + integración R2DBC |

**Tests de dominio (sin Spring):**
```java
class AccountTest {
    @Test
    void shouldBlockActiveAccount() {
        var account = new AccountDomain(OWNER_ID, new AccountNumber("001-123"), AccountType.SAVINGS);
        account.block();
        assertEquals(AccountStatus.BLOCKED, account.getStatus());
    }
}
```

**Tests de aplicación (con StepVerifier):**
```java
@ExtendWith(MockitoExtension.class)
class AccountUseCaseTest {
    @Mock AccountRepository accountRepository;
    @Mock CreateAccountRulesValidator createAccountRulesValidator;

    @Test
    void shouldCreateAccountWhenOwnerExists() {
        when(createAccountRulesValidator.validate(any(AccountDomain.class))).thenReturn(Mono.empty());
        when(accountRepository.saveAccount(any(AccountDomain.class)))
            .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        var account = new AccountDomain(ownerId, new AccountNumber("1234567890"), AccountType.SAVINGS);
        StepVerifier.create(useCase.execute(account))
            .assertNext(result -> assertEquals("1234567890", result.getNumber()))
            .verifyComplete();
    }
}
```

---

## 6. Cómo Generar Issues desde Este Documento

Cada implementación de funcionalidad se mapea a un slice vertical a través de las capas:

```
  User Story
      │
      ▼
  ┌────────────────────────────────────────────┐
  │  Issue Template (docs/ISSUE_TEMPLATE.md)    │
  │                                            │
  │  1. crosscutting/  (helpers, si se necesita)│
  │  2. domain/        (VOs, Entity, Error,    │
  │                     Exceptions)            │
  │  3. application/   (DTOs, Use Cases,       │
  │                     RulesValidators,        │
  │                     Repository interfaces, │
  │                     Mappers)               │
  │  4. infrastructure/ (Controller, Adapter,  │
  │                     GlobalHandler)         │
  │  5. tests/         (Domain + Use Case +    │
  │                     Integration)           │
  └────────────────────────────────────────────┘
```

**Paso a paso:**

1. Identifica el **módulo** (`account`, `person`, `payment`, `payroll`)
2. Abre `docs/ISSUE_TEMPLATE.md`
3. Copia la sección **📝 Plantilla para Nuevos Issues**
4. Completa los `{{ }}` para cada capa según lo que necesite la funcionalidad
5. Crea el Issue en GitHub
6. Implementa capa por capa, marcando los items del checklist
7. Actualiza `docs/ROADMAP.md` al terminar

**Ejemplo concreto — estado actual:**

Módulo 1 (Person + Account): domain → Controllers REST (`/api/v1/persons`, `/api/v1/accounts` + block/unblock/close), `GlobalExceptionHandler` y `SecurityConfig` (permitAll local) están implementados. Pendiente:

| Issue | Capas Necesarias |
|-------|------------------|
| SSE balance stream | application/usecase + infrastructure/controller |
| Angular dashboard | frontend/ |
| JWT real | infrastructure/secondaryadapters/config |
