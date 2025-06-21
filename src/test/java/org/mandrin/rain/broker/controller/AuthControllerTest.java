package org.mandrin.rain.broker.controller;

import org.junit.jupiter.api.Test;
import org.mandrin.rain.broker.service.KiteAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class KiteAuthServiceTestConfig {
        @Bean
        public org.mandrin.rain.broker.service.KiteAuthService kiteAuthService() {
            return new org.mandrin.rain.broker.service.KiteAuthService() {
                @Override
                public String getLoginUrl() {
                    return "https://kite.zerodha.com/oauth/authorize?api_key=test_key";
                }
            };
        }
    }

    @Test
    void login_ShouldRedirectToKite() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
    }
}
