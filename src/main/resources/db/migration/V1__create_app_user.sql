-- "user" es palabra reservada en PostgreSQL.
CREATE TABLE app_user (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name          TEXT    NOT NULL CHECK (btrim(name) <> ''),
    username      TEXT    NOT NULL CHECK (btrim(username) <> ''),
    password_hash TEXT    NOT NULL,
    role          TEXT    NOT NULL CHECK (role IN ('ADMINISTRADOR', 'RECEPCION', 'CAJA')),
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT app_user_username_key UNIQUE (username)
);
