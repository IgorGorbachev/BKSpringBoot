package com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.service.impl;

import com.igorgorbachev.SpringBootBK.exception.FavoritePartsException;
import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.service.SupplierService;
import com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.config.FavoritePartsConfig;
import com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.model.ApiResponse;
import com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.model.FavoritePartsGoods;
import com.igorgorbachev.SpringBootBK.procenka.dto.Warehouse;
import com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoritePartsServiceImpl implements FavoriteService, SupplierService {

    private final WebClient favoritePartsWebClient;
    private final FavoritePartsConfig favoritePartsConfig;
    private final RetryTemplate favoritePartsRetryTemplate;

    // Простой кэш для нормализации (без сложных зависимостей)
    private final Map<String, String> normalizedArticlesCache = Collections.synchronizedMap(new LinkedHashMap<String, String>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > 1000; // Ограничиваем размер
        }
    });

    @Override
    public String getSupplierName() {
        return "Favorite Parts";
    }

    @Override
    public boolean isAvailable() {
        // Упрощенный health check - просто проверяем конфигурацию
        return favoritePartsConfig.getBaseUrl() != null &&
                favoritePartsConfig.getApiKey() != null;
    }

    @Override
    public List<PartOfferDto> searchParts(String article, String brand) {
        try {
            log.info("Favorite Parts searching parts: article='{}', brand='{}'", article, brand);

            List<FavoritePartsGoods> goods = getPriceWithAnaloguesOptimized(article, brand);

            List<PartOfferDto> result = goods.stream()
                    .map(PartOfferDto::new)
                    .collect(Collectors.toList());

            log.info("Favorite Parts search completed: {} offers found", result.size());
            return result;

        } catch (Exception e) {
            log.error("Error searching parts in Favorite-Parts for article: {}, brand: {}. Error: {}",
                    article, brand, e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<FavoritePartsGoods> getPrice(String number, String brand, Boolean analogues, Boolean info) {
        long startTime = System.currentTimeMillis();
        String normalizedNumber = normalizeArticle(number);

        try {
            List<FavoritePartsGoods> result = favoritePartsRetryTemplate.execute(context -> {
                int retryCount = context.getRetryCount();
                if (retryCount > 0) {
                    log.warn("Retry attempt {} for number={}, brand={}", retryCount, normalizedNumber, brand);
                }

                ApiResponse response = executeApiCall(normalizedNumber, brand, analogues, info);
                return processApiResponse(response);

            });

            log.info("Successful request to Favorite-Parts: number={}, brand={}, found {} items, time: {}ms",
                    normalizedNumber, brand, result.size(), System.currentTimeMillis() - startTime);

            return result;

        } catch (Exception e) {
            log.error("All connection attempts to Favorite-Parts failed for number={}, brand={}: {}",
                    normalizedNumber, brand, e.getMessage());
            throw new FavoritePartsException("Service unavailable after retries for number: " + normalizedNumber, e);
        }
    }

    // ОСНОВНЫЕ МЕТОДЫ БЕЗ ИЗМЕНЕНИЙ

    public List<FavoritePartsGoods> getPriceWithAnaloguesOptimized(String number, String brand) {
        String normalizedNumber = normalizeArticle(number);
        List<FavoritePartsGoods> mainGoods = getPrice(normalizedNumber, brand, true, true);

        if (mainGoods == null || mainGoods.isEmpty()) {
            return mainGoods;
        }

        // Оптимизируем каждый товар
        return mainGoods.stream()
                .map(this::optimizeGoodsItem)
                .collect(Collectors.toList());
    }

    private FavoritePartsGoods optimizeGoodsItem(FavoritePartsGoods item) {
        // Сортируем склады основного товара
        if (item.getWarehouses() != null) {
            sortWarehouses(item.getWarehouses());
        }

        // Фильтруем и сортируем аналоги
        if (item.getAnalogues() != null && !item.getAnalogues().isEmpty()) {
            List<FavoritePartsGoods> optimizedAnalogues = filterAndSortAnalogues(item.getAnalogues());
            item.setAnalogues(optimizedAnalogues);
        }

        return item;
    }

    private List<FavoritePartsGoods> filterAndSortAnalogues(List<FavoritePartsGoods> analogues) {
        return analogues.stream()
                .filter(analogue -> analogue.getCount() != null && analogue.getCount() > 0)
                .filter(analogue -> analogue.getPrice() != null && analogue.getPrice() > 0)
                .sorted((a1, a2) -> {
                    if (a1.getPrice() == null && a2.getPrice() == null) return 0;
                    if (a1.getPrice() == null) return 1;
                    if (a2.getPrice() == null) return -1;
                    return Double.compare(a1.getPrice(), a2.getPrice());
                })
                .limit(20) // Ограничиваем количество аналогов
                .collect(Collectors.toList());
    }

    public void sortWarehouses(List<Warehouse> warehouses) {
        if (warehouses == null || warehouses.size() <= 1) {
            return;
        }

        warehouses.sort((w1, w2) -> {
            // 1. Приоритет: свои склады над внешними
            if (Boolean.TRUE.equals(w1.getOwn()) && !Boolean.TRUE.equals(w2.getOwn())) {
                return -1;
            }
            if (!Boolean.TRUE.equals(w1.getOwn()) && Boolean.TRUE.equals(w2.getOwn())) {
                return 1;
            }

            // 2. Приоритет: более низкая цена
            int priceComparison = Double.compare(
                    w1.getPrice() != null ? w1.getPrice() : Double.MAX_VALUE,
                    w2.getPrice() != null ? w2.getPrice() : Double.MAX_VALUE
            );
            if (priceComparison != 0) {
                return priceComparison;
            }

            // 3. Приоритет: более ранняя дата отгрузки
            if (w1.getShipmentDate() != null && w2.getShipmentDate() != null) {
                return w1.getShipmentDate().compareTo(w2.getShipmentDate());
            }
            if (w1.getShipmentDate() != null) {
                return -1;
            }
            if (w2.getShipmentDate() != null) {
                return 1;
            }

            return 0;
        });
    }

    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ

    private ApiResponse executeApiCall(String normalizedNumber, String brand, Boolean analogues, Boolean info) {
        log.debug("Sending request: number={}, brand={}, analogues={}, info={}",
                normalizedNumber, brand, analogues, info);

        return favoritePartsWebClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/hs/hsprice/")
                            .queryParam("key", favoritePartsConfig.getApiKey())
                            .queryParam("number", encodeValue(normalizedNumber));

                    if (brand != null && !brand.trim().isEmpty()) {
                        builder.queryParam("brand", encodeValue(brand));
                    }
                    if (Boolean.TRUE.equals(analogues)) {
                        builder.queryParam("analogues", "on");
                    }
                    if (Boolean.TRUE.equals(info)) {
                        builder.queryParam("info", "on");
                    }

                    return builder.build();
                })
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .timeout(Duration.ofSeconds(30)) // Фиксированный таймаут
                .block();
    }

    private List<FavoritePartsGoods> processApiResponse(ApiResponse response) {
        if (response == null) {
            log.warn("Empty response from API");
            return List.of();
        }

        if (response.hasError()) {
            log.error("API Error: {}", response.getError());
            throw new FavoritePartsException("API Error: " + response.getError());
        }

        List<FavoritePartsGoods> goods = response.getGoods();
        log.info("Successful response: found {} items", goods != null ? goods.size() : 0);

        return goods != null ? goods : List.of();
    }

    private String normalizeArticle(String article) {
        if (article == null) return null;

        return normalizedArticlesCache.computeIfAbsent(article,
                key -> key.replaceAll("[^a-zA-Z0-9]", "").toLowerCase());
    }

    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
