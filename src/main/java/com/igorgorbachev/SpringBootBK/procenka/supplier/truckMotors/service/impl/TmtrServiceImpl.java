package com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.service.impl;

import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.service.SupplierService;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.client.TmtrClient;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.converter.TmtrConverter;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.filter.TmtrFilter;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.model.TmtrBrand;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.model.TmtrGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.service.TmtrService;
import com.igorgorbachev.SpringBootBK.procenka.util.StringNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmtrServiceImpl implements TmtrService, SupplierService {

    private final TmtrClient tmtrClient;
    private final TmtrFilter tmtrFilter;
    private final TmtrConverter tmtrConverter;

    private String lastRawResponse;
    private final ConcurrentHashMap<String, Long> brandRequestCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 минут

    @Override
    public String getSupplierName() {
        return "TMTR";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    @Cacheable(value = "tmtrBrands", key = "#article", unless = "#result.isEmpty()")
    public List<String> getBrands(String article) {
        try {

            if (article == null || article.trim().isEmpty()) {
                log.warn("TMTR: empty article provided for brand search");
                return Collections.emptyList();
            }

            // Проверка частых запросов
            if (isFrequentRequest(article)) {
                log.warn("TMTR: frequent brand request for article: {}", article);
                return Collections.emptyList();
            }

            List<TmtrBrand> brands = tmtrClient.fetchBrands(article);
            String normalizedRequestedArticle = StringNormalizer.normalizeArticle(article);

            List<String> result = brands.stream()
                    .filter(brand -> brand != null && brand.getBrand() != null && !brand.getBrand().trim().isEmpty())
                    .filter(brand -> {
                        if (brand.getArticle() == null) return false;
                        String normalizedBrandArticle = StringNormalizer.normalizeArticle(brand.getArticle());
                        return normalizedBrandArticle.equals(normalizedRequestedArticle);
                    })
                    .map(TmtrBrand::getBrand)
                    .map(String::trim)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            return result;

        } catch (Exception e) {
            log.error("Error getting brands from TMTR for article: {}. Error: {}", article, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<PartOfferDto> searchParts(String article, String brand) {
        try {

            if (brand == null || brand.trim().isEmpty()) {
                log.debug("TMTR: brand is empty, returning empty list");
                return Collections.emptyList();
            }

            if (article == null || article.trim().isEmpty()) {
                log.warn("TMTR: empty article provided");
                return Collections.emptyList();
            }

            List<TmtrGoods> allGoods = tmtrClient.fetchGoods(article, brand);
            List<TmtrGoods> filteredGoods = tmtrFilter.filterOriginals(allGoods, article, brand);
            List<PartOfferDto> result = tmtrConverter.toPartOfferDtos(filteredGoods);

            return result;

        } catch (Exception e) {
            log.error("Error searching in TMTR for article: {}, brand: {}. Error: {}",
                    article, brand, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<PartOfferDto> getAllTmtrParts(String article, String brand) {
        try {


            if (brand == null || brand.trim().isEmpty()) {
                log.debug("TMTR: brand is empty for all parts search");
                return Collections.emptyList();
            }

            List<TmtrGoods> allGoods = tmtrClient.fetchGoods(article, brand);
            List<TmtrGoods> filteredGoods = tmtrFilter.filterAllInStock(allGoods);
            List<PartOfferDto> result = tmtrConverter.toPartOfferDtos(filteredGoods);

            return result;

        } catch (Exception e) {
            log.error("Error getting all TMTR parts for article: {}, brand: {}. Error: {}",
                    article, brand, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<PartOfferDto> searchWithCustomFilter(String article, String brand, boolean exactMatch) {
        try {

            if (brand == null || brand.trim().isEmpty()) {
                return Collections.emptyList();
            }

            List<TmtrGoods> allGoods = tmtrClient.fetchGoods(article, brand);
            List<TmtrGoods> filteredGoods = tmtrFilter.filterWithCustomRules(allGoods, article, brand, exactMatch);
            List<PartOfferDto> result = tmtrConverter.toPartOfferDtos(filteredGoods);

            return result;

        } catch (Exception e) {
            log.error("Error in custom TMTR search for article: {}, brand: {}. Error: {}",
                    article, brand, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public String getLastRawResponse() {
        return this.lastRawResponse;
    }

    public void clearBrandCache() {
        brandRequestCache.clear();
    }

    @Scheduled(fixedRate = CACHE_TTL_MS)
    public void cleanupCache() {
        long currentTime = System.currentTimeMillis();
        brandRequestCache.entrySet().removeIf(entry ->
                currentTime - entry.getValue() > CACHE_TTL_MS);
    }

    private boolean isFrequentRequest(String article) {
        long currentTime = System.currentTimeMillis();
        String key = StringNormalizer.normalizeArticle(article);

        Long lastRequest = brandRequestCache.get(key);
        if (lastRequest == null) {
            brandRequestCache.put(key, currentTime);
            return false;
        }

        // Защита от частых запросов (не чаще чем раз в 10 секунд)
        if (currentTime - lastRequest < 10000) {
            return true;
        }

        brandRequestCache.put(key, currentTime);
        return false;
    }
}