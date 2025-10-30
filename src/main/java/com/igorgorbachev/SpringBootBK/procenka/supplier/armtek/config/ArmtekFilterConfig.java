package com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.armtek.filter")
public class ArmtekFilterConfig {
    private boolean groupByWarehouses = true;
    private int maxWarehousesPerProduct = 2;
    private boolean onlyInStock = true;
    private boolean onlyWithPrice = true;
    private boolean excludeAnalogs = true;
}
