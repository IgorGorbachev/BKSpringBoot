package com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "tmtr.filter")
@Validated
public class TmtrFilterConfig {

    @NotNull
    private Boolean onlyOs1 = true;

    @NotNull
    private Boolean onlyInStock = true;

    @NotNull
    private Boolean onlyWithPrice = true;

    @NotNull
    private Boolean strictArticleMatch = false;

    @NotNull
    private Boolean enableDeliveryProbabilityFilter = false;

    @Min(0)
    @Max(100)
    private Integer minDeliveryProbability = 80;

    @Min(1)
    @Max(500)
    private Integer maxResults = 50;

    @Min(1)
    @Max(120)
    private Integer timeoutSeconds = 30;

    // Вспомогательные методы
    public boolean shouldFilterByOs() {
        return Boolean.TRUE.equals(onlyOs1);
    }

    public boolean shouldFilterStock() {
        return Boolean.TRUE.equals(onlyInStock);
    }

    public boolean shouldFilterPrice() {
        return Boolean.TRUE.equals(onlyWithPrice);
    }

    public boolean useStrictArticleMatch() {
        return Boolean.TRUE.equals(strictArticleMatch);
    }

    public boolean shouldFilterByDeliveryProbability() {
        return Boolean.TRUE.equals(enableDeliveryProbabilityFilter);
    }
}