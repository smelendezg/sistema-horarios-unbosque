# Guion de demostración — 10 minutos

## Antes de empezar

1. Encender backend y frontend.
2. Abrir previamente login, Neon y el resultado de pruebas.
3. Tener `matricula-ejemplo.csv` disponible.
4. Confirmar que 8080 y 5173 están libres.

## Cronómetro

- 0:00–0:45 — Problema, objetivo y dos épicas.
- 0:45–2:00 — Arquitectura React–Spring–Neon, JWT, Repository, Strategy y estados.
- 2:00–3:00 — Administrador: crear usuario, cambiar rol y disponibilidad docente.
- 3:00–4:00 — Importar matrícula y mostrar sugerencia de secciones.
- 4:00–6:00 — Comité de Área: asignar sesión válida y provocar conflicto.
- 6:00–7:30 — Enviar, aprobar/devolver y mostrar historial.
- 7:30–8:30 — Dirección publica; estudiante consulta.
- 8:30–9:15 — Exportar PDF/Excel.
- 9:15–10:00 — Mostrar pruebas, riesgos, resultado del tercero y cierre.

## Inicio rápido

Backend:

```bash
cd ~/Desktop/campus-verde-horarios
set -a; source .env; set +a
cd backend
./mvnw spring-boot:run
```

Frontend:

```bash
cd ~/Desktop/campus-verde-horarios/frontend
npm run dev
```

## Plan de contingencia

Si Neon no responde, mostrar las capturas y el video. Si un puerto está ocupado, ejecutar `lsof -i :8080` o `lsof -i :5173`, cerrar el proceso anterior y reiniciar.
