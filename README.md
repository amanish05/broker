# broker

## Local Development Startup

1. **Configure Environment Variables:**
   - Copy `.env` to your local project root (do not commit this file).
   - Fill in your secrets:
     ```env
     KITE_API_KEY=your_real_api_key_here
     KITE_API_SECRET=your_real_api_secret_here
     KITE_USER_ID=your_real_user_id_here
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