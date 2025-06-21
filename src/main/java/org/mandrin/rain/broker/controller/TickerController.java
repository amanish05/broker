package org.mandrin.rain.broker.controller;

import com.zerodhatech.ticker.KiteTicker;
import jakarta.servlet.http.HttpSession;
import org.mandrin.rain.broker.service.KiteTickerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoints for interacting with the {@link org.mandrin.rain.broker.service.KiteTickerService}.
 * Allows clients to subscribe to live market data and disconnect from the
 * WebSocket stream.
 */
@RestController
@RequestMapping("/api/ticker")
public class TickerController {
    private final KiteTickerService tickerService;

    @Autowired
    public TickerController(KiteTickerService tickerService) {
        this.tickerService = tickerService;
    }

    @PostMapping("/connect")
    public String connect(HttpSession session) {
        KiteTicker ticker = tickerService.connect(session);
        return "connected";
    }

    /**
     * Subscribe the current user to a comma separated list of instrument
     * tokens.
     *
     * @param session  current HTTP session containing the access token
     * @return success message
     */
    @PostMapping("/subscribe")
    public String subscribe(@RequestParam("tokens") String tokens, HttpSession session) {
        List<Long> list = Arrays.stream(tokens.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
        tickerService.subscribe(session, list);
        return "subscribed";
    }

    /**
     * Disconnect the active WebSocket connection.
     *
     * @return status message
     */
    @PostMapping("/disconnect")
    public String disconnect() {
        tickerService.disconnect();
        return "disconnected";
    }
}
