package org.mandrin.rain.broker.handler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.mandrin.rain.broker.config.ApiConstants;
import org.mandrin.rain.broker.model.Instrument;
import org.mandrin.rain.broker.service.ReactiveInstrumentService;
import org.mandrin.rain.broker.service.InstrumentService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Map;

/**
 * Reactive handler for consolidated instrument API operations
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Tag(name = "V2 Instruments", description = "Modern reactive instrument APIs with advanced filtering")
public class InstrumentHandler {
    
    private final ReactiveInstrumentService reactiveInstrumentService;
    private final InstrumentService instrumentService;
    
    // ================== V2 API ENDPOINTS (NEW CONSOLIDATED APPROACH) ==================
    
    public Mono<ServerResponse> getAllInstruments(ServerRequest request) {
        log.debug("Getting all instruments");
        return ServerResponse.ok()
                .body(reactiveInstrumentService.getInstruments("all", "", ""), Instrument.class);
    }
    
    public Mono<ServerResponse> getInstrumentsByExchange(ServerRequest request) {
        String exchange = request.pathVariable("exchange");
        log.debug("Getting instruments for exchange: {}", exchange);
        return ServerResponse.ok()
                .body(reactiveInstrumentService.getInstruments("exchange", exchange, ""), Instrument.class);
    }
    
    public Mono<ServerResponse> getInstrumentsByUnderlying(ServerRequest request) {
        String underlying = request.pathVariable("underlying");
        log.debug("Getting instruments for underlying: {}", underlying);
        return ServerResponse.ok()
                .body(reactiveInstrumentService.getInstruments("underlying", underlying, ""), Instrument.class);
    }
    
    public Mono<ServerResponse> getInstrumentsByUnderlyingAndExpiry(ServerRequest request) {
        String underlying = request.pathVariable("underlying");
        String expiry = request.pathVariable("expiry");
        log.debug("Getting instruments for underlying: {} expiry: {}", underlying, expiry);
        return ServerResponse.ok()
                .body(reactiveInstrumentService.getInstruments("underlying", underlying, expiry), Instrument.class);
    }
    
    public Mono<ServerResponse> getInstrumentsByExpiry(ServerRequest request) {
        String expiry = request.pathVariable("expiry");
        log.debug("Getting instruments for expiry: {}", expiry);
        return ServerResponse.ok()
                .body(reactiveInstrumentService.getInstruments("expiry", expiry, ""), Instrument.class);
    }
    
    // ================== METADATA ENDPOINTS ==================
    
    public Mono<ServerResponse> getExchanges(ServerRequest request) {
        log.debug("Getting exchanges");
        return ServerResponse.ok()
                .body(reactiveInstrumentService.getMetadata("exchanges", ""), String.class);
    }
    
    public Mono<ServerResponse> getInstrumentTypes(ServerRequest request) {
        log.debug("Getting all instrument types");
        return ServerResponse.ok()
                .body(reactiveInstrumentService.getMetadata("types", ""), String.class);
    }
    
    public Mono<ServerResponse> getInstrumentTypesByExchange(ServerRequest request) {
        String exchange = request.pathVariable("exchange");
        log.debug("Getting instrument types for exchange: {}", exchange);
        return ServerResponse.ok()
                .body(reactiveInstrumentService.getMetadata("types", exchange), String.class);
    }
    
    public Mono<ServerResponse> getUnderlyingAssets(ServerRequest request) {
        log.debug("Getting underlying assets");
        return ServerResponse.ok()
                .body(reactiveInstrumentService.getMetadata("underlyings", ""), String.class);
    }
    
    public Mono<ServerResponse> getAllExpiries(ServerRequest request) {
        log.debug("Getting all expiry dates");
        return ServerResponse.ok()
                .body(reactiveInstrumentService.getMetadata("expiries", ""), String.class);
    }
    
    public Mono<ServerResponse> getExpiriesByUnderlying(ServerRequest request) {
        String underlying = request.pathVariable("underlying");
        log.debug("Getting expiry dates for underlying: {}", underlying);
        return ServerResponse.ok()
                .body(reactiveInstrumentService.getMetadata("expiries", underlying), String.class);
    }
    
    public Mono<ServerResponse> getInstrumentNames(ServerRequest request) {
        String exchange = request.pathVariable("exchange");
        String type = request.pathVariable("type");
        log.debug("Getting instrument names for {} {}", exchange, type);
        return ServerResponse.ok()
                .body(reactiveInstrumentService.getInstrumentNames(exchange, type), Map.class);
    }
    
    // ================== REFRESH ENDPOINTS ==================
    
    public Mono<ServerResponse> refreshAllInstruments(ServerRequest request) {
        log.info("Refreshing all instruments");
        return reactiveInstrumentService.refreshInstruments("")
                .flatMap(result -> ServerResponse.ok().bodyValue(result))
                .onErrorResume(error -> {
                    log.error("Failed to refresh instruments: {}", error.getMessage());
                    return ServerResponse.badRequest()
                            .bodyValue(Map.of("error", "Failed to refresh instruments: " + error.getMessage()));
                });
    }
    
    public Mono<ServerResponse> refreshInstrumentsByExchange(ServerRequest request) {
        String exchange = request.pathVariable("exchange");
        log.info("Refreshing instruments for exchange: {}", exchange);
        return reactiveInstrumentService.refreshInstruments(exchange)
                .flatMap(result -> ServerResponse.ok().bodyValue(result))
                .onErrorResume(error -> {
                    log.error("Failed to refresh instruments for {}: {}", exchange, error.getMessage());
                    return ServerResponse.badRequest()
                            .bodyValue(Map.of("error", "Failed to refresh instruments for " + exchange + ": " + error.getMessage()));
                });
    }
    
    // ================== LEGACY COMPATIBILITY ENDPOINTS ==================
    
    public Mono<ServerResponse> getInstrumentTypesLegacy(ServerRequest request) {
        String exchange = request.queryParam("exchange").orElse("");
        if (exchange.isEmpty()) {
            return ServerResponse.badRequest().bodyValue(Map.of("error", "exchange parameter is required"));
        }
        
        log.debug("Getting instrument types for exchange: {} (legacy)", exchange);
        return ServerResponse.ok()
                .body(reactiveInstrumentService.getMetadata("types", exchange), String.class);
    }
    
    public Mono<ServerResponse> getInstrumentNamesLegacy(ServerRequest request) {
        String exchange = request.queryParam("exchange").orElse("");
        String type = request.queryParam("type").orElse("");
        
        if (exchange.isEmpty() || type.isEmpty()) {
            return ServerResponse.badRequest()
                    .bodyValue(Map.of("error", "exchange and type parameters are required"));
        }
        
        log.debug("Getting instrument names for {} {} (legacy)", exchange, type);
        return ServerResponse.ok()
                .body(reactiveInstrumentService.getInstrumentNames(exchange, type), Map.class);
    }
    
    public Mono<ServerResponse> getExpiriesByUnderlyingLegacy(ServerRequest request) {
        String underlying = request.queryParam("underlying").orElse("");
        if (underlying.isEmpty()) {
            return ServerResponse.badRequest().bodyValue(Map.of("error", "underlying parameter is required"));
        }
        
        log.debug("Getting expiry dates for underlying: {} (legacy)", underlying);
        return ServerResponse.ok()
                .body(reactiveInstrumentService.getMetadata("expiries", underlying)
                        .map(LocalDate::parse), LocalDate.class);
    }
    
    public Mono<ServerResponse> getInstrumentsByUnderlyingLegacy(ServerRequest request) {
        String underlying = request.queryParam("underlying").orElse("");
        if (underlying.isEmpty()) {
            return ServerResponse.badRequest().bodyValue(Map.of("error", "underlying parameter is required"));
        }
        
        log.debug("Getting instruments for underlying: {} (legacy)", underlying);
        return ServerResponse.ok()
                .body(reactiveInstrumentService.getInstruments("underlying", underlying, ""), Instrument.class);
    }
    
    public Mono<ServerResponse> getInstrumentsByUnderlyingAndExpiryLegacy(ServerRequest request) {
        String underlying = request.queryParam("underlying").orElse("");
        String expiry = request.queryParam("expiry").orElse("");
        
        if (underlying.isEmpty() || expiry.isEmpty()) {
            return ServerResponse.badRequest()
                    .bodyValue(Map.of("error", "underlying and expiry parameters are required"));
        }
        
        log.debug("Getting instruments for underlying: {} expiry: {} (legacy)", underlying, expiry);
        return ServerResponse.ok()
                .body(reactiveInstrumentService.getInstruments("underlying", underlying, expiry), Instrument.class);
    }
    
    public Mono<ServerResponse> loadInstrumentsByExchangeLegacy(ServerRequest request) {
        String exchange = request.pathVariable("exchange");
        log.info("Loading instruments for {} (legacy)", exchange);
        
        return Mono.fromCallable(() -> {
            try {
                var instruments = instrumentService.fetchAndSave(exchange);
                return Map.of(ApiConstants.RESPONSE_KEY_SAVED, instruments.size());
            } catch (Exception e) {
                throw new RuntimeException("Failed to load instruments for " + exchange, e);
            }
        })
        .flatMap(result -> ServerResponse.ok().bodyValue(result))
        .onErrorResume(error -> {
            log.error("Failed to load instruments for {}: {}", exchange, error.getMessage());
            return ServerResponse.badRequest()
                    .bodyValue(Map.of("error", "Failed to load instruments for " + exchange + ": " + error.getMessage()));
        });
    }
}