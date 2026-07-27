# AGENTS.md

## Cursor Cloud specific instructions

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
- Tests: `./mvnw test` (36 domain + context-load tests; the context-load test spins up an ephemeral Testcontainers Postgres, so Docker must be running).

### Spring Security default auth (no `SecurityConfig` yet)
Only `/actuator/**` health is open. `/api-docs` and `/swagger-ui.html` require HTTP Basic auth using username `user` and the **generated password printed in the backend log on each startup** (grep `Using generated security password`). In a browser the Swagger UI shows "Failed to load remote configuration" because its async config fetch is unauthenticated under the default security setup — expected until a real `SecurityConfig` is added; the raw `/api-docs` JSON loads fine with Basic auth.

### Frontend
- Run: `cd frontend && npm start` (ng serve, `http://localhost:4200`). Tests: `npm test` (Jest via `jest-preset-angular`).
- The app is currently the default Angular scaffold with empty routes; there is no business UI wired to the backend yet.
