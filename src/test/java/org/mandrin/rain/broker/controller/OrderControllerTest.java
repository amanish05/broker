package org.mandrin.rain.broker.controller;

import org.junit.jupiter.api.Test;
import org.mandrin.rain.broker.model.TradeOrder;
import org.mandrin.rain.broker.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void place_ShouldReturnOk() throws Exception {
        TradeOrder o = new TradeOrder();
        o.setOrderId("1");
        when(orderService.placeOrder(any(), any())).thenReturn(o);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("kite_access_token", "t");
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .session(session))
                .andExpect(status().isOk());
        verify(orderService).placeOrder(any(), any());
    }

    @Test
    void list_ShouldReturnOk() throws Exception {
        when(orderService.listOrders()).thenReturn(java.util.List.of());
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());
        verify(orderService).listOrders();
    }
}
