package org.mandrin.rain.broker.controller;

import org.mandrin.rain.broker.model.Instrument;
import org.mandrin.rain.broker.service.InstrumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {
    private final InstrumentService instrumentService;

    @Autowired
    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @PostMapping("/{exchange}")
    public ResponseEntity<?> load(@PathVariable String exchange) throws IOException {
        List<Instrument> list = instrumentService.fetchAndSave(exchange);
        return ResponseEntity.ok(Map.of("saved", list.size()));
    }

    @GetMapping("/exchanges")
    public List<String> exchanges() {
        return instrumentService.listExchanges();
    }

    @GetMapping("/names")
    public List<Map<String, Object>> names() {
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (var v : instrumentService.listNameTokens()) {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("instrumentToken", v.getInstrumentToken());
            m.put("name", v.getName());
            result.add(m);
        }
        return result;
    }
}
