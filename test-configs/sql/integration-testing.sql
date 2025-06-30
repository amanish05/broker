-- Test data for Broker Online application
-- Used by CI/CD pipeline and integration tests

-- ============================================================================
-- INSTRUMENTS TEST DATA
-- ============================================================================

-- Clear existing data
DELETE FROM subscriptions;
DELETE FROM trade_orders;
DELETE FROM instruments;

-- Insert test instruments data
INSERT INTO instruments (instrument_token, tradingsymbol, exchange, segment, name, expiry, strike, tick_size, lot_size, instrument_type, last_price) VALUES
-- NSE Equity instruments
(738561, 'RELIANCE', 'NSE', 'EQ', 'RELIANCE INDUSTRIES LTD', NULL, 0, 0.05, 1, 'EQ', 2550.25),
(2953217, 'TCS', 'NSE', 'EQ', 'TATA CONSULTANCY SERVICES LTD', NULL, 0, 0.05, 1, 'EQ', 3245.75),
(408065, 'INFY', 'NSE', 'EQ', 'INFOSYS LTD', NULL, 0, 0.05, 1, 'EQ', 1465.50),
(5633, 'HDFC', 'NSE', 'EQ', 'HDFC LTD', NULL, 0, 0.05, 1, 'EQ', 2850.90),
(348929, 'ICICIBANK', 'NSE', 'EQ', 'ICICI BANK LTD', NULL, 0, 0.05, 1, 'EQ', 1205.35),
(3834113, 'SBIN', 'NSE', 'EQ', 'STATE BANK OF INDIA', NULL, 0, 0.05, 1, 'EQ', 820.45),
(225537, 'BAJFINANCE', 'NSE', 'EQ', 'BAJAJ FINANCE LTD', NULL, 0, 0.05, 1, 'EQ', 6850.25),
(4267265, 'WIPRO', 'NSE', 'EQ', 'WIPRO LTD', NULL, 0, 0.05, 1, 'EQ', 565.80),
(140033, 'HINDUNILVR', 'NSE', 'EQ', 'HINDUSTAN UNILEVER LTD', NULL, 0, 0.05, 1, 'EQ', 2450.90),
(758529, 'BHARTIARTL', 'NSE', 'EQ', 'BHARTI AIRTEL LTD', NULL, 0, 0.05, 1, 'EQ', 1085.75),

-- BSE instruments (for cross-exchange testing)
(500325, 'RELIANCE', 'BSE', 'EQ', 'RELIANCE INDUSTRIES LTD', NULL, 0, 0.05, 1, 'EQ', 2549.85),
(532540, 'TCS', 'BSE', 'EQ', 'TATA CONSULTANCY SERVICES LTD', NULL, 0, 0.05, 1, 'EQ', 3245.30),

-- NFO instruments (for derivatives testing)
(9604354, 'NIFTY24DEC26000CE', 'NFO', 'OPT-IDX', 'NIFTY 26000 CALL', '2024-12-26', 26000, 0.05, 50, 'CE', 125.50),
(9604355, 'NIFTY24DEC26000PE', 'NFO', 'OPT-IDX', 'NIFTY 26000 PUT', '2024-12-26', 26000, 0.05, 50, 'PE', 245.75),
(11000001, 'NIFTY25JAN26500FUT', 'NFO', 'FUT-IDX', 'NIFTY JAN FUT', '2025-01-30', 0, 0.05, 50, 'FUT', 26150.25);

-- ============================================================================
-- TRADE ORDERS TEST DATA
-- ============================================================================

INSERT INTO trade_orders (order_id, tradingsymbol, exchange, instrument_token, quantity, price, order_type, transaction_type, product, status, timestamp, user_session, filled_quantity, pending_quantity, average_price) VALUES
-- Completed orders
('TEST_ORDER_001', 'RELIANCE', 'NSE', 738561, 1, 0, 'MARKET', 'BUY', 'MIS', 'COMPLETE', '2024-12-29 09:15:32', 'test_session_001', 1, 0, 2551.75),
('TEST_ORDER_002', 'TCS', 'NSE', 2953217, 2, 3250.00, 'LIMIT', 'BUY', 'CNC', 'COMPLETE', '2024-12-29 09:30:15', 'test_session_001', 2, 0, 3248.50),
('TEST_ORDER_003', 'INFY', 'NSE', 408065, 5, 0, 'MARKET', 'SELL', 'MIS', 'COMPLETE', '2024-12-29 10:05:22', 'test_session_002', 5, 0, 1463.25),

-- Pending orders
('TEST_ORDER_004', 'HDFC', 'NSE', 5633, 1, 2800.00, 'LIMIT', 'BUY', 'CNC', 'PENDING', '2024-12-29 10:30:15', 'test_session_001', 0, 1, 0),
('TEST_ORDER_005', 'ICICIBANK', 'NSE', 348929, 3, 1250.00, 'LIMIT', 'SELL', 'CNC', 'PENDING', '2024-12-29 10:45:30', 'test_session_003', 0, 3, 0),

-- Cancelled orders
('TEST_ORDER_006', 'SBIN', 'NSE', 3834113, 10, 800.00, 'LIMIT', 'BUY', 'MIS', 'CANCELLED', '2024-12-29 11:00:45', 'test_session_002', 0, 0, 0),

-- Rejected orders (for error testing)
('TEST_ORDER_007', 'BAJFINANCE', 'NSE', 225537, 1, 6800.00, 'LIMIT', 'BUY', 'CNC', 'REJECTED', '2024-12-29 11:15:20', 'test_session_001', 0, 0, 0),

-- Options orders
('TEST_ORDER_008', 'NIFTY24DEC26000CE', 'NFO', 9604354, 50, 120.00, 'LIMIT', 'BUY', 'MIS', 'COMPLETE', '2024-12-29 11:30:10', 'test_session_004', 50, 0, 122.75),
('TEST_ORDER_009', 'NIFTY24DEC26000PE', 'NFO', 9604355, 100, 250.00, 'LIMIT', 'SELL', 'MIS', 'PENDING', '2024-12-29 11:45:35', 'test_session_004', 0, 100, 0);

-- ============================================================================
-- SUBSCRIPTIONS TEST DATA
-- ============================================================================

INSERT INTO subscriptions (instrument_token, user_session, created_at, is_active) VALUES
-- Active subscriptions
(738561, 'test_session_001', '2024-12-29 09:00:00', true),  -- RELIANCE
(2953217, 'test_session_001', '2024-12-29 09:05:00', true), -- TCS
(408065, 'test_session_002', '2024-12-29 09:10:00', true),  -- INFY
(5633, 'test_session_001', '2024-12-29 09:15:00', true),    -- HDFC
(348929, 'test_session_003', '2024-12-29 09:20:00', true),  -- ICICIBANK

-- Inactive subscriptions (for cleanup testing)
(3834113, 'test_session_expired', '2024-12-28 09:00:00', false), -- SBIN (expired session)
(225537, 'test_session_cancelled', '2024-12-28 10:00:00', false); -- BAJFINANCE (cancelled)

-- ============================================================================
-- USER SESSIONS TEST DATA (if you have a sessions table)
-- ============================================================================

-- Note: This assumes you might have a sessions table for testing
-- Uncomment if you implement persistent session storage

/*
CREATE TABLE IF NOT EXISTS user_sessions (
    session_id VARCHAR(100) PRIMARY KEY,
    user_id VARCHAR(50),
    access_token VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

INSERT INTO user_sessions (session_id, user_id, access_token, created_at, last_accessed_at, expires_at, is_active) VALUES
('test_session_001', 'test_user_001', 'test_valid_access_token_123456789', '2024-12-29 08:00:00', '2024-12-29 11:00:00', '2024-12-30 08:00:00', true),
('test_session_002', 'test_user_002', 'test_valid_access_token_987654321', '2024-12-29 08:30:00', '2024-12-29 11:30:00', '2024-12-30 08:30:00', true),
('test_session_003', 'test_user_003', 'test_valid_access_token_456789123', '2024-12-29 09:00:00', '2024-12-29 12:00:00', '2024-12-30 09:00:00', true),
('test_session_004', 'test_user_004', 'test_valid_access_token_789123456', '2024-12-29 09:30:00', '2024-12-29 12:30:00', '2024-12-30 09:30:00', true),
('test_session_expired', 'test_user_005', 'test_expired_access_token_111111111', '2024-12-28 08:00:00', '2024-12-28 10:00:00', '2024-12-29 08:00:00', false),
('test_session_cancelled', 'test_user_006', 'test_cancelled_access_token_222222222', '2024-12-28 09:00:00', '2024-12-28 11:00:00', '2024-12-29 09:00:00', false);
*/

-- ============================================================================
-- PORTFOLIO TEST DATA (if you have a holdings table)
-- ============================================================================

-- Note: This assumes you might have a holdings/portfolio table
-- Uncomment if you implement persistent portfolio storage

/*
CREATE TABLE IF NOT EXISTS portfolio_holdings (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(50),
    tradingsymbol VARCHAR(50),
    exchange VARCHAR(10),
    instrument_token BIGINT,
    quantity INTEGER,
    average_price DECIMAL(10,2),
    last_price DECIMAL(10,2),
    product VARCHAR(10),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO portfolio_holdings (user_id, tradingsymbol, exchange, instrument_token, quantity, average_price, last_price, product) VALUES
('test_user_001', 'RELIANCE', 'NSE', 738561, 10, 2500.75, 2550.25, 'CNC'),
('test_user_001', 'TCS', 'NSE', 2953217, 5, 3200.50, 3245.75, 'CNC'),
('test_user_001', 'INFY', 'NSE', 408065, 8, 1450.25, 1465.50, 'CNC'),
('test_user_002', 'HDFC', 'NSE', 5633, 3, 2800.00, 2850.90, 'CNC'),
('test_user_002', 'ICICIBANK', 'NSE', 348929, 15, 1180.75, 1205.35, 'CNC'),
('test_user_003', 'SBIN', 'NSE', 3834113, 25, 780.50, 820.45, 'CNC'),
('test_user_004', 'BAJFINANCE', 'NSE', 225537, 2, 6500.00, 6850.25, 'CNC');
*/

-- ============================================================================
-- TEST DATA VERIFICATION QUERIES
-- ============================================================================

-- Uncomment these queries to verify test data after insertion

/*
-- Verify instruments data
SELECT COUNT(*) as total_instruments FROM instruments;
SELECT exchange, COUNT(*) as count FROM instruments GROUP BY exchange;

-- Verify orders data  
SELECT COUNT(*) as total_orders FROM trade_orders;
SELECT status, COUNT(*) as count FROM trade_orders GROUP BY status;

-- Verify subscriptions data
SELECT COUNT(*) as total_subscriptions FROM subscriptions;
SELECT is_active, COUNT(*) as count FROM subscriptions GROUP BY is_active;

-- Sample data overview
SELECT 
    i.tradingsymbol,
    i.exchange, 
    i.last_price,
    COUNT(o.order_id) as order_count,
    COUNT(s.id) as subscription_count
FROM instruments i
LEFT JOIN trade_orders o ON i.instrument_token = o.instrument_token
LEFT JOIN subscriptions s ON i.instrument_token = s.instrument_token
GROUP BY i.tradingsymbol, i.exchange, i.last_price
ORDER BY i.tradingsymbol;
*/