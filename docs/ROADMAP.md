# Roadmap de Módulos — BancoPago

| # | Módulo | Estado | Rama |
|---|--------|--------|------|
| 1 | Gestión de Cuentas y Usuarios | 🟡 En Progreso | `feature/account-management` |
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

### Sub-tareas
| # | Tarea | Estado |
|---|------|--------|
| 1.1 | Modelo de dominio: Person (Client/Employee) + Account con VOs | ✅ Completado |
| 1.2 | Excepciones de dominio + ErrorCode por módulo (AccountError/PersonError) + Layer | ✅ Completado |
| 1.3 | Value Objects: Email, DocumentNumber, AccountNumber, Money | ✅ Completado |
| 1.4 | Puertos de repositorio reactivos | ✅ Completado |
| 1.5 | Tests unitarios (36 tests) | ✅ Completado |
| 1.6 | Entidades R2DBC + adaptadores | ✅ Completado |
| 1.7 | Migraciones Flyway (V1) | ✅ Completado |
| 1.8 | Mappers Entity↔Domain manuales | ✅ Completado |
| 1.9 | Documentación: ARCHITECTURE.md, IMPLEMENTATION_GUIDE.md, ISSUE_TEMPLATE.md | ✅ Completado |
| 1.10 | Casos de uso (CRUD, block/unblock/close) | 🔲 Pendiente |
| 1.11 | Controllers REST + GlobalExceptionHandler | 🔲 Pendiente |
| 1.12 | Stream SSE de saldo | 🔲 Pendiente |
| 1.13 | Dashboard Angular de cuentas | 🔲 Pendiente |

## Módulo 2 — Transferencias P2P

### Sub-tareas
| # | Tarea | Estado |
|---|------|--------|
| 2.1 | Modelo de dominio: Payment + Transfer | 🔲 Pendiente |
| 2.2 | Idempotencia con Redis | 🔲 Pendiente |
| 2.3 | Use case de transferencia con lock | 🔲 Pendiente |
| 2.4 | Endpoints REST | 🔲 Pendiente |
| 2.5 | Tests unitarios + integración | 🔲 Pendiente |
| 2.6 | Formulario Angular de transferencia | 🔲 Pendiente |

**Leyenda:** 🔲 Pendiente · 🟡 En Progreso · ✅ Completado
