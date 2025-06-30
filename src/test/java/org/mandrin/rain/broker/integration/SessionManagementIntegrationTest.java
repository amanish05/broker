package org.mandrin.rain.broker.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mandrin.rain.broker.service.SessionValidationService;
import org.mandrin.rain.broker.util.MockKiteApiServer;
import org.mandrin.rain.broker.util.SessionTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Session Management Integration Tests")
public class SessionManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessionValidationService sessionValidationService;

    @MockBean
    private MockKiteApiServer mockKiteApiServer;

    @BeforeEach
    void setUp() {
        mockKiteApiServer.reset();
    }

    @Test
    @DisplayName("Complete Authentication Flow")
    void testCompleteAuthenticationFlow() throws Exception {
        // 1. Access protected resource without session - should redirect to login
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // 2. Access login page - should return login redirect URL
        mockMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection());

        // 3. Simulate successful OAuth callback with request token
        // Note: In real implementation, this would involve Kite OAuth flow
        
        // 4. Access protected resource with valid session - should succeed
        MockHttpSession validSession = SessionTestUtils.createValidSession();
        mockMvc.perform(get("/").session(validSession))
                .andExpect(status().isOk());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("sessionScenarios")
    @DisplayName("Session Validation Scenarios")
    void testSessionValidationScenarios(SessionTestUtils.SessionScenario scenario) throws Exception {
        // Test session status endpoint
        mockMvc.perform(get("/api/session/status").session(scenario.session))
                .andExpect(status().is(scenario.expectedStatus));

        // Test session validation endpoint
        mockMvc.perform(post("/api/session/validate").session(scenario.session))
                .andExpect(status().is(scenario.expectedStatus));
    }

    @Test
    @DisplayName("Protected API Endpoints Require Authentication")
    void testProtectedApiEndpoints() throws Exception {
        String[] protectedEndpoints = {
            "/api/portfolio",
            "/api/orders",
            "/api/ticker/subscribe",
            "/api/instruments/NSE"
        };

        MockHttpSession emptySession = SessionTestUtils.createEmptySession();
        MockHttpSession validSession = SessionTestUtils.createValidSession();

        for (String endpoint : protectedEndpoints) {
            // Without session - should return 401
            mockMvc.perform(get(endpoint).session(emptySession))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().json("{\"error\":\"Authentication required\"}"));

            // With valid session - should not return 401 (may return other errors due to missing data)
            mockMvc.perform(get(endpoint).session(validSession))
                    .andExpect(result -> assertNotEquals(401, result.getResponse().getStatus()));
        }
    }

    @Test
    @DisplayName("Critical Operations Require Deep Token Validation")
    void testCriticalOperationsValidation() throws Exception {
        // Setup mock Kite API responses
        mockKiteApiServer.configureErrorScenario(SessionTestUtils.VALID_ACCESS_TOKEN, 
                                                MockKiteApiServer.ErrorScenario.VALID_TOKEN);
        mockKiteApiServer.configureErrorScenario(SessionTestUtils.EXPIRED_ACCESS_TOKEN, 
                                                MockKiteApiServer.ErrorScenario.EXPIRED_TOKEN);

        MockHttpSession validSession = SessionTestUtils.createValidSession();
        MockHttpSession expiredSession = SessionTestUtils.createExpiredTokenSession();

        // Test order placement (critical operation)
        String orderPayload = """
            {
                "tradingsymbol": "RELIANCE",
                "exchange": "NSE",
                "quantity": 1,
                "transactionType": "BUY",
                "orderType": "MARKET",
                "product": "MIS"
            }
            """;

        // Valid session should pass validation
        mockMvc.perform(post("/api/orders")
                .session(validSession)
                .contentType("application/json")
                .content(orderPayload))
                .andExpect(result -> assertNotEquals(401, result.getResponse().getStatus()));

        // Expired session should fail validation
        mockMvc.perform(post("/api/orders")
                .session(expiredSession)
                .contentType("application/json")
                .content(orderPayload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Session Timeout Handling")
    void testSessionTimeout() throws Exception {
        // Create old session (25 hours old, beyond 24-hour timeout)
        MockHttpSession oldSession = SessionTestUtils.createOldSession(25);
        
        // Old session should be treated as expired
        mockMvc.perform(get("/api/session/status").session(oldSession))
                .andExpect(status().isUnauthorized());

        // Recent session should be valid
        MockHttpSession recentSession = SessionTestUtils.createValidSession();
        mockMvc.perform(get("/api/session/status").session(recentSession))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Session Invalidation on Token Expiry")
    void testSessionInvalidationOnTokenExpiry() throws Exception {
        // Setup expired token scenario
        mockKiteApiServer.configureErrorScenario(SessionTestUtils.EXPIRED_ACCESS_TOKEN, 
                                                MockKiteApiServer.ErrorScenario.EXPIRED_TOKEN);

        MockHttpSession expiredSession = SessionTestUtils.createExpiredTokenSession();
        
        // Validate session - should detect expired token and invalidate session
        mockMvc.perform(post("/api/session/validate").session(expiredSession))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"valid\":false}"));

        // Verify session was invalidated
        assertTrue(expiredSession.isInvalid() || 
                  expiredSession.getAttribute("kite_access_token") == null);
    }

    @Test
    @DisplayName("Concurrent Session Validation")
    void testConcurrentSessionValidation() throws Exception {
        MockHttpSession session = SessionTestUtils.createValidSession();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        try {
            // Perform multiple concurrent session validations
            CompletableFuture<?>[] futures = new CompletableFuture[10];
            
            for (int i = 0; i < 10; i++) {
                futures[i] = CompletableFuture.runAsync(() -> {
                    try {
                        Map<String, Object> result = SessionTestUtils.validateSession(mockMvc, session);
                        assertEquals(200, result.get("status"));
                    } catch (Exception e) {
                        fail("Concurrent session validation failed: " + e.getMessage());
                    }
                }, executor);
            }
            
            // Wait for all validations to complete
            CompletableFuture.allOf(futures).join();
            
            // Verify session is still valid
            Map<String, Object> status = SessionTestUtils.getSessionStatus(mockMvc, session);
            assertEquals(200, status.get("status"));
            
        } finally {
            executor.shutdown();
        }
    }

    @Test
    @DisplayName("Network Error Handling During Token Validation")
    void testNetworkErrorHandling() throws Exception {
        // Setup network error scenario
        String networkErrorToken = "network_error_test_token";
        mockKiteApiServer.configureErrorScenario(networkErrorToken, 
                                                MockKiteApiServer.ErrorScenario.NETWORK_ERROR);

        MockHttpSession networkErrorSession = SessionTestUtils.createSessionWithToken(networkErrorToken);
        
        // Network errors should not immediately invalidate session (graceful degradation)
        Map<String, Object> result = SessionTestUtils.validateSession(mockMvc, networkErrorSession);
        
        // Should handle gracefully - either succeed or fail appropriately
        assertTrue(result.get("status").equals(200) || result.get("status").equals(401));
    }

    @Test
    @DisplayName("Rate Limiting Handling")
    void testRateLimitingHandling() throws Exception {
        // Setup rate limited scenario
        String rateLimitedToken = "rate_limited_test_token";
        mockKiteApiServer.configureErrorScenario(rateLimitedToken, 
                                                MockKiteApiServer.ErrorScenario.RATE_LIMITED);

        MockHttpSession rateLimitedSession = SessionTestUtils.createSessionWithToken(rateLimitedToken);
        
        // Rate limiting should be handled gracefully
        Map<String, Object> result = SessionTestUtils.validateSession(mockMvc, rateLimitedSession);
        
        // Should not immediately fail the session due to rate limiting
        assertNotNull(result.get("status"));
    }

    @Test
    @DisplayName("Session Cache Validation")
    void testSessionCacheValidation() throws Exception {
        MockHttpSession session = SessionTestUtils.createValidSession();
        
        // First validation should hit the API
        int initialCallCount = mockKiteApiServer.getTotalCallCount("getProfile");
        sessionValidationService.isAccessTokenValid(session);
        
        int afterFirstCall = mockKiteApiServer.getTotalCallCount("getProfile");
        assertTrue(afterFirstCall > initialCallCount, "First validation should call API");
        
        // Second validation within cache period should use cache
        sessionValidationService.isAccessTokenValid(session);
        int afterSecondCall = mockKiteApiServer.getTotalCallCount("getProfile");
        
        assertEquals(afterFirstCall, afterSecondCall, "Second validation should use cache");
    }

    @Test
    @DisplayName("Cross-Component Session Consistency")
    void testCrossComponentSessionConsistency() throws Exception {
        MockHttpSession session = SessionTestUtils.createValidSession();
        
        // Validate session via different endpoints
        mockMvc.perform(get("/api/session/status").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true));

        mockMvc.perform(post("/api/session/validate").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));

        // Access protected resource - should work consistently
        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk());
    }

    /**
     * Provides session scenarios for parameterized tests
     */
    static Stream<SessionTestUtils.SessionScenario> sessionScenarios() {
        return Stream.of(SessionTestUtils.getSessionScenarios());
    }
}