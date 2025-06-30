package org.mandrin.rain.broker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for Broker Online API documentation
 * 
 * Provides comprehensive API documentation for:
 * - V2 Reactive Instrument APIs
 * - Legacy Instrument APIs  
 * - Trading Operations
 * - WebSocket Endpoints
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI brokerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Broker Online - Algorithmic Trading API")
                        .description("""
                            **Broker Online** is a comprehensive algorithmic trading platform built with Spring Boot 3.1.5 
                            that integrates with Zerodha's Kite Connect API.
                            
                            ## Key Features
                            - **Real-time Market Data**: WebSocket integration for live price feeds
                            - **Order Management**: Place BUY/SELL orders with multiple product types
                            - **Portfolio Tracking**: View holdings and positions  
                            - **Instrument Discovery**: Advanced filtering by underlying assets and expiry dates
                            - **Reactive APIs**: High-performance reactive endpoints with streaming support
                            
                            ## API Versions
                            - **V2 API** (Recommended): Modern reactive router-based endpoints with consolidated filtering
                            - **Legacy API**: Backward compatible traditional REST endpoints
                            
                            ## Authentication
                            Most endpoints require authentication via Kite Connect OAuth flow.
                            WebSocket connections require valid session tokens.
                            """)
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("Broker Online Support")
                                .email("support@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Development Server (Port 8081)"),
                        new Server().url("http://localhost:8080").description("Production Server (Port 8080)")
                ))
                .tags(List.of(
                        new Tag().name("V2 Instruments").description("Modern reactive instrument APIs with advanced filtering"),
                        new Tag().name("V2 Metadata").description("Instrument metadata APIs (exchanges, types, underlyings)"),
                        new Tag().name("Legacy Instruments").description("Backward compatible instrument APIs"),
                        new Tag().name("Trading").description("Order placement and portfolio management"),
                        new Tag().name("Ticker").description("Real-time market data streaming"),
                        new Tag().name("Authentication").description("Kite Connect OAuth integration"),
                        new Tag().name("WebSocket").description("Real-time data streaming endpoints")
                ));
    }

    @Bean
    public GroupedOpenApi v2InstrumentsApi() {
        return GroupedOpenApi.builder()
                .group("v2-instruments")
                .displayName("V2 Instruments API")
                .pathsToMatch("/api/v2/instruments/**")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    operation.addTagsItem("V2 Instruments");
                    return operation;
                })
                .build();
    }

    @Bean
    public GroupedOpenApi legacyInstrumentsApi() {
        return GroupedOpenApi.builder()
                .group("legacy-instruments")
                .displayName("Legacy Instruments API")
                .pathsToMatch("/api/instruments/**")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    operation.addTagsItem("Legacy Instruments");
                    return operation;
                })
                .build();
    }

    @Bean
    public GroupedOpenApi tradingApi() {
        return GroupedOpenApi.builder()
                .group("trading")
                .displayName("Trading API")
                .pathsToMatch("/api/orders/**", "/api/portfolio/**")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    operation.addTagsItem("Trading");
                    return operation;
                })
                .build();
    }

    @Bean
    public GroupedOpenApi tickerApi() {
        return GroupedOpenApi.builder()
                .group("ticker")
                .displayName("Ticker API")
                .pathsToMatch("/api/ticker/**")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    operation.addTagsItem("Ticker");
                    return operation;
                })
                .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .displayName("Authentication API")
                .pathsToMatch("/login", "/logout", "/kite/**", "/api/session/**")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    operation.addTagsItem("Authentication");
                    return operation;
                })
                .build();
    }

    @Bean
    public GroupedOpenApi webSocketApi() {
        return GroupedOpenApi.builder()
                .group("websocket")
                .displayName("WebSocket API")
                .pathsToMatch("/ws/**")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    operation.addTagsItem("WebSocket");
                    return operation;
                })
                .build();
    }

    @Bean
    public GroupedOpenApi allApis() {
        return GroupedOpenApi.builder()
                .group("all")
                .displayName("All APIs")
                .pathsToMatch("/api/**", "/login", "/logout", "/kite/**")
                .build();
    }
}