package org.mandrin.rain.broker.controller;

import jakarta.servlet.http.HttpSession;
import org.mandrin.rain.broker.config.ApiConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@Slf4j
public class PortfolioController {
    @Value("${kite.api_key}")
    private String apiKey;
    
    @Value("${kite.dev.mock_session:false}")
    private boolean mockSession;

    private final WebClient webClient;

    @GetMapping("/holdings")
    public ResponseEntity<?> getHoldings(HttpSession session) {
        log.info("Fetching portfolio holdings (Mock mode: {})", mockSession);
        String accessToken = (String) session.getAttribute(ApiConstants.KITE_ACCESS_TOKEN_SESSION);
        if (accessToken == null) {
            return ResponseEntity.status(401).body(Map.of(
                ApiConstants.RESPONSE_KEY_STATUS, ApiConstants.STATUS_ERROR, 
                ApiConstants.RESPONSE_KEY_MESSAGE, ApiConstants.NOT_AUTHENTICATED_MSG
            ));
        }
        
        // Return mock data in development mode
        if (mockSession) {
            log.info("Returning mock portfolio holdings for development");
            Map<String, Object> mockResponse = Map.of(
                "status", "success",
                "data", java.util.List.of(
                    Map.of(
                        "instrument_token", 256265,
                        "exchange", "NSE",
                        "tradingsymbol", "RELIANCE",
                        "product", "CNC",
                        "quantity", 10,
                        "average_price", 2450.50,
                        "last_price", 2475.30,
                        "pnl", 248.0,
                        "close_price", 2460.75
                    ),
                    Map.of(
                        "instrument_token", 408065,
                        "exchange", "NSE",
                        "tradingsymbol", "INFY",
                        "product", "CNC",
                        "quantity", 25,
                        "average_price", 1520.25,
                        "last_price", 1535.80,
                        "pnl", 388.75,
                        "close_price", 1528.90
                    )
                )
            );
            return ResponseEntity.ok(mockResponse);
        }
        
        try {
            Map<String, Object> response = webClient.get()
                .uri(ApiConstants.HOLDINGS_URL)
                .header(ApiConstants.KITE_VERSION_HEADER, ApiConstants.KITE_VERSION)
                .header(ApiConstants.AUTH_HEADER, String.format(ApiConstants.AUTH_TOKEN_FORMAT, apiKey, accessToken))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
                
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching holdings: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                ApiConstants.RESPONSE_KEY_STATUS, ApiConstants.STATUS_ERROR,
                ApiConstants.RESPONSE_KEY_MESSAGE, "Failed to fetch holdings: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/positions")
    public ResponseEntity<?> getPositions(HttpSession session) {
        log.info("Fetching portfolio positions (Mock mode: {})", mockSession);
        String accessToken = (String) session.getAttribute(ApiConstants.KITE_ACCESS_TOKEN_SESSION);
        if (accessToken == null) {
            return ResponseEntity.status(401).body(Map.of(
                ApiConstants.RESPONSE_KEY_STATUS, ApiConstants.STATUS_ERROR, 
                ApiConstants.RESPONSE_KEY_MESSAGE, ApiConstants.NOT_AUTHENTICATED_MSG
            ));
        }
        
        // Return mock data in development mode
        if (mockSession) {
            log.info("Returning mock portfolio positions for development");
            var netPosition = new java.util.HashMap<String, Object>();
            netPosition.put("instrument_token", 256265);
            netPosition.put("exchange", "NSE");
            netPosition.put("tradingsymbol", "RELIANCE");
            netPosition.put("product", "MIS");
            netPosition.put("quantity", 5);
            netPosition.put("average_price", 2470.50);
            netPosition.put("last_price", 2475.30);
            netPosition.put("pnl", 24.0);
            netPosition.put("m2m", 24.0);
            netPosition.put("unrealised", 24.0);
            netPosition.put("realised", 0.0);
            
            var dayPosition = new java.util.HashMap<String, Object>();
            dayPosition.put("instrument_token", 256265);
            dayPosition.put("exchange", "NSE");
            dayPosition.put("tradingsymbol", "RELIANCE");
            dayPosition.put("product", "MIS");
            dayPosition.put("quantity", 5);
            dayPosition.put("average_price", 2470.50);
            dayPosition.put("last_price", 2475.30);
            dayPosition.put("pnl", 24.0);
            dayPosition.put("m2m", 24.0);
            dayPosition.put("unrealised", 24.0);
            dayPosition.put("realised", 0.0);
            
            Map<String, Object> mockResponse = Map.of(
                "status", "success",
                "data", Map.of(
                    "net", java.util.List.of(netPosition),
                    "day", java.util.List.of(dayPosition)
                )
            );
            return ResponseEntity.ok(mockResponse);
        }
        
        try {
            Map<String, Object> response = webClient.get()
                .uri(ApiConstants.POSITIONS_URL)
                .header(ApiConstants.KITE_VERSION_HEADER, ApiConstants.KITE_VERSION)
                .header(ApiConstants.AUTH_HEADER, String.format(ApiConstants.AUTH_TOKEN_FORMAT, apiKey, accessToken))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
                
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching positions: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                ApiConstants.RESPONSE_KEY_STATUS, ApiConstants.STATUS_ERROR,
                ApiConstants.RESPONSE_KEY_MESSAGE, "Failed to fetch positions: " + e.getMessage()
            ));
        }
    }
}
