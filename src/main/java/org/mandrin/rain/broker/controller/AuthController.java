package org.mandrin.rain.broker.controller;

import jakarta.servlet.http.HttpSession;
import org.mandrin.rain.broker.service.KiteAuthService;
import org.mandrin.rain.broker.config.ApiConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final KiteAuthService kiteAuthService;

    @GetMapping(ApiConstants.LOGIN_PATH)
    public String login(HttpSession session) {
        // Check if development mode with auto-session is enabled
        if (kiteAuthService.shouldAutoCreateSession()) {
            log.info("Development mode: Auto-creating session and bypassing OAuth login");
            kiteAuthService.createDevSession(session);
            return "redirect:" + ApiConstants.HOME_PATH;
        }
        
        return "redirect:" + kiteAuthService.getLoginUrl();
    }

    @GetMapping(ApiConstants.KITE_CALLBACK_PATH)
    public String kiteCallback(@RequestParam("request_token") String requestToken, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            kiteAuthService.getAccessToken(requestToken, session);
            return "redirect:" + ApiConstants.HOME_PATH;
        } catch (com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException e) {
            log.error("Kite API error", e);
            redirectAttributes.addFlashAttribute("error", "Kite error: " + e.getMessage());
            return "redirect:/error";
        } catch (Exception e) {
            log.error("Authentication failed", e);
            redirectAttributes.addFlashAttribute("error", "Authentication failed: " + e.getMessage());
            return "redirect:/error";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
