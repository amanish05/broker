package org.mandrin.rain.broker.controller;

import jakarta.servlet.http.HttpSession;
import org.mandrin.rain.broker.config.ApiConstants;
import org.mandrin.rain.broker.service.SessionValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeController {
    
    @Autowired
    private SessionValidationService sessionValidationService;
    
    @GetMapping({"/", "/home"})
    public String home() {
        return "home";
    }
    
    @GetMapping("/instruments")
    public String instruments() {
        return "home"; // Use same template for now
    }
    
    @GetMapping("/portfolio") 
    public String portfolio() {
        return "home"; // Use same template for now
    }
    
    @GetMapping("/orders")
    public String orders() {
        return "home"; // Use same template for now
    }

    /**
     * Utility to fetch a session attribute by key, returns null if not found.
     */
    public Object getSessionValue(HttpSession session, String key) {
        return session != null ? session.getAttribute(key) : null;
    }

    @GetMapping("/api/session/kite-access-token")
    @ResponseBody
    public ResponseEntity<?> getKiteAccessToken(HttpSession session) {
        Object token = getSessionValue(session, ApiConstants.KITE_ACCESS_TOKEN_SESSION);
        if (token != null) {
            return ResponseEntity.ok().body(Map.of("kite_access_token", token));
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "No valid session or token found"));
        }
    }

    @GetMapping("/api/session/status")
    @ResponseBody
    public ResponseEntity<?> getSessionStatus(HttpSession session) {
        Map<String, Object> status = new HashMap<>();
        
        if (session == null) {
            status.put("authenticated", false);
            status.put("error", "No session found");
            return ResponseEntity.status(401).body(status);
        }
        
        Object token = session.getAttribute(ApiConstants.KITE_ACCESS_TOKEN_SESSION);
        boolean hasToken = token != null && !token.toString().trim().isEmpty();
        
        status.put("authenticated", hasToken);
        status.put("sessionId", session.getId());
        status.put("creationTime", Instant.ofEpochMilli(session.getCreationTime()));
        status.put("lastAccessedTime", Instant.ofEpochMilli(session.getLastAccessedTime()));
        status.put("maxInactiveInterval", session.getMaxInactiveInterval());
        
        if (hasToken) {
            boolean tokenValid = sessionValidationService.isAccessTokenValid(session);
            status.put("tokenValid", tokenValid);
            
            if (!tokenValid) {
                status.put("warning", "Token appears to be invalid or expired");
                return ResponseEntity.status(401).body(status);
            }
        } else {
            status.put("error", "No access token in session");
            return ResponseEntity.status(401).body(status);
        }
        
        return ResponseEntity.ok().body(status);
    }

    @PostMapping("/api/session/validate")
    @ResponseBody
    public ResponseEntity<?> validateSession(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        if (session == null) {
            result.put("valid", false);
            result.put("error", "No session found");
            return ResponseEntity.status(401).body(result);
        }
        
        boolean isValid = sessionValidationService.isAccessTokenValid(session);
        result.put("valid", isValid);
        result.put("timestamp", Instant.now());
        
        if (!isValid) {
            result.put("error", "Session validation failed - please re-login");
            sessionValidationService.invalidateSession(session);
            return ResponseEntity.status(401).body(result);
        }
        
        return ResponseEntity.ok().body(result);
    }
}
