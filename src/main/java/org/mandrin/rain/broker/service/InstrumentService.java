package org.mandrin.rain.broker.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
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
        log.info("Fetching instruments for exchange {}", exchange);
        String url = baseUrl + "/instruments/" + exchange;
        String body = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        Reader in = new StringReader(body);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
        List<Instrument> list = new ArrayList<>();
        for (CSVRecord r : records) {
            Instrument i = new Instrument();
            i.setInstrumentToken(Long.parseLong(r.get("instrument_token")));
            i.setExchangeToken(Long.parseLong(r.get("exchange_token")));
            i.setTradingsymbol(r.get("tradingsymbol"));
            i.setName(r.get("name"));
            String price = r.get("last_price");
            i.setLastPrice(price.isEmpty() ? 0.0 : Double.parseDouble(price));
            String exp = r.get("expiry");
            if (exp != null && !exp.isEmpty()) {
                i.setExpiry(LocalDate.parse(exp));
            }
            String strike = r.get("strike");
            i.setStrike(strike.isEmpty() ? 0.0 : Double.parseDouble(strike));
            i.setTickSize(Double.parseDouble(r.get("tick_size")));
            i.setLotSize(Integer.parseInt(r.get("lot_size")));
            i.setInstrumentType(r.get("instrument_type"));
            i.setSegment(r.get("segment"));
            i.setExchange(r.get("exchange"));
            list.add(i);
        }
        List<Instrument> saved = repository.saveAll(list);
        log.info("Saved {} instruments for exchange {}", saved.size(), exchange);
        return saved;
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
}
