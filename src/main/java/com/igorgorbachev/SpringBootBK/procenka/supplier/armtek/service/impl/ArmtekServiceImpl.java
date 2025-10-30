package com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.service.impl;

import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.service.SupplierService;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.client.ArmtekClient;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.converter.ArmtekConverter;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.filter.ArmtekFilter;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.model.ArmtekGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.model.ArmtekDetailedResult;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.service.ArmtekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;



@Slf4j
@Service
@RequiredArgsConstructor
public class ArmtekServiceImpl implements ArmtekService, SupplierService {

    private final ArmtekClient armtekClient;
    private final ArmtekFilter armtekFilter;
    private final ArmtekConverter armtekConverter;

    private List<ArmtekGoods> lastRawGoods = Collections.emptyList();

    @Override
    public String getSupplierName() {
        return "Armtek";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    // Реализация из SupplierService
    @Override
    public List<PartOfferDto> searchParts(String article, String brand) {
        try {
            ArmtekDetailedResult result = searchPartsDetailed(article, brand);
            return armtekConverter.toPartOfferDtos(result.getFilteredGoods());
        } catch (Exception e) {
            log.error("Error searching in Armtek for article: {}, brand: {}. Error: {}",
                    article, brand, e.getMessage());
            return Collections.emptyList();
        }
    }

    // Реализация из ArmtekService
    @Override
    public ArmtekDetailedResult searchPartsDetailed(String article, String brand) {
        try {
            log.info("=== ARMTEK SEARCH STARTED ===");

            List<ArmtekGoods> allGoods = armtekClient.fetchGoods(article, brand);
            // Сохраняем сырые данные для последующего использования
            this.lastRawGoods = allGoods;

            List<ArmtekGoods> filteredGoods = armtekFilter.filterOriginals(allGoods, article, brand);

            ArmtekDetailedResult result = new ArmtekDetailedResult(filteredGoods, allGoods, article, brand);

            log.info("=== ARMTEK SEARCH COMPLETED ===");
            log.info("Raw results: {}, Filtered results: {}",
                    result.getTotalRawResults(), result.getTotalFilteredResults());

            return result;

        } catch (Exception e) {
            log.error("Error searching Armtek parts: {}", e.getMessage(), e);
            return new ArmtekDetailedResult(Collections.emptyList(), Collections.emptyList(), article, brand);
        }
    }

    @Override
    public List<ArmtekGoods> searchArmtekGoods(String article, String brand) {
        ArmtekDetailedResult result = searchPartsDetailed(article, brand);
        return result.getFilteredGoods();
    }

    @Override
    public Mono<ArmtekDetailedResult> searchPartsReactive(String article, String brand) {
        return Mono.fromCallable(() -> searchPartsDetailed(article, brand));
    }

    @Override
    public List<ArmtekGoods> getLastRawGoods() {
        return lastRawGoods;
    }
}