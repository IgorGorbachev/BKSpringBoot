package com.igorgorbachev.SpringBootBK.procenka.controller;


import com.igorgorbachev.SpringBootBK.config.util.DeliveryUtils;
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

import java.util.ArrayList;
import java.util.Collections;
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
    private final DeliveryUtils deliveryUtils;

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

//    @GetMapping("/search")
//    public String searchParts(
//            @RequestParam String article,
//            @RequestParam(required = false) String brand,
//            @RequestParam(required = false, defaultValue = "false") boolean refresh,
//            @RequestParam(required = false, defaultValue = "false") boolean selectBrand,
//            HttpSession session,
//            Model model) {
//
//        log.info("=== SEARCH CONTROLLER CALLED ===");
//        log.info("Article: '{}', Brand: '{}', Refresh: {}, SelectBrand: {}",
//                article, brand, refresh, selectBrand);
//
//        // Очищаем артикул от пробелов в начале и конце
//        String cleanedArticle = article != null ? article.trim() : "";
//
//        try {
//            // Этап 1: Если бренд не указан И мы не в режиме выбора бренда - ищем бренды
//            if ((brand == null || brand.trim().isEmpty()) && !selectBrand) {
//                List<BrandSupplier> availableBrands = findAvailableBrands(cleanedArticle);
//                model.addAttribute("availableBrands", availableBrands);
//                model.addAttribute("article", cleanedArticle);
//                model.addAttribute("selectBrandMode", true);
//                return "search-results";
//            }
//
//            // Этап 2: Если в режиме выбора бренда, но бренд еще не выбран - показываем выбор
//            if (selectBrand && (brand == null || brand.trim().isEmpty())) {
//                List<BrandSupplier> availableBrands = findAvailableBrands(cleanedArticle);
//                model.addAttribute("availableBrands", availableBrands);
//                model.addAttribute("article", cleanedArticle);
//                model.addAttribute("selectBrandMode", true);
//                return "search-results";
//            }
//
//            // Очищаем бренд от пробелов
//            String cleanedBrand = brand != null ? brand.trim() : null;
//
//            // Этап 3: Поиск с выбранным брендом
//            String cacheKey = String.format("search_%s_%s", cleanedArticle, cleanedBrand != null ? cleanedBrand : "");
//            Map<String, List<PartOfferDto>> offersBySupplier;
//
//            if (refresh || session.getAttribute(cacheKey) == null) {
//                List<PartOfferDto> allOffers = aggregationService.searchAllSuppliers(cleanedArticle, cleanedBrand);
//                offersBySupplier = allOffers.stream()
//                        .collect(Collectors.groupingBy(PartOfferDto::getSupplierName));
//                session.setAttribute(cacheKey, offersBySupplier);
//            } else {
//                offersBySupplier = (Map<String, List<PartOfferDto>>) session.getAttribute(cacheKey);
//            }
//
//            List<PartOfferDto> allOffers = offersBySupplier.values().stream()
//                    .flatMap(List::stream)
//                    .collect(Collectors.toList());
//
//// ЛОГИРУЕМ ВСЕ ПРЕДЛОЖЕНИЯ ДО СОРТИРОВКИ
//            log.info("=== ALL OFFERS BEFORE SORTING ===");
//            allOffers.forEach(offer ->
//                    log.info("{}: Article='{}', Price={}, Brand='{}'",
//                            offer.getSupplierName(), offer.getOriginalArticle(), offer.getPrice(), offer.getBrand())
//            );
//
//            List<PartOfferDto> exactMatches = allOffers.stream()
//                    .filter(offer -> isExactMatch(offer, cleanedArticle))
//                    .collect(Collectors.toList());
//
//            List<PartOfferDto> otherOffers = allOffers.stream()
//                    .filter(offer -> !isExactMatch(offer, cleanedArticle))
//                    .collect(Collectors.toList());
//
//// ЛОГИРУЕМ РАСПРЕДЕЛЕНИЕ ПО ГРУППАМ
//            log.info("Distribution - Exact matches: {}, Other offers: {}", exactMatches.size(), otherOffers.size());
//
//// СОРТИРУЕМ ОБЕ ГРУППЫ ПО ЦЕНЕ
//            exactMatches.sort(Comparator.comparing(PartOfferDto::getPrice));
//            otherOffers.sort(Comparator.comparing(PartOfferDto::getPrice));
//

    /// / ЛОГИРУЕМ ОТСОРТИРОВАННЫЕ РЕЗУЛЬТАТЫ
//            log.info("=== SORTED EXACT MATCHES ===");
//            exactMatches.forEach(offer ->
//                    log.info("{}: Price={}, Article='{}'",
//                            offer.getSupplierName(), offer.getPrice(), offer.getOriginalArticle())
//            );
//
//            log.info("=== SORTED OTHER OFFERS ===");
//            otherOffers.forEach(offer ->
//                    log.info("{}: Price={}, Article='{}'",
//                            offer.getSupplierName(), offer.getPrice(), offer.getOriginalArticle())
//            );
//
//            List<String> supplierNames = supplierServices.stream()
//                    .filter(SupplierService::isAvailable)
//                    .map(SupplierService::getSupplierName)
//                    .collect(Collectors.toList());
//
//            model.addAttribute("article", cleanedArticle);
//            model.addAttribute("brand", cleanedBrand);
//            model.addAttribute("exactMatches", exactMatches);
//            model.addAttribute("otherOffers", otherOffers);
//            model.addAttribute("supplierNames", supplierNames);
//            model.addAttribute("totalOffersCount", allOffers.size());
//            model.addAttribute("selectBrandMode", false);
//
//        } catch (Exception e) {
//            log.error("Ошибка при поиске: {}", e.getMessage(), e);
//            model.addAttribute("error", "Ошибка при поиске: " + e.getMessage());
//            model.addAttribute("exactMatches", List.of());
//            model.addAttribute("otherOffers", List.of());
//            model.addAttribute("supplierNames", List.of());
//            model.addAttribute("totalOffersCount", 0);
//            model.addAttribute("selectBrandMode", false);
//        }
//
//        return "search-results";
//    }


    private TmtrService getTmtrService() {
        return supplierServices.stream()
                .filter(service -> service instanceof TmtrService)
                .map(service -> (TmtrService) service)
                .findFirst()
                .orElse(null);
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

            String cleanedBrand = brand != null ? brand.trim() : null;

            // Этап 3: Поиск с выбранным брендом
            log.info("=== FORCING FRESH SEARCH - IGNORING CACHE ===");

            List<PartOfferDto> allOffers = aggregationService.searchAllSuppliers(cleanedArticle, cleanedBrand);
            log.info("aggregationService returned {} offers", allOffers.size());

            Map<String, List<PartOfferDto>> offersBySupplier = allOffers.stream()
                    .collect(Collectors.groupingBy(PartOfferDto::getSupplierName));

            // Получаем ОТФИЛЬТРОВАННЫЕ товары TMTR
            List<PartOfferDto> tmtrOffers = Collections.emptyList();
            TmtrService tmtrService = getTmtrService();
            if (tmtrService != null) {
                try {
                    tmtrOffers = tmtrService.getAllTmtrParts(cleanedArticle, cleanedBrand);
                    log.info("TMTR offers: {} items (in stock, sorted by price)", tmtrOffers.size());
                } catch (Exception e) {
                    log.error("Error getting TMTR offers: {}", e.getMessage());
                    tmtrOffers = offersBySupplier.getOrDefault("TMTR", Collections.emptyList())
                            .stream()
                            .sorted(Comparator.comparing(PartOfferDto::getPrice))
                            .collect(Collectors.toList());
                }
            } else {
                tmtrOffers = offersBySupplier.getOrDefault("TMTR", Collections.emptyList())
                        .stream()
                        .sorted(Comparator.comparing(PartOfferDto::getPrice))
                        .collect(Collectors.toList());
            }

            // ДОПОЛНИТЕЛЬНО: получаем оригиналы Forum-Auto
            List<PartOfferDto> forumAutoOriginals = getForumAutoOriginals(cleanedArticle, cleanedBrand);
            log.info("Forum-Auto originals: {} items", forumAutoOriginals.size());

            // ТОЧНЫЕ СОВПАДЕНИЯ - комбинируем
            List<PartOfferDto> exactMatches = new ArrayList<>();

            // 1. Точные совпадения из aggregationService (исключая Forum-Auto)
            List<PartOfferDto> exactFromAggregation = allOffers.stream()
                    .filter(offer -> !"Forum-Auto".equals(offer.getSupplierName()))
                    .filter(offer -> isExactBrandMatch(offer, cleanedBrand) &&
                            isExactArticleMatch(offer, cleanedArticle))
                    .sorted(Comparator.comparing(PartOfferDto::getPrice))
                    .collect(Collectors.toList());

            exactMatches.addAll(exactFromAggregation);

            // 2. Добавляем оригиналы Forum-Auto
            exactMatches.addAll(forumAutoOriginals);

            // 3. Сортируем по цене
            exactMatches.sort(Comparator.comparing(PartOfferDto::getPrice));

            log.info("Exact matches: {} items ({} from aggregation + {} Forum-Auto originals)",
                    exactMatches.size(), exactFromAggregation.size(), forumAutoOriginals.size());

            // Forum-Auto вкладка - аналоги из aggregationService (отсортированные)
            List<PartOfferDto> forumAutoOffers = offersBySupplier.getOrDefault("Forum-Auto", Collections.emptyList())
                    .stream()
                    .sorted(Comparator.comparing(PartOfferDto::getPrice))
                    .collect(Collectors.toList());

            List<PartOfferDto> favoritePartsOffers = offersBySupplier.getOrDefault("Favorite Parts", Collections.emptyList())
                    .stream()
                    .sorted(Comparator.comparing(PartOfferDto::getPrice))
                    .collect(Collectors.toList());

            List<PartOfferDto> armtekOffers = offersBySupplier.getOrDefault("Armtek", Collections.emptyList())
                    .stream()
                    .sorted(Comparator.comparing(PartOfferDto::getPrice))
                    .collect(Collectors.toList());

            // Обработанные аналоги
            List<PartOfferDto> processedOffers = Collections.emptyList();

            // Логируем распределение
            log.info("=== FINAL DISTRIBUTION ===");
            log.info("Exact matches: {}, Forum-Auto: {}, Favorite Parts: {}, Armtek: {}, TMTR: {}",
                    exactMatches.size(), forumAutoOffers.size(),
                    favoritePartsOffers.size(), armtekOffers.size(), tmtrOffers.size());

            List<String> supplierNames = supplierServices.stream()
                    .filter(SupplierService::isAvailable)
                    .map(SupplierService::getSupplierName)
                    .collect(Collectors.toList());

            model.addAttribute("article", cleanedArticle);
            model.addAttribute("brand", cleanedBrand);

            // Данные для вкладок
            model.addAttribute("exactMatches", exactMatches);
            model.addAttribute("forumAutoOffers", forumAutoOffers);
            model.addAttribute("favoritePartsOffers", favoritePartsOffers);
            model.addAttribute("armtekOffers", armtekOffers);
            model.addAttribute("tmtrOffers", tmtrOffers);
            model.addAttribute("processedOffers", processedOffers);

            model.addAttribute("supplierNames", supplierNames);
            model.addAttribute("totalOffersCount", allOffers.size());
            model.addAttribute("selectBrandMode", false);

        } catch (Exception e) {
            log.error("Ошибка при поиске: {}", e.getMessage(), e);
            model.addAttribute("error", "Ошибка при поиске: " + e.getMessage());
            model.addAttribute("exactMatches", List.of());
            model.addAttribute("forumAutoOffers", List.of());
            model.addAttribute("favoritePartsOffers", List.of());
            model.addAttribute("armtekOffers", List.of());
            model.addAttribute("tmtrOffers", List.of());
            model.addAttribute("processedOffers", List.of());
            model.addAttribute("supplierNames", List.of());
            model.addAttribute("totalOffersCount", 0);
            model.addAttribute("selectBrandMode", false);
        }

        model.addAttribute("deliveryUtils", deliveryUtils);
        return "search-results";
    }

    private List<PartOfferDto> getForumAutoOriginals(String article, String brand) {
        try {
            SupplierService forumAutoService = supplierServices.stream()
                    .filter(service -> "Forum-Auto".equals(service.getSupplierName()))
                    .findFirst()
                    .orElse(null);

            if (forumAutoService != null) {
                List<PartOfferDto> originals = forumAutoService.searchParts(article, brand);
                log.info("Forum-Auto searchParts returned {} originals", originals.size());

                // Логируем детали оригиналов
                if (!originals.isEmpty()) {
                    log.info("Forum-Auto originals details:");
                    originals.forEach(offer ->
                            log.info(" - Brand='{}', Article='{}', Price={}",
                                    offer.getBrand(), offer.getOriginalArticle(), offer.getPrice())
                    );
                }

                return originals;
            }
        } catch (Exception e) {
            log.error("Error getting Forum-Auto originals: {}", e.getMessage());
        }
        return Collections.emptyList();
    }





    /**
     * Проверяет точное совпадение по бренду
     */
    private boolean isExactBrandMatch(PartOfferDto offer, String searchedBrand) {
        if (searchedBrand == null || offer.getBrand() == null) {
            return false;
        }

        return normalizeBrand(offer.getBrand()).equals(normalizeBrand(searchedBrand));
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

//    private boolean isExactMatch(PartOfferDto offer, String searchedArticle) {
//        if (offer.getOriginalArticle() == null || searchedArticle == null) {
//            return false;
//        }
//
//        // Нормализуем оба артикула
//        String offerArt = normalizeArticle(offer.getOriginalArticle());
//        String searchedArt = normalizeArticle(searchedArticle);
//
//        // 1. Прямое сравнение
//        if (offerArt.equals(searchedArt)) {
//            return true;
//        }
//
//        // 2. Сравнение только цифр (для случаев типа 05120201 vs 051.202-01)
//        String digitsOnlyOffer = offerArt.replaceAll("[^0-9]", "");
//        String digitsOnlySearched = searchedArt.replaceAll("[^0-9]", "");
//
//        if (digitsOnlyOffer.equals(digitsOnlySearched) && !digitsOnlyOffer.isEmpty()) {
//            log.debug("Exact match by digits: '{}' -> '{}'", offerArt, digitsOnlyOffer);
//            return true;
//        }
//
//        return false;
//    }

    private boolean isExactMatch(PartOfferDto offer, String searchedArticle, String searchedBrand) {
        if (offer.getOriginalArticle() == null || searchedArticle == null) {
            return false;
        }

        // Нормализуем артикулы
        String offerArt = normalizeArticle(offer.getOriginalArticle());
        String searchedArt = normalizeArticle(searchedArticle);

        // Проверяем точное совпадение артикула
        boolean articleMatches = offerArt.equals(searchedArt) ||
                getDigitsOnly(offerArt).equals(getDigitsOnly(searchedArt));

        // Проверяем совпадение бренда (если бренд указан в запросе)
        boolean brandMatches = searchedBrand == null ||
                (offer.getBrand() != null &&
                        normalizeBrand(offer.getBrand()).equals(normalizeBrand(searchedBrand)));

        return articleMatches && brandMatches;
    }

    /**
     * Проверяет аналог - тот же артикул, но другой бренд
     */
    private boolean isAnalogueMatch(PartOfferDto offer, String searchedArticle, String searchedBrand) {
        if (offer.getOriginalArticle() == null || searchedArticle == null) {
            return false;
        }

        // Нормализуем артикулы
        String offerArt = normalizeArticle(offer.getOriginalArticle());
        String searchedArt = normalizeArticle(searchedArticle);

        // Проверяем точное совпадение артикула
        boolean articleMatches = offerArt.equals(searchedArt) ||
                getDigitsOnly(offerArt).equals(getDigitsOnly(searchedArt));

        // Проверяем, что бренд НЕ совпадает (это аналог)
        boolean brandDifferent = searchedBrand != null &&
                offer.getBrand() != null &&
                !normalizeBrand(offer.getBrand()).equals(normalizeBrand(searchedBrand));

        return articleMatches && brandDifferent;
    }

    /**
     * Обработка аналогов - здесь можно добавить специальную логику
     * Например: фильтрация по качеству, цене, наличию и т.д.
     */
    /**
     * Обработка аналогов - здесь можно добавить специальную логику
     */
    private List<PartOfferDto> processAnalogues(List<PartOfferDto> analogues, String searchedArticle, String searchedBrand) {
        // Пока возвращаем те же аналоги с той же сортировкой
        // Здесь можно добавить дополнительную логику обработки
        return analogues;
    }

    private String normalizeArticle(String article) {
        if (article == null) {
            return "";
        }
        return article.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private String normalizeBrand(String brand) {
        if (brand == null) {
            return "";
        }
        return brand.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private String getDigitsOnly(String str) {
        if (str == null) {
            return "";
        }
        return str.replaceAll("[^0-9]", "");
    }

    private boolean isExactArticleMatch(PartOfferDto offer, String searchedArticle) {
        if (searchedArticle == null || offer.getOriginalArticle() == null) {
            return false;
        }

        String offerArt = normalizeArticle(offer.getOriginalArticle());
        String searchedArt = normalizeArticle(searchedArticle);

        return offerArt.equals(searchedArt) ||
                getDigitsOnly(offerArt).equals(getDigitsOnly(searchedArt));
    }

    /**
     * Новая сортировка аналогов:
     * 1. Сначала возвратные с доставкой ≤ 2 дней (по возрастанию цены)
     * 2. Затем возвратные с доставкой > 2 дней (по возрастанию цены)
     * 3. Затем невозвратные с доставкой ≤ 2 дней (по возрастанию цены)
     * 4. Затем невозвратные с доставкой > 2 дней (по возрастанию цены)
     */
    private List<PartOfferDto> sortAnalogues(List<PartOfferDto> allOffers, String searchedBrand) {
        // Фильтруем аналоги (другие бренды)
        List<PartOfferDto> analogues = allOffers.stream()
                .filter(offer -> !isExactBrandMatch(offer, searchedBrand))
                .collect(Collectors.toList());

        // Разделяем на 4 группы
        List<PartOfferDto> returnableFastDelivery = analogues.stream()
                .filter(offer -> offer.getIsReturnable() != null && offer.getIsReturnable())
                .filter(offer -> deliveryUtils.hasFastDelivery(offer, 2))
                .sorted(Comparator.comparing(PartOfferDto::getPrice))
                .collect(Collectors.toList());

        List<PartOfferDto> returnableSlowDelivery = analogues.stream()
                .filter(offer -> offer.getIsReturnable() != null && offer.getIsReturnable())
                .filter(offer -> !deliveryUtils.hasFastDelivery(offer, 2))
                .sorted(Comparator.comparing(PartOfferDto::getPrice))
                .collect(Collectors.toList());

        List<PartOfferDto> nonReturnableFastDelivery = analogues.stream()
                .filter(offer -> offer.getIsReturnable() == null || !offer.getIsReturnable())
                .filter(offer -> deliveryUtils.hasFastDelivery(offer, 2))
                .sorted(Comparator.comparing(PartOfferDto::getPrice))
                .collect(Collectors.toList());

        List<PartOfferDto> nonReturnableSlowDelivery = analogues.stream()
                .filter(offer -> offer.getIsReturnable() == null || !offer.getIsReturnable())
                .filter(offer -> !deliveryUtils.hasFastDelivery(offer, 2))
                .sorted(Comparator.comparing(PartOfferDto::getPrice))
                .collect(Collectors.toList());

        // Объединяем в правильном порядке
        List<PartOfferDto> result = new ArrayList<>();
        result.addAll(returnableFastDelivery);
        result.addAll(returnableSlowDelivery);
        result.addAll(nonReturnableFastDelivery);
        result.addAll(nonReturnableSlowDelivery);

        log.info("Analogues sorting: {} returnable+fast, {} returnable+slow, {} non-returnable+fast, {} non-returnable+slow",
                returnableFastDelivery.size(), returnableSlowDelivery.size(),
                nonReturnableFastDelivery.size(), nonReturnableSlowDelivery.size());

        return result;
    }

    private boolean hasFastDelivery(PartOfferDto offer, int maxDays) {
        if (offer.getDeliveryDays() != null) {
            return offer.getDeliveryDays() <= maxDays;
        }

        // Если deliveryDays не указан, проверяем formattedDelivery
        if (offer.getFormattedDelivery() != null) {
            String delivery = offer.getFormattedDelivery().toLowerCase();
            // Проверяем наличие цифр 1-2 в описании доставки
            if (delivery.contains("1 дн") || delivery.contains("1дн") ||
                    delivery.contains("2 дн") || delivery.contains("2дн") ||
                    delivery.contains("сегодня") || delivery.contains("завтра")) {
                return true;
            }
        }

        // По умолчанию считаем доставку медленной
        return false;
    }




//    private String normalizeArticle(String article) {
//        if (article == null) {
//            return "";
//        }
//        return article.toLowerCase().replaceAll("[^a-z0-9]", "");
//    }

}
