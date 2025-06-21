package org.mandrin.rain.broker.controller;

import jakarta.servlet.http.HttpSession;
import org.mandrin.rain.broker.config.KiteConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {
    @GetMapping({"/", "/home"})
    public String home() {
        return "home";
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
        Object token = getSessionValue(session, KiteConstants.KITE_ACCESS_TOKEN_SESSION);
        if (token != null) {
            return ResponseEntity.ok().body(java.util.Map.of("kite_access_token", token));
        } else {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "No valid session or token found"));
        }
    }
}
