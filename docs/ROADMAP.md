# Roadmap de Módulos — BancoPago

| # | Módulo | Estado | Rama |
|---|--------|--------|------|
| 1 | Gestión de Cuentas y Usuarios | ✅ Completado | `feature/account-balance-sse` |
| 2 | Transferencias P2P | 🔲 Pendiente | - |
| 3 | Nómina de Empleados | 🔲 Pendiente | - |
| 4 | Pagos a Proveedores | 🔲 Pendiente | - |
| 5 | Pagos Recurrentes | 🔲 Pendiente | - |
| 6 | Pagos QR | 🔲 Pendiente | - |
| 7 | Integración PSE | 🔲 Pendiente | - |
| 8 | Auditoría | 🔲 Pendiente | - |
| 9 | Conciliación | 🔲 Pendiente | - |
| 10 | Portal Cliente (Angular) | 🔲 Pendiente | - |
| 11 | Portal Operativo (Angular) | 🔲 Pendiente | - |

## Módulo 1 — Gestión de Cuentas y Usuarios

| # | Tarea | Estado |
|---|------|--------|
| 1.1 | Modelo de dominio: Person + Account con VOs | ✅ Completado |
| 1.2 | Excepciones de dominio + ErrorCode + Layer | ✅ Completado |
| 1.3 | Value Objects | ✅ Completado |
| 1.4 | Puertos de repositorio reactivos | ✅ Completado |
| 1.5 | Tests unitarios de dominio | ✅ Completado |
| 1.6 | Entidades R2DBC + adaptadores | ✅ Completado |
| 1.7 | Migraciones Flyway (V1–V3: schema + subtype fields + document unique) | ✅ Completado |
| 1.8 | Mappers Entity↔Domain manuales (incl. campos Client/Employee) | ✅ Completado |
| 1.9 | Documentación viva (ARCHITECTURE, DECISIONS, GUIDE, ROADMAP, ISSUE_TEMPLATE) | ✅ Completado |
| 1.10 | Casos de uso + Interactors + RulesValidators (Person/Account) | ✅ Completado (`feature/person-account-use-cases`) |
| 1.11 | Controllers REST + GlobalExceptionHandler + SecurityConfig + ResponseMessages | ✅ Completado (`feature/rest-person-account-api`) |
| 1.11b | Persistencia subtipos Person + UniqueAccountTypePerOwner + validaciones domain/Jakarta | ✅ Completado |
| 1.12 | Stream SSE de saldo (`GET .../balance/stream`) + listado por owner | ✅ Completado (`feature/account-balance-sse`) |
| 1.13 | Dashboard Angular de cuentas | ✅ Completado (`feature/account-balance-sse`) |

## Módulo 2 — Transferencias P2P

| # | Tarea | Estado |
|---|------|--------|
| 2.1 | Modelo de dominio: Payment + Transfer | 🔲 Pendiente |
| 2.2 | Idempotencia con Redis | 🔲 Pendiente |
| 2.3 | Use case de transferencia con lock | 🔲 Pendiente |
| 2.4 | Endpoints REST | 🔲 Pendiente |
| 2.5 | Tests unitarios + integración | 🔲 Pendiente |
| 2.6 | Formulario Angular de transferencia | 🔲 Pendiente |

**Leyenda:** 🔲 Pendiente · 🟡 En Progreso · ✅ Completado

**Docs de arquitectura:** [`ARCHITECTURE.md`](./ARCHITECTURE.md) · [`ARCHITECTURE_DECISIONS.md`](./ARCHITECTURE_DECISIONS.md) · [`IMPLEMENTATION_GUIDE.md`](./IMPLEMENTATION_GUIDE.md)
