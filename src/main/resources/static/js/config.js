// config.js
// Frontend configuration loader that loads from JSON file during HTML load
let APP_CONFIG = null;

/**
 * Load configuration from API endpoint driven by application properties
 * This should be called during HTML page load before other scripts
 */
async function loadAppConfig() {
    try {
        const response = await fetch('/api/config/frontend');
        if (!response.ok) {
            throw new Error(`Failed to load config: ${response.status}`);
        }
        APP_CONFIG = await response.json();
        
        // Dynamic WebSocket URL based on current location
        if (APP_CONFIG.websocket.baseUrl.includes('localhost')) {
            const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
            APP_CONFIG.websocket.baseUrl = `${protocol}//${window.location.host}`;
        }
        
        // Dynamic API base URL (empty means current host)
        if (!APP_CONFIG.api.baseUrl) {
            APP_CONFIG.api.baseUrl = window.location.origin;
        }
        
        console.log('App configuration loaded:', APP_CONFIG);
        return APP_CONFIG;
    } catch (error) {
        console.error('Failed to load app configuration:', error);
        // Fallback configuration
        APP_CONFIG = {
            websocket: {
                baseUrl: `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}`,
                endpoints: {
                    ticker: '/ws/ticker',
                    instruments: '/ws/instruments'
                }
            },
            api: {
                baseUrl: window.location.origin,
                timeout: 30000
            },
            session: {
                checkFrequency: 300000,
                criticalCheckFrequency: 30000,
                redirectDelay: 2000,
                throttleDelay: 30000
            },
            data: {
                topInstrumentsLimit: 10,
                searchResultsLimit: 20,
                filteredResultsLimit: 50,
                refreshDelay: 1000
            },
            orders: {
                largeQuantityThreshold: 1000,
                messages: {
                    success: "Order placed successfully!",
                    error: "Order placement failed. Please try again."
                }
            },
            instruments: {
                defaultTypes: ["Options"],
                popularInstruments: ["RELIANCE", "TCS", "INFY", "HDFC", "ICICIBANK"],
                popularKeywords: ["NIFTY", "BANKNIFTY"]
            },
            exchanges: {
                defaults: ["NSE", "BSE"],
                primary: "NSE"
            }
        };
        return APP_CONFIG;
    }
}

/**
 * Get configuration value with fallback
 * @param {string} path - Dot notation path like 'websocket.baseUrl'
 * @param {*} fallback - Fallback value if not found
 */
function getConfig(path, fallback = null) {
    if (!APP_CONFIG) {
        console.warn('App configuration not loaded yet');
        return fallback;
    }
    
    const keys = path.split('.');
    let value = APP_CONFIG;
    
    for (const key of keys) {
        if (value && typeof value === 'object' && key in value) {
            value = value[key];
        } else {
            return fallback;
        }
    }
    
    return value;
}

/**
 * Get full WebSocket URL for endpoint
 * @param {string} endpoint - WebSocket endpoint name ('ticker' or 'instruments')
 */
function getWebSocketUrl(endpoint) {
    const baseUrl = getConfig('websocket.baseUrl', `ws://${window.location.host}`);
    const endpointPath = getConfig(`websocket.endpoints.${endpoint}`, `/ws/${endpoint}`);
    return baseUrl + endpointPath;
}

/**
 * Get full API URL for endpoint
 * @param {string} endpoint - API endpoint path like '/api/instruments/exchanges'
 */
function getApiUrl(endpoint) {
    const baseUrl = getConfig('api.baseUrl', window.location.origin);
    return baseUrl + endpoint;
}

// Make config functions globally available
window.loadAppConfig = loadAppConfig;
window.getConfig = getConfig;
window.getWebSocketUrl = getWebSocketUrl;
window.getApiUrl = getApiUrl;