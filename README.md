# broker

## Local Development Startup

1. **Configure Environment Variables:**
   - Copy `.env` to your local project root (do not commit this file).
 - Fill in your secrets:
    ```env
    KITE_API_KEY=your_real_api_key_here
    KITE_API_SECRET=your_real_api_secret_here
    KITE_USER_ID=your_real_user_id_here
    POSTGRES_URL=jdbc:postgresql://localhost:5432/broker
    POSTGRES_USER=postgres
    POSTGRES_PASSWORD=your_db_password
    ```

2. **Install Dependencies & Build:**
   ```zsh
   ./gradlew build
   ```

3. **Start the Application (loads .env automatically):**
   ```zsh
   ./start.sh
   ```
   This script loads environment variables from `.env` and starts the Spring Boot server.

4. **Access the Application:**
   - App: [http://localhost:8080/home](http://localhost:8080/home)
   - Actuator: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## Security Notes
- Never commit your real `.env` or secrets to version control.
- Use `application.properties` for non-secret config, and environment variables for secrets.

---

## Useful Commands
- **Run all tests:**
  ```zsh
  ./gradlew test
  ```
- **Build JAR:**
  ```zsh
  ./gradlew bootJar
  ```
- **Run from JAR:**
  ```zsh
  java -jar build/libs/broker-service.jar
  ```

---

For more details, see the comments in `application.properties` and `.env.example` (if provided).

## API Endpoints (Updated December 2024)

### V2 API (Recommended - Reactive Router-Based)

**Instrument Data Management:**
- `POST /api/v2/instruments/refresh` - Refresh all instrument data from broker API
- `POST /api/v2/instruments/refresh/{exchange}` - Refresh specific exchange data
- `GET /api/v2/instruments/metadata/exchanges` - Get available exchanges
- `GET /api/v2/instruments/metadata/types/{exchange}` - Get instrument types for exchange
- `GET /api/v2/instruments/metadata/underlyings` - Get underlying assets (NIFTY, BANKNIFTY, etc.)
- `GET /api/v2/instruments/metadata/expiries/{underlying}` - Get expiry dates for underlying

**Filtered Instrument Queries:**
- `GET /api/v2/instruments/all` - Get all instruments
- `GET /api/v2/instruments/exchange/{exchange}` - Get instruments by exchange
- `GET /api/v2/instruments/underlying/{underlying}` - Get instruments by underlying asset
- `GET /api/v2/instruments/underlying/{underlying}/expiry/{expiry}` - Get instruments by underlying and expiry
- `GET /api/v2/instruments/names/{exchange}/{type}` - Get instrument names for exchange/type

### Legacy API (Backward Compatible)
- `POST /api/instruments/{exchange}` - Load instruments for exchange
- `GET /api/instruments/exchanges` - Get exchanges
- `GET /api/instruments/types?exchange=NSE` - Get instrument types
- `GET /api/instruments/names?exchange=NSE&type=EQ` - Get instrument names
- `GET /api/instruments/underlyings` - Get underlying assets
- `GET /api/instruments/expiry-dates?underlying=NIFTY` - Get expiry dates

### WebSocket Streaming

**Real-time Data Streaming:**
- `WS /ws/instruments` - Real-time instrument data and filtering
- `WS /ws/ticker` - Market ticker data streaming

**REST Endpoints for Ticker:**
- `POST /api/ticker/subscribe` - Subscribe to instrument price feeds
- `GET /api/ticker/subscriptions` - List active subscriptions
- `POST /api/ticker/disconnect` - Disconnect ticker service

### Trading Operations

**Order Management:**
- `POST /api/orders` - Place trading orders
- `GET /api/orders` - List order history

**Portfolio:**
- Portfolio holdings and positions (integrated with UI)

### Usage Examples

```bash
# Refresh all instruments (V2 API - Recommended)
curl -X POST http://localhost:8080/api/v2/instruments/refresh

# Get available exchanges
curl http://localhost:8080/api/v2/instruments/metadata/exchanges

# Get NIFTY instruments
curl http://localhost:8080/api/v2/instruments/underlying/NIFTY

# Get NIFTY expiry dates
curl http://localhost:8080/api/v2/instruments/metadata/expiries/NIFTY

# Get NIFTY instruments for specific expiry
curl http://localhost:8080/api/v2/instruments/underlying/NIFTY/expiry/2024-12-26

# Legacy endpoints (still work)
curl http://localhost:8080/api/instruments/exchanges
curl -X POST http://localhost:8080/api/instruments/NSE

# Place an order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "tradingsymbol": "RELIANCE",
    "instrumentToken": 738561,
    "exchange": "NSE",
    "quantity": 1,
    "price": 2500.00,
    "transactionType": "BUY",
    "orderType": "LIMIT",
    "product": "MIS"
  }'

# Subscribe to ticker data
curl -X POST http://localhost:8080/api/ticker/subscribe \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "tokens=738561,1270529"
```

### WebSocket Examples

**Connect to instrument data stream:**
```javascript
const ws = new WebSocket('ws://localhost:8080/ws/instruments');

// Subscribe to NIFTY instruments
ws.send(JSON.stringify({
  action: 'subscribe_instruments',
  filterType: 'underlying',
  filterValue: 'NIFTY'
}));

// Real-time search
ws.send(JSON.stringify({
  action: 'filter_instruments',
  query: '25000 CALL',
  exchange: 'NSE'
}));
```

### API Documentation

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI Docs**: `http://localhost:8080/v3/api-docs`
- **Health Check**: `http://localhost:8080/actuator/health`

#### API Groups in Swagger UI:
- **All APIs** - Complete API documentation
- **V2 Instruments** - Modern reactive instrument endpoints
- **Legacy Instruments** - Backward compatible endpoints
- **Trading** - Order and portfolio management
- **Ticker** - Real-time market data
- **Authentication** - Kite Connect OAuth

#### Featured Endpoints:
- V2 reactive endpoints with advanced filtering
- WebSocket connection documentation
- Legacy API compatibility reference
- Complete request/response examples
