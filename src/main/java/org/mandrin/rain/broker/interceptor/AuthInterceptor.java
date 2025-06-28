package org.mandrin.rain.broker.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mandrin.rain.broker.config.KiteConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final Logger logger = LogManager.getLogger(AuthInterceptor.class);
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        String uri = request.getRequestURI();
        logger.debug("[AuthInterceptor] URI: {} | Session: {}", uri, session);
        if (session != null) {
            Object token = session.getAttribute(KiteConstants.KITE_ACCESS_TOKEN_SESSION);
            logger.debug("[AuthInterceptor] kite_access_token in session: {}", token);
        }
        if ((uri.equals(KiteConstants.ROOT_PATH) || uri.equals(KiteConstants.HOME_PATH)) && (session == null || session.getAttribute(KiteConstants.KITE_ACCESS_TOKEN_SESSION) == null)) {
            logger.warn("[AuthInterceptor] Redirecting to login: session or kite_access_token missing");
            response.sendRedirect(KiteConstants.KITE_LOGIN_PATH);
            return false;
        }
        return true;
    }
}
