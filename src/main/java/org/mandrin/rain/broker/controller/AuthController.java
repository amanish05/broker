package org.mandrin.rain.broker.controller;

import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import jakarta.servlet.http.HttpSession;
import org.mandrin.rain.broker.service.KiteAuthService;
import org.mandrin.rain.broker.config.KiteConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    @Autowired
    private KiteAuthService kiteAuthService;

    @GetMapping(KiteConstants.KITE_LOGIN_PATH)
    public String login() {
        return "redirect:" + kiteAuthService.getLoginUrl();
    }

    @GetMapping(KiteConstants.KITE_CALLBACK_PATH)
    public String kiteCallback(@RequestParam("request_token") String requestToken, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            kiteAuthService.getAccessToken(requestToken, session);
            return "redirect:" + KiteConstants.HOME_PATH;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Authentication failed: " + e.getMessage());
            return "redirect:/error";
        } catch (KiteException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
