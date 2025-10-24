package com.igorgorbachev.SpringBootBK.procenka.service.impl;

import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.service.SupplierService;
import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.service.impl.ForumAutoServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SupplierAggregationService {
    private final List<SupplierService> supplierServices;

    public SupplierAggregationService(List<SupplierService> supplierServices) {
        this.supplierServices = supplierServices;
        log.info("Loaded suppliers: {}", supplierServices.stream()
                .map(SupplierService::getSupplierName)
                .collect(Collectors.toList()));
    }

    /**
     * Нормализация артикула для сравнения
     * Удаляет все не-алфавитно-цифровые символы (пробелы, дефисы, спецсимволы)
     * и приводит к нижнему регистру
     */
    private String normalizeArticle(String article) {
        if (article == null) {
            return "";
        }
        return article.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    /**
     * Генерация ключа для сравнения предложений
     */
    private String generateOfferKey(PartOfferDto offer) {
        return String.format("%s|%s|%s",
                normalizeArticle(offer.getBrand()),
                normalizeArticle(offer.getOriginalArticle()),
                normalizeArticle(offer.getPartName())
        );
    }

    /**
     * Поиск по всем поставщикам с объединением результатов
     */
//    public List<PartOfferDto> searchAllSuppliers(String article, String brand) {
//        log.info("Starting search across {} suppliers: article={}, brand={}",
//                supplierServices.size(), article, brand);
//
//        List<CompletableFuture<List<PartOfferDto>>> futures = supplierServices.stream()
//                .filter(SupplierService::isAvailable)
//                .map(supplier -> CompletableFuture.supplyAsync(() -> {
//                            try {
//                                log.debug("Searching in supplier: {}", supplier.getSupplierName());
//                                List<PartOfferDto> results = supplier.searchParts(article, brand);
//                                log.info("Supplier {} returned {} results",
//                                        supplier.getSupplierName(), results.size());
//
//                                // Логируем результаты TMTR для диагностики
//                                if ("TMTR".equals(supplier.getSupplierName()) && !results.isEmpty()) {
//                                    log.info("TMTR results details:");
//                                    results.forEach(offer ->
//                                            log.info(" - Article: '{}', Price: {}, Brand: '{}'",
//                                                    offer.getOriginalArticle(), offer.getPrice(), offer.getBrand())
//                                    );
//                                }
//
//                                return results;
//                            } catch (Exception e) {
//                                log.error("Error in supplier {}: {}",
//                                        supplier.getSupplierName(), e.getMessage());
//                                return List.<PartOfferDto>of();
//                            }
//                        })
//                        .orTimeout(45, TimeUnit.SECONDS)
//                        .exceptionally(ex -> {
//                            log.warn("Supplier {} timed out: {}",
//                                    supplier.getSupplierName(), ex.getMessage());
//                            return List.<PartOfferDto>of();
//                        }))
//                .collect(Collectors.toList());
//
//        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
//                futures.toArray(new CompletableFuture[0])
//        );
//
//        List<PartOfferDto> allResults = allFutures.thenApply(v ->
//                futures.stream()
//                        .map(future -> {
//                            try {
//                                return future.join();
//                            } catch (Exception e) {
//                                log.error("Error joining future: {}", e.getMessage());
//                                return List.<PartOfferDto>of();
//                            }
//                        })
//                        .flatMap(List::stream)
//                        .collect(Collectors.toList())
//        ).join();
//
//        // ДОБАВЛЯЕМ ПОДРОБНОЕ ЛОГИРОВАНИЕ ФИНАЛЬНЫХ РЕЗУЛЬТАТОВ
//        log.info("=== FINAL RESULTS FROM ALL SUPPLIERS ===");
//        log.info("Total results from all suppliers: {}", allResults.size());
//
//        // Группируем по поставщикам для наглядности
//        Map<String, List<PartOfferDto>> groupedBySupplier = allResults.stream()
//                .collect(Collectors.groupingBy(PartOfferDto::getSupplierName));
//
//        groupedBySupplier.forEach((supplier, offers) -> {
//            log.info("{}: {} offers", supplier, offers.size());
//            offers.forEach(offer ->
//                    log.info(" - {}: Article='{}', Price={}, Brand='{}'",
//                            supplier, offer.getOriginalArticle(), offer.getPrice(), offer.getBrand())
//            );
//        });
//
//        // Проверяем наличие TMTR в финальных результатах
//        List<PartOfferDto> tmtrResults = allResults.stream()
//                .filter(offer -> "TMTR".equals(offer.getSupplierName()))
//                .collect(Collectors.toList());
//
//        log.info("TMTR in final results: {} offers", tmtrResults.size());
//        if (!tmtrResults.isEmpty()) {
//            log.info("TMTR final offers (sorted by price):");
//            tmtrResults.stream()
//                    .sorted(Comparator.comparing(PartOfferDto::getPrice))
//                    .forEach(offer ->
//                            log.info(" - Price: {}, Article: '{}', Brand: '{}'",
//                                    offer.getPrice(), offer.getOriginalArticle(), offer.getBrand())
//                    );
//        }
//
//        return allResults;
//    }

    public List<PartOfferDto> searchAllSuppliers(String article, String brand) {
        List<PartOfferDto> allOffers = new ArrayList<>();

        // Сначала ищем точные совпадения
        for (SupplierService supplier : supplierServices) {
            if (supplier.isAvailable()) {
                try {
                    List<PartOfferDto> offers = supplier.searchParts(article, brand);
                    allOffers.addAll(offers);
                    log.info("Supplier {} found {} exact matches", supplier.getSupplierName(), offers.size());
                } catch (Exception e) {
                    log.error("Ошибка при поиске в поставщике {}: {}",
                            supplier.getSupplierName(), e.getMessage());
                }
            }
        }

        // Затем ищем аналоги (если указан бренд)
        if (brand != null && !brand.trim().isEmpty()) {
            for (SupplierService supplier : supplierServices) {
                if (supplier.isAvailable() && supplier instanceof ForumAutoServiceImpl) {
                    try {
                        ForumAutoServiceImpl forumAuto = (ForumAutoServiceImpl) supplier;
                        List<PartOfferDto> analogues = forumAuto.searchAnalogues(article, brand);
                        allOffers.addAll(analogues);
                        log.info("Supplier {} found {} analogues", supplier.getSupplierName(), analogues.size());
                    } catch (Exception e) {
                        log.error("Ошибка при поиске аналогов в поставщике {}: {}",
                                supplier.getSupplierName(), e.getMessage());
                    }
                }
            }
        }

        log.info("Total offers found: {} (exact matches + analogues)", allOffers.size());
        return allOffers;
    }

    /**
     * Поиск с фильтрацией дубликатов (оставляем самую низкую цену)
     */
    public List<PartOfferDto> searchWithDeduplication(String article, String brand) {
        List<PartOfferDto> allOffers = searchAllSuppliers(article, brand);
        return deduplicateOffers(allOffers);
    }

    /**
     * Удаление дубликатов - оставляем предложение с наименьшей ценой
     */
    public List<PartOfferDto> deduplicateOffers(List<PartOfferDto> offers) {
        Map<String, PartOfferDto> bestOffers = new HashMap<>();

        for (PartOfferDto offer : offers) {
            String key = generateOfferKey(offer);

            if (!bestOffers.containsKey(key)) {
                bestOffers.put(key, offer);
            } else {
                PartOfferDto existingOffer = bestOffers.get(key);
                // Оставляем предложение с меньшей ценой
                if (offer.getPrice() < existingOffer.getPrice()) {
                    bestOffers.put(key, offer);
                }
            }
        }

        return new ArrayList<>(bestOffers.values());
    }

    /**
     * Поиск с сортировкой по цене
     */
    public List<PartOfferDto> searchSortedByPrice(String article, String brand) {
        List<PartOfferDto> offers = searchWithDeduplication(article, brand);
        return offers.stream()
                .sorted(Comparator.comparing(PartOfferDto::getPrice))
                .collect(Collectors.toList());
    }

    /**
     * Поиск только в наличии
     */
    public List<PartOfferDto> searchInStockOnly(String article, String brand) {
        List<PartOfferDto> offers = searchAllSuppliers(article, brand);
        return offers.stream()
                .filter(offer -> offer.getQuantityAvailable() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Группировка по поставщикам
     */
    public Map<String, List<PartOfferDto>> searchGroupedBySupplier(String article, String brand) {
        List<PartOfferDto> offers = searchAllSuppliers(article, brand);
        return offers.stream()
                .collect(Collectors.groupingBy(PartOfferDto::getSupplierName));
    }

    /**
     * Поиск с приоритетом точных совпадений по артикулу
     */
    public List<PartOfferDto> searchWithExactMatchPriority(String article, String brand) {
        List<PartOfferDto> allOffers = searchAllSuppliers(article, brand);
        return sortWithExactMatchPriority(allOffers, article);
    }

    /**
     * Сортировка с приоритетом точных совпадений по артикулу
     */
    public List<PartOfferDto> sortWithExactMatchPriority(List<PartOfferDto> offers, String searchedArticle) {
        if (offers == null || offers.isEmpty()) {
            return offers;
        }

        String normalizedSearched = normalizeArticle(searchedArticle);

        return offers.stream()
                .sorted((offer1, offer2) -> {
                    String art1 = normalizeArticle(offer1.getOriginalArticle());
                    String art2 = normalizeArticle(offer2.getOriginalArticle());

                    boolean exactMatch1 = art1.equals(normalizedSearched);
                    boolean exactMatch2 = art2.equals(normalizedSearched);

                    // Точные совпадения идут первыми
                    if (exactMatch1 && !exactMatch2) {
                        return -1;
                    } else if (!exactMatch1 && exactMatch2) {
                        return 1;
                    } else if (exactMatch1 && exactMatch2) {
                        // Если оба точные совпадения - сортируем по цене (дешевые первые)
                        return Double.compare(offer1.getPrice(), offer2.getPrice());
                    } else {
                        // Для не точных совпадений - сортируем по цене
                        return Double.compare(offer1.getPrice(), offer2.getPrice());
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Поиск только точных совпадений
     */
    public List<PartOfferDto> searchExactMatchesOnly(String article, String brand) {
        List<PartOfferDto> allOffers = searchAllSuppliers(article, brand);
        String normalizedSearched = normalizeArticle(article);

        return allOffers.stream()
                .filter(offer -> {
                    String offerArticle = normalizeArticle(offer.getOriginalArticle());
                    return offerArticle.equals(normalizedSearched);
                })
                .collect(Collectors.toList());
    }


}
