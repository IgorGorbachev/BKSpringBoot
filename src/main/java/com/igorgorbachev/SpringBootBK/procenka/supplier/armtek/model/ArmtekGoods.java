package com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArmtekGoods {
    @JsonProperty("PIN")
    private String pin;

    @JsonProperty("BRAND")
    private String brand;

    @JsonProperty("NAME")
    private String name;

    @JsonProperty("ARTID")
    private String artId;

    @JsonProperty("PARNR")
    private String parnr;

    @JsonProperty("KEYZAK")
    private String keyzak;

    @JsonProperty("RVALUE")
    private String quantity;

    @JsonProperty("RDPRF")
    private Integer multiplicity;

    @JsonProperty("MINBM")
    private Double minQuantity;

    @JsonProperty("RETDAYS")
    private Integer returnDays;

    @JsonProperty("PRICE")
    private Double price;

    @JsonProperty("WAERS")
    private String currency;

    @JsonProperty("DLVDT")
    private String deliveryDate;

    @JsonProperty("WRNTDT")
    private String guaranteedDeliveryDate;

    @JsonProperty("ANALOG")
    private String analog;

    // Дополнительные поля из спецификации
    @JsonProperty("TYPEB")
    private String typeB;

    @JsonProperty("DSPEC")
    private String dspec;

    @JsonProperty("RCOST")
    private Double retailPrice;

    @JsonProperty("MRKBY")
    private Double markup;

    @JsonProperty("PNOTE")
    private String note;

    @JsonProperty("IMP_ADD")
    private Double importerAdd;

    @JsonProperty("SELLP")
    private Double sellerPrice;

    @JsonProperty("REST_ADD")
    private Double remainingAdd;

    @JsonProperty("REST_ADD_P")
    private Double remainingAddPercent;

    private List<ArmtekGoods> warehouseGoods;

    public boolean isAnalog() {
        return "X".equals(analog);
    }

    public Integer getDeliveryDays() {
        // Используем дату начала доставки для расчета дней
        if (deliveryDate == null || deliveryDate.trim().isEmpty()) {
            return 1; // По умолчанию 1 день
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            LocalDateTime deliveryDateTime = LocalDateTime.parse(deliveryDate, formatter);
            LocalDateTime now = LocalDateTime.now();

            long days = java.time.Duration.between(now, deliveryDateTime).toDays();
            return (int) Math.max(1, days); // Минимум 1 день
        } catch (DateTimeParseException e) {
            return 1; // По умолчанию 1 день при ошибке парсинга
        }
    }

    /**
     * Форматированная строка доставки в формате "13.10 - 15.10"
     */
    public String getFormattedDelivery() {
        // Если есть обе даты - формируем диапазон
        if (deliveryDate != null && !deliveryDate.trim().isEmpty() &&
                guaranteedDeliveryDate != null && !guaranteedDeliveryDate.trim().isEmpty()) {

            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd.MM");

                LocalDateTime startDate = LocalDateTime.parse(deliveryDate, inputFormatter);
                LocalDateTime endDate = LocalDateTime.parse(guaranteedDeliveryDate, inputFormatter);

                // Если даты разные - показываем диапазон
                if (!startDate.toLocalDate().equals(endDate.toLocalDate())) {
                    return startDate.format(outputFormatter) + " - " + endDate.format(outputFormatter);
                } else {
                    // Если даты одинаковые - показываем одну дату
                    return startDate.format(outputFormatter);
                }
            } catch (DateTimeParseException e) {
                log.warn("Failed to parse delivery date range: start={}, end={}", deliveryDate, guaranteedDeliveryDate);
                // Продолжаем обработку ниже
            }
        }

        // Если только одна дата или ошибка парсинга диапазона
        if (deliveryDate != null && !deliveryDate.trim().isEmpty()) {
            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd.MM");

                LocalDateTime deliveryDateTime = LocalDateTime.parse(deliveryDate, inputFormatter);
                return deliveryDateTime.format(outputFormatter);
            } catch (DateTimeParseException e) {
                log.warn("Failed to parse delivery date: {}", deliveryDate);
            }
        }

        return "1 дн."; // По умолчанию
    }

    public Integer getParsedQuantity() {
        if (quantity == null || quantity.trim().isEmpty()) {
            return 0;
        }

        String quantityStr = quantity.trim();

        try {
            // Обрабатываем значения типа ">100"
            if (quantityStr.startsWith(">")) {
                String numStr = quantityStr.substring(1).trim();
                return Integer.parseInt(numStr);
            }
            // Обрабатываем значения типа "100+"
            if (quantityStr.endsWith("+")) {
                String numStr = quantityStr.substring(0, quantityStr.length() - 1).trim();
                return Integer.parseInt(numStr);
            }
            // Пробуем распарсить как обычное число
            return Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            // Если не удалось распарсить, возвращаем 0
            log.warn("Failed to parse quantity value: '{}', defaulting to 0", quantityStr);
            return 0;
        }
    }
}
