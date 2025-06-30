package org.mandrin.rain.broker.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.core.env.Environment;
import org.mockito.Mockito;
import org.mandrin.rain.broker.config.ApiConstants;
import static org.junit.jupiter.api.Assertions.*;

class KiteAuthServiceTest {
    private final KiteAuthService kiteAuthService;

    public KiteAuthServiceTest() {
        Environment mockEnvironment = Mockito.mock(Environment.class);
        kiteAuthService = new KiteAuthService(mockEnvironment);
        // Use reflection to set private fields for testing
        setField(kiteAuthService, "apiKey", "test_key");
        setField(kiteAuthService, "apiSecret", "test_secret");
        setField(kiteAuthService, "userId", "test_user");
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetLoginUrl() {
        String url = kiteAuthService.getLoginUrl();
        assertTrue(url.contains("test_key"));
    }

    @Test
    void testSha256() throws Exception {
        // Make sha256 package-private for testability
        String hash = kiteAuthService.sha256("test");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }
}
