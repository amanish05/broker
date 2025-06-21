package org.mandrin.rain.broker.service;

import com.zerodhatech.ticker.KiteTicker;
import jakarta.servlet.http.HttpSession;
import org.mandrin.rain.broker.config.KiteConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for managing the life cycle of a Kite Connect WebSocket
 * (KiteTicker) connection. It uses the {@code kite_access_token} stored in the
 * HTTP session to authenticate the connection and provides helper methods to
 * subscribe to instrument tokens.
 */
@Service
public class KiteTickerService {
    private static final Logger logger = LogManager.getLogger(KiteTickerService.class);
    
    @Value("${kite.api_key}")
    private String apiKey;

    private KiteTicker kiteTicker;

    /**
     * Lazily create and connect a {@link KiteTicker} instance if one does not
     * already exist or if the previous connection is closed.
     *
     * @param accessToken session specific access token
     * @return active {@link KiteTicker}
     */
    private synchronized KiteTicker getOrCreateTicker(String accessToken) {
        if (kiteTicker == null || !kiteTicker.isConnectionOpen()) {
            kiteTicker = new KiteTicker(apiKey, accessToken);
            kiteTicker.setOnConnectedListener(() -> logger.info("KiteTicker connected"));
            kiteTicker.setOnDisconnectedListener(() -> logger.info("KiteTicker disconnected"));
            kiteTicker.setOnTickerArrivalListener(ticks -> {
                for (var tick : ticks) {
                    logger.info("Tick: {}", tick);
                }
            });
            kiteTicker.connect();
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
        String token = (String) session.getAttribute(KiteConstants.KITE_ACCESS_TOKEN_SESSION);
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Access token not found in session");
        }
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
        kiteTicker.subscribe(list);
        kiteTicker.setMode(list, KiteTicker.modeFull);
    }

    /**
     * Disconnect the active WebSocket connection if one exists.
     */
    public void disconnect() {
        if (kiteTicker != null) {
            kiteTicker.disconnect();
        }
    }
}
