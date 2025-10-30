package com.igorgorbachev.SpringBootBK.procenka.controller;

import com.igorgorbachev.SpringBootBK.config.util.DeliveryUtils;
import com.igorgorbachev.SpringBootBK.procenka.dto.BrandSupplier;
import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.service.SupplierService;
import com.igorgorbachev.SpringBootBK.procenka.service.impl.SearchOrchestrationService;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.converter.ArmtekConverter;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.model.ArmtekGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.service.ArmtekService;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.service.impl.ArmtekGroupingService;
import com.igorgorbachev.SpringBootBK.procenka.util.DeliveryFormatter;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.igorgorbachev.SpringBootBK.procenka.dto.SearchResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SearchController {

    private final SearchOrchestrationService searchOrchestrationService;
    private final ArmtekGroupingService armtekGroupingService;
    private final List<SupplierService> supplierServices;
    private final DeliveryUtils deliveryUtils;
    private final ArmtekService armtekService;
    private final ArmtekConverter armtekConverter;
    private final DeliveryFormatter deliveryFormatter;

    @GetMapping("/search-page")
    public String showSearchPage(Model model) {
        log.info("=== SHOW SEARCH PAGE ===");
        initializeEmptyModel(model);
        return "search-results";
    }

    @GetMapping("/search")
    public String searchParts(
            @RequestParam String article,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false, defaultValue = "false") boolean refresh,
            @RequestParam(required = false, defaultValue = "false") boolean selectBrand,
            HttpSession session,
            Model model) {

        model.addAttribute("deliveryFormatter", deliveryFormatter);
        model.addAttribute("deliveryUtils", deliveryUtils);

        try {
            if (searchOrchestrationService.shouldShowBrandSelection(brand, selectBrand)) {
                return handleBrandSelection(article, model);
            }

            SearchResult searchResult = searchOrchestrationService.search(article, brand, refresh);
            populateModelWithResults(model, article, brand, searchResult);

        } catch (Exception e) {
            log.error("Ошибка при поиске: {}", e.getMessage(), e);
            handleSearchError(model, e.getMessage());
        }

        return "search-results";
    }

    private String handleBrandSelection(String article, Model model) {
        List<BrandSupplier> availableBrands = searchOrchestrationService.findAvailableBrands(article);
        model.addAttribute("availableBrands", availableBrands);
        model.addAttribute("article", article);
        model.addAttribute("selectBrandMode", true);
        return "search-results";
    }

    private void populateModelWithResults(Model model, String article, String brand, SearchResult searchResult) {
        model.addAttribute("article", article);
        model.addAttribute("brand", brand);

        // Подготавливаем данные Armtek
        List<PartOfferDto> armtekRawOffers = getArmtekRawOffers(article, brand);
        List<PartOfferDto> groupedArmtekOffers = armtekGroupingService.groupArmtekOffers(armtekRawOffers);
        List<PartOfferDto> exactMatchesArmtek = armtekGroupingService.filterExactMatches(armtekRawOffers, article, brand);

        // Объединяем точные совпадения
        List<PartOfferDto> allExactMatches = prepareExactMatches(searchResult, exactMatchesArmtek);

        // Устанавливаем атрибуты модели
        setModelAttributes(model, groupedArmtekOffers, allExactMatches, searchResult);
    }

    private List<PartOfferDto> prepareExactMatches(SearchResult searchResult, List<PartOfferDto> exactMatchesArmtek) {
        List<PartOfferDto> otherExactMatches = searchResult.getExactMatches().stream()
                .filter(offer -> !"Armtek".equals(offer.getSupplierName()))
                .collect(Collectors.toList());

        List<PartOfferDto> allExactMatches = new ArrayList<>();
        allExactMatches.addAll(exactMatchesArmtek);
        allExactMatches.addAll(otherExactMatches);

        // Сортируем по цене и дате доставки - используем публичный метод из сервиса
        allExactMatches.sort(Comparator
                .comparing((PartOfferDto offer) ->
                        offer.getPrice() != null ? offer.getPrice() : Double.MAX_VALUE)
                .thenComparing(armtekGroupingService::getEarliestDeliveryDateFromOffer) // ссылка на метод
        );

        return allExactMatches;
    }

    private void setModelAttributes(Model model,
                                    List<PartOfferDto> armtekOffers,
                                    List<PartOfferDto> exactMatches,
                                    SearchResult searchResult) {
        model.addAttribute("armtekOffers", armtekOffers);
        model.addAttribute("exactMatches", exactMatches);
        model.addAttribute("forumAutoOffers", searchResult.getForumAutoOffers());
        model.addAttribute("favoritePartsOffers", searchResult.getFavoritePartsOffers());
        model.addAttribute("tmtrOffers", searchResult.getTmtrOffers());
        model.addAttribute("processedOffers", searchResult.getProcessedOffers());
        model.addAttribute("supplierNames", getAvailableSupplierNames());
        model.addAttribute("totalOffersCount", searchResult.getTotalOffersCount());
        model.addAttribute("selectBrandMode", false);
    }

    private List<PartOfferDto> getArmtekRawOffers(String article, String brand) {
        try {
            List<ArmtekGoods> rawGoods = armtekService.getLastRawGoods();

            if (rawGoods.isEmpty()) {
                var armtekResult = armtekService.searchPartsDetailed(article, brand);
                rawGoods = armtekResult.getRawGoods();
            }

            log.info("Получено {} сырых товаров от Armtek", rawGoods.size());
            return armtekConverter.toPartOfferDtos(rawGoods);

        } catch (Exception e) {
            log.error("Ошибка при получении сырых данных Armtek: {}", e.getMessage());
            return List.of();
        }
    }

    private List<String> getAvailableSupplierNames() {
        return supplierServices.stream()
                .filter(SupplierService::isAvailable)
                .map(SupplierService::getSupplierName)
                .collect(Collectors.toList());
    }

    private void handleSearchError(Model model, String errorMessage) {
        model.addAttribute("error", "Ошибка при поиске: " + errorMessage);
        initializeEmptyModel(model);
    }

    private void initializeEmptyModel(Model model) {
        model.addAttribute("selectBrandMode", false);
        model.addAttribute("exactMatches", List.of());
        model.addAttribute("forumAutoOffers", List.of());
        model.addAttribute("favoritePartsOffers", List.of());
        model.addAttribute("armtekOffers", List.of());
        model.addAttribute("tmtrOffers", List.of());
        model.addAttribute("processedOffers", List.of());
        model.addAttribute("supplierNames", List.of());
        model.addAttribute("totalOffersCount", 0);
    }
}