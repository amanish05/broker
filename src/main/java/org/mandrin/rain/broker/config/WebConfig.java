package org.mandrin.rain.broker.config;

import org.mandrin.rain.broker.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/", "/home", "/api/**")
                .excludePathPatterns("/login", "/kite/callback", "/logout", 
                                   "/api/session/kite-access-token", "/api/session/status", "/api/session/validate",
                                   "/swagger-ui/**", "/v3/api-docs/**", 
                                   "/actuator/**", "/h2-console/**", "/js/**", "/css/**", "/images/**");
    }
}
