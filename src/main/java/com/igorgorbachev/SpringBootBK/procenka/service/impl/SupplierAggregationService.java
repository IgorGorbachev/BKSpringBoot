package com.igorgorbachev.SpringBootBK.procenka.service.impl;

import com.igorgorbachev.SpringBootBK.procenka.dto.BrandSupplier;
import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.service.SupplierService;
import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.service.impl.ForumAutoServiceImpl;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.service.TmtrService;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.service.impl.TmtrServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//@Slf4j
//@Service
//public class SupplierAggregationService {
//    private final List<SupplierService> supplierServices;
//    private String lastTmtrRawResponse;
//
//    public String getTmtrRawResponse() {
//        return this.lastTmtrRawResponse;
//    }
//
//    public SupplierAggregationService(List<SupplierService> supplierServices) {
//        this.supplierServices = supplierServices;
//        log.info("=== SUPPLIER AGGREGATION SERVICE INITIALIZED ===");
//        log.info("Total suppliers found: {}", supplierServices.size());
//        supplierServices.forEach(supplier ->
//                log.info(" - {}: available={}", supplier.getSupplierName(), supplier.isAvailable())
//        );
//    }
//
//    public List<PartOfferDto> searchAllSuppliers(String article, String brand) {
//        log.info("Starting parallel search across all suppliers for article: '{}', brand: '{}'", article, brand);
//        this.lastTmtrRawResponse = null;
//
//        List<PartOfferDto> allOffers = Collections.synchronizedList(new ArrayList<>());
//        List<CompletableFuture<Void>> futures = supplierServices.stream()
//                .filter(SupplierService::isAvailable)
//                .map(supplier -> searchSupplierAsync(supplier, article, brand, allOffers))
//                .collect(Collectors.toList());
//
//        waitForCompletion(futures);
//        log.info("Total offers from all suppliers: {}", allOffers.size());
//        return allOffers;
//    }
//
//    private CompletableFuture<Void> searchSupplierAsync(SupplierService supplier, String article, String brand, List<PartOfferDto> allOffers) {
//        return CompletableFuture.runAsync(() -> {
//            try {
//                log.info("Searching in supplier: {}", supplier.getSupplierName());
//                List<PartOfferDto> supplierOffers = performSupplierSearch(supplier, article, brand);
//
//                if (supplierOffers != null && !supplierOffers.isEmpty()) {
//                    allOffers.addAll(supplierOffers);
//                    log.info("Supplier {} returned {} offers", supplier.getSupplierName(), supplierOffers.size());
//                } else {
//                    log.info("Supplier {} returned no offers", supplier.getSupplierName());
//                }
//            } catch (Exception e) {
//                log.error("Error searching in supplier {}: {}", supplier.getSupplierName(), e.getMessage());
//            }
//        });
//    }
//
//    private List<PartOfferDto> performSupplierSearch(SupplierService supplier, String article, String brand) {
//        if (supplier instanceof ForumAutoServiceImpl) {
//            return ((ForumAutoServiceImpl) supplier).searchAnalogues(article, brand);
//        } else {
//            List<PartOfferDto> offers = supplier.searchParts(article, brand);
//            captureTmtrRawResponse(supplier);
//            return offers;
//        }
//    }
//
//    private void captureTmtrRawResponse(SupplierService supplier) {
//        if (supplier instanceof TmtrServiceImpl) {
//            String rawResponse = ((TmtrServiceImpl) supplier).getLastRawResponse();
//            if (rawResponse != null) {
//                this.lastTmtrRawResponse = rawResponse;
//                log.info("Saved TMTR raw response, length: {}", rawResponse.length());
//            }
//        }
//    }
//
//    private void waitForCompletion(List<CompletableFuture<Void>> futures) {
//        try {
//            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
//                    .get(30, TimeUnit.SECONDS);
//        } catch (Exception e) {
//            log.error("Error waiting for search completion: {}", e.getMessage());
//        }
//    }
//
//    public List<BrandSupplier> findAvailableBrands(String article) {
//        Map<String, String> brandToSupplierMap = new HashMap<>();
//
//        for (SupplierService supplier : supplierServices) {
//            if (supplier.isAvailable()) {
//                try {
//                    log.info("Поиск брендов в поставщике: {}", supplier.getSupplierName());
//
//
//
//                    if (supplier instanceof TmtrService) {
//                        // ОСОБАЯ ЛОГИКА ДЛЯ TMTR - используем метод getBrands
//                        TmtrService tmtrService = (TmtrService) supplier;
//                        List<String> supplierBrands = tmtrService.getBrands(article);
//                        log.info("Поставщик TMTR вернул {} брендов", supplierBrands.size());
//
//                        for (String brand : supplierBrands) {
//                            if (brand != null && !brand.trim().isEmpty()) {
//                                String cleanBrand = brand.trim();
//                                brandToSupplierMap.put(cleanBrand, supplier.getSupplierName());
//                            }
//                        }
//                    } else {
//                        // Для других поставщиков используем стандартный метод
//                        List<PartOfferDto> offers = supplier.searchParts(article, null);
//                        List<String> supplierBrands = offers.stream()
//                                .map(PartOfferDto::getBrand)
//                                .filter(b -> b != null && !b.trim().isEmpty())
//                                .map(String::trim)
//                                .distinct()
//                                .collect(Collectors.toList());
//                        log.info("Поставщик {} вернул {} брендов", supplier.getSupplierName(), supplierBrands.size());
//
//                        for (String brand : supplierBrands) {
//                            brandToSupplierMap.put(brand, supplier.getSupplierName());
//                        }
//                    }
//                } catch (Exception e) {
//                    log.warn("Ошибка при поиске брендов в поставщике {}: {}",
//                            supplier.getSupplierName(), e.getMessage());
//                }
//            }
//        }
//
//        // Преобразуем Map в List объектов BrandSupplier
//        List<BrandSupplier> result = brandToSupplierMap.entrySet().stream()
//                .map(entry -> new BrandSupplier(entry.getKey(), entry.getValue()))
//                .sorted(Comparator.comparing(BrandSupplier::getBrand))
//                .collect(Collectors.toList());
//
//        log.info("Всего найдено брендов: {}", result.size());
//        return result;
//    }
//}

@Slf4j
@Service
public class SupplierAggregationService {
    private final List<SupplierService> supplierServices;
    private String lastTmtrRawResponse;

    public SupplierAggregationService(List<SupplierService> supplierServices) {
        this.supplierServices = supplierServices;
        log.info("=== SUPPLIER AGGREGATION SERVICE INITIALIZED ===");
        log.info("Total suppliers found: {}", supplierServices.size());
        supplierServices.forEach(supplier ->
                log.info(" - {}: available={}", supplier.getSupplierName(), supplier.isAvailable())
        );
    }

    public String getTmtrRawResponse() {
        return this.lastTmtrRawResponse;
    }

    /**
     * Старый синхронный метод для обратной совместимости
     */
    public List<PartOfferDto> searchAllSuppliers(String article, String brand) {
        return searchAllSuppliersReactive(article, brand)
                .blockOptional()
                .orElse(Collections.emptyList());
    }

    /**
     * Новая реактивная версия
     */
    public Mono<List<PartOfferDto>> searchAllSuppliersReactive(String article, String brand) {
        log.info("Starting reactive search across all suppliers for article: '{}', brand: '{}'", article, brand);
        this.lastTmtrRawResponse = null;

        List<Mono<List<PartOfferDto>>> supplierMonos = supplierServices.stream()
                .filter(SupplierService::isAvailable)
                .map(supplier -> searchSupplierReactive(supplier, article, brand))
                .collect(Collectors.toList());

        return Flux.merge(supplierMonos)
                .collectList()
                .map(lists -> lists.stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList()))
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(result -> log.info("Reactive search completed: found {} offers", result.size()))
                .doOnError(error -> log.error("Reactive search failed: {}", error.getMessage()))
                .onErrorReturn(Collections.emptyList());
    }

    private Mono<List<PartOfferDto>> searchSupplierReactive(SupplierService supplier,
                                                            String article, String brand) {
        return Mono.fromCallable(() -> performSupplierSearch(supplier, article, brand))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(error -> {
                    log.error("Error searching in supplier {}: {}", supplier.getSupplierName(), error.getMessage());
                    return Mono.just(Collections.emptyList());
                })
                .doOnSubscribe(subscription ->
                        log.debug("Starting search in supplier: {}", supplier.getSupplierName()))
                .doOnSuccess(result ->
                        log.debug("Supplier {} returned {} offers", supplier.getSupplierName(), result.size()));
    }

    private List<PartOfferDto> performSupplierSearch(SupplierService supplier, String article, String brand) {
        if (supplier instanceof ForumAutoServiceImpl) {
            return ((ForumAutoServiceImpl) supplier).searchAnalogues(article, brand);
        } else {
            List<PartOfferDto> offers = supplier.searchParts(article, brand);
            captureTmtrRawResponse(supplier);
            return offers;
        }
    }

    private void captureTmtrRawResponse(SupplierService supplier) {
        if (supplier instanceof TmtrServiceImpl) {
            String rawResponse = ((TmtrServiceImpl) supplier).getLastRawResponse();
            if (rawResponse != null) {
                this.lastTmtrRawResponse = rawResponse;
                log.debug("Saved TMTR raw response, length: {}", rawResponse.length());
            }
        }
    }

    public List<BrandSupplier> findAvailableBrands(String article) {
        Map<String, String> brandToSupplierMap = new ConcurrentHashMap<>();

        List<Mono<Void>> brandSearchMonos = supplierServices.stream()
                .filter(SupplierService::isAvailable)
                .map(supplier -> searchBrandsFromSupplier(supplier, article, brandToSupplierMap))
                .collect(Collectors.toList());

        // Ждем завершения всех поисков брендов
        Flux.merge(brandSearchMonos)
                .blockLast(Duration.ofSeconds(15));

        // Преобразуем Map в List объектов BrandSupplier
        List<BrandSupplier> result = brandToSupplierMap.entrySet().stream()
                .map(entry -> new BrandSupplier(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(BrandSupplier::getBrand))
                .collect(Collectors.toList());

        log.info("Всего найдено брендов: {}", result.size());
        return result;
    }

    private Mono<Void> searchBrandsFromSupplier(SupplierService supplier, String article,
                                                Map<String, String> brandToSupplierMap) {
        return Mono.fromRunnable(() -> {
            try {
                log.info("Поиск брендов в поставщике: {}", supplier.getSupplierName());

                if (supplier instanceof TmtrService) {
                    // ОСОБАЯ ЛОГИКА ДЛЯ TMTR - используем метод getBrands
                    TmtrService tmtrService = (TmtrService) supplier;
                    List<String> supplierBrands = tmtrService.getBrands(article);
                    log.info("Поставщик TMTR вернул {} брендов", supplierBrands.size());

                    for (String brand : supplierBrands) {
                        if (brand != null && !brand.trim().isEmpty()) {
                            String cleanBrand = brand.trim();
                            brandToSupplierMap.put(cleanBrand, supplier.getSupplierName());
                        }
                    }
                } else {
                    // Для других поставщиков используем стандартный метод
                    List<PartOfferDto> offers = supplier.searchParts(article, null);
                    List<String> supplierBrands = offers.stream()
                            .map(PartOfferDto::getBrand)
                            .filter(b -> b != null && !b.trim().isEmpty())
                            .map(String::trim)
                            .distinct()
                            .collect(Collectors.toList());
                    log.info("Поставщик {} вернул {} брендов", supplier.getSupplierName(), supplierBrands.size());

                    for (String brand : supplierBrands) {
                        brandToSupplierMap.put(brand, supplier.getSupplierName());
                    }
                }
            } catch (Exception e) {
                log.warn("Ошибка при поиске брендов в поставщике {}: {}",
                        supplier.getSupplierName(), e.getMessage());
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}

