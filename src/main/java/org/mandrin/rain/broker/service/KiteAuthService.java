package org.mandrin.rain.broker.service;

import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.zerodhatech.kiteconnect.KiteConnect;
import org.mandrin.rain.broker.config.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import jakarta.annotation.PostConstruct;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class KiteAuthService {
    @Value("${kite.api_key}")
    private String apiKey;

    @Value("${kite.api_secret}")
    private String apiSecret;

    @Value("${kite.user_id:}")
    private String userId;

    @Value("${kite.access_token:}")
    private String devAccessToken;

    @Value("${kite.dev.auto_session:false}")
    private boolean autoSession;

    @Value("${kite.dev.mock_session:false}")
    private boolean mockSession;

    private final Environment environment;

    public KiteAuthService(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        if (isDevelopmentMode() && isDevTokenConfigured()) {
            log.info("Development mode detected with access token configured");
            log.info("Auto-session: {}, Mock-session: {}", autoSession, mockSession);
        }
    }

    public boolean isDevelopmentMode() {
        // Check if development features are enabled via environment variables
        return autoSession || mockSession;
    }

    public boolean isDevTokenConfigured() {
        return devAccessToken != null && !devAccessToken.trim().isEmpty();
    }

    public String getDevAccessToken() {
        return devAccessToken;
    }

    public boolean shouldAutoCreateSession() {
        return isDevelopmentMode() && autoSession && (isDevTokenConfigured() || mockSession);
    }

    public void createDevSession(HttpSession session) {
        if (isDevelopmentMode()) {
            String token = mockSession ? "mock_access_token_dev" : devAccessToken;
            if (token != null && !token.trim().isEmpty()) {
                session.setAttribute(ApiConstants.KITE_ACCESS_TOKEN_SESSION, token);
                log.info("Created development session with token: {}...", token.substring(0, Math.min(8, token.length())));
            }
        }
    }

    public String getLoginUrl() {
        return String.format(ApiConstants.KITE_LOGIN_URL, apiKey);
    }

    public String getAccessToken(String requestToken, HttpSession session) throws Exception, KiteException {
        String checksum = sha256(apiKey + requestToken + apiSecret);
        KiteConnect kiteConnect = new KiteConnect(apiKey);
        kiteConnect.setUserId(userId);
        String accessToken = kiteConnect.generateSession(requestToken, apiSecret).accessToken;
        session.setAttribute(ApiConstants.KITE_ACCESS_TOKEN_SESSION, accessToken);
        return accessToken;
    }

    // Make this method package-private for testability
    String sha256(String base) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(base.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
