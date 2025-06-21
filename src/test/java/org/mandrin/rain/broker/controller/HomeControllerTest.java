package org.mandrin.rain.broker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(HomeController.class)
class HomeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void home_ShouldReturnHomeView() throws Exception {
        org.springframework.mock.web.MockHttpSession session = new org.springframework.mock.web.MockHttpSession();
        session.setAttribute("kite_access_token", "dummy_token");
        mockMvc.perform(MockMvcRequestBuilders.get("/home").session(session))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
