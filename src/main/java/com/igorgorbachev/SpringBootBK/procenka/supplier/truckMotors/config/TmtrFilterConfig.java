package com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "tmtr.filter")
public class TmtrFilterConfig {
    private boolean onlyOs1 = true;
    private boolean onlyInStock = true;
    private boolean onlyWithPrice = true;
    private int maxResults = 50;
    private int timeoutSeconds = 30;
}