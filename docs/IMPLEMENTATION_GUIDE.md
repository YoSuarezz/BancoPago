# Guía de Implementación — BancoPago

Paso a paso para implementar funcionalidades nuevas con Clean Architecture + WebFlux/R2DBC.

Complementa [`ARCHITECTURE.md`](./ARCHITECTURE.md) (visión y flujo) y
[`ARCHITECTURE_DECISIONS.md`](./ARCHITECTURE_DECISIONS.md) (decisiones).

---

## Tabla de contenidos

1. [Antes de empezar](#1-antes-de-empezar)
2. [Paso 1 — Dominio](#2-paso-1--dominio)
3. [Paso 2 — Excepciones y ErrorCode](#3-paso-2--excepciones-y-errorcode)
4. [Paso 3 — Puerto de repositorio](#4-paso-3--puerto-de-repositorio)
5. [Paso 4 — Entity R2DBC + mapper + adapter](#5-paso-4--entity-r2dbc--mapper--adapter)
6. [Paso 5 — DTOs](#6-paso-5--dtos)
7. [Paso 6 — DTOMapper (MapStruct)](#7-paso-6--dtomapper-mapstruct)
8. [Paso 7 — RulesValidator (+ Rule si aplica)](#8-paso-7--rulesvalidator--rule-si-aplica)
9. [Paso 8 — UseCase](#9-paso-8--usecase)
10. [Paso 9 — Interactor](#10-paso-9--interactor)
11. [Paso 10 — Controller](#11-paso-10--controller)
12. [Paso 11 — Tests](#12-paso-11--tests)
13. [Paso 12 — Migración Flyway](#13-paso-12--migración-flyway)
14. [Checklist](#14-checklist)
15. [Anexo: comparación con guía MVC+JPA](#15-anexo-comparación-con-guía-mvcjpa)

---

## 1. Antes de empezar

### Convenciones

- **Código** en inglés; **docs** en español; mensajes de usuario (`*Error`) en español.
- Tablas/columnas/enums de BD en **inglés**.
- Sin Lombok. Excepciones: constructor privado + `create()`.
- Entities: constructor antinulos + factories `create()`.
- Helpers: `TextHelper`, `ObjectHelper`.
- Métodos de negocio explícitos (`createAccount`), no `execute` en APIs concretas.
- Validación en 3 niveles: Jakarta → VOs → RulesValidator/`Rule`.

### Mentalidad reactiva

- `Mono<T>` / `Flux<T>`; nunca `.block()` en producción.
- Encadenar con `flatMap` / `then` / `switchIfEmpty`.
- Operaciones lazy con `Mono.defer(...)` cuando el side-effect no debe ejecutarse si hubo error previo.

### Flujo obligatorio

```
Controller → Interactor (DTO↔Domain) → UseCase (Domain + RulesValidator + Repository)
```

Detalle: [`ARCHITECTURE.md` §2](./ARCHITECTURE.md#2-quién-llama-a-quién-interactor--usecase--rulesvalidator).

---

## 2. Paso 1 — Dominio

Crear en `domain/{module}/`.

### Value Objects (`record` en `vo/`)

```java
public record AccountNumber(String value) {
    public AccountNumber {
        value = TextHelper.applyTrim(value);
        if (TextHelper.isBlank(value)) {
            throw InvalidAccountException.create(AccountError.NUMBER_EMPTY);
        }
    }
}
```

### Entidad de dominio

- Extiende `BaseDomain` (UUID).
- Constructor completo para reconstitución; factory/constructor corto para altas.
- Métodos de negocio (`block()`, `deposit()`), no setters libres de estado.
- Java puro: sin Spring, R2DBC ni HTTP.

---

## 3. Paso 2 — Excepciones y ErrorCode

1. Enum `{Module}Error implements ErrorCode` con mensajes en español.
2. Excepciones en `domain/{module}/exceptions/` extendiendo `DomainException`.
3. Constructor privado + `static create(...)`.
4. Usar `Layer.DOMAIN` o `Layer.APPLICATION` según origen.

```java
public class AccountNotFoundException extends DomainException {
    private AccountNotFoundException(UUID id) {
        super(AccountError.NOT_FOUND, Layer.DOMAIN, id);
    }
    public static AccountNotFoundException create(UUID id) {
        return new AccountNotFoundException(id);
    }
}
```

---

## 4. Paso 3 — Puerto de repositorio

En `application/secondaryports/repository/`. Interfaces reactivas, métodos explícitos.

```java
public interface AccountRepository {
    Mono<AccountDomain> saveAccount(AccountDomain account);
    Mono<AccountDomain> findAccountById(UUID accountId);
    Mono<Boolean> existsAccountByNumber(String accountNumber);
}
```

---

## 5. Paso 4 — Entity R2DBC + mapper + adapter

| Pieza | Ubicación |
|-------|-----------|
| Entity | `application/secondaryports/entity/` |
| Mapper Entity↔Domain | `application/secondaryports/mapper/` (**manual**) |
| Spring Data repo | `infrastructure/secondaryadapters/...` |
| Adapter que implementa el puerto | `infrastructure/secondaryadapters/...` |

Entity: `@Table`, constructor antinulos, `create()` estáticos, setters con helpers.

Mapper Entity↔Domain: **manual** (VOs + herencia). No MapStruct aquí.

---

## 6. Paso 5 — DTOs

`application/primaryports/dto/{module}/request|response/`

Reglas:

1. `record` inmutable.
2. Jakarta Validation solo en Request.
3. **Un Request/Response por caso de uso** (`CreatePersonResponse`, `GetPersonByIdResponse`).
4. **Sin `fromDomain()`** en el DTO — el mapeo vive en `*DTOMapper`.

```java
public record CreateAccountRequest(
    @NotNull UUID ownerId,
    @NotNull AccountType type
) {}

public record CreateAccountResponse(
    UUID id, UUID ownerId, String number, String type,
    BigDecimal balance, String currency, String status
) {}
```

---

## 7. Paso 6 — DTOMapper (MapStruct)

`application/primaryports/mapper/{module}/`

Usar `abstract class` con `@Mapper(componentModel = "spring")`:

- métodos `abstract` → MapStruct genera (**Domain → Response** 1:1)
- métodos concretos → Request→Domain con herencia/VOs (factory de aplicación, ej. `PersonDTOMapper.toDomain`)
- Entity↔Domain: **manual** (no MapStruct)
- No crear Assembler/Factory globales; solo si un Response combina varios agregados

```java
@Mapper(componentModel = "spring")
public abstract class AccountDTOMapper {
    public abstract CreateAccountResponse toCreateAccountResponse(AccountDomain domain);
    public abstract GetAccountBalanceResponse toGetAccountBalanceResponse(AccountDomain domain);
}
```

CreateAccount construye el dominio en el UseCase (`new AccountDomain(...)`); el mapper de Account solo sale a Response.

---

## 8. Paso 7 — RulesValidator + Rule (con estado)

`application/usecase/{module}/rulesvalidator/`

1. Interfaz de la regla: `rules/{Regla}Rule.java` → `extends Rule<T>`
2. Impl: `rules/impl/{Regla}RuleImpl.java` → inyecta repo, `Mono<Void> validate(...)`
3. RulesValidatorImpl: inyecta las interfaces Rule y las ejecuta en un solo `validate(domain)`

```java
public interface UniqueAccountNumberRule extends Rule<String> {
}

@Component
public class UniqueAccountNumberRuleImpl implements UniqueAccountNumberRule {
    private final AccountRepository accountRepository;

    @Override
    public Mono<Void> validate(String accountNumber) {
        return accountRepository.existsAccountByNumber(accountNumber)
            .flatMap(exists -> Boolean.TRUE.equals(exists)
                ? Mono.error(DuplicateAccountException.create(accountNumber))
                : Mono.empty());
    }
}

@Component
public class CreateAccountRulesValidatorImpl implements CreateAccountRulesValidator {
    private final OwnerExistsRule ownerExistsRule;
    private final MaxAccountsPerOwnerRule maxAccountsPerOwnerRule;
    private final UniqueAccountNumberRule uniqueAccountNumberRule;

    public Mono<Void> validate(AccountDomain account) {
        return ownerExistsRule.validate(account.getOwnerId())
            .then(maxAccountsPerOwnerRule.validate(account.getOwnerId()))
            .then(uniqueAccountNumberRule.validate(account.getNumber()));
    }
}
```

| Tipo | Dónde |
|------|--------|
| Email formato / nombre / close con saldo 0 | Dominio (VO / entidad) |
| Documento único, email único, owner existe, máx. cuentas, operación permitida en UC | `*Rule` + `*RuleImpl` orquestados por RulesValidator |
| Cuenta/persona no encontrada al cargar | UseCase (`find` + `switchIfEmpty` → `*NotFoundException`) |

---

## 9. Paso 8 — UseCase (solo Domain)

`application/usecase/{module}/` + `impl/`

El UseCase **no conoce DTOs**. Recibe/devuelve Domain. Orquesta validator + repo.

```java
@Service
public class CreatePersonUseCaseImpl implements CreatePersonUseCase {
    private final PersonRepository personRepository;
    private final CreatePersonRulesValidator rulesValidator;

    public Mono<PersonDomain> createPerson(PersonDomain person) {
        return rulesValidator.validate(person)
            .then(Mono.defer(() -> personRepository.savePerson(person)));
    }
}
```

---

## 10. Paso 9 — Interactor (DTO ↔ Domain)

`application/primaryports/interactor/{module}/` + `impl/`

Mapea Request→Domain, llama UseCase, mapea Domain→Response.

```java
@Component
public class CreatePersonInteractorImpl implements CreatePersonInteractor {
    private final CreatePersonUseCase createPersonUseCase;
    private final PersonDTOMapper personDTOMapper;

    public Mono<CreatePersonResponse> createPerson(CreatePersonRequest request) {
        var domain = personDTOMapper.toDomain(request);
        return createPersonUseCase.createPerson(domain)
            .map(personDTOMapper::toCreatePersonResponse);
    }
}
```

---

## 11. Paso 10 — Controller

`infrastructure/primaryadapters/controller/{module}/`

- Inyecta **Interactor** (no UseCase).
- `@Valid` en el body.
- Errores → `GlobalExceptionHandler`.

```java
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final CreateAccountInteractor createAccountInteractor;

    @PostMapping
    public Mono<ResponseEntity<CreateAccountResponse>> create(
            @RequestBody @Valid CreateAccountRequest request) {
        return createAccountInteractor.createAccount(request)
            .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }
}
```

---

## 12. Paso 11 — Tests

| Capa | Herramienta |
|------|-------------|
| Domain | JUnit 5, sin Spring |
| UseCase | Mockito + StepVerifier (Domain in/out) |
| Controller / integración | WebTestClient + Testcontainers |

---

## 13. Paso 12 — Migración Flyway

`src/main/resources/db/migration/V{n}__{description}.sql` — tablas/columnas en inglés.

---

## 14. Checklist

- [ ] VO + entidad de dominio + excepciones `create()` + `*Error`
- [ ] Puerto Repository + Entity + mapper manual + Adapter
- [ ] DTOs específicos del caso de uso (sin `fromDomain`)
- [ ] `*DTOMapper`: MapStruct Domain→Response; Request→Domain rico en método concreto si aplica
- [ ] RulesValidator con validaciones **con estado** (repos inline)
- [ ] UseCase solo Domain (sin DTO, sin Entity)
- [ ] Interactor: DTO→Domain → UseCase → Domain→Response
- [ ] Controller consume Interactor + `@Valid`
- [ ] Método de negocio explícito (`createAccount`)
- [ ] Tests dominio + use case (StepVerifier)
- [ ] Migración Flyway si hay schema nuevo

### Errores comunes en reactivo

| Error | Evitar |
|-------|--------|
| `.block()` | Nunca en producción |
| Side-effect eager tras `Mono.error` | Usar `Mono.defer(...)` |
| Olvidar `switchIfEmpty` | Mono vacío ≠ error automático |
| Reglas de BD en el UseCase | Moverlas al RulesValidator |
| DTO o Entity en el UseCase | Mapeo en Interactor / Adapter |

---

## 15. Anexo: comparación con guía MVC+JPA

Se revisó una guía de un proyecto similar (Clean Architecture, mismo layout de paquetes) pero **bloqueante** (Spring MVC + JPA + `try-catch`).

### Ya teníamos igual o mejor

| Tema | Ellos | Nosotros |
|------|-------|----------|
| Contratos UC/Interactor | Varias bases (`WithoutInput`/`WithoutReturn`/`WithReturn`) | `UseCaseWithReturn<I,O>` / `InteractorWithReturn<I,O>` reactivos |
| DTOs | Mutables + helpers anti-nulos | `record` + Jakarta Validation |
| Entity↔Domain | MapStruct siempre | Manual (VOs/herencia); MapStruct para DTO↔Domain |
| Errores en Controller | `try-catch` por endpoint | `GlobalExceptionHandler` |
| Multi-tenancy | `suscripcionId` | No aplica |

### Adoptado de ellos

1. Interactor mapea DTO↔Domain; UseCase trabaja con Domain.
2. RulesValidator encapsula validaciones con estado (repos).
3. Factories `create()` en Entities.

### Qué les serviría de nosotros

- Reactivo end-to-end (`Mono`/`Flux`).
- Un solo `GlobalExceptionHandler`.
- VOs como `record` con validación en compact constructor.
- DTOs específicos por caso de uso.