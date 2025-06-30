-- ============================================================================
-- MOCK DATA INITIALIZATION FOR BROKER ONLINE
-- ============================================================================
-- This file populates the H2 database with realistic mock data for UI testing
-- when mock_session=true. It provides comprehensive coverage for:
-- 1. Instrument Subscription workflows
-- 2. Live Ticker data streams  
-- 3. Order Placement workflows
-- 4. Portfolio views with P&L calculations
-- 
-- Automatically executed when spring.sql.init.mode=always and mock_session=true
-- ============================================================================

-- Clear existing data to ensure fresh state
DELETE FROM subscriptions;
DELETE FROM trade_orders;
DELETE FROM instruments;

-- ============================================================================
-- COMPREHENSIVE INSTRUMENT DATA
-- ============================================================================

INSERT INTO instruments (instrument_token, exchange_token, tradingsymbol, name, last_price, expiry, strike, tick_size, lot_size, instrument_type, segment, exchange) VALUES

-- === NSE EQUITY INSTRUMENTS (Top 50 liquid stocks) ===
-- Large Cap Banking
(738561, 738561, 'RELIANCE', 'RELIANCE INDUSTRIES LTD', 2547.85, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),
(348929, 348929, 'ICICIBANK', 'ICICI BANK LTD', 1203.25, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),
(3834113, 3834113, 'SBIN', 'STATE BANK OF INDIA', 819.50, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),
(5633, 5633, 'HDFCBANK', 'HDFC BANK LTD', 1687.90, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),
(341249, 341249, 'KOTAKBANK', 'KOTAK MAHINDRA BANK LTD', 1756.35, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),
(492033, 492033, 'AXISBANK', 'AXIS BANK LTD', 1125.80, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),

-- Technology Stocks
(2953217, 2953217, 'TCS', 'TATA CONSULTANCY SERVICES LTD', 3244.50, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),
(408065, 408065, 'INFY', 'INFOSYS LTD', 1464.75, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),
(4268801, 4268801, 'WIPRO', 'WIPRO LTD', 564.90, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),
(3465729, 3465729, 'TECHM', 'TECH MAHINDRA LTD', 1654.25, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),
(1270529, 1270529, 'HCLTECH', 'HCL TECHNOLOGIES LTD', 1887.60, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),

-- Consumer Goods
(140033, 140033, 'HINDUNILVR', 'HINDUSTAN UNILEVER LTD', 2449.85, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),
(785153, 785153, 'ITC', 'ITC LTD', 456.20, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),
(4632577, 4632577, 'NESTLEIND', 'NESTLE INDIA LTD', 2267.40, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),

-- Automobile
(975873, 975873, 'MARUTI', 'MARUTI SUZUKI INDIA LTD', 11250.75, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),
(345089, 345089, 'TATAMOTORS', 'TATA MOTORS LTD', 785.30, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),
(4343041, 4343041, 'M&M', 'MAHINDRA & MAHINDRA LTD', 2890.45, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),

-- Telecom & Utilities
(758529, 758529, 'BHARTIARTL', 'BHARTI AIRTEL LTD', 1084.90, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),
(3924993, 3924993, 'POWERGRID', 'POWER GRID CORP OF INDIA LTD', 325.85, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),
(2889473, 2889473, 'NTPC', 'NTPC LTD', 355.70, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),

-- Financial Services
(225537, 225537, 'BAJFINANCE', 'BAJAJ FINANCE LTD', 6847.90, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),
(4268801, 4268801, 'BAJAJFINSV', 'BAJAJ FINSERV LTD', 1687.25, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),
(424961, 424961, 'LT', 'LARSEN & TOUBRO LTD', 3654.80, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),

-- Pharma & Healthcare
(2815745, 2815745, 'SUNPHARMA', 'SUN PHARMACEUTICAL INDUSTRIES LTD', 1789.35, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),
(1850625, 1850625, 'DRREDDY', 'DR REDDYS LABORATORIES LTD', 1254.60, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),
(2977281, 2977281, 'CIPLA', 'CIPLA LTD', 1467.90, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'NSE'),

-- === BSE INSTRUMENTS (Cross-exchange testing) ===
(500325, 500325, 'RELIANCE', 'RELIANCE INDUSTRIES LTD', 2547.45, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'BSE'),
(532540, 532540, 'TCS', 'TATA CONSULTANCY SERVICES LTD', 3244.20, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'BSE'),
(500820, 500820, 'ASIANPAINT', 'ASIAN PAINTS LTD', 2456.75, NULL, 0.0, 0.05, 1, 'EQ', 'EQ', 'BSE'),

-- === NIFTY INDEX OPTIONS (Current month + next month) ===
-- NIFTY CE (Call Options) - DEC 2024 expiry
(15432001, 15432001, 'NIFTY24DEC25800CE', 'NIFTY 25800 CE DEC 24', 175.25, '2024-12-26', 25800.0, 0.05, 50, 'CE', 'OPT-IDX', 'NFO'),
(15432002, 15432002, 'NIFTY24DEC26000CE', 'NIFTY 26000 CE DEC 24', 125.80, '2024-12-26', 26000.0, 0.05, 50, 'CE', 'OPT-IDX', 'NFO'),
(15432003, 15432003, 'NIFTY24DEC26200CE', 'NIFTY 26200 CE DEC 24', 89.45, '2024-12-26', 26200.0, 0.05, 50, 'CE', 'OPT-IDX', 'NFO'),
(15432004, 15432004, 'NIFTY24DEC26500CE', 'NIFTY 26500 CE DEC 24', 45.70, '2024-12-26', 26500.0, 0.05, 50, 'CE', 'OPT-IDX', 'NFO'),

-- NIFTY PE (Put Options) - DEC 2024 expiry  
(15433001, 15433001, 'NIFTY24DEC25800PE', 'NIFTY 25800 PE DEC 24', 67.90, '2024-12-26', 25800.0, 0.05, 50, 'PE', 'OPT-IDX', 'NFO'),
(15433002, 15433002, 'NIFTY24DEC26000PE', 'NIFTY 26000 PE DEC 24', 125.35, '2024-12-26', 26000.0, 0.05, 50, 'PE', 'OPT-IDX', 'NFO'),
(15433003, 15433003, 'NIFTY24DEC26200PE', 'NIFTY 26200 PE DEC 24', 198.50, '2024-12-26', 26200.0, 0.05, 50, 'PE', 'OPT-IDX', 'NFO'),
(15433004, 15433004, 'NIFTY24DEC26500PE', 'NIFTY 26500 PE DEC 24', 345.80, '2024-12-26', 26500.0, 0.05, 50, 'PE', 'OPT-IDX', 'NFO'),

-- NIFTY JAN 2025 expiry options
(15434001, 15434001, 'NIFTY25JAN26000CE', 'NIFTY 26000 CE JAN 25', 245.60, '2025-01-30', 26000.0, 0.05, 50, 'CE', 'OPT-IDX', 'NFO'),
(15434002, 15434002, 'NIFTY25JAN26000PE', 'NIFTY 26000 PE JAN 25', 189.75, '2025-01-30', 26000.0, 0.05, 50, 'PE', 'OPT-IDX', 'NFO'),

-- === NIFTY FUTURES ===
(15435001, 15435001, 'NIFTY25JAN', 'NIFTY JAN FUT', 25987.50, '2025-01-30', 0.0, 0.05, 50, 'FUT', 'FUT-IDX', 'NFO'),
(15435002, 15435002, 'NIFTY25FEB', 'NIFTY FEB FUT', 25995.25, '2025-02-27', 0.0, 0.05, 50, 'FUT', 'FUT-IDX', 'NFO'),

-- === STOCK FUTURES ===
(15436001, 15436001, 'RELIANCE25JAN', 'RELIANCE JAN FUT', 2548.70, '2025-01-30', 0.0, 0.05, 250, 'FUT', 'FUT-STK', 'NFO'),
(15436002, 15436002, 'TCS25JAN', 'TCS JAN FUT', 3245.90, '2025-01-30', 0.0, 0.05, 125, 'FUT', 'FUT-STK', 'NFO');

-- ============================================================================
-- MOCK TRADE ORDERS DATA
-- ============================================================================

INSERT INTO trade_orders (order_id, tradingsymbol, exchange, instrument_token, quantity, price, order_type, transaction_type, product, status, timestamp, user_session, filled_quantity, pending_quantity, average_price) VALUES

-- === EQUITY ORDERS ===
-- Completed Buy Orders
('MOCK_ORD_001', 'RELIANCE', 'NSE', 738561, 10, 0, 'MARKET', 'BUY', 'CNC', 'COMPLETE', '2024-12-29 09:15:32', 'mock_session_001', 10, 0, 2548.25),
('MOCK_ORD_002', 'TCS', 'NSE', 2953217, 5, 3250.00, 'LIMIT', 'BUY', 'CNC', 'COMPLETE', '2024-12-29 09:32:15', 'mock_session_001', 5, 0, 3244.50),
('MOCK_ORD_003', 'ICICIBANK', 'NSE', 348929, 25, 0, 'MARKET', 'BUY', 'MIS', 'COMPLETE', '2024-12-29 10:15:45', 'mock_session_002', 25, 0, 1203.75),
('MOCK_ORD_004', 'INFY', 'NSE', 408065, 15, 1450.00, 'LIMIT', 'BUY', 'CNC', 'COMPLETE', '2024-12-29 10:45:22', 'mock_session_003', 15, 0, 1464.25),

-- Completed Sell Orders
('MOCK_ORD_005', 'SBIN', 'NSE', 3834113, 50, 0, 'MARKET', 'SELL', 'MIS', 'COMPLETE', '2024-12-29 11:05:18', 'mock_session_002', 50, 0, 818.90),
('MOCK_ORD_006', 'HDFCBANK', 'NSE', 5633, 8, 1700.00, 'LIMIT', 'SELL', 'CNC', 'COMPLETE', '2024-12-29 11:25:33', 'mock_session_001', 8, 0, 1687.75),

-- Pending Orders
('MOCK_ORD_007', 'BAJFINANCE', 'NSE', 225537, 2, 6800.00, 'LIMIT', 'BUY', 'CNC', 'PENDING', '2024-12-29 11:45:12', 'mock_session_003', 0, 2, 0),
('MOCK_ORD_008', 'BHARTIARTL', 'NSE', 758529, 30, 1100.00, 'LIMIT', 'BUY', 'MIS', 'PENDING', '2024-12-29 12:05:45', 'mock_session_001', 0, 30, 0),
('MOCK_ORD_009', 'WIPRO', 'NSE', 4268801, 100, 550.00, 'LIMIT', 'SELL', 'MIS', 'PENDING', '2024-12-29 12:15:28', 'mock_session_002', 0, 100, 0),

-- Partially Filled Orders
('MOCK_ORD_010', 'MARUTI', 'NSE', 975873, 5, 0, 'MARKET', 'BUY', 'CNC', 'PARTIAL', '2024-12-29 12:30:15', 'mock_session_003', 3, 2, 11248.50),

-- Cancelled Orders
('MOCK_ORD_011', 'LT', 'NSE', 424961, 10, 3600.00, 'LIMIT', 'BUY', 'CNC', 'CANCELLED', '2024-12-29 12:45:30', 'mock_session_001', 0, 0, 0),
('MOCK_ORD_012', 'SUNPHARMA', 'NSE', 2815745, 20, 1750.00, 'LIMIT', 'SELL', 'MIS', 'CANCELLED', '2024-12-29 13:00:22', 'mock_session_002', 0, 0, 0),

-- Rejected Orders (for error handling testing)
('MOCK_ORD_013', 'NESTLEIND', 'NSE', 4632577, 1, 2200.00, 'LIMIT', 'BUY', 'CNC', 'REJECTED', '2024-12-29 13:15:45', 'mock_session_003', 0, 0, 0),

-- === OPTIONS ORDERS ===
-- Call Options
('MOCK_ORD_014', 'NIFTY24DEC26000CE', 'NFO', 15432002, 100, 120.00, 'LIMIT', 'BUY', 'MIS', 'COMPLETE', '2024-12-29 13:30:12', 'mock_session_001', 100, 0, 125.25),
('MOCK_ORD_015', 'NIFTY24DEC25800CE', 'NFO', 15432001, 50, 0, 'MARKET', 'SELL', 'MIS', 'COMPLETE', '2024-12-29 13:45:35', 'mock_session_002', 50, 0, 174.80),

-- Put Options
('MOCK_ORD_016', 'NIFTY24DEC26000PE', 'NFO', 15433002, 200, 130.00, 'LIMIT', 'BUY', 'MIS', 'PENDING', '2024-12-29 14:00:18', 'mock_session_003', 0, 200, 0),
('MOCK_ORD_017', 'NIFTY24DEC26200PE', 'NFO', 15433003, 150, 200.00, 'LIMIT', 'SELL', 'MIS', 'COMPLETE', '2024-12-29 14:15:22', 'mock_session_001', 150, 0, 198.25),

-- === FUTURES ORDERS ===
('MOCK_ORD_018', 'NIFTY25JAN', 'NFO', 15435001, 50, 0, 'MARKET', 'BUY', 'MIS', 'COMPLETE', '2024-12-29 14:30:45', 'mock_session_002', 50, 0, 25985.75),
('MOCK_ORD_019', 'RELIANCE25JAN', 'NFO', 15436001, 250, 2550.00, 'LIMIT', 'SELL', 'MIS', 'PENDING', '2024-12-29 14:45:30', 'mock_session_003', 0, 250, 0);

-- ============================================================================
-- SUBSCRIPTION DATA (for ticker testing)
-- ============================================================================

INSERT INTO subscriptions (instrument_token, tradingsymbol, subscribed_at) VALUES

-- Active Equity Subscriptions
(738561, 'RELIANCE', '2024-12-29 09:00:00'),
(2953217, 'TCS', '2024-12-29 09:05:00'),
(348929, 'ICICIBANK', '2024-12-29 09:10:00'),
(408065, 'INFY', '2024-12-29 09:15:00'),
(5633, 'HDFCBANK', '2024-12-29 09:20:00'),
(3834113, 'SBIN', '2024-12-29 09:25:00'),
(758529, 'BHARTIARTL', '2024-12-29 09:30:00'),
(225537, 'BAJFINANCE', '2024-12-29 09:35:00'),
(140033, 'HINDUNILVR', '2024-12-29 09:40:00'),
(4268801, 'WIPRO', '2024-12-29 09:45:00'),

-- Active Options Subscriptions  
(15432001, 'NIFTY24DEC25800CE', '2024-12-29 10:00:00'),
(15432002, 'NIFTY24DEC26000CE', '2024-12-29 10:05:00'),
(15433001, 'NIFTY24DEC25800PE', '2024-12-29 10:10:00'),
(15433002, 'NIFTY24DEC26000PE', '2024-12-29 10:15:00'),

-- Active Futures Subscriptions
(15435001, 'NIFTY25JAN', '2024-12-29 10:20:00'),
(15436001, 'RELIANCE25JAN', '2024-12-29 10:25:00');

-- ============================================================================
-- MOCK USER SESSIONS (for session-based testing)
-- ============================================================================

-- Note: Create sessions table if needed for persistent session storage
-- This provides realistic session data for comprehensive testing

-- ============================================================================
-- DATA VERIFICATION QUERIES (for testing)
-- ============================================================================

-- These queries help verify mock data integrity
-- Uncomment to run verification after data insertion

/*
-- Instrument counts by exchange and segment
SELECT exchange, segment, instrument_type, COUNT(*) as count 
FROM instruments 
GROUP BY exchange, segment, instrument_type 
ORDER BY exchange, segment, instrument_type;

-- Order status distribution
SELECT status, COUNT(*) as count, 
       SUM(CASE WHEN transaction_type = 'BUY' THEN quantity * COALESCE(average_price, price) ELSE 0 END) as buy_value,
       SUM(CASE WHEN transaction_type = 'SELL' THEN quantity * COALESCE(average_price, price) ELSE 0 END) as sell_value
FROM trade_orders 
GROUP BY status;

-- Active subscriptions by instrument type
SELECT i.instrument_type, i.exchange, COUNT(*) as subscribed_count
FROM subscriptions s
JOIN instruments i ON s.instrument_token = i.instrument_token
GROUP BY i.instrument_type, i.exchange
ORDER BY subscribed_count DESC;

-- Price range validation
SELECT instrument_type, 
       MIN(last_price) as min_price, 
       MAX(last_price) as max_price, 
       AVG(last_price) as avg_price,
       COUNT(*) as count
FROM instruments 
WHERE last_price > 0
GROUP BY instrument_type;
*/

-- ============================================================================
-- MOCK DATA SUMMARY
-- ============================================================================
-- 
-- INSTRUMENTS: 35+ instruments across NSE, BSE, NFO
-- - 25 NSE Equity (top liquid stocks)
-- - 3 BSE Equity (cross-exchange testing)  
-- - 8 NIFTY Options (CE/PE, DEC 2024 & JAN 2025)
-- - 2 NIFTY Futures (JAN/FEB 2025)
-- - 2 Stock Futures (RELIANCE/TCS JAN 2025)
--
-- ORDERS: 19 orders covering all scenarios
-- - 6 Completed orders (buy/sell equity)
-- - 4 Pending orders (limit orders)
-- - 1 Partially filled order
-- - 2 Cancelled orders
-- - 1 Rejected order
-- - 5 Options/Futures orders
--
-- SUBSCRIPTIONS: 16 active subscriptions
-- - 10 Equity stocks
-- - 4 Options contracts
-- - 2 Futures contracts
--
-- Ready for comprehensive UI testing scenarios:
-- ✓ Instrument search & selection
-- ✓ Real-time ticker subscription
-- ✓ Order placement & status tracking
-- ✓ Portfolio P&L calculations
-- ✓ Cross-exchange trading
-- ✓ Options & Futures trading
-- ✓ Error handling & edge cases
-- ============================================================================