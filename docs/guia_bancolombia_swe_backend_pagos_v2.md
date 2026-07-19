# Guía de Estudio — Ingeniero/a de Software Backend de Pagos TI (Nivel 1 - Junior)
## Bancolombia · Grupo Bancolombia · Colombia · 2026

> **Filosofía de esta guía:** No prepararte para *pasar* una entrevista, sino para *pensar* como el Ingeniero Backend de Pagos que Bancolombia quiere contratar. La entrevista deja de ser un cuestionario cuando puedes justificar cada decisión técnica, hablar de frontend con criterio y demostrar que ya conoces el dominio.

---

## Análisis de la vacante — Qué piden realmente

La descripción del cargo tiene dos capas:

**Conocimientos requeridos (la base mínima):**
- Java Spring Boot **reactivo** — no solo MVC clásico.
- Angular — frontend real, no decorativo.
- SQL — manejo de bases de datos relacionales.

**Lo que describe el rol (lo que separa Junior de Aprendiz):**
- Ciclo de vida completo: analizar → diseñar → codificar → probar → desplegar → estabilizar.
- Documentar: diagramas ER, casos de uso, APIs, clases, componentes Cloud.
- Proponer mejoras a soluciones existentes con criterio.
- Ser referente técnico del equipo.
- Gestionar bugs con sistemas de tracking.

Eso significa que no basta saber escribir código. Debes poder diseñarlo, documentarlo, probarlo y defenderlo.

---

## Ruta de Módulos actualizada

| # | Módulo | Tipo | Prioridad |
|---|--------|------|-----------|
| 0 | Mentalidad para entrevistas técnicas | Transversal | 🔴 Alta |
| 1 | Java Profundo (Core + Moderno + SOLID) | Backend | 🔴 Alta |
| 2A | Spring Boot MVC y fundamentos | Backend | 🔴 Alta |
| 2B | Spring WebFlux / Reactive (NUEVO REQUISITO) | Backend | 🔴 Alta |
| 3 | Arquitectura: Clean + Hexagonal | Diseño | 🔴 Alta |
| 4 | Bases de Datos SQL Profundo | Datos | 🔴 Alta |
| 5 | APIs REST: diseño, versionado, idempotencia | Backend | 🔴 Alta |
| 6A | Angular: Fundamentos (NUEVO) | Frontend | 🔴 Alta |
| 6B | Angular: Avanzado + RxJS (NUEVO) | Frontend | 🟡 Media-Alta |
| 7A | Testing: JUnit 5 + Mockito | Testing | 🔴 Alta |
| 7B | Testing: Jest para Angular (NUEVO) | Testing | 🔴 Alta |
| 7C | Testing: Playwright E2E (NUEVO) | Testing | 🟡 Media-Alta |
| 7D | Testing: Karate + JMeter | Testing | 🟡 Media |
| 8 | Documentación Técnica (NUEVO) | Transversal | 🟡 Media-Alta |
| 9 | Pagos Digitales TI (dominio) | Dominio | 🔴 Alta (diferenciador) |
| 10 | Resolución de Problemas en Producción | Ingeniería | 🔴 Alta |
| 11 | System Design Junior | Arquitectura | 🟡 Media-Alta |
| 12 | Casos Reales Bancolombia | Integración | 🔴 Alta |
| 13 | Entrevistas Simuladas | Práctica | 🔴 Alta |
| 14 | Banco de Preguntas (300+) | Referencia | Consulta |
| 15 | Soft Skills + Behavioral | Transversal | 🔴 Alta |
| + | DevOps esencial | Ops | 🟡 Media |
| + | Resilience4j | Backend | 🟡 Media |

---

# MÓDULO 0 — Mentalidad para Entrevistas Técnicas

## Qué evalúa realmente un entrevistador de Bancolombia

No evalúan si memorizaste la documentación de Spring o Angular. Evalúan:

- **Claridad de pensamiento:** ¿Puedes explicar algo complejo de forma simple?
- **Criterio full-stack:** ¿Entiendes cómo backend y frontend se integran y cuáles son las responsabilidades de cada uno?
- **Honestidad técnica:** ¿Admites lo que no sabes sin bloquearte?
- **Visión de ciclo completo:** ¿Puedes hablar de diseño, implementación, pruebas y soporte?
- **Experiencia aplicada:** Tu práctica en Wakanda + Inversiones Patrimoniales es evidencia concreta. Úsala.

## Framework mental para cualquier pregunta técnica

```
1. CONFIRMAR  — "Entiendo que la pregunta es sobre X. ¿Correcto?"
2. ASUMIR     — "Voy a asumir que Y y Z. ¿Está bien?"
3. PROPONER   — "Mi enfoque sería..."
4. JUSTIFICAR — "Elijo esto porque... La alternativa B tiene la desventaja de..."
5. CASOS LÍMITE — "También consideraría concurrencia / errores / escala / seguridad..."
6. ADMITIR con dirección — "No tengo certeza del detalle, pero lo investigaría en X."
```

## Por qué ahora Angular importa en la entrevista

Angular está en los requisitos requeridos. El entrevistador puede preguntarte:

- "¿Cómo consumirías desde Angular la API de transferencias que acabas de describir?"
- "¿Cómo manejas errores HTTP en Angular?"
- "¿Qué es RxJS y qué relación tiene con el modelo reactivo del backend?"

No te pedirán pixel-perfect UI. Te pedirán que demuestres que puedes trabajar en un equipo que tiene frontend.

## Cómo convertir tu práctica en evidencia

Plantilla STAR técnica:
- **Situación:** Contexto del equipo o proyecto en Bancolombia.
- **Tarea:** Qué responsabilidad técnica tenías.
- **Acción:** Qué decisiones tomaste y por qué (arquitectura, patrón, herramienta).
- **Resultado:** Qué mejoró, qué aprendiste, qué habrías hecho diferente.

---

# MÓDULO 1 — Java Profundo

## 1.1 Stack vs Heap

```
Stack                           Heap
Variables locales               Objetos instanciados con new
Referencias                     Arrays
Primitivos (int, long...)       String Pool
Por hilo (thread-safe)          Compartido entre hilos
LIFO                            GC gestiona ciclo de vida
```

## 1.2 Equals, HashCode y el contrato

```java
// Si a.equals(b) == true entonces a.hashCode() == b.hashCode() (obligatorio)
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Cuenta cuenta)) return false;
    return Objects.equals(numeroCuenta, cuenta.numeroCuenta);
}

@Override
public int hashCode() {
    return Objects.hash(numeroCuenta);
}
// Sin esto, dos Cuenta con el mismo número son "distintas" en un HashSet o HashMap
```

## 1.3 SOLID en contexto bancario

**S — Single Responsibility**
```java
// Cada clase tiene una sola razón para cambiar
class TransferenciaValidator  { void validar() { ... } }
class TransferenciaExecutor   { void ejecutar() { ... } }
class NotificacionService     { void notificar() { ... } }
class AuditoriaService        { void registrar() { ... } }
```

**O — Open/Closed**
```java
interface CalculadorComision { BigDecimal calcular(BigDecimal monto); }
class ComisionPersonaNatural implements CalculadorComision { ... }
class ComisionEmpresa        implements CalculadorComision { ... }
// Nuevo tipo = nueva clase. Sin modificar las existentes.
```

**L — Liskov Substitution**
```
CuentaCorriente y CuentaAhorros deben comportarse como Cuenta
en cualquier contexto donde se use Cuenta.
```

**I — Interface Segregation**
```java
// Interfaces especializadas en lugar de una interfaz gorda
interface Transferible { void transferir(); }
interface Invertible   { void invertir(); }
interface Crediticio   { void solicitarCredito(); }
```

**D — Dependency Inversion**
```java
// Depende de la abstracción, no de la implementación concreta
class TransferenciaService {
    private final CuentaRepository repo; // interfaz del dominio

    public TransferenciaService(CuentaRepository repo) {
        this.repo = repo; // Spring inyecta la implementación
    }
}
```

## 1.4 Colecciones

| Estructura | Uso ideal | Get | Add | Thread-safe |
|------------|-----------|-----|-----|-------------|
| ArrayList | Lista ordenada, acceso por índice | O(1) | O(1) amort. | No |
| HashMap | Clave-valor, sin orden | O(1) prom. | O(1) prom. | No |
| ConcurrentHashMap | Clave-valor concurrente | O(1) | O(1) | Sí |
| TreeMap | Clave-valor ordenado | O(log n) | O(log n) | No |
| HashSet | Unicidad, sin orden | O(1) | O(1) | No |
| PriorityQueue | Cola con prioridad | O(log n) | O(log n) | No |

## 1.5 Java Moderno

```java
// Records: clases de datos inmutables (perfectas para DTOs)
public record TransferRequest(String origen, String destino, BigDecimal monto) {}

// Streams
List<Transaccion> grandes = transacciones.stream()
    .filter(t -> t.getMonto().compareTo(new BigDecimal("10000000")) > 0)
    .sorted(Comparator.comparing(Transaccion::getMonto).reversed())
    .collect(Collectors.toList());

// Optional: eliminar NullPointerException
public Optional<String> getNombreCliente(String id) {
    return clienteRepository.findById(id).map(Cliente::getNombre);
}

// Pattern Matching (Java 16+)
if (obj instanceof Transferencia t) {
    System.out.println(t.getMonto()); // t ya está casteado
}
```

## 1.6 Concurrencia esencial

```java
// CompletableFuture: validar origen y destino en paralelo
CompletableFuture<Cuenta> origen = CompletableFuture
    .supplyAsync(() -> cuentaService.findById(req.getOrigen()));
CompletableFuture<Cuenta> destino = CompletableFuture
    .supplyAsync(() -> cuentaService.findById(req.getDestino()));

CompletableFuture.allOf(origen, destino)
    .thenRun(() -> ejecutarTransferencia(origen.join(), destino.join(), req.getMonto()))
    .exceptionally(ex -> { log.error("Error", ex); return null; });

// ReentrantLock para operaciones críticas
private final ReentrantLock lock = new ReentrantLock();
public void debitar(BigDecimal monto) {
    lock.lock();
    try {
        if (saldo.compareTo(monto) < 0) throw new SaldoInsuficienteException();
        saldo = saldo.subtract(monto);
    } finally {
        lock.unlock(); // SIEMPRE en finally
    }
}
```

---

# MÓDULO 2A — Spring Boot MVC y Fundamentos

## 2A.1 IoC y Beans

```java
@Service
public class TransferenciaService {
    private final CuentaRepository repo;       // interfaz
    private final AuditoriaService auditoria;  // interfaz

    // Inyección por constructor — la mejor práctica
    public TransferenciaService(CuentaRepository repo, AuditoriaService auditoria) {
        this.repo = repo;
        this.auditoria = auditoria;
    }
}
```

Scopes: `singleton` (default, sin estado), `prototype` (nueva instancia por solicitud), `request` (por HTTP request en contexto web).

## 2A.2 Controller bien estructurado

```java
@RestController
@RequestMapping("/api/v1/transferencias")
@Validated
public class TransferenciaController {

    private final TransferenciaUseCase useCase;

    public TransferenciaController(TransferenciaUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<TransferenciaResponse> crear(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody @Valid TransferenciaRequest request) {
        TransferenciaResponse response = useCase.ejecutar(idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferenciaResponse> obtener(@PathVariable String id) {
        return useCase.buscarPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

## 2A.3 Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleSaldoInsuficiente(SaldoInsuficienteException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ErrorResponse("SALDO_INSUFICIENTE", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("VALIDACION_FALLIDA", msg));
    }
}
```

## 2A.4 Spring Data JPA — Problemas críticos

**LazyInitializationException:**
```java
// Solución: JOIN FETCH en la query
@Query("SELECT c FROM Cliente c JOIN FETCH c.cuentas WHERE c.id = :id")
Optional<Cliente> findByIdWithCuentas(@Param("id") String id);
```

**Problema N+1:**
```java
// 1 query para clientes + N para cuentas de cada uno (MAL)
// Solucion: JOIN FETCH o @EntityGraph
@EntityGraph(attributePaths = {"cuentas"})
List<Cliente> findAll();
```

**Transacciones:**
```java
@Transactional(rollbackFor = Exception.class)
public void transferir(String origen, String destino, BigDecimal monto) {
    Cuenta cuentaOrigen = cuentaRepo.findByIdForUpdate(origen) // SELECT FOR UPDATE
        .orElseThrow(() -> new CuentaNoEncontradaException(origen));
    Cuenta cuentaDestino = cuentaRepo.findById(destino)
        .orElseThrow(() -> new CuentaNoEncontradaException(destino));

    cuentaOrigen.debitar(monto);
    cuentaDestino.acreditar(monto);
    registroRepo.save(new RegistroTransferencia(origen, destino, monto));
    // Rollback automatico si cualquier linea lanza excepcion
}
```

---

# MÓDULO 2B — Spring WebFlux / Reactive ⭐ REQUISITO EXPLÍCITO

## Por qué existe WebFlux

**Spring MVC bloqueante:** 1 hilo por request. Con 200 threads = máximo 200 requests simultáneos.

**Spring WebFlux no-bloqueante:** Event loop. Un hilo maneja miles de requests porque no bloquea esperando I/O. Cuándo usar: alta concurrencia, muchas llamadas I/O. Cuándo NO usar: CPU-intensivo, equipos sin experiencia reactiva.

## Tipos fundamentales

```java
// Mono: 0 o 1 elemento (como Optional pero asincrono)
Mono<Cuenta> cuenta = cuentaRepository.findById("001");

// Flux: 0 a N elementos (como Stream pero asincrono)
Flux<Transaccion> transacciones = transaccionRepository.findByCuentaId("001");
```

## Operadores esenciales

```java
// map: transformacion sincrona T -> R
Mono<BigDecimal> saldo = cuentaRepository.findById("001").map(Cuenta::getSaldo);

// flatMap: transformacion asincrona T -> Mono<R>
Mono<TransferenciaResponse> resultado = cuentaRepository.findById(req.getOrigen())
    .flatMap(cuenta -> procesarTransferencia(cuenta, req));

// filter: descartar elementos
Flux<Transaccion> grandes = transacciones
    .filter(t -> t.getMonto().compareTo(new BigDecimal("1000000")) > 0);

// switchIfEmpty: actuar cuando no hay valor
Mono<Cuenta> cuenta2 = cuentaRepository.findById("001")
    .switchIfEmpty(Mono.error(new CuentaNoEncontradaException("001")));

// zip: combinar dos Mono en paralelo
Mono<Tuple2<Cuenta, Cuenta>> cuentas = Mono.zip(
    cuentaRepository.findById(req.getOrigen()),
    cuentaRepository.findById(req.getDestino())
);

// doOnNext: side effects sin modificar el flujo (logging, metricas)
Mono<Transferencia> t = procesarTransferencia(req)
    .doOnNext(tx -> log.info("Completada: {}", tx.getId()))
    .doOnError(ex -> log.error("Error", ex));

// onErrorResume: recuperacion con alternativa
Mono<Transferencia> conFallback = procesarConProveedor(req)
    .onErrorResume(ProveedorException.class, ex ->
        colaReintentos.encolar(req).thenReturn(Transferencia.pendiente(req.getId())));

// retry con backoff
Mono<Response> conReintento = llamadaExterna(req)
    .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
        .filter(ex -> ex instanceof TimeoutException));

// timeout
Mono<Response> conTimeout = llamadaExterna(req)
    .timeout(Duration.ofSeconds(5))
    .onErrorResume(TimeoutException.class,
        ex -> Mono.error(new ProveedorNoDisponibleException()));
```

## WebFlux Controller

```java
@RestController
@RequestMapping("/api/v1/transferencias")
public class TransferenciaReactiveController {

    private final TransferenciaReactiveUseCase useCase;

    @PostMapping
    public Mono<ResponseEntity<TransferenciaResponse>> crear(
            @RequestHeader("Idempotency-Key") String key,
            @RequestBody @Valid Mono<TransferenciaRequest> requestMono) {
        return requestMono
            .flatMap(request -> useCase.ejecutar(key, request))
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
            .onErrorResume(SaldoInsuficienteException.class, ex ->
                Mono.just(ResponseEntity.unprocessableEntity()
                    .body(new TransferenciaResponse(null, "SALDO_INSUFICIENTE"))));
    }

    @GetMapping("/{clienteId}/historial")
    public Flux<TransferenciaResponse> historial(
            @PathVariable String clienteId,
            @RequestParam(defaultValue = "50") int limit) {
        return useCase.obtenerHistorial(clienteId).take(limit);
    }
}
```

## R2DBC — Base de datos reactiva

```java
// R2DBC reemplaza JPA en entornos completamente reactivos
// JPA es bloqueante por naturaleza (JDBC bajo el capo)

@Repository
public interface TransferenciaR2dbcRepository extends R2dbcRepository<Transferencia, String> {

    Flux<Transferencia> findByCuentaOrigenIdOrderByFechaDesc(String cuentaOrigenId);

    @Query("SELECT * FROM transferencias WHERE estado = :estado AND fecha >= :desde")
    Flux<Transferencia> findPendientesDesde(@Param("estado") String estado,
                                             @Param("desde") LocalDateTime desde);
}
```

## Testing con StepVerifier

```java
@Test
void deberiaRetornarTransferenciaCreada() {
    when(useCase.ejecutar(anyString(), any())).thenReturn(
        Mono.just(new TransferenciaResponse("txn-001", "PROCESANDO")));

    StepVerifier.create(controller.crear("key-123", Mono.just(request)))
        .assertNext(response -> {
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody().getId()).isEqualTo("txn-001");
        })
        .verifyComplete();
}

// Para Flux:
StepVerifier.create(controller.historial("cliente-001", 10))
    .expectNextCount(5)
    .verifyComplete();

// Para error:
StepVerifier.create(useCase.ejecutar("key", requestConSaldoInsuficiente))
    .expectError(SaldoInsuficienteException.class)
    .verify();
```

## Preguntas de entrevista sobre WebFlux

**"Diferencia entre map y flatMap"**
> "map transforma de forma sincrona: toma T y devuelve R. flatMap transforma con una operacion que a su vez devuelve un Mono o Flux: toma T y devuelve Mono<R>, suscribiendose al Publisher resultante. Usas flatMap cuando la transformacion implica otra operacion asincrona como consultar la BD o llamar un servicio externo."

**"Que es backpressure y por que importa en pagos"**
> "Es el mecanismo por el cual el consumidor controla la velocidad del productor. En pagos: si el servicio de notificaciones procesa eventos mas lento de lo que el servicio de transferencias los genera, sin backpressure se llenarian las colas y colapsaria el sistema. Con backpressure, el consumidor dice dame solo N eventos y el sistema se autorregula."

**"Cuando NO usarias WebFlux"**
> "Cuando el equipo no tiene experiencia reactiva: la depuracion de stacktraces reactivos es mas compleja. Cuando el cuello de botella no es I/O sino CPU (calculos intensivos). Cuando las librerias requeridas son solo bloqueantes y no tienen driver reactivo."

---

# MÓDULO 3 — Arquitectura de Software

## 3.1 Clean Architecture en Bancolombia

```
Regla fundamental: las dependencias solo apuntan hacia adentro.
Domain no importa nada externo.
Application conoce solo Domain.
Infrastructure conoce Application y Domain.

transferencias-service/
├── domain/
│   ├── model/
│   │   ├── Transferencia.java        (Entidad con reglas de negocio)
│   │   └── EstadoTransferencia.java
│   ├── exceptions/
│   │   └── SaldoInsuficienteException.java
│   └── gateways/
│       └── TransferenciaRepository.java  (interfaz que el dominio espera)
├── usecase/
│   └── TransferenciaUseCase.java     (orquesta el flujo de negocio)
├── infrastructure/
│   ├── entrypoints/
│   │   └── api/
│   │       ├── TransferenciaController.java
│   │       └── dto/
│   │           ├── TransferenciaRequest.java
│   │           └── TransferenciaResponse.java
│   └── drivenadapters/
│       └── r2dbc/
│           ├── TransferenciaR2dbcRepository.java
│           └── TransferenciaRepositoryAdapter.java
└── config/
    └── UseCaseConfig.java
```

## 3.2 Hexagonal Architecture

```
  Driving Adapters               Driven Adapters
  REST Controller     →Core←    R2DBC Repository
  WebFlux Router      →    ←    External Payment API
  Scheduled Job       →    ←    Kafka Producer

  Driving adapters llaman al core a traves de Input Ports (interfaces)
  El core llama driven adapters a traves de Output Ports (interfaces)
```

## 3.3 Patrones de diseño esenciales

**Builder:** Objetos con muchos campos opcionales (Transferencia.builder().id(...).build())

**Strategy:** Cambiar algoritmo en runtime (ComisionPersonaNatural vs ComisionEmpresa)

**Observer con Spring Events:**
```java
publisher.publishEvent(new TransferenciaCompletadaEvent(t));

@EventListener
public void onCompletada(TransferenciaCompletadaEvent event) {
    notificacionService.notificar(event.getClienteId(), event.getMonto());
}
```

**CQRS:** Separar comandos (escritura) de queries (lectura) cuando tienen cargas diferentes.

---

# MÓDULO 4 — Bases de Datos SQL

## 4.1 JOINs fundamentales

```sql
-- INNER JOIN: solo filas con coincidencia en ambas tablas
SELECT c.numero, t.monto, t.fecha
FROM cuentas c INNER JOIN transacciones t ON c.id = t.cuenta_id;

-- LEFT JOIN: todas las cuentas, aunque no tengan transacciones
SELECT c.numero, COUNT(t.id) AS total
FROM cuentas c LEFT JOIN transacciones t ON c.id = t.cuenta_id
GROUP BY c.numero;

-- Self JOIN: transferencias entre cuentas del mismo cliente
SELECT c1.numero AS origen, c2.numero AS destino, t.monto
FROM transferencias t
JOIN cuentas c1 ON t.cuenta_origen_id = c1.id
JOIN cuentas c2 ON t.cuenta_destino_id = c2.id
WHERE c1.cliente_id = c2.cliente_id;
```

## 4.2 CTE (Common Table Expressions)

```sql
WITH volumen_mensual AS (
    SELECT c.cliente_id, SUM(t.monto) AS total, COUNT(t.id) AS num
    FROM transferencias t
    JOIN cuentas c ON t.cuenta_origen_id = c.id
    WHERE t.fecha >= DATE_TRUNC('month', CURRENT_DATE)
    GROUP BY c.cliente_id
)
SELECT cl.nombre, vm.total, vm.num
FROM clientes cl JOIN volumen_mensual vm ON cl.id = vm.cliente_id
ORDER BY vm.total DESC LIMIT 10;
```

## 4.3 Indices estrategicos

```sql
-- Simple
CREATE INDEX idx_transferencias_fecha ON transferencias(fecha);

-- Compuesto (filtro mas selectivo primero)
CREATE INDEX idx_txn_cuenta_fecha ON transferencias(cuenta_origen_id, fecha DESC);

-- Parcial (solo filas relevantes)
CREATE INDEX idx_txn_pendientes ON transferencias(cuenta_origen_id)
WHERE estado = 'PENDIENTE';

-- Ver plan de ejecucion
EXPLAIN ANALYZE SELECT * FROM transferencias
WHERE cuenta_origen_id = '001' AND fecha > '2024-01-01';
-- Buscar: Index Scan (bien) vs Seq Scan en tabla grande (problema)
```

## 4.4 ACID en pagos

| Propiedad | En transferencia |
|-----------|-----------------|
| Atomicidad | Debito y acreditamiento son una sola unidad |
| Consistencia | Saldo nunca queda negativo |
| Aislamiento | Dos transferencias simultaneas de la misma cuenta no se interfieren |
| Durabilidad | Confirmada = persiste aunque el servidor caiga |

## 4.5 Locking en transferencias

```java
// Optimistic: version en la entidad, falla si alguien actualizo antes
@Entity
public class Cuenta {
    @Version
    private Long version; // JPA lanza OptimisticLockException si version cambio
}

// Pessimistic: bloquea la fila hasta commit (para debito de saldo)
@Query("SELECT c FROM Cuenta c WHERE c.id = :id")
@Lock(LockModeType.PESSIMISTIC_WRITE) // SELECT FOR UPDATE
Optional<Cuenta> findByIdForUpdate(@Param("id") String id);
```

---

# MÓDULO 5 — APIs REST

## Metodos HTTP e Idempotencia

| Metodo | Idempotente | Safe | Uso |
|--------|-------------|------|-----|
| GET | Si | Si | Leer recurso |
| POST | No | No | Crear recurso |
| PUT | Si | No | Reemplazar completo |
| PATCH | No* | No | Actualizar parcial |
| DELETE | Si | No | Eliminar |

## Idempotencia en pagos

```java
@PostMapping("/transferencias")
public Mono<ResponseEntity<TransferenciaResponse>> crear(
        @RequestHeader("Idempotency-Key") String key,
        @Valid @RequestBody Mono<TransferenciaRequest> requestMono) {
    return requestMono
        .flatMap(req -> idempotencyService.executeOnce(key, () -> useCase.ejecutar(req)))
        .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r));
}
// Si el mismo key llega de nuevo, se devuelve la respuesta guardada sin reprocesar
```

## Versionado y Paginacion

```java
// Breaking change = nueva version
@RequestMapping("/api/v1/transferencias")
@RequestMapping("/api/v2/transferencias")

// Paginacion
GET /api/v1/transacciones?page=0&size=20&sort=fecha,desc
```

---

# MÓDULO 6A — Angular: Fundamentos ⭐ NUEVO REQUISITO

## Arquitectura de un proyecto Angular

```
src/app/
├── core/            ← Servicios singleton, guards, interceptors
│   ├── services/
│   └── interceptors/
├── shared/          ← Componentes, pipes reutilizables
├── features/        ← Modulos de funcionalidad (lazy loading)
│   └── transferencias/
│       ├── transferencias.module.ts
│       ├── transferencias.component.ts
│       ├── transferencias.component.html
│       └── services/
└── app.module.ts
```

## TypeScript esencial

```typescript
// Interfaces: contrato de forma de los datos
interface Transferencia {
  id: string;
  cuentaOrigen: string;
  cuentaDestino: string;
  monto: number;
  estado: 'PENDIENTE' | 'COMPLETADA' | 'FALLIDA'; // Literal types
  fecha: Date;
}

// Tipos genericos
interface ApiResponse<T> { data: T; total?: number; error?: string; }

// Optional chaining y nullish coalescing
const nombre = cliente?.nombre ?? 'Sin nombre';
```

## Componentes

```typescript
@Component({
  selector: 'app-transferencias',
  templateUrl: './transferencias.component.html',
})
export class TransferenciasComponent implements OnInit, OnDestroy {

  transferencias: Transferencia[] = [];
  cargando = false;
  error: string | null = null;

  private readonly destroy$ = new Subject<void>();

  constructor(private readonly transferenciaService: TransferenciaService) {}

  ngOnInit(): void {
    this.cargando = true;
    this.transferenciaService.getTransferencias()
      .pipe(takeUntil(this.destroy$)) // Auto-unsuscribe cuando el componente muere
      .subscribe({
        next: (data) => { this.transferencias = data; this.cargando = false; },
        error: (err) => { this.error = 'Error al cargar'; this.cargando = false; }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete(); // Limpiar suscripciones
  }
}
```

```html
<!-- Template -->
<div *ngIf="cargando">Cargando...</div>
<div *ngIf="error" class="error">{{ error }}</div>

<table *ngIf="!cargando && !error">
  <tbody>
    <tr *ngFor="let t of transferencias; trackBy: trackById">
      <td>{{ t.id }}</td>
      <td>{{ t.monto | currency:'COP':'symbol':'1.0-0' }}</td>
      <td [class]="'estado-' + t.estado.toLowerCase()">{{ t.estado }}</td>
    </tr>
  </tbody>
</table>
```

## Services — HTTP Client

```typescript
@Injectable({ providedIn: 'root' })
export class TransferenciaService {

  private readonly apiUrl = environment.apiUrl + '/api/v1/transferencias';

  constructor(private readonly http: HttpClient) {}

  getTransferencias(page = 0, size = 20): Observable<Transferencia[]> {
    return this.http.get<ApiResponse<Transferencia[]>>(this.apiUrl, {
      params: { page, size }
    }).pipe(
      map(response => response.data),
      catchError(this.handleError)
    );
  }

  crearTransferencia(request: TransferenciaRequest): Observable<Transferencia> {
    return this.http.post<Transferencia>(this.apiUrl, request, {
      headers: { 'Idempotency-Key': crypto.randomUUID() }
    }).pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let mensaje = 'Error desconocido';
    if (error.status === 422) mensaje = 'Saldo insuficiente';
    else if (error.status === 404) mensaje = 'Cuenta no encontrada';
    else if (error.status === 0) mensaje = 'Sin conexion al servidor';
    return throwError(() => new Error(mensaje));
  }
}
```

## HTTP Interceptor — JWT automatico

```typescript
@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    if (token) {
      req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
    }
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) this.authService.logout();
        return throwError(() => error);
      })
    );
  }
}
```

## Routing con Lazy Loading

```typescript
const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  {
    path: 'transferencias',
    loadChildren: () => import('./features/transferencias/transferencias.module')
      .then(m => m.TransferenciasModule),
    canActivate: [AuthGuard]
  },
  { path: '**', redirectTo: '/dashboard' }
];
```

## Reactive Forms

```typescript
form = this.fb.group({
  cuentaOrigen:  ['', [Validators.required, Validators.pattern(/^\d{3}-\d{6}$/)]],
  cuentaDestino: ['', [Validators.required, Validators.pattern(/^\d{3}-\d{6}$/)]],
  monto: [null, [Validators.required, Validators.min(1000), Validators.max(50_000_000)]],
});

get cuentaOrigen() { return this.form.get('cuentaOrigen')!; }
get monto()        { return this.form.get('monto')!; }

onSubmit(): void {
  if (this.form.invalid) return;
  this.service.crearTransferencia(this.form.value as TransferenciaRequest)
    .subscribe({ next: () => {/* navegar */}, error: (err) => {/* mostrar */} });
}
```

```html
<form [formGroup]="form" (ngSubmit)="onSubmit()">
  <input formControlName="cuentaOrigen">
  <div *ngIf="cuentaOrigen.invalid && cuentaOrigen.touched">
    <span *ngIf="cuentaOrigen.errors?.['required']">Requerido</span>
    <span *ngIf="cuentaOrigen.errors?.['pattern']">Formato invalido (ej: 001-123456)</span>
  </div>

  <input formControlName="monto" type="number">
  <div *ngIf="monto.invalid && monto.touched">
    <span *ngIf="monto.errors?.['min']">Monto minimo: $1.000</span>
  </div>

  <button type="submit" [disabled]="form.invalid">Transferir</button>
</form>
```

---

# MÓDULO 6B — Angular Avanzado + RxJS

## RxJS — El puente entre backend reactivo y frontend Angular

RxJS es la libreria de programacion reactiva de Angular. Observable = Flux/Mono del frontend.

```typescript
// BehaviorSubject: guarda el ultimo valor, util para estado
private readonly saldoSubject = new BehaviorSubject<number>(0);
readonly saldo$ = this.saldoSubject.asObservable();
this.saldoSubject.next(1500000); // Actualizar

// En el template con async pipe (suscribe y desuscribe automaticamente)
// {{ saldo$ | async | currency:'COP' }}
```

## Operadores RxJS esenciales

```typescript
// switchMap: cancelar request anterior. Ideal para busqueda en tiempo real.
this.searchInput.valueChanges.pipe(
  debounceTime(300),       // Esperar 300ms sin cambios
  distinctUntilChanged(),  // Solo si el valor cambio
  switchMap(query => this.service.buscar(query)) // Cancela el anterior
).subscribe(resultados => this.resultados = resultados);

// combineLatest: combinar multiples Observables cuando cualquiera emite
combineLatest([
  this.cuentaService.getOrigen(),
  this.cuentaService.getDestino()
]).pipe(map(([origen, destino]) => ({ origen, destino })))
  .subscribe(cuentas => this.cuentas = cuentas);

// forkJoin: esperar que TODOS completen (como Promise.all)
forkJoin({
  saldo: this.cuentaService.getSaldo('001'),
  historial: this.transferenciaService.getHistorial('001')
}).subscribe(({ saldo, historial }) => { ... });

// tap: side effects sin modificar el valor
this.service.getTransferencias().pipe(
  tap(t => console.log('Cargadas:', t.length)),
  map(t => t.filter(x => x.estado === 'COMPLETADA'))
).subscribe(completadas => this.completadas = completadas);
```

## Async Pipe — La forma correcta

```typescript
// MAL: suscripcion en ngOnInit — memory leak si no se desuscribe
ngOnInit() {
  this.service.getTransferencias().subscribe(t => this.transferencias = t);
}

// BIEN: async pipe maneja suscripcion y limpieza automaticamente
transferencias$ = this.service.getTransferencias();
```

```html
<div *ngIf="transferencias$ | async as transferencias; else loading">
  <div *ngFor="let t of transferencias">{{ t.id }}</div>
</div>
<ng-template #loading>Cargando...</ng-template>
```

---

# MÓDULO 7A — Testing: JUnit 5 + Mockito

```java
@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    @Mock private CuentaRepository cuentaRepository;
    @Mock private AuditoriaService auditoriaService;
    @InjectMocks private TransferenciaService service;

    @Test
    @DisplayName("Debe transferir exitosamente cuando hay saldo suficiente")
    void deberiaTransferirExitosamente() {
        // Arrange
        Cuenta origen  = new Cuenta("001", new BigDecimal("1000000"));
        Cuenta destino = new Cuenta("002", new BigDecimal("500000"));
        when(cuentaRepository.findByIdForUpdate("001")).thenReturn(Optional.of(origen));
        when(cuentaRepository.findById("002")).thenReturn(Optional.of(destino));

        // Act
        service.transferir("001", "002", new BigDecimal("200000"));

        // Assert
        assertThat(origen.getSaldo()).isEqualByComparingTo(new BigDecimal("800000"));
        assertThat(destino.getSaldo()).isEqualByComparingTo(new BigDecimal("700000"));
        verify(auditoriaService, times(1)).registrar(any(Transferencia.class));
    }

    @Test
    @DisplayName("Debe lanzar excepcion cuando saldo insuficiente")
    void deberiaLanzarExcepcionCuandoSaldoInsuficiente() {
        when(cuentaRepository.findByIdForUpdate("001"))
            .thenReturn(Optional.of(new Cuenta("001", new BigDecimal("100"))));
        when(cuentaRepository.findById("002"))
            .thenReturn(Optional.of(new Cuenta("002", BigDecimal.ZERO)));

        assertThrows(SaldoInsuficienteException.class, () ->
            service.transferir("001", "002", new BigDecimal("500")));
        verify(auditoriaService, never()).registrar(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    @DisplayName("Debe fallar con cuenta de origen invalida")
    void deberiaFallarConCuentaInvalida(String cuentaOrigen) {
        assertThrows(IllegalArgumentException.class, () ->
            service.transferir(cuentaOrigen, "002", new BigDecimal("1000")));
    }
}
```

## Integration Tests con Testcontainers

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class TransferenciaControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("test_db");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
            "r2dbc:postgresql://localhost:" + postgres.getMappedPort(5432) + "/test_db");
    }

    @Autowired private WebTestClient webClient;

    @Test
    void deberiaCrearTransferenciaYRetornar201() {
        webClient.post().uri("/api/v1/transferencias")
            .header("Idempotency-Key", UUID.randomUUID().toString())
            .bodyValue(new TransferenciaRequest("001", "002", new BigDecimal("10000")))
            .exchange()
            .expectStatus().isCreated()
            .expectBody(TransferenciaResponse.class)
            .value(r -> assertThat(r.getId()).isNotNull());
    }
}
```

---

# MÓDULO 7B — Testing: Jest para Angular ⭐ NUEVO

## Testing de Services

```typescript
describe('TransferenciaService', () => {
  let service: TransferenciaService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TransferenciaService]
    });
    service = TestBed.inject(TransferenciaService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify()); // Sin requests pendientes

  it('debe obtener transferencias del servidor', () => {
    const mockData = [{ id: '1', cuentaOrigen: '001', cuentaDestino: '002',
                        monto: 50000, estado: 'COMPLETADA' as const, fecha: new Date() }];

    service.getTransferencias().subscribe(data => {
      expect(data).toHaveLength(1);
      expect(data[0].id).toBe('1');
    });

    const req = httpMock.expectOne('/api/v1/transferencias?page=0&size=20');
    expect(req.request.method).toBe('GET');
    req.flush({ data: mockData });
  });

  it('debe manejar error 422 como SaldoInsuficiente', () => {
    service.crearTransferencia({ cuentaOrigen: '001', cuentaDestino: '002', monto: 999 })
      .subscribe({
        next: () => fail('Deberia haber fallado'),
        error: (err: Error) => expect(err.message).toBe('Saldo insuficiente')
      });

    const req = httpMock.expectOne('/api/v1/transferencias');
    req.flush({}, { status: 422, statusText: 'Unprocessable Entity' });
  });
});
```

## Testing de Componentes

```typescript
describe('TransferenciasComponent', () => {
  let component: TransferenciasComponent;
  let fixture: ComponentFixture<TransferenciasComponent>;
  let mockService: jest.Mocked<TransferenciaService>;

  beforeEach(async () => {
    mockService = { getTransferencias: jest.fn(), crearTransferencia: jest.fn() } as any;

    await TestBed.configureTestingModule({
      declarations: [TransferenciasComponent],
      providers: [{ provide: TransferenciaService, useValue: mockService }]
    }).compileComponents();

    fixture = TestBed.createComponent(TransferenciasComponent);
    component = fixture.componentInstance;
  });

  it('debe mostrar transferencias al inicializar', () => {
    const mockTransferencias = [{
      id: 'txn-1', cuentaOrigen: '001', cuentaDestino: '002',
      monto: 50000, estado: 'COMPLETADA' as const, fecha: new Date()
    }];
    mockService.getTransferencias.mockReturnValue(of(mockTransferencias));
    fixture.detectChanges(); // Trigger ngOnInit

    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(1);
  });

  it('debe mostrar error cuando el servicio falla', () => {
    mockService.getTransferencias.mockReturnValue(
      throwError(() => new Error('Error de red')));
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.error')).toBeTruthy();
  });

  it('debe limpiar suscripciones al destruir', () => {
    const spy = jest.spyOn(component['destroy$'], 'next');
    component.ngOnDestroy();
    expect(spy).toHaveBeenCalled();
  });
});
```

---

# MÓDULO 7C — Testing: Playwright ⭐ NUEVO

```typescript
// e2e/transferencias.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Transferencias', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('[data-testid="email"]', 'usuario@bancolombia.com');
    await page.fill('[data-testid="password"]', 'password123');
    await page.click('[data-testid="login-btn"]');
    await expect(page).toHaveURL('/dashboard');
  });

  test('debe crear una transferencia exitosamente', async ({ page }) => {
    await page.goto('/transferencias/nueva');
    await page.fill('[data-testid="cuenta-origen"]', '001-123456');
    await page.fill('[data-testid="cuenta-destino"]', '001-789012');
    await page.fill('[data-testid="monto"]', '500000');
    await page.click('[data-testid="btn-transferir"]');

    await expect(page.locator('[data-testid="success-message"]'))
      .toBeVisible({ timeout: 5000 });
  });

  test('debe mostrar error de saldo insuficiente', async ({ page }) => {
    await page.goto('/transferencias/nueva');
    await page.fill('[data-testid="monto"]', '999999999');
    await page.click('[data-testid="btn-transferir"]');
    await expect(page.locator('[data-testid="error-message"]'))
      .toContainText('Saldo insuficiente');
  });

  test('debe validar formato de cuenta antes de enviar', async ({ page }) => {
    await page.goto('/transferencias/nueva');
    await page.fill('[data-testid="cuenta-origen"]', 'invalido');
    await page.click('[data-testid="monto"]'); // Blur
    await expect(page.locator('[data-testid="error-cuenta-origen"]'))
      .toContainText('Formato invalido');
    await expect(page.locator('[data-testid="btn-transferir"]')).toBeDisabled();
  });
});
```

## Playwright vs Karate — Cuando usar cada uno

| | Karate | Playwright |
|--|--------|-----------|
| Que prueba | APIs REST directamente | UI completa en browser real |
| Nivel | Backend / Integration | E2E / Acceptance |
| Ideal para | Validar contratos de API | Validar flujos de usuario completos |

---

# MÓDULO 7D — Karate + JMeter

## Karate — Contract Testing

```gherkin
Feature: API de Transferencias

  Background:
    * url 'http://localhost:8080'
    * header Content-Type = 'application/json'

  Scenario: Crear transferencia exitosa
    Given path '/api/v1/transferencias'
    And header Idempotency-Key = java.util.UUID.randomUUID().toString()
    And request { "cuentaOrigen": "001-123", "cuentaDestino": "001-456", "monto": 50000 }
    When method POST
    Then status 201
    And match response.id == '#notnull'
    And match response.estado == 'PROCESANDO'

  Scenario: Idempotencia — mismo key retorna misma respuesta
    * def key = java.util.UUID.randomUUID().toString()
    * header Idempotency-Key = key
    Given path '/api/v1/transferencias'
    And request { "cuentaOrigen": "001-123", "cuentaDestino": "001-456", "monto": 50000 }
    When method POST
    Then status 201
    * def primeraRespuesta = response

    Given path '/api/v1/transferencias'
    And request { "cuentaOrigen": "001-123", "cuentaDestino": "001-456", "monto": 50000 }
    When method POST
    Then status 201
    And match response.id == primeraRespuesta.id
```

## JMeter — Metricas clave para pagos

| Metrica | Que mide | Umbral tipico |
|---------|----------|---------------|
| Throughput | Requests por segundo | Objetivo: 1000+ TPS |
| Response Time p50 | Mediana | < 300ms |
| Response Time p95 | El 95% responde en X | < 1s |
| Response Time p99 | El 99% responde en X | < 3s |
| Error Rate | % con error | < 0.1% |

---

# MÓDULO 8 — Documentación Técnica ⭐ NUEVO

## 8.1 Diagrama Entidad-Relacion

```
CLIENTE (1) ------- (N) CUENTA (1) ------- (N) TRANSFERENCIA
  id UUID               id UUID                  id UUID
  nombre                cliente_id (FK)           cuenta_origen_id (FK)
  documento             numero                    cuenta_destino_id (FK)
  tipo_documento        tipo (CORRIENTE/AHORRO)   monto DECIMAL(15,2)
  email                 saldo DECIMAL(15,2)       estado ENUM
                        estado                    idempotency_key
                        created_at                created_at
```

## 8.2 Diagrama API — OpenAPI

```yaml
openapi: 3.0.3
info:
  title: Transferencias API
  version: 1.0.0

paths:
  /api/v1/transferencias:
    post:
      summary: Crear transferencia
      parameters:
        - name: Idempotency-Key
          in: header
          required: true
          schema: { type: string, format: uuid }
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [cuentaOrigen, cuentaDestino, monto]
              properties:
                cuentaOrigen:  { type: string, example: "001-123456" }
                cuentaDestino: { type: string, example: "001-789012" }
                monto:         { type: number, minimum: 1000 }
      responses:
        '201': { description: Transferencia creada }
        '422': { description: Saldo insuficiente }
        '400': { description: Datos invalidos }
```

## 8.3 Como documentar un bug en Jira / Azure DevOps

```
TITULO: [Transferencias][Validacion] El monto negativo no es rechazado

DESCRIPCION:
Ambiente: QA | Version: 2.1.3

PASOS PARA REPRODUCIR:
1. POST /api/v1/transferencias con monto = -5000
2. Observar respuesta

RESULTADO ESPERADO: HTTP 400 — "El monto debe ser positivo"
RESULTADO ACTUAL:   HTTP 201 — transferencia creada con monto -5000

EVIDENCIA: [logs del request/response]

SEVERIDAD: Alta
CAUSA RAIZ: Falta @Positive en el DTO TransferenciaRequest
FIX: Agregar @Positive @NotNull BigDecimal monto al request
```

---

# MÓDULO 9 — Pagos Digitales TI ⭐ El diferenciador

## 9.1 Flujo completo de una transferencia

```
1. Cliente inicia en app Angular
   ↓
2. API Gateway (autenticacion + rate limiting)
   ↓
3. Servicio de Transferencias (Spring WebFlux)
   - Validar identidad
   - Validar cuentas (origen y destino existen)
   - Verificar saldo (SELECT FOR UPDATE)
   - Reglas anti-fraude (velocity check, horario, monto)
   - Calcular comision
   - Crear registro PENDIENTE + OutboxEvent (misma transaccion)
   ↓
4. Core bancario / Liquidacion
   - Debitar cuenta origen
   - Acreditar cuenta destino
   ↓
5. Actualizar estado: COMPLETADA
   ↓
6. Notificaciones async (push, SMS, email)
   ↓
7. Registro en auditoria
```

## 9.2 Idempotencia

**El problema:** Usuario presiona "Transferir" dos veces. ¿Se debita dos veces?

**La solucion con Idempotency-Key:**
- La app Angular genera un UUID por intento y lo envia en el header.
- El backend busca en Redis si ese UUID ya tiene respuesta.
- Si si → devuelve la misma respuesta sin reprocesar.
- Si no → procesa y guarda con TTL 24h.

## 9.3 Outbox Pattern — Garantia ante fallos

```
Problema: se llama al core bancario, no llega respuesta, hay timeout.
¿Se ejecuto o no? (Problema del Two Generals)

Solucion:
1. Crear transferencia PENDIENTE en BD
2. Escribir OutboxEvent en la misma transaccion
3. Un worker lee el Outbox y llama al core con el ID unico
4. Si falla, reintenta (el ID garantiza idempotencia en el core)
```

## 9.4 Conceptos del dominio

| Concepto | Descripcion |
|----------|-------------|
| Adquirencia | Procesar pagos a comercios. El banco adquirente recibe el pago. |
| Emision | Emitir medios de pago. El banco emisor autoriza el pago del cliente. |
| Compensacion | Calcular cuanto se deben entre si los bancos por las transacciones del dia. |
| Liquidacion | Transferencia real del dinero entre bancos despues de la compensacion. |
| Reverso | Deshacer una transaccion completada. No es rollback de BD; es nueva transaccion. |
| Conciliacion | Comparar lo que el sistema dice vs lo que el proveedor dice. |
| Pago huerfano | Transaccion procesada sin registro de respuesta (perdida en timeout). |
| PSE | Pasarela para pagos desde cuentas bancarias en Colombia. |

## 9.5 PCI DSS y seguridad

- Nunca almacenar CVV/CVC despues de autorizacion.
- Nunca almacenar numero de tarjeta en texto plano.
- Tokenizacion: numero real se reemplaza por token inofensivo.
- TLS 1.2+ en transito, AES-256 en reposo.
- Logs sin datos sensibles, con acceso restringido.

---

# MÓDULO 10 — Resolución de Problemas en Producción

## Framework de diagnostico

```
1. Que sintoma? (lento / error / caido / inconsistente)
2. Desde cuando? Hubo deploy reciente?
3. A quienes afecta? Un usuario, un canal, todos?
4. Que dicen los logs?
5. Que dicen las metricas? (CPU, RAM, latencia, error rate)
6. Hipotesis (2-3 candidatas)
7. Como descarto cada hipotesis rapido?
8. Solucion temporal (mitigation)?
9. Causa raiz (root cause)?
```

## Casos resueltos

### "Endpoint tarda 12 segundos"
1. **Query SQL lenta** → EXPLAIN ANALYZE → Seq Scan → indice faltante.
2. **Problema N+1** → show-sql=true → contar queries → JOIN FETCH.
3. **Servicio externo lento** → timeout + circuit breaker + respuesta cacheada.
4. **Connection pool agotado** → Hikari metrics → aumentar pool o encontrar fuga.

### "Transaccion a medias"
1. Identificar estado: se debito? se acredito?
2. Buscar en logs por el ID de la transaccion.
3. Si PENDIENTE sin respuesta → reintento controlado con mismo ID.
4. Si debito sin acreditamiento → proceso de compensacion.
5. Prevenir: Outbox Pattern + conciliacion periodica.

### "Hay duplicados en la BD"
1. Falta indice UNIQUE → CREATE UNIQUE INDEX.
2. Retry sin idempotency key → implementar idempotencia.
3. Dos instancias procesaron el mismo mensaje → ON CONFLICT DO NOTHING.

---

# MÓDULO 11 — System Design Junior

## Diseno: Sistema de Transferencias completo

```
Angular App
    |
    | HTTPS
    v
API Gateway (auth, rate limiting, SSL)
    |
    +--- Auth Service (JWT) --- Users DB
    |
    +--- Cuenta Service ------- Cuentas DB (PostgreSQL)
    |                           (con replica de lectura)
    +--- Transferencia Service  Txn DB (PostgreSQL)
         (Spring WebFlux)       |
                                | eventos
                                v
                           Mensajeria (Kafka/SQS)
                                |
                    +-----------+----------+
                    |           |          |
              Notif Svc    Audit Svc   Fraud Svc
```

**Puntos clave a mencionar:**
- Idempotency-Key para prevenir pagos duplicados.
- SELECT FOR UPDATE para evitar race conditions en saldos.
- WebFlux para alta concurrencia con pocos threads.
- Mensajeria asincrona para desacoplar notificaciones y auditoria.
- Circuit Breaker hacia servicios externos (PSE, ACH, core bancario).
- Outbox Pattern para garantia ante fallos.

---

# MÓDULO 12 — Casos Reales Bancolombia

## Caso 1: Garantia de una sola ejecucion

El cliente (Angular) presiona "Transferir" dos veces por lentitud de red:
1. Angular genera UUID como Idempotency-Key por intento.
2. Backend busca en Redis si ese key ya tiene respuesta.
3. Si si → devuelve la misma. Si no → procesa y guarda con TTL 24h.

## Caso 2: Notificaciones sin acoplar el pago

Si el servicio de notificaciones cae, el pago no debe fallar:
1. Pago se completa → evento en cola de mensajes.
2. Servicio de notificaciones consume el evento async.
3. Si falla, la cola reintenta automaticamente.
4. Dead Letter Queue para mensajes que fallan N veces.

## Caso 3: Integracion Angular con Backend WebFlux

```typescript
// El frontend Angular llama al backend reactivo
crearTransferencia(req: TransferenciaRequest): Observable<TransferenciaResponse> {
    return this.http.post<TransferenciaResponse>('/api/v1/transferencias', req, {
        headers: {
            'Idempotency-Key': crypto.randomUUID(),
            // JWT lo agrega automaticamente el interceptor
        }
    }).pipe(
        catchError(this.handleError)
    );
}
```

## Caso 4: Observabilidad

```java
// Metricas criticas para un servicio de pagos
Counter.builder("transferencias.total").register(registry);
Counter.builder("transferencias.error").register(registry);
Timer.builder("transferencias.latencia").register(registry);

// Alertas:
// Error rate > 1% → Warning. > 5% → Critical.
// Latencia p99 > 3s → Warning.
// Transferencias PENDIENTE acumuladas → fallo de procesamiento.
```

---

# MÓDULO 13 — Entrevistas Simuladas

## Preguntas muy probables

**Sobre WebFlux (requisito explícito):**
- "¿Cuál es la diferencia entre map y flatMap en Project Reactor?"
- "¿Cuándo NO usarías WebFlux?"
- "¿Qué es backpressure?"

**Sobre Angular (requisito explícito):**
- "¿Qué es RxJS y qué relación tiene con Project Reactor?"
- "¿Por qué el async pipe es mejor que suscribirse en ngOnInit?"
- "¿Qué hace switchMap? ¿Cuándo lo usarías?"
- "¿Cómo enviarías el JWT sin repetirlo en cada servicio?"

**Sobre Arquitectura:**
- "¿Cuál es la regla de dependencias en Clean Architecture?"
- "¿Qué pasa si el servicio de pagos se cae después de descontar el dinero?"
- "¿Cómo evitarías que un usuario pague dos veces?"

**Sobre Testing:**
- "¿Cuál es la diferencia entre Karate y Playwright?"
- "¿Cómo testearías un componente Angular que hace llamadas HTTP?"
- "¿Para qué sirve StepVerifier?"

**Behavioral:**
- "Cuéntame de un bug que encontraste en tu práctica y cómo lo resolviste."
- "¿Cómo manejas el feedback negativo en un PR?"
- "¿Por qué quieres quedarte en Bancolombia?"

---

# MÓDULO 14 — Banco de Preguntas (Selección)

## Java + Spring (20 preguntas)
1. ¿Qué hace volatile? ¿Cuándo lo usarías?
2. ¿Qué es un FunctionalInterface? Ejemplos del JDK.
3. ¿Cuándo usarías parallelStream()? ¿Qué riesgos tiene?
4. ¿Qué es el String Pool y por qué existe?
5. ¿Cómo funciona HashMap internamente? ¿Qué pasa con colisiones?
6. ¿Qué es un deadlock? Cómo evitarlo.
7. ¿Para qué sirven los Records? ¿Cuándo son mejor que una clase normal?
8. ¿Qué hace @Transactional en un método privado?
9. ¿Cuál es la diferencia entre @Component, @Service, @Repository?
10. ¿Qué es Spring AOP? ¿Cuándo lo usarías?
11. ¿Por qué la inyección por constructor es mejor que @Autowired en campo?
12. ¿Qué es el problema N+1 y cómo se soluciona?
13. ¿Cuándo usarías Optimistic vs Pessimistic locking?
14. ¿Qué es LazyInitializationException y cómo se previene?
15. ¿Qué es un CircularDependency en Spring? ¿Cómo se resuelve?
16. ¿Qué hace @EnableAsync? ¿Qué riesgos tiene?
17. ¿Qué diferencia hay entre CompletableFuture y Future?
18. ¿Qué es un memory leak en Java? ¿Causas frecuentes?
19. ¿Qué hace EXPLAIN ANALYZE en SQL?
20. ¿Qué es un índice parcial? ¿Cuándo conviene?

## Spring WebFlux (10 preguntas)
21. ¿Qué diferencia hay entre MVC bloqueante y WebFlux no-bloqueante?
22. ¿Qué es un Mono? ¿Y un Flux?
23. ¿Diferencia entre map y flatMap?
24. ¿Qué es backpressure y cómo se maneja?
25. ¿Qué hace switchIfEmpty?
26. ¿Qué hace onErrorResume? ¿Y onErrorReturn?
27. ¿Qué es StepVerifier?
28. ¿Por qué JPA es problemático con WebFlux? ¿Qué lo reemplaza?
29. ¿Qué hace el operador zip?
30. ¿Qué es el Event Loop en WebFlux?

## Angular + RxJS (15 preguntas)
31. ¿Qué es un Observable? ¿En qué se parece a un Flux?
32. ¿Cuál es la diferencia entre Subject y BehaviorSubject?
33. ¿Qué hace switchMap? ¿Cuándo lo usarías?
34. ¿Qué hace debounceTime? Caso de uso.
35. ¿Por qué el async pipe es mejor que suscribirse en ngOnInit?
36. ¿Qué es un HttpInterceptor? ¿Para qué lo usarías?
37. ¿Qué es takeUntil y para qué sirve?
38. ¿Cuál es la diferencia entre Reactive Forms y Template-Driven?
39. ¿Qué es lazy loading en Angular? ¿Por qué importa?
40. ¿Qué es un Guard en Angular Routing?
41. ¿Qué hace forkJoin? ¿Y combineLatest?
42. ¿Cómo testearías un Service Angular que hace HTTP?
43. ¿Qué es un snapshot test en Jest?
44. ¿Cómo pasarías datos entre componentes sin relación padre-hijo?
45. ¿Cuándo usarías @Input/@Output vs un Service compartido?

## Arquitectura y Pagos (15 preguntas)
46. ¿Cuál es la regla de dependencias en Clean Architecture?
47. ¿Qué es un Port en arquitectura hexagonal?
48. ¿Qué es idempotencia? ¿Por qué es crítica en pagos?
49. ¿Qué es el Outbox Pattern?
50. ¿Qué es PCI DSS?
51. ¿Qué es tokenización en pagos?
52. ¿Qué diferencia hay entre reverso y rollback de BD?
53. ¿Qué es conciliación bancaria?
54. ¿Qué es PSE?
55. ¿Cómo prevendrías el doble cobro?
56. ¿Qué es ACID?
57. ¿Qué es Optimistic Locking?
58. ¿Qué es un Circuit Breaker? ¿Cuáles son sus estados?
59. ¿Qué es el Saga Pattern?
60. ¿Qué diferencia hay entre adquirencia y emisión?

---

# MÓDULO 15 — Soft Skills y Behavioral

## Respuestas modelo

### "¿Por qué quieres quedarte en Bancolombia?"
> "La práctica me permitió ver que en Bancolombia el trabajo técnico tiene impacto real en millones de personas. Cada mejora en un servicio de pagos afecta la vida del cliente en el momento más importante. Además, ya conozco la cultura, el stack tecnológico y las expectativas de calidad del banco, lo que me permitiría aportar desde el primer día con mínima curva de aprendizaje."

### "¿Cómo manejas feedback negativo en un PR?"
> "Lo recibo como información técnica, no como crítica personal. Mi objetivo es que el código sea correcto, no que sea mío. Cuando no entiendo el porqué de una sugerencia, pregunto. Cuando no estoy de acuerdo, lo argumento con razonamiento técnico. Y cuando me equivocaron, corrijo y documento el aprendizaje."

### "¿Cómo te actualizas técnicamente?"
> "Combino documentación oficial (Spring, Angular, Reactor), proyectos personales donde aplico lo que leo, y el contexto bancario (PCI DSS, estándares de pagos). Recientemente implementé un servicio de transferencias completo con WebFlux, R2DBC y Angular para entender el ciclo completo que describe esta vacante."

---

# MÓDULO + — Resilience4j

```java
@CircuitBreaker(name = "proveedorPagos", fallbackMethod = "pagoFallback")
@Retry(name = "proveedorPagos")
@TimeLimiter(name = "proveedorPagos")
public Mono<TransferenciaResponse> procesarConProveedor(TransferenciaRequest req) {
    return proveedorPagosClient.procesar(req);
}

public Mono<TransferenciaResponse> pagoFallback(TransferenciaRequest req, Exception ex) {
    colaReintentos.encolar(req);
    return Mono.just(TransferenciaResponse.pendiente(req.getId()));
}

// Estados: CLOSED (normal) → OPEN (fallos > umbral, rechaza) → HALF_OPEN (prueba recuperacion)
```

---

# Cronograma de 10 Semanas

| Semana | Modulos | Enfoque |
|--------|---------|---------|
| 1 | 0, 1 | Mentalidad + Java + SOLID |
| 2 | 2A, inicio 2B | Spring MVC + inicio WebFlux |
| 3 | 2B profundo, 5 | WebFlux avanzado + APIs REST |
| 4 | 3, 4 | Clean Architecture + SQL |
| 5 | 6A, inicio 6B | Angular: componentes, services, routing |
| 6 | 6B RxJS, 7B | RxJS avanzado + Jest para Angular |
| 7 | 7A, 7C, 7D | JUnit 5 + Playwright + Karate/JMeter |
| 8 | 8, 9 | Documentacion tecnica + Dominio de Pagos |
| 9 | 10, 11, 12 | Problemas en produccion + System Design |
| 10 | 13, 15 | Entrevistas simuladas + Behavioral |

---

# Recursos

## Documentacion oficial
- Spring WebFlux: https://docs.spring.io/spring-framework/reference/web-reactive.html
- Project Reactor: https://projectreactor.io/docs
- Angular: https://angular.io/docs
- RxJS: https://rxjs.dev/api
- Playwright: https://playwright.dev/docs/intro
- Jest: https://jestjs.io/docs/getting-started
- Resilience4j: https://resilience4j.readme.io

## Proyecto practica recomendado

Implementar desde cero un mini-sistema de transferencias:
- Backend: Spring WebFlux + R2DBC + PostgreSQL + Security JWT
- Frontend: Angular + Reactive Forms + RxJS + HTTP Interceptor
- Tests backend: JUnit 5 + Mockito + StepVerifier + Karate
- Tests frontend: Jest + Playwright
- Documentacion: OpenAPI YAML + diagrama ER + diagrama de clases

Este proyecto cubre el 95% de los requisitos de la vacante.
En la entrevista, hablar de algo que construiste vale mas que explicar la teoria.

---

*Guia actualizada para la vacante de Ingeniero/a de Software Backend de Pagos TI (Nivel 1 - Junior) publicada el 1 de julio de 2026 por el Grupo Bancolombia.*
