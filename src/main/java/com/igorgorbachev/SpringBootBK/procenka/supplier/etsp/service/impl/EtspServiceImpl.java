package com.igorgorbachev.SpringBootBK.procenka.supplier.etsp.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.service.SupplierService;
import com.igorgorbachev.SpringBootBK.procenka.supplier.etsp.config.EtspConfig;
import com.igorgorbachev.SpringBootBK.procenka.supplier.etsp.model.EtspGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.etsp.model.dto.EtspWarehouse;
import com.igorgorbachev.SpringBootBK.procenka.supplier.etsp.model.dto.LogonRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.igorgorbachev.SpringBootBK.procenka.supplier.etsp.model.dto.Part;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@Slf4j
@Service
public class EtspServiceImpl implements SupplierService {
    private final WebClient webClient;
    private final EtspConfig etspConfig;
    private final ObjectMapper objectMapper;
    private final AtomicReference<String> currentHashSession = new AtomicReference<>();
    private volatile long lastAuthTime = 0;
    private static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000; // 30 минут

    public EtspServiceImpl(WebClient.Builder webClientBuilder, EtspConfig etspConfig, ObjectMapper objectMapper) {
        this.etspConfig = etspConfig;
        this.objectMapper = objectMapper;
        this.webClient = createWebClient(webClientBuilder);
    }

    private WebClient createWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(etspConfig.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    @Override
    public List<PartOfferDto> searchParts(String article, String brand) {
        try {
            log.info("Поиск в ETSP: артикул={}, бренд={}", article, brand);

            String hashSession = getValidHashSession();
            if (hashSession == null) {
                log.error("Не удалось получить HashSession для ETSP");
                return List.of();
            }

            // Всегда ищем только по артикулу
            List<EtspGoods> goodsList = performSearch(article, null, hashSession);

            // Если указан бренд - фильтруем результаты
            if (brand != null && !brand.trim().isEmpty()) {
                goodsList = goodsList.stream()
                        .filter(goods -> brand.equalsIgnoreCase(goods.getBrand()))
                        .collect(Collectors.toList());
                log.info("После фильтрации по бренду '{}' осталось {} товаров", brand, goodsList.size());
            }

            return goodsList.stream()
                    .map(PartOfferDto::new)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Ошибка при поиске в ETSP: {}", e.getMessage());
            return List.of();
        }
    }

    private String getValidHashSession() {
        String currentSession = currentHashSession.get();
        long currentTime = System.currentTimeMillis();

        // Если сессия есть и не истекла - используем её
        if (currentSession != null && (currentTime - lastAuthTime) < SESSION_TIMEOUT_MS) {
            return currentSession;
        }

        // Иначе получаем новую сессию
        try {
            String newSession = authenticate();
            if (newSession != null) {
                currentHashSession.set(newSession);
                lastAuthTime = currentTime;
                log.info("Успешно получена новая HashSession для ETSP");
                return newSession;
            }
        } catch (Exception e) {
            log.error("Ошибка аутентификации в ETSP: {}", e.getMessage());
        }

        return null;
    }

    private String authenticate() {
        try {
            LogonRequest logonRequest = new LogonRequest();
            logonRequest.setLogin(etspConfig.getUsername());
            logonRequest.setPassword(etspConfig.getPassword());

            log.info("Аутентификация в ETSP для пользователя: '{}'", etspConfig.getUsername());

            String requestJson = objectMapper.writeValueAsString(logonRequest);
            log.info("JSON запрос: {}", requestJson);

            String rawResponse = webClient.post()
                    .uri("/v2/json/Security.svc/Logon")
                    .bodyValue(logonRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            log.info("Сырой ответ от ETSP: {}", rawResponse);

            if (rawResponse != null) {
                // Парсим как Map для надежности
                Map<String, Object> responseMap = objectMapper.readValue(rawResponse, Map.class);
                log.info("Парсинг как Map: Success={}, Data={}, Errors={}",
                        responseMap.get("Success"), responseMap.get("Data"), responseMap.get("Errors"));

                if (Boolean.TRUE.equals(responseMap.get("Success"))) {
                    String hashSession = (String) responseMap.get("Data");
                    log.info("Аутентификация успешна! HashSession: {}", hashSession);
                    return hashSession;
                } else {
                    log.error("Ошибка аутентификации: {}", responseMap.get("Errors"));
                }
            }

        } catch (WebClientResponseException e) {
            log.error("HTTP ошибка {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Общая ошибка аутентификации: {}", e.getMessage());
        }

        return null;
    }



    private List<EtspGoods> performSearch(String article, String brand, String hashSession) {
        try {
            // Используем SearchNomenclature вместо SearchBasic
            List<Part> foundParts = searchNomenclature(article, brand, hashSession);
            if (foundParts.isEmpty()) {
                log.info("ETSP: товары не найдены для артикула {}", article);
                return List.of();
            }

            // Ограничим количество товаров для обработки (первые 5)
            List<Part> partsToProcess = foundParts.stream()
                    .limit(5)
                    .filter(part -> part.getManufacturerNumber() != null && part.getManufacturerName() != null)
                    .collect(Collectors.toList());

            log.info("Обрабатываем {} товаров из {}", partsToProcess.size(), foundParts.size());

            // Для каждого найденного товара получаем детальную информацию
            List<EtspGoods> goodsList = new ArrayList<>();
            for (Part part : partsToProcess) {
                try {
                    EtspGoods goods = getGoodsDetails(part, hashSession);
                    if (goods != null) {
                        goodsList.add(goods);
                    }
                } catch (Exception e) {
                    log.warn("Не удалось получить детали для товара {}: {}", part.getOmegaNumber(), e.getMessage());
                }
            }

            log.info("ETSP: найдено {} товаров с детальной информацией", goodsList.size());
            return goodsList;

        } catch (Exception e) {
            log.error("Ошибка при выполнении поиска в ETSP: {}", e.getMessage());
            return List.of();
        }
    }



    private List<Part> searchNomenclature(String article, String brand, String hashSession) {
        try {
            String searchText = createSearchText(article, brand);

            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("HashSession", hashSession);
            requestMap.put("Text", searchText);
            requestMap.put("WithAnalogsSearch", true);

            log.info("SearchNomenclature Map запрос: {}", requestMap);

            String rawResponse = webClient.post()
                    .uri("/v2/json/Search.svc/SearchNomenclature")
                    .bodyValue(requestMap)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            log.info("SearchNomenclature сырой ответ: {}", rawResponse);

            if (rawResponse != null) {
                // Парсим как Map
                Map<String, Object> responseMap = objectMapper.readValue(rawResponse, Map.class);
                log.info("SearchNomenclature парсинг: Success={}, Data={}, Errors={}",
                        responseMap.get("Success"), responseMap.get("Data"), responseMap.get("Errors"));

                if (Boolean.TRUE.equals(responseMap.get("Success"))) {
                    Map<String, Object> data = (Map<String, Object>) responseMap.get("Data");
                    if (data != null) {
                        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("Items");
                        if (items != null) {
                            List<Part> parts = items.stream()
                                    .map(this::convertMapToPart)
                                    .collect(Collectors.toList());
                            log.info("SearchNomenclature: найдено {} товаров", parts.size());
                            return parts;
                        }
                    }
                } else {
                    List<String> errors = (List<String>) responseMap.get("Errors");
                    log.error("SearchNomenclature ошибки: {}", errors != null ? String.join(", ", errors) : "Unknown error");
                }
            }

            return List.of();

        } catch (Exception e) {
            log.error("Ошибка SearchNomenclature: {}", e.getMessage());
            return List.of();
        }
    }



    private Part convertMapToPart(Map<String, Object> partMap) {
        Part part = new Part();
        part.setOmegaNumber((String) partMap.get("OmegaNumber"));
        part.setUniqueNumber((String) partMap.get("UniqueNumber"));
        part.setName((String) partMap.get("Name"));
        part.setGroup((String) partMap.get("Group")); // Бренд/производитель
        part.setCode((String) partMap.get("Code")); // Код детали для запроса остатков
        part.setClientArticle((String) partMap.get("ClientArticle"));
        part.setSkubaNumber((String) partMap.get("SkubaNumber"));
        part.setManufacturerNumber((String) partMap.get("ManufacturerNumber"));
        part.setManufacturerName((String) partMap.get("ManufacturerName"));
        part.setNote((String) partMap.get("Note"));
        part.setSubgroup((String) partMap.get("Subgroup"));
        return part;
    }



    private EtspGoods getGoodsDetails(Part part, String hashSession) {
        try {
            // Сначала получаем основную информацию через GoodsUnit
            Map<String, Object> goodsUnitData = getGoodsUnitData(part, hashSession);
            if (goodsUnitData == null) {
                return createBasicGoods(part);
            }

            // Затем получаем цены и остатки через другой метод
            EtspGoods goods = convertToEtspGoods(part, goodsUnitData);

            // Получаем цены и остатки
            getPricesAndStocks(goods, part.getCode(), hashSession);

            return goods;

        } catch (Exception e) {
            log.error("Ошибка получения деталей для {}: {}", part.getManufacturerNumber(), e.getMessage());
            return createBasicGoods(part);
        }
    }

    private Map<String, Object> getGoodsUnitData(Part part, String hashSession) {
        try {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("HashSession", hashSession);
            requestMap.put("ManufacturerNumber", part.getManufacturerNumber());
            requestMap.put("ManufacturerName", part.getManufacturerName());

            String rawResponse = webClient.post()
                    .uri("/v2/json/Search.svc/GoodsUnitByManufacturerNumberGet")
                    .bodyValue(requestMap)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            if (rawResponse != null) {
                Map<String, Object> responseMap = objectMapper.readValue(rawResponse, Map.class);
                if (Boolean.TRUE.equals(responseMap.get("Success"))) {
                    List<Map<String, Object>> dataList = (List<Map<String, Object>>) responseMap.get("Data");
                    if (dataList != null && !dataList.isEmpty()) {
                        return dataList.get(0);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Ошибка GoodsUnit для {}: {}", part.getManufacturerNumber(), e.getMessage());
        }
        return null;
    }


    private void getPricesAndStocks(EtspGoods goods, String partCode, String hashSession) {
        try {
            // Используем метод для получения остатков по коду детали
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("HashSession", hashSession);
            requestMap.put("Code", partCode);

            String rawResponse = webClient.post()
                    .uri("/v2/json/PartsRemains.svc/GetPartsRemainsByCode")
                    .bodyValue(requestMap)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            log.info("GetPartsRemainsByCode ответ: {}", rawResponse);

            if (rawResponse != null) {
                Map<String, Object> responseMap = objectMapper.readValue(rawResponse, Map.class);
                if (Boolean.TRUE.equals(responseMap.get("Success"))) {
                    parsePricesAndStocks(goods, responseMap);
                }
            }

            // Если не нашли данные, устанавливаем нулевые значения
            if (goods.getPrice() == null) {
                goods.setPrice(BigDecimal.ZERO);
            }
            if (goods.getQuantity() == null) {
                goods.setQuantity(0);
            }

        } catch (Exception e) {
            log.error("Ошибка получения цен и остатков: {}", e.getMessage());
            // В случае ошибки тоже устанавливаем нулевые значения
            goods.setPrice(BigDecimal.ZERO);
            goods.setQuantity(0);
        }
    }



    private void parsePricesAndStocks(EtspGoods goods, Map<String, Object> responseMap) {
        try {
            Map<String, Object> data = (Map<String, Object>) responseMap.get("Data");
            if (data != null) {
                List<Map<String, Object>> remains = (List<Map<String, Object>>) data.get("Remains");
                if (remains != null && !remains.isEmpty()) {
                    // Фильтруем остатки по нужному производителю
                    List<Map<String, Object>> filteredRemains = remains.stream()
                            .filter(r -> goods.getBrand().equals(r.get("ManufacturerName")))
                            .collect(Collectors.toList());

                    if (!filteredRemains.isEmpty()) {
                        // Находим лучшую цену с наличием
                        Map<String, Object> bestOffer = filteredRemains.stream()
                                .filter(r -> r.get("Price") != null)
                                .filter(r -> {
                                    // Обрабатываем Quantity который может быть String или Number
                                    Object quantityObj = r.get("Quantity");
                                    if (quantityObj instanceof Number) {
                                        return ((Number) quantityObj).intValue() > 0;
                                    } else if (quantityObj instanceof String) {
                                        String quantityStr = (String) quantityObj;
                                        if (quantityStr.startsWith(">")) {
                                            return true; // ">100" - значит есть в наличии
                                        }
                                        try {
                                            return Integer.parseInt(quantityStr) > 0;
                                        } catch (NumberFormatException e) {
                                            return false;
                                        }
                                    }
                                    return false;
                                })
                                .min((r1, r2) -> {
                                    BigDecimal price1 = new BigDecimal(r1.get("Price").toString());
                                    BigDecimal price2 = new BigDecimal(r2.get("Price").toString());
                                    return price1.compareTo(price2);
                                })
                                .orElse(null);

                        if (bestOffer != null) {
                            goods.setPrice(new BigDecimal(bestOffer.get("Price").toString()));

                            // Обрабатываем Quantity
                            Object quantityObj = bestOffer.get("Quantity");
                            if (quantityObj instanceof Number) {
                                goods.setQuantity(((Number) quantityObj).intValue());
                            } else if (quantityObj instanceof String) {
                                String quantityStr = (String) quantityObj;
                                if (quantityStr.startsWith(">")) {
                                    // Для значений типа ">100" берем минимальное значение
                                    try {
                                        int quantity = Integer.parseInt(quantityStr.substring(1));
                                        goods.setQuantity(quantity);
                                    } catch (NumberFormatException e) {
                                        goods.setQuantity(10); // Значение по умолчанию для ">N"
                                    }
                                } else {
                                    try {
                                        goods.setQuantity(Integer.parseInt(quantityStr));
                                    } catch (NumberFormatException e) {
                                        goods.setQuantity(0);
                                    }
                                }
                            }
                        } else {
                            // Товар есть в системе, но нет в наличии
                            goods.setPrice(BigDecimal.ZERO);
                            goods.setQuantity(0);
                        }
                    } else {
                        // Нет остатков для этого производителя
                        goods.setPrice(BigDecimal.ZERO);
                        goods.setQuantity(0);
                    }
                } else {
                    // Нет остатков вообще
                    goods.setPrice(BigDecimal.ZERO);
                    goods.setQuantity(0);
                }
            } else {
                // Нет данных в ответе
                goods.setPrice(BigDecimal.ZERO);
                goods.setQuantity(0);
            }
        } catch (Exception e) {
            log.warn("Ошибка парсинга цен и остатков: {}", e.getMessage());
            goods.setPrice(BigDecimal.ZERO);
            goods.setQuantity(0);
        }
    }




    private EtspGoods createBasicGoods(Part part) {
        EtspGoods goods = new EtspGoods();
        goods.setNumber(part.getManufacturerNumber() != null ? part.getManufacturerNumber() : part.getOmegaNumber());
        goods.setBrand(part.getManufacturerName() != null ? part.getManufacturerName() : part.getGroup());
        goods.setName(part.getName());
        goods.setSupplierCode(part.getCode());

        // Нулевые значения по умолчанию
        goods.setPrice(BigDecimal.ZERO);
        goods.setQuantity(0);

        return goods;
    }




    private EtspGoods convertToEtspGoods(Part part, Map<String, Object> goodsUnitMap) {
        EtspGoods goods = new EtspGoods();

        // Используем реальные данные из SearchNomenclature
        goods.setNumber(part.getManufacturerNumber());
        goods.setBrand(part.getManufacturerName()); // Правильный бренд!
        goods.setName(part.getName());
        goods.setSupplierCode(part.getCode());

        // Данные из GoodsUnit (если есть)
        if (goodsUnitMap.get("PartName") != null) {
            goods.setName(goodsUnitMap.get("PartName").toString());
        }
        if (goodsUnitMap.get("PartNote") != null) {
            // Можно установить описание если нужно
        }

        log.info("Создан EtspGoods: {} {}, код: {}",
                goods.getBrand(), goods.getNumber(), goods.getSupplierCode());

        return goods;
    }

    private boolean hasStock(Map<String, Object> remain) {
        Object quantityObj = remain.get("Quantity");
        if (quantityObj instanceof Number) {
            return ((Number) quantityObj).intValue() > 0;
        } else if (quantityObj instanceof String) {
            String quantityStr = (String) quantityObj;
            if (quantityStr.startsWith(">")) {
                return true;
            }
            try {
                return Integer.parseInt(quantityStr) > 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private int parseQuantity(Object quantityObj) {
        if (quantityObj instanceof Number) {
            return ((Number) quantityObj).intValue();
        } else if (quantityObj instanceof String) {
            String quantityStr = (String) quantityObj;
            if (quantityStr.startsWith(">")) {
                try {
                    return Integer.parseInt(quantityStr.substring(1));
                } catch (NumberFormatException e) {
                    return 1; // Минимальное значение для ">N"
                }
            }
            try {
                return Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }


    private Map<String, Object> findBestPrice(List<Map<String, Object>> prices) {
        return prices.stream()
                .filter(p -> p.get("Price") != null)
                .filter(p -> p.get("Quantity") != null && ((Number) p.get("Quantity")).intValue() > 0)
                .max((p1, p2) -> Integer.compare(
                        ((Number) p1.get("Quantity")).intValue(),
                        ((Number) p2.get("Quantity")).intValue()
                ))
                .orElse(null);
    }

    private EtspWarehouse convertToEtspWarehouse(Map<String, Object> stockMap) {
        EtspWarehouse warehouse = new EtspWarehouse();
        warehouse.setName((String) stockMap.get("WarehouseName"));

        Object quantityObj = stockMap.get("Quantity");
        if (quantityObj != null) {
            warehouse.setQuantity(((Number) quantityObj).intValue());
        }

        // Можно парсить дату доставки если есть
        // warehouse.setShipmentDate(...);

        warehouse.setIsOwn(true);
        return warehouse;
    }

    private String createSearchText(String article, String brand) {
        if (brand != null && !brand.trim().isEmpty()) {
            // Пробуем разные варианты комбинаций
            return article + " " + brand;  // Сначала артикул, потом бренд
        }
        return article;
    }



    @Override
    public String getSupplierName() {
        return "ETSP";
    }

    @Override
    public boolean isAvailable() {
        try {
            String hashSession = getValidHashSession();
            return hashSession != null;
        } catch (Exception e) {
            log.warn("ETSP недоступен: {}", e.getMessage());
            return false;
        }
    }
}
