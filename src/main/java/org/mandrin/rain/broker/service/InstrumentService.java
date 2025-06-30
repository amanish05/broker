package org.mandrin.rain.broker.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.mandrin.rain.broker.config.ApiConstants;
import org.mandrin.rain.broker.model.Instrument;
import org.mandrin.rain.broker.repository.InstrumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstrumentService {
    @Value("${kite.base-url:https://api.kite.trade}")
    private String baseUrl;

    private final WebClient webClient;
    private final InstrumentRepository repository;

    public List<Instrument> fetchAndSave(String exchange) throws IOException {
        log.info("Fetching fresh instruments for exchange {} from Kite API", exchange);
        
        String url = baseUrl + ApiConstants.INSTRUMENTS_PATH + exchange;
        String body;
        try {
            body = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch instruments from Kite API for {}: {}", exchange, e.getMessage());
            // Return existing data if API fails
            List<Instrument> existing = repository.findByExchange(exchange);
            log.info("API failed, returning {} cached instruments for {}", existing.size(), exchange);
            return existing;
        }
        
        Reader in = new StringReader(body);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
        List<Instrument> newInstruments = new ArrayList<>();
        int duplicateCount = 0;
        
        for (CSVRecord r : records) {
            try {
                Instrument i = new Instrument();
                i.setInstrumentToken(Long.parseLong(r.get(ApiConstants.CSV_INSTRUMENT_TOKEN)));
                i.setExchangeToken(Long.parseLong(r.get(ApiConstants.CSV_EXCHANGE_TOKEN)));
                i.setTradingsymbol(r.get(ApiConstants.CSV_TRADING_SYMBOL));
                i.setName(r.get(ApiConstants.CSV_NAME));
                String price = r.get(ApiConstants.CSV_LAST_PRICE);
                i.setLastPrice(price.isEmpty() ? 0.0 : Double.parseDouble(price));
                String exp = r.get(ApiConstants.CSV_EXPIRY);
                if (exp != null && !exp.isEmpty()) {
                    i.setExpiry(LocalDate.parse(exp));
                }
                String strike = r.get(ApiConstants.CSV_STRIKE);
                i.setStrike(strike.isEmpty() ? 0.0 : Double.parseDouble(strike));
                i.setTickSize(Double.parseDouble(r.get(ApiConstants.CSV_TICK_SIZE)));
                i.setLotSize(Integer.parseInt(r.get(ApiConstants.CSV_LOT_SIZE)));
                i.setInstrumentType(r.get(ApiConstants.CSV_INSTRUMENT_TYPE));
                i.setSegment(r.get(ApiConstants.CSV_SEGMENT));
                i.setExchange(r.get(ApiConstants.CSV_EXCHANGE));
                newInstruments.add(i);
            } catch (Exception e) {
                log.warn("Failed to parse instrument record: {}", e.getMessage());
                // Continue processing other records
            }
        }
        
        // Save instruments, handling duplicates
        List<Instrument> savedInstruments = new ArrayList<>();
        for (Instrument instrument : newInstruments) {
            try {
                Instrument saved = repository.save(instrument);
                savedInstruments.add(saved);
            } catch (Exception e) {
                duplicateCount++;
                // Duplicate or constraint violation - update existing record
                try {
                    repository.findById(instrument.getInstrumentToken())
                            .ifPresent(existing -> {
                                // Update existing instrument with new data
                                existing.setLastPrice(instrument.getLastPrice());
                                existing.setName(instrument.getName());
                                repository.save(existing);
                                savedInstruments.add(existing);
                            });
                } catch (Exception updateEx) {
                    log.warn("Failed to update existing instrument {}: {}", instrument.getInstrumentToken(), updateEx.getMessage());
                }
            }
        }
        
        log.info("Processed {} instruments for exchange {}: {} new/updated, {} duplicates skipped", 
                newInstruments.size(), exchange, savedInstruments.size(), duplicateCount);
        return savedInstruments;
    }

    public List<String> listExchanges() {
        List<String> list = repository.findDistinctExchange();
        log.debug("listExchanges -> {}", list);
        return list;
    }

    public List<InstrumentRepository.NameTokenView> listNameTokens() {
        List<InstrumentRepository.NameTokenView> list = repository.findNameTokenAll();
        log.debug("listNameTokens returned {} items", list.size());
        return list;
    }

    public List<String> listInstrumentTypes(String exchange) {
        List<String> list = repository.findDistinctInstrumentType(exchange);
        log.debug("listInstrumentTypes for {} -> {}", exchange, list);
        return list;
    }

    public List<InstrumentRepository.NameTokenView> listNames(String exchange, String type) {
        List<InstrumentRepository.NameTokenView> list = repository.findNameToken(exchange, type);
        log.debug("listNames {} {} -> {}", exchange, type, list.size());
        return list;
    }
    
    public List<String> listUnderlyingAssets() {
        List<String> list = repository.findDistinctUnderlyingAssets();
        log.debug("listUnderlyingAssets -> {}", list);
        return list;
    }
    
    public List<Instrument> getInstrumentsByUnderlying(String underlying) {
        List<Instrument> list = repository.findByUnderlyingAsset(underlying);
        log.debug("getInstrumentsByUnderlying {} -> {} items", underlying, list.size());
        return list;
    }
    
    public List<java.time.LocalDate> getExpiryDatesByUnderlying(String underlying) {
        List<java.time.LocalDate> list = repository.findDistinctExpiryByUnderlying(underlying);
        log.debug("getExpiryDatesByUnderlying {} -> {}", underlying, list);
        return list;
    }
    
    public List<Instrument> getInstrumentsByUnderlyingAndExpiry(String underlying, java.time.LocalDate expiry) {
        List<Instrument> list = repository.findByUnderlyingAndExpiry(underlying, expiry);
        log.debug("getInstrumentsByUnderlyingAndExpiry {} {} -> {} items", underlying, expiry, list.size());
        return list;
    }
}
