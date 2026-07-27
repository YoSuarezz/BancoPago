# Guía de Implementación — BancoPago

Guía paso a paso para implementar nuevas funcionalidades usando Clean Architecture + Programación Reactiva (WebFlux/R2DBC).

---

## Tabla de Contenidos

1. [Antes de Empezar](#1-antes-de-empezar)
2. [Paso 1: Definir el Modelo de Dominio](#2-paso-1-definir-el-modelo-de-dominio)
3. [Paso 2: Crear Excepciones de Dominio](#3-paso-2-crear-excepciones-de-dominio)
4. [Paso 3: Agregar Entradas en DomainError](#4-paso-3-agregar-entradas-en-domainerror)
5. [Paso 4: Definir Puertos de Repositorio (Salida)](#5-paso-4-definir-puertos-de-repositorio-salida)
6. [Paso 5: Definir Puertos de Use Case (Entrada)](#6-paso-5-definir-puertos-de-use-case-entrada)
7. [Paso 6: Implementar Use Cases](#7-paso-6-implementar-use-cases)
8. [Paso 7: Crear DTOs de Aplicación](#8-paso-7-crear-dtos-de-aplicación)
9. [Paso 8: Crear Entidad R2DBC](#9-paso-8-crear-entidad-r2dbc)
10. [Paso 9: Crear Adaptador de Repositorio R2DBC](#10-paso-9-crear-adaptador-de-repositorio-r2dbc)
11. [Paso 10: Crear Controller REST](#11-paso-10-crear-controller-rest)
12. [Paso 11: Agregar Manejo Global de Excepciones](#12-paso-11-agregar-manejo-global-de-excepciones)
13. [Paso 12: Escribir Tests](#13-paso-12-escribir-tests)
14. [Paso 13: Crear Migración Flyway](#14-paso-13-crear-migración-flyway)
15. [Convenciones & Checklist](#15-convenciones--checklist)

---

## 1. Antes de Empezar

### Convenciones del Proyecto
- **Idioma:** Código en **inglés**; documentación en **español**
- **BD:** Tablas/columnas y enums en **inglés** (`person`, `account`); solo mensajes de usuario (`*Error`) en español
- **Nombres:** Clases en PascalCase, métodos en camelCase, constantes en UPPER_SNAKE_CASE
- **Paquetes:** minúsculas, singular: `domain.account.*`, `domain.person.*`
- **No Lombok:** Getters, setters y constructores manuales
- **Excepciones:** Siempre con constructor privado + `create()` estático
- **Entities:** Siempre con constructor por defecto antinulos + métodos `create()` estáticos
- **Helpers:** Usar `TextHelper.applyTrim()` y `ObjectHelper.getDefault()` / `requireNonNull()` en setters de DTOs/Entities
- **Value Objects (VO):** `record` en `domain/{module}/vo/` con validación en compact constructor
- **Validación en 3 niveles:** Jakarta Validation (DTO/request) → Value Objects (dominio) → DomainRule (repositorio)

### Mentalidad Reactiva
- Piensa en streams: `Mono<T>` para valores async únicos, `Flux<T>` para múltiples valores
- Usa `flatMap` para operaciones async secuenciales, `flatMapMany` para Flux desde Mono
- Nunca bloquees (no `.block()` en código de producción)
- Manejo de errores: `onErrorResume`, `onErrorReturn`, `switchIfEmpty`

---

## 2. Paso 1: Definir el Modelo de Dominio

Crea la entidad de dominio en `domain/{module}/`.

### Value Objects (VO)

Los VOs encapsulan conceptos con validación y comportamiento. Van en `domain/{module}/vo/` (o `domain/person/vo/` para compartidos).

**Reglas:**
- Siempre `record` Java — inmutables por diseño
- Validación en el **compact constructor** (sin métodos `validate*` separados)
- Usar `TextHelper.isBlank()`, `ObjectHelper.getDefault()`, etc., nunca `if (x == null)` directo
- El mensaje de error sale de `DomainError`, nunca hardcodeado
- Operaciones de negocio como métodos del `record` (`Money.add()`, `Money.subtract()`)

**Template:**

```java
// domain/account/vo/AccountNumber.java
package com.bancopago.backend.domain.account.vo;

import com.bancopago.backend.crosscutting.enums.DomainError;
import com.bancopago.backend.crosscutting.helpers.TextHelper;
import com.bancopago.backend.domain.account.exceptions.InvalidAccountException;

public record AccountNumber(String value) {

    public AccountNumber {
        value = TextHelper.applyTrim(value);
        if (TextHelper.isBlank(value)) {
            throw InvalidAccountException.create(DomainError.ACCOUNT_NUMBER_EMPTY);
        }
    }
}
```

```java
// domain/account/vo/Money.java
public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0)
            throw InvalidAmountException.create(amount);
        currency = ObjectHelper.getDefault(currency, Currency.COP);
    }

    public Money add(Money other) { /* valida moneda + suma */ }
    public Money subtract(Money other) { /* valida moneda + saldo + resta */ }
}
```

**Uso en la entidad de dominio:**
```java
public class AccountDomain extends BaseDomain {
    private final AccountNumber number;  // VO en vez de String
    private Money balance;               // VO con amount + currency

    public AccountDomain(UUID id, UUID ownerId, AccountNumber number,
                         AccountType type, Money balance, AccountStatus status) {
        super(id);
        this.ownerId = ObjectHelper.requireNonNull(ownerId, () -> InvalidAccountException.create(DomainError.ACCOUNT_OWNER_REQUIRED));
        this.number = ObjectHelper.requireNonNull(number, () -> InvalidAccountException.create(DomainError.ACCOUNT_NUMBER_EMPTY));
        this.type = ObjectHelper.requireNonNull(type, () -> InvalidAccountException.create(DomainError.ACCOUNT_TYPE_REQUIRED));
        this.balance = ObjectHelper.getDefault(balance, Money::zero);
    }
}
```

**VOs disponibles actualmente:**

| VO | Ubicación | Campos | Valida |
|----|-----------|--------|--------|
| `Email` | `domain/person/vo/` | `value: String` | null, blank, formato, max 100 chars, lowercase |
| `DocumentNumber` | `domain/person/vo/` | `type: DocumentType`, `value: String` | null type, blank, max 30 chars |
| `AccountNumber` | `domain/account/vo/` | `value: String` | null, blank |
| `Money` | `domain/account/vo/` | `amount: BigDecimal`, `currency: Currency` | null amount, negativo, default COP |

### Reglas
- Java puro — sin anotaciones Spring, sin R2DBC, sin HTTP
- Valida invariantes en el constructor
- Los métodos de negocio imponen transiciones de estado
- Identidad UUID (generada en el constructor, no por la BD)

### Template

```java
package com.bancopago.domain.account;

import com.bancopago.domain.error.DomainError;
import com.bancopago.domain.exception.InvalidAccountException;
import java.math.BigDecimal;
import java.util.UUID;

public class Account {

    private final UUID id;
    private final UUID ownerId;
    private final String number;
    private final AccountType type;
    private final Currency currency;
    private BigDecimal balance;
    private AccountStatus status;

    // Constructor completo (para reconstitución desde BD)
    public Account(UUID id, UUID ownerId, String number, AccountType type,
                   Currency currency, BigDecimal balance, AccountStatus status) {
        this.id = id != null ? id : UUID.randomUUID();
        this.ownerId = ownerId;
        this.number = validateNumber(number);
        this.type = validateType(type);
        this.currency = currency != null ? currency : Currency.COP;
        this.balance = validateInitialBalance(balance);
        this.status = status != null ? status : AccountStatus.ACTIVE;
    }

    // Constructor factory (para cuentas nuevas)
    public Account(UUID ownerId, String number, AccountType type) {
        this(null, ownerId, number, type, Currency.COP, BigDecimal.ZERO, AccountStatus.ACTIVE);
    }

    // Métodos de negocio
    public void deposit(BigDecimal amount) {
        requirePositiveAmount(amount);
        ensureOperable();
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        requirePositiveAmount(amount);
        ensureOperable();
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(this.balance, amount);
        }
        this.balance = this.balance.subtract(amount);
    }

    public void block() {
        if (this.status != AccountStatus.ACTIVE) {
            throw new InvalidAccountStateException(this.id, this.status, "block");
        }
        this.status = AccountStatus.BLOCKED;
    }

    public void unblock() {
        if (this.status != AccountStatus.BLOCKED) {
            throw new InvalidAccountStateException(this.id, this.status, "unblock");
        }
        this.status = AccountStatus.ACTIVE;
    }

    public void close() {
        if (this.status == AccountStatus.INACTIVE) {
            throw new InvalidAccountStateException(this.id, this.status, "close");
        }
        this.status = AccountStatus.INACTIVE;
    }

    // Validaciones privadas
    private void ensureOperable() {
        if (this.status == AccountStatus.BLOCKED) {
            throw new AccountBlockedException(this.id);
        }
        if (this.status == AccountStatus.INACTIVE) {
            throw new InvalidAccountStateException(this.id, this.status, "operate");
        }
    }

    private void requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(amount);
        }
    }

    // Getters (sin setters — el estado cambia solo por métodos de negocio)
    public UUID getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public BigDecimal getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    // ...
}
```

---

## 3. Paso 2: Crear Excepciones de Dominio

Ubícalas en `domain/{module}/exception/`. Cada excepción mapea a un `ErrorCode` del módulo correspondiente.

### Template (con patrón `create()`)

```java
package com.bancopago.backend.domain.account.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.crosscutting.exception.Layer;
import com.bancopago.backend.domain.account.AccountError;
import java.util.UUID;

public class AccountBlockedException extends DomainException {
    private static final long serialVersionUID = 1L;
    private final UUID accountId;

    private AccountBlockedException(UUID accountId) {
        super(AccountError.BLOCKED, Layer.DOMAIN, accountId);
        this.accountId = accountId;
    }

    public static AccountBlockedException create(UUID accountId) {
        return new AccountBlockedException(accountId);
    }

    public UUID getAccountId() { return accountId; }
}
```

---

## 4. Paso 3: Crear ErrorCode del Módulo

Crea un enum en el módulo implementando `ErrorCode`. Los mensajes van en **español** (mensajes de usuario).

**Para módulo Account** → `domain/account/AccountError.java`:

```java
package com.bancopago.backend.domain.account;

import com.bancopago.backend.crosscutting.exception.ErrorCode;

public enum AccountError implements ErrorCode {
    NUMBER_EMPTY("El número de cuenta no puede estar vacío"),
    TYPE_REQUIRED("El tipo de cuenta es requerido"),
    INVALID_AMOUNT("El monto %s no es válido. Debe ser un valor positivo."),
    INSUFFICIENT_BALANCE("Saldo insuficiente: actual=%s, requerido=%s"),
    BLOCKED("La cuenta %s se encuentra bloqueada"),
    INVALID_STATE("No se puede ejecutar '%s' en la cuenta %s con estado %s");

    private final String messageTemplate;

    AccountError(String messageTemplate) { this.messageTemplate = messageTemplate; }

    @Override
    public String getCode() { return "ACCOUNT_" + name(); }
    @Override
    public String getMessageTemplate() { return messageTemplate; }
}
```

**Para módulo Person** → `domain/person/PersonError.java`:

```java
public enum PersonError implements ErrorCode {
    NAME_EMPTY("El nombre de la persona no puede estar vacío"),
    EMAIL_INVALID("El formato del correo electrónico no es válido"),
    DOCUMENT_TYPE_REQUIRED("El tipo de documento es requerido");
    // ...
}
```

La interface `ErrorCode` está en `crosscutting/exception/ErrorCode.java`:

```java
public interface ErrorCode {
    String getCode();
    String getMessageTemplate();
}
```

### Layer Enum

`crosscutting/exception/Layer.java` identifica la capa de origen del error. Útil en el `GlobalExceptionHandler` para mapear a HTTP status codes:

```java
public enum Layer { DOMAIN, APPLICATION, INFRASTRUCTURE }
```

### DomainException con Layer

```java
public abstract class DomainException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Layer layer;
    private final Object[] args;

    protected DomainException(ErrorCode errorCode, Layer layer, Object... args) {
        super(String.format(errorCode.getMessageTemplate(), args));
        this.errorCode = errorCode;
        this.layer = layer;
    }

    protected DomainException(ErrorCode errorCode, Object... args) {
        this(errorCode, Layer.DOMAIN, args);  // default: DOMAIN
    }

    public String getCode() { return errorCode.getCode(); }
    public Layer getLayer() { return layer; }
    public String getUserMessage() { return getMessage(); } // mensaje para el usuario
}
```

---

## 5. Paso 4: Definir Puertos de Repositorio (Salida)

Crea en `application/port/output/`. Son interfaces reactivas.

### Template

```java
package com.bancopago.application.port.output;

import com.bancopago.domain.account.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface AccountRepository {
    Mono<Account> save(Account account);
    Mono<Account> findById(UUID id);
    Mono<Account> findByNumber(String number);
    Flux<Account> findByOwnerId(UUID ownerId);
    Mono<Boolean> existsByNumber(String number);
}
```

---

## 6. Paso 5: Definir Puertos de Use Case (Entrada)

Crea en `application/port/input/`. Definen el contrato de la API.

### Template

```java
package com.bancopago.application.port.input;

import com.bancopago.application.dto.request.CreateAccountRequest;
import com.bancopago.application.dto.response.AccountResponse;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface CreateAccountUseCase {
    Mono<AccountResponse> execute(CreateAccountRequest request);
}
```

---

## 7. Paso 6: Implementar Use Cases

Crea en `application/service/`.

### Template

```java
package com.bancopago.application.service;

import com.bancopago.application.port.input.CreateAccountUseCase;
import com.bancopago.application.port.output.AccountRepository;
import com.bancopago.application.port.output.PersonRepository;
import com.bancopago.application.dto.request.CreateAccountRequest;
import com.bancopago.application.dto.response.AccountResponse;
import com.bancopago.domain.account.Account;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
        // 1. Validar que el dueño existe
        return personRepository.findById(request.ownerId())
            .switchIfEmpty(Mono.error(new PersonNotFoundException(request.ownerId())))
            // 2. Validar que el número de cuenta es único
            .flatMap(person -> accountRepository.existsByNumber(request.number())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateAccountException(request.number()));
                    }
                    // 3. Crear entidad de dominio (valida reglas de negocio)
                    var account = new Account(
                        request.ownerId(),
                        request.number(),
                        request.type()
                    );
                    // 4. Persistir y mapear a response
                    return accountRepository.save(account);
                })
            )
            .map(AccountResponse::fromDomain);
    }
}
```

---

## 8. Paso 7: Crear DTOs de Aplicación

Crea en `application/dto/request/` y `application/dto/response/`.

### Reglas para DTOs

1. **Usar `record`** — inmutables, `equals`/`hashCode` automáticos
2. **Jakarta Validation** en el request para filtrar entrada malformada (nivel 1 de validación)
3. **Response sin anotaciones** — es solo datos serializados, no necesita validación
4. **Método `fromDomain()`** estático para construir desde la entidad de dominio

### Request DTO

```java
package com.bancopago.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateAccountRequest(
    @NotNull UUID ownerId,
    @NotBlank String number,
    @NotNull AccountType type
) {}
```

### Response DTO

```java
package com.bancopago.application.dto.response;

import com.bancopago.domain.account.Account;
import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
    UUID id,
    UUID ownerId,
    String number,
    String type,
    BigDecimal balance,
    String status,
    String currency
) {
    public static AccountResponse fromDomain(Account account) {
        return new AccountResponse(
            account.getId(),
            account.getOwnerId(),
            account.getNumber(),
            account.getType().name(),
            account.getBalance(),
            account.getStatus().name(),
            account.getCurrency().name()
        );
    }
}
```

---

## 9. Paso 8: Crear Entidad R2DBC

Crea en `infrastructure/adapter/outbound/r2dbc/entity/`. Las tablas están en **español** (`persona`, `cuenta`), los atributos del Entity también usan nombres en español. Usa `TextHelper` y `ObjectHelper` en setters para garantizar antinulos. Proporciona al menos una fábrica estática `create()`.

```java
package com.bancopago.infrastructure.adapter.outbound.r2dbc.entity;

import com.bancopago.domain.helper.ObjectHelper;
import com.bancopago.domain.helper.TextHelper;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("cuenta")
public class AccountEntity implements Persistable<UUID> {

    @Id
    private UUID id;
    @Column("persona_id")
    private UUID personaId;
    private String numero;
    private String tipo;
    private BigDecimal saldo;
    private String moneda;
    private String estado;
    @Version
    private Long version;
    @Column("created_at")
    private LocalDateTime createdAt;

    public AccountEntity() {
        setId(UUID.randomUUID());
        setNumero(TextHelper.EMPTY);
        setTipo(TextHelper.EMPTY);
        setSaldo(BigDecimal.ZERO);
        setMoneda("COP");
        setEstado("ACTIVA");
        setVersion(0L);
        setCreatedAt(LocalDateTime.now());
    }

    public static AccountEntity create(UUID id, UUID personaId, String numero,
                                        String tipo, BigDecimal saldo,
                                        String moneda, String estado,
                                        Long version, LocalDateTime createdAt) { ... }

    public static AccountEntity create(UUID personaId, String numero, String tipo) { ... }

    // Getters y setters con helpers antinulos
    public void setNumero(String numero) {
        this.numero = TextHelper.applyTrim(numero);
    }
    public void setSaldo(BigDecimal saldo) {
        this.saldo = ObjectHelper.getDefault(saldo, BigDecimal.ZERO);
    }
    // ...
}
```

---

## 10. Paso 9: Crear Adaptador de Repositorio R2DBC

Crea en `infrastructure/adapter/outbound/r2dbc/repository/`.

### Repositorio Spring Data R2DBC

```java
package com.bancopago.infrastructure.adapter.outbound.r2dbc.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface AccountR2dbcRepository extends R2dbcRepository<AccountEntity, UUID> {
    Mono<AccountEntity> findByNumber(String number);
    Flux<AccountEntity> findByOwnerId(UUID ownerId);
    Mono<Boolean> existsByNumber(String number);
}
```

### Adaptador Implementando el Puerto

```java
package com.bancopago.infrastructure.adapter.outbound.r2dbc;

import com.bancopago.application.port.output.AccountRepository;
import com.bancopago.domain.account.Account;
import com.bancopago.infrastructure.adapter.outbound.r2dbc.entity.AccountEntity;
import com.bancopago.infrastructure.adapter.outbound.r2dbc.mapper.AccountEntityMapper;
import com.bancopago.infrastructure.adapter.outbound.r2dbc.repository.AccountR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Component
public class AccountR2dbcAdapter implements AccountRepository {

    private final AccountR2dbcRepository repository;
    private final AccountEntityMapper mapper;

    public AccountR2dbcAdapter(AccountR2dbcRepository repository,
                                AccountEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Account> save(Account account) {
        return repository.save(mapper.toEntity(account))
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Account> findById(UUID id) {
        return repository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Account> findByNumber(String number) {
        return repository.findByNumber(number)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Account> findByOwnerId(UUID ownerId) {
        return repository.findByOwnerId(ownerId)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByNumber(String number) {
        return repository.existsByNumber(number);
    }
}
```

### Mapper Entity-Dominio (Manual)

Los mappers Entity↔Domain son **manuales** (no MapStruct) por dos razones:
1. **Nombres españoles vs ingleses:** La BD usa `persona`, `cuenta`, `numero`, `saldo`; el dominio usa `PersonDomain`, `AccountDomain`, `number`, `balance`
2. **Herencia en dominio:** `PersonDomain` es abstracta — MapStruct no puede instanciar clases abstractas

MapStruct se usa exclusivamente para mapeo **DTO↔Domain** (cuando haya conversión entre DTOs planos y entidades de dominio), porque ahí los nombres coinciden y no hay herencia.

**Estrategia híbrida:**
| Mapeo | Herramienta | Razón |
|-------|-----------|-------|
| Entity↔Domain | Manual (Spring `@Component`) | Nombres español↔inglés, herencia |
| DTO↔Domain | MapStruct | Nombres iguales, sin herencia |

Template del mapper manual:

```java
package com.bancopago.infrastructure.adapter.outbound.r2dbc.mapper;

import com.bancopago.domain.account.Account;
import com.bancopago.domain.account.AccountStatus;
import com.bancopago.domain.account.AccountType;
import com.bancopago.domain.account.Currency;
import com.bancopago.infrastructure.adapter.outbound.r2dbc.entity.AccountEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountEntityMapper {

    public AccountEntity toEntity(Account domain) {
        if (domain == null) return null;
        return AccountEntity.create(
            domain.getId(),
            domain.getOwnerId(),
            domain.getNumber(),
            mapAccountType(domain.getType()),
            domain.getBalance(),
            mapCurrency(domain.getCurrency()),
            mapAccountStatus(domain.getStatus()),
            0L, null
        );
    }

    public AccountDomain toDomain(AccountEntity entity) {
        if (entity == null) return null;
        return new AccountDomain(
            entity.getId(),
            entity.getPersonaId(),
            new AccountNumber(entity.getNumero()),     // VO desde String
            mapAccountType(entity.getTipo()),
            new Money(entity.getSaldo(), mapCurrency(entity.getMoneda())), // VO desde datos crudos
            mapAccountStatus(entity.getEstado())
        );
    }

    private String mapAccountType(AccountType type) { ... }
    private AccountType mapAccountType(String value) { ... }
    private String mapCurrency(Currency currency) { ... }
    private Currency mapCurrency(String value) { ... }
    private String mapAccountStatus(AccountStatus status) { ... }
    private AccountStatus mapAccountStatus(String value) { ... }
}
```

---

## 11. Paso 10: Crear Controller REST

Crea en `infrastructure/adapter/inbound/rest/`.

```java
package com.bancopago.infrastructure.adapter.inbound.rest;

import com.bancopago.application.port.input.CreateAccountUseCase;
import com.bancopago.application.port.input.GetBalanceUseCase;
import com.bancopago.application.dto.request.CreateAccountRequest;
import com.bancopago.application.dto.response.AccountResponse;
import com.bancopago.application.dto.response.BalanceEvent;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetBalanceUseCase getBalanceUseCase;

    public AccountController(CreateAccountUseCase createAccountUseCase,
                              GetBalanceUseCase getBalanceUseCase) {
        this.createAccountUseCase = createAccountUseCase;
        this.getBalanceUseCase = getBalanceUseCase;
    }

    @PostMapping
    public Mono<ResponseEntity<AccountResponse>> create(@RequestBody @Valid CreateAccountRequest request) {
        return createAccountUseCase.execute(request)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @GetMapping("/{id}/balance")
    public Mono<ResponseEntity<BigDecimal>> getBalance(@PathVariable UUID id) {
        return getBalanceUseCase.execute(id)
            .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/{id}/balance/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<BalanceEvent>> streamBalance(@PathVariable UUID id) {
        return getBalanceUseCase.stream(id)
            .map(event -> ServerSentEvent.builder(event)
                .id(event.eventId())
                .event("balance-update")
                .build());
    }
}
```

---

## 12. Paso 11: Agregar Manejo Global de Excepciones

```java
package com.bancopago.infrastructure.config;

import com.bancopago.domain.error.DomainError;
import com.bancopago.domain.exception.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDomain(DomainException ex) {
        var status = mapHttpStatus(ex.getDomainError());
        return Mono.just(ResponseEntity.status(status).body(
            new ErrorResponse(ex.getCode(), ex.getMessage(), Instant.now())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgument(IllegalArgumentException ex) {
        return Mono.just(ResponseEntity.badRequest().body(
            new ErrorResponse("INVALID_ARGUMENT", ex.getMessage(), Instant.now())));
    }

    private HttpStatus mapHttpStatus(DomainError error) {
        return switch (error) {
            case null -> HttpStatus.INTERNAL_SERVER_ERROR;
            case PERSON_NOT_FOUND, ACCOUNT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ACCOUNT_INSUFFICIENT_BALANCE -> HttpStatus.UNPROCESSABLE_ENTITY;
            case ACCOUNT_BLOCKED -> HttpStatus.CONFLICT;
            case ACCOUNT_DUPLICATE_NUMBER -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    record ErrorResponse(String code, String message, Instant timestamp) {}
}
```

---

## 13. Paso 12: Escribir Tests

### Tests de Dominio (JUnit 5 — Sin Spring, Sin Mocks)

```java
package com.bancopago.domain.account;

import com.bancopago.domain.exception.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    private static final UUID OWNER_ID = UUID.randomUUID();

    @Test
    void shouldCreateAccountWithZeroBalanceAndActiveStatus() {
        var account = new Account(OWNER_ID, "001-123", AccountType.SAVINGS);

        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertEquals(Currency.COP, account.getCurrency());
    }

    @Test
    void shouldBlockActiveAccount() {
        var account = new Account(OWNER_ID, "001-123", AccountType.SAVINGS);
        account.block();
        assertEquals(AccountStatus.BLOCKED, account.getStatus());
    }

    @Test
    void shouldRejectWithdrawFromBlockedAccount() {
        var account = new Account(OWNER_ID, "001-123", AccountType.SAVINGS);
        account.deposit(new BigDecimal("500"));
        account.block();
        assertThrows(AccountBlockedException.class,
            () -> account.withdraw(new BigDecimal("100")));
    }
}
```

### Tests de Use Case (JUnit 5 + Mockito + StepVerifier)

```java
package com.bancopago.application.service;

import com.bancopago.application.dto.request.CreateAccountRequest;
import com.bancopago.application.port.output.AccountRepository;
import com.bancopago.application.port.output.PersonRepository;
import com.bancopago.domain.account.Account;
import com.bancopago.domain.account.AccountType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private PersonRepository personRepository;
    @InjectMocks private CreateAccountService service;

    @Test
    void shouldCreateAccountWhenDataIsValid() {
        var ownerId = UUID.randomUUID();
        var request = new CreateAccountRequest(ownerId, "001-123", AccountType.SAVINGS);

        when(personRepository.findById(ownerId))
            .thenReturn(Mono.just(new Person(/* ... */)));
        when(accountRepository.existsByNumber("001-123"))
            .thenReturn(Mono.just(false));
        when(accountRepository.save(any()))
            .thenReturn(Mono.just(/* account with ID */));

        StepVerifier.create(service.execute(request))
            .assertNext(response -> {
                assertNotNull(response.id());
                assertEquals("001-123", response.number());
            })
            .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenPersonNotFound() {
        var ownerId = UUID.randomUUID();
        var request = new CreateAccountRequest(ownerId, "001-123", AccountType.SAVINGS);

        when(personRepository.findById(ownerId))
            .thenReturn(Mono.empty());

        StepVerifier.create(service.execute(request))
            .expectError(PersonNotFoundException.class)
            .verify();
    }
}
```

### Test de Integración (WebTestClient + Testcontainers)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class AccountControllerIT {

    @Autowired private WebTestClient webTestClient;

    @Test
    void shouldReturnCreatedOnValidAccountRequest() {
        var request = new CreateAccountRequest(ownerId, "001-123", AccountType.SAVINGS);

        webTestClient.post().uri("/api/v1/accounts")
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.id").isNotEmpty()
            .jsonPath("$.balance").isEqualTo(0);
    }
}
```

---

## 14. Paso 13: Crear Migración Flyway

Ubica en `src/main/resources/db/migration/`.

```sql
-- V2__create_account_table.sql
CREATE TABLE account (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES person(id),
    number VARCHAR(20) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'COP',
    balance DECIMAL(15,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_account_owner ON account(owner_id);
CREATE INDEX idx_account_number ON account(number);
```

### Convención de Nombres

```
V{major}{minor}__{description}.sql
V1__create_person_account.sql
V2__create_payment_table.sql
V3__add_transaction_indexes.sql
```

---

## 15. Convenciones & Checklist

### Convenciones de Nombres

| Elemento | Convención | Ejemplo |
|---------|-----------|---------|
| Entidad de dominio | PascalCase | `AccountDomain`, `PersonDomain` |
| Value Object | PascalCase (record) | `Email`, `AccountNumber`, `Money` |
| Enum | PascalCase | `AccountStatus`, `AccountError` |
| Excepción | PascalCase + `Exception` | `AccountBlockedException` |
| ErrorCode (interface) | PascalCase | `ErrorCode` |
| Layer | Enum | `DOMAIN`, `APPLICATION`, `INFRASTRUCTURE` |
| Use Case (interfaz) | PascalCase + `UseCase` | `CreateAccountUseCase` |
| Use Case (impl) | PascalCase + `Service` | `CreateAccountService` |
| Puerto de repositorio | PascalCase + `Repository` | `AccountRepository` |
| Entidad R2DBC | PascalCase + `Entity` | `AccountEntity` |
| Adaptador R2DBC | PascalCase + `Adapter` | `AccountR2dbcAdapter` |
| Controller | PascalCase + `Controller` | `AccountController` |
| DTO Request | PascalCase + `Request` | `CreateAccountRequest` |
| DTO Response | PascalCase + `Response` | `AccountResponse` |
| Paquetes | minúsculas.singular | `domain.account`, `application.service` |
| Métodos | camelCase | `findById`, `execute`, `processPayment` |
| Constantes | UPPER_SNAKE_CASE | `MAX_CONCURRENCY`, `DEFAULT_CURRENCY` |

### Checklist de Implementación

- [ ] Value Object `record` en `domain/{module}/vo/` con validación en compact constructor
- [ ] Entidad de dominio con VOs + métodos de negocio (en `domain/{module}/`)
- [ ] Enum de value objects en el mismo paquete (`AccountType`, `AccountStatus`, `Currency`)
- [ ] Excepción(es) con patrón `create()` + `serialVersionUID` + constructor privado
- [ ] ErrorCode del módulo (`{Module}Error.java`) con mensajes en español
- [ ] Layer enum (`Layer.DOMAIN`, `Layer.APPLICATION`, `Layer.INFRASTRUCTURE`)
- [ ] `getLayer()` en la excepción para mapeo HTTP consistente
- [ ] Helpers usados en Entities (`TextHelper.applyTrim()`, `ObjectHelper.getDefault()`, `ObjectHelper.requireNonNull()`)
- [ ] Puerto de repositorio (interface en `application/secondaryports/repository/`)
- [ ] Puerto de use case (interface en `application/primaryports/interactor/`)
- [ ] Implementación de use case (en `application/usecase/{module}/impl/`)
- [ ] DTOs Request con anotaciones Jakarta Validation (`@NotBlank`, `@NotNull`, etc.)
- [ ] DTOs Response con `fromDomain()` estático
- [ ] Entidad R2DBC con `@Table("nombre_espanol")`, constructor antinulos + `create()` (si hay nueva tabla)
- [ ] Migración Flyway en español (si hay nueva tabla)
- [ ] Adaptador R2DBC implementando el puerto
- [ ] Mapper manual Entity↔Domain (traducción español→inglés + VOs)
- [ ] Controller REST con `@Valid` en request body
- [ ] Entradas en GlobalExceptionHandler (mapeo DomainError→HTTP)
- [ ] Tests unitarios de reglas de dominio (VOs + entidades)
- [ ] Tests unitarios de orquestación de use case (StepVerifier)
- [ ] Tests de integración del controller (opcional en primera iteración)

### Errores Comunes en Reactivo

| Error | Cómo Evitarlo |
|-------|--------------|
| Usar `.block()` | Nunca uses `.block()` en producción. Usa operadores reactivos. |
| Bloquear dentro de `flatMap` | No llames APIs bloqueantes dentro de `flatMap`. |
| Olvidar `onErrorResume` | Siempre maneja errores en cadenas reactivas. |
| Ignorar contrapresión | Usa `Flux.flatMap(concurrency=N)` para limitar operaciones concurrentes. |
| Estado mutable compartido | Las entidades de dominio son mutables pero no se comparten entre hilos. |
| Olvidar `switchIfEmpty` | Siempre maneja el caso donde un Mono puede estar vacío. |
