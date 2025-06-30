package org.mandrin.rain.broker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides frontend configuration derived from application properties
 * This ensures URLs and environment-specific settings are consistent
 */
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {
    
    private final Environment environment;
    
    @Value("${server.port:8080}")
    private int serverPort;
    
    @GetMapping("/frontend")
    public ResponseEntity<Map<String, Object>> getFrontendConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // Build base URLs from application properties
        String protocol = environment.getProperty("server.ssl.enabled", "false").equals("true") ? "https" : "http";
        String wsProtocol = protocol.equals("https") ? "wss" : "ws";
        String host = environment.getProperty("server.address", "localhost");
        
        // WebSocket configuration
        Map<String, Object> websocket = new HashMap<>();
        websocket.put("baseUrl", String.format("%s://%s:%d", wsProtocol, host, serverPort));
        Map<String, String> wsEndpoints = new HashMap<>();
        wsEndpoints.put("ticker", "/ws/ticker");
        wsEndpoints.put("instruments", "/ws/instruments");
        websocket.put("endpoints", wsEndpoints);
        config.put("websocket", websocket);
        
        // API configuration
        Map<String, Object> api = new HashMap<>();
        api.put("baseUrl", String.format("%s://%s:%d", protocol, host, serverPort));
        api.put("timeout", environment.getProperty("frontend.config.api.timeout", "30000"));
        config.put("api", api);
        
        // Session management configuration
        Map<String, Object> session = new HashMap<>();
        session.put("checkFrequency", environment.getProperty("frontend.config.session.check-frequency", "300000"));
        session.put("criticalCheckFrequency", environment.getProperty("frontend.config.session.critical-check-frequency", "30000"));
        session.put("redirectDelay", environment.getProperty("frontend.config.session.redirect-delay", "2000"));
        session.put("throttleDelay", environment.getProperty("frontend.config.session.throttle-delay", "30000"));
        config.put("session", session);
        
        // Data display configuration
        Map<String, Object> data = new HashMap<>();
        data.put("topInstrumentsLimit", environment.getProperty("frontend.config.data.top-instruments-limit", "10"));
        data.put("searchResultsLimit", environment.getProperty("frontend.config.data.search-results-limit", "20"));
        data.put("filteredResultsLimit", environment.getProperty("frontend.config.data.filtered-results-limit", "50"));
        data.put("refreshDelay", environment.getProperty("frontend.config.data.refresh-delay", "1000"));
        config.put("data", data);
        
        // Orders configuration
        Map<String, Object> orders = new HashMap<>();
        orders.put("largeQuantityThreshold", environment.getProperty("frontend.config.orders.large-quantity-threshold", "1000"));
        Map<String, String> messages = new HashMap<>();
        messages.put("success", "Order placed successfully!");
        messages.put("error", "Order placement failed. Please try again.");
        orders.put("messages", messages);
        config.put("orders", orders);
        
        // Instruments configuration
        Map<String, Object> instruments = new HashMap<>();
        String[] defaultTypes = environment.getProperty("frontend.config.instruments.default-types", "Options").split(",");
        instruments.put("defaultTypes", defaultTypes);
        String[] popularInstruments = environment.getProperty("frontend.config.instruments.popular", "RELIANCE,TCS,INFY,HDFC,ICICIBANK").split(",");
        instruments.put("popularInstruments", popularInstruments);
        String[] popularKeywords = environment.getProperty("frontend.config.instruments.keywords", "NIFTY,BANKNIFTY").split(",");
        instruments.put("popularKeywords", popularKeywords);
        config.put("instruments", instruments);
        
        // Exchanges configuration
        Map<String, Object> exchanges = new HashMap<>();
        String[] defaults = environment.getProperty("frontend.config.exchanges.defaults", "NSE,BSE").split(",");
        exchanges.put("defaults", defaults);
        exchanges.put("primary", environment.getProperty("frontend.config.exchanges.primary", "NSE"));
        config.put("exchanges", exchanges);
        
        return ResponseEntity.ok(config);
    }
}