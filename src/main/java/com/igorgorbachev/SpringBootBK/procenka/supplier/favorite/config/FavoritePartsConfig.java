package com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.config;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;

@Validated
@Component
@ConfigurationProperties(prefix = "app.favorit-parts")
public class FavoritePartsConfig {

    @NotBlank(message = "Base URL is required")
    private String baseUrl;

    @NotBlank(message = "API key is required")
    private String apiKey;

    @Min(1000) @Max(60000)
    private int timeout = 30000;

    @Min(1) @Max(10)
    private int maxAttempts = 3;

    @Min(1000)
    private long initialInterval = 2000L;

    @Min(1) @Max(5)
    private double multiplier = 1.5;

    @Min(5000)
    private long maxInterval = 10000L;

    // Новые параметры для оптимизации
    private boolean enableCache = true;
    private int cacheSize = 1000;
    private Duration cacheTtl = Duration.ofHours(1);
    private boolean enableMetrics = true;
    private int maxAnaloguesPerItem = 50;
    private boolean filterZeroQuantity = true;

    // Getters and Setters
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    public long getInitialInterval() { return initialInterval; }
    public void setInitialInterval(long initialInterval) { this.initialInterval = initialInterval; }
    public double getMultiplier() { return multiplier; }
    public void setMultiplier(double multiplier) { this.multiplier = multiplier; }
    public long getMaxInterval() { return maxInterval; }
    public void setMaxInterval(long maxInterval) { this.maxInterval = maxInterval; }
    public boolean isEnableCache() { return enableCache; }
    public void setEnableCache(boolean enableCache) { this.enableCache = enableCache; }
    public int getCacheSize() { return cacheSize; }
    public void setCacheSize(int cacheSize) { this.cacheSize = cacheSize; }
    public Duration getCacheTtl() { return cacheTtl; }
    public void setCacheTtl(Duration cacheTtl) { this.cacheTtl = cacheTtl; }
    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
    public int getMaxAnaloguesPerItem() { return maxAnaloguesPerItem; }
    public void setMaxAnaloguesPerItem(int maxAnaloguesPerItem) { this.maxAnaloguesPerItem = maxAnaloguesPerItem; }
    public boolean isFilterZeroQuantity() { return filterZeroQuantity; }
    public void setFilterZeroQuantity(boolean filterZeroQuantity) { this.filterZeroQuantity = filterZeroQuantity; }
}