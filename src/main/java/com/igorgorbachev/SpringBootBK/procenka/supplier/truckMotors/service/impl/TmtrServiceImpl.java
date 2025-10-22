package com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igorgorbachev.SpringBootBK.exception.TmtrException;
import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.service.SupplierService;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.config.TmtrConfig;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.model.TmtrBrand;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.model.TmtrGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.service.TmtrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;


import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TmtrServiceImpl implements TmtrService, SupplierService {
    private final WebClient webClient;
    private final TmtrConfig tmtrConfig;
    private final ObjectMapper objectMapper;

    public TmtrServiceImpl(WebClient.Builder webClientBuilder,
                           TmtrConfig tmtrConfig,
                           ObjectMapper objectMapper) {
        this.tmtrConfig = tmtrConfig;
        this.objectMapper = objectMapper;

        this.webClient = webClientBuilder
                .baseUrl(tmtrConfig.getBaseUrl())
                .defaultHeader("Content-Type", "text/plain")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();

        log.info("TMTR service initialized for login: {}", tmtrConfig.getLogin());
    }

    /**
     * Этап 1: Получение списка брендов по артикулу (PreProboy)
     */
    @Override
    public List<String> getBrands(String article) {
        try {
            String requestBody = buildPreProboyRequestBody(article);

//            log.info("=== TMTR PreProboy API Request ===");
//            log.info("URL: {}/API.asmx/PreProboy", tmtrConfig.getBaseUrl());
//            log.info("Headers: login={}, password={}", tmtrConfig.getLogin(), "***");
//            log.info("Body: {}", requestBody);

            String responseBody = webClient.post()
                    .uri("/API.asmx/PreProboy")
                    .header("login", tmtrConfig.getLogin())
                    .header("password", tmtrConfig.getPassword())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(tmtrConfig.getTimeoutSeconds()))
                    .block();

            log.info("=== TMTR PreProboy API Response ===");
//            log.info("Response: {}", responseBody);

            return parsePreProboyResponse(responseBody, article); // Передаем article для фильтрации

        } catch (WebClientResponseException e) {
            log.error("=== TMTR PreProboy API Error ===");
            log.error("Status: {}", e.getStatusCode());
            log.error("Response: {}", e.getResponseBodyAsString());
            throw new TmtrException("HTTP error " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("TMTR PreProboy service error: {}", e.getMessage());
            throw new TmtrException("Service error: " + e.getMessage());
        }
    }

    /**
     * Этап 2: Получение детальной информации по артикулу и бренду (Proboy)
     */
    @Override
    public List<TmtrGoods> searchTmtrParts(String article, String brand) {
        try {
            String requestBody = buildProboyRequestBody(article, brand);

            log.info("=== TMTR Proboy API Request ===");
            log.info("URL: {}/API.asmx/Proboy", tmtrConfig.getBaseUrl());
            log.info("Headers: login={}, password={}", tmtrConfig.getLogin(), "***");
            log.info("Body: {}", requestBody);

            String responseBody = webClient.post()
                    .uri("/API.asmx/Proboy")
                    .header("login", tmtrConfig.getLogin())
                    .header("password", tmtrConfig.getPassword())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(tmtrConfig.getTimeoutSeconds()))
                    .block();

            log.info("=== TMTR Proboy API Response ===");
//            log.info("Response: {}", responseBody);

            return parseProboyResponse(responseBody, article, brand);

        } catch (WebClientResponseException e) {
            log.error("=== TMTR Proboy API Error ===");
            log.error("Status: {}", e.getStatusCode());
            log.error("Response: {}", e.getResponseBodyAsString());
            throw new TmtrException("HTTP error " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("TMTR Proboy service error: {}", e.getMessage());
            throw new TmtrException("Service error: " + e.getMessage());
        }
    }

    @Override
    public Mono<List<TmtrGoods>> searchPartsReactive(String article, String brand) {
        String requestBody = buildProboyRequestBody(article, brand);

        return webClient.post()
                .uri("/API.asmx/Proboy")
                .header("login", tmtrConfig.getLogin())
                .header("password", tmtrConfig.getPassword())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(tmtrConfig.getTimeoutSeconds()))
                .flatMap(responseBody -> {
                    try {
                        List<TmtrGoods> goods = parseProboyResponse(responseBody, article, brand);
                        return Mono.just(goods);
                    } catch (Exception e) {
                        return Mono.error(new TmtrException("JSON parsing error: " + e.getMessage()));
                    }
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    String errorMessage = String.format("HTTP error %d: %s",
                            e.getStatusCode().value(),
                            e.getResponseBodyAsString()
                    );
                    return Mono.error(new TmtrException(errorMessage));
                });
    }

    @Override
    public List<PartOfferDto> searchParts(String article, String brand) {
        try {
            log.info("TMTR searchParts called: article={}, brand={}", article, brand);

            // Если бренд не указан, возвращаем пустой список
            if (brand == null || brand.trim().isEmpty()) {
                log.info("TMTR: brand is empty, returning empty list");
                return Collections.emptyList();
            }

            log.info("TMTR: searching for brand '{}'", brand);
            List<TmtrGoods> goods = this.searchTmtrParts(article, brand);
            log.info("TMTR searchTmtrParts returned {} items for brand '{}'", goods.size(), brand);

            List<PartOfferDto> offers = convertToPartOffers(goods);
            log.info("TMTR convertToPartOffers returned {} offers", offers.size());

            return offers;
        } catch (Exception e) {
            log.error("Error searching in TMTR for article: {}, brand: {}. Error: {}",
                    article, brand, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Формирование тела запроса для PreProboy (получение брендов)
     */
    private String buildPreProboyRequestBody(String article) {
        try {
            PreProboyRequestBody requestBody = new PreProboyRequestBody(article);
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new TmtrException("Failed to build PreProboy request body: " + e.getMessage());
        }
    }

    /**
     * Формирование тела запроса для Proboy (получение деталей)
     */
    private String buildProboyRequestBody(String article, String brand) {
        try {
            ProboyRequestBody requestBody = new ProboyRequestBody(article, brand);
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new TmtrException("Failed to build Proboy request body: " + e.getMessage());
        }
    }

    /**
     * Парсинг ответа от PreProboy (список брендов)
     */
    private List<String> parsePreProboyResponse(String responseBody, String requestedArticle) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            log.warn("Empty response from TMTR PreProboy API");
            return Collections.emptyList();
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            if (root.isArray()) {
                // Парсим как массив объектов TmtrBrand
                TmtrBrand[] brandArray = objectMapper.treeToValue(root, TmtrBrand[].class);

                // Нормализуем запрашиваемый артикул для сравнения
                String normalizedRequestedArticle = normalizeString(requestedArticle);

                List<String> brands = Arrays.stream(brandArray)
                        .filter(brand -> brand != null && brand.getBrand() != null && !brand.getBrand().trim().isEmpty())
                        // Фильтруем по точному совпадению артикула
                        .filter(brand -> {
                            if (brand.getArticle() == null) return false;
                            String normalizedBrandArticle = normalizeString(brand.getArticle());
                            return normalizedBrandArticle.equals(normalizedRequestedArticle);
                        })
                        .map(TmtrBrand::getBrand)
                        .map(String::trim)
                        .distinct()
                        .collect(Collectors.toList());

                log.info("TMTR PreProboy found {} unique brands for article {} from {} total items",
                        brands.size(), requestedArticle, brandArray.length);

                // Логируем найденные бренды для отладки
                if (!brands.isEmpty()) {
                    log.info("Found TMTR brands for article {}: {}", requestedArticle, String.join(", ", brands));
                }

                return brands;
            }

            log.info("TMTR PreProboy returned no brands (not an array)");
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to parse TMTR PreProboy response: {}", e.getMessage());
            log.debug("Response body: {}", responseBody);
            throw new TmtrException("Failed to parse PreProboy response: " + e.getMessage());
        }
    }

    /**
     * Парсинг ответа от Proboy (детальная информация)
     */
    private List<TmtrGoods> parseProboyResponse(String responseBody, String requestedArticle, String requestedBrand) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            log.warn("Empty response from TMTR Proboy API");
            return Collections.emptyList();
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            if (root.isArray()) {
                TmtrGoods[] goodsArray = objectMapper.treeToValue(root, TmtrGoods[].class);
                List<TmtrGoods> allGoods = goodsArray != null ? Arrays.asList(goodsArray) : Collections.emptyList();

                log.info("TMTR Proboy search successful, found {} total results", allGoods.size());

                // Применяем фильтрацию по бренду
                return filterOriginalGoods(allGoods, requestedArticle, requestedBrand);
            }

            log.info("TMTR Proboy search returned no results (not an array)");
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to parse TMTR Proboy response: {}", e.getMessage());
            log.debug("Response body: {}", responseBody);
            throw new TmtrException("Failed to parse Proboy response: " + e.getMessage());
        }
    }

    /**
     * Фильтрует только оригинальные детали (не аналоги) по артикулу и бренду
     */
    private List<TmtrGoods> filterOriginalGoods(List<TmtrGoods> allGoods, String requestedArticle, String requestedBrand) {
        if (allGoods == null || allGoods.isEmpty()) {
            return Collections.emptyList();
        }

//        // Логируем все товары до фильтрации
//        log.info("TMTR ALL goods before filtering ({} items):", allGoods.size());
//        allGoods.forEach(goods ->
//                log.info(" - Brand: '{}', Article: '{}', Price: {}, Qty: {}, Returnable: {}, OS: {}",
//                        goods.getBrand(), goods.getNumber(), goods.getPrice(),
//                        goods.getParsedQuantity(), goods.isReturnable(), goods.getOs()));

        // Нормализуем запрашиваемый бренд для сравнения
        String normalizedRequestedBrand = requestedBrand != null ? normalizeString(requestedBrand) : "";

        List<TmtrGoods> filtered = allGoods.stream()
                .filter(goods -> goods != null)
                // ФИЛЬТР 1: Только товары с указанным брендом
                .filter(goods -> isBrandMatch(goods.getBrand(), normalizedRequestedBrand))
                // ФИЛЬТР 2: Только товары в наличии (количество > 0)
                .filter(goods -> goods.getParsedQuantity() > 0)
                // ФИЛЬТР 3: Только товары с валидной ценой
                .filter(goods -> goods.getPrice() != null && goods.getPrice() > 0)
                // ФИЛЬТР 4: Только товары со склада ТракМоторс (OS = 1)
                .filter(goods -> goods.getOs() != null && goods.getOs() == 1)
                // Сортируем по цене (по возрастанию)
                .sorted((g1, g2) -> Double.compare(g1.getPrice(), g2.getPrice()))
                .collect(Collectors.toList());

        log.info("TMTR filterOriginalGoods: {} -> {} items after all filters", allGoods.size(), filtered.size());

        // Логируем отфильтрованные товары
        if (!filtered.isEmpty()) {
            log.info("TMTR FILTERED goods (brand '{}', OS=1):", requestedBrand);
            filtered.forEach(goods ->
                    log.info(" - Brand: '{}', Article: '{}', Price: {}, Qty: {}, OS: {}",
                            goods.getBrand(), goods.getNumber(), goods.getPrice(),
                            goods.getParsedQuantity(), goods.getOs()));
        } else {
            log.info("TMTR: No goods found after filtering for brand '{}' and OS=1", requestedBrand);

            // Для диагностики: посмотрим какие значения OS есть в исходных данных
            Map<Integer, Long> osDistribution = allGoods.stream()
                    .filter(goods -> goods != null && goods.getOs() != null)
                    .collect(Collectors.groupingBy(TmtrGoods::getOs, Collectors.counting()));

            log.info("TMTR OS distribution in original data: {}", osDistribution);
        }

        return filtered;
    }

    private boolean isBrandMatch(String goodsBrand, String requestedBrand) {
        if (goodsBrand == null || requestedBrand == null || requestedBrand.isEmpty()) {
            return false;
        }

        String normalizedGoodsBrand = normalizeString(goodsBrand);

        boolean matches = normalizedGoodsBrand.equals(requestedBrand);

        log.debug("TMTR brand match: '{}' == '{}' -> {}",
                normalizedGoodsBrand, requestedBrand, matches);

        return matches;
    }


    /**
     * Проверяет, является ли товар оригинальным (не аналогом)
     */
    private boolean isOriginalGoods(TmtrGoods goods, String requestedArticle, String requestedBrand) {
        // ВРЕМЕННО: пропускаем все товары без проверки
        log.info("TMTR TEMPORARY: accepting ALL goods without filtering");
        return true;
    }

    /**
     * Нормализует строку для сравнения: убирает пробелы, приводит к нижнему регистру
     */
    private String normalizeString(String str) {
        if (str == null) {
            return "";
        }
        return str.replaceAll("\\s+", "").toLowerCase();
    }

    @Override
    public String getSupplierName() {
        return "TMTR";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private List<PartOfferDto> convertToPartOffers(List<TmtrGoods> tmtrGoods) {
        if (tmtrGoods == null || tmtrGoods.isEmpty()) {
            log.info("TMTR convertToPartOffers: empty input");
            return List.of();
        }

        List<PartOfferDto> offers = tmtrGoods.stream()
                .filter(Objects::nonNull)
                .map(goods -> {
                    try {
                        PartOfferDto offer = new PartOfferDto(goods);
                        log.debug("TMTR converted: {} - {} - {}", offer.getBrand(), offer.getOriginalArticle(), offer.getPrice());
                        return offer;
                    } catch (Exception e) {
                        log.error("Error converting TMTR goods to offer: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("TMTR convertToPartOffers: {} -> {} offers", tmtrGoods.size(), offers.size());
        return offers;
    }

    // Вспомогательные классы для формирования тел запросов

    private static class PreProboyRequestBody {
        private String article;

        public PreProboyRequestBody(String article) {
            this.article = article;
        }

        public String getArticle() { return article; }
    }

    private static class ProboyRequestBody {
        private String article;
        private String brand;

        public ProboyRequestBody(String article, String brand) {
            this.article = article;
            this.brand = brand;
        }

        public String getArticle() { return article; }
        public String getBrand() { return brand; }
    }
}