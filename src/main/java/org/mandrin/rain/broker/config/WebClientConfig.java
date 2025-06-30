package org.mandrin.rain.broker.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    @Primary
    @ConditionalOnProperty(name = "kite.dev.mock_session", havingValue = "false", matchIfMissing = true)
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
