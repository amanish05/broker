package org.mandrin.rain.broker.controller;

import com.zerodhatech.ticker.KiteTicker;
import jakarta.servlet.http.HttpSession;
import org.mandrin.rain.broker.service.KiteTickerService;
import org.mandrin.rain.broker.service.SubscriptionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
@RequiredArgsConstructor
@Slf4j
public class TickerController {
    private final KiteTickerService tickerService;
    private final SubscriptionService subscriptionService;

    @PostMapping("/connect")
    public String connect(HttpSession session) {
        log.info("/ticker/connect invoked");
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
        log.info("Subscribing tokens {}", list);
        tickerService.subscribe(session, list);
        subscriptionService.saveAll(list);
        return "subscribed";
    }

    @GetMapping("/subscriptions")
    public java.util.List<Long> list() {
        List<Long> list = subscriptionService.listTokens();
        log.debug("Listing subscriptions -> {}", list);
        return list;
    }

    /**
     * Disconnect the active WebSocket connection.
     *
     * @return status message
     */
    @PostMapping("/disconnect")
    public String disconnect() {
        log.info("/ticker/disconnect invoked");
        tickerService.disconnect();
        return "disconnected";
    }
}
