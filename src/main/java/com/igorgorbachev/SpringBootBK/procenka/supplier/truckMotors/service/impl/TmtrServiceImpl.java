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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TmtrServiceImpl implements TmtrService, SupplierService {
    private final WebClient webClient;
    private final TmtrConfig tmtrConfig;
    private final ObjectMapper objectMapper;
    private String lastRawResponse;

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

            String responseBody = webClient.post()
                    .uri("/API.asmx/PreProboy")
                    .header("login", tmtrConfig.getLogin())
                    .header("password", tmtrConfig.getPassword())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(tmtrConfig.getTimeoutSeconds()))
                    .block();

            return parsePreProboyResponse(responseBody, article);

        } catch (WebClientResponseException e) {
            log.error("TMTR PreProboy API Error - Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
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

            log.debug("TMTR Proboy API Request - Article: {}, Brand: {}", article, brand);

            String responseBody = webClient.post()
                    .uri("/API.asmx/Proboy")
                    .header("login", tmtrConfig.getLogin())
                    .header("password", tmtrConfig.getPassword())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(tmtrConfig.getTimeoutSeconds()))
                    .block();

            // Сохраняем сырой ответ
            this.lastRawResponse = responseBody;
            log.info("TMTR raw response saved, length: {}", responseBody != null ? responseBody.length() : 0);

            return parseProboyResponse(responseBody, article, brand);

        } catch (WebClientResponseException e) {
            log.error("TMTR Proboy API Error - Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            // Сохраняем ответ даже при ошибке
            this.lastRawResponse = e.getResponseBodyAsString();
            throw new TmtrException("HTTP error " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("TMTR Proboy service error: {}", e.getMessage());
            this.lastRawResponse = null;
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
            log.debug("TMTR searchParts - Article: {}, Brand: {}", article, brand);

            // Если бренд не указан, возвращаем пустой список
            if (brand == null || brand.trim().isEmpty()) {
                log.debug("TMTR: brand is empty, returning empty list");
                return Collections.emptyList();
            }

            // Используем старый метод с фильтрацией для основного поиска
            List<TmtrGoods> goods = this.searchTmtrParts(article, brand);
            log.info("TMTR found {} FILTERED items for article: {}, brand: {}",
                    goods.size(), article, brand);

            List<PartOfferDto> offers = convertToPartOffers(goods);
            return offers;
        } catch (Exception e) {
            log.error("Error searching in TMTR for article: {}, brand: {}. Error: {}",
                    article, brand, e.getMessage());
            return Collections.emptyList();
        }
    }

    public String getLastRawResponse() {
        return this.lastRawResponse;
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
                TmtrBrand[] brandArray = objectMapper.treeToValue(root, TmtrBrand[].class);

                String normalizedRequestedArticle = normalizeString(requestedArticle);

                List<String> brands = Arrays.stream(brandArray)
                        .filter(brand -> brand != null && brand.getBrand() != null && !brand.getBrand().trim().isEmpty())
                        .filter(brand -> {
                            if (brand.getArticle() == null) return false;
                            String normalizedBrandArticle = normalizeString(brand.getArticle());
                            return normalizedBrandArticle.equals(normalizedRequestedArticle);
                        })
                        .map(TmtrBrand::getBrand)
                        .map(String::trim)
                        .distinct()
                        .collect(Collectors.toList());

                log.info("TMTR PreProboy found {} unique brands for article {}",
                        brands.size(), requestedArticle);

                return brands;
            }

            log.info("TMTR PreProboy returned no brands");
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to parse TMTR PreProboy response: {}", e.getMessage());
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

                log.debug("TMTR Proboy search found {} total results", allGoods.size());

                return filterOriginalGoods(allGoods, requestedArticle, requestedBrand);
            }

            log.info("TMTR Proboy search returned no results");
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to parse TMTR Proboy response: {}", e.getMessage());
            throw new TmtrException("Failed to parse Proboy response: " + e.getMessage());
        }
    }

    /**
     * Фильтрует только оригинальные детали по артикулу и бренду
     */
    private List<TmtrGoods> filterOriginalGoods(List<TmtrGoods> allGoods, String requestedArticle, String requestedBrand) {
        if (allGoods == null || allGoods.isEmpty()) {
            return Collections.emptyList();
        }

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

        log.debug("TMTR filtered: {} -> {} items", allGoods.size(), filtered.size());

        return filtered;
    }

    private boolean isBrandMatch(String goodsBrand, String requestedBrand) {
        if (goodsBrand == null || requestedBrand == null || requestedBrand.isEmpty()) {
            return false;
        }

        String normalizedGoodsBrand = normalizeString(goodsBrand);
        return normalizedGoodsBrand.equals(requestedBrand);
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

    private List<PartOfferDto> convertAllToPartOffers(List<TmtrGoods> tmtrGoods) {
        if (tmtrGoods == null || tmtrGoods.isEmpty()) {
            return List.of();
        }

        List<PartOfferDto> offers = tmtrGoods.stream()
                .filter(Objects::nonNull)
                .map(goods -> {
                    try {
                        // Используем существующий конструктор PartOfferDto(TmtrGoods goods)
                        PartOfferDto offer = new PartOfferDto(goods);

                        // Устанавливаем deliveryDays = 1 для всех отфильтрованных товаров
                        // так как они прошли проверку isFastDelivery()
                        offer.setDeliveryDays(1);

                        // Добавляем информацию о складе для отображения
                        if (goods.getOs() != null) {
                            offer.setWarehouse("OS=" + goods.getOs() + " (" + goods.getWarehouseName() + ")");
                        } else {
                            offer.setWarehouse(goods.getWarehouse());
                        }

                        return offer;
                    } catch (Exception e) {
                        log.error("Error converting TMTR goods to offer: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Converted {} TMTR goods to {} offers", tmtrGoods.size(), offers.size());
        return offers;
    }

    private List<PartOfferDto> convertToPartOffers(List<TmtrGoods> tmtrGoods) {
        if (tmtrGoods == null || tmtrGoods.isEmpty()) {
            return List.of();
        }

        List<PartOfferDto> offers = tmtrGoods.stream()
                .filter(Objects::nonNull)
                .map(goods -> {
                    try {
                        return new PartOfferDto(goods);
                    } catch (Exception e) {
                        log.error("Error converting TMTR goods to offer: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

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

    public List<TmtrGoods> getAllTmtrGoods(String article, String brand) {
        try {
            String requestBody = buildProboyRequestBody(article, brand);
            log.debug("TMTR Proboy API Request - Article: {}, Brand: {}", article, brand);

            String responseBody = webClient.post()
                    .uri("/API.asmx/Proboy")
                    .header("login", tmtrConfig.getLogin())
                    .header("password", tmtrConfig.getPassword())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(tmtrConfig.getTimeoutSeconds()))
                    .block();

            log.info("=== RAW TMTR RESPONSE ===");
            log.info("Length: {} chars", responseBody.length());
            log.info("First 500 chars: {}", responseBody.substring(0, Math.min(500, responseBody.length())));

            // Сохраняем сырой ответ
            this.lastRawResponse = responseBody;
            log.info("TMTR raw response saved, length: {}", responseBody != null ? responseBody.length() : 0);

            // Парсим ВСЕ товары без фильтрации
            return parseAllProboyResponse(responseBody, article, brand);

        } catch (WebClientResponseException e) {
            log.error("TMTR Proboy API Error - Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            this.lastRawResponse = e.getResponseBodyAsString();
            throw new TmtrException("HTTP error " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("TMTR Proboy service error: {}", e.getMessage());
            this.lastRawResponse = null;
            throw new TmtrException("Service error: " + e.getMessage());
        }
    }

    @Override
    public List<PartOfferDto> getAllTmtrParts(String article, String brand) {
        try {
            log.debug("TMTR getAllTmtrParts - Article: {}, Brand: {}", article, brand);

            if (brand == null || brand.trim().isEmpty()) {
                return Collections.emptyList();
            }

            // Получаем ВСЕ товары без фильтрации
            List<TmtrGoods> allGoods = this.getAllTmtrGoods(article, brand);
            log.info("TMTR found {} ALL items for article: {}, brand: {}",
                    allGoods.size(), article, brand);

            // ФИЛЬТРАЦИЯ: только товары в наличии с сортировкой по цене и ТОЛЬКО OS=1
            List<TmtrGoods> filteredGoods = allGoods.stream()
                    .filter(goods -> goods != null)
                    .filter(goods -> goods.getParsedQuantity() != null && goods.getParsedQuantity() > 0) // только в наличии
                    .filter(goods -> goods.getPrice() != null && goods.getPrice() > 0) // только с валидной ценой
                    .filter(goods -> goods.getOs() != null && goods.getOs() == 1) // ТОЛЬКО OS=1 (основной склад)
                    .sorted(Comparator.comparing(TmtrGoods::getPrice)) // сортировка по возрастанию цены
                    .collect(Collectors.toList());

            log.info("TMTR after filtering (in stock, OS=1): {} -> {} items",
                    allGoods.size(), filteredGoods.size());

            // Конвертируем отфильтрованные товары в PartOfferDto
            List<PartOfferDto> filteredOffers = convertAllToPartOffers(filteredGoods);
            return filteredOffers;
        } catch (Exception e) {
            log.error("Error getting all TMTR parts for article: {}, brand: {}. Error: {}",
                    article, brand, e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<PartOfferDto> getAllTmtrPartsRaw(String article, String brand) {
        try {
            log.debug("TMTR getAllTmtrPartsRaw - Article: {}, Brand: {}", article, brand);

            if (brand == null || brand.trim().isEmpty()) {
                return Collections.emptyList();
            }

            // Получаем ВСЕ товары без фильтрации
            List<TmtrGoods> allGoods = this.getAllTmtrGoods(article, brand);
            log.info("TMTR found {} RAW items for article: {}, brand: {}",
                    allGoods.size(), article, brand);

            // Конвертируем все товары в PartOfferDto без фильтрации
            List<PartOfferDto> allOffers = convertAllToPartOffers(allGoods);
            return allOffers;
        } catch (Exception e) {
            log.error("Error getting raw TMTR parts for article: {}, brand: {}. Error: {}",
                    article, brand, e.getMessage());
            return Collections.emptyList();
        }
    }


    private List<TmtrGoods> parseAllProboyResponse(String responseBody, String requestedArticle, String requestedBrand) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            log.warn("Empty response from TMTR Proboy API");
            return Collections.emptyList();
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            if (root.isArray()) {
                TmtrGoods[] goodsArray = objectMapper.treeToValue(root, TmtrGoods[].class);
                List<TmtrGoods> allGoods = goodsArray != null ? Arrays.asList(goodsArray) : Collections.emptyList();

                log.info("TMTR Proboy search found {} total results (ALL ITEMS)", allGoods.size());

                // Логируем первые несколько товаров для отладки
                if (!allGoods.isEmpty()) {
                    log.info("First 5 TMTR items (ALL):");
                    for (int i = 0; i < Math.min(5, allGoods.size()); i++) {
                        TmtrGoods goods = allGoods.get(i);
                        log.info("  {}: Brand='{}', Article='{}', Price={}, Quantity={}, OS={}, Warehouse='{}'",
                                i+1, goods.getBrand(), goods.getNumber(), goods.getPrice(),
                                goods.getParsedQuantity(), goods.getOs(), goods.getWarehouse());
                    }
                }

                return allGoods;
            }

            log.info("TMTR Proboy search returned no results");
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to parse TMTR Proboy response: {}", e.getMessage());
            throw new TmtrException("Failed to parse Proboy response: " + e.getMessage());
        }
    }

    private boolean isFastDelivery(TmtrGoods goods) {
        if (goods == null) {
            return false;
        }

        // Все товары OS=1 считаем с быстрой доставкой
        if (goods.getOs() != null && goods.getOs() == 1) {
            return true;
        }

        // Остальная логика для других случаев (если понадобится)
        return false;
    }
}