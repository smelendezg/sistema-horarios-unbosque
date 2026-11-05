# Plan de pruebas y gestión de riesgos

## 1. Objetivo y alcance

Validar que el Sistema de Programación de Horarios Académicos de la Universidad El Bosque permita administrar información académica, proyectar secciones desde la matrícula, construir horarios sin conflictos, aprobarlos, publicarlos y consultarlos según el rol. El alcance incluye API, interfaz web, reglas de negocio, seguridad, persistencia en Neon PostgreSQL y exportaciones.

## 2. Estrategia de pruebas

Se aplica una estrategia incremental basada en riesgo:

1. **Pruebas unitarias:** entidades, reglas Strategy, validaciones y transiciones.
2. **Pruebas de servicios/controladores:** autenticación, usuarios, matrícula, aprobación y exportación.
3. **Pruebas de interfaz:** Playwright simula los roles y los recorridos principales.
4. **Prueba manual de usabilidad:** un tercero mide interacciones y comprensión del constructor.
5. **Regresión:** Maven y Playwright se ejecutan antes de cada demostración.

Ambientes: JDK 26 ejecutando bytecode Java 21, Node/Vite, Chrome Playwright y PostgreSQL Neon con SSL.

## 3. Criterios

- Entrada: build exitoso, datos de prueba disponibles, variables de entorno cargadas y Neon accesible.
- Aprobación: 100 % de pruebas automáticas verdes, sin bugs críticos/altos abiertos y recorrido de dos épicas demostrable.
- Suspensión: base de datos inaccesible, migración fallida, credenciales inválidas o error que impida iniciar sesión.
- Reanudación: corrección aplicada, build limpio y reejecución de regresión.

## 4. Épicas e historias de usuario

### Épica 1 — Construcción inteligente del horario

- HU-01: Como administrador quiero mantener usuarios, cursos, docentes, aulas y parámetros.
- HU-02: Como Dirección quiero importar matrícula para proyectar la cantidad de secciones.
- HU-03: Como Comité de Área quiero ubicar y mover sesiones recibiendo validaciones inmediatas.
- HU-04: Como usuario académico quiero exportar el horario en PDF y Excel.

### Épica 2 — Revisión, publicación y consulta

- HU-05: Como Comité de Área quiero enviar el borrador a revisión.
- HU-06: Como Comité Táctico quiero aprobar o devolver con observaciones.
- HU-07: Como Dirección quiero publicar únicamente un horario aprobado.
- HU-08: Como estudiante o docente quiero consultar exclusivamente la versión publicada.

La trazabilidad detallada CP-01–CP-40 está en `plan-pruebas-implementado.md` y el archivo importable al gestor en `kanban-casos-prueba.csv`.

## 5. Categorización de bugs

| Severidad | Definición | Tiempo objetivo |
|---|---|---|
| Crítica | Sistema indisponible, pérdida de datos o acceso sin autorización | Inmediato |
| Alta | Falla de una épica o publicación de información incorrecta | Antes de 24 h |
| Media | Función usable con alternativa o dato secundario incorrecto | Antes de entregar |
| Baja | Problema visual, texto o mejora de experiencia | Próxima iteración |

Prioridad: P1 bloquea la entrega; P2 debe corregirse antes de presentar; P3 se planifica; P4 es mejora futura. La plantilla y el histórico se encuentran en `registro-bugs.md`.

## 6. Herramientas metodológicas

- Historias de usuario y criterios de aceptación para delimitar comportamiento.
- Partición de equivalencia para entradas válidas e inválidas.
- Valores límite para demanda 40, 44 y 45, capacidad y frecuencia semanal.
- Tabla de decisión para estados BORRADOR, EN_REVISION, APROBADO y PUBLICADO.
- Pruebas basadas en riesgo para seguridad, publicación y colisiones.
- Matriz de trazabilidad HU–CP–evidencia.
- Kanban para visualizar pendiente, ejecución, aprobado, fallido y corregido.

## 7. Registro de riesgos

Escala: probabilidad e impacto de 1 a 5. Nivel = P × I. Bajo 1–4, medio 5–9, alto 10–16, crítico 17–25.

| ID | Riesgo | P | I | Nivel | Respuesta | Responsable |
|---|---|---:|---:|---:|---|---|
| R-01 | Neon no disponible durante la demostración | 3 | 5 | 15 Alto | Mitigar: verificar conexión y conservar capturas/video | Equipo técnico |
| R-02 | Exposición accidental del `.env` | 2 | 5 | 10 Alto | Evitar: `.gitignore`, rotar contraseña y no compartir pantalla del archivo | Líder técnico |
| R-03 | Migración Flyway incompatible con datos existentes | 2 | 5 | 10 Alto | Mitigar: migraciones nuevas, respaldo y prueba previa | Backend |
| R-04 | Conflicto académico no detectado | 2 | 5 | 10 Alto | Mitigar: motor de reglas y regresión CP-20–CP-27 | QA |
| R-05 | CSV de matrícula con formato incorrecto | 3 | 3 | 9 Medio | Mitigar: validación completa y plantilla oficial | Dirección |
| R-06 | Falta de tiempo en presentación | 3 | 4 | 12 Alto | Mitigar: guion cronometrado y datos preparados | Expositores |
| R-07 | Puerto 8080 o 5173 ocupado | 3 | 2 | 6 Medio | Mitigar: comprobar puertos y cerrar proceso previo | Equipo técnico |
| R-08 | Usuario no comprende arrastrar y soltar | 2 | 3 | 6 Medio | Mitigar: instrucción visible y prueba con tercero | UX |

El mapa visual está en `mapa-calor-riesgos.svg`.

## 8. Análisis cuantitativo y reserva

Valor monetario esperado (VME) = probabilidad porcentual × costo de impacto.

| Riesgo | Prob. | Impacto estimado | VME |
|---|---:|---:|---:|
| R-01 caída de Neon | 30 % | $150.000 | $45.000 |
| R-03 migración fallida | 20 % | $200.000 | $40.000 |
| R-04 conflicto no detectado | 20 % | $250.000 | $50.000 |
| R-06 demora en exposición | 30 % | $100.000 | $30.000 |
| **Reserva recomendada** | | | **$165.000 COP** |

La reserva representa tiempo/soporte académico estimado, no un pago obligatorio.

## 9. Estándares aplicados

- **ISO/IEC/IEEE 29119:** organización del plan, diseño de casos, ejecución y evidencia.
- **ISO/IEC 25010:** seguridad, adecuación funcional, usabilidad, rendimiento y mantenibilidad.
- **OWASP:** contraseñas BCrypt, JWT, autorización RBAC y mensajes de autenticación sin revelar datos.
- **WCAG 2.1:** etiquetas, contraste, navegación por teclado y estados visuales acompañados de texto.
- **REST:** recursos y verbos HTTP coherentes para usuarios, matrícula, horarios y disponibilidad.
- **Flyway:** evolución versionada y reproducible del esquema de Neon.

## 10. Arquitectura y patrones

- React + TypeScript: interfaz por componentes y validación temprana de tipos.
- Spring Boot: API REST, Bean Validation, seguridad y transacciones.
- PostgreSQL/Neon: integridad relacional, restricciones e índices.
- Repository: separación de persistencia.
- Service: reglas de aplicación y transacciones.
- Strategy: reglas de horario independientes y extensibles.
- State machine: transiciones explícitas del horario.
- DTO: contratos controlados sin exponer entidades sensibles.

Flujo: navegador → React → API JWT → controladores → servicios/motor de reglas → repositorios → Neon PostgreSQL.

## 11. Costos

| Concepto | Base | Costo estimado |
|---|---|---:|
| Neon Free Plan | Prototipo académico | $0 |
| React, Spring Boot, PostgreSQL, Playwright | Código abierto | $0 |
| Desarrollo | 4 personas × 20 h × $15.000 | $1.200.000 |
| Pruebas y documentación | 4 personas × 8 h × $15.000 | $480.000 |
| Reserva de riesgos | VME | $165.000 |
| **Total académico estimado** | | **$1.845.000 COP** |

## 12. Evidencias y cierre

- Maven: `backend/target/surefire-reports/`.
- Playwright: salida de `npm test` y `frontend/test-results/` si ocurre un fallo.
- Build React: salida de `npm run build`.
- Evidencia humana: completar `evidencia-prueba-tercero.md` y adjuntar video.
- Demostración: seguir `guion-demostracion.md`.

El equipo acepta la versión cuando la regresión queda verde, el tercero completa CP-18 y no existen bugs críticos o altos abiertos.
