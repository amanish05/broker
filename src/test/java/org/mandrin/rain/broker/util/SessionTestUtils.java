package org.mandrin.rain.broker.util;

import jakarta.servlet.http.HttpSession;
import org.mandrin.rain.broker.config.ApiConstants;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Utility class for session-related testing
 */
public class SessionTestUtils {

    // Test access tokens
    public static final String VALID_ACCESS_TOKEN = "test_valid_access_token_123456789";
    public static final String EXPIRED_ACCESS_TOKEN = "test_expired_access_token_987654321";
    public static final String INVALID_ACCESS_TOKEN = "test_invalid_access_token_000000000";
    
    // Test session IDs
    public static final String TEST_SESSION_ID = "TEST_SESSION_12345";
    
    /**
     * Creates a mock session with valid access token
     */
    public static MockHttpSession createValidSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(ApiConstants.KITE_ACCESS_TOKEN_SESSION, VALID_ACCESS_TOKEN);
        return session;
    }

    /**
     * Creates a mock session with expired access token
     */
    public static MockHttpSession createExpiredTokenSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(ApiConstants.KITE_ACCESS_TOKEN_SESSION, EXPIRED_ACCESS_TOKEN);
        return session;
    }

    /**
     * Creates a mock session with invalid access token
     */
    public static MockHttpSession createInvalidTokenSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(ApiConstants.KITE_ACCESS_TOKEN_SESSION, INVALID_ACCESS_TOKEN);
        return session;
    }

    /**
     * Creates a mock session without access token
     */
    public static MockHttpSession createEmptySession() {
        return new MockHttpSession();
    }

    /**
     * Creates a mock session with custom access token
     */
    public static MockHttpSession createSessionWithToken(String accessToken) {
        MockHttpSession session = new MockHttpSession();
        if (accessToken != null) {
            session.setAttribute(ApiConstants.KITE_ACCESS_TOKEN_SESSION, accessToken);
        }
        return session;
    }

    /**
     * Verifies session status via API
     */
    public static Map<String, Object> getSessionStatus(MockMvc mockMvc, HttpSession session) throws Exception {
        var result = mockMvc.perform(get("/api/session/status").session((MockHttpSession) session))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        // Simple JSON parsing for test purposes
        Map<String, Object> status = new HashMap<>();
        status.put("status", result.getResponse().getStatus());
        status.put("content", content);
        status.put("authenticated", content.contains("\"authenticated\":true"));
        status.put("tokenValid", content.contains("\"tokenValid\":true"));
        
        return status;
    }

    /**
     * Validates session via API
     */
    public static Map<String, Object> validateSession(MockMvc mockMvc, HttpSession session) throws Exception {
        var result = mockMvc.perform(post("/api/session/validate").session((MockHttpSession) session))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        Map<String, Object> validation = new HashMap<>();
        validation.put("status", result.getResponse().getStatus());
        validation.put("content", content);
        validation.put("valid", content.contains("\"valid\":true"));
        
        return validation;
    }

    /**
     * Performs authenticated request with session
     */
    public static MockHttpServletRequestBuilder authenticatedGet(String url, HttpSession session) {
        return get(url).session((MockHttpSession) session);
    }

    /**
     * Performs authenticated POST request with session
     */
    public static MockHttpServletRequestBuilder authenticatedPost(String url, HttpSession session) {
        return post(url).session((MockHttpSession) session);
    }

    /**
     * Creates test session scenarios for parameterized tests
     */
    public static class SessionScenario {
        public final String name;
        public final MockHttpSession session;
        public final boolean shouldBeValid;
        public final int expectedStatus;

        public SessionScenario(String name, MockHttpSession session, boolean shouldBeValid, int expectedStatus) {
            this.name = name;
            this.session = session;
            this.shouldBeValid = shouldBeValid;
            this.expectedStatus = expectedStatus;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Returns common session test scenarios
     */
    public static SessionScenario[] getSessionScenarios() {
        return new SessionScenario[]{
            new SessionScenario("Valid Session", createValidSession(), true, 200),
            new SessionScenario("Expired Token", createExpiredTokenSession(), false, 401),
            new SessionScenario("Invalid Token", createInvalidTokenSession(), false, 401),
            new SessionScenario("Empty Session", createEmptySession(), false, 401),
            new SessionScenario("Null Token", createSessionWithToken(null), false, 401),
            new SessionScenario("Empty Token", createSessionWithToken(""), false, 401),
            new SessionScenario("Whitespace Token", createSessionWithToken("   "), false, 401)
        };
    }

    /**
     * Asserts that session is in expected state
     */
    public static void assertSessionState(MockHttpSession session, boolean shouldHaveToken) {
        Object token = session.getAttribute(ApiConstants.KITE_ACCESS_TOKEN_SESSION);
        if (shouldHaveToken) {
            assert token != null : "Session should have access token";
            assert !token.toString().trim().isEmpty() : "Access token should not be empty";
        } else {
            assert token == null || token.toString().trim().isEmpty() : "Session should not have valid access token";
        }
    }

    /**
     * Creates session with specific creation and access times for timeout testing
     */
    public static MockHttpSession createSessionWithTiming(long creationTime, long lastAccessTime) {
        MockHttpSession session = new MockHttpSession() {
            @Override
            public long getCreationTime() {
                return creationTime;
            }

            @Override
            public long getLastAccessedTime() {
                return lastAccessTime;
            }
        };
        session.setAttribute(ApiConstants.KITE_ACCESS_TOKEN_SESSION, VALID_ACCESS_TOKEN);
        return session;
    }

    /**
     * Helper to create old session for timeout testing
     */
    public static MockHttpSession createOldSession(int hoursOld) {
        long now = System.currentTimeMillis();
        long oldTime = now - (hoursOld * 60 * 60 * 1000L);
        return createSessionWithTiming(oldTime, oldTime);
    }

    /**
     * Helper to get current timestamp for testing
     */
    public static long getCurrentTimestamp() {
        return Instant.now().toEpochMilli();
    }

    /**
     * Helper to parse JSON response for test assertions
     */
    public static boolean containsJsonField(String json, String field, String value) {
        return json.contains("\"" + field + "\":" + (value.equals("true") || value.equals("false") ? value : "\"" + value + "\""));
    }
}