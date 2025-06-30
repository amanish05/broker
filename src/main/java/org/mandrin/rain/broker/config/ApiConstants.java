package org.mandrin.rain.broker.config;

/**
 * Centralized constants for the Broker Online Application
 * This class contains all string literals used throughout the application
 * to ensure consistency and maintainability.
 */
public final class ApiConstants {
    
    // ========================================================================
    // KITE CONNECT API CONSTANTS
    // ========================================================================
    
    // Kite API URLs
    public static final String KITE_BASE_URL = "https://api.kite.trade";
    public static final String KITE_LOGIN_URL = "https://kite.zerodha.com/connect/login?v=3&api_key=%s";
    public static final String HOLDINGS_URL = "https://api.kite.trade/portfolio/holdings";
    public static final String POSITIONS_URL = "https://api.kite.trade/portfolio/positions";
    public static final String ORDER_URL = "https://api.kite.trade/orders/regular";
    public static final String INSTRUMENTS_PATH = "/instruments/";
    
    // Kite API Headers
    public static final String KITE_VERSION_HEADER = "X-Kite-Version";
    public static final String KITE_VERSION = "3";
    public static final String AUTH_HEADER = "Authorization";
    public static final String AUTH_TOKEN_FORMAT = "token %s:%s";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_FORM_DATA = "application/x-www-form-urlencoded";
    
    // ========================================================================
    // APPLICATION PATHS
    // ========================================================================
    
    // Authentication Paths
    public static final String ROOT_PATH = "/";
    public static final String HOME_PATH = "/home";
    public static final String LOGIN_PATH = "/login";
    public static final String LOGOUT_PATH = "/logout";
    public static final String KITE_CALLBACK_PATH = "/kite/callback";
    public static final String ERROR_REDIRECT = "redirect:/error";
    public static final String LOGIN_REDIRECT = "redirect:/login";
    
    // API Base Paths
    public static final String API_BASE_PATH = "/api";
    public static final String API_PATTERN = "/api/**";
    public static final String API_ORDERS_PATH = "/api/orders";
    public static final String API_INSTRUMENTS_PATH = "/api/instruments";
    public static final String API_PORTFOLIO_PATH = "/api/portfolio";
    public static final String API_TICKER_PATH = "/api/ticker";
    
    // Session API Paths
    public static final String API_SESSION_TOKEN_PATH = "/api/session/kite-access-token";
    public static final String API_SESSION_STATUS_PATH = "/api/session/status";
    public static final String API_SESSION_VALIDATE_PATH = "/api/session/validate";
    
    // Static Resource Paths
    public static final String SWAGGER_UI_PATH = "/swagger-ui/**";
    public static final String API_DOCS_PATH = "/v3/api-docs/**";
    public static final String ACTUATOR_HEALTH_PATH = "/actuator/health";
    public static final String JS_RESOURCES_PATH = "/js/**";
    public static final String CSS_RESOURCES_PATH = "/css/**";
    public static final String IMAGES_RESOURCES_PATH = "/images/**";
    
    // ========================================================================
    // TRADING CONSTANTS
    // ========================================================================
    
    // Order Types
    public static final String ORDER_TYPE_MARKET = "MARKET";
    public static final String ORDER_TYPE_LIMIT = "LIMIT";
    public static final String ORDER_TYPE_SL = "SL";
    public static final String ORDER_TYPE_SLM = "SL-M";
    
    // Transaction Types
    public static final String TRANSACTION_TYPE_BUY = "BUY";
    public static final String TRANSACTION_TYPE_SELL = "SELL";
    
    // Product Types
    public static final String PRODUCT_MIS = "MIS";
    public static final String PRODUCT_CNC = "CNC";
    public static final String PRODUCT_NRML = "NRML";
    
    // Instrument Types
    public static final String INSTRUMENT_TYPE_EQ = "EQ";
    public static final String INSTRUMENT_TYPE_CE = "CE";
    public static final String INSTRUMENT_TYPE_PE = "PE";
    public static final String INSTRUMENT_TYPE_FUT = "FUT";
    
    // Segments
    public static final String SEGMENT_EQ = "EQ";
    public static final String SEGMENT_NFO = "NFO";
    
    // Exchanges
    public static final String EXCHANGE_NSE = "NSE";
    public static final String EXCHANGE_BSE = "BSE";
    
    // ========================================================================
    // SESSION CONSTANTS
    // ========================================================================
    
    public static final String KITE_ACCESS_TOKEN_SESSION = "kite_access_token";
    public static final String SESSION_ATTR_USER_ID = "user_id";
    public static final String SESSION_ATTR_API_KEY = "api_key";
    
    // ========================================================================
    // RESPONSE CONSTANTS
    // ========================================================================
    
    // Status Values
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";
    public static final String STATUS_WARNING = "warning";
    public static final String STATUS_CONNECTED = "connected";
    public static final String STATUS_DISCONNECTED = "disconnected";
    public static final String STATUS_SUBSCRIBED = "subscribed";
    
    // Response Keys
    public static final String RESPONSE_KEY_ERROR = "error";
    public static final String RESPONSE_KEY_MESSAGE = "message";
    public static final String RESPONSE_KEY_DATA = "data";
    public static final String RESPONSE_KEY_STATUS = "status";
    public static final String RESPONSE_KEY_AUTHENTICATED = "authenticated";
    public static final String RESPONSE_KEY_VALID = "valid";
    public static final String RESPONSE_KEY_SAVED = "saved";
    public static final String RESPONSE_KEY_TOKENS = "tokens";
    public static final String RESPONSE_KEY_ORDER_ID = "order_id";
    public static final String RESPONSE_KEY_INSTRUMENT_TOKEN = "instrumentToken";
    public static final String RESPONSE_KEY_NAME = "name";
    public static final String RESPONSE_KEY_TIMESTAMP = "timestamp";
    
    // Session Info Keys
    public static final String SESSION_KEY_SESSION_ID = "sessionId";
    public static final String SESSION_KEY_CREATION_TIME = "creationTime";
    public static final String SESSION_KEY_LAST_ACCESSED_TIME = "lastAccessedTime";
    public static final String SESSION_KEY_MAX_INACTIVE_INTERVAL = "maxInactiveInterval";
    public static final String SESSION_KEY_TOKEN_VALID = "tokenValid";
    
    // ========================================================================
    // ERROR MESSAGES
    // ========================================================================
    
    // Authentication Errors
    public static final String NOT_AUTHENTICATED_MSG = "Not authenticated";
    public static final String ERROR_AUTHENTICATION_REQUIRED = "Authentication required";
    public static final String ERROR_PLEASE_LOGIN = "Please login to access this resource";
    public static final String ERROR_INVALID_TOKEN = "Invalid or expired token";
    public static final String ERROR_PLEASE_RELOGIN = "Please re-login to continue";
    public static final String ERROR_TOKEN_REQUIRED = "Token required";
    public static final String ERROR_INVALID_API_CREDENTIALS = "Invalid API credentials";
    
    // Session Errors
    public static final String ERROR_NO_SESSION = "No session found";
    public static final String ERROR_NO_VALID_SESSION = "No valid session or token found";
    public static final String ERROR_NO_ACCESS_TOKEN = "No access token in session";
    public static final String ERROR_SESSION_VALIDATION_FAILED = "Session validation failed - please re-login";
    public static final String ERROR_TOKEN_INVALID_OR_EXPIRED = "Token appears to be invalid or expired";
    
    // Kite API Errors
    public static final String ERROR_KITE_PREFIX = "Kite error: ";
    public static final String ERROR_AUTHENTICATION_FAILED_PREFIX = "Authentication failed: ";
    public static final String ERROR_VALIDATION_INCONCLUSIVE_PREFIX = "Validation inconclusive: ";
    public static final String ERROR_NETWORK_VALIDATION = "Network error during validation";
    public static final String ERROR_VALIDATION_PREFIX = "Validation error: ";
    
    // ========================================================================
    // FORM FIELD NAMES
    // ========================================================================
    
    // Order Form Fields
    public static final String FORM_FIELD_TRADING_SYMBOL = "tradingsymbol";
    public static final String FORM_FIELD_EXCHANGE = "exchange";
    public static final String FORM_FIELD_TRANSACTION_TYPE = "transaction_type";
    public static final String FORM_FIELD_QUANTITY = "quantity";
    public static final String FORM_FIELD_PRICE = "price";
    public static final String FORM_FIELD_ORDER_TYPE = "order_type";
    public static final String FORM_FIELD_PRODUCT = "product";
    public static final String FORM_FIELD_REQUEST_TOKEN = "request_token";
    
    // CSV Column Headers
    public static final String CSV_INSTRUMENT_TOKEN = "instrument_token";
    public static final String CSV_EXCHANGE_TOKEN = "exchange_token";
    public static final String CSV_TRADING_SYMBOL = "tradingsymbol";
    public static final String CSV_NAME = "name";
    public static final String CSV_LAST_PRICE = "last_price";
    public static final String CSV_EXPIRY = "expiry";
    public static final String CSV_STRIKE = "strike";
    public static final String CSV_TICK_SIZE = "tick_size";
    public static final String CSV_LOT_SIZE = "lot_size";
    public static final String CSV_INSTRUMENT_TYPE = "instrument_type";
    public static final String CSV_SEGMENT = "segment";
    public static final String CSV_EXCHANGE = "exchange";
    
    // ========================================================================
    // WEBSOCKET CONSTANTS
    // ========================================================================
    
    // WebSocket Message Types
    public static final String WS_MESSAGE_TYPE = "type";
    public static final String WS_MESSAGE_CONNECTION = "connection";
    public static final String WS_MESSAGE_TICKER = "ticker";
    public static final String WS_MESSAGE_DATA = "data";
    
    // ========================================================================
    // SECURITY CONSTANTS
    // ========================================================================
    
    public static final String HASH_ALGORITHM_SHA256 = "SHA-256";
    
    // ========================================================================
    // VALIDATION CONSTANTS
    // ========================================================================
    
    public static final int DEFAULT_LOT_SIZE = 1;
    public static final int FUTURES_LOT_SIZE = 50;
    public static final double DEFAULT_TICK_SIZE = 0.05;
    public static final int EXCHANGE_TOKEN_DIVISOR = 256;
    
    // ========================================================================
    // CACHE CONSTANTS
    // ========================================================================
    
    public static final long TOKEN_VALIDATION_CACHE_DURATION_MINUTES = 5;
    public static final int MAX_INSTRUMENT_SEARCH_RESULTS = 20;
    public static final int MIN_SEARCH_LENGTH = 2;
    public static final int TOP_INSTRUMENTS_COUNT = 10;
    public static final int POPULAR_OPTIONS_COUNT = 6;
    public static final int POPULAR_EQUITIES_COUNT = 4;
    
    // Private constructor to prevent instantiation
    private ApiConstants() {
        throw new AssertionError("Constants class should not be instantiated");
    }
}
