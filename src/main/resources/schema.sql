-- NOTE: This schema file does not create the database. Please ensure the database exists before running these statements.
CREATE DATABASE "BROKER_ZERODHA_ONLINE_DB"
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'C'
    LC_CTYPE = 'C'
    LOCALE_PROVIDER = 'libc'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = False;

CREATE TABLE IF NOT EXISTS instruments (
    instrument_token BIGINT PRIMARY KEY,
    exchange_token BIGINT,
    tradingsymbol VARCHAR(50),
    name VARCHAR(255),
    last_price DOUBLE PRECISION,
    expiry DATE,
    strike DOUBLE PRECISION,
    tick_size DOUBLE PRECISION,
    lot_size INTEGER,
    instrument_type VARCHAR(20),
    segment VARCHAR(20),
    exchange VARCHAR(20)
);
