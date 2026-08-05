# BancoPago

Plataforma de pagos digitales que simula operaciones bancarias: transferencias P2P,
nómina de empleados, pagos a proveedores, pagos QR, integración PSE y pagos recurrentes.

Construida para demostrar dominio de arquitectura reactiva (Spring WebFlux),
frontend moderno (Angular + RxJS) y testing integral en el dominio de
Pagos Digitales.

## Estado del Proyecto

🚧 Desarrollo Activo — Ver [Hoja de Ruta](docs/ROADMAP.md)

## Stack Tecnológico

**Backend:** Spring Boot 4.1 · WebFlux · R2DBC · PostgreSQL 16 · Redis · Flyway
**Frontend:** Angular 18 · RxJS · Angular Material
**Testing:** JUnit 5 · Mockito · StepVerifier · Jest · Playwright · Karate · JMeter
**Infraestructura:** Docker Compose

## Arquitectura

Clean Architecture + WebFlux. Flujo:

`Controller → Interactor → UseCase → RulesValidator (+ Rule) + Repository`

- [Arquitectura (visión y flujo)](docs/ARCHITECTURE.md)
- [Decisiones y convenciones](docs/ARCHITECTURE_DECISIONS.md)
- [Guía de implementación](docs/IMPLEMENTATION_GUIDE.md)

## Cómo ejecutar localmente

### Prerrequisitos
- Java 21
- Node.js 20+
- Docker & Docker Compose

### Pasos

```bash
# 1. Iniciar infraestructura (PostgreSQL + Redis)
docker-compose up -d

# 2. Ejecutar el backend
cd backend
./mvnw spring-boot:run

# 3. Ejecutar el frontend (terminal separada)
cd frontend
ng serve
```

- Backend: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Frontend: http://localhost:4200

## Documentación

- [Arquitectura](docs/ARCHITECTURE.md)
- [Decisiones de arquitectura](docs/ARCHITECTURE_DECISIONS.md)
- [Guía de implementación](docs/IMPLEMENTATION_GUIDE.md)
- [Hoja de ruta](docs/ROADMAP.md)
- [Plantilla de issues](docs/ISSUE_TEMPLATE.md)
- [Guía de contribución](CONTRIBUTING.md)

## Licencia

MIT — ver [LICENSE](LICENSE)
