# Decisiones de Arquitectura — BancoPago

*Versión: 2.0*
*Última actualización: Julio 2026*
*Idioma: Español (documentación), columnas BD en español, mensajes de error en español*

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
| Mapeo | MapStruct (DTO↔Domain) + Manual (Entity↔Domain) | Generación en compilación; manual para español→inglés |
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
│   ├── BaseDomain.java                   # Clase base UUID
│   └── DomainRule.java                   # @FunctionalInterface
│
├── application/                          # Casos de uso y puertos
│   ├── primaryports/
│   │   ├── dto/{module}/
│   │   │   ├── request/                  # Anotaciones Jakarta Validation
│   │   │   └── response/                 # con fromDomain()
│   │   ├── interactor/{module}/          # Interfaces de puerto de entrada
│   │   └── mapper/{module}/              # MapStruct DTO ↔ Domain
│   ├── secondaryports/
│   │   ├── entity/{module}/              # Entidades R2DBC @Table (columnas español)
│   │   ├── repository/                   # Interfaces de repositorio reactivas
│   │   └── mapper/{module}/              # Mapeo manual Entity ↔ Domain
│   └── usecase/{module}/
│       ├── {UseCase}.java                # Interfaz
│       ├── impl/                         # Implementación reactiva
│       └── rulesvalidator/               # Reglas de negocio (composición DomainRule)
│
├── infrastructure/
│   ├── primaryadapters/
│   │   ├── controller/{module}/          # Controladores REST con @Valid
│   │   └── adapter/response/             # Wrapper GenericResponse
│   ├── secondaryadapters/
│   │   └── config/                       # Beans, seguridad, etc.
│   └── GlobalExceptionHandler.java       # DomainException → HTTP status
│
└── resources/
    └── db/migration/                     # Flyway SQL (nombres de tablas en español)
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

### 3.3 Validación en Tres Niveles

| Nivel | Capa | Mecanismo | Propósito |
|-------|------|-----------|-----------|
| **1** | Infraestructura (Controller) | Jakarta Validation (`@NotBlank`, `@Email`, `@NotNull`) | Rechazar entrada malformada antes de la lógica de negocio |
| **2** | Dominio (VO) | Compact constructor del `record` | Garantizar que el concepto siempre sea válido |
| **3** | Aplicación (Use Case) | `DomainRule<T>` con acceso a repositorio | Reglas que requieren estado del sistema (unicidad, existencia) |

### 3.4 Mapeo (Estrategia Híbrida)

| Mapeo | Herramienta | Razón |
|-------|-------------|-------|
| Entity ↔ Domain | **Manual** (Spring `@Component`) | Nombres BD en español (`numero`→`number`), `PersonDomain` abstracta |
| DTO ↔ Domain | **MapStruct** (`@Mapper(componentModel = "spring")`) | Mismos nombres de campo, sin herencia, seguridad en compilación |

**Ejemplo de mapper Entity (manual, maneja VOs):**
```java
public AccountDomain toDomain(AccountEntity entity) {
    return new AccountDomain(
        entity.getId(),
        entity.getPersonaId(),
        new AccountNumber(entity.getNumero()),           // VO desde String
        mapAccountType(entity.getTipo()),
        new Money(entity.getSaldo(), mapCurrency(entity.getMoneda())),  // VO desde datos crudos
        mapAccountStatus(entity.getEstado())
    );
}
```

### 3.5 Patrón de Entidad (R2DBC)

```java
@Table("cuenta")
public class AccountEntity implements Persistable<UUID> {
    @Id private UUID id;
    @Column("persona_id") private UUID personaId;
    private String numero;
    // ...

    public AccountEntity() {
        setId(UUID.randomUUID());
        setNumero(TextHelper.EMPTY);
        // todos los campos inicializados con helpers
    }

    // Factory estáticos
    public static AccountEntity create(UUID id, UUID personaId, ...) { ... }

    // Getters/setters con helpers
    public void setNumero(String numero) { this.numero = TextHelper.applyTrim(numero); }
    public void setSaldo(BigDecimal saldo) { this.saldo = ObjectHelper.getDefault(saldo, BigDecimal.ZERO); }
}
```

### 3.6 Patrón de Use Case (Reactivo)

```java
@Service
public class CreateAccountUseCaseImpl implements CreateAccountUseCase {

    private final AccountRepository accountRepository;
    private final PersonRepository personRepository;
    private final CreateAccountRulesValidator rulesValidator;
    private final AccountEntityMapper entityMapper;

    public Mono<AccountResponse> createAccount(CreateAccountRequest request) {
        return personRepository.findPersonById(request.ownerId())
            .switchIfEmpty(Mono.error(new PersonNotFoundException(request.ownerId())))
            .flatMap(person -> {
                var domain = new AccountDomain(
                    request.ownerId(),
                    new AccountNumber(request.number()),
                    request.type()
                );
                return rulesValidator.validate(domain)
                    .then(accountRepository.saveAccount(domain))
                    .map(AccountResponse::fromDomain);
            });
    }
}
```

---

## 4. Convenciones

### 4.1 Idioma

| Contexto | Idioma | Ejemplo |
|----------|--------|---------|
| Código Java (clases, métodos, vars, entities) | Inglés | `AccountDomain`, `getBalance()`, `ownerId` |
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
| DTO Response | PascalCase + `Response` | `AccountResponse` |
| Paquetes | minusculas.singular | `domain.account`, `application.service` |
| Métodos de repositorio (puerto) | `verb` + `Entity` + criterio | `savePerson`, `findAccountByNumber`, `existsPersonByDocument`, `findAccountsByOwnerId` |
| Métodos de use case / interactor (API concreta) | `verb` + sujeto de negocio | `createAccount`, `getAccountBalance`, `blockAccount`, `changeAccountStatus` |
| Métodos genéricos técnicos (`UseCase*` / `Interactor*`) | `execute` solo en interfaces genéricas | No exponer `execute` como API pública del use case concreto |
| Constantes | UPPER_SNAKE_CASE | `MAX_CONCURRENCY`, `DEFAULT_CURRENCY` |

### 4.3 Estilo de Código

- **Sin Lombok.** Constructores, getters, setters manuales.
- **Excepciones:** constructor privado + static `create()`.
- **Entidades:** constructor por defecto antinull + factories estáticas `create()`.
- **Helpers:** `TextHelper.applyTrim()`, `ObjectHelper.getDefault()`, `ObjectHelper.requireNonNull()`.
- **MapStruct** solo para DTO↔Domain. Entity↔Domain siempre manual (VOs + herencia).
- **Sin `if (x == null)`** — usar `TextHelper.isBlank()` o `ObjectHelper.requireNonNull()`.
- **Nombres de métodos explícitos:** en puertos de repositorio y use cases concretos, el nombre debe decir *qué* se hace y *sobre qué* / *por qué criterio* (`saveAccount`, `findPersonByDocument`, `createAccount`). Evitar `save`, `findById` o `execute` ambiguos en la API pública.

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
class CreateAccountUseCaseTest {
    @Mock AccountRepository accountRepository;
    @Mock PersonRepository personRepository;
    @InjectMocks CreateAccountUseCaseImpl useCase;

    @Test
    void shouldCreateAccountWhenValid() {
        when(personRepository.findPersonById(any())).thenReturn(Mono.just(client));
        StepVerifier.create(useCase.createAccount(request))
            .assertNext(response -> assertEquals("001-123", response.number()))
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

El primer módulo (Cuentas y Usuarios) está completo hasta las capas 1-4 (crosscutting → domain → application/secondaryports → infrastructure/entities). Lo que falta:

| Issue | Capas Necesarias |
|-------|------------------|
| `CreateAccount` use case + controller | application/usecase + infrastructure/controller |
| `Block/Unblock/Close` account endpoints | application/usecase + infrastructure/controller |
| SSE balance stream | application/usecase + infrastructure/controller |
| Angular dashboard | frontend/ |
