package com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.service.impl;

import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.dto.Warehouse;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.service.ArmtekDataProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    private final ArmtekDataProcessor dataProcessor;

    public List<PartOfferDto> groupArmtekOffers(List<PartOfferDto> rawOffers) {
        if (rawOffers.isEmpty()) {
            return rawOffers;
        }

        Map<String, List<PartOfferDto>> grouped = rawOffers.stream()
                .filter(offer -> offer.getBrand() != null && offer.getOriginalArticle() != null)
                .collect(Collectors.groupingBy(offer ->
                        offer.getBrand() + "|" + offer.getOriginalArticle()));


        List<PartOfferDto> result = grouped.entrySet().stream()
                .map(this::createGroupedOffer)
                .filter(Objects::nonNull)
                .sorted(createPriorityComparator())
                .collect(Collectors.toList());

        return result;
    }

    public List<PartOfferDto> filterExactMatches(List<PartOfferDto> offers, String article, String brand) {

        List<PartOfferDto> filteredOffers = offers.stream()
                .filter(offer -> isExactMatch(offer, article, brand))
                .collect(Collectors.toList());

        return groupArmtekOffers(filteredOffers);
    }

    /**
     * НОВАЯ ЛОГИКА СОРТИРОВКИ:
     * 1. Свои склады (MOV), доставка 0-1 день (по цене)
     * 2. Свои склады (MOV), доставка >1 дня (по цене)
     * 3. Чужие склады (по цене)
     */
    private Comparator<PartOfferDto> createPriorityComparator() {
        return (offer1, offer2) -> {
            int priority1 = calculatePriority(offer1);
            int priority2 = calculatePriority(offer2);

            // Сначала сравниваем по приоритету группы
            if (priority1 != priority2) {
                return Integer.compare(priority1, priority2);
            }

            // Внутри группы сортируем по цене
            Double price1 = offer1.getPrice() != null ? offer1.getPrice() : Double.MAX_VALUE;
            Double price2 = offer2.getPrice() != null ? offer2.getPrice() : Double.MAX_VALUE;

            return Double.compare(price1, price2);
        };
    }

    /**
     * Расчет приоритета:
     * 1 - MOV склады, доставка 0-1 день
     * 2 - MOV склады, доставка >1 дня
     * 3 - другие склады
     */
    private int calculatePriority(PartOfferDto offer) {
        boolean hasMovWarehouses = hasMovWarehouses(offer);
        boolean hasFastDelivery = hasFastDelivery(offer);

        if (hasMovWarehouses && hasFastDelivery) {
            return 1; // Высший приоритет
        } else if (hasMovWarehouses) {
            return 2; // Средний приоритет
        } else {
            return 3; // Низший приоритет
        }
    }

    /**
     * Проверяет, есть ли у оффера MOV склады (свои)
     */
    private boolean hasMovWarehouses(PartOfferDto offer) {
        if (offer.getWarehouses() == null || offer.getWarehouses().isEmpty()) {
            return false;
        }

        return offer.getWarehouses().stream()
                .anyMatch(warehouse -> isMovWarehouse(warehouse));
    }

    /**
     * Проверяет, является ли склад MOV складом (своим)
     */
    private boolean isMovWarehouse(Warehouse warehouse) {
        return warehouse != null &&
                warehouse.getCode() != null &&
                warehouse.getCode().toUpperCase().startsWith("MOV");
    }

    /**
     * Проверяет, есть ли быстрая доставка (0-1 день) на MOV складах
     */
    private boolean hasFastDelivery(PartOfferDto offer) {
        if (offer.getWarehouses() == null || offer.getWarehouses().isEmpty()) {
            return false;
        }

        // Ищем минимальное количество дней доставки среди MOV складов
        Optional<Integer> minDeliveryDays = offer.getWarehouses().stream()
                .filter(this::isMovWarehouse) // только MOV склады
                .map(Warehouse::getDeliveryDays)
                .filter(Objects::nonNull)
                .min(Integer::compareTo);

        return minDeliveryDays.isPresent() && minDeliveryDays.get() <= 1;
    }

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

    /**
     * Компаратор для складов: сначала MOV склады, потом по дате доставки, потом по цене
     */
    private Comparator<Warehouse> createWarehouseComparator() {
        return Comparator
                // Сначала MOV склады
                .comparing((Warehouse warehouse) -> !isMovWarehouse(warehouse))
                // Потом по дате доставки
                .thenComparing(warehouse -> parseArmtekDeliveryDate(warehouse.getShipmentDate()))
                // Потом по цене
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

            // Логируем тип склада для отладки
            boolean isMov = isMovWarehouse(bestWarehouse);
            log.debug("Best warehouse for {} {}: code={}, isMov={}, deliveryDays={}",
                    groupedOffer.getBrand(), groupedOffer.getOriginalArticle(),
                    bestWarehouse.getCode(), isMov, groupedOffer.getDeliveryDays());
        }
    }

    private boolean isExactMatch(PartOfferDto offer, String requestedArticle, String requestedBrand) {
        if (offer == null) return false;

        String normalizedRequestedArticle = normalizeArticle(requestedArticle);
        String normalizedOfferArticle = normalizeArticle(offer.getOriginalArticle());

        boolean articleMatches = normalizedOfferArticle != null &&
                normalizedOfferArticle.equals(normalizedRequestedArticle);

        boolean brandMatches = requestedBrand == null || requestedBrand.trim().isEmpty() ||
                (offer.getBrand() != null && offer.getBrand().equalsIgnoreCase(requestedBrand));

        return articleMatches && brandMatches;
    }

    private String normalizeArticle(String article) {
        if (article == null) return null;
        return article.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
    }

    private LocalDateTime parseArmtekDeliveryDate(String shipmentDate) {
        if (shipmentDate == null || shipmentDate.trim().isEmpty()) {
            return LocalDateTime.MAX;
        }

        try {
            if (shipmentDate.length() == 14 && shipmentDate.matches("\\d+")) {
                LocalDateTime result = LocalDateTime.parse(shipmentDate, ARMTEK_DATE_FORMATTER);
                return result;
            }
            LocalDateTime result = LocalDateTime.parse(shipmentDate);
            return result;
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
                    LocalDateTime now = LocalDateTime.now();
                    LocalDate shipmentDateOnly = shipmentDate.toLocalDate();
                    LocalDate nowDateOnly = now.toLocalDate();

                    long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(nowDateOnly, shipmentDateOnly);

                    if (daysBetween <= 0) {
                        return 1;
                    }

                    return (int) daysBetween;
                })
                .orElse(1);
    }

    private String getPriorityName(int priority) {
        return switch (priority) {
            case 1 -> "MOV+БЫСТРАЯ";
            case 2 -> "MOV+МЕДЛЕННАЯ";
            case 3 -> "ДРУГИЕ";
            default -> "НЕИЗВЕСТНО";
        };
    }
}