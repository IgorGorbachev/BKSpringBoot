package com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.health;

import com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FavoritePartsHealthCheck {

    private final FavoriteService favoriteService;

    public Map<String, Object> health() {
        Map<String, Object> healthInfo = new HashMap<>();

        try {
            long startTime = System.currentTimeMillis();

            // Простой тестовый запрос
            var result = favoriteService.getPrice("TEST123", "TEST", false, false);

            long responseTime = System.currentTimeMillis() - startTime;

            healthInfo.put("status", "UP");
            healthInfo.put("responseTime", responseTime + "ms");
            healthInfo.put("itemsFound", result.size());
            healthInfo.put("details", "Service is operational");

        } catch (Exception e) {
            log.warn("Favorite Parts health check failed: {}", e.getMessage());
            healthInfo.put("status", "DOWN");
            healthInfo.put("error", e.getMessage());
            healthInfo.put("details", "Service is unavailable");
        }

        return healthInfo;
    }

    public boolean isHealthy() {
        try {
            favoriteService.getPrice("TEST123", "TEST", false, false);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}