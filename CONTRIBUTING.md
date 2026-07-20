# Guía de Contribución — BancoPago

Aunque es un proyecto personal de práctica, sigue convenciones de flujo de trabajo profesional.

## Flujo de Trabajo

1. Todas las funcionalidades se desarrollan en ramas desde `develop`.
2. Nombra las ramas con el patrón: `tipo/descripcion-corta`
   Ejemplos: `feature/transferencias-p2p`, `fix/validacion-saldo`
3. Los commits siguen Conventional Commits (ver abajo).
4. Antes de abrir un Pull Request, verifica:
    - Tests pasan: `./mvnw test` (backend) y `ng test` (frontend)
    - El código sigue las convenciones del linter
5. Los PRs se fusionan a `develop`. `main` solo recibe merges de `develop`
   cuando un conjunto de funcionalidades está listo para "release".

## Conventional Commits

```
<tipo>(<alcance opcional>): <descripción corta>

[cuerpo opcional]
```

Tipos permitidos:
- `feat`: nueva funcionalidad
- `fix`: corrección de bug
- `docs`: solo documentación
- `test`: agregar o corregir tests
- `refactor`: cambio de código sin cambio de comportamiento
- `chore`: tareas de mantenimiento (dependencias, config)
- `perf`: mejora de rendimiento

Ejemplos:
```
feat(transfers): agregar validación de límite diario
fix(payroll): corregir cálculo de deducción de salud
docs(readme): actualizar instrucciones de instalación
test(transfers): agregar test de idempotencia
```

## Estándares de Código

- Backend: seguir Clean Architecture (domain → application → infrastructure)
- Frontend: un componente, una responsabilidad
- Todas las funciones públicas deben tener al menos un test
- Todo el código, comentarios y commits deben estar en **inglés**
- Sin strings hardcodeadas — usar `DomainError` enum para mensajes de error
