-- ---------- Planes: referencia mensual y reglas ----------

ALTER TABLE plan_membresia
    ADD COLUMN referencia_mensual_id BIGINT REFERENCES plan_membresia (id),
    ADD CONSTRAINT plan_membresia_referencia_check CHECK (referencia_mensual_id <> id),
    DROP CONSTRAINT plan_membresia_precio_check,
    ADD CONSTRAINT plan_membresia_precio_check CHECK (precio > 0),
    DROP CONSTRAINT plan_membresia_duracion_check,
    ADD CONSTRAINT plan_membresia_duracion_check CHECK (duracion_dias BETWEEN 1 AND 730);

ALTER TABLE plan_membresia DROP CONSTRAINT plan_membresia_nombre_key;
CREATE UNIQUE INDEX plan_membresia_nombre_key ON plan_membresia (LOWER(nombre));
