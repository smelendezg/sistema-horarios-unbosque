# Evidencia de implementación del plan de pruebas

Este archivo enlaza los casos CP-01 a CP-40 del plan entregado con las pruebas ejecutables del prototipo. Se evita declarar como aprobada una función que todavía no existe.

## Resumen

- **Con cobertura automática total o parcial:** 39 casos.
- **Con complemento manual:** 2 casos (CP-18 y la validación visual de CP-26).
- **Pendientes funcionales:** 0 casos.
- **Capas cubiertas:** dominio, servicios, controladores, validación, seguridad declarativa, exportación y UI web.

## Matriz de trazabilidad

| Caso | Alcance | Estado | Evidencia |
|---|---|---|---|
| CP-01 | Inicio de sesión válido | Automatizado | `AuthAndValidationTest`, `login.spec.ts` |
| CP-02 | Credenciales inválidas | Automatizado | `AuthAndValidationTest`, `login.spec.ts` |
| CP-03 | Acceso por rol | Automatizado | `AuthAndValidationTest`, `roles-and-workflow.spec.ts` |
| CP-04 | Crear usuario | Automatizado | `MissingCasesTest`, `roles-and-workflow.spec.ts` |
| CP-05 | Cambiar rol de usuario | Automatizado | `MissingCasesTest`, `roles-and-workflow.spec.ts` |
| CP-06 | Crear docente y disponibilidad | Automatizado | `MissingCasesTest`, `roles-and-workflow.spec.ts` |
| CP-07 | Crear aula | Automatizado | `MasterDataAndSuggestionTest` |
| CP-08 | Actualizar curso | Automatizado | `MasterDataAndSuggestionTest` |
| CP-09 | Capacidad inválida | Automatizado | `AuthAndValidationTest` |
| CP-10 | Cambiar tamaño objetivo | Automatizado | `MasterDataAndSuggestionTest` |
| CP-11 | Frecuencia inválida | Automatizado | `AuthAndValidationTest` |
| CP-12 | Sugerir secciones con tamaño 35 | Automatizado | `MasterDataAndSuggestionTest` |
| CP-13 | Demanda 40 | Automatizado | `MasterDataAndSuggestionTest` |
| CP-14 | Demanda 44 con tolerancia | Automatizado | `MasterDataAndSuggestionTest` |
| CP-15 | Demanda 45 | Automatizado | `MasterDataAndSuggestionTest` |
| CP-16 | Sugerencia usando matrícula importada | Automatizado | `MissingCasesTest`, `roles-and-workflow.spec.ts` |
| CP-17 | Mover sesión | Automatizado | `SchedulingRulesTest` |
| CP-18 | Máximo de interacciones de arrastre | Manual | Medición durante la simulación de usuario |
| CP-19 | Recalcular en menos de 2 segundos | Automatizado | `SchedulingRulesTest` |
| CP-20 | Bloqueo 12:00–14:00 | Automatizado | `InstitutionalSlotRuleTest` |
| CP-21 | Bloqueo sábado desde 13:00 | Automatizado | `InstitutionalSlotRuleTest` |
| CP-22 | Choque de docente | Automatizado | `SchedulingRulesTest` |
| CP-23 | Capacidad insuficiente | Automatizado | `SchedulingRulesTest` |
| CP-24 | Disponibilidad docente | Automatizado | `SchedulingRulesTest` |
| CP-25 | Docente no habilitado | Automatizado | `SchedulingRulesTest` |
| CP-26 | Conflictos simultáneos | Automatizado en reglas / visual manual | `SchedulingRulesTest`; el color rojo se valida en demostración |
| CP-27 | Advertencia por días consecutivos | Automatizado | `SchedulingRulesTest` |
| CP-28 | Enviar a revisión | Automatizado | `ScheduleWorkflowTest` |
| CP-29 | Transición inválida | Automatizado | `ScheduleWorkflowTest` |
| CP-30 | Aprobar | Automatizado | `ScheduleWorkflowTest`, `roles-and-workflow.spec.ts` |
| CP-31 | Devolver con observaciones | Automatizado | `ScheduleWorkflowTest` |
| CP-32 | Publicar | Automatizado | `ScheduleWorkflowTest`, `roles-and-workflow.spec.ts` |
| CP-33 | Publicación inválida | Automatizado | `ScheduleWorkflowTest` |
| CP-34 | Bloqueo de edición publicado | Automatizado | `ScheduleWorkflowTest`, `roles-and-workflow.spec.ts` |
| CP-35 | Consulta pública | Automatizado | `ScheduleWorkflowTest`, `roles-and-workflow.spec.ts` |
| CP-36 | Borrador no visible públicamente | Automatizado | `ScheduleWorkflowTest` |
| CP-37 | Importar matrícula CSV válida | Automatizado | `MissingCasesTest`, `roles-and-workflow.spec.ts` |
| CP-38 | Rechazar matrícula CSV inválida | Automatizado | `MissingCasesTest` |
| CP-39 | Exportar PDF | Automatizado | `ExportServiceTest`, `roles-and-workflow.spec.ts` |
| CP-40 | Exportar Excel | Automatizado | `ExportServiceTest`, `roles-and-workflow.spec.ts` |

## Cómo ejecutar

Backend, desde la raíz del proyecto:

```bash
cd backend
./mvnw test
```

Frontend, desde la raíz del proyecto:

```bash
cd frontend
npm test
```

Compilación completa:

```bash
cd backend && ./mvnw clean package
cd ../frontend && npm run build
```

## Evidencia para la entrega

Para cada ejecución se deben conservar:

1. Captura del resultado de Maven y Playwright.
2. Reportes de Maven en `backend/target/surefire-reports/`.
3. Reporte de Playwright en `frontend/playwright-report/` si se habilita el reporter HTML.
4. Registro del caso en Jira, Trello o Azure con su historia de usuario, resultado esperado, resultado obtenido y evidencia.
5. Los casos manuales CP-18 y la comprobación visual de CP-26 deben grabarse durante la simulación con un tercero.
