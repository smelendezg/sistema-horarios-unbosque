# Universidad El Bosque · Programación de Horarios

Prototipo web de alta fidelidad para construir, validar, revisar y publicar horarios académicos. Implementa React + TypeScript, API REST Spring Boot 3 con Java 21, PostgreSQL, JWT/RBAC, administración de usuarios, disponibilidad docente personalizada, importación CSV de matrícula, motor de reglas Strategy, estados del horario, exportación PDF/XLSX, Flyway, OpenAPI y Docker.

## Usuarios de demostración

Todos usan la contraseña `Campus2026!`.

| Rol | Correo |
|---|---|
| Dirección | `direccion@unbosque.edu.co` |
| Comité de Área | `area@unbosque.edu.co` |
| Comité Táctico | `tactico@unbosque.edu.co` |
| Administrador | `admin@unbosque.edu.co` |
| Docente | `docente@unbosque.edu.co` |
| Estudiante | `estudiante@unbosque.edu.co` |

## Conectar el proyecto con Neon DB

### 1. Crear la base de datos

1. Entre a [Neon](https://console.neon.tech/) y cree una cuenta o inicie sesión.
2. Seleccione **New project**.
3. Use un nombre como `horarios-unbosque`.
4. Elija una región cercana y mantenga PostgreSQL como motor.
5. Espere a que Neon cree el proyecto.

### 2. Obtener los datos de conexión

1. Dentro del proyecto, abra **Dashboard**.
2. Pulse **Connect**.
3. Seleccione la rama `main`, la base de datos creada y el rol propietario.
4. Active **Pooled connection** para el uso normal de la aplicación.
5. Neon mostrará una dirección parecida a esta:

```text
postgresql://usuario:contraseña@ep-ejemplo-pooler.us-east-2.aws.neon.tech/neondb?sslmode=require
```

No publique esa dirección ni la contraseña en GitHub.

### 3. Crear el archivo `.env`

En la raíz del proyecto, copie `.env.example` y llame a la copia `.env`:

```bash
cd ~/Desktop/campus-verde-horarios
cp .env.example .env
```

Divida la dirección entregada por Neon así:

```dotenv
DATABASE_URL='jdbc:postgresql://ep-ejemplo-pooler.us-east-2.aws.neon.tech/neondb?sslmode=require'
DB_USER='usuario'
DB_PASSWORD='contraseña'
JWT_SECRET='una-clave-privada-larga-de-al-menos-32-caracteres'
CORS_ORIGIN=http://localhost:5173
VITE_API_URL=http://localhost:8080/api
```

Importante: Spring necesita que la URL comience con `jdbc:postgresql://`. La URL original de Neon comienza con `postgresql://`, así que debe agregar `jdbc:` al inicio y retirar de la URL el usuario y la contraseña porque ya están en `DB_USER` y `DB_PASSWORD`.

### 4. Cargar las variables en la terminal

En macOS, desde la raíz del proyecto:

```bash
set -a
source .env
set +a
```

Estas variables solo quedan cargadas en esa terminal. El archivo `.env` está ignorado por Git.

### 5. Iniciar el backend

```bash
cd backend
./mvnw spring-boot:run
```

Durante el primer inicio, Flyway ejecutará automáticamente:

- `V1__esquema_inicial.sql`, que crea tablas, índices, relaciones y parámetros.
- `V2__correos_universidad_el_bosque.sql`, que actualiza datos antiguos al dominio `@unbosque.edu.co`.
- `V3__actualizar_nombre_pepita.sql`, que actualiza el usuario de demostración del Comité de Área.
- `V4__matricula_proyectada.sql`, que crea la demanda por curso y periodo para importar matrícula.
- La carga de usuarios, docentes, cursos, aulas, secciones y periodo de demostración.

Compruebe el backend en:

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html

### 6. Iniciar el frontend

Abra otra terminal:

```bash
cd ~/Desktop/campus-verde-horarios/frontend
npm install
npm run dev
```

Abra http://localhost:5173 e ingrese con uno de los usuarios de demostración.

### 7. Verificar Neon

En la consola de Neon, abra **SQL Editor** y ejecute:

```sql
SELECT id, nombre, correo, rol, activo
FROM usuario
ORDER BY id;
```

También puede verificar las tablas creadas:

```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;
```

## Inicio alternativo con Docker

El `docker-compose.yml` incluido inicia una base PostgreSQL local, no Neon:

```bash
docker compose up --build
```

- Aplicación: http://localhost:5173
- Swagger: http://localhost:8080/swagger-ui.html

Para probar específicamente Neon, use el procedimiento con `.env` descrito arriba.

## Flujo demostrable

1. Ingrese como Comité de Área.
2. Arrastre una sección a la grilla semanal.
3. Observe la validación de franja, aula, capacidad, docente y colisiones.
4. Envíe el horario a revisión.
5. Ingrese como Comité Táctico y apruébelo o devuélvalo.
6. Ingrese como Dirección y publíquelo.
7. Consulte la versión publicada como Estudiante.
8. Como Administrador, cree usuarios y cambie roles desde **Usuarios**.
9. Importe `docs/matricula-ejemplo.csv` desde **Matrícula**.
10. Configure las franjas de un docente desde **Datos maestros → Docentes**.

## Pruebas

```bash
cd backend && ./mvnw test
cd ../frontend && npm run build && npm test
```

El backend se compila para Java 21, que es una versión LTS. También puede ejecutarlo con su JDK 26 instalado, como se validó durante el desarrollo.

Nunca suba `.env`, contraseñas, tokens o cadenas de conexión a un repositorio.
