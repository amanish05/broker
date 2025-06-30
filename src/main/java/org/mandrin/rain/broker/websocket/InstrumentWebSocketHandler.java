package org.mandrin.rain.broker.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mandrin.rain.broker.service.ReactiveInstrumentService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Traditional WebSocket handler for real-time instrument data updates
 * 
 * Supports:
 * - Real-time instrument filtering updates
 * - Live metadata updates (exchanges, types, underlyings)
 * - Subscription-based data streaming
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InstrumentWebSocketHandler extends TextWebSocketHandler {
    
    private final ReactiveInstrumentService reactiveInstrumentService;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        log.info("Instrument WebSocket connection established: {}", session.getId());
        
        // Send welcome message
        sendMessage(session, Map.of(
            "type", "connection", 
            "status", "connected",
            "endpoint", "instruments"
        ));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            log.debug("Received message from {}: {}", session.getId(), payload);
            
            Map<String, Object> request = objectMapper.readValue(payload, Map.class);
            String action = (String) request.get("action");
            
            switch (action) {
                case "subscribe":
                    handleSubscribe(session, request);
                    break;
                case "getMetadata":
                    handleGetMetadata(session, request);
                    break;
                case "ping":
                    sendMessage(session, Map.of("type", "pong", "timestamp", System.currentTimeMillis()));
                    break;
                default:
                    sendMessage(session, Map.of("type", "error", "message", "Unknown action: " + action));
            }
        } catch (Exception e) {
            log.error("Error handling message from {}: {}", session.getId(), e.getMessage());
            sendMessage(session, Map.of("type", "error", "message", "Invalid message format"));
        }
    }

    private void handleSubscribe(WebSocketSession session, Map<String, Object> request) throws Exception {
        String filterType = (String) request.get("filterType");
        String filterValue = (String) request.get("filterValue");
        
        log.info("Subscription request from {}: {} = {}", session.getId(), filterType, filterValue);
        
        // For now, send a mock response
        sendMessage(session, Map.of(
            "type", "subscriptionConfirmed",
            "filterType", filterType,
            "filterValue", filterValue,
            "count", 0 // Mock count
        ));
    }

    private void handleGetMetadata(WebSocketSession session, Map<String, Object> request) throws Exception {
        String metadataType = (String) request.get("metadataType");
        
        log.info("Metadata request from {}: {}", session.getId(), metadataType);
        
        // Use reactive service to get metadata
        switch (metadataType) {
            case "exchanges":
                reactiveInstrumentService.getMetadata("exchanges", "")
                    .collectList()
                    .subscribe(exchanges -> {
                        try {
                            sendMessage(session, Map.of(
                                "type", "metadata",
                                "metadataType", "exchanges",
                                "data", exchanges
                            ));
                        } catch (Exception e) {
                            log.error("Error sending exchanges metadata: {}", e.getMessage());
                        }
                    });
                break;
            case "underlyings":
                reactiveInstrumentService.getMetadata("underlyings", "")
                    .collectList()
                    .subscribe(underlyings -> {
                        try {
                            sendMessage(session, Map.of(
                                "type", "metadata",
                                "metadataType", "underlyings", 
                                "data", underlyings
                            ));
                        } catch (Exception e) {
                            log.error("Error sending underlyings metadata: {}", e.getMessage());
                        }
                    });
                break;
            default:
                sendMessage(session, Map.of("type", "error", "message", "Unknown metadata type: " + metadataType));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        log.info("Instrument WebSocket connection closed: {} ({})", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Instrument WebSocket transport error for {}: {}", session.getId(), exception.getMessage());
        sessions.remove(session.getId());
    }

    private void sendMessage(WebSocketSession session, Map<String, Object> message) throws Exception {
        if (session.isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
            log.debug("Sent message to {}: {}", session.getId(), json);
        }
    }

    /**
     * Broadcast a message to all connected sessions
     */
    public void broadcastMessage(Map<String, Object> message) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Error serializing broadcast message: {}", e.getMessage());
            return;
        }
        
        sessions.values().removeIf(session -> {
            if (!session.isOpen()) {
                return true; // Remove closed sessions
            }
            
            try {
                session.sendMessage(new TextMessage(json));
                return false; // Keep open sessions
            } catch (Exception e) {
                log.error("Error broadcasting to {}: {}", session.getId(), e.getMessage());
                return true; // Remove problematic sessions
            }
        });
    }
}