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