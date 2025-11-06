package com.igorgorbachev.SpringBootBK.procenka.service.impl;

import com.igorgorbachev.SpringBootBK.config.util.DeliveryUtils;
import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.dto.SearchResult;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.service.TmtrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultProcessingService {

    private final TmtrService tmtrService;
    private final DeliveryUtils deliveryUtils;

//    public SearchResult processResults(List<PartOfferDto> allOffers, String article, String brand) {
//        log.info("Processing {} offers for article='{}', brand='{}'", allOffers.size(), article, brand);
//
//        // Группировка по поставщикам
//        Map<String, List<PartOfferDto>> offersBySupplier = allOffers.stream()
//                .collect(Collectors.groupingBy(PartOfferDto::getSupplierName));
//
//        // Получаем TMTR офферы
//        List<PartOfferDto> tmtrOffers = getTmtrOffers(article, brand, offersBySupplier);
//
//        // Точные совпадения
//        List<PartOfferDto> exactMatches = getExactMatches(allOffers, article, brand);
//
//        // Офферы по поставщикам
//        List<PartOfferDto> forumAutoOffers = sortByPrice(offersBySupplier.getOrDefault("Forum-Auto", Collections.emptyList()));
//        List<PartOfferDto> favoritePartsOffers = sortByPrice(offersBySupplier.getOrDefault("Favorite Parts", Collections.emptyList()));
//        List<PartOfferDto> armtekOffers = sortByPrice(offersBySupplier.getOrDefault("Armtek", Collections.emptyList()));
//
//        log.info("Processed results - Exact: {}, Forum-Auto: {}, Favorite Parts: {}, Armtek: {}, TMTR: {}",
//                exactMatches.size(), forumAutoOffers.size(), favoritePartsOffers.size(),
//                armtekOffers.size(), tmtrOffers.size());
//
//        return SearchResult.builder()
//                .exactMatches(exactMatches)
//                .forumAutoOffers(forumAutoOffers)
//                .favoritePartsOffers(favoritePartsOffers)
//                .armtekOffers(armtekOffers)
//                .tmtrOffers(tmtrOffers)
//                .processedOffers(Collections.emptyList())
//                .totalOffersCount(allOffers.size())
//                .build();
//    }

    public SearchResult processResults(List<PartOfferDto> allOffers, String article, String brand) {
        log.info("Processing {} offers for article='{}', brand='{}'", allOffers.size(), article, brand);

        // Группировка по поставщикам
        Map<String, List<PartOfferDto>> offersBySupplier = allOffers.stream()
                .collect(Collectors.groupingBy(PartOfferDto::getSupplierName));

        log.info("Found suppliers: {}", offersBySupplier.keySet());

        // Детально логируем Favorite Parts до сортировки
        List<PartOfferDto> rawFavoriteParts = offersBySupplier.getOrDefault("Favorite Parts", Collections.emptyList());
        log.info("Raw Favorite Parts count: {}", rawFavoriteParts.size());

        // Получаем TMTR офферы
        List<PartOfferDto> tmtrOffers = getTmtrOffers(article, brand, offersBySupplier);

        // Точные совпадения
        List<PartOfferDto> exactMatches = getExactMatches(allOffers, article, brand);

        // Офферы по поставщикам с сортировкой
        List<PartOfferDto> forumAutoOffers = sortByPrice(offersBySupplier.getOrDefault("Forum-Auto", Collections.emptyList()));
        List<PartOfferDto> favoritePartsOffers = sortFavoritePartsOffers(rawFavoriteParts);
        List<PartOfferDto> armtekOffers = sortByPrice(offersBySupplier.getOrDefault("Armtek", Collections.emptyList()));

        log.info("Processed results - Exact: {}, Forum-Auto: {}, Favorite Parts: {}, Armtek: {}, TMTR: {}",
                exactMatches.size(), forumAutoOffers.size(), favoritePartsOffers.size(),
                armtekOffers.size(), tmtrOffers.size());

        return SearchResult.builder()
                .exactMatches(exactMatches)
                .forumAutoOffers(forumAutoOffers)
                .favoritePartsOffers(favoritePartsOffers)
                .armtekOffers(armtekOffers)
                .tmtrOffers(tmtrOffers)
                .processedOffers(Collections.emptyList())
                .totalOffersCount(allOffers.size())
                .build();
    }

    /**
     * Специальная сортировка для Favorite Parts:
     * 1. Сначала товары со своими складами
     * 2. Потом по цене (от меньшей к большей)
     */
    private List<PartOfferDto> sortFavoritePartsOffers(List<PartOfferDto> offers) {
        if (offers == null || offers.isEmpty()) {
            return offers;
        }

        log.info("=== RESULT PROCESSING SERVICE: Sorting Favorite Parts with structure ===");
        log.info("Main offers to sort: {}", offers.size());

        // 1. СОРТИРУЕМ ОСНОВНЫЕ ТОВАРЫ
        List<PartOfferDto> sortedMainOffers = deliveryUtils.sortFavoritePartsOffers(offers);

        // Логируем сортировку основных товаров
        log.info("=== MAIN OFFERS SORTING RESULT ===");
        for (int i = 0; i < Math.min(sortedMainOffers.size(), 5); i++) {
            PartOfferDto offer = sortedMainOffers.get(i);
            log.info("MAIN SORTED [{}]: brand={}, price={}, hasOwnWarehouses={}",
                    i, offer.getBrand(), offer.getPrice(),
                    deliveryUtils.hasOwnWarehouses(offer));
        }

        // 2. ДЛЯ КАЖДОГО ОСНОВНОГО ТОВАРА СОРТИРУЕМ ЕГО АНАЛОГИ
        for (PartOfferDto mainOffer : sortedMainOffers) {
            if (mainOffer.getAnalogues() != null && !mainOffer.getAnalogues().isEmpty()) {
                log.info("Sorting {} analogues for main offer: {}",
                        mainOffer.getAnalogues().size(), mainOffer.getBrand());

                List<PartOfferDto> sortedAnalogues = deliveryUtils.sortFavoritePartsOffers(mainOffer.getAnalogues());
                mainOffer.setAnalogues(sortedAnalogues);

                // Логируем сортировку аналогов
                for (int i = 0; i < Math.min(sortedAnalogues.size(), 3); i++) {
                    PartOfferDto analogue = sortedAnalogues.get(i);
                    log.info("ANALOGUE SORTED [{}]: brand={}, price={}, hasOwnWarehouses={}",
                            i, analogue.getBrand(), analogue.getPrice(),
                            deliveryUtils.hasOwnWarehouses(analogue));
                }
            }
        }

        log.info("Final sorted structure: {} main offers", sortedMainOffers.size());
        return sortedMainOffers;
    }



    private List<PartOfferDto> getTmtrOffers(String article, String brand, Map<String, List<PartOfferDto>> offersBySupplier) {
        try {
            List<PartOfferDto> tmtrOffers = tmtrService.getAllTmtrParts(article, brand);
            log.info("TMTR service returned {} offers", tmtrOffers.size());
            return sortByPrice(tmtrOffers);
        } catch (Exception e) {
            log.error("Error getting TMTR offers, using fallback: {}", e.getMessage());
            return sortByPrice(offersBySupplier.getOrDefault("TMTR", Collections.emptyList()));
        }
    }

    private List<PartOfferDto> getExactMatches(List<PartOfferDto> allOffers, String article, String brand) {
        List<PartOfferDto> exactMatches = allOffers.stream()
                .filter(offer -> isExactMatch(offer, article, brand))
                .sorted(Comparator.comparing(PartOfferDto::getPrice))
                .collect(Collectors.toList());

        log.info("Found {} exact matches for article='{}', brand='{}'", exactMatches.size(), article, brand);
        return exactMatches;
    }

    private boolean isExactMatch(PartOfferDto offer, String searchedArticle, String searchedBrand) {
        if (searchedBrand == null || offer.getBrand() == null) {
            return false;
        }

        boolean brandMatch = normalizeString(offer.getBrand()).equals(normalizeString(searchedBrand));
        boolean articleMatch = isExactArticleMatch(offer, searchedArticle);

        return brandMatch && articleMatch;
    }

    private boolean isExactArticleMatch(PartOfferDto offer, String searchedArticle) {
        if (searchedArticle == null || offer.getOriginalArticle() == null) {
            return false;
        }

        String offerArt = normalizeString(offer.getOriginalArticle());
        String searchedArt = normalizeString(searchedArticle);

        return offerArt.equals(searchedArt) ||
                getDigitsOnly(offerArt).equals(getDigitsOnly(searchedArt));
    }

    private String normalizeString(String input) {
        if (input == null) return "";
        return input.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private String getDigitsOnly(String str) {
        if (str == null) return "";
        return str.replaceAll("[^0-9]", "");
    }

//    private List<PartOfferDto> sortByPrice(List<PartOfferDto> offers) {
//        return offers.stream()
//                .sorted(Comparator.comparing(PartOfferDto::getPrice))
//                .collect(Collectors.toList());
//    }

    /**
     * Стандартная сортировка по цене для других поставщиков
     */
    private List<PartOfferDto> sortByPrice(List<PartOfferDto> offers) {
        if (offers == null || offers.isEmpty()) {
            return offers;
        }

        return offers.stream()
                .sorted(Comparator.comparing(offer ->
                        offer.getPrice() != null ? offer.getPrice() : Double.MAX_VALUE))
                .collect(Collectors.toList());
    }
}
