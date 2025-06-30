#!/bin/bash

# ============================================================================
# PRODUCTION STARTUP SCRIPT FOR BROKER ONLINE
# ============================================================================
# This script starts the Broker Online application in production mode
# with proper environment variable validation and error handling.
#
# Usage: ./start.sh
# Prerequisites: 
#   - PostgreSQL database running and accessible
#   - All required environment variables set
#   - Valid Kite Connect API credentials
# ============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Helper functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
    exit 1
}

# Load environment variables from .env if it exists
load_environment() {
    if [ -f .env ]; then
        log_info "Loading environment variables from .env file..."
        export $(grep -v '^#' .env | xargs)
        log_success "Environment variables loaded from .env"
    fi
}

# Production environment validation
check_production_environment() {
    log_info "Validating production environment..."
    
    # Required environment variables
    required_vars=(
        "KITE_API_KEY"
        "KITE_API_SECRET" 
        "KITE_USER_ID"
        "POSTGRES_URL"
        "POSTGRES_USER"
        "POSTGRES_PASSWORD"
    )
    
    missing_vars=()
    for var in "${required_vars[@]}"; do
        if [ -z "${!var}" ]; then
            missing_vars+=("$var")
        fi
    done
    
    if [ ${#missing_vars[@]} -ne 0 ]; then
        log_error "Missing required environment variables: ${missing_vars[*]}"
        echo ""
        echo "Please set the following environment variables:"
        for var in "${missing_vars[@]}"; do
            echo "  export $var=your_value_here"
        done
        echo ""
        echo "Or create a .env file with:"
        for var in "${missing_vars[@]}"; do
            echo "  $var=your_value_here"
        done
        echo ""
        echo "Example:"
        echo "  KITE_API_KEY=your_zerodha_api_key"
        echo "  KITE_API_SECRET=your_zerodha_api_secret"
        echo "  KITE_USER_ID=your_zerodha_user_id"
        echo "  POSTGRES_URL=jdbc:postgresql://localhost:5432/broker"
        echo "  POSTGRES_USER=your_db_user"
        echo "  POSTGRES_PASSWORD=your_db_password"
        exit 1
    fi
    
    log_success "All required environment variables are set"
}

# Database connectivity check
check_database_connectivity() {
    log_info "Checking database connectivity..."
    
    # Extract database details from POSTGRES_URL
    if [[ $POSTGRES_URL =~ jdbc:postgresql://([^:]+):([0-9]+)/(.+) ]]; then
        db_host="${BASH_REMATCH[1]}"
        db_port="${BASH_REMATCH[2]}"
        db_name="${BASH_REMATCH[3]}"
    else
        log_error "Invalid POSTGRES_URL format. Expected: jdbc:postgresql://host:port/database"
    fi
    
    # Test database connection using pg_isready if available
    if command -v pg_isready &> /dev/null; then
        if pg_isready -h "$db_host" -p "$db_port" -d "$db_name" -U "$POSTGRES_USER" &> /dev/null; then
            log_success "Database is accessible"
        else
            log_error "Cannot connect to database at $db_host:$db_port/$db_name"
        fi
    else
        log_warning "pg_isready not available - skipping database connectivity check"
    fi
}

# Build application
build_application() {
    log_info "Building application for production..."
    
    # Clean and build
    ./gradlew clean build -x test
    
    if [ $? -eq 0 ]; then
        log_success "Application built successfully"
    else
        log_error "Application build failed"
    fi
}

# Run essential tests
run_essential_tests() {
    log_info "Running essential service tests..."
    
    # Run tests with clean environment (unset production database variables)
    # This ensures tests use H2 test database instead of production PostgreSQL
    env -u POSTGRES_URL -u POSTGRES_USER -u POSTGRES_PASSWORD ./gradlew test --tests "*ServiceTest" --tests "BrokerApplicationTests"
    
    if [ $? -eq 0 ]; then
        log_success "Essential tests passed"
    else
        log_error "Essential tests failed - cannot start production"
    fi
}

# Start application
start_application() {
    log_info "Starting Broker Online in production mode..."
    
    echo "=============================================="
    echo "🚀 Starting Broker Online - PRODUCTION MODE"
    echo "=============================================="
    echo "Configuration:"
    echo "- Profile: prod"
    echo "- Port: ${SERVER_PORT:-8080}"
    echo "- Database: PostgreSQL"
    echo "- Environment: Production"
    echo "- CSRF: Enabled"
    echo "- Session Timeout: 1 hour"
    echo ""
    echo "🔗 Application will be available at:"
    echo "   Main App: http://localhost:${SERVER_PORT:-8080}/"
    echo "   Health Check: http://localhost:${SERVER_PORT:-8080}/actuator/health"
    echo ""
    
    # Set production environment variables for unified configuration
    export SERVER_PORT=${SERVER_PORT:-8080}
    export SESSION_TIMEOUT=1h
    export SESSION_SECURE=true
    export DDL_AUTO=validate
    export SHOW_SQL=false
    export FORMAT_SQL=false
    export SQL_INIT_MODE=never
    export AUTO_SESSION=false
    export MOCK_SESSION=false
    export CSRF_ENABLED=true
    export LOG_LEVEL_APP=INFO
    export LOG_LEVEL_SPRING=WARN
    export LOG_LEVEL_SQL=WARN
    export LOG_LEVEL_HIBERNATE=WARN
    export LOG_LEVEL_HIKARI=WARN
    export ACTUATOR_ENDPOINTS=health,info,metrics
    export HEALTH_DETAILS=when-authorized
    export ENV_ENDPOINT=false
    export BEANS_ENDPOINT=false
    export LOGGERS_ENDPOINT=false
    export CACHE_TYPE=redis
    
    # Start with unified configuration (no profile needed)
    ./gradlew bootRun
}

# Main execution
main() {
    echo "=============================================="
    echo "🏭 BROKER ONLINE - PRODUCTION STARTUP"
    echo "=============================================="
    echo ""
    
    load_environment
    check_production_environment
    check_database_connectivity
    build_application
    run_essential_tests
    start_application
}

# Trap signals and cleanup
cleanup() {
    log_info "Shutting down application..."
    exit 0
}

trap cleanup SIGINT SIGTERM

# Run main function
main "$@"
