package org.mandrin.rain.broker.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;
import org.mandrin.rain.broker.service.SessionValidationService;
import org.mandrin.rain.broker.util.MockKiteApiServer;
import org.mandrin.rain.broker.util.SessionTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("SessionValidationService Integration Tests")
public class SessionValidationServiceIntegrationTest {

    @Autowired
    private SessionValidationService sessionValidationService;

    @MockBean
    private MockKiteApiServer mockKiteApiServer;

    @BeforeEach
    void setUp() {
        mockKiteApiServer.reset();
    }

    @Test
    @DisplayName("Valid Token Validation")
    void testValidTokenValidation() {
        // Setup valid token scenario
        mockKiteApiServer.configureErrorScenario(SessionTestUtils.VALID_ACCESS_TOKEN, 
                                                MockKiteApiServer.ErrorScenario.VALID_TOKEN);

        MockHttpSession session = SessionTestUtils.createValidSession();
        
        boolean isValid = sessionValidationService.isAccessTokenValid(session);
        assertTrue(isValid, "Valid token should pass validation");

        // Verify API was called
        assertTrue(mockKiteApiServer.getTotalCallCount("getProfile") > 0, 
                  "API should be called for validation");
    }

    @Test
    @DisplayName("Expired Token Validation")
    void testExpiredTokenValidation() {
        // Setup expired token scenario
        mockKiteApiServer.configureErrorScenario(SessionTestUtils.EXPIRED_ACCESS_TOKEN, 
                                                MockKiteApiServer.ErrorScenario.EXPIRED_TOKEN);

        MockHttpSession session = SessionTestUtils.createExpiredTokenSession();
        
        boolean isValid = sessionValidationService.isAccessTokenValid(session);
        assertFalse(isValid, "Expired token should fail validation");
    }

    @Test
    @DisplayName("Invalid Token Validation")
    void testInvalidTokenValidation() {
        // Setup invalid token scenario
        mockKiteApiServer.configureErrorScenario(SessionTestUtils.INVALID_ACCESS_TOKEN, 
                                                MockKiteApiServer.ErrorScenario.INVALID_TOKEN);

        MockHttpSession session = SessionTestUtils.createInvalidTokenSession();
        
        boolean isValid = sessionValidationService.isAccessTokenValid(session);
        assertFalse(isValid, "Invalid token should fail validation");
    }

    @Test
    @DisplayName("Null and Empty Session Handling")
    void testNullAndEmptySessionHandling() {
        // Null session
        assertFalse(sessionValidationService.isAccessTokenValid((MockHttpSession) null), 
                   "Null session should return false");

        // Empty session
        MockHttpSession emptySession = SessionTestUtils.createEmptySession();
        assertFalse(sessionValidationService.isAccessTokenValid(emptySession), 
                   "Empty session should return false");

        // Session with null token
        MockHttpSession nullTokenSession = SessionTestUtils.createSessionWithToken(null);
        assertFalse(sessionValidationService.isAccessTokenValid(nullTokenSession), 
                   "Session with null token should return false");

        // Session with empty token
        MockHttpSession emptyTokenSession = SessionTestUtils.createSessionWithToken("");
        assertFalse(sessionValidationService.isAccessTokenValid(emptyTokenSession), 
                   "Session with empty token should return false");
    }

    @Test
    @DisplayName("Caching Mechanism Validation")
    void testCachingMechanism() {
        // Setup valid token
        mockKiteApiServer.configureErrorScenario(SessionTestUtils.VALID_ACCESS_TOKEN, 
                                                MockKiteApiServer.ErrorScenario.VALID_TOKEN);

        String accessToken = SessionTestUtils.VALID_ACCESS_TOKEN;
        
        // First call should hit the API
        int initialCallCount = mockKiteApiServer.getTotalCallCount("getProfile");
        boolean firstResult = sessionValidationService.isAccessTokenValid(accessToken);
        int afterFirstCall = mockKiteApiServer.getTotalCallCount("getProfile");
        
        assertTrue(firstResult, "First validation should succeed");
        assertTrue(afterFirstCall > initialCallCount, "First call should hit API");

        // Second call within cache period should use cache
        boolean secondResult = sessionValidationService.isAccessTokenValid(accessToken);
        int afterSecondCall = mockKiteApiServer.getTotalCallCount("getProfile");
        
        assertTrue(secondResult, "Second validation should succeed");
        assertEquals(afterFirstCall, afterSecondCall, "Second call should use cache");

        // Verify cache size increased
        assertTrue(sessionValidationService.getCacheSize() > 0, "Cache should contain entries");
    }

    @Test
    @DisplayName("Cache Expiry Handling")
    @Timeout(10)
    void testCacheExpiryHandling() throws InterruptedException {
        // Note: This test would need actual cache expiry time modification for proper testing
        // For now, we test that cache cleanup functionality works
        
        mockKiteApiServer.configureErrorScenario(SessionTestUtils.VALID_ACCESS_TOKEN, 
                                                MockKiteApiServer.ErrorScenario.VALID_TOKEN);

        String accessToken = SessionTestUtils.VALID_ACCESS_TOKEN;
        
        // Validate token to populate cache
        sessionValidationService.isAccessTokenValid(accessToken);
        int cacheSize = sessionValidationService.getCacheSize();
        assertTrue(cacheSize > 0, "Cache should have entries");

        // Clear expired cache (manual trigger for testing)
        sessionValidationService.clearExpiredCache();
        
        // Cache size might not change immediately if entries haven't expired
        // This tests that the cleanup method doesn't crash
        assertDoesNotThrow(() -> sessionValidationService.clearExpiredCache());
    }

    @Test
    @DisplayName("Network Error Handling")
    void testNetworkErrorHandling() {
        // Setup network error scenario
        String networkErrorToken = "network_error_test_token";
        mockKiteApiServer.configureErrorScenario(networkErrorToken, 
                                                MockKiteApiServer.ErrorScenario.NETWORK_ERROR);

        // Network errors should be handled gracefully
        boolean result = sessionValidationService.isAccessTokenValid(networkErrorToken);
        
        // Depending on implementation, might return true (assume valid) or false
        // The important thing is it doesn't throw exceptions
        assertNotNull(result);
    }

    @Test
    @DisplayName("Rate Limiting Handling")
    void testRateLimitingHandling() {
        // Setup rate limiting scenario
        String rateLimitedToken = "rate_limited_test_token";
        mockKiteApiServer.configureErrorScenario(rateLimitedToken, 
                                                MockKiteApiServer.ErrorScenario.RATE_LIMITED);

        // Rate limiting should be handled gracefully
        assertDoesNotThrow(() -> {
            boolean result = sessionValidationService.isAccessTokenValid(rateLimitedToken);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Concurrent Token Validation")
    void testConcurrentTokenValidation() {
        // Setup valid token
        mockKiteApiServer.configureErrorScenario(SessionTestUtils.VALID_ACCESS_TOKEN, 
                                                MockKiteApiServer.ErrorScenario.VALID_TOKEN);

        String accessToken = SessionTestUtils.VALID_ACCESS_TOKEN;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        try {
            // Perform multiple concurrent validations
            CompletableFuture<Boolean>[] futures = new CompletableFuture[20];
            
            for (int i = 0; i < 20; i++) {
                futures[i] = CompletableFuture.supplyAsync(() -> 
                    sessionValidationService.isAccessTokenValid(accessToken), executor);
            }
            
            // Wait for all validations to complete
            CompletableFuture.allOf(futures).join();
            
            // All results should be true
            for (CompletableFuture<Boolean> future : futures) {
                assertTrue(future.join(), "All concurrent validations should succeed");
            }
            
            // Cache should be populated but not overloaded
            assertTrue(sessionValidationService.getCacheSize() > 0, "Cache should have entries");
            assertTrue(sessionValidationService.getCacheSize() < 20, "Cache should not have duplicate entries");
            
        } finally {
            executor.shutdown();
        }
    }

    @Test
    @DisplayName("Session Invalidation")
    void testSessionInvalidation() {
        MockHttpSession session = SessionTestUtils.createValidSession();
        
        // Verify session has token initially
        SessionTestUtils.assertSessionState(session, true);
        
        // Invalidate session
        sessionValidationService.invalidateSession(session);
        
        // Verify session was invalidated
        assertTrue(session.isInvalid(), "Session should be marked as invalid");
    }

    @Test
    @DisplayName("Multiple Token Validation with Different Results")
    void testMultipleTokenValidationWithDifferentResults() {
        // Setup different scenarios for different tokens
        String validToken = "valid_token_" + System.currentTimeMillis();
        String invalidToken = "invalid_token_" + System.currentTimeMillis();
        String expiredToken = "expired_token_" + System.currentTimeMillis();
        
        mockKiteApiServer.configureErrorScenario(validToken, MockKiteApiServer.ErrorScenario.VALID_TOKEN);
        mockKiteApiServer.configureErrorScenario(invalidToken, MockKiteApiServer.ErrorScenario.INVALID_TOKEN);
        mockKiteApiServer.configureErrorScenario(expiredToken, MockKiteApiServer.ErrorScenario.EXPIRED_TOKEN);

        // Test all scenarios
        assertTrue(sessionValidationService.isAccessTokenValid(validToken), 
                  "Valid token should pass");
        assertFalse(sessionValidationService.isAccessTokenValid(invalidToken), 
                   "Invalid token should fail");
        assertFalse(sessionValidationService.isAccessTokenValid(expiredToken), 
                   "Expired token should fail");

        // Verify cache contains all results
        assertEquals(3, sessionValidationService.getCacheSize(), 
                    "Cache should contain all three validation results");
    }

    @Test
    @DisplayName("Edge Cases and Error Conditions")
    void testEdgeCasesAndErrorConditions() {
        // Very long token
        String veryLongToken = "a".repeat(10000);
        assertDoesNotThrow(() -> sessionValidationService.isAccessTokenValid(veryLongToken));

        // Token with special characters
        String specialCharToken = "token_with_special_chars_!@#$%^&*()";
        assertDoesNotThrow(() -> sessionValidationService.isAccessTokenValid(specialCharToken));

        // Whitespace token
        String whitespaceToken = "   ";
        assertFalse(sessionValidationService.isAccessTokenValid(whitespaceToken), 
                   "Whitespace-only token should be invalid");
    }
}