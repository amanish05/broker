package org.mandrin.rain.broker.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.mandrin.rain.broker.config.ApiConstants;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AuthInterceptorTest {
    @Test
    void preHandle_Unauthenticated_ShouldRedirect() throws Exception {
        AuthInterceptor interceptor = new AuthInterceptor();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getRequestURI()).thenReturn("/home");
        when(request.getSession(false)).thenReturn(null);
        assertFalse(interceptor.preHandle(request, response, new Object()));
        verify(response).sendRedirect("/login");
    }

    @Test
    void preHandle_Authenticated_ShouldAllow() throws Exception {
        AuthInterceptor interceptor = new AuthInterceptor();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getRequestURI()).thenReturn("/home");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(ApiConstants.KITE_ACCESS_TOKEN_SESSION)).thenReturn("token");
        assertTrue(interceptor.preHandle(request, response, new Object()));
    }
}
