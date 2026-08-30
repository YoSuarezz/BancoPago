# AGENTS.md

This repo is a two-part app: a Spring Boot 4 / WebFlux reactive backend (`backend/`, Java 21, Maven wrapper `./mvnw`, port 8080) and an Angular 18 frontend (`frontend/`, npm, dev server port 4200). Standard run commands live in `README.md`; commit/test conventions in `CONTRIBUTING.md`.

Dependency refresh (Maven `dependency:go-offline` + `npm install`) is handled by the startup update script, so it is not repeated here. The notes below are non-obvious startup/run caveats.

### Docker is required and must be started manually
Postgres + Redis run via `docker-compose.yml`, and backend tests use Testcontainers — both need a running Docker daemon. This VM has no systemd, so Docker does not auto-start:
- Start the daemon (once per boot): `sudo dockerd &` (config already sets the `fuse-overlayfs` storage driver).
- If `docker` needs sudo, the `ubuntu` user is in the `docker` group; a quick unblock for the current shell is `sudo chmod 666 /var/run/docker.sock`.
- Start infra from repo root: `docker compose up -d` (postgres becomes `healthy` on host port **5433**, redis on 6379).
- If Docker is not installed at all (fresh VM), install Docker Engine + `fuse-overlayfs` and switch to `iptables-legacy` (docker-in-docker requirements) before the steps above.

### Backend
- Run: `cd backend && ./mvnw spring-boot:run` (needs Postgres+Redis up first; Flyway migrates on startup). Health at `http://localhost:8080/actuator/health` (public, shows r2dbc + redis status).
- The committed `mvnw` may lack the executable bit; the update script `chmod +x` it, otherwise use `sh mvnw`.
- Tests: `./mvnw test` (unit tests run without Docker; integration tests use Testcontainers Postgres, so Docker must be running for the full suite).

### Spring Security & JWT Authentication
`SecurityConfig` secures `/api/**` with JWT (CSRF off; HTTP Basic and form login disabled). Public paths: `/api/v1/auth/**`, `/api-docs/**`, `/swagger-ui/**`, `/actuator/health`, `/actuator/info`. `JwtAuthenticationFilter` (reactive `WebFilter`) extracts the Bearer token and populates `ReactiveSecurityContextHolder`. `JwtService` generates/validates HS256 tokens via JJWT. Roles: `ROL_CLIENTE`, `ROL_ADMIN`. Config: `jwt.secret` and `jwt.expiration-ms` in `application.yml` (env vars `JWT_SECRET`, `JWT_EXPIRATION_MS`). Swagger UI: `http://localhost:8080/swagger-ui.html`.

### API responses (Module 1)
- Success envelope: `ApiResponse` with Spanish messages from `infrastructure/ResponseMessages` (do not hardcode strings in controllers).
- Person responses are sealed Client/Employee DTOs (`@JsonInclude(NON_NULL)`).
- Flyway: V1 schema, V2 person subtype columns, V3 document uniqueness `(document_type, document_number)`. If checksum mismatch locally: `docker compose down -v` then `up -d` (destructive).
- CreatePerson: CLIENT requires `clientNumber`; EMPLOYEE requires `position` + `area` (domain). Jakarta: `@Email` + `@Size(max=100)` on name.
- CreateAccount: max 5 accounts/owner; max 1 non-INACTIVE account of the same type per owner.

### Frontend
- Run: `cd frontend && npm start` (ng serve, `http://localhost:4200`). Tests: `npm test` (Jest via `jest-preset-angular`).
- Dashboard en `/accounts`: registrar persona, abrir cuenta, listar por `ownerId`, saldo live vía SSE (`EventSource` → `/api/v1/accounts/{accountId}/balance/stream`, evento `balance`).
- **IDs:** lista usa UUID de persona (`ownerId`); SSE usa UUID de cuenta (`accountId`). Confundirlos produce “cuenta no existe” / EventSource cerrado.
- Design system: regla `.cursor/rules/frontend-bancolombia-design.mdc` + skill `.cursor/skills/bancopago-frontend/`. Backend CORS permite `localhost`.
