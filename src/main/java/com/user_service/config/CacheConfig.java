package com.user_service.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "users", 
            "roles", 
            "menus",
            "managers",
            "stats"
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(800)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .recordStats());
        
        System.out.println("[Cache] User service cache initialized - 800 entries, 10min TTL");
        return cacheManager;
    }
}
