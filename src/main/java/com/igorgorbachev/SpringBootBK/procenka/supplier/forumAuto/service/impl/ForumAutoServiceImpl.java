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
import java.util.Objects;
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

//    @Override
//    public List<PartOfferDto> searchParts(String article, String brand) {
//        try {
//            log.info("ForumAuto searchParts called: article='{}', brand='{}'", article, brand);
//
//            // Ищем БЕЗ кросс-поиска, только оригинальные детали
//            List<ForumAutoGoods> goods = this.listGoods(article, brand, false, null);
//
//            log.info("ForumAuto search completed: found {} goods", goods.size());
//
//            // Преобразуем в DTO (будет до 2 товаров с разных складов)
//            List<PartOfferDto> result = goods.stream()
//                    .map(good -> new PartOfferDto(good))
//                    .collect(Collectors.toList());
//
//            log.info("ForumAuto converted to {} DTOs", result.size());
//            return result;
//
//        } catch (Exception e) {
//            // Логируем ошибку, но не прерываем выполнение
//            log.error("Ошибка при поиске в Forum-Auto for article: {}, brand: {}. Error: {}",
//                    article, brand, e.getMessage(), e);
//            return List.of();
//        }
//    }

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
            log.error("Ошибка при поиске в Forum-Auto for article: {}, brand: {}. Error: {}",
                    article, brand, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Новый метод для поиска аналогов (кросс-поиск)
     */
    public List<PartOfferDto> searchAnalogues(String article, String brand) {
        try {
            log.info("ForumAuto searchAnalogues called: article='{}', brand='{}'", article, brand);

            // Ищем С кросс-поиском - все товары для этой модели
            List<ForumAutoGoods> allGoods = this.listGoodsWithCross(article, brand, true, null);

            log.info("ForumAuto analogues search completed: found {} total goods", allGoods.size());

            // Фильтруем только аналоги (другие бренды) - ВСЕ товары кроме точных совпадений
            List<PartOfferDto> result = allGoods.stream()
                    .filter(good -> brand == null ||
                            (good.getBrand() != null && !normalizeString(good.getBrand()).equals(normalizeString(brand))))
                    // Дополнительная фильтрация: убираем товары с нулевой ценой или количеством
                    .filter(good -> good.getPrice() != null && good.getPrice() > 0)
                    .filter(good -> good.getQuantity() != null && good.getQuantity() > 0)
                    .map(good -> new PartOfferDto(good))
                    .collect(Collectors.toList());

            log.info("ForumAuto analogues filtered to {} DTOs (other brands)", result.size());

            // Логируем все найденные аналоги для отладки
            if (!result.isEmpty()) {
                log.info("ForumAuto analogues found (first 10):");
                result.stream()
                        .limit(10)
                        .forEach(dto ->
                                log.info(" - Brand: '{}', Article: '{}', Price: {}, Quantity: {}",
                                        dto.getBrand(), dto.getOriginalArticle(), dto.getPrice(), dto.getQuantityAvailable())
                        );
                if (result.size() > 10) {
                    log.info("... and {} more analogues", result.size() - 10);
                }
            }

            return result;

        } catch (Exception e) {
            log.error("Ошибка при поиске аналогов в Forum-Auto for article: {}, brand: {}. Error: {}",
                    article, brand, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Новый метод для поиска с кросс-поиском БЕЗ фильтрации оригиналов
     */
    private List<ForumAutoGoods> listGoodsWithCross(String article, String brand, Boolean cross, String gid) {
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
                // Возвращаем ВСЕ товары без фильтрации по артикулу
                List<ForumAutoGoods> allGoods = Arrays.stream(goodsArray)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                log.info("Raw goods from API: {} items", allGoods.size());
                return allGoods;
            }

            return List.of();

        } catch (Exception e) {
            log.error("Error getting goods with cross: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Фильтрует ВСЕ товары (включая аналоги) по артикулу
     * Без ограничения по бренду и без ограничения количества
     */
    private List<ForumAutoGoods> filterAllGoods(ForumAutoGoods[] goodsArray, String requestedArticle, String requestedBrand) {
        if (goodsArray == null || goodsArray.length == 0) {
            return List.of();
        }

        // Нормализуем запрашиваемые значения для сравнения
        String normalizedRequestedArticle = normalizeString(requestedArticle);

        log.info("ForumAuto filtering ALL goods: requested article='{}' (normalized: '{}'), total goods: {}",
                requestedArticle, normalizedRequestedArticle, goodsArray.length);

        List<ForumAutoGoods> filteredGoods = Arrays.stream(goodsArray)
                .filter(goods -> goods != null)
                // Фильтруем только товары в наличии
                .filter(goods -> goods.getQuantity() != null && goods.getQuantity() > 0)
                // Фильтруем товары с валидной ценой
                .filter(goods -> goods.getPrice() != null && goods.getPrice() > 0)
                // Фильтруем по артикулу (включая аналоги) - РАССЛАБЛЕННАЯ ФИЛЬТРАЦИЯ
                .filter(goods -> isArticleMatchRelaxed(goods.getArt(), requestedArticle))
                // Сортируем по цене (по возрастанию)
                .sorted(Comparator.comparing(ForumAutoGoods::getPrice))
                // Убираем лимит - берем все подходящие товары
                .collect(Collectors.toList());

        log.info("ForumAuto found {} goods after relaxed filtering (all brands)", filteredGoods.size());
        if (!filteredGoods.isEmpty()) {
            log.info("ForumAuto filtered goods (all brands, first 10):");
            filteredGoods.stream()
                    .limit(10)
                    .forEach(goods -> {
                        log.info(" - Article: '{}', Brand: '{}', Price: {}, Quantity: {}, Returnable: {}, Warehouse: {}",
                                goods.getArt(), goods.getBrand(), goods.getPrice(),
                                goods.getQuantity(), goods.getIsReturnable(), goods.getWarehouse());
                    });
            if (filteredGoods.size() > 10) {
                log.info("... and {} more goods", filteredGoods.size() - 10);
            }
        }

        return filteredGoods;
    }

    private boolean isArticleMatchRelaxed(String goodsArticle, String requestedArticle) {
        if (goodsArticle == null || requestedArticle == null) {
            return false;
        }

        // Нормализуем оба артикула
        String normalizedGoodsArticle = normalizeString(goodsArticle);
        String normalizedRequestedArticle = normalizeString(requestedArticle);

        // 1. Прямое сравнение после нормализации
        if (normalizedGoodsArticle.equals(normalizedRequestedArticle)) {
            return true;
        }

        // 2. Убираем ВСЕ не-цифровые символы и сравниваем
        String digitsOnlyGoods = normalizedGoodsArticle.replaceAll("[^0-9]", "");
        String digitsOnlyRequested = normalizedRequestedArticle.replaceAll("[^0-9]", "");

        if (!digitsOnlyRequested.isEmpty() && digitsOnlyGoods.contains(digitsOnlyRequested)) {
            log.debug("ForumAuto: article matched by digits contains: '{}' contains '{}'",
                    digitsOnlyGoods, digitsOnlyRequested);
            return true;
        }

        // 3. Проверяем частичное совпадение (если один артикул содержит другой)
        if (normalizedGoodsArticle.contains(normalizedRequestedArticle) ||
                normalizedRequestedArticle.contains(normalizedGoodsArticle)) {
            log.debug("ForumAuto: article partial match: '{}' vs '{}'",
                    normalizedGoodsArticle, normalizedRequestedArticle);
            return true;
        }

        // 4. Проверяем совпадение по основным цифрам (для случаев типа A899 vs 899)
        if (digitsOnlyGoods.equals(digitsOnlyRequested) && !digitsOnlyGoods.isEmpty()) {
            log.debug("ForumAuto: article matched by digits only: '{}' -> '{}'",
                    normalizedGoodsArticle, digitsOnlyGoods);
            return true;
        }

        // 5. Проверяем обратное совпадение (requested содержит goods)
        if (normalizedRequestedArticle.contains(normalizedGoodsArticle)) {
            log.debug("ForumAuto: article reverse match: '{}' contained in '{}'",
                    normalizedGoodsArticle, normalizedRequestedArticle);
            return true;
        }

        return false;
    }


    @Override
    public boolean isAvailable() {
        return true;
    }
}