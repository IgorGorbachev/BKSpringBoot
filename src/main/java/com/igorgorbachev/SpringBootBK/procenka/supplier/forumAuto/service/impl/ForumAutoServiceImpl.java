package com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igorgorbachev.SpringBootBK.exception.ForumAutoException;
import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.service.SupplierService;
import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.config.ForumAutoConfig;
import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.model.ForumAutoGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.service.ForumAutoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;


import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ForumAutoServiceImpl implements ForumAutoService, SupplierService {

    private final WebClient webClient;
    private final ForumAutoConfig forumAutoConfig;

    public ForumAutoServiceImpl(WebClient.Builder webClientBuilder, ForumAutoConfig forumAutoConfig) {
        this.forumAutoConfig = forumAutoConfig;
        this.webClient = webClientBuilder
                .baseUrl(forumAutoConfig.getBaseUrl())
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    @Override
    public List<ForumAutoGoods> listGoods(String article, String brand, Boolean cross, String gid) {
        try {
            String responseBody = webClient.get()
                    .uri(uriBuilder -> buildUri(uriBuilder, article, brand, cross, gid))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (responseBody == null || responseBody.trim().isEmpty()) {
                return List.of();
            }

            // Пытаемся разобрать как массив
            try {
                ObjectMapper mapper = new ObjectMapper();
                ForumAutoGoods[] goodsArray = mapper.readValue(responseBody, ForumAutoGoods[].class);
                return filterOriginalGoods(goodsArray, article, brand);
            } catch (Exception e) {
                // Если не получилось как массив, пробуем как объект с полем goods
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(responseBody);
                    JsonNode goodsNode = root.get("goods");
                    if (goodsNode != null && goodsNode.isArray()) {
                        ForumAutoGoods[] goodsArray = mapper.treeToValue(goodsNode, ForumAutoGoods[].class);
                        return filterOriginalGoods(goodsArray, article, brand);
                    }
                    // Если есть поле error
                    JsonNode errorNode = root.get("error");
                    if (errorNode != null) {
                        throw new ForumAutoException("API Error: " + errorNode.asText());
                    }
                    return List.of();
                } catch (Exception ex) {
                    throw new ForumAutoException("JSON parsing error: " + ex.getMessage() + ". Response: " + responseBody);
                }
            }

        } catch (WebClientResponseException e) {
            String errorMessage = String.format("HTTP error %d: %s",
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );
            throw new ForumAutoException(errorMessage);
        } catch (Exception e) {
            throw new ForumAutoException("Service error: " + e.getMessage());
        }
    }

    @Override
    public Mono<List<ForumAutoGoods>> listGoodsReactive(String article, String brand, Boolean cross, String gid) {
        return webClient.get()
                .uri(uriBuilder -> buildUri(uriBuilder, article, brand, cross, gid))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .flatMap(responseBody -> {
                    try {
                        if (responseBody == null || responseBody.trim().isEmpty()) {
                            return Mono.just(List.<ForumAutoGoods>of());
                        }

                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(responseBody);

                        ForumAutoGoods[] goodsArray = null;

                        // Пробуем разные форматы ответа
                        JsonNode goodsNode = root.get("goods");
                        if (goodsNode != null && goodsNode.isArray()) {
                            goodsArray = mapper.treeToValue(goodsNode, ForumAutoGoods[].class);
                        } else if (root.isArray()) {
                            goodsArray = mapper.treeToValue(root, ForumAutoGoods[].class);
                        }

                        if (goodsArray != null) {
                            List<ForumAutoGoods> filteredGoods = filterOriginalGoods(goodsArray, article, brand);
                            return Mono.just(filteredGoods);
                        }

                        JsonNode errorNode = root.get("error");
                        if (errorNode != null) {
                            return Mono.error(new ForumAutoException("API Error: " + errorNode.asText()));
                        }

                        return Mono.just(List.<ForumAutoGoods>of());
                    } catch (Exception e) {
                        return Mono.error(new ForumAutoException("JSON parsing error: " + e.getMessage()));
                    }
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    String errorMessage = String.format("HTTP error %d: %s",
                            e.getStatusCode().value(),
                            e.getResponseBodyAsString()
                    );
                    return Mono.error(new ForumAutoException(errorMessage));
                });
    }

    /**
     * Фильтрует только оригинальные детали (не аналоги) по артикулу и бренду
     * с учетом возвратности детали и выводит оба основных склада
     */
    private List<ForumAutoGoods> filterOriginalGoods(ForumAutoGoods[] goodsArray, String requestedArticle, String requestedBrand) {
        if (goodsArray == null || goodsArray.length == 0) {
            return List.of();
        }

        // Нормализуем запрашиваемые значения для сравнения
        String normalizedRequestedArticle = normalizeString(requestedArticle);
        String normalizedRequestedBrand = requestedBrand != null ? normalizeString(requestedBrand) : null;

        log.info("ForumAuto filtering: requested article='{}' (normalized: '{}'), brand='{}'",
                requestedArticle, normalizedRequestedArticle, requestedBrand);

        // ВРЕМЕННО ЛОГИРУЕМ ВСЕ ТОВАРЫ ДЛЯ ДИАГНОСТИКИ
        log.info("ForumAuto ALL goods before filtering ({} items):", goodsArray.length);
        Arrays.stream(goodsArray)
                .filter(goods -> goods != null)
                .forEach(goods ->
                        log.info(" - Article: '{}', Brand: '{}', Price: {}, Quantity: {}, Returnable: {}, Warehouse: {}",
                                goods.getArt(), goods.getBrand(), goods.getPrice(),
                                goods.getQuantity(), goods.getIsReturnable(), goods.getWarehouse())
                );

        List<ForumAutoGoods> filteredGoods = Arrays.stream(goodsArray)
                .filter(goods -> goods != null)
                // Фильтруем только товары в наличии
                .filter(goods -> goods.getQuantity() != null && goods.getQuantity() > 0)
                // Фильтруем товары с валидной ценой
                .filter(goods -> goods.getPrice() != null && goods.getPrice() > 0)
                // ВРЕМЕННО УБИРАЕМ ФИЛЬТР ПО ВОЗВРАТНОСТИ - включаем все детали
                // .filter(goods -> goods.getIsReturnable() != null && goods.getIsReturnable() == 1)
                // Фильтруем только оригинальные детали (не аналоги)
                .filter(goods -> isOriginalGoods(goods, normalizedRequestedArticle, normalizedRequestedBrand))
                // Сортируем по цене (по возрастанию)
                .sorted(Comparator.comparing(ForumAutoGoods::getPrice))
                // Берем до 2 складов с наименьшей ценой
                .limit(2)
                .collect(Collectors.toList());

        // Логируем для отладки
        log.info("ForumAuto found {} goods after filtering", filteredGoods.size());
        if (!filteredGoods.isEmpty()) {
            log.info("ForumAuto filtered goods:");
            for (ForumAutoGoods goods : filteredGoods) {
                log.info(" - Article: '{}', Brand: '{}', Price: {}, Quantity: {}, Returnable: {}, Warehouse: {}",
                        goods.getArt(), goods.getBrand(), goods.getPrice(),
                        goods.getQuantity(), goods.getIsReturnable(), goods.getWarehouse());
            }
        } else {
            log.info("ForumAuto: No goods found after filtering. Checking what was filtered out...");

            // Диагностика: почему товары отфильтровались
            long totalGoods = Arrays.stream(goodsArray).filter(g -> g != null).count();
            long withStock = Arrays.stream(goodsArray)
                    .filter(g -> g != null && g.getQuantity() != null && g.getQuantity() > 0)
                    .count();
            long withPrice = Arrays.stream(goodsArray)
                    .filter(g -> g != null && g.getPrice() != null && g.getPrice() > 0)
                    .count();
            long originalMatch = Arrays.stream(goodsArray)
                    .filter(g -> g != null && isOriginalGoods(g, normalizedRequestedArticle, normalizedRequestedBrand))
                    .count();

            log.info("ForumAuto filtering diagnostics:");
            log.info(" - Total goods: {}", totalGoods);
            log.info(" - With stock: {}", withStock);
            log.info(" - With price: {}", withPrice);
            log.info(" - Original match: {}", originalMatch);
        }

        return filteredGoods;
    }

    /**
     * Проверяет, является ли товар оригинальным (не аналогом)
     * Сравнивает артикул и бренд с запрошенными значениями
     */
    private boolean isOriginalGoods(ForumAutoGoods goods, String requestedArticle, String requestedBrand) {
        if (goods.getArt() == null) {
            return false;
        }

        // Нормализуем артикул и бренд из товара
        String normalizedGoodsArticle = normalizeString(goods.getArt());
        String normalizedGoodsBrand = goods.getBrand() != null ? normalizeString(goods.getBrand()) : null;

        // ГИБКОЕ сравнение артикулов - учитываем разные форматы
        boolean articleMatches = isArticleMatch(normalizedGoodsArticle, requestedArticle);

        // Если бренд был указан в запросе, сравниваем и бренды
        boolean brandMatches = requestedBrand == null ||
                (normalizedGoodsBrand != null && normalizedGoodsBrand.equals(requestedBrand));

        log.debug("ForumAuto article match: '{}' vs '{}' -> {}",
                normalizedGoodsArticle, requestedArticle, articleMatches);
        log.debug("ForumAuto brand match: '{}' vs '{}' -> {}",
                normalizedGoodsBrand, requestedBrand, brandMatches);

        return articleMatches && brandMatches;
    }

    private boolean isArticleMatch(String goodsArticle, String requestedArticle) {
        if (goodsArticle == null || requestedArticle == null) {
            return false;
        }

        // 1. Прямое сравнение после нормализации
        if (goodsArticle.equals(requestedArticle)) {
            return true;
        }

        // 2. Убираем ВСЕ не-цифровые символы и сравниваем
        String digitsOnlyGoods = goodsArticle.replaceAll("[^0-9]", "");
        String digitsOnlyRequested = requestedArticle.replaceAll("[^0-9]", "");

        if (digitsOnlyGoods.equals(digitsOnlyRequested) && !digitsOnlyGoods.isEmpty()) {
            log.info("ForumAuto: article matched by digits only: '{}' -> '{}'",
                    goodsArticle, digitsOnlyGoods);
            return true;
        }

        // 3. Проверяем частичное совпадение (если один артикул содержит другой)
        if (goodsArticle.contains(requestedArticle) || requestedArticle.contains(goodsArticle)) {
            log.info("ForumAuto: article partial match: '{}' contains '{}'",
                    goodsArticle, requestedArticle);
            return true;
        }

        return false;
    }


    /**
     * Нормализует строку для сравнения: убирает пробелы, приводит к нижнему регистру
     */
    private String normalizeString(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("\\s+", "").toLowerCase();
    }

    // Вспомогательный метод для построения URI
    private URI buildUri(UriBuilder uriBuilder, String article, String brand, Boolean cross, String gid) {
        UriBuilder builder = uriBuilder
                .path("/listGoods")
                .queryParam("login", forumAutoConfig.getLogin())
                .queryParam("pass", forumAutoConfig.getPassword());

        if (article != null && !article.trim().isEmpty()) {
            builder.queryParam("art", article.trim());
        }
        if (brand != null && !brand.trim().isEmpty()) {
            builder.queryParam("br", brand.trim());
        }
        if (cross != null) {
            builder.queryParam("cross", cross ? 1 : 0);
        }
        if (gid != null && !gid.trim().isEmpty()) {
            builder.queryParam("gid", gid.trim());
        }

        return builder.build();
    }

    // Дополнительные методы API
    @Override
    public Mono<String> getClientInfo() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/clientinfo")
                        .queryParam("login", forumAutoConfig.getLogin())
                        .queryParam("pass", forumAutoConfig.getPassword())
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }

    @Override
    public String getSupplierName() {
        return "Forum-Auto";
    }

    @Override
    public List<PartOfferDto> searchParts(String article, String brand) {
        try {
            log.info("ForumAuto searchParts called: article='{}', brand='{}'", article, brand);

            // Ищем БЕЗ кросс-поиска, только оригинальные детали
            List<ForumAutoGoods> goods = this.listGoods(article, brand, false, null);

            log.info("ForumAuto search completed: found {} goods", goods.size());

            // Преобразуем в DTO (будет до 2 товаров с разных складов)
            List<PartOfferDto> result = goods.stream()
                    .map(good -> new PartOfferDto(good))
                    .collect(Collectors.toList());

            log.info("ForumAuto converted to {} DTOs", result.size());
            return result;

        } catch (Exception e) {
            // Логируем ошибку, но не прерываем выполнение
            log.error("Ошибка при поиске в Forum-Auto for article: {}, brand: {}. Error: {}",
                    article, brand, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}