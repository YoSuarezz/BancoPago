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
| Entity R2DBC | `infrastructure/secondaryadapters/r2dbc/entity/` |
| Mapper Entity↔Domain | `infrastructure/secondaryadapters/r2dbc/mapper/` (**manual**) |
| Spring Data repo | `infrastructure/secondaryadapters/r2dbc/{module}/` |
| Adapter que implementa el puerto | `infrastructure/secondaryadapters/r2dbc/{module}/` |

`secondaryports` solo contiene **interfaces** de repositorio (`repository/`). Las entidades R2DBC
dependen de Spring Data (`@Table`, `@Column`, `Persistable`) y son detalle del adaptador, no del
contrato de aplicación — por eso viven en infraestructura junto al adapter que las usa.

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

- Domain→Response 1:1 puro → `interface` MapStruct (`AccountDTOMapper`)
- Request→Domain con herencia/VOs → `abstract class` + método concreto (`PersonDTOMapper.toPersonDomain`)
- Entity↔Domain: **manual** (no MapStruct)
- No crear Assembler/Factory globales; solo si un Response combina varios agregados

```java
@Mapper(componentModel = "spring")
public interface AccountDTOMapper {
    CreateAccountResponse toCreateAccountResponse(AccountDomain domain);
    GetAccountBalanceResponse toGetAccountBalanceResponse(AccountDomain domain);
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
    private final UniqueAccountTypePerOwnerRule uniqueAccountTypePerOwnerRule;
    private final UniqueAccountNumberRule uniqueAccountNumberRule;

    public Mono<Void> validate(AccountDomain account) {
        return ownerExistsRule.validate(account.getOwnerId())
            .then(maxAccountsPerOwnerRule.validate(account.getOwnerId()))
            .then(uniqueAccountTypePerOwnerRule.validate(account))
            .then(uniqueAccountNumberRule.validate(account.getNumber()));
    }
}
```

| Tipo | Dónde |
|------|--------|
| Email formato / nombre / clientNumber / position+area / close con saldo 0 | Dominio (VO / entidad) |
| Documento único, email único, owner existe, máx. cuentas, 1 tipo por owner | `*Rule` + `*RuleImpl` orquestados por RulesValidator |
| Cuenta/persona no encontrada al cargar | UseCase (`find` + `switchIfEmpty` → `*NotFoundException`) |
| Transición de estado inválida / close con saldo | Dominio (`block`/`unblock`/`close`) |

---

## 9. Paso 8 — UseCase (solo Domain)

`application/usecase/{module}/` + `impl/`

El UseCase **no conoce DTOs**. Recibe/devuelve Domain (o VO de dominio). Orquesta validator + repo.

La interfaz concreta es **vacía** y solo extiende la base; la impl implementa `execute`:

```java
public interface CreatePersonUseCase
        extends UseCaseWithReturn<PersonDomain, PersonDomain> {
}

@Service
public class CreatePersonUseCaseImpl implements CreatePersonUseCase {
    private final PersonRepository personRepository;
    private final CreatePersonRulesValidator rulesValidator;

    @Override
    public Mono<PersonDomain> execute(PersonDomain person) {
        return rulesValidator.validate(person)
            .then(Mono.defer(() -> personRepository.savePerson(person)));
    }
}
```

---

## 10. Paso 9 — Interactor (DTO ↔ Domain)

`application/primaryports/interactor/{module}/` + `impl/`

Mapea Request→Domain, llama `useCase.execute`, mapea Domain→Response DTO.

```java
public interface CreatePersonInteractor
        extends InteractorWithReturn<CreatePersonRequest, CreatePersonResponse> {
}

@Service
public class CreatePersonInteractorImpl implements CreatePersonInteractor {
    private final CreatePersonUseCase createPersonUseCase;
    private final PersonDTOMapper personDTOMapper;

    @Override
    public Mono<CreatePersonResponse> execute(CreatePersonRequest request) {
        var domain = personDTOMapper.toPersonDomain(request);
        return createPersonUseCase.execute(domain)
            .map(personDTOMapper::toCreatePersonResponse);
    }
}
```

CreateAccount: el Interactor genera el número (`AccountNumberGenerator`), construye `AccountDomain` y llama `useCase.execute(domain)`.

---

## 11. Paso 10 — Response wrapper + Controller

`infrastructure/primaryadapters/adapter/response/`:

| Clase | Rol |
|-------|-----|
| `ApiResponse<T>` | Envelope `{ data, messages }` |
| `HttpResponses` | `created` / `ok` / `okList` → `Mono<ResponseEntity<ApiResponse<T>>>` |
| `SseEvents` | `of(event, data)` / `map(flux, eventName)` → `ServerSentEvent` |

`infrastructure/ResponseMessages.java` — constantes de mensajes de éxito en español. **No quemar literales** en el controller.

`infrastructure/primaryadapters/controller/{module}/`

- Inyecta **Interactor** (no UseCase).
- `@Valid` en el body.
- Errores → `GlobalExceptionHandler`.
- Envuelve con **helpers**, no con `.map(ApiResponse::of)` a mano.

**Regla de capas (obligatoria):**

| Qué | Dónde |
|-----|--------|
| DTO de negocio | Interactor (application) |
| `ApiResponse` / `ResponseEntity` / status HTTP | `HttpResponses` (infrastructure) |
| `ServerSentEvent` / nombre de evento SSE | `SseEvents` (infrastructure) |

**No** mover `ServerSentEvent` ni `ApiResponse` al Interactor: contaminaría application con Spring Web y rompería reutilización desde otros adapters.

```java
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final CreateAccountInteractor createAccountInteractor;
    private final BlockAccountInteractor blockAccountInteractor;
    private final StreamAccountBalanceInteractor streamAccountBalanceInteractor;
    // + GetAccountBalance, ListAccountsByOwner, UnblockAccount, CloseAccount

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<CreateAccountResponse>>> createAccount(
            @RequestBody @Valid CreateAccountRequest request) {
        return HttpResponses.created(
                createAccountInteractor.execute(request),
                ResponseMessages.ACCOUNT_CREATED);
    }

    @PostMapping("/{id}/block")
    public Mono<ResponseEntity<ApiResponse<AccountStatusResponse>>> blockAccount(@PathVariable UUID id) {
        return HttpResponses.ok(
                blockAccountInteractor.execute(id),
                ResponseMessages.ACCOUNT_BLOCKED);
    }

    @GetMapping(value = "/{id}/balance/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<GetAccountBalanceResponse>> streamAccountBalance(@PathVariable UUID id) {
        return SseEvents.map(streamAccountBalanceInteractor.execute(id), SseEvents.BALANCE);
    }
}
```

Métodos del controller: nombres explícitos (`createAccount`, `getAccountBalance`, `blockAccount`, `streamAccountBalance`) — mismo criterio que repos (`saveAccount`). Rutas REST: sustantivos plurales + verbo HTTP; acciones de estado como `POST .../block|unblock|close` (no `PATCH .../estado`). Persons: `POST/GET /api/v1/persons`. Listado: `GET /accounts?ownerId=`. Seguridad local: `SecurityConfig` permitAll (JWT después). CORS: `CorsConfig` para frontend local.

**DTOs de respuesta Person:** sealed interfaces (`CreatePersonResponse`, `GetPersonByIdResponse`) con implementaciones Client/Employee; omitir nulls con `@JsonInclude(NON_NULL)`. `AccountStatusResponse` mínimo: `id`, `number`, `status`, `balance`.

Jakarta en `CreatePersonRequest`: `@NotBlank` + `@Size(max=100)` en name; `@NotBlank` + `@Email` en email.

**SSE:** UseCase/Interactor → `Flux<DTO>` (`UseCaseWithFluxReturn` / `InteractorWithFluxReturn`). Polling en UseCase (`Flux.interval`). Nombre de evento en `SseEvents` (constante `BALANCE`).

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

Módulo 1 actual:
- **V1** — `person` + `account`
- **V2** — campos de subtipo Person (`client_number`, `membership_date`, `position`, `area`, `cost_center`, `contract_type`)
- **V3** — unicidad documento `(document_type, document_number)` + `document_number VARCHAR(30)` (alineado con VO y `UniqueDocumentRule`)

Si hay mismatch de checksum Flyway en local: `docker compose down -v` y recrear (destructivo).

---

## 14. Checklist

- [ ] VO + entidad de dominio + excepciones `create()` + `*Error`
- [ ] Puerto Repository + Entity + mapper manual + Adapter
- [ ] DTOs específicos del caso de uso (sin `fromDomain`)
- [ ] `*DTOMapper`: MapStruct Domain→Response; Request→Domain rico en método concreto si aplica
- [ ] RulesValidator con validaciones **con estado** (repos inline)
- [ ] UseCase solo Domain (sin DTO, sin Entity)
- [ ] Interactor: DTO→Domain → UseCase → Domain→Response (**sin** HTTP / ApiResponse / SSE)
- [ ] Controller consume Interactor + `@Valid` + `HttpResponses` / `SseEvents`
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
| Contratos UC/Interactor | Bases + interfaz vacía + `ejecutar` | Bases + interfaz vacía + `execute` reactivo (`Mono`) |
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