-- ---------- Reglas de configuración ----------

ALTER TABLE configuracion
    DROP CONSTRAINT configuracion_dias_aviso_check,
    ADD CONSTRAINT configuracion_dias_aviso_check CHECK (dias_aviso BETWEEN 1 AND 60),
    DROP CONSTRAINT configuracion_dias_baja_check,
    ADD CONSTRAINT configuracion_dias_baja_check CHECK (dias_baja_automatica BETWEEN 1 AND 365),
    ALTER COLUMN leyenda_recibo TYPE varchar(160);

ALTER TABLE folio_secuencia
    ADD CONSTRAINT folio_secuencia_prefijo_check CHECK (prefijo ~ '^[A-Z0-9-]{1,8}$');

ALTER TABLE metodo_pago DROP CONSTRAINT metodo_pago_nombre_key;
CREATE UNIQUE INDEX metodo_pago_nombre_key ON metodo_pago (lower(nombre));

-- ---------- Datos iniciales ----------

INSERT INTO configuracion (nombre_gimnasio) VALUES ('Mi gimnasio');

INSERT INTO folio_secuencia (serie, prefijo) VALUES ('COBRO', 'GYM-');

INSERT INTO metodo_pago (nombre, es_efectivo) VALUES
    ('Efectivo', true),
    ('Tarjeta', false),
    ('Transferencia', false);
