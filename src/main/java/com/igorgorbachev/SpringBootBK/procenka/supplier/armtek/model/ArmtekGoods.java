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
        if (deliveryDate == null || deliveryDate.trim().isEmpty()) {
            return 1; // значение по умолчанию
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            LocalDateTime deliveryDateTime = LocalDateTime.parse(deliveryDate, formatter);
            LocalDateTime now = LocalDateTime.now();

            // Вычисляем разницу в днях (округляем вверх)
            long hours = java.time.Duration.between(now, deliveryDateTime).toHours();
            long days = (hours + 23) / 24; // Округляем вверх до целых дней

            return (int) Math.max(1, days); // минимум 1 день
        } catch (DateTimeParseException e) {
            log.debug("Failed to parse delivery date: {}", deliveryDate);
            return 1; // значение по умолчанию при ошибке
        }
    }

    public String getFormattedDelivery() {
        // Если есть гарантированная дата доставки - показываем диапазон
        if (deliveryDate != null && !deliveryDate.trim().isEmpty() &&
                guaranteedDeliveryDate != null && !guaranteedDeliveryDate.trim().isEmpty()) {

            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd.MM");

                LocalDateTime startDate = LocalDateTime.parse(deliveryDate, inputFormatter);
                LocalDateTime endDate = LocalDateTime.parse(guaranteedDeliveryDate, inputFormatter);

                // Если дата начала и окончания в одном месяце
                if (startDate.getMonth() == endDate.getMonth()) {
                    return startDate.format(DateTimeFormatter.ofPattern("dd")) + "-" +
                            endDate.format(outputFormatter);
                } else {
                    // Если в разных месяцах - показываем полные даты
                    return startDate.format(outputFormatter) + " - " +
                            endDate.format(outputFormatter);
                }
            } catch (DateTimeParseException e) {
                log.debug("Failed to parse delivery date range: start={}, end={}", deliveryDate, guaranteedDeliveryDate);
                // Продолжаем обработку ниже
            }
        }

        // Если только дата начала доставки
        if (deliveryDate != null && !deliveryDate.trim().isEmpty()) {
            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd.MM");

                LocalDateTime deliveryDateTime = LocalDateTime.parse(deliveryDate, inputFormatter);
                return deliveryDateTime.format(outputFormatter);
            } catch (DateTimeParseException e) {
                log.debug("Failed to parse delivery date: {}", deliveryDate);
            }
        }

        // Fallback - показываем в днях
        Integer days = getDeliveryDays();
        return days + " дн.";
    }

    public Integer getParsedQuantity() {
        if (quantity == null || quantity.trim().isEmpty()) {
            return 0;
        }

        String quantityStr = quantity.trim();

        try {
            if (quantityStr.startsWith(">")) {
                String numStr = quantityStr.substring(1).trim();
                return Integer.parseInt(numStr);
            }
            if (quantityStr.endsWith("+")) {
                String numStr = quantityStr.substring(0, quantityStr.length() - 1).trim();
                return Integer.parseInt(numStr);
            }
            return Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            log.debug("Failed to parse quantity value: '{}', defaulting to 0", quantityStr);
            return 0;
        }
    }

    public boolean hasValidPrice() {
        return price != null && price > 0;
    }

    public boolean hasValidStock() {
        return getParsedQuantity() > 0;
    }
}
