# BancoPago — Guía de Arranque del Proyecto
## Pasos iniciales detallados: de cero a primer commit funcional

> **Propósito:** Esta guía te lleva paso a paso desde que no existe nada hasta que tienes un repositorio bien estructurado, con backend y frontend arrancando, documentación base y un flujo de trabajo por ramas listo para desarrollar los 11 módulos del proyecto BancoPago.
>
> Cada paso incluye el **por qué**, no solo el **qué**. La idea es que entiendas cada decisión, no que copies comandos sin criterio — que es justo lo que se evalúa en la entrevista.

---

## Índice de esta guía

1. Decisiones previas antes de escribir código
2. Configuración del repositorio en GitHub
3. Estructura general del monorepo
4. Spring Initializr — configuración del backend
5. Estructura de paquetes inicial (Clean Architecture)
6. Docker Compose — infraestructura local
7. Angular — inicialización del frontend
8. Documentación inicial obligatoria
9. Estrategia de ramas (branching model)
10. Conventional Commits
11. Configuración de calidad de código
12. Primer commit — checklist completo
13. Orden de las primeras 10 tareas reales

---

# PASO 1 — Decisiones previas antes de escribir código

Antes de correr un solo comando, resuelve estas preguntas. Son las que un Ingeniero Nivel 1 debe poder justificar en la sustentación de su proyecto.

## 1.1 ¿Monorepo o repos separados?

**Decisión recomendada: Monorepo** (backend + frontend + docs + tests en un solo repositorio)

| | Monorepo | Repos separados |
|--|----------|-----------------|
| Ventaja | Un solo lugar, versionado conjunto, fácil de compartir en la entrevista | Equipos grandes con ciclos de release independientes |
| Para este proyecto | ✅ Recomendado — es tu proyecto de práctica personal | Tendría sentido en un equipo real con pipelines separados |

**Justificación que puedes dar en la entrevista:**
> "Elegí monorepo porque backend y frontend evolucionan juntos en este proyecto, y quería que cualquier persona revisando el repositorio viera el sistema completo sin saltar entre repos. En un contexto de equipo grande con pipelines de CI/CD independientes, separaría los repos para no acoplar los despliegues."

## 1.2 Maven o Gradle

**Decisión recomendada: Maven**

Bancolombia usa predominantemente Maven en sus proyectos Java (lo viste en tu práctica con Azure DevOps). Usa lo que se usa en la organización a la que aplicas — es una señal de que ya conoces el ecosistema.

## 1.3 Versión de Java

**Decisión recomendada: Java 21 (LTS)**

Java 21 es la LTS más reciente con soporte a largo plazo, incluye Virtual Threads (Project Loom), Records estables, y Pattern Matching maduro. Si Bancolombia todavía usa Java 17 internamente (es común en la industria bancaria por estabilidad), igual vale la pena mostrar que conoces las features más recientes — simplemente asegúrate de poder explicar la diferencia si te preguntan.

## 1.4 Nombre del proyecto

**Decisión: `bancopago`**

Reglas para nombres de repositorio:
- Minúsculas, sin espacios, guiones en vez de espacios.
- Debe ser reconocible sin contexto adicional.
- Evita nombres genéricos como `proyecto-final` o `test-app`.

---

# PASO 2 — Configuración del repositorio en GitHub

## 2.1 Crear el repositorio

Ve a GitHub → New Repository con esta configuración exacta:

```
Nombre del repositorio:  bancopago

Descripción (bio corta, aparece en el listado de repos):
Sistema integral de pagos digitales (transferencias, nómina, proveedores,
QR, PSE) construido con Spring WebFlux + Angular. Proyecto de práctica
enfocado en el dominio de Pagos Digitales TI.

Visibilidad: Public
  → Público porque quieres poder compartir el link en la entrevista
  → Si tiene datos sensibles de práctica, usa Private y compartes
    acceso puntual al entrevistador

Initialize repository with:
  ☑ Add a README file
  ☑ Add .gitignore → plantilla "Java" (la ajustaremos después)
  ☑ Choose a license → MIT License
     (Licencia permisiva, estándar para proyectos personales/portafolio)
```

## 2.2 Topics del repositorio (mejora el descubrimiento y comunica stack)

En la página del repo → ⚙️ (junto a "About") → Topics:

```
spring-boot, spring-webflux, angular, reactive-programming,
r2dbc, postgresql, java21, typescript, rxjs, clean-architecture,
payment-system, junit5, playwright, karate-framework
```

Esto no es decorativo: cuando un reclutador o entrevistador entra al repo, los topics comunican en 3 segundos qué dominas.

## 2.3 Configuración inicial de settings

```
Settings → General
  ☑ Features → Issues (activado, los usarás para trackear bugs
    como pide la vacante: "documentar defectos usando sistemas
    de tracking de bugs")
  ☑ Features → Projects (activado, para el tablero Kanban)

Settings → Branches → Branch protection rules → Add rule
  Branch name pattern: main
  ☑ Require a pull request before merging
  ☑ Require approvals: 0 (proyecto personal, pero deja el hábito
    configurado — en equipo real sería 1 o 2)
  ☑ Require status checks to pass before merging (cuando tengas CI)
```

**Por qué proteger `main` incluso en un proyecto personal:** Practicar el flujo de PR (Pull Request) desde ahora te entrena en el hábito que usarás en Bancolombia. Nunca hacer push directo a main es una de las prácticas más básicas y más citadas en entrevistas de "cuéntame tu flujo de trabajo con Git".

## 2.4 Clonar en tu máquina

```bash
git clone https://github.com/TU_USUARIO/bancopago.git
cd bancopago
```

---

# PASO 3 — Estructura general del monorepo

Antes de generar el proyecto Spring Boot, crea el esqueleto de carpetas de más alto nivel:

```bash
mkdir -p backend frontend e2e karate jmeter docs
touch .gitignore
```

Estructura resultante:

```
bancopago/
├── backend/          ← Spring Boot WebFlux (se genera en el Paso 4)
├── frontend/          ← Angular (se genera en el Paso 7)
├── e2e/                ← Tests Playwright
├── karate/             ← Tests de contrato de API
├── jmeter/             ← Planes de prueba de performance
├── docs/                ← Documentación técnica (ER, secuencia, decisiones)
├── docker-compose.yml   ← Se crea en el Paso 6
├── .gitignore
├── README.md
├── CONTRIBUTING.md
└── LICENSE (ya viene del setup de GitHub)
```

**Por qué esta estructura desde el día 1:** Si empiezas solo con el backend y agregas todo lo demás después "cuando lo necesites", terminas con una estructura desordenada y probablemente reorganizando carpetas a mitad de proyecto. Definir el esqueleto completo primero — aunque las carpetas estén vacías — te obliga a pensar en el proyecto completo desde el principio, que es exactamente el enfoque de "ciclo de vida completo" que pide la vacante.

---

# PASO 4 — Spring Initializr: configuración exacta del backend

## 4.1 Generación vía web (start.spring.io)

Ve a **https://start.spring.io** y configura exactamente así:

```
Project:        Maven
Language:       Java
Spring Boot:    3.3.x (la última versión GA estable, evita snapshots)

Project Metadata:
  Group:         com.bancopago
  Artifact:      backend
  Name:          backend
  Description:   Sistema de pagos digitales — Backend reactivo con Spring WebFlux
  Package name:  com.bancopago.backend
  Packaging:     Jar
  Java:          21
```

### Dependencias a agregar (busca cada una en el buscador de Initializr)

```
□ Spring Reactive Web          → spring-boot-starter-webflux
□ Spring Data R2DBC            → spring-boot-starter-data-r2dbc
□ PostgreSQL Driver            → org.postgresql:postgresql (driver JDBC, para herramientas de migración)
□ R2DBC PostgreSQL Driver      → io.r2dbc:r2dbc-postgresql (driver reactivo)
□ Spring Security              → spring-boot-starter-security
□ Validation                   → spring-boot-starter-validation
□ Spring Boot Actuator         → spring-boot-starter-actuator
□ Lombok                       → reduce boilerplate (getters/setters/constructores)
□ Spring Boot DevTools         → hot reload en desarrollo
□ Testcontainers                → para integration tests con PostgreSQL real
```

### Dependencias que agregarás manualmente en el pom.xml (no están en Initializr)

```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<!-- Resilience4j (Circuit Breaker, Retry) -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.2.0</version>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-reactor</artifactId>
    <version>2.2.0</version>
</dependency>

<!-- Redis reactivo (idempotencia) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>

<!-- OpenAPI / Swagger UI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
    <version>2.6.0</version>
</dependency>

<!-- Flyway (migraciones de BD versionadas) -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>r2dbc</artifactId>
    <scope>test</scope>
</dependency>
```

### Por qué cada dependencia clave (para que lo puedas explicar)

| Dependencia | Por qué la eliges |
|-------------|-------------------|
| **spring-boot-starter-webflux** | Requisito explícito de la vacante — programación reactiva |
| **spring-data-r2dbc** | JPA es bloqueante (usa JDBC). En un stack 100% reactivo, R2DBC es la contraparte no-bloqueante |
| **r2dbc-postgresql** | Driver reactivo específico. El driver JDBC normal de PostgreSQL NO sirve para R2DBC |
| **flyway** | Versionar el esquema de BD es una buena práctica que evitas con `ddl-auto=update` (mala práctica en producción) |
| **resilience4j** | Para el Circuit Breaker hacia PSE (Módulo 7 del proyecto) |
| **redis-reactive** | Idempotencia y caché, de forma no-bloqueante consistente con el resto del stack |
| **springdoc-openapi** | Genera el Swagger UI automáticamente — cubre el requisito de documentación de APIs de la vacante |
| **testcontainers** | Integration tests contra un PostgreSQL real en Docker, no contra H2 en memoria (que se comporta diferente) |

## 4.2 Descargar y descomprimir

```bash
# Descarga el .zip desde start.spring.io y descomprímelo dentro de /backend
cd bancopago
unzip ~/Downloads/backend.zip -d backend
rm ~/Downloads/backend.zip
```

## 4.3 Verificar que arranca

```bash
cd backend
./mvnw spring-boot:run
```

Si ves el banner de Spring Boot y no hay errores (aunque falle al conectar a BD porque aún no existe — eso es esperado), el setup base es correcto.

---

# PASO 5 — Estructura de paquetes inicial (Clean Architecture)

Dentro de `backend/src/main/java/com/bancopago/backend/`, crea la estructura de carpetas ANTES de escribir la primera clase. Esto evita que el código empiece a mezclarse por pereza de crear carpetas después.

```bash
cd backend/src/main/java/com/bancopago/backend

mkdir -p domain/model domain/exceptions domain/gateways
mkdir -p usecase/transferencia usecase/nomina usecase/proveedor usecase/recurrente
mkdir -p infrastructure/entrypoints/api/dto
mkdir -p infrastructure/drivenadapters/r2dbc infrastructure/drivenadapters/redis infrastructure/drivenadapters/external
mkdir -p infrastructure/config

# Crear un .gitkeep en cada carpeta vacía para que Git la trackee
find . -type d -empty -exec touch {}/.gitkeep \;
```

Resultado:

```
com.bancopago.backend/
├── BackendApplication.java          (generado por Initializr)
├── domain/
│   ├── model/          .gitkeep
│   ├── exceptions/     .gitkeep
│   └── gateways/        .gitkeep
├── usecase/
│   ├── transferencia/  .gitkeep
│   ├── nomina/          .gitkeep
│   ├── proveedor/       .gitkeep
│   └── recurrente/      .gitkeep
└── infrastructure/
    ├── entrypoints/api/dto/  .gitkeep
    ├── drivenadapters/
    │   ├── r2dbc/        .gitkeep
    │   ├── redis/        .gitkeep
    │   └── external/     .gitkeep
    └── config/            .gitkeep
```

**La razón de crear esto vacío desde ya:** cuando llegues a implementar el Módulo 2 (Transferencias) de tu guía de proyecto, no perderás tiempo decidiendo dónde va cada clase — ya lo decidiste aquí, con calma, pensando en la arquitectura completa.

---

# PASO 6 — Docker Compose: infraestructura local

Crea `docker-compose.yml` en la raíz del monorepo (no dentro de `backend/`):

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: bancopago-postgres
    environment:
      POSTGRES_DB: bancopago_db
      POSTGRES_USER: bancopago
      POSTGRES_PASSWORD: bancopago_dev_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U bancopago"]
      interval: 5s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: bancopago-redis
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
```

**Nota importante sobre Flyway y R2DBC:** R2DBC no ejecuta migraciones Flyway directamente porque Flyway necesita JDBC. Por eso agregamos tanto el driver `postgresql` (JDBC, solo para Flyway) como `r2dbc-postgresql` (para la app en runtime) en el Paso 4. Esto es una pregunta común de entrevista:

> **"¿Por qué tu proyecto tiene dos drivers de PostgreSQL?"**
> "Flyway, la herramienta que uso para versionar el esquema de base de datos, requiere JDBC porque es una librería síncrona por diseño. La aplicación en runtime usa R2DBC porque es reactiva. Ambos coexisten: Flyway corre las migraciones al arrancar con JDBC, y luego la app opera con R2DBC de forma no-bloqueante."

## 6.1 Levantar la infraestructura

```bash
docker-compose up -d
docker-compose ps   # Verificar que ambos servicios están "healthy"
```

## 6.2 application.yml del backend

Crea `backend/src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: bancopago-backend

  r2dbc:
    url: r2dbc:postgresql://localhost:5432/bancopago_db
    username: bancopago
    password: bancopago_dev_password
    pool:
      initial-size: 5
      max-size: 20

  flyway:
    url: jdbc:postgresql://localhost:5432/bancopago_db
    user: bancopago
    password: bancopago_dev_password
    locations: classpath:db/migration

  data:
    redis:
      host: localhost
      port: 6379

  security:
    jwt:
      secret: ${JWT_SECRET:cambiar-este-secreto-en-produccion-minimo-256-bits}
      expiration: 3600000  # 1 hora en milisegundos

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
  endpoint:
    health:
      show-details: always

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

logging:
  level:
    com.bancopago: DEBUG
    org.springframework.r2dbc: DEBUG
```

## 6.3 Primera migración Flyway

Crea `backend/src/main/resources/db/migration/V1__create_persona_cuenta.sql`:

```sql
CREATE TABLE persona (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(200) NOT NULL,
    documento VARCHAR(20) NOT NULL UNIQUE,
    tipo_documento VARCHAR(20) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    tipo VARCHAR(20) NOT NULL, -- CLIENTE | EMPLEADO
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE cuenta (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    persona_id UUID NOT NULL REFERENCES persona(id),
    numero VARCHAR(20) NOT NULL UNIQUE,
    tipo VARCHAR(20) NOT NULL, -- CORRIENTE | AHORROS | NOMINA | TESORERIA | PROVEEDOR
    saldo DECIMAL(15,2) NOT NULL DEFAULT 0,
    moneda VARCHAR(3) NOT NULL DEFAULT 'COP',
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    version BIGINT NOT NULL DEFAULT 0, -- Optimistic locking
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_cuenta_persona ON cuenta(persona_id);
CREATE INDEX idx_cuenta_numero ON cuenta(numero);
```

Esta es tu primera pieza de documentación viva: las migraciones de Flyway *son* el historial versionado del modelo de datos, mejor que cualquier diagrama estático que se desactualiza.

---

# PASO 7 — Angular: inicialización del frontend

## 7.1 Instalar Angular CLI (si no lo tienes)

```bash
npm install -g @angular/cli@18
ng version   # Verificar instalación
```

## 7.2 Generar el proyecto

```bash
cd bancopago
ng new frontend --routing --style=scss --strict --skip-git

# --routing   → genera el módulo de routing desde el inicio
# --style=scss → SCSS en vez de CSS plano (más control de estilos)
# --strict    → TypeScript en modo estricto (mejor calidad, detecta más errores)
# --skip-git  → no crear un repo Git anidado (ya estamos en uno)
```

## 7.3 Dependencias adicionales

```bash
cd frontend

# Angular Material (componentes UI)
ng add @angular/material

# Jest en lugar de Karma+Jasmine (requisito explícito de la vacante)
ng add @briebug/jest-schematic

# Testing utilities
npm install --save-dev @testing-library/angular

# Interceptor y manejo de JWT
npm install jwt-decode
```

## 7.4 Verificar que arranca

```bash
ng serve
# Abre http://localhost:4200
```

## 7.5 Estructura inicial de carpetas

```bash
cd src/app
mkdir -p core/auth core/services core/interceptors
mkdir -p shared/components shared/pipes shared/models
mkdir -p features/transferencias features/nomina features/proveedores
mkdir -p features/qr features/recurrentes features/auditoria
mkdir -p portals/cliente portals/operativo
```

---

# PASO 8 — Documentación inicial obligatoria

La vacante pide explícitamente documentación técnica como parte del rol. Estos son los documentos que debes tener desde el día 1, no al final.

## 8.1 README.md (raíz del proyecto)

```markdown
# BancoPago

Sistema integral de pagos digitales que simula las operaciones de un banco:
transferencias P2P, nómina de empleados, pagos a proveedores, pagos QR,
integración PSE y pagos recurrentes.

Proyecto de práctica orientado a demostrar dominio de arquitectura reactiva
(Spring WebFlux), frontend moderno (Angular + RxJS) y testing integral,
en el contexto del dominio de Pagos Digitales.

## Estado del proyecto

🚧 En desarrollo activo — Ver [Project Board](link-al-proyecto-de-github)

## Stack Tecnológico

**Backend:** Spring Boot 3.3 · WebFlux · R2DBC · PostgreSQL 16 · Redis · Flyway
**Frontend:** Angular 18 · RxJS · Angular Material
**Testing:** JUnit 5 · Mockito · StepVerifier · Jest · Playwright · Karate · JMeter
**Infraestructura:** Docker Compose

## Arquitectura

Ver [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md) para el detalle de
Clean Architecture, diagramas ER y decisiones técnicas.

## Cómo correr el proyecto localmente

### Prerrequisitos
- Java 21
- Node.js 20+
- Docker y Docker Compose

### Pasos

\`\`\`bash
# 1. Levantar infraestructura (PostgreSQL + Redis)
docker-compose up -d

# 2. Correr el backend
cd backend
./mvnw spring-boot:run

# 3. Correr el frontend (en otra terminal)
cd frontend
ng serve
\`\`\`

- Backend: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Frontend: http://localhost:4200

## Documentación

- [Arquitectura y decisiones técnicas](docs/ARQUITECTURA.md)
- [Modelo de datos (ER)](docs/MODELO-DATOS.md)
- [Guía de contribución](CONTRIBUTING.md)
- [Roadmap de módulos](docs/ROADMAP.md)

## Licencia

MIT — ver [LICENSE](LICENSE)
```

## 8.2 CONTRIBUTING.md (raíz)

```markdown
# Guía de Contribución — BancoPago

Aunque este es un proyecto de práctica personal, sigue las convenciones
de un flujo de trabajo profesional real.

## Flujo de trabajo

1. Toda la funcionalidad se desarrolla en una rama a partir de \`develop\`.
2. Nombra las ramas siguiendo el patrón: \`tipo/descripcion-corta\`
   Ejemplos: \`feature/transferencias-p2p\`, \`fix/validacion-saldo\`
3. Los commits siguen Conventional Commits (ver sección abajo).
4. Antes de abrir un Pull Request, verifica que:
   - Los tests pasan: \`./mvnw test\` (backend) y \`ng test\` (frontend)
   - El código sigue las convenciones del linter
5. El PR se mergea a \`develop\`. \`main\` solo recibe merges desde \`develop\`
   cuando un conjunto de features está listo para "release".

## Conventional Commits

\`\`\`
<tipo>(<alcance opcional>): <descripción corta>

[cuerpo opcional]
\`\`\`

Tipos permitidos:
- \`feat\`: nueva funcionalidad
- \`fix\`: corrección de bug
- \`docs\`: solo documentación
- \`test\`: agregar o corregir tests
- \`refactor\`: cambio de código sin alterar comportamiento
- \`chore\`: tareas de mantenimiento (dependencias, configuración)
- \`perf\`: mejora de performance

Ejemplos:
\`\`\`
feat(transferencias): agregar validación de límite diario
fix(nomina): corregir cálculo de deducción de salud
docs(readme): actualizar instrucciones de instalación
test(transferencias): agregar test de idempotencia
\`\`\`

## Estándares de código

- Backend: seguir Clean Architecture (domain → usecase → infrastructure)
- Frontend: un componente, una responsabilidad
- Toda función pública debe tener al menos un test
```

## 8.3 docs/ARQUITECTURA.md

```markdown
# Arquitectura — BancoPago

## Estilo arquitectónico

Clean Architecture con separación en tres capas:

\`\`\`
domain/          → Entidades y reglas de negocio puras, sin dependencias externas
usecase/         → Orquestación de casos de uso, depende solo de domain
infrastructure/  → Controllers, adaptadores de BD, clientes HTTP externos
\`\`\`

Regla de dependencias: las capas externas dependen de las internas,
nunca al revés.

## Por qué Spring WebFlux

[Completar cuando implementes el primer módulo — explica la decisión
con el caso de uso real de nómina masiva]

## Por qué R2DBC en vez de JPA

[Completar]

## Por qué idempotencia con Redis

[Completar]

## Diagrama de componentes

[Agregar cuando tengas Docker Compose funcionando con todos los servicios]

## Decisiones pendientes de revisar

- [ ] Estrategia de manejo de eventos (síncrono vs Kafka)
- [ ] Estrategia de secrets en producción (actualmente variables de entorno)
```

**Por qué dejar secciones "[Completar]" desde el inicio:** Documentar antes de implementar te obliga a pensar la decisión con calma. Documentar solo al final es lo que hace que la documentación termine siendo genérica y poco útil. Ir llenando el ARQUITECTURA.md a medida que tomas cada decisión real es más honesto y más fácil de defender en la sustentación.

## 8.4 docs/ROADMAP.md

```markdown
# Roadmap de Módulos — BancoPago

| # | Módulo | Estado | Rama |
|---|--------|--------|------|
| 1 | Gestión de Cuentas y Usuarios | 🔲 Por iniciar | - |
| 2 | Transferencias P2P | 🔲 Por iniciar | - |
| 3 | Nómina de Empleados | 🔲 Por iniciar | - |
| 4 | Pagos a Proveedores | 🔲 Por iniciar | - |
| 5 | Pagos Recurrentes | 🔲 Por iniciar | - |
| 6 | Pagos QR | 🔲 Por iniciar | - |
| 7 | Integración PSE | 🔲 Por iniciar | - |
| 8 | Auditoría | 🔲 Por iniciar | - |
| 9 | Conciliación | 🔲 Por iniciar | - |
| 10 | Portal Cliente (Angular) | 🔲 Por iniciar | - |
| 11 | Portal Operativo (Angular) | 🔲 Por iniciar | - |

Leyenda: 🔲 Por iniciar · 🟡 En progreso · ✅ Completado
```

## 8.5 GitHub Issues iniciales

En vez de escribir todo en el roadmap y olvidarlo, crea un Issue por módulo en GitHub:

```
Ve a Issues → New Issue, crea 11 issues (uno por módulo), cada uno con:

Título: [Módulo 1] Gestión de Cuentas y Usuarios
Labels: feature, backend, frontend
Descripción:
  ## Objetivo
  [Copiar la descripción del módulo desde tu guía de proyecto]

  ## Criterios de aceptación
  - [ ] CRUD de personas
  - [ ] Apertura y cierre de cuentas
  - [ ] Consulta de saldo con Mono
  - [ ] Streaming de saldo con Flux (SSE)
  - [ ] Tests unitarios >80% cobertura
  - [ ] Documentado en OpenAPI
```

Esto directamente practica lo que pide la vacante: *"Documentarás defectos de software, usando sistemas de tracking de bugs"* — usar Issues desde ya, no solo para bugs sino para features, te da experiencia real con el flujo.

---

# PASO 9 — Estrategia de ramas (branching model)

## 9.1 Modelo recomendado: GitFlow simplificado

Para un proyecto personal no necesitas el GitFlow completo (con ramas de release y hotfix separadas), pero sí vale la pena practicar la separación `main` / `develop` / `feature`:

```
main
  │  ← Solo código estable, listo para "mostrar" en la entrevista
  │  ← Protegida: nadie hace push directo
  │
develop
  │  ← Rama de integración. Aquí se juntan los features completos
  │  ← Es la rama "por defecto" para trabajar
  │
  ├── feature/cuentas-crud
  ├── feature/transferencias-p2p
  ├── feature/nomina-lote-procesamiento
  ├── feature/angular-portal-cliente
  ├── fix/validacion-saldo-negativo
  └── docs/arquitectura-decisiones
```

## 9.2 Crear la rama develop

```bash
git checkout main
git pull origin main
git checkout -b develop
git push -u origin develop

# En GitHub: Settings → Branches → Default branch → cambiar a "develop"
# Así, cualquier PR nuevo apunta a develop por defecto
```

## 9.3 Convención de nombres de rama

```
feature/nombre-corto-descriptivo   → nueva funcionalidad
fix/nombre-del-bug                 → corrección de bug
docs/que-se-documenta              → solo documentación
refactor/que-se-refactoriza        → refactor sin cambio de comportamiento
test/que-se-testea                 → agregar tests
chore/que-se-hace                  → tareas de mantenimiento

Ejemplos reales para BancoPago:
feature/cuentas-crud
feature/transferencias-idempotencia
feature/nomina-flujo-aprobacion
feature/angular-jwt-interceptor
fix/race-condition-debito-saldo
docs/diagrama-secuencia-transferencia
test/karate-contrato-transferencias
```

## 9.4 Flujo de trabajo real para cada módulo

```bash
# 1. Actualizar develop
git checkout develop
git pull origin develop

# 2. Crear rama de feature
git checkout -b feature/cuentas-crud

# 3. Trabajar, hacer commits pequeños y frecuentes
git add .
git commit -m "feat(cuentas): agregar entidad Cuenta y migración Flyway"

git add .
git commit -m "feat(cuentas): implementar CuentaR2dbcRepository"

git add .
git commit -m "test(cuentas): agregar tests unitarios de CuentaService"

# 4. Subir la rama
git push -u origin feature/cuentas-crud

# 5. Abrir Pull Request en GitHub: feature/cuentas-crud → develop
#    Describe qué se implementó, vincula el Issue correspondiente

# 6. Mergear (squash merge recomendado para mantener develop limpio)

# 7. Borrar la rama local y remota
git checkout develop
git pull origin develop
git branch -d feature/cuentas-crud
```

## 9.5 Cuándo promover develop a main

Cuando un conjunto de módulos forma un "hito" demostrable (por ejemplo: "Transferencias P2P completo con tests"), mergeas develop → main con un tag de versión:

```bash
git checkout main
git pull origin main
git merge develop
git tag -a v0.1.0 -m "Hito 1: Cuentas + Transferencias P2P completo"
git push origin main --tags
```

Esto te da un historial de versiones que puedes mostrar en la entrevista: *"aquí pueden ver que v0.1.0 fue el primer hito con transferencias funcionando end-to-end"*.

---

# PASO 10 — Conventional Commits en profundidad

Ya lo mencionamos en CONTRIBUTING.md, pero merece práctica real desde el primer commit.

## 10.1 Por qué importa

Un historial de commits como este:

```
❌ MAL:
- "cambios"
- "fix"
- "arreglos varios"
- "wip"
```

vs. uno como este:

```
✅ BIEN:
- feat(cuentas): agregar entidad Cuenta con validación de saldo
- test(cuentas): cubrir caso de saldo negativo con test parametrizado
- fix(cuentas): corregir precisión decimal en cálculo de saldo
- docs(cuentas): documentar endpoints en OpenAPI
```

El segundo historial es una narrativa legible de cómo evolucionó el proyecto. Es literalmente lo que un entrevistador ve si revisa tu repositorio, y comunica disciplina de ingeniería sin que tengas que decir una palabra.

## 10.2 Configurar un commit-msg hook (opcional pero recomendado)

```bash
npm install --save-dev @commitlint/cli @commitlint/config-conventional husky
npx husky init
```

Crea `commitlint.config.js` en la raíz:

```javascript
module.exports = { extends: ['@commitlint/config-conventional'] };
```

Crea `.husky/commit-msg`:

```bash
npx --no -- commitlint --edit "$1"
```

A partir de ahora, si intentas hacer un commit que no siga Conventional Commits, el hook lo rechaza. Esto te fuerza el hábito automáticamente en vez de depender de tu memoria.

---

# PASO 11 — Configuración de calidad de código

## 11.1 .gitignore completo

Reemplaza el `.gitignore` generado por GitHub con esta versión (cubre backend Java + frontend Angular):

```gitignore
### Backend (Java/Maven) ###
backend/target/
backend/.mvn/wrapper/maven-wrapper.jar
!backend/.mvn/wrapper/maven-wrapper.properties
*.class
*.jar
!.mvn/wrapper/maven-wrapper.jar

### Frontend (Angular/Node) ###
frontend/node_modules/
frontend/dist/
frontend/.angular/
frontend/coverage/
frontend/e2e/screenshots/
frontend/e2e/videos/

### IDEs ###
.idea/
*.iml
.vscode/
!.vscode/extensions.json
.DS_Store

### Variables de entorno (NUNCA subir secretos) ###
.env
.env.local
**/application-local.yml
**/application-secrets.yml

### Docker ###
docker-compose.override.yml

### Logs ###
*.log
logs/

### Testcontainers ###
.testcontainers.properties
```

**Por qué es crítico el bloque de variables de entorno:** Nunca debes subir credenciales, ni siquiera las de desarrollo local, como buena práctica. Es una de las primeras cosas que se evalúan al revisar un repositorio en un contexto bancario — la higiene de secretos.

## 11.2 .editorconfig (consistencia entre editores)

Crea `.editorconfig` en la raíz:

```ini
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true
indent_style = space

[*.java]
indent_size = 4

[*.{ts,html,scss,json,yml,yaml}]
indent_size = 2

[*.md]
trim_trailing_whitespace = false
```

## 11.3 Checkstyle (opcional, para backend)

Si quieres ir un paso más allá y mostrar disciplina de estilo de código Java (Bancolombia valora mucho esto), agrega Checkstyle con las reglas de Google:

```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.5.0</version>
    <configuration>
        <configLocation>google_checks.xml</configLocation>
        <consoleOutput>true</consoleOutput>
        <failsOnError>true</failsOnError>
    </configuration>
</plugin>
```

---

# PASO 12 — Primer commit: checklist completo

Antes de hacer tu primer commit real, verifica que tienes todo esto:

```
Estructura de carpetas
  ☑ backend/ generado desde Spring Initializr y arranca sin errores
  ☑ frontend/ generado con Angular CLI y arranca sin errores
  ☑ Estructura de Clean Architecture creada (con .gitkeep en vacías)
  ☑ e2e/, karate/, jmeter/, docs/ creadas

Infraestructura
  ☑ docker-compose.yml con PostgreSQL y Redis
  ☑ docker-compose up -d levanta ambos servicios en estado "healthy"
  ☑ application.yml configurado apuntando a esos servicios

Base de datos
  ☑ Primera migración Flyway creada (V1__create_persona_cuenta.sql)
  ☑ La migración corre exitosamente al arrancar el backend

Documentación
  ☑ README.md completo con instrucciones de instalación
  ☑ CONTRIBUTING.md con convenciones del proyecto
  ☑ docs/ARQUITECTURA.md con estructura inicial
  ☑ docs/ROADMAP.md con los 11 módulos listados

Git y GitHub
  ☑ .gitignore correcto (sin node_modules, target, secretos)
  ☑ .editorconfig configurado
  ☑ Rama develop creada y configurada como default
  ☑ Branch protection en main configurado
  ☑ 11 Issues creados (uno por módulo) en GitHub
  ☑ Topics del repositorio configurados

Commits
  ☑ Conventional Commits configurado (commitlint + husky, opcional)
  ☑ Primer commit sigue el formato: "chore: configuración inicial del proyecto"
```

## 12.1 El primer commit real

```bash
git checkout develop
git add .
git commit -m "chore: configuración inicial del proyecto

- Backend Spring Boot 3.3 con WebFlux, R2DBC, Security, Flyway
- Frontend Angular 18 con Material y Jest configurado
- Docker Compose con PostgreSQL 16 y Redis 7
- Estructura de Clean Architecture (domain/usecase/infrastructure)
- Documentación inicial: README, CONTRIBUTING, ARQUITECTURA, ROADMAP
- .gitignore, .editorconfig y convenciones de commit configuradas"

git push -u origin develop
```

---

# PASO 13 — Orden de las primeras 10 tareas reales

Con todo el andamiaje listo, este es el orden recomendado para empezar a escribir código de negocio, cada uno como una rama `feature/` independiente:

```
1. feature/persona-cuenta-domain
   → Crear las clases de dominio: Persona, Cuenta (sin lógica de infraestructura)
   → Tests unitarios de las reglas de negocio (ej: Cuenta no permite saldo negativo)

2. feature/cuenta-r2dbc-adapter
   → Implementar el repositorio R2DBC y el adaptador que conecta domain con BD
   → Integration test con Testcontainers

3. feature/security-jwt-basico
   → Configurar Spring Security con JWT (login, registro, filtro)
   → Roles básicos: ROL_CLIENTE, ROL_ADMIN

4. feature/cuentas-api-rest
   → Controller REST para CRUD de cuentas
   → OpenAPI documentado
   → Karate feature file para el contrato

5. feature/angular-auth-modulo
   → Login component, AuthService, JwtInterceptor, AuthGuard en Angular
   → Jest tests del AuthService

6. feature/angular-dashboard-cuentas
   → Pantalla de listado de cuentas del cliente autenticado
   → Consumo del API con HttpClient + manejo de errores

7. feature/transferencias-domain-usecase
   → Entidad Pago, EjecutarTransferenciaUseCase con toda la lógica de negocio
   → Tests unitarios exhaustivos (saldo insuficiente, cuenta bloqueada, límites)

8. feature/transferencias-idempotencia-redis
   → IdempotencyService con Redis
   → Tests de idempotencia (mismo key, misma respuesta)

9. feature/transferencias-api-reactive
   → TransferenciaController en WebFlux (Mono/Flux)
   → StepVerifier tests
   → Karate: contrato completo con escenario de idempotencia

10. feature/angular-nueva-transferencia
    → Formulario reactivo de transferencia en Angular
    → Playwright E2E: flujo completo de transferencia exitosa
```

Al completar estas 10 tareas ya tienes el Módulo 1 (Cuentas) y el Módulo 2 (Transferencias) completos end-to-end — backend, frontend, y las 5 capas de testing que pide la vacante. Es tu primer hito real (`v0.1.0`).

---

# Resumen visual del flujo completo

```
┌─────────────────────────────────────────────────────────┐
│  1. Decisiones (monorepo, Maven, Java 21)               │
└───────────────────────┬───────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  2. GitHub: crear repo, topics, branch protection        │
└───────────────────────┬───────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  3-4. Estructura monorepo + Spring Initializr             │
└───────────────────────┬───────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  5. Paquetes Clean Architecture (vacíos, con .gitkeep)   │
└───────────────────────┬───────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  6. Docker Compose (PostgreSQL + Redis) + Flyway V1       │
└───────────────────────┬───────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  7. Angular init + estructura de carpetas                │
└───────────────────────┬───────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  8. Documentación (README, CONTRIBUTING, ARQUITECTURA)   │
└───────────────────────┬───────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  9-10. Rama develop + Conventional Commits                │
└───────────────────────┬───────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  11. .gitignore, .editorconfig, Checkstyle                │
└───────────────────────┬───────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  12. Primer commit: "chore: configuración inicial"        │
└───────────────────────┬───────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  13. Empezar las 10 tareas del Módulo 1 y 2               │
│      → Primer hito real: v0.1.0                           │
└─────────────────────────────────────────────────────────┘
```

---

*Con esta base, cada nueva funcionalidad que agregues sigue el mismo patrón: rama feature desde develop, TDD cuando sea posible, documentación actualizada en el mismo PR, y merge a develop cuando esté completa. Esto no es burocracia — es exactamente el flujo de trabajo que la vacante describe cuando dice "velarás por el correcto ciclo de vida del software".*
