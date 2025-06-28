package org.mandrin.rain.broker.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.mandrin.rain.broker.model.Instrument;
import org.mandrin.rain.broker.repository.InstrumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class InstrumentService {
    @Value("${kite.base-url:https://api.kite.trade}")
    private String baseUrl;

    private final WebClient webClient;
    private final InstrumentRepository repository;

    @Autowired
    public InstrumentService(WebClient webClient, InstrumentRepository repository) {
        this.webClient = webClient;
        this.repository = repository;
    }

    public List<Instrument> fetchAndSave(String exchange) throws IOException {
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
        return repository.saveAll(list);
    }

    public List<String> listExchanges() {
        return repository.findDistinctExchange();
    }

    public List<InstrumentRepository.NameTokenView> listNameTokens() {
        return repository.findNameTokenAll();
    }
}
