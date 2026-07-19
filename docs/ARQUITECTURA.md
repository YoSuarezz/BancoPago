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