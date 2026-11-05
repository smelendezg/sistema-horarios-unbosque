CREATE TABLE matricula_proyectada (
  id bigserial PRIMARY KEY,
  id_periodo bigint NOT NULL REFERENCES periodo_academico(id),
  id_curso bigint NOT NULL REFERENCES curso(id),
  demanda int NOT NULL CHECK (demanda >= 0),
  fecha_importacion date NOT NULL DEFAULT current_date,
  UNIQUE(id_periodo,id_curso)
);
CREATE INDEX idx_matricula_proyectada_periodo ON matricula_proyectada(id_periodo);
