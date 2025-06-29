package org.mandrin.rain.broker.service;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.mandrin.rain.broker.config.KiteConstants;
import org.mandrin.rain.broker.model.TradeOrder;
import org.mandrin.rain.broker.repository.TradeOrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {
    @Test
    void placeOrder_ShouldCallApiAndPersist() {
        ExchangeFunction fn = mock(ExchangeFunction.class);
        ClientResponse resp = ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body("{\"data\":{\"order_id\":\"1\"}}")
                .build();
        when(fn.exchange(any(ClientRequest.class))).thenReturn(Mono.just(resp));
        WebClient client = WebClient.builder().exchangeFunction(fn).build();
        TradeOrderRepository repo = mock(TradeOrderRepository.class);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        OrderService service = new OrderService(client, repo);
        setField(service, "apiKey", "key");
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(KiteConstants.KITE_ACCESS_TOKEN_SESSION)).thenReturn("token");
        TradeOrder req = new TradeOrder();
        req.setTradingsymbol("ABC");
        req.setExchange("NSE");
        req.setTransactionType("BUY");
        req.setQuantity(1);
        TradeOrder saved = service.placeOrder(session, req);
        assertEquals("1", saved.getOrderId());
        verify(repo).save(any());
        verify(fn).exchange(any(ClientRequest.class));
    }

    private void setField(Object target, String field, Object value) {
        try {
            java.lang.reflect.Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
