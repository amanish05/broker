-- NOTE: This schema file does not create the database. Please ensure the database exists before running these statements.


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

CREATE TABLE IF NOT EXISTS trade_orders (
    id BIGSERIAL PRIMARY KEY,
    instrument_token BIGINT,
    tradingsymbol VARCHAR(50),
    exchange VARCHAR(20),
    transaction_type VARCHAR(10),
    quantity INTEGER,
    price DOUBLE PRECISION,
    order_id VARCHAR(50),
    placed_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGSERIAL PRIMARY KEY,
    instrument_token BIGINT UNIQUE,
    tradingsymbol VARCHAR(50),
    subscribed_at TIMESTAMP
);

-- Ensure id columns are BIGINT for JPA/Hibernate compatibility
ALTER TABLE trade_orders ALTER COLUMN id TYPE bigint;
ALTER TABLE subscriptions ALTER COLUMN id TYPE bigint;
