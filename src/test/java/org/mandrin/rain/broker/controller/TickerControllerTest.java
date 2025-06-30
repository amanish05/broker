package org.mandrin.rain.broker.controller;

import org.junit.jupiter.api.Test;
import org.mandrin.rain.broker.service.KiteTickerService;
import org.mandrin.rain.broker.websocket.TickerWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.mandrin.rain.broker.service.SubscriptionService;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@WebMvcTest(TickerController.class)
class TickerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KiteTickerService tickerService;

    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    private TickerWebSocketHandler webSocketHandler;

    @TestConfiguration
    static class KiteTickerServiceTestConfig {
        @Bean
        public KiteTickerService tickerService(TickerWebSocketHandler webSocketHandler) {
            return new TestKiteTickerService(webSocketHandler);
        }
        static class TestKiteTickerService extends KiteTickerService {
            boolean subscribeCalled = false;
            boolean disconnectCalled = false;
            
            public TestKiteTickerService(TickerWebSocketHandler webSocketHandler) {
                super(webSocketHandler);
            }
            @Override
            public void subscribe(jakarta.servlet.http.HttpSession session, java.util.List<Long> tokens) {
                subscribeCalled = true;
            }
            @Override
            public void disconnect() {
                disconnectCalled = true;
            }
        }
    }

    @Test
    void subscribe_ShouldParseTokensAndCallService() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("kite_access_token", "t");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/ticker/subscribe")
                .param("tokens", "1,2")
                .session(session))
                .andExpect(MockMvcResultMatchers.status().isOk());
        org.junit.jupiter.api.Assertions.assertTrue(((KiteTickerServiceTestConfig.TestKiteTickerService)tickerService).subscribeCalled);
        verify(subscriptionService).saveAll(anyList());
    }

    @Test
    void subscriptions_ShouldReturnOk() throws Exception {
        when(subscriptionService.listTokens()).thenReturn(List.of(1L));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/ticker/subscriptions"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(subscriptionService).listTokens();
    }

    @Test
    void disconnect_ShouldInvokeService() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/ticker/disconnect"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        org.junit.jupiter.api.Assertions.assertTrue(((KiteTickerServiceTestConfig.TestKiteTickerService)tickerService).disconnectCalled);
    }
}
