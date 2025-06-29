# Agent Instructions

This repository hosts an algorithmic trading application using Spring Boot and Zerodha's Kite Connect API.

## Project Overview
- Authenticate with Kite Connect and store the access token in the session.
- REST endpoints under `/api` refresh instrument lists, place orders, list portfolio holdings, and manage ticker subscriptions.
- Instrument details, active subscriptions and placed orders are persisted in Postgres/H2.
- A WebSocket endpoint streams live ticks from the broker to the browser.
- `home.html` and the JavaScript modules in `src/main/resources/static/js` provide the UI for authentication, instrument refresh, subscribing tokens and placing orders.

## Development Conventions
- Use Lombok annotations for getters, setters, constructors and logging to reduce boilerplate.
- Reuse constants from `ApiConstants` and `KiteConstants` instead of hard coded strings.
- Log key events with INFO, DEBUG and ERROR levels. Unexpected exceptions are handled by `GlobalExceptionHandler`.

## Running Locally
1. Copy `.env` and fill in Kite credentials and database settings.
2. Start the app with `./start-local.sh`.
3. Open `http://localhost:8080/home` in the browser.

## Required Checks
Always run unit tests before committing:

```bash
./gradlew test --no-daemon
```
