package com.igorgorbachev.SpringBootBK.procenka.controller;


import com.igorgorbachev.SpringBootBK.procenka.dto.BrandSupplier;
import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.service.SupplierService;
import com.igorgorbachev.SpringBootBK.procenka.service.impl.SupplierAggregationService;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.service.TmtrService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SearchController {

    private final SupplierAggregationService aggregationService;
    private final List<SupplierService> supplierServices;


    @GetMapping("/search-page")
    public String showSearchPage(Model model) {
        log.info("=== SHOW SEARCH PAGE ===");
        model.addAttribute("selectBrandMode", false);
        model.addAttribute("exactMatches", List.of());
        model.addAttribute("otherOffers", List.of());
        model.addAttribute("supplierNames", List.of());
        model.addAttribute("totalOffersCount", 0);
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

        log.info("=== SEARCH CONTROLLER CALLED ===");
        log.info("Article: '{}', Brand: '{}', Refresh: {}, SelectBrand: {}",
                article, brand, refresh, selectBrand);

        // Очищаем артикул от пробелов в начале и конце
        String cleanedArticle = article != null ? article.trim() : "";

        try {
            // Этап 1: Если бренд не указан И мы не в режиме выбора бренда - ищем бренды
            if ((brand == null || brand.trim().isEmpty()) && !selectBrand) {
                List<BrandSupplier> availableBrands = findAvailableBrands(cleanedArticle);
                model.addAttribute("availableBrands", availableBrands);
                model.addAttribute("article", cleanedArticle);
                model.addAttribute("selectBrandMode", true);
                return "search-results";
            }

            // Этап 2: Если в режиме выбора бренда, но бренд еще не выбран - показываем выбор
            if (selectBrand && (brand == null || brand.trim().isEmpty())) {
                List<BrandSupplier> availableBrands = findAvailableBrands(cleanedArticle);
                model.addAttribute("availableBrands", availableBrands);
                model.addAttribute("article", cleanedArticle);
                model.addAttribute("selectBrandMode", true);
                return "search-results";
            }

            // Очищаем бренд от пробелов
            String cleanedBrand = brand != null ? brand.trim() : null;

            // Этап 3: Поиск с выбранным брендом
            String cacheKey = String.format("search_%s_%s", cleanedArticle, cleanedBrand != null ? cleanedBrand : "");
            Map<String, List<PartOfferDto>> offersBySupplier;

            if (refresh || session.getAttribute(cacheKey) == null) {
                List<PartOfferDto> allOffers = aggregationService.searchAllSuppliers(cleanedArticle, cleanedBrand);
                offersBySupplier = allOffers.stream()
                        .collect(Collectors.groupingBy(PartOfferDto::getSupplierName));
                session.setAttribute(cacheKey, offersBySupplier);
            } else {
                offersBySupplier = (Map<String, List<PartOfferDto>>) session.getAttribute(cacheKey);
            }

            List<PartOfferDto> allOffers = offersBySupplier.values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

// ЛОГИРУЕМ ВСЕ ПРЕДЛОЖЕНИЯ ДО СОРТИРОВКИ
            log.info("=== ALL OFFERS BEFORE SORTING ===");
            allOffers.forEach(offer ->
                    log.info("{}: Article='{}', Price={}, Brand='{}'",
                            offer.getSupplierName(), offer.getOriginalArticle(), offer.getPrice(), offer.getBrand())
            );

            List<PartOfferDto> exactMatches = allOffers.stream()
                    .filter(offer -> isExactMatch(offer, cleanedArticle))
                    .collect(Collectors.toList());

            List<PartOfferDto> otherOffers = allOffers.stream()
                    .filter(offer -> !isExactMatch(offer, cleanedArticle))
                    .collect(Collectors.toList());

// ЛОГИРУЕМ РАСПРЕДЕЛЕНИЕ ПО ГРУППАМ
            log.info("Distribution - Exact matches: {}, Other offers: {}", exactMatches.size(), otherOffers.size());

// СОРТИРУЕМ ОБЕ ГРУППЫ ПО ЦЕНЕ
            exactMatches.sort(Comparator.comparing(PartOfferDto::getPrice));
            otherOffers.sort(Comparator.comparing(PartOfferDto::getPrice));

// ЛОГИРУЕМ ОТСОРТИРОВАННЫЕ РЕЗУЛЬТАТЫ
            log.info("=== SORTED EXACT MATCHES ===");
            exactMatches.forEach(offer ->
                    log.info("{}: Price={}, Article='{}'",
                            offer.getSupplierName(), offer.getPrice(), offer.getOriginalArticle())
            );

            log.info("=== SORTED OTHER OFFERS ===");
            otherOffers.forEach(offer ->
                    log.info("{}: Price={}, Article='{}'",
                            offer.getSupplierName(), offer.getPrice(), offer.getOriginalArticle())
            );

            List<String> supplierNames = supplierServices.stream()
                    .filter(SupplierService::isAvailable)
                    .map(SupplierService::getSupplierName)
                    .collect(Collectors.toList());

            model.addAttribute("article", cleanedArticle);
            model.addAttribute("brand", cleanedBrand);
            model.addAttribute("exactMatches", exactMatches);
            model.addAttribute("otherOffers", otherOffers);
            model.addAttribute("supplierNames", supplierNames);
            model.addAttribute("totalOffersCount", allOffers.size());
            model.addAttribute("selectBrandMode", false);

        } catch (Exception e) {
            log.error("Ошибка при поиске: {}", e.getMessage(), e);
            model.addAttribute("error", "Ошибка при поиске: " + e.getMessage());
            model.addAttribute("exactMatches", List.of());
            model.addAttribute("otherOffers", List.of());
            model.addAttribute("supplierNames", List.of());
            model.addAttribute("totalOffersCount", 0);
            model.addAttribute("selectBrandMode", false);
        }

        return "search-results";
    }

    private List<BrandSupplier> findAvailableBrands(String article) {
        Map<String, String> brandToSupplierMap = new HashMap<>();

        for (SupplierService supplier : supplierServices) {
            if (supplier.isAvailable()) {
                try {
                    log.info("Поиск брендов в поставщике: {}", supplier.getSupplierName());

                    if (supplier instanceof TmtrService) {
                        // Для TMTR используем специальный метод getBrands
                        TmtrService tmtrService = (TmtrService) supplier;
                        List<String> supplierBrands = tmtrService.getBrands(article);
                        log.info("Поставщик TMTR вернул {} брендов", supplierBrands.size());

                        for (String brand : supplierBrands) {
                            if (brand != null && !brand.trim().isEmpty()) {
                                String cleanBrand = brand.trim();
                                // Сохраняем бренд и поставщика
                                brandToSupplierMap.put(cleanBrand, supplier.getSupplierName());
                            }
                        }
                    } else {
                        // Для других поставщиков используем старый метод
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
            }
        }

        // Преобразуем Map в List объектов BrandSupplier
        List<BrandSupplier> result = brandToSupplierMap.entrySet().stream()
                .map(entry -> new BrandSupplier(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(BrandSupplier::getBrand))
                .collect(Collectors.toList());

        log.info("Всего найдено брендов: {}", result.size());
        return result;
    }

    private boolean isExactMatch(PartOfferDto offer, String searchedArticle) {
        if (offer.getOriginalArticle() == null || searchedArticle == null) {
            return false;
        }

        // Нормализуем оба артикула
        String offerArt = normalizeArticle(offer.getOriginalArticle());
        String searchedArt = normalizeArticle(searchedArticle);

        // 1. Прямое сравнение
        if (offerArt.equals(searchedArt)) {
            return true;
        }

        // 2. Сравнение только цифр (для случаев типа 05120201 vs 051.202-01)
        String digitsOnlyOffer = offerArt.replaceAll("[^0-9]", "");
        String digitsOnlySearched = searchedArt.replaceAll("[^0-9]", "");

        if (digitsOnlyOffer.equals(digitsOnlySearched) && !digitsOnlyOffer.isEmpty()) {
            log.debug("Exact match by digits: '{}' -> '{}'", offerArt, digitsOnlyOffer);
            return true;
        }

        return false;
    }

    private String normalizeArticle(String article) {
        if (article == null) {
            return "";
        }
        return article.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

}
