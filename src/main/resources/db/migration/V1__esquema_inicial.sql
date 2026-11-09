CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TYPE rol_usuario AS ENUM ('administrador', 'recepcion', 'caja');
CREATE TYPE tipo_movimiento AS ENUM ('entrada', 'salida', 'ajuste');

-- ---------- Configuración ----------

CREATE TABLE configuracion (
    id                   smallint     PRIMARY KEY DEFAULT 1,
    nombre_gimnasio      varchar(100) NOT NULL,
    direccion            varchar(200),
    telefono             varchar(30),
    logotipo             bytea,
    leyenda_recibo       varchar(200),
    moneda               char(3)      NOT NULL DEFAULT 'MXN',
    dias_aviso           integer      NOT NULL DEFAULT 7,
    dias_baja_automatica integer      NOT NULL DEFAULT 30,
    CONSTRAINT configuracion_una_fila CHECK (id = 1),
    CONSTRAINT configuracion_dias_aviso_check CHECK (dias_aviso >= 0),
    CONSTRAINT configuracion_dias_baja_check CHECK (dias_baja_automatica > 0)
);

CREATE TABLE folio_secuencia (
    serie   varchar(10) PRIMARY KEY,
    prefijo varchar(20) NOT NULL,
    ultimo  bigint      NOT NULL DEFAULT 0,
    ancho   smallint    NOT NULL DEFAULT 4
);

-- ---------- Usuarios ----------

CREATE TABLE usuario (
    id              bigint       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre          varchar(100) NOT NULL,
    nombre_usuario  varchar(50)  NOT NULL,
    contrasena_hash varchar(255) NOT NULL,
    rol             rol_usuario  NOT NULL,
    activo          boolean      NOT NULL DEFAULT true
);
CREATE UNIQUE INDEX usuario_nombre_usuario_key ON usuario (lower(nombre_usuario));

-- ---------- Catálogos ----------

CREATE TABLE plan_membresia (
    id            bigint        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre        varchar(60)   NOT NULL UNIQUE,
    duracion_dias integer       NOT NULL,
    precio        numeric(12,2) NOT NULL,
    activo        boolean       NOT NULL DEFAULT true,
    CONSTRAINT plan_membresia_duracion_check CHECK (duracion_dias > 0),
    CONSTRAINT plan_membresia_precio_check CHECK (precio >= 0)
);

CREATE TABLE metodo_pago (
    id          bigint      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre      varchar(40) NOT NULL UNIQUE,
    es_efectivo boolean     NOT NULL DEFAULT false,
    activo      boolean     NOT NULL DEFAULT true
);

CREATE TABLE motivo_movimiento (
    id         bigint      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre     varchar(60) NOT NULL UNIQUE,
    es_sistema boolean     NOT NULL DEFAULT false,
    activo     boolean     NOT NULL DEFAULT true
);

CREATE TABLE categoria_producto (
    id     bigint      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre varchar(60) NOT NULL UNIQUE,
    activo boolean     NOT NULL DEFAULT true
);

-- ---------- Socios y membresías ----------

CREATE TABLE socio (
    id         bigint       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre     varchar(100) NOT NULL,
    telefono   char(10)     NOT NULL,
    correo     varchar(120),
    fecha_alta date         NOT NULL,
    CONSTRAINT socio_telefono_check CHECK (telefono ~ '^[0-9]{10}$')
);
CREATE UNIQUE INDEX socio_correo_key ON socio (lower(correo)) WHERE correo IS NOT NULL;
CREATE INDEX socio_nombre_trgm_idx ON socio USING gin (nombre gin_trgm_ops);
CREATE INDEX socio_telefono_idx ON socio (telefono);

CREATE TABLE pago (
    id                bigint        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    folio             varchar(30)   NOT NULL UNIQUE,
    socio_id          bigint        NOT NULL REFERENCES socio (id),
    usuario_id        bigint        NOT NULL REFERENCES usuario (id),
    metodo_pago_id    bigint        NOT NULL REFERENCES metodo_pago (id),
    monto             numeric(12,2) NOT NULL,
    efectivo_recibido numeric(12,2),
    fecha_hora        timestamptz   NOT NULL DEFAULT now(),
    CONSTRAINT pago_id_socio_key UNIQUE (id, socio_id),
    CONSTRAINT pago_monto_check CHECK (monto >= 0),
    CONSTRAINT pago_efectivo_check CHECK (efectivo_recibido >= monto)
);
CREATE INDEX pago_socio_fecha_idx ON pago (socio_id, fecha_hora);
CREATE INDEX pago_fecha_idx ON pago (fecha_hora);

CREATE TABLE membresia (
    id                bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    socio_id          bigint NOT NULL,
    plan_id           bigint NOT NULL REFERENCES plan_membresia (id),
    pago_id           bigint NOT NULL UNIQUE,
    fecha_inicio      date   NOT NULL,
    fecha_vencimiento date   NOT NULL,
    CONSTRAINT membresia_vigencia_check CHECK (fecha_vencimiento > fecha_inicio),
    CONSTRAINT membresia_pago_fkey FOREIGN KEY (pago_id, socio_id) REFERENCES pago (id, socio_id)
);
CREATE INDEX membresia_socio_vencimiento_idx ON membresia (socio_id, fecha_vencimiento);

-- ---------- Productos, ventas e inventario ----------

CREATE TABLE producto (
    id           bigint        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    clave        varchar(20)   NOT NULL,
    nombre       varchar(100)  NOT NULL,
    categoria_id bigint        NOT NULL REFERENCES categoria_producto (id),
    precio       numeric(12,2) NOT NULL,
    minimo       integer       NOT NULL DEFAULT 0,
    existencia   integer       NOT NULL DEFAULT 0,
    activo       boolean       NOT NULL DEFAULT true,
    CONSTRAINT producto_precio_check CHECK (precio >= 0),
    CONSTRAINT producto_minimo_check CHECK (minimo >= 0)
);
CREATE UNIQUE INDEX producto_clave_key ON producto (lower(clave));
CREATE INDEX producto_nombre_idx ON producto (nombre);

CREATE TABLE venta (
    id                bigint        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    folio             varchar(30)   NOT NULL UNIQUE,
    fecha_hora        timestamptz   NOT NULL DEFAULT now(),
    metodo_pago_id    bigint        NOT NULL REFERENCES metodo_pago (id),
    total             numeric(12,2) NOT NULL,
    efectivo_recibido numeric(12,2),
    usuario_id        bigint        NOT NULL REFERENCES usuario (id),
    CONSTRAINT venta_total_check CHECK (total >= 0),
    CONSTRAINT venta_efectivo_check CHECK (efectivo_recibido >= total)
);
CREATE INDEX venta_fecha_idx ON venta (fecha_hora);

CREATE TABLE detalle_venta (
    id              bigint        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    venta_id        bigint        NOT NULL REFERENCES venta (id),
    producto_id     bigint        NOT NULL REFERENCES producto (id),
    cantidad        integer       NOT NULL,
    precio_unitario numeric(12,2) NOT NULL,
    importe         numeric(12,2) GENERATED ALWAYS AS (cantidad * precio_unitario) STORED,
    CONSTRAINT detalle_venta_producto_key UNIQUE (venta_id, producto_id),
    CONSTRAINT detalle_venta_cantidad_check CHECK (cantidad > 0)
);

CREATE TABLE movimiento_inventario (
    id                    bigint          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fecha_hora            timestamptz     NOT NULL DEFAULT now(),
    producto_id           bigint          NOT NULL REFERENCES producto (id),
    tipo                  tipo_movimiento NOT NULL,
    cantidad              integer         NOT NULL,
    existencia_resultante integer         NOT NULL,
    motivo_movimiento_id  bigint          REFERENCES motivo_movimiento (id),
    detalle_venta_id      bigint          REFERENCES detalle_venta (id),
    usuario_id            bigint          NOT NULL REFERENCES usuario (id),
    CONSTRAINT movimiento_cantidad_check CHECK (
        cantidad <> 0
        AND (tipo <> 'entrada' OR cantidad > 0)
        AND (tipo <> 'salida' OR cantidad < 0)),
    CONSTRAINT movimiento_origen_check CHECK (motivo_movimiento_id IS NOT NULL OR detalle_venta_id IS NOT NULL)
);
CREATE INDEX movimiento_producto_fecha_idx ON movimiento_inventario (producto_id, fecha_hora);
