package org.mandrin.rain.broker.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mandrin.rain.broker.config.KiteConstants;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        String uri = request.getRequestURI();
        if ((uri.equals(KiteConstants.ROOT_PATH) || uri.equals(KiteConstants.HOME_PATH)) && (session == null || session.getAttribute(KiteConstants.KITE_ACCESS_TOKEN_SESSION) == null)) {
            response.sendRedirect(KiteConstants.KITE_LOGIN_PATH);
            return false;
        }
        return true;
    }
}
