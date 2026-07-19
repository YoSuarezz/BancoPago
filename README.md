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