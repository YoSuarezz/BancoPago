# Plantilla de Issues — BancoPago

Usa esta plantilla para crear issues de implementación de módulos o sub-tareas.
Cada issue debe mapear explícitamente a la arquitectura del proyecto.

---

## 📋 Ejemplo: Issue Completado

### [M1] Modelar dominio Persona y Cuenta

**Objetivo:** Modelar las entidades y reglas de negocio de Persona y Cuenta sin depender de Spring, R2DBC, HTTP ni PostgreSQL.

**Criterios de aceptación:**
- [x] Crear entidades de dominio `PersonDomain` (abstracta), `ClientDomain`, `EmployeeDomain`, `AccountDomain`
- [x] Crear enums: `PersonType`, `DocumentType`, `AccountType`, `AccountStatus`, `Currency`
- [x] Crear Value Objects: `Email`, `DocumentNumber`, `AccountNumber`, `Money`
- [x] Validar reglas de negocio en VOs (compact constructor) y entidades
- [x] Una cuenta inicia en estado `ACTIVE` y saldo cero
- [x] No permitir saldos negativos
- [x] Permitir bloquear, desbloquear y cerrar una cuenta mediante métodos del dominio (`block()`, `unblock()`, `close()`)
- [x] Crear excepciones específicas de dominio con patrón `create()`
- [x] Crear errores por módulo en español: `AccountError`, `PersonError` implementando `ErrorCode`
- [x] Agregar `Layer` enum y `getLayer()` en `DomainException`
- [x] No contiene anotaciones de Spring, R2DBC ni HTTP
- [x] 36 tests unitarios pasando

**Archivos creados/modificados por capa:**

```
crosscutting/
└── exception/
    ├── ErrorCode.java          # Interface base para errores
    ├── Layer.java              # Enum DOMAIN / APPLICATION / INFRASTRUCTURE
    └── DomainException.java    # Abstracta base (usa ErrorCode + Layer)

domain/
├── account/
│   ├── AccountError.java       # Errores de cuenta en español
│   ├── AccountDomain.java      # Entidad con AccountNumber + Money VOs
│   ├── vo/
│   │   ├── AccountNumber.java  # VO: valida null/blank
│   │   └── Money.java          # VO: amount+currency, add/subtract
│   └── exceptions/
│       ├── InvalidAccountException.java
│       ├── InvalidAmountException.java
│       ├── InsufficientBalanceException.java
│       ├── AccountBlockedException.java
│       └── InvalidAccountStateException.java
├── person/
│   ├── PersonError.java        # Errores de persona en español
│   ├── PersonDomain.java        # Abstracta con DocumentNumber + Email VOs
│   ├── ClientDomain.java
│   ├── EmployeeDomain.java
│   ├── vo/
│   │   ├── Email.java          # VO: valida formato, normaliza lowercase
│   │   └── DocumentNumber.java # VO: type+value, valida null/blank
│   └── exceptions/
│       └── InvalidPersonException.java
├── enums/
│   ├── PersonType.java
│   ├── DocumentType.java
│   ├── AccountType.java
│   ├── AccountStatus.java
│   └── Currency.java
├── BaseDomain.java
└── DomainRule.java

test/
├── domain/account/AccountTest.java      # 25 tests
└── domain/person/PersonTest.java        # 10 tests
```

---

## 📝 Plantilla para Nuevos Issues

Copia el bloque siguiente y completa los campos marcados con `{{ }}`:

---

### [{{Módulo}}] {{Título corto}}

**Objetivo:**
{{Descripción del propósito del issue — 2-3 líneas}}

**Criterios de aceptación:**
- [ ] {{Criterio 1: ej. "Crear entidad de dominio XxxDomain"}}
- [ ] {{Criterio 2: ej. "Crear XxxError con mensajes en español"}}
- [ ] {{Criterio 3: ej. "Implementar use case YyyUseCase"}}
- [ ] {{...}}
- [ ] Tests unitarios pasando (mínimo {{N}})
- [ ] Tests de integración pasando (si aplica)

**Archivos a crear/modificar por capa:**

```
# ─── CROSSCUTTING ───
crosscutting/
├── helpers/
│   └── {{NuevoHelper.java}} (si se necesita)

# ─── DOMINIO ───
domain/{{modulo}}/
├── {{Modulo}}Error.java             # Errores en español implementando ErrorCode
├── {{Modulo}}Domain.java            # Entidad de dominio con VOs
├── vo/
│   └── {{NuevoVo}.java}            # Value Objects (records)
├── enums/
│   └── {{NuevoEnum}.java}          # Enumeraciones
├── rules/
│   └── {{Regla}Rule.java}          # Reglas de negocio (DomainRule<T>)
└── exceptions/
    └── {{Nombre}Exception.java}    # Excepciones con patrón create()

# ─── APLICACIÓN ───
application/
├── primaryports/
│   ├── dto/{{modulo}}/
│   │   ├── request/
│   │   │   └── {{Operacion}Request.java}   # DTO con Jakarta Validation
│   │   └── response/
│   │       └── {{Operacion}Response.java}   # DTO con fromDomain()
│   ├── interactor/{{modulo}}/
│   │   └── {{Operacion}Interactor.java}    # Interface (puerto primario)
│   └── mapper/{{modulo}}/
│       └── {{Modulo}DTOMapper.java}        # MapStruct DTO ↔ Domain
├── secondaryports/
│   ├── entity/{{modulo}}/
│   │   └── {{Modulo}Entity.java}           # @Table + helpers antinulos
│   ├── repository/
│   │   └── {{Modulo}Repository.java}       # Interface reactiva (puerto secundario)
│   └── mapper/{{modulo}}/
│       └── {{Modulo}EntityMapper.java}     # Manual (nombres español↔inglés)
└── usecase/{{modulo}}/
    ├── {{Operacion}UseCase.java}          # Interface del caso de uso
    ├── impl/
    │   └── {{Operacion}UseCaseImpl.java}  # Implementación reactiva
    └── rulesvalidator/
        ├── {{Operacion}RulesValidator.java}
        └── impl/
            └── {{Operacion}RulesValidatorImpl.java}

# ─── INFRAESTRUCTURA ───
infrastructure/
├── primaryadapters/
│   ├── controller/{{modulo}}/
│   │   └── {{Modulo}Controller.java}      # REST con @Valid
│   └── adapter/response/
│       └── GenericResponse.java           # Wrapper estándar
├── secondaryadapters/
│   └── config/
│       └── {{Config}.java}
└── GlobalExceptionHandler.java            # DomainError → HTTP status

# ─── TESTS ───
test/
├── domain/{{modulo}}/
│   └── {{Modulo}Test.java}               # Reglas de dominio (JUnit 5)
├── application/usecase/{{modulo}}/
│   └── {{Operacion}UseCaseTest.java}     # Orquestación (StepVerifier + Mockito)
└── infrastructure/controller/{{modulo}}/
    └── {{Modulo}ControllerTest.java}      # Integración (WebTestClient)
```

**Dependencias:**
- {{Listar issues previos de los que depende}}

**Checklist rápido:**
- [ ] Value Objects en `vo/` con compact constructor
- [ ] `{{Modulo}}Error.java` con mensajes en español
- [ ] Excepciones con `create()` + `serialVersionUID`
- [ ] DTOs con Jakarta Validation (`@NotBlank`, `@NotNull`)
- [ ] Mapper Entity↔Domain manual (nombres español)
- [ ] Mapper DTO↔Domain con MapStruct
- [ ] Controller con `@Valid`
- [ ] Tests con StepVerifier para flujos reactivos

---

## 🧭 Cómo Usar

1. **Crear el issue** en GitHub: pegar el bloque `[{{Módulo}}] {{Título}}` completo
2. **Reemplazar `{{ }}`** con los valores concretos del módulo
3. **Marcar checklist** a medida que se avanza
4. **Al completar**, mover los archivos de `docs/ROADMAP.md` a ✅
5. **Commit message:** `feat({{modulo}}): {{descripción}}`

### Nomenclatura de commits

```
feat(account): implementar CreateAccount use case
feat(person): agregar regla UniqueDocumentRule
fix(account): corregir validación de saldo negativo
test(account): agregar tests de block/unblock/close
docs: actualizar issue template
```
