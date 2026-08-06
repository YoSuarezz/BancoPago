# BancoPago — Sistema Integral de Pagos
## Proyecto de Práctica · Bancolombia SWE Backend Junior

> **Propósito:** Construir un sistema de pagos realista que demuestre dominio de los requisitos explícitos e implícitos del problema.

---

## Por qué este proyecto específico

Los pagos digitales en Bancolombia no son solo transferencias entre personas. Incluyen:

- Pagos entre clientes del banco (P2P, QR).
- Pagos de clientes hacia comercios y servicios (PSE, débito automático).
- Pagos del banco hacia sus propios empleados (nómina).
- Pagos del banco hacia proveedores (cuentas por pagar).
- Pagos internos entre áreas del banco (tesorería, centros de costo).
- Pagos programados y recurrentes de clientes.

Este proyecto cubre todos esos flujos.

---

## Nombre del proyecto: BancoPago

**BancoPago** es una plataforma de pagos que sirve a tres tipos de usuarios:

| Portal | Usuarios | Qué pueden hacer |
|--------|----------|-----------------|
| **Portal Cliente** | Clientes del banco | Transferir, pagar servicios, pagar con QR, programar pagos |
| **Portal Empleado** | Empleados de Bancolombia | Ver su nómina, historial de pagos, certificados laborales |
| **Portal Operativo** | Usuarios funcionales, tesorería, RRHH, admin | Procesar nóminas, gestionar proveedores, conciliar, auditar |

---

## Stack tecnológico

```
Backend
├── Spring WebFlux (Reactor) ← Requisito explícito
├── Spring Security (JWT + Roles)
├── R2DBC + PostgreSQL (acceso reactivo a BD)
├── Spring Boot Actuator (métricas, health)
├── Resilience4j (Circuit Breaker, Retry)
├── MapStruct (mapeo DTO ↔ Domain)
├── Flyway (migraciones versionadas)
├── Helpers antinulos (TextHelper, ObjectHelper)
└── Springdoc OpenAPI (documentación API)

Frontend
├── Angular 17+ ← Requisito explícito
├── RxJS (Observables, operadores)
├── Angular Material o Tailwind
└── Angular Reactive Forms

Testing
├── JUnit 5 + Mockito + StepVerifier (backend) ← Requisito explícito
├── Jest + Angular Testing (frontend) ← Requisito explícito
├── Playwright E2E ← Requisito explícito
├── Karate (API contract testing) ← Requisito explícito
└── JMeter (performance) ← Requisito explícito

Infraestructura
├── Docker + Docker Compose (local)
├── PostgreSQL (base de datos relacional) ← Requisito explícito
└── Redis (idempotencia, caché de sesiones)

Documentación
├── OpenAPI/Swagger (contratos de API)
├── Diagrama ER
├── Diagramas UML (clases, casos de uso)
└── Diagrama de componentes
```

---

## Modelo de Dominio — Entidades Centrales

### Value Objects (inmutables, autovalidables en compact constructor)

```
Email              → valida formato, normaliza lowercase, max 100 chars
DocumentNumber     → DocumentType + value, valida no vacío, max 30 chars
AccountNumber      → valida no vacío
Money              → BigDecimal amount + Currency, add/subtract con currency check
```

### Entidades de Dominio (con VOs en vez de tipos primitivos)

```
PersonDomain (abstract)
    ├── ClientDomain (clientNumber, membershipDate)
    └── EmployeeDomain (position, area, costCenter, contractType)

AccountDomain (belongs to a Person)
    ├── AccountNumber number (VO)
    ├── AccountType type: SAVINGS | CHECKING | PAYROLL | TREASURY | VENDOR
    ├── Money balance (VO, amount + currency)
    └── AccountStatus status: ACTIVE | INACTIVE | BLOCKED | FROZEN

PAGO (evento transaccional — el corazón del sistema)
    ├── id: UUID
    ├── idempotency_key: String (único por intento)
    ├── tipo: TRANSFERENCIA | NOMINA | PROVEEDOR | PSE | QR | RECURRENTE | DEBITO_AUTO
    ├── cuenta_origen_id
    ├── cuenta_destino_id
    ├── monto: DECIMAL(15,2)
    ├── moneda: COP | USD
    ├── estado: CREADO | VALIDANDO | PROCESANDO | COMPLETADO | FALLIDO | REVERTIDO
    ├── canal: APP | WEB | API | BATCH | INTERNO
    ├── descripcion
    ├── metadata: JSONB (datos específicos por tipo de pago)
    ├── created_by (usuario que inició)
    ├── created_at
    └── updated_at

LOTE_NOMINA (agrupador de pagos masivos)
    ├── id: UUID
    ├── periodo: YYYY-MM (mes de pago)
    ├── tipo_nomina: ORDINARIA | PRIMA | VACACIONES | CESANTIAS | BONIFICACION
    ├── estado: BORRADOR | APROBADO | PROCESANDO | COMPLETADO | FALLIDO
    ├── total_empleados
    ├── total_monto
    ├── aprobado_por (empleado)
    └── pagos: List<PAGO>

PROVEEDOR
    ├── id: UUID
    ├── razon_social
    ├── nit
    ├── cuenta_bancaria: CUENTA
    ├── tipo: BIENES | SERVICIOS | ARRENDAMIENTO | CONSULTORIA
    └── estado: ACTIVO | INACTIVO | BLOQUEADO

PAGO_RECURRENTE
    ├── id: UUID
    ├── cliente_id
    ├── cuenta_origen_id
    ├── cuenta_destino_id
    ├── monto
    ├── frecuencia: DIARIA | SEMANAL | QUINCENAL | MENSUAL | ANUAL
    ├── proximo_pago: LocalDate
    ├── fecha_fin: LocalDate (puede ser nula)
    └── estado: ACTIVO | PAUSADO | CANCELADO

AUDITORIA
    ├── id: UUID
    ├── entidad: String (PAGO, LOTE_NOMINA, PROVEEDOR...)
    ├── entidad_id: UUID
    ├── accion: CREAR | ACTUALIZAR | APROBAR | REVERTIR | CANCELAR
    ├── usuario_id: UUID
    ├── ip_origen
    ├── datos_antes: JSONB
    ├── datos_despues: JSONB
    └── timestamp

CONCILIACION
    ├── id: UUID
    ├── fecha: LocalDate
    ├── tipo: INTERBANCARIA | PROVEEDORES | TARJETAS | PSE
    ├── total_registros_sistema
    ├── total_registros_externo
    ├── diferencias: Integer
    ├── estado: PENDIENTE | EN_PROCESO | COMPLETADA | CON_DIFERENCIAS
    └── archivo_externo: String (ruta o referencia)
```

---

## Diagrama ER Completo

```
                 ┌──────────────┐
                 │   PERSONA    │
                 │──────────────│
                 │ id (PK)      │
                 │ nombre       │
                 │ documento    │
                 │ tipo_doc     │
                 │ email        │
                 │ telefono     │
                 │ tipo         │ ← CLIENTE | EMPLEADO
                 └──────┬───────┘
                        │ 1
                        │
                        N
                 ┌──────┴───────┐
                 │    CUENTA    │
                 │──────────────│
                 │ id (PK)      │
                 │ persona_id   │ (FK)
                 │ numero       │ (UNIQUE)
                 │ tipo         │
                 │ saldo        │
                 │ moneda       │
                 │ estado       │
                 └──────┬───────┘
                        │ 1
           ┌────────────┤
           │            │
           N            N
    ┌──────┴──────┐    ┌┴───────────────┐
    │   PAGO      │    │ PAGO_RECURRENTE │
    │─────────────│    │────────────────│
    │ id (PK)     │    │ id (PK)        │
    │ tipo        │    │ cuenta_orig_id │
    │ cuenta_orig │    │ cuenta_dest_id │
    │ cuenta_dest │    │ monto          │
    │ monto       │    │ frecuencia     │
    │ estado      │    │ proximo_pago   │
    │ canal       │    │ estado         │
    │ idempot_key │    └────────────────┘
    │ lote_id     │ (FK nullable)
    │ metadata    │
    └──────┬──────┘
           │ N
           │
           1
    ┌──────┴──────┐
    │ LOTE_NOMINA │
    │─────────────│
    │ id (PK)     │
    │ periodo     │
    │ tipo_nomina │
    │ estado      │
    │ total_monto │
    └─────────────┘

    ┌──────────────┐
    │  PROVEEDOR   │
    │──────────────│
    │ id (PK)      │
    │ razon_social │
    │ nit (UNIQUE) │
    │ cuenta_id    │ (FK → CUENTA)
    │ tipo         │
    │ estado       │
    └──────────────┘

    ┌──────────────┐     ┌──────────────┐
    │  AUDITORIA   │     │ CONCILIACION │
    │──────────────│     │──────────────│
    │ id (PK)      │     │ id (PK)      │
    │ entidad      │     │ fecha        │
    │ entidad_id   │     │ tipo         │
    │ accion       │     │ diferencias  │
    │ usuario_id   │     │ estado       │
    │ datos_antes  │     └──────────────┘
    │ datos_despues│
    │ timestamp    │
    └──────────────┘
```

---

## Roles y Permisos (Spring Security)

```
ROL_CLIENTE
  - Ver sus propias cuentas y saldo
  - Realizar transferencias desde sus cuentas
  - Pagar servicios (PSE, débito automático)
  - Pagar con QR
  - Configurar pagos recurrentes
  - Ver historial de pagos propios

ROL_EMPLEADO
  - Ver sus pagos de nómina
  - Descargar certificados laborales
  - Ver sus datos salariales

ROL_RRHH
  - Crear y gestionar lotes de nómina
  - Aprobar/rechazar nóminas (con límite de monto)
  - Ver nóminas históricas de todos los empleados
  - Gestionar tipos de nómina (prima, vacaciones, etc.)

ROL_TESORERIA
  - Gestionar pagos a proveedores
  - Aprobar pagos superiores a umbral
  - Ejecutar conciliaciones
  - Ver posición de liquidez en tiempo real
  - Gestionar cuentas de tesorería

ROL_ADMIN
  - Gestión completa de usuarios y roles
  - Configurar límites de pago por canal/rol
  - Ver auditoría completa del sistema
  - Activar/desactivar servicios
```

---

## Módulos del Proyecto

### MÓDULO 1 — Gestión de Cuentas y Usuarios

**Funcionalidades:**
- CRUD de personas (clientes y empleados).
- Apertura y cierre de cuentas por tipo.
- Consulta de saldo en tiempo real (reactivo, streaming SSE si está conectado).
- Bloqueo/desbloqueo/cierre de cuentas mediante métodos de dominio (`block()`, `unblock()`, `close()`).

**Lo que demuestra:**
- Clean Architecture con entidades de dominio + Value Objects (`Email`, `DocumentNumber`, `AccountNumber`, `Money`).
- Validación en 3 niveles: Jakarta (infra) → VOs (dominio) → DomainRules (app).
- Manejo de errores por módulo (`AccountError`, `PersonError`) con mensajes en español.
- Excepciones con Layer enum (`Layer.DOMAIN`) + patrón `create()`.
- WebFlux con Flux para streaming de saldo (SSE en tiempo real).
- Spring Security con roles.
- Angular con guards por rol.

**Endpoints backend:**
```
POST   /api/v1/personas
GET    /api/v1/personas/{id}
POST   /api/v1/cuentas
GET    /api/v1/cuentas/{id}/saldo         ← Mono<BigDecimal>
GET    /api/v1/cuentas/{id}/saldo/stream  ← Flux<SaldoEvent> (SSE)
GET    /api/v1/clientes/{id}/cuentas      ← Flux<AccountDTO>
PATCH  /api/v1/cuentas/{id}/estado
```

**Pantallas Angular:**
- `CuentasDashboardComponent`: lista de cuentas con saldos actualizados via SSE.
- `DetalleCuentaComponent`: historial de movimientos paginado.
- `AdminCuentasComponent`: panel de administración para ROL_ADMIN.

---

### MÓDULO 2 — Transferencias P2P

**El módulo central. Aquí demuestras dominio completo.**

**Funcionalidades:**
- Transferencia inmediata entre cuentas.
- Validación de saldo (con locking pesimista para evitar race conditions).
- Idempotencia con Idempotency-Key (el frontend Angular lo genera).
- Límites de transferencia por canal y rol.
- Transferencia programada (ejecutar en fecha futura).
- Notificación al destinatario al completarse.
- Reverso de transferencia (dentro de ventana de tiempo configurable).

**Lo que demuestra:**
- Transacciones reactivas con R2DBC.
- Manejo correcto de race conditions (SELECT FOR UPDATE equivalente en R2DBC).
- Idempotencia real con Redis.
- Manejo de errores (SaldoInsuficiente, CuentaBloqueada, LimiteExcedido).
- Testing completo: unitario, integración, contrato (Karate), E2E (Playwright).

**Flujo de la transferencia:**
```
Angular envía POST con Idempotency-Key
         ↓
IdempotencyFilter verifica Redis
   ├── Si existe → retorna respuesta guardada (200)
   └── Si no existe → continúa
         ↓
TransferenciaUseCase
   ├── Validar montos y límites
   ├── Buscar cuenta origen (R2DBC) → con lock
   ├── Verificar saldo disponible
   ├── Buscar cuenta destino
   ├── Registrar Pago en estado PROCESANDO
   ├── Debitar origen
   ├── Acreditar destino
   ├── Actualizar estado a COMPLETADO
   └── Publicar evento (notificación async)
         ↓
Guardar respuesta en Redis (TTL 24h)
         ↓
Retornar respuesta al cliente
```

**Endpoints:**
```
POST   /api/v1/transferencias                    ← Crear transferencia
GET    /api/v1/transferencias/{id}               ← Consultar estado
GET    /api/v1/cuentas/{id}/transferencias       ← Historial paginado
POST   /api/v1/transferencias/{id}/reverso       ← Reverso
GET    /api/v1/transferencias/stream             ← SSE: notificaciones en tiempo real
```

**Tests a implementar:**
```java
// JUnit 5 + Mockito
TransferenciaServiceTest:
  - deberiaTransferirExitosamente()
  - deberiaFallarConSaldoInsuficiente()
  - deberiaFallarConCuentaBloqueada()
  - deberiaFallarConLimiteDiarioExcedido()
  - deberiaRechazarTransferenciaRepetidaConMismoIdempotencyKey()
  - deberiaRevertirTransferenciaExitosamente()
  - deberiaFallarReversoCuandoVencioVentanaDeTiempo()

// StepVerifier (WebFlux)
TransferenciaReactiveTest:
  - deberiaRetornarMonoConRespuesta()
  - deberiaRetornarErrorCuandoSaldoInsuficiente()
  - deberiaEmitirEventoEnFluxCuandoCompletada()
```

```gherkin
# Karate
Scenario: Idempotencia en transferencias
  * def key = java.util.UUID.randomUUID().toString()
  Given request con Idempotency-Key = key
  When method POST /api/v1/transferencias
  Then status 201
  * def primeraRespuesta = response.id

  Given request con Idempotency-Key = key (misma)
  When method POST /api/v1/transferencias
  Then status 200
  And match response.id == primeraRespuesta
```

```typescript
// Playwright E2E
test('debe completar transferencia y mostrar confirmación', async ({ page }) => {
  await page.goto('/transferencias/nueva');
  await page.fill('[data-testid="cuenta-destino"]', '001-789012');
  await page.fill('[data-testid="monto"]', '100000');
  await page.click('[data-testid="btn-transferir"]');
  await expect(page.locator('[data-testid="confirmacion"]')).toBeVisible();
});
```

**Pantallas Angular:**
- `NuevaTransferenciaComponent`: formulario reactivo con validación en tiempo real.
- `ConfirmacionTransferenciaComponent`: resumen antes de confirmar (UX crítico).
- `HistorialTransferenciasComponent`: tabla paginada con filtros.
- `DetalleTransferenciaComponent`: estado, timeline del proceso, opción de reverso.

---

### MÓDULO 3 — Nómina de Empleados (Pagos Masivos)

**El módulo más diferenciador. Pagos internos del banco a sus empleados.**

**Funcionalidades:**
- Creación de lote de nómina por período.
- Importación de archivo CSV/Excel con empleados y salarios.
- Cálculo automático de deducciones (salud, pensión, retención) — simplificado.
- Flujo de aprobación: RRHH crea → Supervisor aprueba → Tesorería libera fondos → Procesamiento masivo.
- Procesamiento reactivo en batch (Flux que procesa cada empleado).
- Reintentos automáticos para pagos fallidos.
- Reporte de nómina por empleado (PDF downloadable desde el Portal Empleado).
- Tipos de nómina: ordinaria mensual, prima de servicios, vacaciones, cesantías, bonificaciones.

**Lo que demuestra:**
- Procesamiento masivo con Flux (no bloquea el hilo por cada pago).
- Flujo de aprobación multi-nivel (saga simple).
- Manejo de errores parciales (10 pagos de 1000 fallaron — qué hacer).
- Roles y permisos complejos (RRHH vs Tesorería vs Empleado).
- R2DBC para inserción masiva eficiente.
- Angular con dashboard de progreso en tiempo real (SSE).

**Flujo de aprobación del lote:**
```
RRHH crea lote (BORRADOR)
        ↓
RRHH sube CSV de empleados y montos
        ↓
Sistema valida: empleados existen, cuentas activas, fondos en cuenta tesorería
        ↓
RRHH envía a aprobación (PENDIENTE_APROBACION)
        ↓
Supervisor RRHH aprueba (APROBADO)
        ↓
Tesorería libera fondos (FONDOS_LIBERADOS)
        ↓
Sistema procesa lote (PROCESANDO)
   Flux<PagoEmpleado>
     .flatMap(pago -> procesarPago(pago), 50) // 50 pagos simultáneos
     .onErrorContinue((error, pago) -> marcarFallido(pago, error))
     ↓
Estado final: COMPLETADO (con o sin errores parciales)
        ↓
Empleados reciben notificación en su portal
```

**Endpoints:**
```
POST   /api/v1/nominas/lotes                          ← Crear lote
POST   /api/v1/nominas/lotes/{id}/empleados           ← Subir CSV
GET    /api/v1/nominas/lotes/{id}/vista-previa        ← Flux<EmpleadoDTO>
POST   /api/v1/nominas/lotes/{id}/aprobar             ← Aprobar (RRHH Supervisor)
POST   /api/v1/nominas/lotes/{id}/liberar-fondos      ← Tesorería
POST   /api/v1/nominas/lotes/{id}/procesar            ← Ejecutar pagos
GET    /api/v1/nominas/lotes/{id}/progreso            ← SSE: progreso en tiempo real
GET    /api/v1/nominas/lotes/{id}/resultado           ← Resumen: exitosos/fallidos
POST   /api/v1/nominas/lotes/{id}/reprocesar-fallidos ← Reintentar fallidos
GET    /api/v1/empleados/{id}/nominas                 ← Historial del empleado
GET    /api/v1/empleados/{id}/nominas/{periodo}/comprobante ← PDF
```

**Pantallas Angular:**
- `LotesNominaComponent` (ROL_RRHH): lista de lotes, crear nuevo, ver estado.
- `CrearLoteNominaComponent` (ROL_RRHH): wizard paso a paso.
- `ProgresoProcesamiento` (ROL_RRHH): dashboard con barra de progreso en tiempo real via SSE.
- `MiNominaComponent` (ROL_EMPLEADO): historial de pagos recibidos.
- `ComprobanteNominaComponent` (ROL_EMPLEADO): visualizar y descargar comprobante.
- `AprobacionNominaComponent` (ROL_SUPERVISOR): bandeja de lotes pendientes de aprobación.

**Tests a implementar:**
```java
LoteNominaServiceTest:
  - deberiaCrearLoteNominaExitosamente()
  - deberiaValidarEmpleadosAntesDeAprobar()
  - deberiaFallarSiNoHayFondosSuficientesEnTesoreria()
  - deberiaProcesarPagosEnParaleloConFlux()
  - deberiaManejarErroresParciales_completarLoteConFallidos()
  - deberiaReprocesarSoloPagosFallidos()
  - deberiaCalcularCorrectamenteDeducciones()
```

---

### MÓDULO 4 — Pagos a Proveedores

**Pagos del banco hacia empresas externas que le prestan servicios.**

**Funcionalidades:**
- CRUD de proveedores con validación de NIT.
- Factura → Orden de pago → Aprobación → Pago.
- Límites de aprobación por nivel (RRHH hasta $10M, Tesorería hasta $100M, Gerencia sin límite).
- Programación de pagos (pagar el día 15 y 30 de cada mes).
- Pagos en lote (pagar todas las facturas vencidas de una vez).
- Integración simulada con PSE para pagos a proveedores externos.
- Dashboard de cuentas por pagar: vencidas, por vencer, pagadas.

**Lo que demuestra:**
- Flujo de aprobación con estados (Saga pattern).
- Reglas de negocio complejas (escalación según monto).
- Fechas y programación temporal.
- Integración con servicio externo simulado (PSE mock).
- Circuit Breaker con Resilience4j para la integración externa.

**Endpoints:**
```
POST   /api/v1/proveedores                              ← Crear proveedor
GET    /api/v1/proveedores/{id}
POST   /api/v1/proveedores/{id}/facturas                ← Registrar factura
GET    /api/v1/proveedores/facturas/pendientes          ← Por vencer/vencidas
POST   /api/v1/pagos-proveedores                       ← Crear orden de pago
POST   /api/v1/pagos-proveedores/{id}/aprobar          ← Aprobar (según nivel)
POST   /api/v1/pagos-proveedores/{id}/ejecutar         ← Ejecutar pago
POST   /api/v1/pagos-proveedores/lote                  ← Pagar todas las vencidas
GET    /api/v1/pagos-proveedores/dashboard             ← KPIs de cuentas por pagar
```

---

### MÓDULO 5 — Pagos Recurrentes y Programados

**Pagos automáticos que el cliente configura una vez y el sistema ejecuta periódicamente.**

**Funcionalidades:**
- Configurar pago recurrente: destino, monto, frecuencia (diario, semanal, mensual, etc.).
- Activar/pausar/cancelar pagos recurrentes.
- Notificación al cliente 3 días antes de la ejecución.
- Ejecución por job programado (@Scheduled reactivo).
- Manejo de fallo: si no hay saldo, notificar y reintentar en 24h o cancelar según configuración.
- Historial de ejecuciones de cada pago recurrente.
- Débito automático: similar pero el origen es el banco (no el cliente), ej: cuota de crédito.

**Lo que demuestra:**
- Tareas programadas con Spring WebFlux (@Scheduled + Reactor).
- Gestión del estado de objetos con ciclo de vida largo.
- Notificaciones proactivas.
- Manejo de fallos controlado con reintentos y fallback.

**Endpoints:**
```
POST   /api/v1/pagos-recurrentes                        ← Configurar
GET    /api/v1/pagos-recurrentes                        ← Mis pagos recurrentes
PATCH  /api/v1/pagos-recurrentes/{id}/estado            ← Pausar/activar/cancelar
GET    /api/v1/pagos-recurrentes/{id}/historial         ← Ejecuciones pasadas
GET    /api/v1/pagos-recurrentes/proximos               ← Próximos a ejecutar (Admin)
```

---

### MÓDULO 6 — Pagos QR

**Pagos entre personas y a comercios mediante código QR.**

**Funcionalidades:**
- Generación de QR dinámico (con monto específico) o estático (el pagador pone el monto).
- El QR contiene: cuenta destino, monto (si es dinámico), descripción, expiración.
- Flujo de pago: escanear QR → confirmar → pago inmediato.
- QR con expiración (5 minutos para completar el pago).
- Historial de pagos QR recibidos (útil para comercios).

**Lo que demuestra:**
- Generación de datos en formato estándar (emvco/qrcode).
- Manejo de expiración con Redis TTL.
- Flujo de tiempo real con WebFlux.
- Caso de uso completo de extremo a extremo.

**Endpoints:**
```
POST   /api/v1/qr/generar                    ← Generar QR (retorna imagen o datos)
GET    /api/v1/qr/{token}                    ← Leer QR (para el pagador)
POST   /api/v1/qr/{token}/pagar              ← Ejecutar pago por QR
GET    /api/v1/qr/recibidos                  ← Historial de cobros QR
```

---

### MÓDULO 7 — Pagos PSE (Integración Externa Simulada)

**Pagos desde cuentas bancarias hacia servicios externos: facturas de servicios públicos, impuestos, compras en línea.**

**Funcionalidades:**
- Integración con proveedor PSE simulado (mock HTTP server).
- Flujo: cliente elige servicio → ingresa referencia → PSE valida → cliente confirma → pago.
- Manejo de estados de PSE: PENDIENTE (mientras el banco procesa) → APROBADO/RECHAZADO.
- Webhook: PSE notifica al banco el resultado asíncrono.
- Circuit Breaker cuando PSE no responde.
- Reconciliación diaria con el proveedor.

**Lo que demuestra:**
- Integración con servicios externos (el patrón más común en pagos).
- WebhookController: recibir notificaciones asíncronas.
- Circuit Breaker con Resilience4j.
- Manejo de estados asíncronos (el cliente espera, el backend espera a PSE).
- Reconciliación: comparar lo que el banco registró vs lo que PSE reporta.

**Endpoints:**
```
GET    /api/v1/pse/servicios                         ← Catálogo de servicios
POST   /api/v1/pse/validar-referencia                ← Validar referencia de pago
POST   /api/v1/pse/iniciar-pago                      ← Iniciar transacción
GET    /api/v1/pse/pagos/{id}/estado                 ← Consultar estado (polling)
POST   /api/v1/webhooks/pse/notificacion             ← Webhook de PSE (interno)
GET    /api/v1/pse/reconciliacion/{fecha}            ← Diferencias del día
```

---

### MÓDULO 8 — Auditoría y Trazabilidad

**Registro inmutable de todo lo que pasa en el sistema.**

**Funcionalidades:**
- Registro automático de cada operación (con Spring AOP).
- Búsqueda de auditoría por: usuario, entidad, acción, rango de fechas.
- Ver el "antes y después" de cada cambio de estado.
- Alertas de auditoría: accesos fuera de horario, montos inusuales, múltiples intentos fallidos.
- Exportar reporte de auditoría (para revisión regulatoria).

**Lo que demuestra:**
- Spring AOP (Aspect-Oriented Programming) para interceptar operaciones.
- JSONB en PostgreSQL para almacenar estados anterior/posterior.
- Consultas complejas con filtros dinámicos.
- Concepto de pista de auditoría (cumplimiento regulatorio — PCI DSS).

**Implementación con AOP:**
```java
@Aspect
@Component
public class AuditoriaAspect {

    private final AuditoriaRepository auditoriaRepository;

    @AfterReturning(
        pointcut = "@annotation(Auditable)",
        returning = "resultado"
    )
    public void registrarAuditoria(JoinPoint joinPoint, Object resultado) {
        // Capturar automáticamente quién hizo qué y cuándo
        AuditoriaEvent evento = AuditoriaEvent.builder()
            .entidad(obtenerNombreEntidad(joinPoint))
            .accion(obtenerAccion(joinPoint))
            .usuarioId(SecurityContextHolder.getContext()
                .getAuthentication().getName())
            .timestamp(LocalDateTime.now())
            .datosResultado(serializar(resultado))
            .build();
        auditoriaRepository.save(evento).subscribe();
    }
}

// Uso:
@Auditable
public Mono<Transferencia> transferir(TransferenciaRequest req) { ... }
```

---

### MÓDULO 9 — Conciliación

**Proceso de comparar lo que el sistema interno registró vs lo que los sistemas externos reportan.**

**Funcionalidades:**
- Cargar archivo externo (PSE, ACH, interbancario) en formato CSV.
- Comparar automáticamente con los registros de BancoPago.
- Identificar: transacciones que el sistema tiene pero el externo no, y viceversa.
- Clasificar diferencias: montos, estados, tiempos.
- Generar reporte de diferencias para revisión humana.
- Marcar diferencias como resueltas.
- Dashboard de conciliación: % de coincidencia por día.

**Lo que demuestra:**
- Procesamiento de archivos con Spring WebFlux (stream processing).
- SQL complejo: comparar dos conjuntos de datos.
- Concepto crítico en pagos: sin conciliación no sabes si tu sistema es correcto.
- Roles: solo Tesorería puede ejecutar y ver conciliaciones.

---

### MÓDULO 10 — Portal Clientes (Angular)

**La cara visible del sistema para los clientes del banco.**

**Pantallas:**

1. **Login / Autenticación**
    - Formulario de login (reactive form).
    - Manejo de JWT: guardar, renovar, expirar.
    - Guard: redirigir al login si no está autenticado.

2. **Dashboard Cliente**
    - Resumen de cuentas y saldos (con actualizaciones en tiempo real via SSE).
    - Últimos movimientos (Flux paginado).
    - Accesos rápidos: Transferir, Pagar QR, Programar pago.

3. **Nueva Transferencia** (el flujo más importante)
    - Paso 1: Ingresar datos (cuenta destino, monto, descripción).
    - Paso 2: Confirmar (mostrar resumen, nombre del destinatario).
    - Paso 3: Autenticar (OTP simulado o contraseña).
    - Paso 4: Resultado (éxito o error con opción de reintentar).
    - Generación de Idempotency-Key antes de enviar.

4. **Historial de Transacciones**
    - Tabla paginada con filtros (fecha, tipo, estado, monto).
    - Búsqueda en tiempo real (debounceTime + switchMap).
    - Detalle de cada transacción con timeline de estados.

5. **Pagos Recurrentes**
    - Lista de pagos configurados.
    - Crear/editar/pausar/cancelar.
    - Próximas ejecuciones con calendario.

6. **Pagos QR**
    - Generar QR para cobrar.
    - Escanear QR para pagar (simulado con input de texto).

---

### MÓDULO 11 — Portal Operativo (Angular)

**Panel para empleados, RRHH, Tesorería y Administradores.**

**Pantallas:**

1. **Dashboard Operativo** (según rol)
    - KPIs en tiempo real: pagos procesados hoy, monto total, errores.
    - Alertas de conciliación pendiente.
    - Lotes de nómina pendientes de aprobación.

2. **Gestión de Nómina** (ROL_RRHH)
    - Lista de lotes con estado.
    - Crear nuevo lote (wizard de pasos).
    - Subir CSV de empleados.
    - Ver progreso de procesamiento en tiempo real.
    - Reporte de nómina procesada.

3. **Aprobación de Nóminas** (ROL_SUPERVISOR)
    - Bandeja de lotes pendientes.
    - Ver detalle (empleados, montos, deducciones).
    - Aprobar o rechazar con comentario.

4. **Gestión de Proveedores** (ROL_TESORERIA)
    - Lista de proveedores.
    - Órdenes de pago pendientes por aprobar.
    - Dashboard de cuentas por pagar.
    - Ejecutar pago o programar.

5. **Conciliación** (ROL_TESORERIA)
    - Cargar archivo externo.
    - Ver diferencias encontradas.
    - Resolver diferencias manualmente.
    - Dashboard: % conciliado por día.

6. **Auditoría** (ROL_ADMIN)
    - Búsqueda de logs con filtros.
    - Ver antes/después de cambios.
    - Exportar reporte.

7. **Mi Nómina** (ROL_EMPLEADO)
    - Historial de pagos recibidos.
    - Descargar comprobante de nómina.
    - Resumen: salario bruto, deducciones, neto.

---

## Estrategia de Testing Completa

### Por módulo — qué testear y con qué herramienta

| Módulo | JUnit 5 | StepVerifier | Jest | Playwright | Karate | JMeter |
|--------|---------|--------------|------|-----------|--------|--------|
| Transferencias | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Nómina | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Proveedores | ✅ | ✅ | ✅ | — | ✅ | — |
| Recurrentes | ✅ | ✅ | ✅ | — | ✅ | — |
| QR | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| PSE | ✅ | ✅ | — | ✅ | ✅ | ✅ |
| Auditoría | ✅ | — | ✅ | — | — | — |

### JMeter — Escenarios de carga

```
Plan de prueba 1: Transferencias concurrentes
  Usuarios concurrentes: 100
  Ramp-up: 30 segundos
  Duración: 5 minutos
  Meta: < 500ms p95, < 1% error rate

Plan de prueba 2: Procesamiento de nómina masiva
  Lote: 5000 empleados
  Procesamiento: reactivo con concurrencia 50
  Meta: completar en < 30 segundos

Plan de prueba 3: Dashboard en tiempo real
  Usuarios conectados via SSE: 200
  Tiempo de conexión: 10 minutos
  Meta: sin memory leaks, latencia < 100ms por evento
```

---

## Documentación a producir

Para esta vacante, la documentación es parte del trabajo. Por cada módulo debes tener:

### 1. Diagrama ER (por módulo)
Ya incluido arriba. Para el proyecto final: un ER completo unificado.

### 2. Diagrama de Secuencia (los 3 flujos más importantes)

**Transferencia P2P:**
```
Cliente  Angular  API Gateway  TransSvc  Redis  CuentaR2DBC  Notif
   |       |            |          |        |         |          |
   |--POST transferencia-->         |        |         |          |
   |       |            |---JWT--->|        |         |          |
   |       |            |          |--GET idempot.--->|          |
   |       |            |          |<--no existe--|   |          |
   |       |            |          |--SELECT FOR UPDATE Cuenta--|
   |       |            |          |<--cuentaOrigen--------------|
   |       |            |          |--UPDATE saldo origen------->|
   |       |            |          |--UPDATE saldo destino------>|
   |       |            |          |--SET idempot.-->|           |
   |       |            |          |--publish evento------------>|
   |<--201 TransferenciaResponse---|        |         |          |
   |       |            |          |        |         |   notif-->|
```

**Procesamiento de Nómina:**
```
RRHH   Angular  NominaSvc    Flux<Pago>  CuentaR2DBC  SSE Stream
  |       |          |             |            |           |
  |--POST lote/procesar->          |            |           |
  |       |          |--Flux.from(empleados)--> |           |
  |       |          |--flatMap(concurrency=50)-|           |
  |       |          |             |--UPDATE saldo x 50 paralelo
  |       |          |--progress event--------->|           |
  |<--SSE progress 1%-------------|             |  <---------|
  |<--SSE progress 10%------------|                          |
  |<--SSE progress 100%-----------|                          |
  |<--200 LoteCompletado----------|                          |
```

### 3. OpenAPI / Swagger
Cada endpoint documentado con:
- Descripción del propósito.
- Request body con ejemplos.
- Todas las respuestas posibles (200, 201, 400, 401, 403, 404, 409, 422, 500).
- Headers requeridos (Authorization, Idempotency-Key).

### 4. Diagrama de Componentes Cloud
```
┌─────────────────────────────────────────────────────────────┐
│                     Infraestructura Local (Docker Compose)  │
│                                                             │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────────────┐  │
│  │  Angular    │  │  BancoPago   │  │    PostgreSQL      │  │
│  │  App        │  │  Backend     │  │    (puerto 5432)   │  │
│  │  :4200      │  │  Spring Boot │  │                    │  │
│  │             │  │  WebFlux     │  │  bancopago_db      │  │
│  └──────┬──────┘  │  :8080       │  └───────────────────┘  │
│         │         └──────┬───────┘                          │
│         │ HTTP           │ R2DBC                           │
│         └────────────────┘                                  │
│                          │ CRUD                             │
│                   ┌──────┴───────┐  ┌───────────────────┐  │
│                   │    Redis     │  │   PSE Mock Server │  │
│                   │    :6379     │  │   (WireMock :9090)│  │
│                   │(idempotencia │  │                    │  │
│                   │ + caché)     │  └───────────────────┘  │
│                   └──────────────┘                          │
└─────────────────────────────────────────────────────────────┘
```

---

## Orden de Implementación Sugerido

El proyecto se puede construir progresivamente. En cada etapa tienes algo funcionando y demostrable.

### Etapa 1 — Esqueleto (Semana 1-2)
```
✅ Estructura de proyecto Spring Boot WebFlux
✅ Módulo de Cuentas: CRUD básico con R2DBC
✅ Spring Security con JWT (registro y login)
✅ Docker Compose con PostgreSQL y Redis
✅ OpenAPI configurado (Swagger UI)
✅ Angular app con routing, guards, interceptor JWT
✅ Módulo de Cuentas en Angular (pantalla simple)
✅ Primer test JUnit 5 y primer test Jest
```

### Etapa 2 — Núcleo (Semana 3-4)
```
✅ Módulo de Transferencias completo (backend + frontend)
✅ Idempotencia real con Redis
✅ Tests completos: JUnit 5 + StepVerifier + Jest + Playwright
✅ Karate para contrato de API de transferencias
✅ GlobalExceptionHandler con todos los errores del dominio
✅ Auditoría automática con AOP
```

### Etapa 3 — Pagos Internos (Semana 5-6)
```
✅ Módulo de Nómina: creación de lote + CSV upload
✅ Flujo de aprobación multi-nivel
✅ Procesamiento reactivo en batch con Flux
✅ SSE para progreso en tiempo real
✅ Portal Empleado: Mi Nómina
✅ JMeter: prueba de carga del procesamiento de nómina
```

### Etapa 4 — Pagos Externos (Semana 7-8)
```
✅ Módulo de Proveedores
✅ Pagos Recurrentes con @Scheduled
✅ PSE simulado con WireMock + Circuit Breaker
✅ Webhook receiver para notificaciones de PSE
✅ Módulo de QR
✅ Playwright E2E para flujos de usuario completos
```

### Etapa 5 — Operaciones (Semana 9-10)
```
✅ Módulo de Conciliación
✅ Dashboard operativo en Angular
✅ Métricas con Actuator + gráficas en Angular
✅ Pruebas de carga completas con JMeter
✅ Documentación completa (ER, secuencia, componentes)
✅ README de proyecto con decisiones de arquitectura
```

---

## README del Proyecto — Lo que debes explicar

El README es lo primero que ve el entrevistador si compartes el repositorio. Debe contener:

```markdown
# BancoPago — Sistema Integral de Pagos

## Descripción
Sistema de pagos que simula las operaciones de un banco digital:
pagos P2P, nómina de empleados, proveedores, QR, PSE y pagos recurrentes.

## Stack
- Backend: Spring Boot 3.x + WebFlux + R2DBC + PostgreSQL
- Frontend: Angular 17 + RxJS + Angular Material
- Testing: JUnit 5, Jest, Playwright, Karate, JMeter
- Infraestructura: Docker Compose

## Decisiones de Arquitectura

### Por qué WebFlux y no Spring MVC
El procesamiento de nómina puede involucrar miles de pagos simultáneos.
Con MVC bloqueante, cada pago bloquea un hilo. Con WebFlux,
el Event Loop maneja miles de pagos concurrentes con pocos hilos.
El operador `.flatMap(pago -> procesarPago(pago), 50)` procesa
50 pagos en paralelo sin bloquear.

### Por qué Clean Architecture
Separar las reglas de negocio (dominio) de la infraestructura permite
testear la lógica de pagos sin BD ni Spring, y cambiar PostgreSQL por
otra BD sin tocar el dominio.

### Por qué idempotencia con Redis y no en PostgreSQL
Redis permite verificar el key antes de iniciar la transacción,
con TTL automático. PostgreSQL requeriría un índice único y
manejar excepciones de constraint, lo cual es más complejo en
un contexto reactivo.

### Por qué R2DBC y no JPA
JPA usa JDBC que es bloqueante. En un servicio WebFlux, usar JPA
bloquearía el Event Loop. R2DBC provee acceso reactivo y
no-bloqueante a PostgreSQL.

## Cómo correr localmente
docker-compose up -d
./mvnw spring-boot:run
cd frontend && ng serve

## Decisiones que cambiaría en producción
- Kafka en lugar de eventos síncronos para notificaciones.
- Schema migration con Flyway (no DDL auto).
- Vault para secrets (no variables de entorno).
- Kubernetes para orquestación (no Docker Compose).
- ELK stack para centralización de logs.
```

---

## Qué demuestras con cada módulo en la entrevista

| Módulo | Concepto que demuestras | Pregunta probable del entrevistador |
|--------|------------------------|--------------------------------------|
| Transferencias | WebFlux + idempotencia + race conditions | "¿Cómo evitaste el doble débito?" |
| Nómina batch | Flux concurrente + SSE + roles complejos | "¿Cómo procesarías 100.000 empleados?" |
| PSE | Circuit Breaker + webhook + conciliación | "¿Qué pasa si PSE no responde?" |
| Recurrentes | @Scheduled reactivo + gestión de estado | "¿Qué pasa si no hay saldo el día del cobro?" |
| Auditoría | AOP + JSONB + pista regulatoria | "¿Cómo demuestras que no se alteraron datos?" |
| Angular/JWT | Interceptor + guards + async pipe | "¿Cómo renueves el token sin cerrar sesión?" |
| Testing | Cobertura multi-capa | "¿Cómo testeas que la idempotencia funciona?" |
| Documentación | OpenAPI + ER + UML | "¿Dónde está la documentación de la API?" |

---

## Estructura completa del proyecto

```
bancopago/
├── backend/
│   └── src/main/java/com/bancopago/backend/
│       ├── crosscutting/                      # Infraestructura compartida
│       │   ├── exception/
│       │   │   ├── ErrorCode.java             # Interfaz: getCode(), getMessageTemplate()
│       │   │   ├── Layer.java                 # Enum: DOMAIN, APPLICATION, INFRASTRUCTURE
│       │   │   └── DomainException.java       # Excepción base abstracta
│       │   └── helpers/
│       │       ├── TextHelper.java            # applyTrim(), isBlank(), truncate()
│       │       └── ObjectHelper.java          # getDefault(), requireNonNull()
│       │
│       ├── domain/                            # Lógica de negocio pura
│       │   ├── account/
│       │   │   ├── AccountError.java          # Códigos de error en español
│       │   │   ├── AccountDomain.java         # Entidad con AccountNumber + Money VOs
│       │   │   ├── vo/
│       │   │   │   ├── AccountNumber.java     # Value Object (record)
│       │   │   │   └── Money.java             # Value Object con add/subtract
│       │   │   └── exceptions/
│       │   ├── person/
│       │   │   ├── PersonError.java           # Códigos de error en español
│       │   │   ├── PersonDomain.java          # Entidad abstracta
│       │   │   ├── ClientDomain.java
│       │   │   ├── EmployeeDomain.java
│       │   │   ├── vo/
│       │   │   │   ├── Email.java             # Value Object (record, valida formato)
│       │   │   │   └── DocumentNumber.java    # Value Object (record)
│       │   │   └── exceptions/
│       │   ├── enums/                         # AccountStatus, AccountType, Currency, etc.
│       │   ├── BaseDomain.java                # Clase base UUID
│       │   └── DomainRule.java                # @FunctionalInterface
│       │
│       ├── application/                       # Casos de uso y puertos
│       │   ├── primaryports/
│       │   │   ├── dto/{module}/
│       │   │   │   ├── request/               # Anotaciones Jakarta Validation
│       │   │   │   └── response/              # con fromDomain()
│       │   │   ├── interactor/{module}/       # Interfaces de puerto de entrada
│       │   │   └── mapper/{module}/           # MapStruct DTO ↔ Domain
│       │   ├── secondaryports/
│       │   │   ├── entity/{module}/           # Entidades R2DBC @Table (columnas español)
│       │   │   ├── repository/                # Interfaces de repositorio reactivas
│       │   │   └── mapper/{module}/           # Mapeo manual Entity ↔ Domain
│       │   └── usecase/{module}/
│       │       ├── {UseCase}.java
│       │       ├── impl/
│       │       └── rulesvalidator/
│       │
│       └── infrastructure/
│           ├── primaryadapters/
│           │   ├── controller/{module}/       # Controladores REST con @Valid
│           │   └── adapter/response/          # Wrapper GenericResponse
│           ├── secondaryadapters/
│           │   └── config/                    # Beans, seguridad, WebClient
│           └── GlobalExceptionHandler.java
│
├── frontend/
│   └── src/app/
│       ├── core/
│       │   ├── auth/
│       │   │   ├── auth.service.ts
│       │   │   ├── auth.guard.ts
│       │   │   └── jwt.interceptor.ts
│       │   └── services/
│       │       └── idempotency.service.ts     # Genera UUID por operación
│       ├── shared/
│       │   ├── components/
│       │   │   ├── loading-spinner/
│       │   │   ├── error-banner/
│       │   │   └── amount-input/             # Input formateado COP
│       │   └── pipes/
│       │       └── cop-currency.pipe.ts
│       ├── features/
│       │   ├── transfers/
│       │   ├── payroll/
│       │   ├── vendors/
│       │   ├── qr/
│       │   ├── recurring/
│       │   └── audit/
│       └── portals/
│           ├── customer/                     # Rutas portal cliente
│           └── operations/                   # Rutas portal operativo
│
├── e2e/ (Playwright)
│   ├── transfers.spec.ts
│   ├── payroll.spec.ts
│   └── login.spec.ts
│
├── karate/ (Pruebas de contrato)
│   └── src/test/java/karate/
│       ├── transfers.feature
│       └── payroll.feature
│
├── jmeter/
│   ├── transfers_load_plan.jmx
│   └── payroll_batch_plan.jmx
│
├── docs/
│   ├── ARCHITECTURE.md                        # Documentación completa de arquitectura (ES)
│   ├── ARCHITECTURE_DECISIONS.md              # Decisiones de arquitectura (EN)
│   ├── IMPLEMENTATION_GUIDE.md                # Guía paso a paso (ES)
│   ├── ISSUE_TEMPLATE.md                      # Plantilla de issue reutilizable
│   ├── ROADMAP.md                             # Seguimiento de módulos y tareas
│   ├── openapi.yml
│   ├── er-diagram.png
│   ├── sequence-diagram-transfer.png
│   └── component-diagram.png
│
└── docker-compose.yml
```

---

## Implementación de referencia — Fragmentos clave

### Entidad de Dominio con Value Objects

```java
// AccountDomain usa VOs en vez de tipos primitivos
public class AccountDomain extends BaseDomain {

    private final UUID ownerId;
    private final AccountNumber number;       // VO: valida no vacío
    private final AccountType type;
    private Money balance;                    // VO: amount + currency, operaciones inmutables

    public AccountDomain(UUID id, UUID ownerId, AccountNumber number, AccountType type,
                         Money balance, AccountStatus status) {
        super(id);
        this.ownerId = ObjectHelper.requireNonNull(ownerId, () ->
            InvalidAccountException.create(AccountError.OWNER_REQUIRED));
        this.number = ObjectHelper.requireNonNull(number, () ->
            InvalidAccountException.create(AccountError.NUMBER_EMPTY));
        this.type = ObjectHelper.requireNonNull(type, () ->
            InvalidAccountException.create(AccountError.TYPE_REQUIRED));
        this.balance = ObjectHelper.getDefault(balance, Money::zero);
        this.status = ObjectHelper.getDefault(status, AccountStatus.ACTIVE);
    }

    public void deposit(BigDecimal amount) {
        ensureOperable();
        validatePositiveAmount(amount);
        this.balance = this.balance.add(new Money(amount, this.balance.currency()));
        // Money.add() valida moneda y retorna nuevo Money inmutable
    }

    public void block() {
        if (this.status != AccountStatus.ACTIVE)
            throw InvalidAccountStateException.create(getId(), this.status, "block");
        this.status = AccountStatus.BLOCKED;
    }
}
```

### Value Object (Record autovalidable)

```java
public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0)
            throw InvalidAmountException.create(amount);
        currency = ObjectHelper.getDefault(currency, Currency.COP);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO, Currency.COP);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency))
            throw InvalidAccountException.create(
                AccountError.CURRENCY_MISMATCH, this.currency, other.currency);
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

### Manejo de Errores por Módulo (Mensajes en Español)

```java
// domain/account/AccountError.java
public enum AccountError implements ErrorCode {
    NUMBER_EMPTY("El número de cuenta no puede estar vacío"),
    INSUFFICIENT_BALANCE("Saldo insuficiente: actual=%s, requerido=%s"),
    BLOCKED("La cuenta %s se encuentra bloqueada"),
    INVALID_STATE("No se puede ejecutar '%s' en la cuenta %s con estado %s");

    @Override public String getCode() { return "ACCOUNT_" + name(); }
    @Override public String getMessageTemplate() { return messageTemplate; }
}

// Excepción con patrón create()
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

### Procesamiento Batch Reactivo (Nómina)

```java
@Service
public class ProcessPayrollUseCaseImpl implements ProcessPayrollUseCase {

    private static final int CONCURRENCY = 50;

    public Flux<EmployeePaymentResult> process(String batchId,
                                                FluxSink<ProgressEvent> sink) {
        return payrollBatchRepository.findById(batchId)
            .flatMapMany(batch -> employeeRepository.findByBatchId(batchId))
            .flatMap(employee -> processEmployeePayment(employee)
                .onErrorResume(ex ->
                    Mono.just(EmployeePaymentResult.failed(employee, ex))),
                CONCURRENCY)  // 50 pagos concurrentes, no bloqueante
            .doOnNext(result -> sink.next(new ProgressEvent(result)))
            .doOnComplete(() -> updateBatchStatus(batchId, BatchStatus.COMPLETED))
            .doOnError(ex -> updateBatchStatus(batchId, BatchStatus.FAILED));
    }

    private Mono<EmployeePaymentResult> processEmployeePayment(EmployeePayment employee) {
        return accountRepository.findByIdForUpdate(employee.getOriginAccountId())
            .flatMap(origin -> {
                if (origin.getBalance().compareTo(employee.getNetAmount()) < 0) {
                    return Mono.error(new InsufficientBalanceException(employee.getId()));
                }
                return Mono.zip(
                    accountRepository.debit(origin.getId(), employee.getNetAmount()),
                    accountRepository.credit(employee.getDestAccountId(), employee.getNetAmount()),
                    paymentRepository.save(Payment.completed(employee))
                ).thenReturn(EmployeePaymentResult.success(employee));
            });
    }
}
```

### Angular — Dashboard de Progreso de Nómina en Tiempo Real

```typescript
@Component({
  selector: 'app-payroll-progress',
  template: `
    <div class="progress-container">
      <mat-progress-bar
        [value]="progress$ | async"
        mode="determinate">
      </mat-progress-bar>
      <p>{{ message$ | async }}</p>

      <div *ngIf="result$ | async as result" class="result">
        <span class="success">✓ {{ result.successful }} payments completed</span>
        <span class="failed" *ngIf="result.failed > 0">
          ✗ {{ result.failed }} payments failed
          <button (click)="retryFailed()">Retry</button>
        </span>
      </div>
    </div>
  `
})
export class PayrollProgressComponent implements OnInit, OnDestroy {

  private readonly destroy$ = new Subject<void>();

  progress$!: Observable<number>;
  message$!: Observable<string>;
  result$!: Observable<BatchResult>;

  constructor(
    private readonly payrollService: PayrollService,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const batchId = this.route.snapshot.paramMap.get('id')!;

    const events$: Observable<ProgressEvent> = this.payrollService
      .streamProgress(batchId)
      .pipe(takeUntil(this.destroy$));

    this.progress$ = events$.pipe(
      map(e => (e.processed / e.total) * 100),
      startWith(0)
    );

    this.message$ = events$.pipe(
      map(e => `Processing ${e.processed} of ${e.total} employees...`)
    );

    this.result$ = events$.pipe(
      filter(e => e.completed),
      map(e => e.result!)
    );
  }

  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }
}
```

### Servicio de Idempotencia (Angular)

```typescript
// Servicio que garantiza que cada operación tenga su UUID único.
// El backend rechaza la segunda llamada con el mismo UUID.

@Injectable({ providedIn: 'root' })
export class IdempotencyService {

  generateKey(): string {
    return crypto.randomUUID();
  }

  // Para operaciones reintentables: mismo key si es retry, nuevo key si es operación fresca
  createTransferHeaders(isRetry: boolean, existingKey?: string): HttpHeaders {
    const key = isRetry && existingKey ? existingKey : this.generateKey();
    return new HttpHeaders({ 'Idempotency-Key': key });
  }
}
```

### Circuit Breaker para PSE

```java
@Service
public class PseIntegrationService {

    @CircuitBreaker(name = "pse", fallbackMethod = "pseFallback")
    @Retry(name = "pse")
    @TimeLimiter(name = "pse")
    public Mono<PseResponse> iniciarPago(PseRequest request) {
        return webClient.post()
            .uri("/pse/v1/pagos")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(PseResponse.class);
    }

    // Fallback cuando PSE no responde: encola para reprocesar
    public Mono<PseResponse> pseFallback(PseRequest request, Exception ex) {
        log.warn("PSE no disponible, encolando pago {}: {}", request.getReferencia(), ex.getMessage());
        return colaPse.encolar(request)
            .thenReturn(PseResponse.pendiente(request.getReferencia()));
    }
}
```

---

## Métricas que deberías poder mostrar al final

Al terminar el proyecto:

- "El sistema procesa nóminas de 5.000 empleados en menos de 8 segundos con concurrencia 50."
- "La API de transferencias maneja 150 TPS con latencia p99 < 800ms según JMeter."
- "Cobertura de pruebas unitarias: >85% en use cases y domain."
- "3 flujos de usuario completos cubiertos con Playwright."
- "8 contratos de API validados con Karate."
- "Sistema probado con 200 conexiones SSE simultáneas sin degradación."

Esas son afirmaciones concretas que demuestran criterio de ingeniería, no solo código que funciona.

---

*BancoPago es el proyecto que conecta todos los puntos: reactive backend, Angular frontend, testing completo y dominio de pagos. Construirlo es preparación para el trabajo real.*