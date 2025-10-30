package com.igorgorbachev.SpringBootBK.procenka.util;

import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.dto.Warehouse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Optional;

@Slf4j
@Component
public class DeliveryFormatter {

    private static final DateTimeFormatter ARMTEK_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter OUTPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM");

    public String formatDelivery(PartOfferDto offer) {
        if (offer == null) {
            return "Нет данных";
        }

        try {
            switch (offer.getSupplierName()) {
                case "Armtek":
                    return formatArmtekOfferDelivery(offer);
                case "TMTR":
                    return formatTmtrDelivery(offer);
                case "Forum-Auto":
                    return formatForumAutoDelivery(offer);
                case "Favorite Parts":
                    return formatFavoritePartsDelivery(offer);
                case "ETSP":
                    return formatEtspDelivery(offer);
                default:
                    return formatDefaultDelivery(offer);
            }
        } catch (Exception e) {
            log.warn("Ошибка форматирования доставки для {}: {}", offer.getSupplierName(), e.getMessage());
            return formatDefaultDelivery(offer);
        }
    }

    public String formatWarehouseDelivery(Warehouse warehouse) {
        return Optional.ofNullable(warehouse)
                .map(Warehouse::getFormattedDelivery)
                .orElse("Нет данных");
    }

    public String formatArmtekDelivery(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return "Нет данных";
        }

        try {
            String cleanDate = dateString.trim();
            if (cleanDate.length() == 14 && cleanDate.matches("\\d{14}")) {
                LocalDateTime deliveryDate = LocalDateTime.parse(cleanDate, ARMTEK_DATE_FORMATTER);
                return deliveryDate.format(OUTPUT_DATE_FORMATTER);
            }
            return cleanDate;
        } catch (DateTimeParseException e) {
            log.warn("Не удалось распарсить дату Armtek: {}", dateString);
            return "Ошибка даты";
        }
    }

    private String formatArmtekOfferDelivery(PartOfferDto offer) {
        return Optional.ofNullable(offer.getWarehouses())
                .filter(warehouses -> !warehouses.isEmpty())
                .flatMap(warehouses -> warehouses.stream().findFirst())
                .map(warehouse -> formatArmtekDelivery(warehouse.getShipmentDate()))
                .orElseGet(() -> offer.getDeliveryDays() != null ?
                        offer.getDeliveryDays() + " дн." : "Нет данных");
    }

    private String formatTmtrDelivery(PartOfferDto offer) {
        return offer.getDeliveryDays() != null ? offer.getDeliveryDays() + " дн." : "Нет данных";
    }

    private String formatForumAutoDelivery(PartOfferDto offer) {
        return offer.getDeliveryDays() != null ? offer.getDeliveryDays() + " дн." : "1-2 дн.";
    }

    private String formatFavoritePartsDelivery(PartOfferDto offer) {
        return Optional.ofNullable(offer.getWarehouses())
                .filter(warehouses -> !warehouses.isEmpty())
                .flatMap(warehouses -> warehouses.stream()
                        .filter(w -> w.getShipmentDate() != null)
                        .min(Comparator.comparing(Warehouse::getShipmentDate)))
                .map(warehouse -> formatArmtekDelivery(warehouse.getShipmentDate()))
                .orElseGet(() -> formatDefaultDelivery(offer));
    }

    private String formatEtspDelivery(PartOfferDto offer) {
        return offer.getDeliveryDays() != null ? offer.getDeliveryDays() + " дн." : "1 дн.";
    }

    private String formatDefaultDelivery(PartOfferDto offer) {
        return offer.getDeliveryDays() != null ? offer.getDeliveryDays() + " дн." : "Нет данных";
    }
}
