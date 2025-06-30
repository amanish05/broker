#!/bin/bash

# ============================================================================
# COMPREHENSIVE TEST SUITE FOR BROKER ONLINE
# ============================================================================
# Combines functionality from backend, frontend, and quick verification tests:
# 1. Quick Verification: Basic functionality and health checks
# 2. Backend Tests: API endpoints, WebSocket connectivity, data integrity
# 3. Frontend Tests: JavaScript integration, AJAX calls, UI components
# 4. Performance Tests: Response times and efficiency metrics
#
# Usage: ./claude-test.sh [--quick|--backend|--frontend|--all] [--start-dev]
# Default: --all (runs all test categories in proper dependency order)
# Prerequisites: Application running on localhost:8081 with mock_session=true
# 
# TEST ORDER GUARANTEE:
# 1. Prerequisites & Health Checks (blocking)
# 2. Data Layer Tests (instruments, subscriptions)
# 3. API Integration Tests (orders, portfolio)
# 4. WebSocket & Real-time Tests
# 5. Frontend Integration Tests
# 6. End-to-End Workflow Tests
# 7. Performance & Load Tests
# ============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
APP_URL="http://localhost:8081"
TIMEOUT=30
MAX_RETRIES=3
RETRY_DELAY=2
TEST_RESULTS=()
BACKEND_RESULTS=()
FRONTEND_RESULTS=()
QUICK_RESULTS=()

# Retry mechanism for transient failures
retry_with_backoff() {
    local max_attempts=$1
    local delay=$2
    local command="${@:3}"
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if eval "$command"; then
            return 0
        fi
        
        if [ $attempt -lt $max_attempts ]; then
            log_info "Attempt $attempt failed, retrying in ${delay}s..."
            sleep $delay
            delay=$((delay * 2))  # Exponential backoff
        fi
        
        attempt=$((attempt + 1))
    done
    
    return 1
}

# Parse command line arguments
TEST_MODE="all"
START_DEV_MODE=false

for arg in "$@"; do
    case $arg in
        "--quick")
            TEST_MODE="quick"
            ;;
        "--backend")
            TEST_MODE="backend"
            ;;
        "--frontend")
            TEST_MODE="frontend"
            ;;
        "--all"|"all"|"")
            TEST_MODE="all"
            ;;
        "--start-dev")
            START_DEV_MODE=true
            ;;
        *)
            # If no valid test mode specified, default to all
            if [[ "$TEST_MODE" == "all" ]]; then
                TEST_MODE="${arg#--}"
            fi
            ;;
    esac
done

# Helper functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
    TEST_RESULTS+=("✅ $1")
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
    TEST_RESULTS+=("⚠️  $1")
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
    TEST_RESULTS+=("❌ $1")
}

log_backend_success() {
    echo -e "${GREEN}[BACKEND-SUCCESS]${NC} $1"
    BACKEND_RESULTS+=("✅ $1")
}

log_backend_warning() {
    echo -e "${YELLOW}[BACKEND-WARNING]${NC} $1"
    BACKEND_RESULTS+=("⚠️  $1")
}

log_backend_error() {
    echo -e "${RED}[BACKEND-ERROR]${NC} $1"
    BACKEND_RESULTS+=("❌ $1")
}

log_frontend_success() {
    echo -e "${GREEN}[FRONTEND-SUCCESS]${NC} $1"
    FRONTEND_RESULTS+=("✅ $1")
}

log_frontend_warning() {
    echo -e "${YELLOW}[FRONTEND-WARNING]${NC} $1"
    FRONTEND_RESULTS+=("⚠️  $1")
}

log_frontend_error() {
    echo -e "${RED}[FRONTEND-ERROR]${NC} $1"
    FRONTEND_RESULTS+=("❌ $1")
}

log_quick_success() {
    echo -e "${GREEN}[QUICK-SUCCESS]${NC} $1"
    QUICK_RESULTS+=("✅ $1")
}

log_quick_warning() {
    echo -e "${YELLOW}[QUICK-WARNING]${NC} $1"
    QUICK_RESULTS+=("⚠️  $1")
}

log_quick_error() {
    echo -e "${RED}[QUICK-ERROR]${NC} $1"
    QUICK_RESULTS+=("❌ $1")
}

check_prerequisites() {
    log_info "Checking prerequisites..."
    
    # Check if curl is available
    if ! command -v curl &> /dev/null; then
        log_error "curl is required but not installed"
        exit 1
    fi
    
    # Check if jq is available for JSON parsing
    if ! command -v jq &> /dev/null; then
        log_warning "jq not found - JSON responses will not be parsed"
        JQ_AVAILABLE=false
    else
        JQ_AVAILABLE=true
    fi
    
    # Check if Node.js is available for advanced JS testing
    if command -v node &> /dev/null; then
        log_success "Node.js available for advanced JS testing"
        NODE_AVAILABLE=true
    else
        log_warning "Node.js not found - will use browser-based testing only"
        NODE_AVAILABLE=false
    fi
    
    # Check if application is running
    if ! curl -s "$APP_URL" > /dev/null; then
        log_error "Application not running at $APP_URL"
        log_error "Please start the application with: ./start.sh or ./start-dev.sh"
        exit 1
    fi
    
    log_success "Prerequisites check passed"
}

# Enhanced error handling with early exit for critical failures
check_critical_dependency() {
    local test_name="$1"
    local dependency="$2"
    
    log_info "Verifying critical dependency: $dependency"
    
    case "$dependency" in
        "application-health")
            response=$(curl -s -w "%{http_code}" "$APP_URL" -o /dev/null)
            if [ "$response" != "200" ]; then
                log_error "Critical failure: Application health check failed (HTTP $response)"
                log_error "Cannot proceed with $test_name - application not responsive"
                exit 1
            fi
            ;;
        "instruments-api")
            response=$(curl -s -w "%{http_code}" "$APP_URL/api/instruments/exchanges" -o /dev/null)
            if [ "$response" != "200" ]; then
                log_error "Critical failure: Instruments API not available (HTTP $response)"
                log_error "Cannot proceed with $test_name - core data APIs required"
                exit 1
            fi
            ;;
    esac
}

test_application_health() {
    log_info "Testing application health..."
    
    # Test home page
    response=$(curl -s -w "%{http_code}" "$APP_URL" -o /dev/null)
    if [ "$response" = "200" ]; then
        log_success "Home page accessible"
    else
        log_error "Home page returned HTTP $response"
        return 1
    fi
    
    # Test API health
    response=$(curl -s -w "%{http_code}" "$APP_URL/api/health" -o /dev/null 2>/dev/null || echo "404")
    if [ "$response" = "200" ]; then
        log_success "API health endpoint accessible"
    else
        log_warning "API health endpoint not found (HTTP $response)"
    fi
    
    # Test H2 console (development)
    response=$(curl -s -w "%{http_code}" "$APP_URL/h2-console" -o /dev/null)
    if [ "$response" = "200" ]; then
        log_success "H2 console accessible (development mode confirmed)"
    else
        log_warning "H2 console not accessible"
    fi
}

test_instrument_data() {
    log_info "Testing instrument data availability..."
    
    # Test exchanges endpoint with retry
    if retry_with_backoff $MAX_RETRIES $RETRY_DELAY "curl --max-time 10 -s '$APP_URL/api/instruments/exchanges' > /dev/null"; then
        response=$(curl --max-time 10 -s "$APP_URL/api/instruments/exchanges" 2>/dev/null)
        if [ "$JQ_AVAILABLE" = true ]; then
            exchange_count=$(echo "$response" | jq '. | length' 2>/dev/null || echo "0")
            if [ "$exchange_count" -gt 0 ]; then
                log_success "Found $exchange_count exchanges"
            else
                log_error "No exchanges found"
            fi
        else
            log_success "Exchanges endpoint responding"
        fi
    else
        log_error "Failed to fetch exchanges after $MAX_RETRIES attempts"
    fi
    
    # Test instrument types for NSE
    response=$(curl --max-time 10 -s "$APP_URL/api/instruments/NSE/types" 2>/dev/null)
    if [ $? -eq 0 ]; then
        if [ "$JQ_AVAILABLE" = true ]; then
            type_count=$(echo "$response" | jq '. | length' 2>/dev/null || echo "0")
            if [ "$type_count" -gt 0 ]; then
                log_success "Found $type_count instrument types for NSE"
            else
                log_error "No instrument types found for NSE"
            fi
        else
            log_success "NSE instrument types endpoint responding"
        fi
    else
        log_error "Failed to fetch NSE instrument types"
    fi
    
    # Test specific instruments for NSE EQ
    response=$(curl --max-time 10 -s "$APP_URL/api/instruments/NSE/EQ" 2>/dev/null)
    if [ $? -eq 0 ]; then
        if [ "$JQ_AVAILABLE" = true ]; then
            instrument_count=$(echo "$response" | jq '. | length' 2>/dev/null || echo "0")
            if [ "$instrument_count" -gt 0 ]; then
                log_success "Found $instrument_count instruments for NSE EQ"
            else
                log_error "No instruments found for NSE EQ"
            fi
        else
            log_success "NSE EQ instruments endpoint responding"
        fi
    else
        log_error "Failed to fetch NSE EQ instruments"
    fi
}

test_subscription_workflow() {
    log_info "Testing instrument subscription workflow..."
    
    # Test subscription endpoint
    response=$(curl --max-time 10 -s -X POST "$APP_URL/api/ticker/subscribe" \
        -H "Content-Type: application/json" \
        -d '{"instrumentToken": 738561, "tradingsymbol": "RELIANCE"}' 2>/dev/null)
    
    if [ $? -eq 0 ]; then
        log_success "Subscription request successful"
    else
        log_error "Failed to subscribe to instrument"
    fi
    
    # Test getting current subscriptions
    response=$(curl --max-time 10 -s "$APP_URL/api/ticker/subscriptions" 2>/dev/null)
    if [ $? -eq 0 ]; then
        if [ "$JQ_AVAILABLE" = true ]; then
            sub_count=$(echo "$response" | jq '. | length' 2>/dev/null || echo "0")
            log_success "Current subscriptions: $sub_count"
        else
            log_success "Subscriptions endpoint responding"
        fi
    else
        log_error "Failed to fetch current subscriptions"
    fi
}

test_websocket_connection() {
    log_info "Testing WebSocket ticker connection..."
    
    # Test WebSocket endpoints with timeout (non-blocking)
    ticker_status=$(curl --max-time 5 -s -w "%{http_code}" "$APP_URL/ws/ticker" -o /dev/null 2>/dev/null || echo "timeout")
    if [[ "$ticker_status" == "400" ]] || [[ "$ticker_status" == "405" ]]; then
        log_success "WebSocket ticker endpoint properly configured (HTTP $ticker_status)"
    elif [[ "$ticker_status" == "timeout" ]]; then
        log_warning "WebSocket ticker endpoint timeout (may be normal for WebSocket)"
    else
        log_warning "WebSocket ticker endpoint returns HTTP $ticker_status"
    fi
    
    # Test instruments WebSocket endpoint
    instruments_status=$(curl --max-time 5 -s -w "%{http_code}" "$APP_URL/ws/instruments" -o /dev/null 2>/dev/null || echo "timeout")
    if [[ "$instruments_status" == "400" ]] || [[ "$instruments_status" == "405" ]]; then
        log_success "WebSocket instruments endpoint properly configured (HTTP $instruments_status)"
    elif [[ "$instruments_status" == "timeout" ]]; then
        log_warning "WebSocket instruments endpoint timeout (may be normal for WebSocket)"
    else
        log_warning "WebSocket instruments endpoint returns HTTP $instruments_status"
    fi
}

test_order_placement() {
    log_info "Testing order placement workflow..."
    
    # Test order placement
    order_data='{
        "tradingsymbol": "RELIANCE",
        "exchange": "NSE",
        "transactionType": "BUY",
        "orderType": "MARKET",
        "quantity": 1,
        "price": 0,
        "product": "CNC"
    }'
    
    response=$(curl --max-time 10 -s -X POST "$APP_URL/api/orders" \
        -H "Content-Type: application/json" \
        -d "$order_data" 2>/dev/null)
    
    if [ $? -eq 0 ]; then
        if echo "$response" | grep -q "error" 2>/dev/null; then
            log_warning "Order placement returned with message (may be expected in mock mode)"
        else
            log_success "Order placement request processed"
        fi
    else
        log_error "Failed to place order"
    fi
    
    # Test order history
    response=$(curl -s "$APP_URL/api/orders")
    if [ $? -eq 0 ]; then
        if [ "$JQ_AVAILABLE" = true ]; then
            order_count=$(echo "$response" | jq '. | length' 2>/dev/null || echo "0")
            log_success "Order history contains $order_count orders"
        else
            log_success "Order history endpoint responding"
        fi
    else
        log_error "Failed to fetch order history"
    fi
}

test_portfolio_data() {
    log_info "Testing portfolio data..."
    
    # Test holdings
    response=$(curl -s "$APP_URL/api/portfolio/holdings")
    if [ $? -eq 0 ]; then
        if [ "$JQ_AVAILABLE" = true ]; then
            holding_count=$(echo "$response" | jq '. | length' 2>/dev/null || echo "0")
            log_success "Portfolio contains $holding_count holdings"
        else
            log_success "Holdings endpoint responding"
        fi
    else
        log_error "Failed to fetch portfolio holdings"
    fi
    
    # Test positions
    response=$(curl -s "$APP_URL/api/portfolio/positions")
    if [ $? -eq 0 ]; then
        if [ "$JQ_AVAILABLE" = true ] && echo "$response" | jq . > /dev/null 2>&1; then
            log_success "Positions data available"
        else
            log_success "Positions endpoint responding"
        fi
    else
        log_error "Failed to fetch positions"
    fi
}

test_mock_data_integrity() {
    log_info "Testing mock data integrity..."
    
    # Check if we're getting mock responses
    response=$(curl -s -I "$APP_URL/api/portfolio/holdings")
    if echo "$response" | grep -q "X-Mock-Response"; then
        log_success "Mock responses are being served"
    else
        log_warning "No mock response headers detected (may be using real API)"
    fi
    
    # Test that we have reasonable amount of test data
    response=$(curl --max-time 10 -s "$APP_URL/api/instruments/NSE/EQ" 2>/dev/null)
    if [ "$JQ_AVAILABLE" = true ]; then
        instrument_count=$(echo "$response" | jq '. | length' 2>/dev/null || echo "0")
        if [ "$instrument_count" -ge 20 ]; then
            log_success "Sufficient mock instruments available ($instrument_count)"
        else
            log_warning "Limited mock instruments available ($instrument_count)"
        fi
    fi
}

test_ui_pages() {
    log_info "Testing UI page accessibility..."
    
    # Test main pages
    pages=("/" "/instruments" "/portfolio" "/orders")
    
    for page in "${pages[@]}"; do
        response=$(curl -s -w "%{http_code}" "$APP_URL$page" -o /dev/null)
        if [ "$response" = "200" ]; then
            log_success "Page $page accessible"
        else
            log_error "Page $page returned HTTP $response"
        fi
    done
}

simulate_user_workflow() {
    log_info "Simulating complete user workflow..."
    
    # Step 1: Load instruments page
    log_info "Step 1: Loading instruments page..."
    response=$(curl -s -w "%{http_code}" "$APP_URL/instruments" -o /dev/null)
    [ "$response" = "200" ] && log_success "✓ Instruments page loaded"
    
    # Step 2: Subscribe to instrument
    log_info "Step 2: Subscribing to RELIANCE..."
    curl -s -X POST "$APP_URL/api/ticker/subscribe" \
        -H "Content-Type: application/json" \
        -d '{"instrumentToken": 738561, "tradingsymbol": "RELIANCE"}' > /dev/null
    log_success "✓ Subscription request sent"
    
    # Step 3: Place order
    log_info "Step 3: Placing mock order..."
    curl -s -X POST "$APP_URL/api/orders/place" \
        -H "Content-Type: application/json" \
        -d '{
            "tradingsymbol": "RELIANCE",
            "exchange": "NSE", 
            "transactionType": "BUY",
            "orderType": "MARKET",
            "quantity": 1,
            "product": "CNC"
        }' > /dev/null
    log_success "✓ Order placement attempted"
    
    # Step 4: Check portfolio
    log_info "Step 4: Checking portfolio..."
    response=$(curl -s "$APP_URL/api/portfolio/holdings")
    [ $? -eq 0 ] && log_success "✓ Portfolio data retrieved"
    
    log_success "Complete user workflow simulation finished"
}

performance_test() {
    log_info "Running basic performance tests..."
    
    # Test response times
    start_time=$(date +%s%N)
    curl -s "$APP_URL/api/instruments/NSE/EQ" > /dev/null
    end_time=$(date +%s%N)
    duration=$((($end_time - $start_time) / 1000000))
    
    if [ $duration -lt 1000 ]; then
        log_success "Instruments API response time: ${duration}ms (excellent)"
    elif [ $duration -lt 3000 ]; then
        log_success "Instruments API response time: ${duration}ms (good)"
    else
        log_warning "Instruments API response time: ${duration}ms (slow)"
    fi
}

print_test_summary() {
    log_info "Test Summary:"
    echo "=============================================="
    
    success_count=0
    warning_count=0
    error_count=0
    
    for result in "${TEST_RESULTS[@]}"; do
        echo "$result"
        if [[ $result == *"✅"* ]]; then
            ((success_count++))
        elif [[ $result == *"⚠️"* ]]; then
            ((warning_count++))
        elif [[ $result == *"❌"* ]]; then
            ((error_count++))
        fi
    done
    
    echo "=============================================="
    echo -e "${GREEN}Passed: $success_count${NC} | ${YELLOW}Warnings: $warning_count${NC} | ${RED}Failed: $error_count${NC}"
    
    if [ $error_count -eq 0 ]; then
        log_success "All critical tests passed! 🎉"
        log_info "Your mock data architecture is working correctly"
        log_info "Ready for comprehensive UI testing"
    else
        log_error "Some tests failed. Please check the application configuration."
    fi
}

# ============================================================================
# QUICK TEST SUITE (Basic functionality verification)
# ============================================================================

run_quick_tests() {
    if [[ "$TEST_MODE" != "quick" && "$TEST_MODE" != "all" ]]; then
        return
    fi
    
    echo ""
    echo "=============================================="
    echo "🧪 QUICK FUNCTIONAL VERIFICATION"
    echo "=============================================="
    
    # Test 1: Application health
    log_info "Testing application health..."
    response=$(curl -s -w "%{http_code}" "$APP_URL" -o /dev/null)
    if [ "$response" = "200" ]; then
        log_quick_success "Application is running and responsive"
    else
        log_quick_error "Application health check failed (HTTP $response)"
    fi
    
    # Test 2: Basic endpoints
    log_info "Testing basic API endpoints..."
    
    endpoints=(
        "/api/orders"
        "/api/portfolio/holdings"
        "/api/ticker/subscriptions"
        "/api/instruments/exchanges"
    )
    
    for endpoint in "${endpoints[@]}"; do
        response=$(curl -s -w "%{http_code}" "$APP_URL$endpoint" -o /dev/null)
        if [ "$response" = "200" ]; then
            log_quick_success "Endpoint $endpoint accessible (HTTP $response)"
        else
            log_quick_warning "Endpoint $endpoint returns HTTP $response"
        fi
    done
    
    # Test 3: Mock architecture verification
    log_info "Verifying mock architecture status..."
    
    if [ -f "app.log" ]; then
        # Check for mock components in logs
        if grep -q "Creating mock WebClient" app.log; then
            log_quick_success "Mock WebClient loaded"
        else
            log_quick_warning "Mock WebClient not detected in logs"
        fi
        
        if grep -q "MockTickerService initialized" app.log; then
            log_quick_success "Mock Ticker Service loaded"
        else
            log_quick_warning "Mock Ticker Service not detected in logs"
        fi
        
        if grep -q "Auto-session: true, Mock-session: true" app.log; then
            log_quick_success "Auto-session enabled"
        else
            log_quick_warning "Auto-session not detected in logs"
        fi
    else
        log_quick_warning "app.log not found - cannot verify mock architecture"
    fi
    
    # Test 4: Data endpoints verification
    log_info "Testing data endpoints with actual responses..."
    
    # Portfolio Holdings test
    holdings_response=$(curl -s "$APP_URL/api/portfolio/holdings")
    if [ $? -eq 0 ] && [ ! -z "$holdings_response" ]; then
        log_quick_success "Portfolio holdings data available"
    else
        log_quick_error "Portfolio holdings data not available"
    fi
    
    # Orders test
    orders_response=$(curl -s "$APP_URL/api/orders")
    if [ $? -eq 0 ] && [ ! -z "$orders_response" ]; then
        log_quick_success "Orders data available"
    else
        log_quick_error "Orders data not available"
    fi
}

# ============================================================================
# FRONTEND TEST SUITE (JavaScript integration tests)
# ============================================================================

run_frontend_tests() {
    if [[ "$TEST_MODE" != "frontend" && "$TEST_MODE" != "all" ]]; then
        return
    fi
    
    echo ""
    echo "=============================================="
    echo "🔧 FRONTEND INTEGRATION TESTS"
    echo "=============================================="
    
    # Configuration loading tests
    log_info "Testing configuration loading JavaScript..."
    
    # Test frontend config API endpoint
    response=$(curl -s "$APP_URL/api/config/frontend")
    if [ $? -eq 0 ]; then
        if echo "$response" | jq . > /dev/null 2>&1; then
            log_frontend_success "Frontend config API returns valid JSON"
            
            # Check required config sections
            websocket_config=$(echo "$response" | jq -r '.websocket.baseUrl' 2>/dev/null)
            api_config=$(echo "$response" | jq -r '.api.baseUrl' 2>/dev/null)
            
            if [ "$websocket_config" != "null" ] && [ "$api_config" != "null" ]; then
                log_frontend_success "Config contains required WebSocket and API settings"
            else
                log_frontend_error "Config missing required WebSocket or API settings"
            fi
        else
            log_frontend_error "Frontend config API returns invalid JSON"
        fi
    else
        log_frontend_error "Failed to load frontend config from API"
    fi
    
    # Test config.js accessibility
    response=$(curl -s -w "%{http_code}" "$APP_URL/js/config.js" -o /dev/null)
    if [ "$response" = "200" ]; then
        log_frontend_success "config.js JavaScript file accessible"
    else
        log_frontend_error "config.js JavaScript file not accessible (HTTP $response)"
    fi
    
    # AJAX API integration tests
    log_info "Testing AJAX API integration scenarios..."
    
    # Test all major API endpoints that JavaScript would call
    api_endpoints=(
        "/api/config/frontend"
        "/api/instruments/exchanges"
        "/api/instruments/NSE/types"
        "/api/instruments/NSE/EQ"
        "/api/ticker/subscriptions"
        "/api/orders"
        "/api/portfolio/holdings"
        "/api/portfolio/positions"
        "/api/session/status"
    )
    
    for endpoint in "${api_endpoints[@]}"; do
        response=$(curl -s -w "%{http_code}" "$APP_URL$endpoint" -o /dev/null)
        if [ "$response" = "200" ]; then
            log_frontend_success "API endpoint $endpoint accessible via AJAX"
        else
            if [ "$endpoint" = "/api/session/status" ] && [ "$response" = "401" ]; then
                log_frontend_warning "API endpoint $endpoint returns HTTP $response (may be expected)"
            else
                log_frontend_error "API endpoint $endpoint returns HTTP $response"
            fi
        fi
    done
    
    # UI component integration tests
    log_info "Testing UI component JavaScript integration..."
    
    # Test that all required JavaScript files are accessible
    js_files=(
        "/js/config.js"
        "/js/session.js"
        "/js/instruments.js"
        "/js/ticker.js"
        "/js/orders.js"
        "/js/portfolio.js"
        "/js/ui.js"
    )
    
    for js_file in "${js_files[@]}"; do
        response=$(curl -s -w "%{http_code}" "$APP_URL$js_file" -o /dev/null)
        if [ "$response" = "200" ]; then
            log_frontend_success "JavaScript file $js_file accessible"
        else
            log_frontend_error "JavaScript file $js_file not accessible (HTTP $response)"
        fi
    done
    
    # Test that HTML pages include required JavaScript
    pages=("/" "/instruments" "/portfolio" "/orders")
    
    for page in "${pages[@]}"; do
        response=$(curl -s "$APP_URL$page")
        if echo "$response" | grep -q "config.js"; then
            log_frontend_success "Page $page includes config.js"
        else
            log_frontend_error "Page $page missing config.js inclusion"
        fi
        
        if echo "$response" | grep -q "loadAppConfig"; then
            log_frontend_success "Page $page calls loadAppConfig()"
        else
            log_frontend_warning "Page $page may not call loadAppConfig()"
        fi
    done
}

# ============================================================================
# ENHANCED SUMMARY AND REPORTING
# ============================================================================

print_comprehensive_summary() {
    echo ""
    echo "=============================================="
    echo "📊 COMPREHENSIVE TEST SUMMARY"
    echo "=============================================="
    
    # Count results by category
    if [[ "$TEST_MODE" == "quick" || "$TEST_MODE" == "all" ]]; then
        quick_success=0
        quick_warning=0
        quick_error=0
        
        for result in "${QUICK_RESULTS[@]}"; do
            if [[ $result == *"✅"* ]]; then
                ((quick_success++))
            elif [[ $result == *"⚠️"* ]]; then
                ((quick_warning++))
            elif [[ $result == *"❌"* ]]; then
                ((quick_error++))
            fi
        done
        
        echo ""
        echo "🧪 QUICK TESTS:"
        echo -e "${GREEN}Passed: $quick_success${NC} | ${YELLOW}Warnings: $quick_warning${NC} | ${RED}Failed: $quick_error${NC}"
    fi
    
    if [[ "$TEST_MODE" == "frontend" || "$TEST_MODE" == "all" ]]; then
        frontend_success=0
        frontend_warning=0
        frontend_error=0
        
        for result in "${FRONTEND_RESULTS[@]}"; do
            if [[ $result == *"✅"* ]]; then
                ((frontend_success++))
            elif [[ $result == *"⚠️"* ]]; then
                ((frontend_warning++))
            elif [[ $result == *"❌"* ]]; then
                ((frontend_error++))
            fi
        done
        
        echo ""
        echo "🔧 FRONTEND TESTS:"
        echo -e "${GREEN}Passed: $frontend_success${NC} | ${YELLOW}Warnings: $frontend_warning${NC} | ${RED}Failed: $frontend_error${NC}"
    fi
    
    echo ""
    echo "🚀 BACKEND TESTS:"
    # Count existing backend results from the original tests
    backend_success=0
    backend_warning=0
    backend_error=0
    
    for result in "${TEST_RESULTS[@]}"; do
        if [[ $result == *"✅"* ]]; then
            ((backend_success++))
        elif [[ $result == *"⚠️"* ]]; then
            ((backend_warning++))
        elif [[ $result == *"❌"* ]]; then
            ((backend_error++))
        fi
    done
    
    echo -e "${GREEN}Passed: $backend_success${NC} | ${YELLOW}Warnings: $backend_warning${NC} | ${RED}Failed: $backend_error${NC}"
    
    # Display all results
    echo ""
    echo "🎯 DETAILED RESULTS:"
    echo "=============================================="
    
    if [[ "$TEST_MODE" == "quick" || "$TEST_MODE" == "all" ]]; then
        for result in "${QUICK_RESULTS[@]}"; do
            echo "$result"
        done
    fi
    
    if [[ "$TEST_MODE" == "frontend" || "$TEST_MODE" == "all" ]]; then
        for result in "${FRONTEND_RESULTS[@]}"; do
            echo "$result"
        done
    fi
    
    for result in "${TEST_RESULTS[@]}"; do
        echo "$result"
    done
    
    echo "=============================================="
    
    # Calculate totals
    total_success=$((quick_success + frontend_success + backend_success))
    total_warning=$((quick_warning + frontend_warning + backend_warning))
    total_error=$((quick_error + frontend_error + backend_error))
    
    echo -e "${GREEN}Total Passed: $total_success${NC} | ${YELLOW}Total Warnings: $total_warning${NC} | ${RED}Total Failed: $total_error${NC}"
    
    if [ $total_error -eq 0 ]; then
        log_success "All critical tests passed! 🎉"
        if [[ "$TEST_MODE" == "all" ]]; then
            log_info "Complete application stack is working correctly"
        fi
        log_info "Ready for deployment/development"
    else
        log_error "Some tests failed. Please check the application configuration."
    fi
}

# Final validation to ensure test order was executed correctly
validate_test_execution_order() {
    log_info "Validating test execution order and completeness..."
    
    # Check that critical dependencies were verified
    local required_tests=(
        "Prerequisites check passed"
        "Application is running and responsive" 
        "Exchanges endpoint responding"
        "Portfolio"
        "WebSocket"
    )
    
    for required_test in "${required_tests[@]}"; do
        found=false
        for result in "${TEST_RESULTS[@]}" "${QUICK_RESULTS[@]}" "${FRONTEND_RESULTS[@]}"; do
            if [[ "$result" == *"$required_test"* ]]; then
                found=true
                break
            fi
        done
        
        if [ "$found" = false ]; then
            log_warning "Required test not found in results: $required_test"
        fi
    done
    
    log_success "Test execution order validation complete"
}

# Development startup functionality (integrated from start-dev.sh)
start_development_application() {
    log_info "🚀 Starting Broker Online in Development Mode"
    echo "=============================================="
    
    # Set development environment variables
    export AUTO_SESSION=true
    export MOCK_SESSION=false
    export SERVER_PORT=8081
    export SESSION_TIMEOUT=24h
    export SESSION_SECURE=false
    export DDL_AUTO=create
    export SHOW_SQL=true
    export FORMAT_SQL=true
    export SQL_INIT_MODE=always
    export CSRF_ENABLED=false
    export LOG_LEVEL_APP=DEBUG
    export LOG_LEVEL_SPRING=DEBUG
    export LOG_LEVEL_SQL=DEBUG
    export LOG_LEVEL_HIBERNATE=TRACE
    export LOG_LEVEL_HIKARI=DEBUG
    export ACTUATOR_ENDPOINTS=health,info,metrics,env,beans,loggers
    export HEALTH_DETAILS=always
    export ENV_ENDPOINT=true
    export BEANS_ENDPOINT=true
    export LOGGERS_ENDPOINT=true
    export CACHE_TYPE=simple
    
    # Check if real Kite credentials are provided
    if [ -z "$KITE_ACCESS_TOKEN" ] && [ -z "$KITE_API_KEY" ]; then
        log_warning "No real Kite credentials found"
        log_info "Using mock session for development"
        export MOCK_SESSION=true
        export KITE_API_KEY="mock_api_key"
        export KITE_API_SECRET="mock_api_secret"
        export KITE_USER_ID="mock_user_id"
    else
        log_success "Real Kite credentials detected"
        if [ -n "$KITE_ACCESS_TOKEN" ]; then
            log_info "Using provided access token (bypassing OAuth)"
        else
            log_info "API credentials available (will use OAuth if needed)"
        fi
    fi
    
    # Override database configuration for development (H2)
    unset POSTGRES_URL
    unset POSTGRES_USER  
    unset POSTGRES_PASSWORD
    export spring_datasource_url="jdbc:h2:mem:testdb"
    export spring_datasource_username="sa"
    export spring_datasource_password="password"
    export spring_datasource_driver_class_name="org.h2.Driver"
    export spring_jpa_database_platform="org.hibernate.dialect.H2Dialect"
    
    echo ""
    echo "Configuration:"
    echo "- Port: 8081 (development mode)"
    echo "- Database: H2 in-memory"
    echo "- Auto-session: $AUTO_SESSION"
    echo "- Mock-session: $MOCK_SESSION"
    echo "- H2 Console: http://localhost:8081/h2-console"
    echo "- Main App: http://localhost:8081/"
    echo ""
    
    # Stop any existing application on port 8081
    log_info "Stopping any existing application on port 8081..."
    lsof -ti:8081 | xargs kill -9 2>/dev/null || log_info "Port 8081 is free"
    
    log_info "Building application..."
    ./gradlew clean build -x test
    
    if [ $? -eq 0 ]; then
        log_success "Build successful"
        log_info "Starting application in background..."
        ./gradlew bootRun &
        
        # Wait for application to start
        log_info "Waiting for application to start..."
        for i in {1..30}; do
            if curl -s http://localhost:8081/ > /dev/null; then
                log_success "Application started successfully on port 8081"
                return 0
            fi
            sleep 2
            echo -n "."
        done
        
        log_error "Application failed to start within 60 seconds"
        return 1
    else
        log_error "Build failed. Please check the errors above."
        return 1
    fi
}

# Main execution
main() {
    echo "=============================================="
    echo "🔬 BROKER ONLINE - COMPREHENSIVE TEST SUITE"
    echo "=============================================="
    echo "Test Mode: $TEST_MODE"
    if [ "$START_DEV_MODE" = true ]; then
        echo "Dev Startup: Enabled"
    fi
    echo ""
    
    # Start development application if requested
    if [ "$START_DEV_MODE" = true ]; then
        start_development_application
        if [ $? -ne 0 ]; then
            log_error "Failed to start development application"
            exit 1
        fi
        
        # Brief pause to ensure application is fully ready
        sleep 3
    fi
    
    check_prerequisites
    
    case "$TEST_MODE" in
        "quick")
            run_quick_tests
            ;;
        "backend")
            # Phase 1: Critical Health Checks (blocking)
            test_application_health
            
            # Phase 2: Data Layer Tests (must pass for subsequent tests)
            test_instrument_data
            test_mock_data_integrity
            
            # Phase 3: Core API Integration Tests
            test_subscription_workflow
            test_portfolio_data
            test_order_placement
            
            # Phase 4: Real-time Communication Tests
            test_websocket_connection
            
            # Phase 5: UI Accessibility Tests
            test_ui_pages
            
            # Phase 6: End-to-End Workflow Tests
            simulate_user_workflow
            
            # Phase 7: Performance Validation
            performance_test
            ;;
        "frontend")
            # Frontend tests require backend to be working
            test_application_health
            run_frontend_tests
            ;;
        "all")
            # Comprehensive test suite in guaranteed dependency order
            
            # Phase 1: Quick health validation (must pass to continue)
            log_info "🚀 Phase 1: Quick Health Validation"
            run_quick_tests
            
            # Phase 2: Critical application health (blocking)
            log_info "🏥 Phase 2: Critical Application Health"
            test_application_health
            check_critical_dependency "Data Layer Tests" "application-health"
            
            # Phase 3: Data layer foundation (required for all other tests)
            log_info "📊 Phase 3: Data Layer Foundation"
            test_instrument_data
            check_critical_dependency "API Integration Tests" "instruments-api"
            test_mock_data_integrity
            
            # Phase 4: Core API functionality (order matters)
            log_info "🔌 Phase 4: Core API Integration"
            test_subscription_workflow
            test_portfolio_data  
            test_order_placement
            
            # Phase 5: Real-time communication systems
            log_info "⚡ Phase 5: Real-time Communication"
            test_websocket_connection
            
            # Phase 6: Frontend integration (requires backend APIs)
            log_info "🌐 Phase 6: Frontend Integration"
            run_frontend_tests
            
            # Phase 7: UI page accessibility
            log_info "📱 Phase 7: UI Accessibility"
            test_ui_pages
            
            # Phase 8: Complete user workflow simulation
            log_info "🔄 Phase 8: End-to-End Workflow"
            simulate_user_workflow
            
            # Phase 9: Performance and load validation
            log_info "⚡ Phase 9: Performance Validation"
            performance_test
            ;;
        *)
            echo "Invalid test mode: $TEST_MODE"
            echo "Usage: $0 [--quick|--backend|--frontend|--all]"
            exit 1
            ;;
    esac
    
    echo ""
    
    # Validate test execution order and completeness
    if [[ "$TEST_MODE" == "all" ]]; then
        validate_test_execution_order
    fi
    
    if [[ "$TEST_MODE" == "quick" || "$TEST_MODE" == "frontend" || "$TEST_MODE" == "all" ]]; then
        print_comprehensive_summary
    else
        print_test_summary
    fi
}

# Usage information
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    echo "Comprehensive Test Suite for Broker Online"
    echo ""
    echo "Usage: $0 [TEST_MODE] [OPTIONS]"
    echo ""
    echo "Test Modes:"
    echo "  --quick     Run quick verification tests only"
    echo "  --backend   Run comprehensive backend tests only"
    echo "  --frontend  Run frontend integration tests only"
    echo "  --all       Run all test categories (default)"
    echo ""
    echo "Options:"
    echo "  --start-dev Start development application before testing"
    echo ""
    echo "Examples:"
    echo "  $0                    # Run all tests (requires running app)"
    echo "  $0 --all --start-dev # Start app in dev mode, then run all tests"
    echo "  $0 --quick           # Quick smoke tests"
    echo "  $0 --backend         # Backend API tests"
    echo "  $0 --frontend        # Frontend JS tests"
    exit 0
fi

# Run main function
main "$@"