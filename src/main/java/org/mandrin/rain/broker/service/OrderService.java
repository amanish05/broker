package org.mandrin.rain.broker.service;

import jakarta.servlet.http.HttpSession;
import org.mandrin.rain.broker.config.ApiConstants;
import org.mandrin.rain.broker.config.ApiConstants;
import org.mandrin.rain.broker.model.TradeOrder;
import org.mandrin.rain.broker.repository.TradeOrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    @Value("${kite.api_key}")
    private String apiKey;

    private final WebClient webClient;
    private final TradeOrderRepository repository;

    /**
     * Place an order using Kite Connect and persist the details.
     */
    public TradeOrder placeOrder(HttpSession session, TradeOrder req) {
        log.info("Placing order for {} qty {}", req.getTradingsymbol(), req.getQuantity());
        String accessToken = (String) session.getAttribute(ApiConstants.KITE_ACCESS_TOKEN_SESSION);
        if (accessToken == null) {
            throw new IllegalStateException(ApiConstants.NOT_AUTHENTICATED_MSG);
        }
        String url = ApiConstants.ORDER_URL;
        HttpHeaders headers = new HttpHeaders();
        headers.set(ApiConstants.KITE_VERSION_HEADER, ApiConstants.KITE_VERSION);
        headers.set(ApiConstants.AUTH_HEADER,
                String.format(ApiConstants.AUTH_TOKEN_FORMAT, apiKey, accessToken));
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("tradingsymbol", req.getTradingsymbol());
        body.add("exchange", req.getExchange());
        body.add("transaction_type", req.getTransactionType());
        body.add("quantity", String.valueOf(req.getQuantity()));
        if (req.getPrice() != null) {
            body.add("price", String.valueOf(req.getPrice()));
        }
        body.add("order_type", ApiConstants.ORDER_TYPE_MARKET);
        body.add("product", ApiConstants.PRODUCT_MIS);

        Mono<Map> mono = webClient.post()
                .uri(url)
                .headers(h -> {
                    h.addAll(headers);
                    h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                })
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                .bodyToMono(Map.class);
        Map resp = mono.block();
        if (resp != null && resp.containsKey("data")) {
            Object data = ((Map) resp.get("data")).get("order_id");
            req.setOrderId(data != null ? data.toString() : null);
        }
        req.setPlacedAt(LocalDateTime.now());
        TradeOrder saved = repository.save(req);
        log.info("Order saved with id {} and orderId {}", saved.getId(), saved.getOrderId());
        return saved;
    }

    public java.util.List<TradeOrder> listOrders() {
        java.util.List<TradeOrder> list = repository.findAll();
        log.debug("listOrders -> {}", list.size());
        return list;
    }
}
