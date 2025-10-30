package com.igorgorbachev.SpringBootBK.procenka.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Warehouse {
    @JsonProperty("code")
    private String code;

    @JsonProperty("id")
    private String id;

    @JsonProperty("own")
    private Boolean own;

    @JsonProperty("shipmentDate")
    private String shipmentDate;

    // ДОБАВЬТЕ ЭТИ ПОЛЯ ДЛЯ ДИАПАЗОНОВ ДАТ
    @JsonProperty("startDate")
    private String startDate;

    @JsonProperty("endDate")
    private String endDate;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("stock")
    private Integer stock;

    @JsonProperty("notRefund")
    private Boolean notRefund;

    // ДОБАВЬТЕ ЭТО ПОЛЕ
    @JsonProperty("deliveryDays")
    private Integer deliveryDays;

    private String formattedDelivery;

    public String getFormattedDelivery() {
        // ПРИОРИТЕТ: startDate + endDate -> shipmentDate -> deliveryDays
        if (startDate != null && endDate != null) {
            return formatDateRange(startDate, endDate);
        }

        if (startDate != null) {
            return formatSingleDate(startDate);
        }

        if (shipmentDate != null) {
            return formatSingleDate(shipmentDate);
        }

        // Fallback на deliveryDays
        return deliveryDays != null ? deliveryDays + " дн." : "1 дн.";
    }

    private String formatSingleDate(String dateString) {
        if (dateString == null) {
            return deliveryDays != null ? deliveryDays + " дн." : "1 дн.";
        }

        try {
            // Формат Armtek: "20251014143000" (yyyyMMddHHmmss)
            if (dateString.length() == 14 && dateString.matches("\\d+")) {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                LocalDateTime dateTime = LocalDateTime.parse(dateString, inputFormatter);
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd.MM");
                return outputFormatter.format(dateTime);
            }

            // Формат Favorite Parts: "2025-10-13T20:00:00+0300"
            if (dateString.contains("T")) {
                String datePart = dateString.substring(0, 16);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                LocalDateTime dateTime = LocalDateTime.parse(datePart, formatter);
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("до HH:mm - dd.MM.yyyy");
                return outputFormatter.format(dateTime);
            }

        } catch (Exception e) {
            // Если не удалось распарсить
        }

        return deliveryDays != null ? deliveryDays + " дн." : "1 дн.";
    }

    private String formatDateRange(String startDate, String endDate) {
        try {
            String formattedStart = formatSingleDate(startDate);
            String formattedEnd = formatSingleDate(endDate);

            // Если даты одинаковые - показываем одну
            if (formattedStart.equals(formattedEnd)) {
                return formattedStart;
            }

            // Показываем диапазон
            return formattedStart + "-" + formattedEnd;

        } catch (Exception e) {
            return formatSingleDate(startDate);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Warehouse warehouse = (Warehouse) o;
        return Objects.equals(code, warehouse.code) &&
                Objects.equals(id, warehouse.id) &&
                Objects.equals(own, warehouse.own) &&
                Objects.equals(shipmentDate, warehouse.shipmentDate) &&
                Objects.equals(startDate, warehouse.startDate) &&
                Objects.equals(endDate, warehouse.endDate) &&
                Objects.equals(price, warehouse.price) &&
                Objects.equals(stock, warehouse.stock) &&
                Objects.equals(notRefund, warehouse.notRefund) &&
                Objects.equals(deliveryDays, warehouse.deliveryDays);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, id, own, shipmentDate, startDate, endDate, price, stock, notRefund, deliveryDays);
    }
}
