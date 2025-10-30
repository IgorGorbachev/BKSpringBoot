package com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.service.impl;

import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.service.SupplierService;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.client.TmtrClient;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.converter.TmtrConverter;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.filter.TmtrFilter;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.model.TmtrBrand;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.model.TmtrGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.service.TmtrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmtrServiceImpl implements TmtrService, SupplierService {

    private final TmtrClient tmtrClient;
    private final TmtrFilter tmtrFilter;
    private final TmtrConverter tmtrConverter;

    private String lastRawResponse;

    @Override
    public String getSupplierName() {
        return "TMTR";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public List<String> getBrands(String article) {
        try {
            log.info("TMTR getting brands for article: {}", article);

            List<TmtrBrand> brands = tmtrClient.fetchBrands(article);

            String normalizedRequestedArticle = normalizeString(article);

            List<String> result = brands.stream()
                    .filter(brand -> brand != null && brand.getBrand() != null && !brand.getBrand().trim().isEmpty())
                    .filter(brand -> {
                        if (brand.getArticle() == null) return false;
                        String normalizedBrandArticle = normalizeString(brand.getArticle());
                        return normalizedBrandArticle.equals(normalizedRequestedArticle);
                    })
                    .map(TmtrBrand::getBrand)
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());

            log.info("TMTR found {} unique brands for article {}", result.size(), article);
            return result;

        } catch (Exception e) {
            log.error("Error getting brands from TMTR for article: {}. Error: {}", article, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<PartOfferDto> searchParts(String article, String brand) {
        try {
            log.info("TMTR searching parts - Article: {}, Brand: {}", article, brand);

            if (brand == null || brand.trim().isEmpty()) {
                log.debug("TMTR: brand is empty, returning empty list");
                return Collections.emptyList();
            }

            List<TmtrGoods> allGoods = tmtrClient.fetchGoods(article, brand);
            List<TmtrGoods> filteredGoods = tmtrFilter.filterOriginals(allGoods, article, brand);
            List<PartOfferDto> result = tmtrConverter.toPartOfferDtos(filteredGoods);

            log.info("TMTR parts search completed: {} offers found", result.size());
            return result;

        } catch (Exception e) {
            log.error("Error searching in TMTR for article: {}, brand: {}. Error: {}",
                    article, brand, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<PartOfferDto> getAllTmtrParts(String article, String brand) {
        try {
            log.info("TMTR getting all parts - Article: {}, Brand: {}", article, brand);

            if (brand == null || brand.trim().isEmpty()) {
                return Collections.emptyList();
            }

            List<TmtrGoods> allGoods = tmtrClient.fetchGoods(article, brand);
            List<TmtrGoods> filteredGoods = tmtrFilter.filterAllInStock(allGoods);
            List<PartOfferDto> result = tmtrConverter.toPartOfferDtos(filteredGoods);

            log.info("TMTR all parts completed: {} offers found", result.size());
            return result;

        } catch (Exception e) {
            log.error("Error getting all TMTR parts for article: {}, brand: {}. Error: {}",
                    article, brand, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public String getLastRawResponse() {
        return this.lastRawResponse;
    }

    private String normalizeString(String str) {
        if (str == null) {
            return "";
        }
        return str.replaceAll("\\s+", "").toLowerCase();
    }
}
