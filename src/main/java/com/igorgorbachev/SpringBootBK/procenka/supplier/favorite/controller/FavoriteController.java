package com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.controller;

import com.igorgorbachev.SpringBootBK.exception.FavoritePartsException;
import com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.model.FavoritePartsGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.service.impl.FavoritePartsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.List;

@Slf4j
@Controller
@RequestMapping("/favorite-parts")
public class FavoriteController {
    private final FavoritePartsServiceImpl favoritePartsService;

    public FavoriteController(FavoritePartsServiceImpl favoritePartsService) {
        this.favoritePartsService = favoritePartsService;
    }

    /**
     * Страница поиска - сначала ищем бренды
     */
    @GetMapping("/search")
    public String searchPage(
            @RequestParam(required = false) String number,
            Model model) {

        if (number != null && !number.trim().isEmpty()) {
            // Этап 1: Ищем бренды по артикулу
            List<String> brands = favoritePartsService.findBrandsByArticle(number);
            model.addAttribute("brands", brands);
            model.addAttribute("searchNumber", number);
        }

        return "favorite-parts/search";
    }

    @GetMapping("/results")
    public String searchResults(
            @RequestParam String number,
            @RequestParam String brand,
            @RequestParam(required = false, defaultValue = "true") Boolean analogues,
            Model model) {

        log.info("=== НАЧАЛО searchResults ===");
        log.info("Параметры: number={}, brand={}, analogues={}", number, brand, analogues);

        try {
            List<FavoritePartsGoods> goods;

            if (Boolean.TRUE.equals(analogues)) {
                // Используем оптимизированный метод без дополнительных запросов
                goods = favoritePartsService.getPriceWithAnaloguesOptimized(number, brand);
            } else {
                // Без аналогов
                List<FavoritePartsGoods> basicGoods = favoritePartsService.getPrice(number, brand, false, true);
                // Сортируем склады основного товара
                if (basicGoods != null) {
                    basicGoods.forEach(good -> {
                        if (good.getWarehouses() != null) {
                            // Используем метод сортировки из сервиса
                            favoritePartsService.sortWarehouses(good.getWarehouses());
                        }
                    });
                }
                goods = basicGoods;
            }

            log.info("Получено товаров: {}", goods != null ? goods.size() : 0);
            if (goods != null && !goods.isEmpty()) {
                int totalAnalogues = goods.stream()
                        .mapToInt(g -> g.getAnalogues() != null ? g.getAnalogues().size() : 0)
                        .sum();
                log.info("Всего аналогов после фильтрации: {}", totalAnalogues);
            }

            model.addAttribute("goods", goods);
            model.addAttribute("searchNumber", number);
            model.addAttribute("searchBrand", brand);
            model.addAttribute("searchAnalogues", analogues);

        } catch (FavoritePartsException e) {
            log.error("Ошибка в searchResults: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
        }

        log.info("=== КОНЕЦ searchResults ===");
        return "favorite-parts/results";
    }

    // API методы
    @GetMapping("/api/brands")
    public ResponseEntity<?> getBrands(@RequestParam String number) {
        try {
            List<String> brands = favoritePartsService.findBrandsByArticle(number);
            return ResponseEntity.ok(brands);
        } catch (FavoritePartsException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/api/search")
    public ResponseEntity<?> searchWithAnalogues(
            @RequestParam String number,
            @RequestParam String brand) {
        try {
            List<FavoritePartsGoods> goods = favoritePartsService.getPriceWithAnalogues(number, brand);
            return ResponseEntity.ok(goods);
        } catch (FavoritePartsException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }


    // DTO для ошибок
    public static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}
