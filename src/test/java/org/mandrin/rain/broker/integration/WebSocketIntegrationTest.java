package org.mandrin.rain.broker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mandrin.rain.broker.websocket.TickerWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TickerWebSocketHandler tickerWebSocketHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Timeout(10)
    public void testWebSocketConnection() throws Exception {
        String wsUrl = "ws://localhost:" + port + "/ws/ticker";
        StandardWebSocketClient client = new StandardWebSocketClient();
        
        CompletableFuture<String> messageFuture = new CompletableFuture<>();
        
        WebSocketHandler handler = new WebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                System.out.println("WebSocket connected");
            }

            @Override
            public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
                String payload = message.getPayload().toString();
                System.out.println("Received: " + payload);
                messageFuture.complete(payload);
            }

            @Override
            public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                messageFuture.completeExceptionally(exception);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                System.out.println("WebSocket disconnected");
            }

            @Override
            public boolean supportsPartialMessages() {
                return false;
            }
        };

        WebSocketSession session = client.doHandshake(handler, null, URI.create(wsUrl)).get(5, TimeUnit.SECONDS);
        assertTrue(session.isOpen());

        // Wait for connection message
        String connectionMessage = messageFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(connectionMessage);
        
        // Parse the connection message
        Map<String, Object> messageData = objectMapper.readValue(connectionMessage, Map.class);
        assertEquals("connection", messageData.get("type"));
        assertEquals("connected", messageData.get("status"));

        session.close();
    }

    @Test
    public void testWebSocketHandlerBroadcast() {
        // Test that the handler can broadcast messages
        int initialSessions = tickerWebSocketHandler.getActiveSessionCount();
        
        // Create mock ticker data
        Map<String, Object> mockTick = Map.of(
            "instrumentToken", 12345L,
            "lastPrice", 100.50,
            "volumeTraded", 1000L,
            "netChange", 2.5
        );

        // This should not throw an exception even with no active sessions
        assertDoesNotThrow(() -> tickerWebSocketHandler.broadcastTickerData(mockTick));
        
        assertEquals(initialSessions, tickerWebSocketHandler.getActiveSessionCount());
    }
}