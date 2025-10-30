package com.igorgorbachev.SpringBootBK.procenka.service.impl;

import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.dto.SearchResult;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.service.TmtrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public SearchResult processResults(List<PartOfferDto> allOffers, String article, String brand) {
        log.info("Processing {} offers for article='{}', brand='{}'", allOffers.size(), article, brand);

        // Группировка по поставщикам
        Map<String, List<PartOfferDto>> offersBySupplier = allOffers.stream()
                .collect(Collectors.groupingBy(PartOfferDto::getSupplierName));

        // Получаем TMTR офферы
        List<PartOfferDto> tmtrOffers = getTmtrOffers(article, brand, offersBySupplier);

        // Точные совпадения
        List<PartOfferDto> exactMatches = getExactMatches(allOffers, article, brand);

        // Офферы по поставщикам
        List<PartOfferDto> forumAutoOffers = sortByPrice(offersBySupplier.getOrDefault("Forum-Auto", Collections.emptyList()));
        List<PartOfferDto> favoritePartsOffers = sortByPrice(offersBySupplier.getOrDefault("Favorite Parts", Collections.emptyList()));
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

    private List<PartOfferDto> sortByPrice(List<PartOfferDto> offers) {
        return offers.stream()
                .sorted(Comparator.comparing(PartOfferDto::getPrice))
                .collect(Collectors.toList());
    }
}
