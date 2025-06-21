package org.mandrin.rain.broker.controller;

import org.junit.jupiter.api.Test;
import org.mandrin.rain.broker.service.KiteTickerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.mockito.Mockito.*;

@WebMvcTest(TickerController.class)
class TickerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KiteTickerService tickerService;

    @Test
    void subscribe_ShouldParseTokensAndCallService() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("kite_access_token", "t");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/ticker/subscribe")
                .param("tokens", "1,2")
                .session(session))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(tickerService).subscribe(eq(session), eq(List.of(1L,2L)));
    }

    @Test
    void disconnect_ShouldInvokeService() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/ticker/disconnect"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(tickerService).disconnect();
    }
}
