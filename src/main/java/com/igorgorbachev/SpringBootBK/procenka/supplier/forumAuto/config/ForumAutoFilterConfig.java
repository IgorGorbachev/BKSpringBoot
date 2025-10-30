package com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Component
public class ForumAutoFilterConfig {
    private List<String> allowedWarehouses = List.of("YAR", "MSK");
    private int maxOriginals = 10;
    private boolean includeOutOfStockAnalogues = true;
    private boolean prioritizeReturnable = true;
    private int timeoutSeconds = 30;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ForumAutoFilterConfig that = (ForumAutoFilterConfig) o;
        return maxOriginals == that.maxOriginals && includeOutOfStockAnalogues == that.includeOutOfStockAnalogues && prioritizeReturnable == that.prioritizeReturnable && timeoutSeconds == that.timeoutSeconds && Objects.equals(allowedWarehouses, that.allowedWarehouses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowedWarehouses, maxOriginals, includeOutOfStockAnalogues, prioritizeReturnable, timeoutSeconds);
    }
}