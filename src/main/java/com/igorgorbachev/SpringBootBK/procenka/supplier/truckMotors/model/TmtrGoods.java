package com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmtrGoods {

    @JsonProperty("OS")
    private Integer os;

    @JsonProperty("ShowedQuantity")
    private String showedQuantity;

    @JsonProperty("DeliveryDate")
    private String deliveryDate;

    @JsonProperty("MinPartyQuantity")
    private Integer minPartyQuantity;

    @JsonProperty("MinPackQuantity")
    private Integer minPackQuantity;

    @JsonProperty("StockName")
    private String stockName;

    @JsonProperty("Price")
    private Double price;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("Producer")
    private String brand;

    @JsonProperty("Article")
    private String number;

    @JsonProperty("Nomenclature")
    private String name;

    @JsonProperty("Warehouse")
    private String warehouse;

    @JsonProperty("DeliveryPeriod")
    private Integer deliveryPeriod;

    @JsonProperty("VerVsrok")
    private Double deliveryProbability;

    @JsonProperty("IsReturn")
    private Boolean isReturn;

    @JsonProperty("DeadLine")
    private String deadLine;

    @JsonProperty("GuarantedDate")
    private String guaranteedDate;

    @JsonProperty("PriceLastUpdateDate")
    private String priceLastUpdateDate;

    @JsonProperty("Ver")
    private Integer ver;

    @JsonProperty("CrossID")
    private Long crossId;

    @JsonProperty("HashCode")
    private Long hashCode;

    // Вычисляемые поля
    public Integer getParsedQuantity() {
        if (showedQuantity == null || showedQuantity.trim().isEmpty()) {
            return 0;
        }

        try {
            String quantityStr = showedQuantity.trim();

            // Обработка специальных символов
            if (quantityStr.startsWith(">")) {
                quantityStr = quantityStr.substring(1);
            }
            if (quantityStr.endsWith("+")) {
                quantityStr = quantityStr.substring(0, quantityStr.length() - 1);
            }

            // Обработка диапазонов
            if (quantityStr.contains("-")) {
                String[] parts = quantityStr.split("-");
                if (parts.length == 2) {
                    try {
                        return Integer.parseInt(parts[0].trim()); // Берем минимальное значение
                    } catch (NumberFormatException e) {
                        // Продолжаем обработку
                    }
                }
            }

            return Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            log.debug("Failed to parse quantity: '{}'", showedQuantity);
            return 0;
        }
    }

    public String getFormattedDelivery() {
        if (deliveryDate == null || deliveryDate.trim().isEmpty() ||
                deliveryDate.equals("0001-01-01T00:00:00")) {
            return deliveryPeriod != null ? deliveryPeriod + " дн." : "Нет данных";
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime date = LocalDateTime.parse(deliveryDate, formatter);
            return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (DateTimeParseException e) {
            log.debug("Failed to parse delivery date: '{}'", deliveryDate);
            return deliveryPeriod != null ? deliveryPeriod + " дн." : "Нет данных";
        }
    }

    public String getWarehouseDisplayName() {
        // Приоритет: StockName -> Warehouse -> OS-based name
        if (stockName != null && !stockName.trim().isEmpty()) {
            return stockName.trim();
        }

        if (warehouse != null && !warehouse.trim().isEmpty()) {
            return warehouse.trim();
        }

        if (os == null) return "TMTR";

        switch (os) {
            case 1:
                return "Склад ТМ";
            case 2:
                String period = deliveryPeriod != null ? " (" + deliveryPeriod + " дн.)" : "";
                return "В пути" + period;
            case 3:
                return "Партнерский склад";
            default:
                return "TMTR";
        }
    }

    public String getDeliveryStatus() {
        if (os == null) return "Неизвестно";

        switch (os) {
            case 1: return "В наличии";
            case 2: return "В пути";
            case 3: return "Под заказ";
            default: return "Неизвестно";
        }
    }

    public boolean isReturnable() {
        return isReturn != null && isReturn;
    }

    public boolean isFastDelivery() {
        return os != null && os == 1;
    }

    public Double getEffectivePrice() {
        return price != null && price > 0 ? price : null;
    }

    public boolean hasHighDeliveryProbability() {
        return deliveryProbability != null && deliveryProbability >= 80.0;
    }
}
