package org.mandrin.rain.broker.util;

import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock Kite API server for testing session validation scenarios
 * Simulates different Kite Connect API responses for token validation
 */
@Component
@TestPropertySource(properties = {
    "kite.api_key=test_api_key",
    "kite.api_secret=test_api_secret", 
    "kite.user_id=test_user_id"
})
public class MockKiteApiServer {

    // Mock API responses based on access token
    private final Map<String, ApiResponse> tokenResponses = new ConcurrentHashMap<>();
    
    // Track API call counts for testing
    private final Map<String, Integer> callCounts = new ConcurrentHashMap<>();
    
    public MockKiteApiServer() {
        setupDefaultResponses();
    }

    /**
     * API response model
     */
    public static class ApiResponse {
        public final boolean isSuccess;
        public final String errorMessage;
        public final Map<String, Object> data;
        public final int statusCode;

        public ApiResponse(boolean isSuccess, String errorMessage, Map<String, Object> data, int statusCode) {
            this.isSuccess = isSuccess;
            this.errorMessage = errorMessage;
            this.data = data != null ? data : new HashMap<>();
            this.statusCode = statusCode;
        }

        public static ApiResponse success(Map<String, Object> data) {
            return new ApiResponse(true, null, data, 200);
        }

        public static ApiResponse error(String message, int statusCode) {
            return new ApiResponse(false, message, null, statusCode);
        }
    }

    /**
     * Setup default mock responses for different token scenarios
     */
    private void setupDefaultResponses() {
        // Valid token response
        Map<String, Object> validUserData = new HashMap<>();
        validUserData.put("user_id", "test_user");
        validUserData.put("user_name", "Test User");
        validUserData.put("email", "test@example.com");
        validUserData.put("broker", "ZERODHA");
        
        tokenResponses.put(SessionTestUtils.VALID_ACCESS_TOKEN, 
            ApiResponse.success(validUserData));

        // Expired token response
        tokenResponses.put(SessionTestUtils.EXPIRED_ACCESS_TOKEN,
            ApiResponse.error("Token expired", 403));

        // Invalid token response  
        tokenResponses.put(SessionTestUtils.INVALID_ACCESS_TOKEN,
            ApiResponse.error("Invalid token", 401));

        // Rate limited response (for testing rate limiting scenarios)
        tokenResponses.put("rate_limited_token",
            ApiResponse.error("Rate limit exceeded", 429));

        // Network error simulation
        tokenResponses.put("network_error_token",
            ApiResponse.error("Network timeout", 500));
    }

    /**
     * Simulates Kite Connect profile API call
     */
    public ApiResponse getProfile(String accessToken) {
        incrementCallCount("getProfile", accessToken);
        
        // Simulate network delay
        try {
            Thread.sleep(50); // 50ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ApiResponse response = tokenResponses.get(accessToken);
        if (response == null) {
            return ApiResponse.error("Unknown token", 401);
        }

        return response;
    }

    /**
     * Simulates Kite Connect holdings API call
     */
    public ApiResponse getHoldings(String accessToken) {
        incrementCallCount("getHoldings", accessToken);
        
        ApiResponse profileResponse = getProfile(accessToken);
        if (!profileResponse.isSuccess) {
            return profileResponse;
        }

        Map<String, Object> holdings = new HashMap<>();
        holdings.put("holdings", new Object[]{
            Map.of("tradingsymbol", "RELIANCE", "quantity", 10, "price", 2500.0),
            Map.of("tradingsymbol", "TCS", "quantity", 5, "price", 3200.0)
        });
        
        return ApiResponse.success(holdings);
    }

    /**
     * Simulates Kite Connect order placement
     */
    public ApiResponse placeOrder(String accessToken, Map<String, Object> orderData) {
        incrementCallCount("placeOrder", accessToken);
        
        ApiResponse profileResponse = getProfile(accessToken);
        if (!profileResponse.isSuccess) {
            return profileResponse;
        }

        Map<String, Object> orderResponse = new HashMap<>();
        orderResponse.put("order_id", "ORDER_" + System.currentTimeMillis());
        orderResponse.put("status", "COMPLETE");
        
        return ApiResponse.success(orderResponse);
    }

    /**
     * Configures custom response for specific token
     */
    public void setTokenResponse(String accessToken, ApiResponse response) {
        tokenResponses.put(accessToken, response);
    }

    /**
     * Configures token to simulate network error
     */
    public void setTokenNetworkError(String accessToken) {
        tokenResponses.put(accessToken, ApiResponse.error("Network timeout", 500));
    }

    /**
     * Configures token to simulate rate limiting
     */
    public void setTokenRateLimited(String accessToken) {
        tokenResponses.put(accessToken, ApiResponse.error("Rate limit exceeded", 429));
    }

    /**
     * Gets call count for specific API and token
     */
    public int getCallCount(String apiMethod, String accessToken) {
        return callCounts.getOrDefault(apiMethod + ":" + accessToken, 0);
    }

    /**
     * Gets total call count for specific API method
     */
    public int getTotalCallCount(String apiMethod) {
        return callCounts.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(apiMethod + ":"))
            .mapToInt(Map.Entry::getValue)
            .sum();
    }

    /**
     * Resets all call counts
     */
    public void resetCallCounts() {
        callCounts.clear();
    }

    /**
     * Resets to default responses
     */
    public void reset() {
        resetCallCounts();
        tokenResponses.clear();
        setupDefaultResponses();
    }

    /**
     * Helper to increment call count
     */
    private void incrementCallCount(String apiMethod, String accessToken) {
        String key = apiMethod + ":" + accessToken;
        callCounts.put(key, callCounts.getOrDefault(key, 0) + 1);
    }

    /**
     * Simulates different error scenarios for testing
     */
    public enum ErrorScenario {
        VALID_TOKEN,
        EXPIRED_TOKEN,
        INVALID_TOKEN,
        RATE_LIMITED,
        NETWORK_ERROR,
        SERVER_ERROR,
        PERMISSION_DENIED
    }

    /**
     * Configures token for specific error scenario
     */
    public void configureErrorScenario(String accessToken, ErrorScenario scenario) {
        switch (scenario) {
            case VALID_TOKEN -> setTokenResponse(accessToken, tokenResponses.get(SessionTestUtils.VALID_ACCESS_TOKEN));
            case EXPIRED_TOKEN -> setTokenResponse(accessToken, ApiResponse.error("Token expired", 403));
            case INVALID_TOKEN -> setTokenResponse(accessToken, ApiResponse.error("Invalid token", 401));
            case RATE_LIMITED -> setTokenResponse(accessToken, ApiResponse.error("Rate limit exceeded", 429));
            case NETWORK_ERROR -> setTokenResponse(accessToken, ApiResponse.error("Connection timeout", 500));
            case SERVER_ERROR -> setTokenResponse(accessToken, ApiResponse.error("Internal server error", 500));
            case PERMISSION_DENIED -> setTokenResponse(accessToken, ApiResponse.error("Insufficient permissions", 403));
        }
    }

    /**
     * Creates test tokens for different scenarios
     */
    public static class TestTokens {
        public static final String VALID = SessionTestUtils.VALID_ACCESS_TOKEN;
        public static final String EXPIRED = SessionTestUtils.EXPIRED_ACCESS_TOKEN;
        public static final String INVALID = SessionTestUtils.INVALID_ACCESS_TOKEN;
        public static final String RATE_LIMITED = "rate_limited_token";
        public static final String NETWORK_ERROR = "network_error_token";
        public static final String CUSTOM_VALID = "custom_valid_token_" + System.currentTimeMillis();
        public static final String CUSTOM_INVALID = "custom_invalid_token_" + System.currentTimeMillis();
    }
}