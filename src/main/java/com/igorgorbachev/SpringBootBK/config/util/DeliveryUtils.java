package com.igorgorbachev.SpringBootBK.config.util;

import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.dto.Warehouse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class DeliveryUtils {

    private static final DateTimeFormatter FAVORITE_PARTS_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    public boolean hasFastDelivery(PartOfferDto offer, int maxDays) {
        if (offer.getDeliveryDays() != null) {
            return offer.getDeliveryDays() <= maxDays;
        }

        if (offer.getFormattedDelivery() != null) {
            String delivery = offer.getFormattedDelivery().toLowerCase();
            if (delivery.contains("1 дн") || delivery.contains("1дн") ||
                    delivery.contains("2 дн") || delivery.contains("2дн") ||
                    delivery.contains("сегодня") || delivery.contains("завтра")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Проверяет, есть ли быстрая доставка у конкретного склада
     */
    public boolean hasFastDeliveryForWarehouse(Warehouse warehouse, int maxDays) {
        if (warehouse == null) {
            return false;
        }

        if (warehouse.getDeliveryDays() != null) {
            return warehouse.getDeliveryDays() <= maxDays;
        }

        if (warehouse.getFormattedDelivery() != null) {
            String delivery = warehouse.getFormattedDelivery().toLowerCase();
            if (delivery.contains("1 дн") || delivery.contains("1дн") ||
                    delivery.contains("2 дн") || delivery.contains("2дн") ||
                    delivery.contains("сегодня") || delivery.contains("завтра")) {
                return true;
            }
        }

        if (warehouse.getShipmentDate() != null) {
            try {
                LocalDateTime shipmentDate = parseFavoritePartsDate(warehouse.getShipmentDate());
                if (shipmentDate != null) {
                    LocalDateTime now = LocalDateTime.now();
                    return shipmentDate.toLocalDate().isBefore(now.toLocalDate().plusDays(maxDays + 1));
                }
            } catch (Exception e) {
                log.debug("Не удалось распарсить дату доставки: {}", warehouse.getShipmentDate());
            }
        }

        return false;
    }

    /**
     * Проверяет, является ли склад РС-складом (сторонним)
     */
    public boolean isRSWarehouse(Warehouse warehouse) {
        return warehouse != null &&
                warehouse.getCode() != null &&
                warehouse.getCode().toUpperCase().contains(" РС");
    }

    /**
     * Проверяет, есть ли у оффера СВОИ склады (не РС)
     */
    public boolean hasOwnWarehouses(PartOfferDto offer) {
        if (offer == null || offer.getWarehouses() == null) {
            return false;
        }

        boolean result = offer.getWarehouses().stream()
                .anyMatch(warehouse -> !isRSWarehouse(warehouse));

        log.debug("hasOwnWarehouses check: offer={}, result={}, warehousesCount={}",
                offer.getBrand(), result, offer.getWarehouses().size());

        return result;
    }

    /**
     * Проверяет, есть ли у оффера БЫСТРАЯ доставка на СВОИХ складах
     */
    public boolean hasFastDeliveryOnOwnWarehouses(PartOfferDto offer, int maxDays) {
        if (offer == null || offer.getWarehouses() == null) {
            return false;
        }

        return offer.getWarehouses().stream()
                .filter(warehouse -> !isRSWarehouse(warehouse)) // только свои склады
                .anyMatch(warehouse -> hasFastDeliveryForWarehouse(warehouse, maxDays));
    }

    /**
     * Получает цветовой класс для строки Favorite Parts
     */
    public String getFavoritePartsRowClass(PartOfferDto offer, int fastDeliveryThreshold) {
        if (offer == null || offer.getWarehouses() == null || offer.getWarehouses().isEmpty()) {
            return "favorite-no-warehouse";
        }

        // Если нет своих складов - красный
        if (!hasOwnWarehouses(offer)) {
            return "favorite-rs-warehouse";
        }

        // Если есть свои склады и быстрая доставка - зеленый
        if (hasFastDeliveryOnOwnWarehouses(offer, fastDeliveryThreshold)) {
            return "favorite-fast-delivery";
        }

        // Если есть свои склады, но медленная доставка - желтый
        return "favorite-slow-delivery";
    }

    /**
     * Получает цветовой класс для склада в выпадающем списке
     */
    public String getWarehouseColorClass(Warehouse warehouse, int fastDeliveryThreshold) {
        if (warehouse == null) {
            return "";
        }

        if (isRSWarehouse(warehouse)) {
            return "warehouse-rs-warehouse";
        }

        if (hasFastDeliveryForWarehouse(warehouse, fastDeliveryThreshold)) {
            return "warehouse-fast-delivery";
        }

        return "warehouse-slow-delivery";
    }

    private LocalDateTime parseFavoritePartsDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            if (dateString.contains("T")) {
                return LocalDateTime.parse(dateString, FAVORITE_PARTS_DATE_FORMATTER);
            }
        } catch (DateTimeParseException e) {
            log.debug("Не удалось распарсить дату Favorite Parts: {}", dateString);
        }

        return null;
    }

    /**
     * Компаратор для сортировки Favorite Parts:
     * 1. Сначала товары со своими складами (не РС)
     * 2. Потом по цене (от меньшей к большей)
     */
    public Comparator<PartOfferDto> getFavoritePartsComparator() {
        return (offer1, offer2) -> {
            // 1. Сначала сравниваем по наличию своих складов
            boolean hasOwn1 = hasOwnWarehouses(offer1);
            boolean hasOwn2 = hasOwnWarehouses(offer2);

            if (hasOwn1 && !hasOwn2) {
                return -1; // offer1 имеет свои склады, offer2 - нет
            }
            if (!hasOwn1 && hasOwn2) {
                return 1; // offer2 имеет свои склады, offer1 - нет
            }

            // 2. Если оба имеют или не имеют свои склады - сравниваем по цене
            Double price1 = offer1.getPrice() != null ? offer1.getPrice() : Double.MAX_VALUE;
            Double price2 = offer2.getPrice() != null ? offer2.getPrice() : Double.MAX_VALUE;

            int priceComparison = Double.compare(price1, price2);
            if (priceComparison != 0) {
                return priceComparison;
            }

            // 3. При равенстве цены - по наличию (большее количество первее)
            Integer stock1 = offer1.getQuantityAvailable() != null ? offer1.getQuantityAvailable() : 0;
            Integer stock2 = offer2.getQuantityAvailable() != null ? offer2.getQuantityAvailable() : 0;

            return Integer.compare(stock2, stock1); // обратный порядок - большее количество первее
        };
    }

    /**
     * Сортирует список офферов Favorite Parts по правилам:
     * 1. Сначала товары со своими складами
     * 2. Потом по цене (от меньшей к большей)
     */
    public List<PartOfferDto> sortFavoritePartsOffers(List<PartOfferDto> offers) {
        if (offers == null || offers.isEmpty()) {
            return offers;
        }

        log.info("=== START FAVORITE PARTS SORTING ===");
        log.info("Total offers to sort: {}", offers.size());

        // Логируем исходные данные
        for (int i = 0; i < offers.size(); i++) {
            PartOfferDto offer = offers.get(i);
            log.debug("Before sorting [{}]: brand={}, price={}, hasOwnWarehouses={}, warehouses={}",
                    i, offer.getBrand(), offer.getPrice(), hasOwnWarehouses(offer),
                    offer.getWarehouses() != null ? offer.getWarehouses().size() : 0);
        }

        List<PartOfferDto> sorted = offers.stream()
                .sorted(getFavoritePartsComparator())
                .toList();

        // Логируем результат сортировки
        log.info("=== SORTING RESULT ===");
        int ownWarehouseCount = 0;
        for (int i = 0; i < sorted.size(); i++) {
            PartOfferDto offer = sorted.get(i);
            boolean hasOwn = hasOwnWarehouses(offer);
            if (hasOwn) ownWarehouseCount++;

            log.info("After sorting [{}]: brand={}, price={}, hasOwnWarehouses={}",
                    i, offer.getBrand(), offer.getPrice(), hasOwn);
        }

        log.info("Sorting completed: {} with own warehouses, {} with only RS warehouses",
                ownWarehouseCount, sorted.size() - ownWarehouseCount);
        log.info("=== END FAVORITE PARTS SORTING ===");

        return sorted;
    }
}
