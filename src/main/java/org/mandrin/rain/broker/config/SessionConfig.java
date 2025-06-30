package org.mandrin.rain.broker.config;

import org.mandrin.rain.broker.service.SessionValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableScheduling
@Slf4j
public class SessionConfig {

    @Autowired
    private SessionValidationService sessionValidationService;

    /**
     * Scheduled task to clean up expired validation cache entries
     * Runs every 30 minutes
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30 minutes
    public void cleanupExpiredCache() {
        try {
            int sizeBefore = sessionValidationService.getCacheSize();
            sessionValidationService.clearExpiredCache();
            int sizeAfter = sessionValidationService.getCacheSize();
            
            if (sizeBefore > sizeAfter) {
                log.info("Cleaned up {} expired validation cache entries (before: {}, after: {})", 
                         sizeBefore - sizeAfter, sizeBefore, sizeAfter);
            }
        } catch (Exception e) {
            log.error("Error during scheduled cache cleanup: {}", e.getMessage());
        }
    }

    /**
     * Scheduled task to log session health metrics
     * Runs every hour
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // 1 hour
    public void logSessionMetrics() {
        try {
            int cacheSize = sessionValidationService.getCacheSize();
            log.info("Session health metrics - Validation cache size: {}", cacheSize);
        } catch (Exception e) {
            log.error("Error during session metrics logging: {}", e.getMessage());
        }
    }
}