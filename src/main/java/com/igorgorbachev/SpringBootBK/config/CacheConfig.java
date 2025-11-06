package com.igorgorbachev.SpringBootBK.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();

        // Настройка TTL для кэша
        cacheManager.setCacheNames(List.of("armtek_parts", "armtek_detailed", "tmtrBrands", "forumAutoOffers"));
        return cacheManager;
    }
}
