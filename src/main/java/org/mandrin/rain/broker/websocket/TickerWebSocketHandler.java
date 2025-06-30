package org.mandrin.rain.broker.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
@Slf4j
public class TickerWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        log.info("WebSocket connection established: {}", session.getId());
        
        // Send welcome message
        sendMessage(session, Map.of("type", "connection", "status", "connected"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        log.info("WebSocket connection closed: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.debug("Received message from {}: {}", session.getId(), message.getPayload());
        // Handle incoming messages from frontend if needed
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        sessions.remove(session.getId());
    }

    public void broadcastTickerData(Object tickerData) {
        if (sessions.isEmpty()) {
            return;
        }

        String message;
        try {
            message = objectMapper.writeValueAsString(Map.of(
                "type", "ticker",
                "data", tickerData
            ));
        } catch (Exception e) {
            log.error("Error serializing ticker data", e);
            return;
        }

        sessions.values().removeIf(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                    return false;
                } else {
                    return true;
                }
            } catch (IOException e) {
                log.error("Error sending message to session {}: {}", session.getId(), e.getMessage());
                return true;
            }
        });
    }

    private void sendMessage(WebSocketSession session, Object data) {
        try {
            String message = objectMapper.writeValueAsString(data);
            session.sendMessage(new TextMessage(message));
        } catch (Exception e) {
            log.error("Error sending message to session {}: {}", session.getId(), e.getMessage());
        }
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }

    public void broadcast(String message) {
        sessions.values().removeIf(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                    return false;
                } else {
                    return true;
                }
            } catch (IOException e) {
                log.error("Error broadcasting message to session {}: {}", session.getId(), e.getMessage());
                return true;
            }
        });
    }
}