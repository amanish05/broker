-- Simple mock data for testing
-- Clear existing data
DELETE FROM subscriptions;
DELETE FROM trade_orders;
DELETE FROM instruments;

-- Insert basic test instruments (one at a time to avoid H2 limits)
INSERT INTO instruments (instrument_token, exchange_token, tradingsymbol, name, last_price, expiry, strike, tick_size, lot_size, instrument_type, segment, exchange) VALUES
(738561, 738561, 'RELIANCE', 'RELIANCE INDUSTRIES LTD', 2547.85, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE');

INSERT INTO instruments (instrument_token, exchange_token, tradingsymbol, name, last_price, expiry, strike, tick_size, lot_size, instrument_type, segment, exchange) VALUES
(2953217, 2953217, 'TCS', 'TATA CONSULTANCY SERVICES LTD', 3244.50, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE');

INSERT INTO instruments (instrument_token, exchange_token, tradingsymbol, name, last_price, expiry, strike, tick_size, lot_size, instrument_type, segment, exchange) VALUES
(348929, 348929, 'ICICIBANK', 'ICICI BANK LTD', 1203.25, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE');

INSERT INTO instruments (instrument_token, exchange_token, tradingsymbol, name, last_price, expiry, strike, tick_size, lot_size, instrument_type, segment, exchange) VALUES
(408065, 408065, 'INFY', 'INFOSYS LTD', 1464.75, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE');

INSERT INTO instruments (instrument_token, exchange_token, tradingsymbol, name, last_price, expiry, strike, tick_size, lot_size, instrument_type, segment, exchange) VALUES
(5633, 5633, 'HDFCBANK', 'HDFC BANK LTD', 1687.90, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE');

-- BSE instruments
INSERT INTO instruments (instrument_token, exchange_token, tradingsymbol, name, last_price, expiry, strike, tick_size, lot_size, instrument_type, segment, exchange) VALUES
(500325, 500325, 'RELIANCE', 'RELIANCE INDUSTRIES LTD', 2547.45, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'BSE');

-- NFO Options
INSERT INTO instruments (instrument_token, exchange_token, tradingsymbol, name, last_price, expiry, strike, tick_size, lot_size, instrument_type, segment, exchange) VALUES
(15432002, 15432002, 'NIFTY24DEC26000CE', 'NIFTY 26000 CE DEC 24', 125.80, '2024-12-26', 26000.0, 0.05, 50, 'CE', 'OPT-IDX', 'NFO');

INSERT INTO instruments (instrument_token, exchange_token, tradingsymbol, name, last_price, expiry, strike, tick_size, lot_size, instrument_type, segment, exchange) VALUES
(15433002, 15433002, 'NIFTY24DEC26000PE', 'NIFTY 26000 PE DEC 24', 125.35, '2024-12-26', 26000.0, 0.05, 50, 'PE', 'OPT-IDX', 'NFO');

-- NFO Futures
INSERT INTO instruments (instrument_token, exchange_token, tradingsymbol, name, last_price, expiry, strike, tick_size, lot_size, instrument_type, segment, exchange) VALUES
(15435001, 15435001, 'NIFTY25JAN', 'NIFTY JAN FUT', 25987.50, '2025-01-30', 0.0, 0.05, 50, 'FUT', 'FUT-IDX', 'NFO');

-- Mock subscriptions
INSERT INTO subscriptions (instrument_token, tradingsymbol, subscribed_at) VALUES
(738561, 'RELIANCE', '2024-12-29 09:00:00');

INSERT INTO subscriptions (instrument_token, tradingsymbol, subscribed_at) VALUES
(2953217, 'TCS', '2024-12-29 09:05:00');