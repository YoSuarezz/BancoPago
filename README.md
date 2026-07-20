# BancoPago

Plataforma de pagos digitales que simula operaciones bancarias: transferencias P2P,
nómina de empleados, pagos a proveedores, pagos QR, integración PSE y pagos recurrentes.

Construida para demostrar dominio de arquitectura reactiva (Spring WebFlux),
frontend moderno (Angular + RxJS) y testing integral en el dominio de
Pagos Digitales.

## Estado del Proyecto

🚧 Desarrollo Activo — Ver [Hoja de Ruta](docs/ROADMAP.md)

## Stack Tecnológico

**Backend:** Spring Boot 3.x · WebFlux · R2DBC · PostgreSQL 16 · Redis · Flyway
**Frontend:** Angular 18 · RxJS · Angular Material
**Testing:** JUnit 5 · Mockito · StepVerifier · Jest · Playwright · Karate · JMeter
**Infraestructura:** Docker Compose

## Arquitectura

Ver [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) para detalles de Clean Architecture,
diagramas ER y decisiones técnicas.

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

- [Arquitectura y Diseño](docs/ARCHITECTURE.md)
- [Guía de Implementación](docs/IMPLEMENTATION_GUIDE.md)
- [Hoja de Ruta](docs/ROADMAP.md)
- [Guía de Contribución](CONTRIBUTING.md)

## Licencia

MIT — ver [LICENSE](LICENSE)
