package com.igorgorbachev.SpringBootBK.procenka.service.impl;

import com.igorgorbachev.SpringBootBK.procenka.dto.BrandSupplier;
import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.service.SupplierService;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.service.TmtrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.igorgorbachev.SpringBootBK.procenka.dto.SearchResult;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchOrchestrationService {

    private final SupplierAggregationService aggregationService;
    private final List<SupplierService> supplierServices;
    private final TmtrService tmtrService;
    private final ResultProcessingService resultProcessingService;

    public SearchResult search(String article, String brand, boolean refresh) {
        log.info("Starting search orchestration: article='{}', brand='{}'", article, brand);

        String cleanedArticle = cleanInput(article);
        String cleanedBrand = cleanInput(brand);

        List<PartOfferDto> allOffers = aggregationService.searchAllSuppliers(cleanedArticle, cleanedBrand);

        return resultProcessingService.processResults(allOffers, cleanedArticle, cleanedBrand);
    }

    public List<BrandSupplier> findAvailableBrands(String article) {
        String cleanedArticle = cleanInput(article);
        return aggregationService.findAvailableBrands(cleanedArticle);
    }

    public boolean shouldShowBrandSelection(String brand, boolean selectBrand) {
        return (brand == null || brand.trim().isEmpty()) && !selectBrand;
    }

    private String cleanInput(String input) {
        return input != null ? input.trim() : "";
    }
}
