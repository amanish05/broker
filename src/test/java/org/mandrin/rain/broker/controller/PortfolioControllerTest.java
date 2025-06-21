package org.mandrin.rain.broker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.mandrin.rain.broker.config.KiteConstants;
import org.mandrin.rain.broker.config.ApiConstants;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.springframework.http.HttpMethod;

@WebMvcTest(PortfolioController.class)
class PortfolioControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate restTemplate;

    @TestConfiguration
    static class RestTemplateTestConfig {
        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }

    @Test
    void getHoldings_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolio/holdings"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void getHoldings_Authenticated_ShouldReturnSuccess() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(KiteConstants.KITE_ACCESS_TOKEN_SESSION, "dummy_token");
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo(ApiConstants.HOLDINGS_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"status\":\"success\",\"data\":[]}", MediaType.APPLICATION_JSON));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolio/holdings").session(session))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        server.verify();
    }
}
