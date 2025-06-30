package org.mandrin.rain.broker.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mandrin.rain.broker.config.ApiConstants;
import org.mandrin.rain.broker.service.SessionValidationService;
import org.mandrin.rain.broker.service.KiteAuthService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final Logger logger = LogManager.getLogger(AuthInterceptor.class);
    
    @Autowired
    private SessionValidationService sessionValidationService;
    
    @Autowired
    private KiteAuthService kiteAuthService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        logger.debug("[AuthInterceptor] {} {} | Session: {}", method, uri, session != null ? "exists" : "null");
        
        // Check for valid session and access token
        boolean isAuthenticated = false;
        if (session != null) {
            Object token = session.getAttribute(ApiConstants.KITE_ACCESS_TOKEN_SESSION);
            isAuthenticated = token != null && !token.toString().trim().isEmpty();
            logger.debug("[AuthInterceptor] Access token present: {}", isAuthenticated);
        }
        
        if (!isAuthenticated) {
            // Development mode: Auto-create session if configured
            if (kiteAuthService.shouldAutoCreateSession()) {
                logger.info("[AuthInterceptor] Development mode: Auto-creating session for {} {}", method, uri);
                if (session == null) {
                    session = request.getSession(true);
                }
                kiteAuthService.createDevSession(session);
                logger.debug("[AuthInterceptor] Development session created, proceeding with request");
                return true;
            }
            
            logger.warn("[AuthInterceptor] Authentication required for {} {}", method, uri);
            
            // Handle API endpoints vs web pages differently
            if (uri.startsWith("/api/")) {
                // For API endpoints, return 401 JSON response
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Authentication required\",\"message\":\"Please login to access this resource\"}");
                return false;
            } else {
                // For web pages, redirect to login
                response.sendRedirect(ApiConstants.LOGIN_PATH);
                return false;
            }
        }
        
        // Additional token validation for critical operations
        if (session != null && isCriticalOperation(uri, method)) {
            logger.debug("[AuthInterceptor] Performing token validation for critical operation: {} {}", method, uri);
            
            if (!sessionValidationService.isAccessTokenValid(session)) {
                logger.error("[AuthInterceptor] Token validation failed for critical operation: {} {}", method, uri);
                
                // Invalidate the session since token is invalid
                sessionValidationService.invalidateSession(session);
                
                if (uri.startsWith("/api/")) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Invalid or expired token\",\"message\":\"Please re-login to continue\"}");
                    return false;
                } else {
                    response.sendRedirect(ApiConstants.LOGIN_PATH);
                    return false;
                }
            }
        }
        
        logger.debug("[AuthInterceptor] Authentication successful for {} {}", method, uri);
        return true;
    }
    
    /**
     * Determines if the operation requires additional token validation
     */
    private boolean isCriticalOperation(String uri, String method) {
        return (uri.startsWith("/api/orders") && "POST".equals(method)) ||
               (uri.startsWith("/api/ticker/subscribe") && "POST".equals(method)) ||
               (uri.startsWith("/api/portfolio") && "GET".equals(method));
    }
}
