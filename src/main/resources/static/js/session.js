// session.js
// Enhanced session validation and management

let sessionCheckInterval;
let lastSessionCheck = 0;

function initializeSession() {
    // Called after configuration is loaded
    console.log('Session module initialized with config');
    startPeriodicSessionCheck();
}

function getSessionCheckFrequency() {
    return getConfig('session.checkFrequency', 300000); // Default 5 minutes
}

function getCriticalSessionCheckFrequency() {
    return getConfig('session.criticalCheckFrequency', 30000); // Default 30 seconds
}

// Session status cache
let sessionStatus = {
    authenticated: false,
    tokenValid: false,
    lastChecked: 0
};

/**
 * Validates session with backend
 */
function validateSession(forceCheck = false) {
    const now = Date.now();
    
    // Avoid excessive API calls unless forced
    if (!forceCheck && (now - lastSessionCheck) < 30000) {
        return Promise.resolve(sessionStatus.authenticated);
    }
    
    lastSessionCheck = now;
    
    return fetch('/api/session/status', { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            sessionStatus = {
                authenticated: data.authenticated && data.tokenValid,
                tokenValid: data.tokenValid || false,
                lastChecked: now,
                sessionInfo: data
            };
            
            if (!sessionStatus.authenticated) {
                console.warn('Session validation failed:', data.error || data.warning);
                handleSessionExpiry();
                return false;
            }
            
            console.debug('Session validated successfully');
            return true;
        })
        .catch(err => {
            console.error('Session validation error:', err);
            handleSessionExpiry();
            return false;
        });
}

/**
 * Performs deep token validation with Kite API
 */
function deepValidateSession() {
    return fetch('/api/session/validate', { 
        method: 'POST',
        credentials: 'include' 
    })
    .then(res => res.json())
    .then(data => {
        if (!data.valid) {
            console.warn('Deep session validation failed:', data.error);
            handleSessionExpiry();
            return false;
        }
        
        sessionStatus.authenticated = true;
        sessionStatus.tokenValid = true;
        sessionStatus.lastChecked = Date.now();
        
        console.debug('Deep session validation successful');
        return true;
    })
    .catch(err => {
        console.error('Deep session validation error:', err);
        handleSessionExpiry();
        return false;
    });
}

/**
 * Handles session expiry/invalid scenarios
 */
function handleSessionExpiry() {
    sessionStatus.authenticated = false;
    sessionStatus.tokenValid = false;
    
    // Clear any ongoing session checks
    if (sessionCheckInterval) {
        clearInterval(sessionCheckInterval);
    }
    
    // Show user-friendly message
    if (document.getElementById('session-expired-notice')) {
        document.getElementById('session-expired-notice').style.display = 'block';
    }
    
    // Redirect to login after short delay
    setTimeout(() => {
        window.location.href = '/login';
    }, 2000);
}

/**
 * Validates session before critical operations
 */
function validateSessionForCriticalOperation() {
    return deepValidateSession();
}

/**
 * Starts periodic session validation
 */
function startSessionMonitoring() {
    // Clear existing interval
    if (sessionCheckInterval) {
        clearInterval(sessionCheckInterval);
    }
    
    // Periodic validation
    sessionCheckInterval = setInterval(() => {
        validateSession();
    }, SESSION_CHECK_FREQUENCY);
    
    console.debug('Session monitoring started');
}

/**
 * Enhances forms to validate session before submission
 */
function enhanceFormsWithSessionValidation() {
    // Order form
    const orderForm = document.getElementById('order-form');
    if (orderForm) {
        const originalSubmit = orderForm.onsubmit;
        orderForm.onsubmit = function(e) {
            e.preventDefault();
            
            validateSessionForCriticalOperation().then(valid => {
                if (valid) {
                    if (originalSubmit) {
                        originalSubmit.call(this, e);
                    } else {
                        // Call the original submit handler
                        this.dispatchEvent(new Event('submit'));
                    }
                }
            });
            
            return false;
        };
    }
    
    // Ticker subscription form
    const tickerForm = document.getElementById('api-action-form');
    if (tickerForm) {
        const originalSubmit = tickerForm.onsubmit;
        tickerForm.onsubmit = function(e) {
            e.preventDefault();
            
            validateSession(true).then(valid => {
                if (valid) {
                    if (originalSubmit) {
                        originalSubmit.call(this, e);
                    } else {
                        this.dispatchEvent(new Event('submit'));
                    }
                }
            });
            
            return false;
        };
    }
}

/**
 * Adds session expired notice to the page
 */
function addSessionExpiredNotice() {
    if (document.getElementById('session-expired-notice')) {
        return; // Already exists
    }
    
    const notice = document.createElement('div');
    notice.id = 'session-expired-notice';
    notice.style.cssText = `
        display: none;
        position: fixed;
        top: 20px;
        left: 50%;
        transform: translateX(-50%);
        background: #f8d7da;
        color: #721c24;
        padding: 12px 20px;
        border-radius: 4px;
        border: 1px solid #f5c6cb;
        z-index: 1000;
        font-weight: bold;
    `;
    notice.innerHTML = '⚠️ Your session has expired. Redirecting to login...';
    
    document.body.appendChild(notice);
}

/**
 * Initialize session management
 */
function initializeSessionManagement() {
    // Add session expired notice
    addSessionExpiredNotice();
    
    // Initial session validation
    validateSession(true).then(valid => {
        if (valid) {
            // Start monitoring if session is valid
            startSessionMonitoring();
            
            // Enhance forms
            enhanceFormsWithSessionValidation();
        }
    });
}

// Initialize when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeSessionManagement);
} else {
    initializeSessionManagement();
}

// Legacy function for backward compatibility
function validateSessionLegacy() {
    return validateSession();
}

// Only run session check once per page load (legacy)
if (!window._sessionChecked) {
    window._sessionChecked = true;
    validateSessionLegacy();
}
