CREATE TABLE rol (id_rol smallserial PRIMARY KEY, nombre varchar(40) NOT NULL UNIQUE);
CREATE TABLE usuario (
  id bigserial PRIMARY KEY, nombre varchar(120) NOT NULL, correo varchar(180) NOT NULL UNIQUE,
  hash_clave varchar(255) NOT NULL, rol varchar(30) NOT NULL, activo boolean NOT NULL DEFAULT true
);
CREATE TABLE periodo_academico (
  id bigserial PRIMARY KEY, codigo varchar(20) NOT NULL UNIQUE, fecha_inicio date NOT NULL, fecha_fin date NOT NULL
);
CREATE TABLE docente (
  id bigserial PRIMARY KEY, id_externo varchar(40) UNIQUE, nombre varchar(120) NOT NULL, correo varchar(180) NOT NULL,
  tipo_vinculacion varchar(40) NOT NULL, escalafon varchar(40) NOT NULL
);
CREATE TABLE curso (
  id bigserial PRIMARY KEY, codigo varchar(30) NOT NULL UNIQUE, nombre varchar(140) NOT NULL, creditos int NOT NULL,
  frecuencia_semanal int NOT NULL CHECK (frecuencia_semanal BETWEEN 1 AND 4),
  requiere_computadores boolean NOT NULL DEFAULT false, requiere_sillas_moviles boolean NOT NULL DEFAULT false
);
CREATE TABLE aula (
  id bigserial PRIMARY KEY, codigo varchar(30) NOT NULL UNIQUE, nombre varchar(120) NOT NULL, capacidad int NOT NULL CHECK (capacidad > 0),
  tiene_computadores boolean NOT NULL DEFAULT false, tiene_sillas_moviles boolean NOT NULL DEFAULT false
);
CREATE TABLE horario (
  id bigserial PRIMARY KEY, id_periodo bigint NOT NULL REFERENCES periodo_academico(id), version int NOT NULL DEFAULT 1,
  estado varchar(30) NOT NULL CHECK (estado IN ('BORRADOR','EN_REVISION','APROBADO','PUBLICADO')),
  fecha_creacion timestamptz NOT NULL DEFAULT now(), fecha_publicacion timestamptz,
  UNIQUE(id_periodo, version)
);
CREATE TABLE seccion (
  id bigserial PRIMARY KEY, id_curso bigint NOT NULL REFERENCES curso(id), id_periodo bigint NOT NULL REFERENCES periodo_academico(id),
  id_docente bigint REFERENCES docente(id), numero int NOT NULL, cupo_maximo int NOT NULL DEFAULT 44,
  estudiantes_estimados int NOT NULL DEFAULT 0, estado varchar(20) NOT NULL DEFAULT 'ABIERTA',
  UNIQUE(id_curso,id_periodo,numero)
);
CREATE TABLE sesion (
  id bigserial PRIMARY KEY, id_horario bigint NOT NULL REFERENCES horario(id), id_seccion bigint NOT NULL REFERENCES seccion(id),
  id_aula bigint NOT NULL REFERENCES aula(id), dia_semana smallint NOT NULL CHECK (dia_semana BETWEEN 1 AND 6),
  hora_inicio time NOT NULL, hora_fin time NOT NULL, CHECK (hora_fin > hora_inicio)
);
CREATE TABLE disponibilidad_docente (
  id bigserial PRIMARY KEY, id_docente bigint NOT NULL REFERENCES docente(id), dia_semana smallint NOT NULL,
  hora_inicio time NOT NULL, hora_fin time NOT NULL
);
CREATE TABLE docente_curso (id_docente bigint REFERENCES docente(id), id_curso bigint REFERENCES curso(id), PRIMARY KEY(id_docente,id_curso));
CREATE TABLE parametro (id bigserial PRIMARY KEY, clave varchar(80) NOT NULL UNIQUE, valor varchar(120) NOT NULL, unidad varchar(40), descripcion varchar(255));
CREATE TABLE revision_horario (
  id bigserial PRIMARY KEY, id_horario bigint NOT NULL REFERENCES horario(id), id_usuario bigint NOT NULL REFERENCES usuario(id),
  decision varchar(20) NOT NULL CHECK (decision IN ('APROBADO','DEVUELTO')), observaciones text, fecha timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE matricula_seccion (
  id bigserial PRIMARY KEY, id_seccion bigint NOT NULL REFERENCES seccion(id), inscritos int NOT NULL, fecha_corte date NOT NULL,
  fuente varchar(20) NOT NULL CHECK (fuente IN ('SIMULADA','IMPORTADA'))
);
CREATE TABLE bitacora (
  id bigserial PRIMARY KEY, id_usuario bigint REFERENCES usuario(id), id_horario bigint REFERENCES horario(id),
  entidad varchar(80) NOT NULL, accion varchar(80) NOT NULL, detalle text, fecha timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_sesion_horario_dia ON sesion(id_horario,dia_semana,hora_inicio);
CREATE INDEX idx_seccion_docente ON seccion(id_docente);
INSERT INTO parametro(clave,valor,unidad,descripcion) VALUES
 ('duracion_sesion','2','horas','Duracion fija de cada sesion'),('tamano_objetivo','40','estudiantes','Tamano recomendado'),
 ('tolerancia','10','porcentaje','Tolerancia sobre el tamano objetivo'),('matricula_minima','10','estudiantes','Minimo por seccion'),
 ('hora_inicio_semana','07:00','hora','Lunes a viernes'),('hora_fin_semana','22:00','hora','Lunes a viernes'),
 ('hora_fin_sabado','13:00','hora','Sabados');
