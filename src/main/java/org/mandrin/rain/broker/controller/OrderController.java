package org.mandrin.rain.broker.controller;

import jakarta.servlet.http.HttpSession;
import org.mandrin.rain.broker.model.TradeOrder;
import org.mandrin.rain.broker.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> place(@RequestBody TradeOrder req, HttpSession session) {
        log.info("/orders POST received for {}", req.getTradingsymbol());
        TradeOrder saved = orderService.placeOrder(session, req);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<TradeOrder> list() {
        List<TradeOrder> list = orderService.listOrders();
        log.debug("/orders GET returned {} entries", list.size());
        return list;
    }
}
