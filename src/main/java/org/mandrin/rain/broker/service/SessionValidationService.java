package org.mandrin.rain.broker.service;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.mandrin.rain.broker.config.ApiConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
@Slf4j
public class SessionValidationService {

    @Value("${kite.api_key}")
    private String apiKey;
    
    @Value("${kite.dev.mock_session:false}")
    private boolean mockSession;

    // Cache validation results to avoid excessive API calls
    private final Map<String, TokenValidationResult> validationCache = new ConcurrentHashMap<>();
    private static final long CACHE_VALIDITY_MINUTES = 5;

    public static class TokenValidationResult {
        public final boolean isValid;
        public final LocalDateTime checkedAt;
        public final String error;

        public TokenValidationResult(boolean isValid, String error) {
            this.isValid = isValid;
            this.checkedAt = LocalDateTime.now();
            this.error = error;
        }

        public boolean isCacheExpired() {
            return checkedAt.plusMinutes(CACHE_VALIDITY_MINUTES).isBefore(LocalDateTime.now());
        }
    }

    /**
     * Validates if the access token in the session is still valid with Kite Connect
     */
    public boolean isAccessTokenValid(HttpSession session) {
        if (session == null) {
            return false;
        }

        String accessToken = (String) session.getAttribute(ApiConstants.KITE_ACCESS_TOKEN_SESSION);
        if (accessToken == null || accessToken.trim().isEmpty()) {
            return false;
        }

        return isAccessTokenValid(accessToken);
    }

    /**
     * Validates if the given access token is still valid with Kite Connect
     */
    public boolean isAccessTokenValid(String accessToken) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            return false;
        }

        // Check cache first
        TokenValidationResult cached = validationCache.get(accessToken);
        if (cached != null && !cached.isCacheExpired()) {
            log.debug("Using cached validation result for token: valid={}", cached.isValid);
            return cached.isValid;
        }

        // Perform actual validation
        TokenValidationResult result = validateWithKiteAPI(accessToken);
        validationCache.put(accessToken, result);

        log.debug("Token validation result: valid={}, error={}", result.isValid, result.error);
        return result.isValid;
    }

    /**
     * Validates token by making a lightweight API call to Kite Connect or mock validation
     */
    private TokenValidationResult validateWithKiteAPI(String accessToken) {
        // In mock mode, always return valid for development testing
        if (mockSession) {
            log.debug("Mock mode enabled - skipping actual Kite API validation");
            return new TokenValidationResult(true, "Mock validation (development mode)");
        }
        
        try {
            KiteConnect kiteConnect = new KiteConnect(apiKey);
            kiteConnect.setAccessToken(accessToken);

            // Make a lightweight API call to check token validity
            // Using profile endpoint as it's a simple GET request
            kiteConnect.getProfile();
            
            return new TokenValidationResult(true, null);
            
        } catch (KiteException e) {
            String errorMessage = e.getMessage();
            log.error("KiteException details:");
            log.error("  - Message: {}", errorMessage);
            log.error("  - Exception class: {}", e.getClass().getName());
            log.error("  - Error code: {}", e.code);
            log.error("  - Full exception: ", e);
            
            // Handle null message gracefully
            if (errorMessage != null) {
                // Check for specific authentication errors
                if (errorMessage.contains("Invalid token") || 
                    errorMessage.contains("Token required") ||
                    errorMessage.contains("Invalid API credentials")) {
                    log.warn("Authentication error detected: {}", errorMessage);
                    return new TokenValidationResult(false, "Invalid or expired token");
                }
                // For other errors, assume token might still be valid (network issues, etc.)
                log.warn("Non-authentication error, assuming token valid: {}", errorMessage);
                return new TokenValidationResult(true, "Validation inconclusive: " + errorMessage);
            } else {
                // Handle null message case
                log.error("KiteException with null message - error code: {}", e.code);
                return new TokenValidationResult(false, "Validation error: KiteException with null message (code: " + e.code + ")");
            }
            
        } catch (IOException e) {
            log.error("Network error during token validation: {}", e.getMessage());
            // On network errors, assume token is still valid to avoid false negatives
            return new TokenValidationResult(true, "Network error during validation");
            
        } catch (Exception e) {
            log.error("Unexpected error during token validation: {}", e.getMessage());
            return new TokenValidationResult(false, "Validation error: " + e.getMessage());
        }
    }

    /**
     * Invalidates session and clears related cache
     */
    public void invalidateSession(HttpSession session) {
        if (session == null) {
            return;
        }

        String accessToken = (String) session.getAttribute(ApiConstants.KITE_ACCESS_TOKEN_SESSION);
        if (accessToken != null) {
            validationCache.remove(accessToken);
        }

        try {
            session.invalidate();
            log.info("Session invalidated successfully");
        } catch (IllegalStateException e) {
            log.warn("Session was already invalidated: {}", e.getMessage());
        }
    }

    /**
     * Clears expired entries from validation cache
     */
    public void clearExpiredCache() {
        validationCache.entrySet().removeIf(entry -> entry.getValue().isCacheExpired());
        log.debug("Cleared expired validation cache entries");
    }

    /**
     * Gets the validation cache size (for monitoring)
     */
    public int getCacheSize() {
        return validationCache.size();
    }
}