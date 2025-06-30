package org.mandrin.rain.broker.mock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

@Configuration
@ConditionalOnProperty(name = "kite.dev.mock_session", havingValue = "true")
@EnableAsync
@Slf4j
public class MockWebClientConfig {

    @Value("${kite.base-url:https://api.kite.trade}")
    private String kiteBaseUrl;

    private JsonNode mockResponses;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    @PostConstruct
    public void loadMockResponses() {
        try {
            Path mockResponsesPath = Paths.get("test-configs/mock-api-responses.json");
            if (Files.exists(mockResponsesPath)) {
                log.info("Loading mock API responses from: {}", mockResponsesPath.toAbsolutePath());
                String content = Files.readString(mockResponsesPath);
                mockResponses = objectMapper.readTree(content);
                log.info("Successfully loaded mock API responses");
            } else {
                // Fallback to classpath resource
                ClassPathResource resource = new ClassPathResource("test-configs/mock-api-responses.json");
                if (resource.exists()) {
                    log.info("Loading mock API responses from classpath");
                    mockResponses = objectMapper.readTree(resource.getInputStream());
                    log.info("Successfully loaded mock API responses from classpath");
                } else {
                    log.warn("Mock API responses file not found, using minimal defaults");
                    mockResponses = createMinimalMockResponses();
                }
            }
        } catch (IOException e) {
            log.error("Failed to load mock API responses: {}", e.getMessage());
            mockResponses = createMinimalMockResponses();
        }
    }

    @Bean
    @Primary
    public WebClient mockWebClient() {
        log.info("Creating mock WebClient for Kite API interceptor");
        
        return WebClient.builder()
                .filter(this::mockKiteApiCalls)
                .build();
    }

    private Mono<ClientResponse> mockKiteApiCalls(ClientRequest request, ExchangeFunction next) {
        String uri = request.url().toString();
        String path = request.url().getPath();
        HttpMethod method = request.method();
        
        log.debug("Intercepting request: {} {}", method, uri);

        // Only intercept Kite API calls
        if (uri.contains("api.kite.trade") || uri.contains("kite.trade")) {
            log.info("Intercepting Kite API call: {} {}", method, path);
            return createMockResponse(path, method);
        }

        // Pass through non-Kite API calls
        return next.exchange(request);
    }

    private Mono<ClientResponse> createMockResponse(String path, HttpMethod method) {
        try {
            // Normalize the path for lookup
            String normalizedPath = normalizePath(path);
            log.debug("Looking up mock response for: {} {}", method, normalizedPath);

            JsonNode endpointNode = mockResponses.path("endpoints").path(normalizedPath);
            if (endpointNode.isMissingNode()) {
                log.debug("No specific mock for {}, trying wildcard patterns", normalizedPath);
                endpointNode = findWildcardMatch(normalizedPath);
            }

            JsonNode methodNode = endpointNode.path(method.name());
            if (methodNode.isMissingNode()) {
                log.warn("No mock response found for {} {}, returning generic success", method, normalizedPath);
                return createGenericSuccessResponse();
            }

            // Add realistic delays to simulate network latency
            int delay = random.nextInt(100) + 50; // 50-150ms delay
            
            String responseBody = objectMapper.writeValueAsString(methodNode);
            log.debug("Returning mock response for {} {} with {}ms delay", method, normalizedPath, delay);

            return Mono.delay(java.time.Duration.ofMillis(delay))
                    .then(Mono.just(ClientResponse.create(HttpStatus.OK)
                            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .header("X-Mock-Response", "true")
                            .body(responseBody)
                            .build()));

        } catch (Exception e) {
            log.error("Error creating mock response for {} {}: {}", method, path, e.getMessage());
            return createErrorResponse(e.getMessage());
        }
    }

    private String normalizePath(String path) {
        // Remove API version prefix if present
        if (path.startsWith("/api/")) {
            path = path.substring(4);
        }
        if (path.startsWith("/v3/")) {
            path = path.substring(3);
        }
        
        // Handle dynamic path parameters
        if (path.matches("/orders/[^/]+")) {
            return "/orders/{order_id}";
        }
        
        return path;
    }

    private JsonNode findWildcardMatch(String path) {
        // Check for wildcard patterns like /orders/{order_id}
        JsonNode endpoints = mockResponses.path("endpoints");
        
        if (path.startsWith("/orders/") && path.split("/").length == 3) {
            return endpoints.path("/orders/{order_id}");
        }
        
        return objectMapper.createObjectNode();
    }

    private Mono<ClientResponse> createGenericSuccessResponse() {
        String genericResponse = """
            {
                "status": "success",
                "data": {
                    "message": "Mock operation completed successfully"
                }
            }
            """;
        
        return Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-Mock-Response", "true")
                .body(genericResponse)
                .build());
    }

    private Mono<ClientResponse> createErrorResponse(String errorMessage) {
        String errorResponse = String.format("""
            {
                "status": "error",
                "message": "Mock API error: %s",
                "error_type": "MockException"
            }
            """, errorMessage);
        
        return Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("X-Mock-Response", "true")
                .body(errorResponse)
                .build());
    }

    private JsonNode createMinimalMockResponses() {
        try {
            String minimalJson = """
                {
                    "endpoints": {
                        "/session/token": {
                            "POST": {
                                "status": "success",
                                "data": {
                                    "user_id": "mock_user",
                                    "access_token": "mock_access_token",
                                    "user_type": "individual"
                                }
                            }
                        },
                        "/portfolio/holdings": {
                            "GET": {
                                "status": "success",
                                "data": []
                            }
                        },
                        "/orders": {
                            "GET": {
                                "status": "success",
                                "data": []
                            },
                            "POST": {
                                "status": "success",
                                "data": {
                                    "order_id": "MOCK_ORDER_001"
                                }
                            }
                        }
                    }
                }
                """;
            return objectMapper.readTree(minimalJson);
        } catch (Exception e) {
            log.error("Failed to create minimal mock responses: {}", e.getMessage());
            return objectMapper.createObjectNode();
        }
    }
}