package org.mandrin.rain.broker.service;

import com.zerodhatech.ticker.KiteTicker;
import jakarta.servlet.http.HttpSession;
import org.mandrin.rain.broker.config.ApiConstants;
import org.mandrin.rain.broker.websocket.TickerWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for managing the life cycle of a Kite Connect WebSocket
 * (KiteTicker) connection. It uses the {@code kite_access_token} stored in the
 * HTTP session to authenticate the connection and provides helper methods to
 * subscribe to instrument tokens.
 */
@Service
@Slf4j
public class KiteTickerService {
    
    @Value("${kite.api_key}")
    private String apiKey;

    private final TickerWebSocketHandler webSocketHandler;
    private KiteTicker kiteTicker;

    public KiteTickerService(TickerWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    /**
     * Lazily create and connect a {@link KiteTicker} instance if one does not
     * already exist or if the previous connection is closed.
     *
     * @param accessToken session specific access token
     * @return active {@link KiteTicker}
     */
    private synchronized KiteTicker getOrCreateTicker(String accessToken) {
        if (kiteTicker == null || !kiteTicker.isConnectionOpen()) {
            log.info("Creating new KiteTicker with API key: {}...", apiKey.substring(0, Math.min(6, apiKey.length())));
            kiteTicker = new KiteTicker(apiKey, accessToken);
            kiteTicker.setOnConnectedListener(() -> log.info("KiteTicker connected"));
            kiteTicker.setOnDisconnectedListener(() -> log.info("KiteTicker disconnected"));
            // Note: KiteTicker error listener interface is not easily accessible, 
            // so we handle errors in the try-catch block below
            kiteTicker.setOnTickerArrivalListener(ticks -> {
                for (var tick : ticks) {
                    log.debug("Tick received: {}", tick);
                    // Broadcast tick data to frontend WebSocket clients
                    webSocketHandler.broadcastTickerData(tick);
                }
            });
            
            try {
                kiteTicker.connect();
                log.info("KiteTicker connection attempt completed");
            } catch (Exception e) {
                log.error("Failed to connect KiteTicker: {}", e.getMessage());
                throw new RuntimeException("Unable to connect to Kite WebSocket. Please check your authentication and try again.", e);
            }
        }
        return kiteTicker;
    }

    /**
     * Establishes a WebSocket connection using the access token stored in the
     * provided HTTP session.
     *
     * @param session current user session containing the access token
     * @throws IllegalStateException if the token is missing
     */
    public KiteTicker connect(HttpSession session) {
        String token = (String) session.getAttribute(ApiConstants.KITE_ACCESS_TOKEN_SESSION);
        if (token == null || token.isEmpty()) {
            log.error("No access token found in session. User may not be authenticated with Kite Connect.");
            throw new IllegalStateException("Access token not found in session. Please login to Kite Connect first.");
        }
        
        log.info("Found access token in session, attempting to connect to KiteTicker");
        return getOrCreateTicker(token);
    }

    /**
     * Subscribe to streaming ticks for the given instrument tokens. The method
     * ensures the WebSocket connection is established before subscribing.
     *
     * @param session current user session
     * @param tokens  list of instrument tokens
     */
    public void subscribe(HttpSession session, List<Long> tokens) {
        connect(session);
        ArrayList<Long> list = new ArrayList<>(tokens);
        log.info("Subscribing to {} instruments", list.size());
        kiteTicker.subscribe(list);
        kiteTicker.setMode(list, KiteTicker.modeFull);
    }

    /**
     * Disconnect the active WebSocket connection if one exists.
     */
    public void disconnect() {
        if (kiteTicker != null) {
            log.info("Disconnecting ticker");
            kiteTicker.disconnect();
        }
    }
}
