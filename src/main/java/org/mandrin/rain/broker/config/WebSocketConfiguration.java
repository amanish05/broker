package org.mandrin.rain.broker.config;

import org.mandrin.rain.broker.websocket.TickerWebSocketHandler;
import org.mandrin.rain.broker.websocket.InstrumentWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Unified WebSocket configuration for all WebSocket endpoints
 * 
 * All endpoints use traditional Spring WebSocket for consistency and compatibility:
 * - /ws/ticker - Market ticker data streaming using TickerWebSocketHandler
 * - /ws/instruments - Real-time instrument data streaming using InstrumentWebSocketHandler
 * 
 * Benefits of this approach:
 * 1. Single configuration for all WebSocket endpoints
 * 2. Consistent behavior across all endpoints
 * 3. Better compatibility and easier debugging
 * 4. No mixing of reactive and traditional WebSocket frameworks
 */
@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final TickerWebSocketHandler tickerWebSocketHandler;
    private final InstrumentWebSocketHandler instrumentWebSocketHandler;

    public WebSocketConfiguration(TickerWebSocketHandler tickerWebSocketHandler, 
                                  InstrumentWebSocketHandler instrumentWebSocketHandler) {
        this.tickerWebSocketHandler = tickerWebSocketHandler;
        this.instrumentWebSocketHandler = instrumentWebSocketHandler;
    }

    /**
     * Configure all WebSocket endpoints
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Market ticker data streaming
        registry.addHandler(tickerWebSocketHandler, "/ws/ticker")
                .setAllowedOrigins("*");
                
        // Instrument data streaming  
        registry.addHandler(instrumentWebSocketHandler, "/ws/instruments")
                .setAllowedOrigins("*");
    }
}