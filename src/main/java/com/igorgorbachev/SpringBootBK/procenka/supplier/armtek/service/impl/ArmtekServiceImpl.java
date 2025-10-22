package com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igorgorbachev.SpringBootBK.exception.ArmtekException;
import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.service.SupplierService;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.config.ArmtekConfig;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.model.ArmtekGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.service.ArmtekService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;


import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArmtekServiceImpl implements ArmtekService, SupplierService {
    private final WebClient webClient;
    private final ArmtekConfig armtekConfig;
    private final ObjectMapper objectMapper;

    public ArmtekServiceImpl(WebClient.Builder webClientBuilder,
                             ArmtekConfig armtekConfig,
                             ObjectMapper objectMapper) {
        this.armtekConfig = armtekConfig;
        this.objectMapper = objectMapper;

        // Создаем Basic Auth header
        String credentials = armtekConfig.getUsername() + ":" + armtekConfig.getPassword();
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        this.webClient = webClientBuilder
                .baseUrl(armtekConfig.getBaseUrl())
                .defaultHeader("Authorization", "Basic " + encodedCredentials)
                .defaultHeader("Content-Type", "application/x-www-form-urlencoded")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();

        log.info("Armtek service initialized with Basic Auth for user: {}", armtekConfig.getUsername());
    }

    @Override
    public List<ArmtekGoods> searchParts(String article, String brand, String queryType, String program) {
        try {
            MultiValueMap<String, String> formData = buildFormData(article, brand, queryType, program);

            log.info("=== Armtek API Request ===");
            log.info("URL: {}/api/ws_search/search?format=json", armtekConfig.getBaseUrl());
            log.info("Parameters (form-data):");
            formData.forEach((key, value) -> {
                log.info("  {}: {}", key, value);
            });

            String responseBody = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/ws_search/search")
                            .queryParam("format", "json")
                            .build())
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(armtekConfig.getTimeoutSeconds()))
                    .block();

            log.info("=== Armtek API Response ===");
//            log.info("Response: {}", responseBody);

            List<ArmtekGoods> allGoods = parseResponse(responseBody);

            // Фильтруем только оригинальные детали (не аналоги)
            return filterOriginalGoods(allGoods, article, brand);

        } catch (WebClientResponseException e) {
            log.error("=== Armtek API Error ===");
            log.error("Status: {}", e.getStatusCode());
            log.error("Response: {}", e.getResponseBodyAsString());
            throw new ArmtekException("HTTP error " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Armtek service error: {}", e.getMessage());
            throw new ArmtekException("Service error: " + e.getMessage());
        }
    }

    @Override
    public Mono<List<ArmtekGoods>> searchPartsReactive(String article, String brand, String queryType, String program) {
        MultiValueMap<String, String> formData = buildFormData(article, brand, queryType, program);

        return webClient.post()
                .uri("/api/ws_search/search?format=json")
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(armtekConfig.getTimeoutSeconds()))
                .flatMap(responseBody -> {
                    try {
                        List<ArmtekGoods> allGoods = parseResponse(responseBody);
                        List<ArmtekGoods> filteredGoods = filterOriginalGoods(allGoods, article, brand);
                        return Mono.just(filteredGoods);
                    } catch (Exception e) {
                        return Mono.error(new ArmtekException("JSON parsing error: " + e.getMessage()));
                    }
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    String errorMessage = String.format("HTTP error %d: %s",
                            e.getStatusCode().value(),
                            e.getResponseBodyAsString()
                    );
                    return Mono.error(new ArmtekException(errorMessage));
                });
    }

    /**
     * Фильтрует только оригинальные детали (не аналоги) по артикулу и бренду
     * Группирует по складам и возвращает до 2 складов с наименьшей ценой
     */


    private List<ArmtekGoods> filterOriginalGoods(List<ArmtekGoods> allGoods, String requestedArticle, String requestedBrand) {
        if (allGoods == null || allGoods.isEmpty()) {
            return Collections.emptyList();
        }

        // Нормализуем запрашиваемые значения для сравнения
        String normalizedRequestedArticle = normalizeString(requestedArticle);
        String normalizedRequestedBrand = requestedBrand != null ? normalizeString(requestedBrand) : null;

        log.info("Armtek filtering: requested article='{}' (normalized: '{}'), brand='{}'",
                requestedArticle, normalizedRequestedArticle, requestedBrand);

        // Сначала фильтруем оригинальные товары
        List<ArmtekGoods> originalGoods = allGoods.stream()
                .filter(goods -> goods != null)
                .filter(goods -> isOriginalGoods(goods, normalizedRequestedArticle, normalizedRequestedBrand))
                .filter(goods -> goods.getParsedQuantity() > 0)
                .filter(goods -> goods.getPrice() != null && goods.getPrice() > 0)
                .collect(Collectors.toList());

        log.info("Armtek found {} original goods before grouping", originalGoods.size());

        // Группируем по артикулу+бренду и выбираем товар с минимальной ценой
        Map<String, List<ArmtekGoods>> groupedByArticleBrand = originalGoods.stream()
                .collect(Collectors.groupingBy(goods ->
                        (goods.getPin() != null ? goods.getPin() : "") + "|" +
                                (goods.getBrand() != null ? goods.getBrand() : "")
                ));

        List<ArmtekGoods> bestOffers = new ArrayList<>();

        for (List<ArmtekGoods> goodsList : groupedByArticleBrand.values()) {
            if (!goodsList.isEmpty()) {
                // Находим товар с минимальной ценой
                ArmtekGoods bestGoods = goodsList.stream()
                        .min(Comparator.comparing(ArmtekGoods::getPrice))
                        .orElse(goodsList.get(0));

                // Сохраняем все товары этой группы в поле warehouses (для отображения в выпадающем списке)
                bestGoods.setWarehouseGoods(goodsList); // Нужно добавить это поле в модель
                bestOffers.add(bestGoods);
            }
        }

        // Сортируем по цене
        bestOffers.sort(Comparator.comparing(ArmtekGoods::getPrice));

        log.info("Armtek found {} best offers after grouping", bestOffers.size());
        if (!bestOffers.isEmpty()) {
            log.info("Armtek best offers:");
            for (ArmtekGoods goods : bestOffers) {
                log.info(" - Article: '{}', Brand: '{}', Price: {}, Quantity: {}, Warehouses: {}",
                        goods.getPin(), goods.getBrand(), goods.getPrice(),
                        goods.getParsedQuantity(),
                        goods.getWarehouseGoods() != null ? goods.getWarehouseGoods().size() : 0);
            }
        }

        return bestOffers;
    }
    /**
     * Проверяет, является ли товар оригинальным (не аналогом)
     * Сравнивает артикул и бренд с запрошенными значениями
     */
    private boolean isOriginalGoods(ArmtekGoods goods, String requestedArticle, String requestedBrand) {
        if (goods.getPin() == null) {
            return false;
        }

        // Нормализуем артикул и бренд из товара
        String normalizedGoodsArticle = normalizeString(goods.getPin());
        String normalizedGoodsBrand = goods.getBrand() != null ? normalizeString(goods.getBrand()) : null;

        // ГИБКОЕ сравнение артикулов - учитываем разные форматы
        boolean articleMatches = isArticleMatch(normalizedGoodsArticle, requestedArticle);

        // Если бренд был указан в запросе, сравниваем и бренды
        boolean brandMatches = requestedBrand == null ||
                (normalizedGoodsBrand != null && normalizedGoodsBrand.equals(requestedBrand));

        log.debug("Armtek article match: '{}' vs '{}' -> {}",
                normalizedGoodsArticle, requestedArticle, articleMatches);
        log.debug("Armtek brand match: '{}' vs '{}' -> {}",
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
            log.info("Armtek: article matched by digits only: '{}' -> '{}'",
                    goodsArticle, digitsOnlyGoods);
            return true;
        }

        // 3. Проверяем частичное совпадение (если один артикул содержит другой)
        if (goodsArticle.contains(requestedArticle) || requestedArticle.contains(goodsArticle)) {
            log.info("Armtek: article partial match: '{}' contains '{}'",
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

    private MultiValueMap<String, String> buildFormData(String article, String brand, String queryType, String program) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        // Обязательные параметры согласно документации
        formData.add("VKORG", armtekConfig.getVkorg());
        formData.add("KUNNR_RG", armtekConfig.getKunnrRg());

        // PIN - артикул для поиска (обязательный)
        if (article != null && !article.trim().isEmpty()) {
            formData.add("PIN", article.trim());
        } else {
            throw new ArmtekException("Article (PIN) is required for search");
        }

        // BRAND - рекомендуется всегда заполнять
        if (brand != null && !brand.trim().isEmpty()) {
            formData.add("BRAND", brand.trim());
        } else {
            // Если бренд не указан, используем пустую строку
            formData.add("BRAND", "");
        }

        // QUERY_TYPE - устанавливаем 1 (без аналогов) чтобы API не возвращал аналоги
        formData.add("QUERY_TYPE", "1");

        // PROGRAM - опционально
        if (program != null && !program.trim().isEmpty()) {
            formData.add("PROGRAM", program.trim());
        }

        log.debug("Form data prepared: VKORG={}, KUNNR_RG={}, PIN={}, BRAND={}, QUERY_TYPE=1 (no analogs)",
                armtekConfig.getVkorg(), armtekConfig.getKunnrRg(), article, brand);

        return formData;
    }

    private List<ArmtekGoods> parseResponse(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            log.warn("Empty response from Armtek API");
            return Collections.emptyList();
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // Проверяем статус ответа
            JsonNode statusNode = root.get("STATUS");
            if (statusNode != null) {
                int status = statusNode.asInt();

                // STATUS 208 - частичный успех, но с сообщениями
                if (status == 208) {
                    log.warn("Armtek API returned STATUS 208 with messages");

                    // Проверяем MESSAGES
                    JsonNode messagesNode = root.get("MESSAGES");
                    if (messagesNode != null && messagesNode.isArray()) {
                        for (JsonNode message : messagesNode) {
                            String msgType = message.get("TYPE").asText();
                            String msgText = message.get("TEXT").asText();
                            log.warn("Armtek message [{}]: {}", msgType, msgText);
                        }
                    }

                    // Продолжаем обработку, если есть результаты
                } else if (status != 200) {
                    JsonNode errorNode = root.get("RESP");
                    String errorMessage = "Unknown error";
                    if (errorNode != null && errorNode.has("ERROR")) {
                        errorMessage = errorNode.get("ERROR").asText();
                    }

                    // Проверяем MESSAGES
                    JsonNode messagesNode = root.get("MESSAGES");
                    if (messagesNode != null && messagesNode.isArray() && messagesNode.size() > 0) {
                        errorMessage = messagesNode.get(0).get("TEXT").asText();
                    }

                    throw new ArmtekException("API returned error " + status + ": " + errorMessage);
                }
            }

            // Получаем массив результатов - поле RESP согласно документации
            JsonNode respNode = root.get("RESP");
            if (respNode != null && respNode.isArray() && respNode.size() > 0) {
                // Проверяем, есть ли реальные результаты или только сообщение
                JsonNode firstElement = respNode.get(0);
                if (firstElement.has("MSG")) {
                    // Это сообщение "ничего не найдено", а не реальные товары
                    String msg = firstElement.get("MSG").asText();
                    log.info("Armtek search: {}", msg);
                    return Collections.emptyList();
                } else {
                    // Это реальные товары
                    ArmtekGoods[] goodsArray = objectMapper.treeToValue(respNode, ArmtekGoods[].class);
                    List<ArmtekGoods> result = goodsArray != null ? Arrays.asList(goodsArray) : Collections.emptyList();
                    log.info("Armtek search successful, found {} results before filtering", result.size());
                    return result;
                }
            }

            log.info("Armtek search returned no results (empty RESP array)");
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to parse Armtek response: {}", e.getMessage());
            log.debug("Response body: {}", responseBody);
            throw new ArmtekException("Failed to parse response: " + e.getMessage());
        }
    }

    @Override
    public String getSupplierName() {
        return "Armtek";
    }

    @Override
    public List<PartOfferDto> searchParts(String article, String brand) {
        try {
            List<ArmtekGoods> goods = this.searchParts(article, brand, null, null);
            return convertToPartOffers(goods);
        } catch (Exception e) {
            log.error("Error searching in Armtek for article: {}, brand: {}. Error: {}",
                    article, brand, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }


    private List<PartOfferDto> convertToPartOffers(List<ArmtekGoods> armtekGoods) {
        if (armtekGoods == null || armtekGoods.isEmpty()) {
            return List.of();
        }

        return armtekGoods.stream()
                .filter(Objects::nonNull)
                .map(goods -> {
                    try {
                        // ИСПОЛЬЗУЕМ КОНСТРУКТОР, КОТОРЫЙ СОЗДАЕТ СКЛАДЫ
                        return new PartOfferDto(goods);
                    } catch (Exception e) {
                        log.error("Error converting Armtek goods to offer: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
