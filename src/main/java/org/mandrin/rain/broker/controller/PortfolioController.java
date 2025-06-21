package org.mandrin.rain.broker.controller;

import jakarta.servlet.http.HttpSession;
import org.mandrin.rain.broker.config.ApiConstants;
import org.mandrin.rain.broker.config.KiteConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {
    @Value("${kite.api_key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    @Autowired
    public PortfolioController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/holdings")
    public ResponseEntity<?> getHoldings(HttpSession session) {
        String accessToken = (String) session.getAttribute(KiteConstants.KITE_ACCESS_TOKEN_SESSION);
        if (accessToken == null) {
            return ResponseEntity.status(401).body(Map.of("status", ApiConstants.STATUS_ERROR, "message", ApiConstants.NOT_AUTHENTICATED_MSG));
        }
        String url = ApiConstants.HOLDINGS_URL;
        HttpHeaders headers = new HttpHeaders();
        headers.set(ApiConstants.KITE_VERSION_HEADER, ApiConstants.KITE_VERSION);
        headers.set(ApiConstants.AUTH_HEADER, String.format(ApiConstants.AUTH_TOKEN_FORMAT, apiKey, accessToken));
        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, Map.class);
        return ResponseEntity.ok(response.getBody());
    }
}
