package org.mandrin.rain.broker.service;

import org.mandrin.rain.broker.model.Instrument;
import org.mandrin.rain.broker.repository.InstrumentRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Reactive service for instrument operations with consolidated filtering
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReactiveInstrumentService {
    
    private final InstrumentService instrumentService;
    private final InstrumentRepository repository;
    
    /**
     * Get instruments with comprehensive filtering support
     * 
     * @param filterType - "exchange", "underlying", "expiry", "all"
     * @param filterValue - the filter value
     * @param subFilter - optional secondary filter (e.g., expiry date for underlying)
     * @return Flux of instruments
     */
    public Flux<Instrument> getInstruments(String filterType, String filterValue, String subFilter) {
        log.debug("Getting instruments with filter: {} = {}, subFilter: {}", filterType, filterValue, subFilter);
        
        return Mono.fromCallable(() -> {
            switch (filterType.toLowerCase()) {
                case "exchange":
                    return repository.findByExchange(filterValue);
                    
                case "underlying":
                    if (subFilter != null && !subFilter.isEmpty()) {
                        // Filter by underlying and expiry
                        LocalDate expiry = LocalDate.parse(subFilter);
                        return repository.findByUnderlyingAndExpiry(filterValue, expiry);
                    } else {
                        // Filter by underlying only
                        return repository.findByUnderlyingAsset(filterValue);
                    }
                    
                case "expiry":
                    LocalDate expiryDate = LocalDate.parse(filterValue);
                    return repository.findAll().stream()
                            .filter(i -> i.getExpiry() != null && i.getExpiry().equals(expiryDate))
                            .toList();
                    
                case "all":
                default:
                    return repository.findAll();
            }
        }).flatMapMany(Flux::fromIterable);
    }
    
    /**
     * Get metadata with comprehensive support
     * 
     * @param metadataType - "exchanges", "types", "underlyings", "expiries"
     * @param filterValue - optional filter value for context-specific metadata
     * @return Flux of metadata values
     */
    public Flux<String> getMetadata(String metadataType, String filterValue) {
        log.debug("Getting metadata: {} with filter: {}", metadataType, filterValue);
        
        return Mono.fromCallable(() -> {
            switch (metadataType.toLowerCase()) {
                case "exchanges":
                    return instrumentService.listExchanges();
                    
                case "types":
                    if (filterValue != null && !filterValue.isEmpty()) {
                        return instrumentService.listInstrumentTypes(filterValue);
                    }
                    // Return all distinct types if no exchange specified
                    return repository.findAll().stream()
                            .map(Instrument::getInstrumentType)
                            .distinct()
                            .toList();
                    
                case "underlyings":
                    return instrumentService.listUnderlyingAssets();
                    
                case "expiries":
                    if (filterValue != null && !filterValue.isEmpty()) {
                        return instrumentService.getExpiryDatesByUnderlying(filterValue)
                                .stream()
                                .map(LocalDate::toString)
                                .toList();
                    }
                    // Return all distinct expiry dates
                    return repository.findAll().stream()
                            .map(Instrument::getExpiry)
                            .filter(java.util.Objects::nonNull)
                            .map(LocalDate::toString)
                            .distinct()
                            .sorted()
                            .toList();
                            
                default:
                    return List.<String>of();
            }
        }).flatMapMany(Flux::fromIterable);
    }
    
    /**
     * Get instrument names with token mapping
     */
    public Flux<Map<String, Object>> getInstrumentNames(String exchange, String type) {
        return Mono.fromCallable(() -> {
            return instrumentService.listNames(exchange, type).stream()
                    .map(item -> Map.<String, Object>of(
                        "instrumentToken", item.getInstrumentToken(),
                        "name", item.getName()
                    ))
                    .toList();
        }).flatMapMany(Flux::fromIterable);
    }
    
    /**
     * Refresh instruments data reactively
     */
    public Mono<Map<String, Object>> refreshInstruments(String exchange) {
        return Mono.fromCallable(() -> {
            if (exchange != null && !exchange.isEmpty()) {
                // Refresh specific exchange
                try {
                    List<Instrument> updated = instrumentService.fetchAndSave(exchange);
                    return Map.<String, Object>of(
                        "message", "Refresh completed for " + exchange,
                        "totalUpdated", updated.size(),
                        "exchangesProcessed", 1
                    );
                } catch (Exception e) {
                    log.error("Failed to refresh instruments for {}: {}", exchange, e.getMessage());
                    throw new RuntimeException("Failed to refresh instruments for " + exchange, e);
                }
            } else {
                // Refresh all exchanges
                int totalUpdated = 0;
                List<String> exchanges = instrumentService.listExchanges();
                
                if (exchanges.isEmpty()) {
                    exchanges = List.of("NSE", "BSE", "NFO", "BFO", "MCX");
                }
                
                for (String ex : exchanges) {
                    try {
                        List<Instrument> updated = instrumentService.fetchAndSave(ex);
                        totalUpdated += updated.size();
                        log.info("Updated {} instruments for exchange {}", updated.size(), ex);
                    } catch (Exception e) {
                        log.warn("Failed to update instruments for exchange {}: {}", ex, e.getMessage());
                    }
                }
                
                return Map.<String, Object>of(
                    "message", "Refresh completed",
                    "totalUpdated", totalUpdated,
                    "exchangesProcessed", exchanges.size()
                );
            }
        });
    }
}