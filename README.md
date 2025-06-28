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
   ./start-local.sh
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

### Loading Instrument Data

An endpoint `/api/instruments/{exchange}` downloads the instrument CSV from Kite
and stores it in the configured Postgres database. Invoke it with a POST request
and provide exchanges like `nse`, `bse`, `bfo`, etc. Additional GET endpoints
`/api/instruments/exchanges` and `/api/instruments/names` expose stored
exchanges and instrument names for populating UI drop-downs.

## WebSocket Streaming

The application exposes `/api/ticker/subscribe` and `/api/ticker/disconnect` REST
endpoints which delegate to `KiteTickerService`. The service opens a WebSocket
connection to Kite using the `kite_access_token` stored in the session. Ticks
received from the stream are currently printed to the console.

Swagger documentation is available once the application is running at
`http://localhost:8080/swagger-ui.html`.
