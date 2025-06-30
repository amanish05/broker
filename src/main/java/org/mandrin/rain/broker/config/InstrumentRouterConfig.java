package org.mandrin.rain.broker.config;

import org.mandrin.rain.broker.handler.InstrumentHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * Reactive router configuration for consolidated instrument API endpoints
 * 
 * Consolidates multiple endpoints into fewer, more flexible routes:
 * 
 * OLD APPROACH (10+ endpoints):
 * - GET /api/instruments/exchanges
 * - GET /api/instruments/types?exchange=X
 * - GET /api/instruments/names?exchange=X&type=Y
 * - GET /api/instruments/underlyings
 * - GET /api/instruments/expiry-dates?underlying=X
 * - GET /api/instruments/by-underlying?underlying=X
 * - GET /api/instruments/by-underlying-expiry?underlying=X&expiry=Y
 * - POST /api/instruments/{exchange}
 * - POST /api/instruments/refresh
 * 
 * NEW APPROACH (3 main endpoints):
 * - GET /api/v2/instruments/{filterType}[/{filterValue}[/{subFilter}]]
 * - GET /api/v2/instruments/metadata/{type}[?filter=value]
 * - POST /api/v2/instruments/refresh[/{exchange}]
 */
@Configuration
public class InstrumentRouterConfig {

    @Bean
    public RouterFunction<ServerResponse> instrumentRoutes(InstrumentHandler handler) {
        return route()
            // Main instrument data endpoints with flexible filtering
            .GET("/api/v2/instruments/all", handler::getAllInstruments)
            .GET("/api/v2/instruments/exchange/{exchange}", handler::getInstrumentsByExchange)
            .GET("/api/v2/instruments/underlying/{underlying}", handler::getInstrumentsByUnderlying)
            .GET("/api/v2/instruments/underlying/{underlying}/expiry/{expiry}", handler::getInstrumentsByUnderlyingAndExpiry)
            .GET("/api/v2/instruments/expiry/{expiry}", handler::getInstrumentsByExpiry)
            
            // Metadata endpoints (consolidated)
            .GET("/api/v2/instruments/metadata/exchanges", handler::getExchanges)
            .GET("/api/v2/instruments/metadata/types", handler::getInstrumentTypes)
            .GET("/api/v2/instruments/metadata/types/{exchange}", handler::getInstrumentTypesByExchange)
            .GET("/api/v2/instruments/metadata/underlyings", handler::getUnderlyingAssets)
            .GET("/api/v2/instruments/metadata/expiries", handler::getAllExpiries)
            .GET("/api/v2/instruments/metadata/expiries/{underlying}", handler::getExpiriesByUnderlying)
            
            // Legacy names endpoint for compatibility
            .GET("/api/v2/instruments/names/{exchange}/{type}", handler::getInstrumentNames)
            
            // Refresh operations
            .POST("/api/v2/instruments/refresh", handler::refreshAllInstruments)
            .POST("/api/v2/instruments/refresh/{exchange}", handler::refreshInstrumentsByExchange)
            
            // Backward compatibility routes (can be removed later)
            .GET("/api/instruments/exchanges", handler::getExchanges)
            .GET("/api/instruments/types", handler::getInstrumentTypesLegacy)
            .GET("/api/instruments/names", handler::getInstrumentNamesLegacy)
            .GET("/api/instruments/underlyings", handler::getUnderlyingAssets)
            .GET("/api/instruments/expiry-dates", handler::getExpiriesByUnderlyingLegacy)
            .GET("/api/instruments/by-underlying", handler::getInstrumentsByUnderlyingLegacy)
            .GET("/api/instruments/by-underlying-expiry", handler::getInstrumentsByUnderlyingAndExpiryLegacy)
            .POST("/api/instruments/{exchange}", handler::loadInstrumentsByExchangeLegacy)
            .POST("/api/instruments/refresh", handler::refreshAllInstruments)
            
            .build();
    }
}