# Plantilla de Issues — BancoPago

Cada issue debe mapear a la arquitectura del proyecto
([`ARCHITECTURE.md`](./ARCHITECTURE.md)).

---

## Ejemplo: Issue completado

### [M1] Modelar dominio Persona y Cuenta

**Objetivo:** Modelar entidades y reglas de Persona/Cuenta sin Spring, R2DBC ni HTTP.

**Criterios de aceptación:**
- [x] `PersonDomain` (abstracta), `ClientDomain`, `EmployeeDomain`, `AccountDomain`
- [x] Enums + Value Objects (`Email`, `DocumentNumber`, `AccountNumber`, `Money`)
- [x] Excepciones con `create()` + `AccountError` / `PersonError`
- [x] Tests unitarios de dominio pasando

**Archivos por capa:**

```
crosscutting/exception/   ErrorCode, Layer, DomainException
domain/account|person|enums/
test/domain/account|person/
```

---

## Plantilla para nuevos issues

```markdown
### [M{{N}}] {{Título}}

**Objetivo:** {{una frase}}

**Criterios de aceptación:**
- [ ] ...

**Archivos esperados por capa:**

```
domain/{module}/
  ├── {Module}Domain.java
  ├── {Module}Error.java
  ├── vo/
  └── exceptions/

application/
  ├── primaryports/
  │   ├── dto/{module}/request|response/
  │   ├── interactor/{module}/(+ impl/)
  │   └── mapper/{module}/
  ├── secondaryports/
  │   ├── entity/ repository/ mapper/
  └── usecase/{module}/
      ├── {UseCase}.java + impl/
      └── rulesvalidator/(+ impl/ + rules/ si 2+ Rule)

infrastructure/primaryadapters/controller/{module}/
```

**Flujo a respetar:**
Controller → Interactor → UseCase → RulesValidator (+ Rule) + Repository + DTOMapper

**Notas:**
- Interactor delgado (solo delega).
- UseCase orquesta; no mete reglas de BD inline.
- `Rule<T>` solo si hay 2+ reglas independientes o reutilización.
- DTOs específicos por caso de uso; sin `fromDomain()` en el DTO.
- Interfaces UseCase/Interactor vacías que extienden la base; impl con `execute`.
```

---

## Checklist al cerrar el issue

1. Código alineado con [`IMPLEMENTATION_GUIDE.md`](./IMPLEMENTATION_GUIDE.md)
2. Tests verdes (`./mvnw test` en lo afectado)
3. Actualizar [`ROADMAP.md`](./ROADMAP.md)
