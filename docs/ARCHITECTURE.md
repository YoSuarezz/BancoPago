# Arquitectura — BancoPago

## Tabla de Contenidos
1. [Estilo Arquitectónico](#estilo-arquitectónico)
2. [Por qué Reactivo (WebFlux + R2DBC)](#por-qué-reactivo-webflux--r2dbc)
3. [Stack Tecnológico](#stack-tecnológico)
4. [Estructura del Proyecto](#estructura-del-proyecto)
5. [Responsabilidades de Capas](#responsabilidades-de-capas)
6. [Capa de Dominio en Detalle](#capa-de-dominio-en-detalle)
7. [Capa de Aplicación en Detalle](#capa-de-aplicación-en-detalle)
8. [Capa de Infraestructura en Detalle](#capa-de-infraestructura-en-detalle)
9. [Flujo de Datos Reactivo](#flujo-de-datos-reactivo)
10. [Patrones de Diseño](#patrones-de-diseño)
11. [Principios SOLID Aplicados](#principios-solid-aplicados)
12. [Estrategia de Manejo de Errores](#estrategia-de-manejo-de-errores)
13. [Estrategia de Testing](#estrategia-de-testing)
14. [Decisiones Arquitectónicas Clave](#decisiones-arquitectónicas-clave)

---

## Estilo Arquitectónico

El sistema sigue **Clean Architecture** (también conocida como Arquitectura Hexagonal o Puertos y Adaptadores) con una **regla de dependencia estricta**: las capas externas dependen de las internas, nunca al revés.

```
┌──────────────────────────────────────────────────────────────┐
│                   CAPA DE INFRAESTRUCTURA                     │
│  ┌──────────────────┐    ┌──────────────────┐                │
│  │  Adaptadores      │    │  Adaptadores     │               │
│  │  Primarios        │    │  Secundarios     │               │
│  │  (Controllers     │    │  (R2DBC, Redis,  │               │
│  │   REST, SSE)      │    │   Clientes HTTP) │               │
│  └────────┬─────────┘    └────────┬─────────┘                │
│           │                       │                          │
│           ▼                       ▼                          │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │              CAPA DE APLICACIÓN (PUERTOS)                 │ │
│  │  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐ │ │
│  │  │  Casos de Uso│  │  DTOs        │  │  Mappers         │ │ │
│  │  │ (Orquestación)│  │ (Entrada/    │  │  (DTO↔Domain)    │ │ │
│  │  │              │  │  Salida)     │  │                  │ │ │
│  │  └─────────────┘  └──────────────┘  └──────────────────┘ │ │
│  │                       │                                   │ │
│  │                       ▼                                   │ │
│  │  ┌──────────────────────────────────────────────────────┐ │ │
│  │  │   GATEWAYS (Interfaces de Repositorio)                │ │ │
│  │  │   (Indirección para inversión de dependencias)       │ │ │
│  │  └──────────────────────────────────────────────────────┘ │ │
│  └──────────────────────────────────────────────────────────┘ │
│                              │                                 │
│                              ▼                                 │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │                  CAPA DE DOMINIO                           │ │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌─────────────┐  │ │
│  │  │ Entidades │ │ Value    │ │Exceptiones│ │ DomainError │  │ │
│  │  │ (Agregados)│ │ Objects  │ │ de Dominio│ │ (Catálogo   │  │ │
│  │  │          │ │          │ │          │ │  de Errores)│  │ │
│  │  └──────────┘ └──────────┘ └──────────┘ └─────────────┘  │ │
│  └──────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
```

### Regla de Dependencias
- **Dominio** → Cero dependencias. Java puro. Sin Spring, R2DBC, HTTP.
- **Aplicación** → Depende solo del Dominio.
- **Infraestructura** → Depende del Dominio y la Aplicación (implementa puertos).

---

## Por qué Reactivo (WebFlux + R2DBC)

### El Problema
Un sistema bancario debe manejar:
- Miles de actualizaciones de saldo concurrentes
- Streams SSE en tiempo real para dashboards
- Procesamiento batch de nómina (5000+ empleados)
- Llamadas HTTP no bloqueantes a servicios externos (PSE mock)

Con Spring MVC tradicional (thread-per-request), cada operación concurrente consume un hilo del SO. Bajo carga, el agotamiento del thread pool degrada la latencia.

### La Solución
- **Spring WebFlux** (Project Reactor) usa un modelo event-loop: un número fijo de hilos pequeños maneja miles de peticiones concurrentes.
- **R2DBC** provee acceso reactivo y no bloqueante a la BD — ningún hilo espera por un resultado de BD.
- **`Mono<T>`** representa un valor async único (ej: consulta de cuenta).
- **`Flux<T>`** representa un stream de valores async (ej: eventos SSE, procesamiento batch).

### Comparación

| Aspecto | Spring MVC (Bloqueante) | Spring WebFlux (Reactivo) |
|---------|------------------------|--------------------------|
| Modelo de hilos | 1 hilo por petición | Event loop (pocos hilos) |
| Acceso a BD | JDBC (bloqueante) | R2DBC (reactivo) |
| Usuarios concurrentes | Limitado por thread pool | Miles con pocos hilos |
| Streaming | Polling o WebSocket | SSE via `Flux<ServerSentEvent>` |
| Batch | Loop síncrono | `Flux.flatMap(concurrency=N)` |

---

## Stack Tecnológico

```
Backend
├── Java 21
├── Spring Boot 3.x (WebFlux)
├── Spring Security (JWT + Roles)
├── R2DBC + PostgreSQL 16
├── Flyway (Migraciones de BD)
├── Redis Reactive (Idempotencia, Caché)
├── Resilience4j (Circuit Breaker, Retry)
├── Springdoc OpenAPI (Documentación API)
├── MapStruct (Mapeo de Objetos — opcional)
└── Helpers (TextHelper, ObjectHelper — antinulos manuales)

Frontend
├── Angular 18+
├── RxJS (Observables, Operadores)
├── Angular Material
└── Angular Reactive Forms

Testing
├── JUnit 5 + Mockito + StepVerifier
├── Jest + Angular Testing Library
├── Playwright (E2E)
├── Karate (Contract Testing)
└── JMeter (Performance Testing)

Infraestructura
├── Docker + Docker Compose
├── PostgreSQL 16
└── Redis 7
```

---

## Estructura del Proyecto

```
bancopago/
├── backend/
│   └── src/main/java/com/bancopago/
│       ├── BackendApplication.java
│       │
│       ├── domain/                          # LÓGICA DE NEGOCIO PURA
│       │   ├── account/
│       │   │   ├── Account.java
│       │   │   ├── AccountStatus.java
│       │   │   ├── AccountType.java
│       │   │   └── Currency.java
│       │   ├── person/
│       │   │   ├── Person.java
│       │   │   ├── Client.java
│       │   │   ├── Employee.java
│       │   │   ├── PersonType.java
│       │   │   └── DocumentType.java
│       │   ├── payment/
│       │   │   ├── Payment.java
│       │   │   ├── PaymentStatus.java
│       │   │   └── PaymentType.java
│       │   ├── error/
│       │   │   └── DomainError.java         # Catálogo central de errores
│       │   └── exception/
│       │       ├── AccountBlockedException.java
│       │       ├── InsufficientBalanceException.java
│       │       └── ...
│       │
│       ├── application/                     # CASOS DE USO & PUERTOS
│       │   ├── port/
│       │   │   ├── input/                   # Puertos de entrada (interfaces Use Case)
│       │   │   └── output/                  # Puertos de salida (interfaces Repository)
│       │   ├── service/                     # Implementaciones de Use Cases
│       │   └── dto/                         # DTOs de la aplicación
│       │       ├── request/
│       │       └── response/
│       │
│       └── infrastructure/                  # ADAPTADORES & CONFIG
│           ├── adapter/
│           │   ├── inbound/                 # Controllers REST
│           │   │   └── rest/
│           │   └── outbound/                # Adaptadores secundarios
│           │       ├── r2dbc/
│           │       │   ├── entity/
│           │       │   ├── mapper/
│           │       │   └── repository/
│           │       ├── redis/
│           │       └── external/
│           └── config/
│
├── frontend/
│   └── src/app/
│       ├── core/
│       ├── features/
│       └── shared/
│
├── e2e/             (Playwright)
├── karate/          (Contract tests)
├── jmeter/          (Performance tests)
└── docs/
    ├── ARCHITECTURE.md
    ├── IMPLEMENTATION_GUIDE.md
    └── ROADMAP.md
```

---

## Responsabilidades de Capas

### 1. Capa de Dominio (`domain/`)
**Propósito:** Expresar reglas de negocio en Java puro. Sin dependencias de frameworks.

**Qué va aquí:**
- **Entidades:** `Account`, `Person`, `Payment` — objetos con identidad y ciclo de vida
- **Value Objects:** `DocumentType`, `Currency`, `Email` — inmutables, autovalidables
- **DomainError Enum:** Catálogo central de códigos de error y plantillas de mensajes i18n
- **Excepciones de Dominio:** Excepciones tipadas para cada violación de regla de negocio
- **Métodos de negocio:** `Account.block()`, `Account.withdraw()`, `Payment.complete()`

**Qué NO va:**
- Anotaciones Spring (`@Service`, `@Repository`, `@Autowired`)
- Anotaciones R2DBC/JPA
- Concerns HTTP (códigos de estado, headers)
- Lógica de serialización (JSON)

### 2. Capa de Aplicación (`application/`)
**Propósito:** Orquestar casos de uso. Coordinar objetos de dominio con infraestructura.

**Qué va aquí:**
- **Interfaces de Use Case** (puertos de entrada): `CreateAccountUseCase`, `TransferPaymentUseCase`
- **Implementaciones de Use Case:** Orquestan validación de dominio, llamadas a repositorio, mappers
- **Interfaces de Repositorio** (puertos de salida): `AccountRepository`, `PersonRepository`
- **DTOs:** Objetos de request/response para comunicación API
- **Mappers:** Convierten entre DTOs y objetos de dominio

**Contrato reactivo:**
```java
// Las interfaces de Use Case retornan tipos reactivos
public interface CreateAccountUseCase {
    Mono<AccountResponse> execute(CreateAccountRequest request);
}

public interface GetAccountBalanceUseCase {
    Mono<BigDecimal> execute(UUID accountId);
    Flux<BalanceEvent> stream(UUID accountId);  // SSE
}
```

### 3. Capa de Infraestructura (`infrastructure/`)
**Propósito:** Detalles técnicos — HTTP, base de datos, caché, servicios externos.

**Qué va aquí:**
- **Controllers REST:** Reciben peticiones HTTP, llaman casos de uso, retornan respuestas
- **Repositorios R2DBC:** Implementan el puerto `AccountRepository` con R2DBC
- **Adaptadores Redis:** Implementan caché de idempotencia
- **Configuración de Seguridad:** Filtro JWT, acceso basado en roles
- **Manejador de Excepciones:** `@ExceptionHandler` mapea `DomainException` → HTTP status codes
- **Endpoints SSE:** `Flux<ServerSentEvent>` para actualizaciones en tiempo real

---

## Capa de Dominio en Detalle

### Patrón de Entidad
Las entidades tienen identidad (UUID), ciclo de vida y métodos de negocio que imponen invariantes.

```java
public class Account {
    private final UUID id;
    private final UUID ownerId;
    private BigDecimal balance;
    private AccountStatus status;

    // Constructor valida invariantes de creación
    public Account(UUID ownerId, String number, AccountType type) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.number = validateNumber(number);
        this.type = validateType(type);
        this.balance = BigDecimal.ZERO;           // Regla: inicia en cero
        this.status = AccountStatus.ACTIVE;       // Regla: inicia activa
    }

    // Métodos de negocio imponen transiciones de estado
    public void block() {
        if (status != AccountStatus.ACTIVE)
            throw new InvalidAccountStateException(id, status, "block");
        this.status = AccountStatus.BLOCKED;
    }

    public void withdraw(BigDecimal amount) {
        validatePositiveAmount(amount);
        assertOperable();
        if (balance.compareTo(amount) < 0)
            throw new InsufficientBalanceException(balance, amount);
        this.balance = balance.subtract(amount);
    }
}
```

### Patrón Value Object

Objetos inmutables (Java `record`) que encapsulan validación, formato y comportamiento. Los VOs se autovalidan en su **compact constructor**.

**¿Dónde ubicarlos?**
- `domain/{module}/vo/` para VOs específicos del módulo (ej: `AccountNumber`, `Money` en `domain/account/vo/`)
- `domain/person/vo/` para VOs de persona (ej: `Email`, `DocumentNumber`)

**Reglas:**
- Siempre `record` — inmutables por diseño, `equals`/`hashCode`/`toString` automáticos
- Validación en el compact constructor
- Normalización de datos (trim, lowercase) en el compact constructor
- Sin dependencias externas (Java puro + helpers de `crosscutting`)

```java
// Ejemplo: Email Value Object
public record Email(String value) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        value = TextHelper.applyTrim(value);
        if (TextHelper.isBlank(value))
            throw InvalidPersonException.create(DomainError.PERSON_EMAIL_EMPTY);
        value = value.toLowerCase();
        if (!EMAIL_PATTERN.matcher(value).matches())
            throw InvalidPersonException.create(DomainError.PERSON_EMAIL_INVALID);
    }
}

// Ejemplo: Money Value Object (con operaciones)
public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0)
            throw InvalidAmountException.create(amount);
        currency = ObjectHelper.getDefault(currency, Currency.COP);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency))
            throw InvalidAccountException.create(DomainError.ACCOUNT_CURRENCY_MISMATCH, ...);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        // validación de saldo insuficiente + devolución de nuevo Money
    }
}
```

**Ventajas:**
- Validación centralizada — nunca un email inválido en el sistema
- Comportamiento encapsulado — `Money.add()`, `Money.subtract()` con reglas de moneda
- Inmutabilidad — thread-safe sin locks
- Auto-documentado — el tipo revela la intención (`Email` vs `String`)

**Uso en entidades de dominio:**

```java
public class AccountDomain extends BaseDomain {
    private final AccountNumber number;  // VO, no String
    private Money balance;               // VO con amount + currency

    public AccountDomain(UUID id, UUID ownerId, AccountNumber number,
                         AccountType type, Money balance, AccountStatus status) {
        // los VOs ya vienen validados — solo se verifica null con helpers
        this.number = ObjectHelper.requireNonNull(number, () ->
            InvalidAccountException.create(DomainError.ACCOUNT_NUMBER_EMPTY));
        this.balance = ObjectHelper.getDefault(balance, Money::zero);
    }
}
```

### Patrón ErrorCode (Catálogo de Errores por Módulo)

Cada módulo tiene su propio enum implementando `ErrorCode`, con mensajes en **español** (mensajes de usuario). Esto centraliza los códigos y mensajes de error sin acoplar módulos entre sí.

**Interface base:**
```java
// crosscutting/exception/ErrorCode.java
public interface ErrorCode {
    String getCode();
    String getMessageTemplate();
}
```

**Ejemplo por módulo:**
```java
// domain/account/AccountError.java
public enum AccountError implements ErrorCode {
    NUMBER_EMPTY("El número de cuenta no puede estar vacío"),
    TYPE_REQUIRED("El tipo de cuenta es requerido");
    // ...
    @Override
    public String getCode() { return "ACCOUNT_" + name(); }
    @Override
    public String getMessageTemplate() { return messageTemplate; }
}
```

**DomainException recibe `ErrorCode` (genérico):**
```java
public abstract class DomainException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Layer layer;

    protected DomainException(ErrorCode errorCode, Layer layer, Object... args) {
        super(String.format(errorCode.getMessageTemplate(), args));
        this.errorCode = errorCode;
        this.layer = layer;
    }
}
```

### Jerarquía de Excepciones de Dominio

```
DomainException (abstracta, contiene ErrorCode + Layer + args)
├── InvalidPersonException
├── InvalidAccountException
├── AccountBlockedException
├── InsufficientBalanceException
├── InvalidAccountStateException
└── InvalidAmountException
```

Cada excepción usa el patrón **static factory `create()`** con constructor privado:

```java
public class InsufficientBalanceException extends DomainException {
    private static final long serialVersionUID = 1L;
    private final BigDecimal currentBalance;
    private final BigDecimal requiredAmount;

    private InsufficientBalanceException(BigDecimal current, BigDecimal required) {
        super(AccountError.INSUFFICIENT_BALANCE, Layer.DOMAIN, current, required);
        this.currentBalance = current;
        this.requiredAmount = required;
    }

    public static InsufficientBalanceException create(BigDecimal current, BigDecimal required) {
        return new InsufficientBalanceException(current, required);
    }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public BigDecimal getRequiredAmount() { return requiredAmount; }
}
```

### Errores por Módulo (separados en español)

Cada módulo tiene su propio enum de errores implementando `ErrorCode`. Los mensajes están en **español** porque son mensajes de usuario:

```java
// domain/account/AccountError.java
public enum AccountError implements ErrorCode {
    NUMBER_EMPTY("El número de cuenta no puede estar vacío"),
    TYPE_REQUIRED("El tipo de cuenta es requerido"),
    INSUFFICIENT_BALANCE("Saldo insuficiente: actual=%s, requerido=%s"),
    BLOCKED("La cuenta %s se encuentra bloqueada"),
    // ...
}

// domain/person/PersonError.java
public enum PersonError implements ErrorCode {
    NAME_EMPTY("El nombre de la persona no puede estar vacío"),
    EMAIL_INVALID("El formato del correo electrónico no es válido"),
    DOCUMENT_TYPE_REQUIRED("El tipo de documento es requerido"),
    // ...
}
```

**Estructura de clases relacionada con errores:**
```
crosscutting/exception/
├── ErrorCode.java       # Interfaz con getCode() + getMessageTemplate()
├── Layer.java           # Enum: DOMAIN, APPLICATION, INFRASTRUCTURE
└── DomainException.java # Abstracta base
```

La interface `ErrorCode` permite que cualquier enum (de cualquier módulo) pueda ser usado como fuente de errores, manteniendo la consistencia en `DomainException`.

### Estrategia de Validación en Tres Niveles

| Nivel | Capa | Mecanismo | Propósito |
|-------|------|-----------|-----------|
| **1. Formato/Sintaxis** | Infraestructura (DTO) | Jakarta Validation (`@NotBlank`, `@Email`, etc.) | Validar entrada HTTP antes de llegar al use case |
| **2. Invariantes de VO** | Dominio (Value Object) | Compact constructor de `record` | Garantizar que el concepto siempre sea válido |
| **3. Reglas de Negocio** | Dominio/Aplicación | `DomainRule<T>` con acceso a repositorios | Validaciones que requieren estado del sistema (unicidad, saldo, etc.) |

#### Nivel 1: Jakarta Validation en Infraestructura

Los DTOs de request llevan anotaciones `jakarta.validation` para filtrar entrada malformada antes de llegar al dominio:

```java
// application/dto/request/CreateAccountRequest.java
public record CreateAccountRequest(
    @NotNull UUID ownerId,
    @NotBlank String number,
    @NotNull AccountType type
) {}
```

El `@Valid` en el Controller activa la validación automática:

```java
@PostMapping
public Mono<ResponseEntity<AccountResponse>> create(
        @RequestBody @Valid CreateAccountRequest request) { ... }
```

#### Nivel 2: Value Objects (ver sección anterior)

#### Nivel 3: DomainRule

Para validaciones que requieren acceso a repositorios (ej: unicidad de número de cuenta), se usa `DomainRule<T>`:

```java
@FunctionalInterface
public interface DomainRule<T> {
    void validate(T data);
}
```

Las reglas se implementan como beans de Spring e inyectan repositorios:

```java
@Component
public class UniqueAccountNumberRule implements DomainRule<Account> {
    private final AccountRepository accountRepository;

    public UniqueAccountNumberRule(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void validate(Account account) {
        // lógica reactiva o síncrona según el caso
    }
}
```

### Helpers Antinulos (TextHelper, ObjectHelper)

Ubicados en `domain/helper/`, son utilidades transversales para DTOs y Entities:

```java
public final class TextHelper {
    public static String applyTrim(String value) {
        return value == null ? EMPTY : value.trim();
    }
}

public final class ObjectHelper {
    public static <T> T getDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
```

Se usan en setters de Entities para evitar nulos:

```java
public void setNombre(String nombre) {
    this.nombre = TextHelper.applyTrim(nombre);
}
```

---

## Capa de Aplicación en Detalle

### Patrón Use Case (Reactivo)

```java
@Service
public class CreateAccountService implements CreateAccountUseCase {

    private final AccountRepository accountRepository;
    private final PersonRepository personRepository;

    public CreateAccountService(AccountRepository accountRepository,
                                 PersonRepository personRepository) {
        this.accountRepository = accountRepository;
        this.personRepository = personRepository;
    }

    @Override
    public Mono<AccountResponse> execute(CreateAccountRequest request) {
        return personRepository.findById(request.ownerId())
            .switchIfEmpty(Mono.error(new PersonNotFoundException(request.ownerId())))
            .flatMap(person -> {
                var account = new Account(request.ownerId(), request.number(), request.type());
                return accountRepository.save(account);
            })
            .map(AccountResponse::from);
    }
}
```

### Patrón de Validación con Reglas

En lugar de dispersar validaciones entre use cases, compón reglas reutilizables:

```java
@FunctionalInterface
public interface DomainRule<T> {
    Mono<Void> validate(T data);
}

public class CreateAccountValidator {
    private final List<DomainRule<Account>> rules = List.of(
        new PositiveAmountRule(),
        new UniqueAccountNumberRule(accountRepository)
    );

    public Mono<Void> validate(Account account) {
        return Flux.fromIterable(rules)
            .flatMap(rule -> rule.validate(account))
            .then();
    }
}
```

---

## Capa de Infraestructura en Detalle

### Controller REST (Reactivo)

```java
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final CreateAccountUseCase createAccount;
    private final GetBalanceUseCase getBalance;

    @PostMapping
    public Mono<ResponseEntity<AccountResponse>> create(@RequestBody @Valid CreateAccountRequest request) {
        return createAccount.execute(request)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @GetMapping("/{id}/balance/stream")
    public Flux<ServerSentEvent<BalanceEvent>> streamBalance(@PathVariable UUID id) {
        return getBalance.stream(id)
            .map(event -> ServerSentEvent.builder(event).build());
    }
}
```

### Puerto de Repositorio Reactivo

```java
public interface AccountRepository {
    Mono<Account> save(Account account);
    Mono<Account> findById(UUID id);
    Mono<Account> findByNumber(String number);
    Flux<Account> findByOwnerId(UUID ownerId);
    Mono<Boolean> existsByNumber(String number);
}
```

### Manejador Global de Errores

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex) {
        var status = mapToHttpStatus(ex.getDomainError());
        return ResponseEntity.status(status).body(
            new ErrorResponse(ex.getCode(), ex.getMessage()));
    }

    private HttpStatus mapToHttpStatus(DomainError error) {
        return switch (error) {
            case PERSON_NOT_FOUND, ACCOUNT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ACCOUNT_INSUFFICIENT_BALANCE -> HttpStatus.UNPROCESSABLE_ENTITY;
            case ACCOUNT_BLOCKED -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
```

---

## Flujo de Datos Reactivo

### Crear Cuenta (POST)

```
1. Controller recibe CreateAccountRequest (DTO)
        ↓
2. Controller llama CreateAccountUseCase.execute(request)
        ↓
3. Service valida que la Persona exista via PersonRepository
        ↓
4. Service crea entidad Account (valida reglas de negocio)
        ↓
5. Service llama AccountRepository.save(account) → R2DBC
        ↓
6. Service mapea Account → AccountResponse
        ↓
7. Controller retorna Mono<ResponseEntity<AccountResponse>>
```

### Stream de Saldo (SSE)

```
1. Cliente se conecta a GET /api/v1/accounts/{id}/balance/stream
        ↓
2. Controller retorna Flux<ServerSentEvent<BalanceEvent>>
        ↓
3. UseCase retorna Flux<BalanceEvent> de una fuente reactiva
   (ej: Sinks.Many, o polling + deduplicación)
        ↓
4. Cada evento es enviado al cliente via SSE
        ↓
5. Conexión se mantiene abierta hasta que el cliente se desconecta
```

### Procesamiento Batch (Nómina)

```java
public Flux<PaymentResult> processPayroll(String batchId) {
    return employeeRepository.findByBatchId(batchId)
        .flatMap(employee -> processPayment(employee)
            .onErrorResume(ex -> Mono.just(PaymentResult.failed(employee, ex))),
            50  // concurrencia = 50 — procesamiento paralelo no bloqueante
        );
}
```

---

## Patrones de Diseño

| Patrón | Uso | Ejemplo |
|--------|-----|---------|
| **Entity** | Objetos con identidad y ciclo de vida | `Account`, `Person`, `Payment` |
| **Value Object** | Conceptos inmutables y autovalidables | `Email`, `Currency`, `DocumentNumber` |
| **Aggregate** | Cluster de entidades tratadas como unidad | `Account` + `TransactionHistory` |
| **Repository** | Abstracción sobre acceso a datos | `AccountRepository` (puerto) |
| **Use Case / Interactor** | Operación de negocio única | `CreateAccountService` |
| **Port & Adapter** | Arquitectura hexagonal | Puerto = interface, Adapter = impl |
| **DomainError Catalog** | Códigos de error centralizados (enum) | `DomainError.ACCOUNT_BLOCKED` |
| **Strategy** | Reglas de validación intercambiables | `DomainRule<T>` implementations |
| **Factory Method** | Métodos estáticos de creación | `Exception.create()`, `Entity.create()` |
| **Helper** | Utilidades antinulos transversales | `TextHelper`, `ObjectHelper` |
| **Server-Sent Events** | Push en tiempo real al cliente | `Flux<ServerSentEvent>` |

---

## Principios SOLID Aplicados

| Principio | Implementación |
|-----------|---------------|
| **S**ingle Responsibility | Cada entidad, use case y repositorio tiene un propósito claro |
| **O**pen/Closed | `Person` es abstracta; `Client`/`Employee` extienden sin modificar la base. `DomainError` crece agregando nuevos valores del enum. |
| **L**iskov Substitution | Los subtipos son completamente sustituibles por sus tipos base |
| **I**nterface Segregation | Interfaces pequeñas y enfocadas: `CreateAccountUseCase` ≠ `GetBalanceUseCase` |
| **D**ependency Inversion | Aplicación depende de `AccountRepository` (interface), no de la implementación R2DBC |

---

## Estrategia de Manejo de Errores

```
                    DomainException (código + mensaje)
                            │
                            ▼
              GlobalExceptionHandler
                            │
              ┌─────────────┼─────────────┐
              ▼             ▼             ▼
        4xx Error Cliente  5xx Error     Validación
        (reglas negocio)   (inesperado)  (constraint)
              │
              ▼
     ErrorResponse { code, message, timestamp }
```

**Beneficios:**
- La capa de dominio nunca lidia con códigos de estado HTTP
- Los códigos de error de `DomainError` se mapean consistentemente a HTTP en un solo lugar
- Cada clase de excepción específica permite manejo detallado
- Las plantillas de mensaje soportan i18n sin tocar el dominio

---

## Estrategia de Testing

### Por Capa

| Capa | Herramienta | Enfoque |
|------|-------------|---------|
| **Dominio** | JUnit 5 | Reglas de negocio puras, sin mocks |
| **Aplicación** | JUnit 5 + Mockito | Orquestación de use cases |
| **Infraestructura** | StepVerifier + Testcontainers | Endpoints reactivos, R2DBC |
| **E2E** | Playwright | Flujos de usuario completos en navegador |
| **Contrato** | Karate | Validación de contratos API |
| **Performance** | JMeter | Pruebas de carga (usuarios concurrentes) |

### Testing de Dominio (Sin Mocks, Sin Spring)

```java
class AccountTest {
    @Test
    void shouldBlockActiveAccount() {
        var account = new Account(ownerId, "001-123", AccountType.SAVINGS);
        account.block();
        assertEquals(AccountStatus.BLOCKED, account.getStatus());
    }

    @Test
    void shouldRejectWithdrawFromBlockedAccount() {
        var account = new Account(ownerId, "001-123", AccountType.SAVINGS);
        account.deposit(new BigDecimal("500"));
        account.block();
        assertThrows(AccountBlockedException.class,
            () -> account.withdraw(new BigDecimal("100")));
    }
}
```

### Testing Reactivo (StepVerifier)

```java
class AccountControllerTest {
    @Test
    void shouldReturnCreatedOnValidAccount() {
        var request = new CreateAccountRequest(ownerId, "001-123", AccountType.SAVINGS);

        webTestClient.post().uri("/api/v1/accounts")
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.id").isNotEmpty();
    }
}
```

---

## Decisiones Arquitectónicas Clave

| Decisión | Justificación |
|----------|---------------|
| **WebFlux sobre MVC** | IO no bloqueante para miles de pagos concurrentes y SSE en tiempo real |
| **R2DBC sobre JPA** | Acceso reactivo a BD — ningún hilo se bloquea por consultas |
| **Redis para idempotencia** | Búsqueda rápida con TTL, antes de cualquier escritura en BD |
| **Flyway sobre DDL auto** | Migraciones versionadas, auditables y controladas |
| **UUID sobre secuencias** | Amigable con sistemas distribuidos, sin round-trip a BD para generar ID |
| **DomainError enum** | Catálogo de errores centralizado — cero strings hardcodeados, mapeo HTTP en un lugar |
| **MapStruct** | Mapeo DTO↔Domain en compilación; Entity↔Domain manual por naming español/inglés |
| **SSE sobre WebSocket** | Más simple para actualizaciones unidireccionales servidor→cliente |

---

> **English version:** [`ARCHITECTURE_DECISIONS.md`](./ARCHITECTURE_DECISIONS.md) — source of truth for issue generation.

*Versión del documento: 2.0.0*
*Última actualización: Julio 2026*
