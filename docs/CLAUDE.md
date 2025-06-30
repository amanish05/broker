# Broker Online - Algorithmic Trading Application

## Overview

**Broker Online** is a comprehensive Java-based algorithmic trading platform built with Spring Boot 3.1.5 that integrates with Zerodha's Kite Connect API. It provides real-time market data streaming, portfolio management, order execution, and a web-based interface for manual trading operations.

### Key Features

- **Real-time Market Data**: WebSocket integration for live price feeds
- **Order Management**: Place BUY/SELL orders with multiple product types (MIS, CNC, NRML)
- **Portfolio Tracking**: View holdings and positions
- **Instrument Discovery**: Search and subscribe to trading instruments
- **Web Interface**: Complete trading workflow via browser
- **REST API**: Full API for programmatic access (future MCP server integration)

## Technology Stack

### Backend
- **Java 17** with **Spring Boot 3.1.5**
- **Spring Web** - REST API endpoints
- **Spring WebFlux** - Reactive HTTP client
- **Spring WebSocket** - Real-time data streaming
- **Spring Data JPA** - Database operations
- **PostgreSQL** - Production database
- **H2** - Development/testing database
- **Zerodha Kite Connect API** - Trading broker integration

### Frontend
- **Thymeleaf** - Server-side templating
- **Vanilla JavaScript** - Modular frontend components
- **WebSocket API** - Real-time data consumption
- **HTML5/CSS3** - Responsive UI

### Testing & Documentation
- **JUnit 5** - Unit and integration testing
- **Mockito** - Mock testing framework
- **Swagger/OpenAPI** - API documentation
- **Spring Boot Actuator** - Application monitoring

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend UI   │◄──►│  Spring Boot    │◄──►│  Kite Connect   │
│  (Browser/JS)   │    │   Application   │    │      API        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
        │                       │                       │
        │                       │                       │
        ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   WebSocket     │    │   PostgreSQL    │    │   KiteTicker   │
│  (Real-time)    │    │   Database      │    │  (WebSocket)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Current Implementation Status

### ✅ Completed Features

1. **Authentication System**
   - OAuth integration with Kite Connect
   - Session-based user management
   - Secure token storage

2. **WebSocket Implementation** (Recently Fixed)
   - Server-side WebSocket endpoint at `/ws/ticker`
   - Real-time data broadcasting from Kite API to frontend
   - Connection lifecycle management
   - Structured message format with type/data fields

3. **Order Management**
   - Enhanced order placement with validation
   - Support for MARKET/LIMIT orders
   - Multiple product types (MIS, CNC, NRML)
   - Order history and tracking
   - Real-time order status updates

4. **Instrument Management**
   - Exchange-based instrument filtering
   - Real-time instrument data fetching
   - Search and discovery functionality
   - Token-to-name mapping for orders

5. **Portfolio Integration**
   - Holdings display
   - Real-time portfolio updates
   - Position tracking

6. **Testing Infrastructure**
   - Integration tests for API endpoints
   - WebSocket connectivity testing
   - End-to-end workflow validation

## Production Readiness & Cleanup (December 2024 - June 2025)

### ✅ Completed Production Optimizations

1. **Unified Production Configuration (Latest)**
   - **BREAKING CHANGE**: Merged `application-prod.properties` into unified `application.properties`
   - **Environment Variable Strategy**: Single config file using environment variables with production defaults
   - **Profile-less Architecture**: No more Spring profiles - uses env vars for dev/prod differentiation
   - Enhanced `start.sh` production startup script with isolated test environment
   - Environment validation for required variables
   - Database connectivity checks with proper PostgreSQL/H2 separation

2. **Code Organization & Cleanup**
   - **Removed duplicate InstrumentController**: Eliminated redundant REST controller that conflicted with reactive InstrumentHandler routes
   - **Mock service relocation**: Moved `MockTickerService` and `MockWebClientConfig` from main source to test source (`src/test/java/org/mandrin/rain/broker/mock/`)
   - **SQL consolidation with PURPOSE-BASED NAMING (Latest)**:
     - `mock-data-comprehensive.sql` → **`development-ui-testing.sql`** (35+ instruments, realistic test data)
     - `mock-data-simple.sql` → **`development-minimal.sql`** (basic testing, 9 instruments)
     - `test-data.sql` → **`integration-testing.sql`** (CI/CD pipeline data)
   - **Created SQL documentation**: `test-configs/sql/README.md` explaining purpose of each file

3. **Testing Infrastructure Enhancement**
   - **Unified test script**: Merged all testing into comprehensive `claude-test.sh`
   - **Multi-mode testing**: `--quick`, `--backend`, `--frontend`, `--all` modes
   - **Integrated development startup**: Added `--start-dev` flag combining functionality from `start-dev.sh`
   - **Enhanced validation**: WebSocket testing, API endpoint verification, workflow simulation
   - **Performance testing**: Response time monitoring, load validation
   - **Authentication fix**: Updated `KiteAuthService` to work with unified configuration (no Spring profiles)

4. **Documentation Consolidation**
   - **Merged DEV-SETUP.md into CLAUDE.md**: Single source of truth for development setup
   - **Enhanced troubleshooting**: Added database operations, environment setup guides
   - **Testing protocols**: Detailed JavaScript integration testing procedures
   - **Updated for unified configuration**: All documentation reflects new environment variable approach
   - **Purpose-based SQL naming**: Documentation updated to reflect new meaningful file names

5. **Log & Build Cleanup**
   - Removed development log files (`app.log`, `dev.log`, `claude-test-output.log`)
   - Cleaned build artifacts
   - Updated `.gitignore` to prevent log file commits

### 📋 **MANDATORY TESTING PROTOCOLS FOR ANY CODE CHANGES**

#### **🚨 CRITICAL: When to Run Tests (MANDATORY)**
- **After ANY code changes**: Run `./claude-test.sh` for basic validation
- **After refactoring**: Run `./claude-test.sh --all` for comprehensive validation  
- **After UI changes**: Run `./claude-test.sh --frontend` to verify JavaScript integration
- **After server changes**: Run `./claude-test.sh --backend` to test API endpoints and workflows  
- **After configuration changes**: Run `./claude-test.sh --all` for comprehensive validation
- **Before deployment**: Always run full test suite
- **During development**: Use `./claude-test.sh --start-dev` to start application with proper test environment

#### **⚠️ NEVER skip claude-test.sh validation after refactoring!**

#### Test Coverage
- ✅ API endpoint availability and response codes
- ✅ WebSocket connectivity and message handling
- ✅ Database initialization and mock data loading
- ✅ Complete user workflows (instrument subscription, order placement, portfolio viewing)
- ✅ Performance validation (response times, connection stability)
- ✅ Mock architecture verification (dev mode vs production mode)

### 🏗️ Architecture Improvements

1. **Reactive API Consolidation**
   - Single reactive router (`InstrumentRouterConfig`) handles all instrument endpoints
   - V2 API endpoints (`/api/v2/instruments/*`) for enhanced functionality
   - Legacy compatibility routes for existing frontend code
   - Better error handling and response structure

2. **Mock Architecture Refinement**
   - Mock services only active when `kite.dev.mock_session=true`
   - Realistic ticker data generation with market hours simulation
   - Comprehensive mock API responses for development testing
   - Clean separation between development mocks and production code

## API Endpoints

### Authentication
- `GET /login` - Redirect to Kite Connect OAuth
- `GET /kite/callback` - Handle OAuth callback
- `POST /logout` - Session termination

### Instruments
- `POST /api/instruments/{exchange}` - Fetch and store instrument data
- `GET /api/instruments/exchanges` - List available exchanges
- `GET /api/instruments/types` - Get instrument types by exchange
- `GET /api/instruments/names` - Get instrument names filtered by exchange/type

### Trading
- `POST /api/orders` - Place trading orders
- `GET /api/orders` - List order history
- Order payload supports:
  ```json
  {
    "tradingsymbol": "RELIANCE",
    "instrumentToken": 738561,
    "exchange": "NSE",
    "quantity": 1,
    "price": 2500.00,
    "transactionType": "BUY",
    "orderType": "LIMIT",
    "product": "MIS"
  }
  ```

### Real-time Data
- `POST /api/ticker/subscribe` - Subscribe to instrument price feeds
- `GET /api/ticker/subscriptions` - List active subscriptions
- `POST /api/ticker/disconnect` - Disconnect ticker service
- WebSocket endpoint: `ws://localhost:8080/ws/ticker`

### Portfolio
- Portfolio holdings and positions endpoints (integrated with UI)

## Configuration

### **NEW: Unified Configuration System (Environment Variables)**

**BREAKING CHANGE (June 2025)**: No more Spring profiles! Single `application.properties` uses environment variables.

#### **Production Environment Variables (Required)**
```bash
KITE_API_KEY=your_zerodha_api_key
KITE_API_SECRET=your_zerodha_api_secret
KITE_USER_ID=your_zerodha_user_id
POSTGRES_URL=jdbc:postgresql://localhost:5432/broker
POSTGRES_USER=your_db_user
POSTGRES_PASSWORD=your_db_password
```

#### **Development Environment Variables (Optional)**
```bash
# Enable development features
AUTO_SESSION=true
MOCK_SESSION=true
SERVER_PORT=8081
SESSION_TIMEOUT=24h
SESSION_SECURE=false

# Override database (optional - defaults to production settings)
spring_datasource_url=jdbc:h2:mem:testdb
spring_datasource_username=sa
spring_datasource_password=password
```

### **NEW: Purpose-Based SQL File Organization**

**BREAKING CHANGE (June 2025)**: SQL files renamed for clarity and purpose-based organization:

```bash
# OLD NAMES (Confusing)          →   NEW NAMES (Purpose-Based)
mock-data-comprehensive.sql     →   development-ui-testing.sql
mock-data-simple.sql            →   development-minimal.sql  
test-data.sql                   →   integration-testing.sql
```

**File Purpose Documentation:**
- **`development-ui-testing.sql`**: 35+ instruments, realistic test data for comprehensive UI testing
- **`development-minimal.sql`**: 9 basic instruments for quick development testing  
- **`integration-testing.sql`**: Structured data for automated tests and CI/CD pipelines

**Location**: All files in `test-configs/sql/` with `README.md` documentation

### **NEW: Unified Application Properties Structure**
```properties
# Application Identity
spring.application.name=broker-service

# Server configuration (environment-driven)
server.port=${SERVER_PORT:8080}
server.servlet.session.timeout=${SESSION_TIMEOUT:1h}
server.servlet.session.cookie.secure=${SESSION_SECURE:true}

# Database configuration (environment-driven)
spring.datasource.url=${POSTGRES_URL}
spring.datasource.username=${POSTGRES_USER}
spring.datasource.password=${POSTGRES_PASSWORD}

# Kite API Configuration
kite.api_key=${KITE_API_KEY}
kite.api_secret=${KITE_API_SECRET}
kite.user_id=${KITE_USER_ID}

# Development features (disabled by default for production)
kite.dev.auto_session=${AUTO_SESSION:false}
kite.dev.mock_session=${MOCK_SESSION:false}

# JPA Configuration  
spring.jpa.hibernate.ddl-auto=${DDL_AUTO:validate}
spring.jpa.show-sql=${SHOW_SQL:false}
```

## Database Schema

### Key Tables
- `instruments` - Trading instrument master data
- `trade_orders` - Order history and tracking
- `subscriptions` - WebSocket subscription management

## Missing Features & Recommendations

### 🔴 Critical Missing Features

1. **Advanced Order Types**
   - Stop-loss orders
   - Bracket orders (BO)
   - Cover orders (CO)
   - Good Till Date (GTD) orders
   - After Market Orders (AMO)

2. **Risk Management**
   - Position sizing algorithms
   - Maximum loss limits
   - Daily/weekly loss circuit breakers
   - Risk-reward ratio calculations

3. **Strategy Framework**
   - Algorithmic strategy runner
   - Backtesting capabilities
   - Strategy performance metrics
   - Paper trading mode

### 🟡 High Priority Enhancements

4. **Real-time Analytics**
   - Technical indicators (SMA, EMA, RSI, MACD)
   - Chart data integration
   - Market depth (Level 2 data)
   - Historical data analysis

5. **Portfolio Analytics**
   - P&L calculations
   - Performance metrics
   - Risk analysis
   - Tax reporting

6. **Notification System**
   - Email/SMS alerts for orders
   - Price alerts
   - Strategy execution notifications
   - System health alerts

### 🟢 Medium Priority Features

7. **Enhanced UI/UX**
   - Professional trading interface
   - Real-time charts
   - Dark mode
   - Mobile responsiveness

8. **Security Improvements**
   - Rate limiting
   - API key rotation
   - Input validation enhancements
   - HTTPS enforcement

9. **Monitoring & Logging**
   - Structured logging with ELK stack
   - Application metrics (Prometheus)
   - Performance monitoring
   - Error tracking

## MCP Server Integration Readiness

### ✅ Ready for MCP
- **REST API Foundation**: All endpoints documented and tested
- **Authentication**: Session-based auth (can be extended for API keys)
- **Data Models**: Well-defined request/response structures
- **Error Handling**: Consistent error responses

### 🔄 Needed for MCP Integration
1. **API Authentication**
   - API key-based authentication
   - Rate limiting per API key
   - Scope-based permissions

2. **Enhanced API Responses**
   - Standardized response formats
   - Pagination support
   - Filtering and sorting

3. **Real-time Data API**
   - WebSocket authentication
   - Subscription management API
   - Historical data endpoints

## Deployment Recommendations

### **NEW: Unified Configuration Deployment**

#### **Development**
```bash
# MANDATORY: Use claude-test.sh for development
./claude-test.sh --start-dev

# Alternative: Manual development startup
export AUTO_SESSION=true
export MOCK_SESSION=true
export SERVER_PORT=8081
./gradlew bootRun
```

#### **Production**
```bash
# Set production environment variables
export KITE_API_KEY=your_api_key
export KITE_API_SECRET=your_api_secret
export KITE_USER_ID=your_user_id
export POSTGRES_URL=jdbc:postgresql://localhost:5432/broker
export POSTGRES_USER=your_db_user
export POSTGRES_PASSWORD=your_db_password

# Build and start (unified configuration)
./start.sh
```

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY build/libs/broker-service.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Testing

### 🚨 **MANDATORY TESTING PROTOCOL - UPDATED FOR UNIFIED CONFIG**

**⚠️ CRITICAL: Before ANY code changes or deployments, ALL these tests MUST pass:**

```bash
# Step 1: Clean build verification (REQUIRED)
./gradlew clean build -x test

# Step 2: Core service tests with isolated environment (REQUIRED) 
env -u POSTGRES_URL -u POSTGRES_USER -u POSTGRES_PASSWORD ./gradlew test --tests "*ServiceTest" --tests "BrokerApplicationTests"

# Step 3: Comprehensive functional verification (MANDATORY)
./claude-test.sh

# Step 4: Validate refactoring (MANDATORY after any refactoring)
./claude-test.sh --all

# Step 5: Full test suite (OPTIONAL but recommended)
./gradlew test
```

**❌ DO NOT proceed if Steps 1, 2, or 3 fail!**
**⚠️ NEW: Tests now run in isolated environment to prevent production DB conflicts**

### 🤖 **CLAUDE-TEST.SH - COMPREHENSIVE VERIFICATION - UPDATED**

**The `./claude-test.sh` script is now the MANDATORY comprehensive test that must pass after every code change and especially after refactoring:**

#### **🚨 MANDATORY USAGE SCENARIOS:**
1. **After refactoring**: `./claude-test.sh --all` (comprehensive validation)
2. **After configuration changes**: `./claude-test.sh` (basic validation)
3. **During development**: `./claude-test.sh --start-dev` (start app with test environment)
4. **Before deployment**: `./claude-test.sh` (mandatory validation)
5. **After any code changes**: `./claude-test.sh` (basic functional test)

#### **⚠️ CRITICAL: REFACTORING PROTOCOL WITH CLAUDE-TEST.SH**

**EVERY refactoring task MUST include these mandatory steps:**

```bash
# BEFORE REFACTORING: Establish baseline
./claude-test.sh --all > refactor-baseline.log

# DURING REFACTORING: Test at each major step
./claude-test.sh  # Quick validation after each change

# AFTER REFACTORING: Comprehensive validation
./claude-test.sh --all
# Must achieve ≥80% success rate to proceed

# DEPLOYMENT: Final validation
./claude-test.sh && ./start.sh
```

**❌ NEVER complete refactoring without running claude-test.sh validation**
**✅ ALWAYS verify the refactored system passes comprehensive testing**

#### **What it tests:**
- ✅ **Session Management** - Auto-session creation, authentication bypass
- ✅ **UI Experience** - Main page loads, H2 console accessible
- ✅ **Core APIs** - Session status, authentication, orders, portfolio
- ✅ **Documentation** - Swagger UI, OpenAPI docs, health checks
- ✅ **Performance** - Response times under 2 seconds
- ✅ **Database** - H2 console accessibility
- ✅ **WebSocket Connectivity** - Real-time data streaming endpoints
- ✅ **Portfolio APIs** - Holdings and positions data access
- ⚠️ **Known Missing APIs** - Legacy and V2 instrument endpoints (expected to fail)

#### **NEW: JavaScript Integration Testing Protocol**

**The `./claude-js-test.sh` script provides comprehensive frontend-backend integration validation:**

##### **What it tests:**
- ✅ **Configuration Loading** - Frontend config API driven by application properties
- ✅ **WebSocket Integration** - Real-time ticker and instrument data streaming
- ✅ **AJAX API Integration** - All RESTful endpoints for CRUD operations
- ✅ **Session Management JS** - Authentication and session validation from frontend
- ✅ **UI Component Integration** - Form submissions, dynamic updates
- ✅ **Error Handling JS** - Network failures, validation errors, timeouts
- ✅ **Complete User Workflow** - End-to-end JavaScript-driven scenarios

##### **Usage:**
```bash
# MANDATORY: Run after any UI/server code changes
./claude-js-test.sh

# Expected output for healthy frontend-backend integration:
# "Passed: 45+ | Warnings: <5 | Failed: 0-1"
```

##### **Test Categories:**
1. **Configuration Loading Tests** - Environment-aware frontend config
2. **WebSocket JS Tests** - Real-time data streaming via JavaScript
3. **AJAX Integration Tests** - All API endpoints accessible from JavaScript
4. **Session Management Tests** - Authentication flow in JavaScript
5. **UI Component Tests** - Form submissions and dynamic content
6. **Error Handling Tests** - Graceful failure scenarios
7. **Performance Tests** - Response time validation from frontend

#### **Success Criteria:**
- **100% pass rate**: All critical functionality working
- **80%+ pass rate**: Acceptable for continued development (some known missing endpoints)
- **<80% pass rate**: CRITICAL - Requires immediate fixes

#### **Usage:**
```bash
# MANDATORY after any code changes
./claude-test.sh

# Expected output for healthy application:
# "✅ OVERALL STATUS: FUNCTIONAL FOR DEVELOPMENT"
# "📈 Success Rate: 85% (17/20)" (or higher)
```

#### **Integration with Development Workflow:**
```bash
# Complete development verification sequence:
./gradlew clean build                    # Build verification
./gradlew test --tests "*ServiceTest"    # Unit tests
./claude-test.sh                        # Comprehensive functional test
./claude-js-test.sh                     # Frontend-backend integration test
./gradlew bootRun                       # Manual testing (optional)
```

#### **When to Run JavaScript Integration Tests:**

**MANDATORY scenarios for running `./claude-js-test.sh`:**

1. **After API Endpoint Changes** - New/modified REST endpoints
2. **After Configuration Changes** - Properties files, application.yml modifications
3. **After WebSocket Changes** - Real-time data streaming modifications
4. **After JavaScript Code Changes** - Frontend logic updates
5. **After Session/Authentication Changes** - Login flow modifications
6. **Before Deployment** - Complete integration verification
7. **After Dependency Updates** - Spring Boot, frontend library updates

**Example workflow:**
```bash
# Developer makes API change
./gradlew clean build                    # Verify compilation
./claude-test.sh                        # Verify backend functionality
./claude-js-test.sh                     # Verify frontend integration
# Only deploy if both pass with 80%+ success rate
```

### Test Categories

#### Core Tests (Must Pass)
```bash
./gradlew test --tests "*ServiceTest" --tests "BrokerApplicationTests"
```
- ✅ KiteTickerServiceTest 
- ✅ OrderServiceTest
- ✅ InstrumentServiceTest 
- ✅ KiteAuthServiceTest
- ✅ SubscriptionServiceTest
- ✅ BrokerApplicationTests

#### Integration Tests (Optional)
```bash
./gradlew test --tests "*Integration*"
```

#### Full Test Suite
```bash
./gradlew test
```

### Current Test Coverage
- ✅ Unit tests for services and controllers
- ✅ Integration tests for API endpoints  
- ✅ WebSocket connectivity tests
- ✅ **JavaScript Integration Tests** (comprehensive frontend-backend validation)
- ⚠️ Some integration tests fail due to Mockito/Java 23 compatibility issues (non-blocking)

### 🧪 **Comprehensive Test Coverage Guidelines**

**For detailed test scenario coverage requirements, refactoring protocols, and test maintenance guidelines, see:** 
**→ [COMPREHENSIVE TEST COVERAGE PROTOCOL](#-comprehensive-test-coverage-protocol)**

**Key Points:**
- **Map all test scenarios** before refactoring
- **Cover 5 mandatory categories**: Happy Path, Error Handling, Edge Cases, Integration, Performance
- **Minimum coverage thresholds**: 80% unit tests, 70% integration, 95% critical paths
- **Never skip test creation** during refactoring
- **Test backward compatibility** during API transitions

### Recent Fix Applied (2024-12-29)
- **Issue**: KiteTicker OnError interface compilation error
- **Fix**: Removed unsupported error listener, maintained try-catch error handling
- **Status**: ✅ Core functionality verified, build successful

## Build & Run

### Prerequisites
- Java 17+
- Gradle 8.0+
- PostgreSQL 12+ (for production)
- Zerodha Kite Connect API credentials

### 🚨 **MANDATORY BUILD REQUIREMENTS**

**⚠️ CRITICAL: Any code changes MUST pass clean build and core tests before deployment!**

```bash
# REQUIRED: Clean build and test verification for ANY changes
./gradlew clean build
./gradlew test --tests "*ServiceTest" --tests "BrokerApplicationTests"

# Both commands MUST succeed before proceeding with deployment
```

**⚠️ ADDITIONAL: If API contracts changed, ALSO verify UI functionality:**

```bash
# MANDATORY: Manual UI testing after API contract changes
./gradlew bootRun
# Open browser: http://localhost:8080
# Test all UI features that interact with modified APIs
# Check browser console for JavaScript errors
# Verify API calls in network tab
```

**Why this is mandatory:**
- Ensures WebClient configuration is correct
- Validates all service integrations
- Prevents runtime failures
- Maintains code quality standards
- **Prevents UI breaking** when API contracts change
- **Ensures full-stack functionality** works end-to-end

### **NEW: Unified Configuration Quick Start**
```bash
# Clone repository
git clone <repository-url>
cd broker

# MANDATORY: For Claude Agents - ALWAYS use claude-test.sh for development
# This sets proper environment variables and starts with unified config
./claude-test.sh --start-dev

# Access application (unified configuration)
open http://localhost:8081
open http://localhost:8081/h2-console  # Database console
open http://localhost:8081/swagger-ui/index.html  # API documentation
```

### **NEW: Unified Configuration Production Setup**
```bash
# Set ALL required environment variables for production
export KITE_API_KEY=your_api_key
export KITE_API_SECRET=your_api_secret
export KITE_USER_ID=your_user_id
export POSTGRES_URL=jdbc:postgresql://localhost:5432/broker
export POSTGRES_USER=your_db_user
export POSTGRES_PASSWORD=your_db_password

# MANDATORY: Verify unified configuration build
./gradlew clean build -x test
env -u POSTGRES_URL -u POSTGRES_USER -u POSTGRES_PASSWORD ./gradlew test --tests "*ServiceTest" --tests "BrokerApplicationTests"

# MANDATORY: Validate with claude-test.sh
./claude-test.sh

# Only if ALL tests pass, then run production application
./start.sh

# Access application
open http://localhost:8080
```

### Swagger API Documentation
- Available at: `http://localhost:8080/swagger-ui/index.html`
- API documentation: `http://localhost:8080/v3/api-docs`

## Troubleshooting

### Common Issues

1. **WebSocket Connection Fails**
   - Verify port 8080 is not blocked
   - Check browser console for connection errors
   - Ensure proper authentication session

2. **Kite API Integration Issues**
   - Verify API credentials are correct
   - Check API key permissions with Zerodha
   - Ensure proper OAuth callback URL configuration

3. **Database Connection Issues**
   - Verify PostgreSQL is running
   - Check connection credentials
   - Ensure database exists and is accessible

### Logging
- Application logs: Standard output
- Log level: INFO (configurable via `logging.level.org.mandrin.rain=DEBUG`)
- WebSocket events: DEBUG level

## Future Roadmap

### Phase 1: Core Trading Features (Next 4 weeks)
- [ ] Advanced order types implementation
- [ ] Risk management framework
- [ ] Basic strategy runner

### Phase 2: Analytics & Monitoring (Weeks 5-8)
- [ ] Technical indicators
- [ ] Performance analytics
- [ ] Comprehensive monitoring

### Phase 3: MCP Server Integration (Weeks 9-12)
- [ ] API key authentication
- [ ] MCP-specific endpoints
- [ ] Real-time data streaming for MCP

### Phase 4: Advanced Features (Weeks 13-16)
- [ ] Backtesting engine
- [ ] Professional UI
- [ ] Mobile app

## Contributing

### Code Style
- Follow Spring Boot conventions
- Use Lombok for boilerplate reduction
- Implement comprehensive error handling
- Add unit tests for new features

### 🧹 **MANDATORY CODE CLEANUP PROTOCOL**

**⚠️ CRITICAL: When refactoring or implementing new patterns, ALWAYS perform complete cleanup!**

#### **Refactoring Cleanup Requirements:**

1. **Remove Redundant Files**
   - Delete old controllers when implementing new patterns (e.g., reactive routers)
   - Remove obsolete service classes
   - Clean up unused configuration files
   - Delete outdated test files

2. **Update Related Tests**
   - Remove tests for deleted components
   - Create new tests for replacement components
   - Ensure all test references are updated
   - Verify no broken imports remain

3. **Documentation Updates**
   - Update API documentation for new endpoints
   - Remove references to deleted components
   - Add migration guides for new patterns
   - Update architecture diagrams

4. **Dependency Cleanup**
   - Remove unused imports across all files
   - Clean up unused dependencies in build.gradle.kts
   - Update version compatibility notes

#### **Cleanup Verification Steps:**
```bash
# MANDATORY: Always run after cleanup
./gradlew clean build
./gradlew test --tests "*ServiceTest" --tests "BrokerApplicationTests"

# Verify no broken references
./gradlew compileJava compileTestJava

# Check for unused imports (recommended)
find src -name "*.java" -exec grep -l "import.*unused" {} \;
```

#### **Recent Cleanup Example (December 2024):**
- **Refactored**: InstrumentController.java → Reactive Router-based approach
- **Removed**: 
  - `InstrumentController.java` (replaced by `InstrumentHandler.java`)
  - `InstrumentControllerTest.java` (obsolete)
- **Added**: 
  - `ReactiveInstrumentService.java`
  - `InstrumentRouterConfig.java`
  - `InstrumentHandler.java`
  - `InstrumentWebSocketHandler.java`
- **Result**: 10+ endpoints → 3 consolidated patterns + WebSocket

**❌ DO NOT leave dead code in the repository!**
**✅ Always clean up thoroughly when implementing new patterns!**

### 🧪 **COMPREHENSIVE TEST COVERAGE PROTOCOL**

**⚠️ CRITICAL: Test scenarios are vital for system reliability and must be maintained during refactoring!**

#### **Test Coverage Requirements When Refactoring:**

1. **Identify All Affected Test Scenarios**
   ```bash
   # Find all tests related to component being refactored
   find src/test -name "*Test.java" -exec grep -l "ComponentName" {} \;
   
   # Check for integration tests
   find src/test -name "*Integration*Test.java" -exec grep -l "endpoint_pattern" {} \;
   ```

2. **Map Old Scenarios to New Implementation**
   - **List all existing test scenarios** for the component being refactored
   - **Identify which scenarios are still valid** for the new implementation
   - **Identify new scenarios needed** for the new functionality
   - **Mark obsolete scenarios** for removal

3. **Create Comprehensive Test Migration Plan**
   ```
   BEFORE REFACTORING:
   ├── ComponentTest.java (Unit Tests)
   │   ├── Scenario 1: Normal operation
   │   ├── Scenario 2: Error handling
   │   └── Scenario 3: Edge cases
   ├── ComponentIntegrationTest.java
   └── ComponentWebTest.java
   
   AFTER REFACTORING:
   ├── NewComponentTest.java (Unit Tests)
   │   ├── ✅ Scenario 1: Normal operation (migrated)
   │   ├── ✅ Scenario 2: Error handling (migrated)
   │   ├── ✅ Scenario 3: Edge cases (migrated)
   │   ├── 🆕 Scenario 4: New reactive features
   │   └── 🆕 Scenario 5: WebSocket functionality
   ├── NewComponentIntegrationTest.java
   └── ❌ ComponentTest.java (removed)
   ```

#### **Mandatory Test Scenario Categories:**

1. **Happy Path Scenarios** ✅
   - Normal successful operations
   - Valid input processing
   - Expected output verification

2. **Error Handling Scenarios** ⚠️
   - Invalid input validation
   - Network failure simulation
   - Database connection errors
   - Authentication/authorization failures

3. **Edge Case Scenarios** 🔍
   - Boundary value testing
   - Null/empty input handling
   - Large data set processing
   - Concurrent request handling

4. **Integration Scenarios** 🔗
   - End-to-end workflow testing
   - Cross-service communication
   - Database transaction testing
   - External API integration

5. **Performance Scenarios** ⚡
   - Load testing critical paths
   - Memory usage validation
   - Response time verification
   - Concurrent user simulation

#### **Test Implementation Guidelines:**

```java
// Example: Comprehensive test scenarios for refactored component
@SpringBootTest
class ReactiveInstrumentServiceTest {
    
    // ===== HAPPY PATH SCENARIOS =====
    @Test
    void getInstruments_WithValidExchange_ShouldReturnInstruments() {}
    
    @Test
    void getInstruments_WithValidUnderlying_ShouldReturnFilteredResults() {}
    
    // ===== ERROR HANDLING SCENARIOS =====
    @Test
    void getInstruments_WithInvalidFilter_ShouldReturnEmptyResult() {}
    
    @Test
    void getInstruments_WithDatabaseError_ShouldHandleGracefully() {}
    
    // ===== EDGE CASE SCENARIOS =====
    @Test
    void getInstruments_WithNullFilter_ShouldReturnAllInstruments() {}
    
    @Test
    void getInstruments_WithLargeDataSet_ShouldHandleEfficiently() {}
    
    // ===== REACTIVE-SPECIFIC SCENARIOS =====
    @Test
    void getInstruments_ShouldReturnReactiveStream() {}
    
    @Test
    void getInstruments_WithBackpressure_ShouldHandleCorrectly() {}
}
```

#### **WebSocket Test Scenarios:**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InstrumentWebSocketTest {
    
    // ===== CONNECTION SCENARIOS =====
    @Test
    void webSocketConnection_ShouldEstablishSuccessfully() {}
    
    @Test
    void webSocketConnection_WithInvalidAuth_ShouldReject() {}
    
    // ===== MESSAGE HANDLING SCENARIOS =====
    @Test
    void subscribeInstruments_WithValidFilter_ShouldReceiveUpdates() {}
    
    @Test
    void subscribeInstruments_WithInvalidMessage_ShouldSendError() {}
    
    // ===== REAL-TIME SCENARIOS =====
    @Test
    void instrumentUpdates_ShouldBroadcastToAllClients() {}
    
    @Test
    void clientDisconnection_ShouldCleanupResources() {}
}
```

#### **Legacy API Compatibility Tests:**

```java
// Ensure backward compatibility during transition
@Test
void legacyEndpoints_ShouldStillWork() {
    // Test old endpoint format
    mockMvc.perform(get("/api/instruments/exchanges"))
           .andExpect(status().isOk());
}

@Test
void newEndpoints_ShouldProvideEquivalentData() {
    // Compare old vs new endpoint results
    List<String> legacyResult = callLegacyAPI();
    List<String> newResult = callNewAPI();
    assertThat(newResult).containsExactlyElementsOf(legacyResult);
}
```

#### **Test Coverage Verification:**

```bash
# MANDATORY: Check test coverage after refactoring
./gradlew test jacocoTestReport

# Verify minimum coverage thresholds
# - Unit Tests: 80%+ line coverage
# - Integration Tests: 70%+ scenario coverage
# - Critical Paths: 95%+ coverage

# Run specific test categories
./gradlew test --tests "*ServiceTest*"      # Unit tests
./gradlew test --tests "*Integration*"      # Integration tests
./gradlew test --tests "*WebSocket*"        # WebSocket tests
```

#### **Test Maintenance Checklist:**

- [ ] **Map all existing test scenarios** before refactoring
- [ ] **Create new tests for new functionality** 
- [ ] **Migrate valid scenarios** to new test structure
- [ ] **Remove obsolete test files** completely
- [ ] **Verify test coverage** meets minimum thresholds
- [ ] **Test backward compatibility** if legacy APIs remain
- [ ] **Document new test scenarios** in commit messages
- [ ] **Run full test suite** before completing refactoring

#### **Recent Test Refactoring Example (December 2024):**

**Issues Found & Fixed:**
- **404 errors on legacy endpoints** - Fixed `getInstrumentTypesLegacy` handler implementation
- **Missing Swagger documentation** - Added comprehensive OpenAPI annotations
- **UI defaulting to BSE** - Already correctly defaulted to NSE

**Removed Obsolete Tests:**
- `InstrumentControllerTest.java` (old @WebMvcTest approach)

**Fixed Issues:**
- **Legacy API Handler Bug** - `getInstrumentTypesLegacy` was calling wrong method
- **Route Registration** - All legacy routes properly registered in `InstrumentRouterConfig`
- **Query Parameter Handling** - Fixed extraction in legacy compatibility methods

**Critical Fix Applied:**
```java
// BEFORE (Broken):
public Mono<ServerResponse> getInstrumentTypesLegacy(ServerRequest request) {
    String exchange = request.queryParam("exchange").orElse("");
    return getInstrumentTypesByExchange(request); // ❌ Wrong - used path variable method
}

// AFTER (Fixed):
public Mono<ServerResponse> getInstrumentTypesLegacy(ServerRequest request) {
    String exchange = request.queryParam("exchange").orElse("");
    return ServerResponse.ok()
            .body(reactiveInstrumentService.getMetadata("types", exchange), String.class); // ✅ Correct
}
```

**Should Have Added (Recommended for Future):**
- `ReactiveInstrumentServiceTest.java` - Unit tests for reactive service
- `InstrumentHandlerTest.java` - Handler logic tests  
- `InstrumentWebSocketTest.java` - WebSocket functionality tests
- `EndpointIntegrationTest.java` - Test all endpoints return 200, not 404

**Test Scenarios to Cover:**
```java
// Critical test scenarios to prevent 404 regressions:
@Test void legacy_underlyings_ShouldReturn200NotFound404() {}
@Test void legacy_exchanges_ShouldReturn200NotFound404() {}
@Test void legacy_typesWithExchange_ShouldReturn200NotFound404() {}
@Test void v2_allEndpoints_ShouldReturn200NotFound404() {}
@Test void legacyVsV2_ShouldReturnEquivalentData() {}
```

**❌ NEVER skip test creation during refactoring!**
**✅ Always ensure comprehensive scenario coverage!**

### 📚 **MANDATORY DOCUMENTATION UPDATE PROTOCOL**

**⚠️ CRITICAL: Documentation must stay synchronized with code changes during refactoring!**

#### **Documentation Files to Update During Refactoring:**

1. **README.md** (Root Level)
   - **API endpoints** - Update all changed/removed/added endpoints
   - **Usage examples** - Update code samples and curl commands
   - **Installation steps** - Update if dependencies change
   - **Quick start guide** - Ensure examples work with new implementation

2. **docs/CLAUDE.md** (This File)
   - **API Endpoints section** - Update with new consolidated endpoints
   - **Architecture diagrams** - Update if patterns change (controller → reactive)
   - **Implementation Status** - Mark completed features
   - **Recent fixes** - Add new refactoring examples

3. **API Documentation**
   - **Swagger/OpenAPI** - Update endpoint definitions
   - **Postman collections** - Update request formats
   - **Integration guides** - Update external API documentation

4. **Code Comments & JavaDoc**
   - **Update class-level docs** for refactored components
   - **Update method signatures** and parameter descriptions
   - **Remove obsolete comments** referencing deleted code

5. **Frontend/UI Code** ⚠️ **CRITICAL**
   - **Update API endpoint URLs** in JavaScript files
   - **Update request/response data structures** to match new contracts
   - **Update error handling** for new response formats
   - **Test UI functionality** with new API endpoints

#### **Documentation Update Checklist:**

##### **Before Refactoring:**
- [ ] **Document current API structure** in refactoring notes
- [ ] **Screenshot current documentation** for reference
- [ ] **List all files referencing** the component being refactored

##### **During Refactoring:**
- [ ] **Update inline code comments** in new components
- [ ] **Add JavaDoc** for new public methods and classes
- [ ] **Document design decisions** in commit messages

##### **After Refactoring:**
- [ ] **Update README.md** with new API endpoints and examples
- [ ] **Update docs/CLAUDE.md** with architectural changes
- [ ] **Update Swagger annotations** for new endpoints
- [ ] **Update any integration documentation**
- [ ] **Remove references** to deleted components
- [ ] **Test all documented examples** to ensure they work
- [ ] **⚠️ UPDATE ALL UI CODE** that calls modified APIs
- [ ] **Verify UI functionality** works with new API contracts
- [ ] **Update frontend error handling** for new response formats

#### **Documentation Verification Commands:**

```bash
# MANDATORY: Check all documentation references
grep -r "old_endpoint_pattern" docs/ README.md
grep -r "OldClassName" docs/ README.md

# Find outdated API examples
grep -r "/api/instruments/" docs/ README.md | grep -v "/api/v2/"

# Check for broken internal links
grep -r "\[.*\](#.*)" docs/

# Verify code examples compile
# (Extract code blocks and test them)
```

#### **README.md Update Requirements:**

```markdown
# Example: What to update in README.md

OLD (Outdated):
```bash
# Loading instrument data
POST /api/instruments/{exchange}
GET /api/instruments/exchanges
GET /api/instruments/types?exchange=NSE
```

NEW (Current):
```bash
# V2 API (Recommended)
POST /api/v2/instruments/refresh/{exchange}
GET /api/v2/instruments/metadata/exchanges
GET /api/v2/instruments/metadata/types/{exchange}

# Legacy API (Backward Compatible)
POST /api/instruments/{exchange}
GET /api/instruments/exchanges
```
```

#### **Documentation Examples - Real Update Needed:**

**Current README.md Issues (December 2024):**
- ❌ **Outdated API endpoints** - Still shows old `/api/instruments/` patterns
- ❌ **Missing V2 API documentation** - No mention of new reactive endpoints
- ❌ **Missing WebSocket documentation** - No `/ws/instruments` endpoint
- ❌ **Outdated examples** - Code samples won't work with new structure

**Required Updates:**
```markdown
# MUST UPDATE in README.md:

## API Endpoints (Updated December 2024)

### V2 API (Recommended - Reactive)
- `GET /api/v2/instruments/metadata/exchanges` - Get available exchanges
- `GET /api/v2/instruments/metadata/types/{exchange}` - Get instrument types
- `GET /api/v2/instruments/underlying/{underlying}` - Get instruments by underlying
- `POST /api/v2/instruments/refresh` - Refresh all instrument data

### Legacy API (Backward Compatible)
- `POST /api/instruments/{exchange}` - Load instruments for exchange
- `GET /api/instruments/exchanges` - Get exchanges (legacy)

### WebSocket Streaming
- `WS /ws/instruments` - Real-time instrument data streaming
- `WS /ws/ticker` - Market data streaming

### Examples
```bash
# Refresh all instruments (V2 API)
curl -X POST http://localhost:8080/api/v2/instruments/refresh

# Get NIFTY instruments (V2 API)  
curl http://localhost:8080/api/v2/instruments/underlying/NIFTY

# Legacy endpoint (still works)
curl http://localhost:8080/api/instruments/exchanges
```
```

#### **Documentation Maintenance Commands:**

```bash
# MANDATORY: Update documentation after refactoring
./gradlew clean build
./gradlew test --tests "*ServiceTest" --tests "BrokerApplicationTests"

# Verify documentation examples work
curl -X GET http://localhost:8080/api/v2/instruments/metadata/exchanges
curl -X POST http://localhost:8080/api/v2/instruments/refresh

# Check for broken references
find . -name "*.md" -exec grep -l "InstrumentController" {} \;
find . -name "*.md" -exec grep -l "/api/instruments/" {} \;
```

### 💻 **MANDATORY FRONTEND/UI UPDATE PROTOCOL**

**⚠️ CRITICAL: When server API contracts change, frontend code MUST be updated immediately!**

#### **Frontend Files That Require Updates:**

1. **JavaScript API Client Files:**
   - `src/main/resources/static/js/instruments.js` - Instrument API calls
   - `src/main/resources/static/js/orders.js` - Order management APIs
   - `src/main/resources/static/js/portfolio.js` - Portfolio APIs
   - `src/main/resources/static/js/ticker.js` - WebSocket and ticker APIs
   - `src/main/resources/static/js/session.js` - Authentication APIs

2. **HTML Templates:**
   - `src/main/resources/templates/home.html` - Main UI elements
   - Any forms that submit to modified endpoints
   - Error message displays that handle new response formats

3. **Frontend Configuration:**
   - API endpoint URLs and paths
   - Request/response data structures
   - Error handling logic

#### **UI Update Requirements by API Change Type:**

##### **Endpoint URL Changes:**
```javascript
// BEFORE API refactoring:
fetch('/api/instruments/underlyings')

// AFTER API refactoring - UPDATE REQUIRED:
fetch('/api/v2/instruments/metadata/underlyings')
  .catch(() => fetch('/api/instruments/underlyings')) // Fallback for compatibility
```

##### **Request Format Changes:**
```javascript
// BEFORE: Query parameters
fetch(`/api/instruments/names?exchange=${exchange}&type=${type}`)

// AFTER: Path parameters - UPDATE REQUIRED:
fetch(`/api/v2/instruments/names/${exchange}/${type}`)
```

##### **Response Format Changes:**
```javascript
// BEFORE: Simple array response
.then(list => list.forEach(item => console.log(item)))

// AFTER: Wrapped response - UPDATE REQUIRED:
.then(response => response.data.forEach(item => console.log(item)))
```

##### **Error Handling Updates:**
```javascript
// BEFORE: Basic error handling
.catch(err => console.error('Failed:', err))

// AFTER: Structured error handling - UPDATE REQUIRED:
.catch(err => {
    if (err.status === 404) {
        // Handle not found
    } else if (err.status === 400) {
        // Handle bad request
    }
    console.error('Failed:', err.message || err)
})
```

#### **UI Update Verification Checklist:**

- [ ] **Search all JS files** for old endpoint patterns
- [ ] **Update fetch() calls** to use new endpoint URLs
- [ ] **Update request data structures** to match new API contracts
- [ ] **Update response parsing** for new response formats
- [ ] **Update error handling** for new error response structures
- [ ] **Test all UI functionality** manually in browser
- [ ] **Verify fallback behavior** if legacy APIs are maintained
- [ ] **Check browser console** for JavaScript errors
- [ ] **Test with both success and error scenarios**

#### **Frontend Update Commands:**

```bash
# MANDATORY: Find all API calls that need updating
grep -r "api/instruments" src/main/resources/static/js/
grep -r "api/orders" src/main/resources/static/js/
grep -r "api/ticker" src/main/resources/static/js/

# Find WebSocket connections
grep -r "ws://" src/main/resources/static/js/
grep -r "WebSocket" src/main/resources/static/js/

# Check for hardcoded API paths
grep -r "/api/" src/main/resources/static/js/

# Verify HTML forms and AJAX calls
grep -r "fetch(" src/main/resources/static/js/
grep -r "XMLHttpRequest" src/main/resources/static/js/
```

#### **Real Example: Recent UI Updates for Instrument API Refactoring**

**Required Changes Made:**
```javascript
// ✅ UPDATED: instruments.js
function loadUnderlyingAssets() {
    // NEW: Try V2 API first with fallback
    fetch('/api/v2/instruments/metadata/underlyings')
        .then(r => r.json())
        .then(list => {
            // ✅ Updated to handle V2 response format
            populateUnderlyingSelect(list);
        })
        .catch(err => {
            // ✅ Added fallback to legacy API
            console.warn('V2 API failed, falling back to legacy API:', err);
            fetch('/api/instruments/underlyings')
                .then(r => r.json())
                .then(list => populateUnderlyingSelect(list))
                .catch(legacyErr => console.error('Both APIs failed:', legacyErr));
        });
}
```

**Files Updated:**
- ✅ `instruments.js` - Updated to use V2 API with legacy fallback
- ✅ `home.html` - Already using correct form structure
- ⚠️ **Still Need To Update:** Other JS files if they use old endpoints

#### **Frontend Testing Requirements:**

```bash
# MANDATORY: Manual UI testing after API changes
1. Start application: ./gradlew bootRun
2. Open browser: http://localhost:8080
3. Test each UI feature that uses modified APIs:
   - ✅ Instrument loading and filtering
   - ✅ Order placement forms
   - ✅ Portfolio display
   - ✅ WebSocket connections
   - ✅ Error handling scenarios
4. Check browser console for JavaScript errors
5. Test with network tab open to verify API calls
```

**❌ NEVER modify server APIs without updating UI code!**
**✅ Always verify UI functionality after API contract changes!**
**❌ NEVER leave outdated documentation after refactoring!**
**✅ Always update ALL documentation references!**

### Pull Request Process
1. Create feature branch from `develop`
2. Implement changes with tests
3. **🚨 MANDATORY: RUN CLAUDE-TEST.SH VALIDATION** ← **CRITICAL STEP**
   ```bash
   ./claude-test.sh --all  # Must pass with ≥80% success rate
   ```
4. **PERFORM MANDATORY CLEANUP** (see above)
5. **ENSURE COMPREHENSIVE TEST COVERAGE** (see above)
6. **UPDATE ALL DOCUMENTATION** (see above)
7. **⚠️ UPDATE FRONTEND/UI CODE** (see above) ← **CRITICAL MANDATORY STEP**
8. **Test UI functionality** manually in browser
9. **🚨 FINAL VALIDATION: RUN CLAUDE-TEST.SH AGAIN** ← **CRITICAL STEP**
10. Submit PR with detailed description

**❌ NO PULL REQUEST without claude-test.sh validation!**
**✅ MANDATORY: Include test results in PR description**

## 🤖 **MANDATORY CLAUDE AGENT PROTOCOL**

**⚠️ CRITICAL: When Claude agents are asked to run, test, or work with this application, ALWAYS follow these protocols:**

### **1. Application Startup Protocol**

**❌ NEVER DO:**
```bash
./gradlew bootRun  # DON'T - Uses production profile, requires real Kite credentials
./start.sh        # DON'T - Production mode, requires PostgreSQL and env vars
```

**✅ ALWAYS DO:**
```bash
./start-dev.sh     # CORRECT - Uses dev profile with H2 and mock session
```

**✅ Expected results for Claude agents:**
- Application runs on port 8081 (NOT 8080)
- No authentication errors (auto-session created)
- All API endpoints return data immediately
- H2 database accessible at http://localhost:8081/h2-console
- UI loads without login prompts

### **2. Development Environment Requirements**

**When starting the application, Claude agents MUST:**

1. **Use Development Profile ONLY**
   ```bash
   # MANDATORY startup command for Claude agents
   ./start-dev.sh
   ```

2. **Alternative Development Modes:**
   
   **Option 1: Mock Session (Default - No Real Kite Credentials Needed)**
   ```bash
   ./start-dev.sh
   ```
   This provides:
   - Mock session automatically created
   - Port 8081
   - H2 in-memory database
   - No OAuth login required
   
   **Option 2: Real Kite API with Access Token**
   ```bash
   export KITE_ACCESS_TOKEN="your_real_access_token_here"
   export KITE_API_KEY="your_api_key"
   export KITE_API_SECRET="your_api_secret"
   export KITE_USER_ID="your_user_id"
   ./start-dev.sh
   ```
   
   **Option 3: Real Kite API with OAuth Flow**
   ```bash
   export KITE_API_KEY="your_api_key"
   export KITE_API_SECRET="your_api_secret"
   export KITE_USER_ID="your_user_id"
   export KITE_DEV_AUTO_SESSION=false
   ./start-dev.sh
   ```

3. **Verify Development URLs**
   - Main Application: `http://localhost:8081/` (NOT 8080)
   - H2 Database Console: `http://localhost:8081/h2-console`
   - Swagger UI: `http://localhost:8081/swagger-ui/index.html`

4. **Access H2 Database Console**
   ```
   JDBC URL: jdbc:h2:mem:testdb
   Username: sa
   Password: password
   ```

### **3. Database Operations for Development**

**View Instruments in Real-Time:**
1. Open H2 Console: http://localhost:8081/h2-console
2. Connect with credentials above
3. Query: `SELECT * FROM INSTRUMENTS`
4. Refresh instrument data via API or UI
5. Re-run query to see updates

**Useful Development Queries:**
```sql
-- View all instruments by exchange
SELECT exchange, COUNT(*) as count FROM instruments GROUP BY exchange;

-- View instrument types
SELECT DISTINCT instrument_type FROM instruments;

-- Search for NIFTY instruments
SELECT * FROM instruments WHERE name LIKE '%NIFTY%' LIMIT 10;

-- View recent orders
SELECT * FROM trade_orders ORDER BY placed_at DESC;

-- Check subscriptions
SELECT * FROM subscriptions ORDER BY subscribed_at DESC;
```

### **3. Endpoint Testing Protocol**

**Before declaring any functionality working, Claude agents MUST test:**

1. **Authentication Bypass (Dev Mode)**
   ```bash
   curl http://localhost:8081/api/instruments/exchanges
   # Should return data, NOT authentication error
   ```

2. **Core API Endpoints**
   ```bash
   # Test all these endpoints return 200, not 404 or 401
   curl http://localhost:8081/api/instruments/underlyings
   curl http://localhost:8081/api/instruments/types?exchange=NSE
   curl http://localhost:8081/api/v2/instruments/metadata/exchanges
   curl http://localhost:8081/api/v2/instruments/metadata/underlyings
   ```

3. **Database Functionality**
   ```sql
   -- Access H2 console and verify these queries work
   SELECT COUNT(*) FROM instruments;
   SELECT DISTINCT exchange FROM instruments;
   SELECT * FROM instruments LIMIT 5;
   ```

4. **UI Access**
   ```bash
   # Verify main page loads without login redirect
   curl -I http://localhost:8081/
   # Should return 200, not 302 redirect to login
   ```

### **4. Functional Verification Checklist**

**Claude agents MUST verify these work in real-time:**

- [ ] ✅ Application starts on port 8081 (dev profile)
- [ ] ✅ H2 database accessible via console
- [ ] ✅ No authentication errors (auto-session created)
- [ ] ✅ All API endpoints return data (not 404/401)
- [ ] ✅ UI loads without login prompts
- [ ] ✅ Database operations work (INSERT/SELECT)
- [ ] ✅ Real-time data updates in H2 console

### **5. Troubleshooting Commands for Claude Agents**

**If application fails to start:**
```bash
# 1. Kill any existing processes
pkill -f java
lsof -ti:8081 | xargs kill -9

# 2. Clean build
./gradlew clean build

# 3. Start fresh
./start-dev.sh
```

**If endpoints return 401/404:**
```bash
# Check application logs for session creation
tail -f app.log | grep -i "development\|session\|auth"
```

**If H2 database not accessible:**
```bash
# Verify H2 console URL and credentials
open http://localhost:8081/h2-console
# Use: jdbc:h2:mem:testdb, sa, password
```

**Port Already in Use:**
```bash
lsof -ti:8081 | xargs kill -9
```

**Build Issues:**
```bash
./gradlew clean build
```

**Clear Gradle Cache:**
```bash
./gradlew clean
rm -rf ~/.gradle/caches/
```

**Reset Everything (Nuclear Option):**
```bash
pkill -f java
lsof -ti:8081 | xargs kill -9
./gradlew clean build
./start-dev.sh
```

**Configuration Issues:**
```bash
# Check development configuration is loaded
grep -i "dev.*profile" app.log
grep -i "auto.session.*true" app.log

# Verify mock session creation
grep -i "mock.*session" app.log
```

### **6. Reporting Protocol**

**When reporting status, Claude agents MUST include:**

1. ✅ **Development mode confirmed** - Port 8081, H2 database
2. ✅ **Authentication bypassed** - Auto-session created in logs
3. ✅ **API endpoints functional** - All return data, not errors
4. ✅ **Database accessible** - H2 console working with test queries
5. ✅ **UI responsive** - Main page loads without authentication

### **7. Example Functional Test Script**

```bash
#!/bin/bash
# Claude agent functional verification script

echo "🤖 Claude Agent - Functional Verification"
echo "========================================="

# 1. Start application
./start-dev.sh &
sleep 15

# 2. Test core endpoints
echo "Testing core endpoints..."
curl -f http://localhost:8081/api/instruments/exchanges || echo "❌ exchanges endpoint failed"
curl -f http://localhost:8081/api/instruments/underlyings || echo "❌ underlyings endpoint failed"
curl -f http://localhost:8081/api/v2/instruments/metadata/exchanges || echo "❌ v2 exchanges endpoint failed"

# 3. Test UI access
echo "Testing UI access..."
curl -I http://localhost:8081/ | head -1 | grep "200 OK" || echo "❌ UI access failed"

# 4. Test H2 database
echo "Testing H2 database..."
curl -f http://localhost:8081/h2-console || echo "❌ H2 console failed"

echo "✅ Functional verification complete"
```

### **8. Environment Configuration**

**Claude agents should verify these environment settings:**
```bash
# These should be set automatically by start-dev.sh
echo "KITE_DEV_AUTO_SESSION: $KITE_DEV_AUTO_SESSION"      # Should be true
echo "KITE_DEV_MOCK_SESSION: $KITE_DEV_MOCK_SESSION"      # Should be true (if no real token)
echo "Server Port: 8081 (dev profile)"
echo "Database: H2 in-memory"
```

**❌ NEVER assume production environment works for Claude agents**
**✅ ALWAYS verify development environment is fully functional**

## Support

For issues and questions:
- Check existing issues in the repository
- Review this documentation
- Create new issue with detailed reproduction steps

---

**Last Updated**: June 2025  
**Version**: 2.0.0  
**Status**: Production Ready (Core Features) - **Unified Configuration Architecture**

## **🔄 Recent Breaking Changes (June 2025)**

### **Configuration Architecture Overhaul**
- **REMOVED**: `application-prod.properties` (merged into unified `application.properties`)
- **REMOVED**: Spring profiles dependency (now uses environment variables)
- **ADDED**: Environment variable-driven configuration with production defaults
- **ADDED**: Integrated development startup in `claude-test.sh --start-dev`
- **UPDATED**: Purpose-based SQL file naming for clarity
- **FIXED**: Authentication system to work without Spring profiles

### **Testing Infrastructure Changes**
- **ENHANCED**: `claude-test.sh` now includes `--start-dev` functionality
- **ADDED**: Isolated test environment to prevent production DB conflicts
- **FIXED**: Essential tests now pass consistently with unified configuration
- **MANDATORY**: All refactoring must include `./claude-test.sh` validation

### **Migration Notes for Existing Deployments**
1. **Update environment variables** to include all required production settings
2. **Remove profile references** from deployment scripts
3. **Use `./start.sh`** for production (handles environment validation)
4. **Test with `./claude-test.sh`** before deploying
5. **Update monitoring** to check unified configuration endpoints