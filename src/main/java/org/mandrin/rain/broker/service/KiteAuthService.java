package org.mandrin.rain.broker.service;

import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.zerodhatech.kiteconnect.KiteConnect;
import org.mandrin.rain.broker.config.KiteConstants;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class KiteAuthService {
    @Value("${kite.api_key}")
    private String apiKey;

    @Value("${kite.api_secret}")
    private String apiSecret;

    @Value("${kite.user_id:}")
    private String userId;

    public String getLoginUrl() {
        return String.format(KiteConstants.KITE_LOGIN_URL, apiKey);
    }

    public String getAccessToken(String requestToken, HttpSession session) throws Exception, KiteException {
        String checksum = sha256(apiKey + requestToken + apiSecret);
        KiteConnect kiteConnect = new KiteConnect(apiKey);
        kiteConnect.setUserId(userId);
        String accessToken = kiteConnect.generateSession(requestToken, apiSecret).accessToken;
        session.setAttribute(KiteConstants.KITE_ACCESS_TOKEN_SESSION, accessToken);
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
