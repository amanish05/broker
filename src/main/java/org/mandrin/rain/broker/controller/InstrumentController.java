package org.mandrin.rain.broker.controller;

import org.mandrin.rain.broker.model.Instrument;
import org.mandrin.rain.broker.service.InstrumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/instruments")
@RequiredArgsConstructor
@Slf4j
public class InstrumentController {
    private final InstrumentService instrumentService;

    @PostMapping("/{exchange}")
    public ResponseEntity<?> load(@PathVariable String exchange) throws IOException {
        log.info("Loading instruments for {}", exchange);
        List<Instrument> list = instrumentService.fetchAndSave(exchange);
        return ResponseEntity.ok(Map.of("saved", list.size()));
    }

    @GetMapping("/exchanges")
    public List<String> exchanges() {
        List<String> list = instrumentService.listExchanges();
        log.debug("exchanges -> {}", list);
        return list;
    }

    @GetMapping("/names")
    public List<Map<String, Object>> names(@RequestParam String exchange, @RequestParam String type) {
        log.info("Fetching instrument names for {} {}", exchange, type);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (var v : instrumentService.listNames(exchange, type)) {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("instrumentToken", v.getInstrumentToken());
            m.put("name", v.getName());
            result.add(m);
        }
        return result;
    }

    @GetMapping("/types")
    public List<String> types(@RequestParam String exchange) {
        List<String> list = instrumentService.listInstrumentTypes(exchange);
        log.debug("types for {} -> {}", exchange, list);
        return list;
    }
}
