package org.mandrin.rain.broker.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TradingWorkflowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    public void testInstrumentApiEndpoints() {
        // Test exchanges endpoint
        ResponseEntity<String> exchangesResponse = restTemplate.getForEntity(
            getBaseUrl() + "/api/instruments/exchanges", String.class);
        assertEquals(HttpStatus.OK, exchangesResponse.getStatusCode());

        // Test that we can fetch instrument types (this may fail without Kite connection)
        ResponseEntity<String> typesResponse = restTemplate.getForEntity(
            getBaseUrl() + "/api/instruments/types?exchange=NSE", String.class);
        // Should return OK even if empty due to missing Kite connection
        assertTrue(typesResponse.getStatusCode().is2xxSuccessful() || 
                  typesResponse.getStatusCode().is4xxClientError());
    }

    @Test
    public void testOrderApiEndpoints() {
        // Test orders list endpoint
        ResponseEntity<String> ordersResponse = restTemplate.getForEntity(
            getBaseUrl() + "/api/orders", String.class);
        assertEquals(HttpStatus.OK, ordersResponse.getStatusCode());
    }

    @Test
    public void testTickerApiEndpoints() {
        // Test ticker subscriptions endpoint
        ResponseEntity<String> subscriptionsResponse = restTemplate.getForEntity(
            getBaseUrl() + "/api/ticker/subscriptions", String.class);
        assertEquals(HttpStatus.OK, subscriptionsResponse.getStatusCode());
    }

    @Test
    public void testHomePage() {
        // Test that home page loads
        ResponseEntity<String> homeResponse = restTemplate.getForEntity(
            getBaseUrl() + "/", String.class);
        assertEquals(HttpStatus.OK, homeResponse.getStatusCode());
        assertTrue(homeResponse.getBody().contains("Broker Online"));
    }

    @Test
    public void testApiDocumentation() {
        // Test that Swagger UI is accessible
        ResponseEntity<String> swaggerResponse = restTemplate.getForEntity(
            getBaseUrl() + "/swagger-ui/index.html", String.class);
        assertEquals(HttpStatus.OK, swaggerResponse.getStatusCode());
    }

    @Test
    public void testHealthEndpoint() {
        // Test actuator health endpoint
        ResponseEntity<String> healthResponse = restTemplate.getForEntity(
            getBaseUrl() + "/actuator/health", String.class);
        assertEquals(HttpStatus.OK, healthResponse.getStatusCode());
        assertTrue(healthResponse.getBody().contains("UP"));
    }
}