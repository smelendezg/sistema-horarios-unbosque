# Registro y categorización de bugs

## Plantilla

| Campo | Contenido |
|---|---|
| ID | BUG-XX |
| Caso relacionado | CP-XX |
| Título | Descripción breve |
| Pasos | Secuencia reproducible |
| Esperado | Comportamiento correcto |
| Obtenido | Comportamiento observado |
| Severidad / prioridad | Crítica–Baja / P1–P4 |
| Estado | Nuevo, en corrección, corregido, cerrado |
| Evidencia | Captura, video o reporte |

## Histórico del prototipo

| ID | Caso | Hallazgo | Severidad | Prioridad | Estado | Corrección/evidencia |
|---|---|---|---|---|---|---|
| BUG-01 | CP-24 | Se permitía ubicar un docente fuera de disponibilidad | Alta | P1 | Cerrado | `TeacherEligibilityRule` y `SchedulingRulesTest` |
| BUG-02 | CP-14/15 | La tolerancia no se aplicaba al sugerir secciones | Alta | P1 | Cerrado | `SectionController` y pruebas de valores límite |
| BUG-03 | Ejecución | Backend fallaba si 8080 estaba ocupado | Media | P2 | Cerrado/operativo | Procedimiento de comprobación de puertos |
| BUG-04 | Flujo | No existía forma clara de iniciar otro horario publicado | Media | P2 | Cerrado | Endpoint y botón “Nuevo horario” |
| BUG-05 | CP-04/05 | No existía administración de usuarios | Alta | P1 | Cerrado | `UserController` y página Usuarios |
| BUG-06 | CP-16/37/38 | No existía importación de matrícula | Alta | P1 | Cerrado | `EnrollmentController`, V4 y página Matrícula |
| BUG-07 | CP-06 | Disponibilidad docente era fija 07:00–22:00 | Alta | P1 | Cerrado | Editor personalizado y validación de solapamientos |

No quedan bugs críticos o altos abiertos al finalizar la regresión automática.
