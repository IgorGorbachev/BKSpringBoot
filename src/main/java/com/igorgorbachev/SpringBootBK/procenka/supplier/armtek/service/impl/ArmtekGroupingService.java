package com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.service.impl;

import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.dto.Warehouse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArmtekGroupingService {

    private static final DateTimeFormatter ARMTEK_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // Сделаем компаратор нестатическим или вынесем в отдельный метод
    private Comparator<PartOfferDto> createPriceDeliveryComparator() {
        return Comparator.comparing((PartOfferDto offer) ->
                        offer.getPrice() != null ? offer.getPrice() : Double.MAX_VALUE)
                .thenComparing(this::getEarliestDeliveryDateFromOffer);
    }

    public List<PartOfferDto> groupArmtekOffers(List<PartOfferDto> rawOffers) {
        log.info("=== ARMTEK GROUPING STARTED: {} offers ===", rawOffers.size());

        if (rawOffers.isEmpty()) {
            return rawOffers;
        }

        Map<String, List<PartOfferDto>> grouped = rawOffers.stream()
                .filter(offer -> offer.getBrand() != null && offer.getOriginalArticle() != null)
                .collect(Collectors.groupingBy(offer ->
                        offer.getBrand() + "|" + offer.getOriginalArticle()));

        log.info("Found {} unique brand+article combinations", grouped.size());

        List<PartOfferDto> result = grouped.entrySet().stream()
                .map(this::createGroupedOffer)
                .filter(Objects::nonNull)
                .sorted(createPriceDeliveryComparator()) // используем метод вместо статической переменной
                .collect(Collectors.toList());

        logGroupingResult(rawOffers, result);
        return result;
    }

    public List<PartOfferDto> filterExactMatches(List<PartOfferDto> offers, String article, String brand) {
        log.info("=== FILTERING ARMTEK FOR EXACT MATCHES ===");

        List<PartOfferDto> filteredOffers = offers.stream()
                .filter(offer -> isExactMatch(offer, article, brand))
                .collect(Collectors.toList());

        log.info("Filtered {} Armtek offers for exact matches", filteredOffers.size());
        return groupArmtekOffers(filteredOffers);
    }

    // Сделаем метод публичным для использования в контроллере
    public LocalDateTime getEarliestDeliveryDateFromOffer(PartOfferDto offer) {
        return Optional.ofNullable(offer.getWarehouses())
                .flatMap(warehouses -> warehouses.stream().findFirst())
                .map(Warehouse::getShipmentDate)
                .map(this::parseArmtekDeliveryDate)
                .orElse(LocalDateTime.MAX);
    }

    private PartOfferDto createGroupedOffer(Map.Entry<String, List<PartOfferDto>> entry) {
        List<PartOfferDto> group = entry.getValue();
        if (group.isEmpty()) return null;

        PartOfferDto baseOffer = group.get(0);
        PartOfferDto groupedOffer = createBaseGroupedOffer(baseOffer, group.size());

        List<Warehouse> allWarehouses = group.stream()
                .map(PartOfferDto::getWarehouses)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .sorted(createWarehouseComparator())
                .collect(Collectors.toList());

        groupedOffer.setWarehouses(allWarehouses);
        setBestWarehouseFields(groupedOffer, allWarehouses);

        return groupedOffer;
    }

    private PartOfferDto createBaseGroupedOffer(PartOfferDto baseOffer, int groupSize) {
        PartOfferDto groupedOffer = new PartOfferDto();

        // Основные поля
        groupedOffer.setSupplierName(baseOffer.getSupplierName());
        groupedOffer.setBrand(baseOffer.getBrand());
        groupedOffer.setOriginalArticle(baseOffer.getOriginalArticle());
        groupedOffer.setWarranty(baseOffer.getWarranty());
        groupedOffer.setDeliveryDays(baseOffer.getDeliveryDays());

        // Поля возвратности
        groupedOffer.setIsReturnable(baseOffer.getIsReturnable());
        groupedOffer.setReturnInfo(baseOffer.getReturnInfo());

        // Название с количеством вариантов
        String baseName = baseOffer.getPartName() != null ? baseOffer.getPartName() : "";
        groupedOffer.setPartName(groupSize > 1 ?
                baseName + " (" + groupSize + " варианта)" : baseName);

        return groupedOffer;
    }

    private Comparator<Warehouse> createWarehouseComparator() {
        return Comparator.comparing((Warehouse warehouse) ->
                        parseArmtekDeliveryDate(warehouse.getShipmentDate()))
                .thenComparing(warehouse ->
                        warehouse.getPrice() != null ? warehouse.getPrice() : Double.MAX_VALUE);
    }

    private void setBestWarehouseFields(PartOfferDto groupedOffer, List<Warehouse> warehouses) {
        if (!warehouses.isEmpty()) {
            Warehouse bestWarehouse = warehouses.get(0);
            groupedOffer.setPrice(bestWarehouse.getPrice());
            groupedOffer.setQuantityAvailable(bestWarehouse.getStock());
            groupedOffer.setWarehouse(bestWarehouse.getCode());
            groupedOffer.setDeliveryDays(calculateDeliveryDays(bestWarehouse));
        }
    }

    private boolean isExactMatch(PartOfferDto offer, String requestedArticle, String requestedBrand) {
        if (offer == null) return false;

        boolean articleMatches = offer.getOriginalArticle() != null &&
                offer.getOriginalArticle().equalsIgnoreCase(requestedArticle);

        boolean brandMatches = requestedBrand == null || requestedBrand.trim().isEmpty() ||
                (offer.getBrand() != null && offer.getBrand().equalsIgnoreCase(requestedBrand));

        return articleMatches && brandMatches;
    }

    private LocalDateTime parseArmtekDeliveryDate(String shipmentDate) {
        if (shipmentDate == null || shipmentDate.trim().isEmpty()) {
            return LocalDateTime.MAX;
        }

        try {
            if (shipmentDate.length() == 14 && shipmentDate.matches("\\d+")) {
                return LocalDateTime.parse(shipmentDate, ARMTEK_DATE_FORMATTER);
            }
            return LocalDateTime.parse(shipmentDate);
        } catch (Exception e) {
            log.debug("Failed to parse delivery date: {}", shipmentDate);
            return LocalDateTime.MAX;
        }
    }

    private Integer calculateDeliveryDays(Warehouse warehouse) {
        return Optional.ofNullable(warehouse)
                .map(Warehouse::getShipmentDate)
                .map(this::parseArmtekDeliveryDate)
                .map(shipmentDate -> {
                    long hours = java.time.Duration.between(LocalDateTime.now(), shipmentDate).toHours();
                    long days = (hours + 23) / 24;
                    return Math.max(1, (int) days);
                })
                .orElse(1);
    }

    private void logGroupingResult(List<PartOfferDto> rawOffers, List<PartOfferDto> result) {
        log.info("=== ARMTEK GROUPING COMPLETED: {} raw -> {} grouped ===",
                rawOffers.size(), result.size());

        result.stream().limit(5).forEach(offer -> {
            String deliveryInfo = Optional.ofNullable(offer.getWarehouses())
                    .filter(list -> !list.isEmpty())
                    .map(list -> list.get(0).getFormattedDelivery())
                    .orElse("Нет данных");

            log.info("Grouped offer: {} {}, delivery={}, price={}",
                    offer.getBrand(), offer.getOriginalArticle(), deliveryInfo, offer.getPrice());
        });
    }
}