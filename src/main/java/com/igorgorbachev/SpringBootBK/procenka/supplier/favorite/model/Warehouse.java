package com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.model;

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

    @JsonProperty("price")
    private Double price;

    @JsonProperty("stock")
    private Integer stock;

    @JsonProperty("notRefund")
    private Boolean notRefund;

    private String formattedDelivery;

    public String getFormattedDelivery() {
        if (shipmentDate == null) return "1 дн."; // По умолчанию

        try {
            // Пробуем формат Armtek: "20251014143000" (yyyyMMddHHmmss)
            if (shipmentDate.length() == 14 && shipmentDate.matches("\\d+")) {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                LocalDateTime dateTime = LocalDateTime.parse(shipmentDate, inputFormatter);
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd.MM");
                return outputFormatter.format(dateTime);
            }

            // Пробуем формат Favorite Parts: "2025-10-13T20:00:00+0300"
            if (shipmentDate.contains("T")) {
                String datePart = shipmentDate.substring(0, 16); // Берем "2025-10-13T20:00"
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                LocalDateTime dateTime = LocalDateTime.parse(datePart, formatter);
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("до HH:mm - dd.MM.yyyy");
                return outputFormatter.format(dateTime);
            }

        } catch (Exception e) {
            // Если не удалось распарсить, возвращаем по умолчанию
        }

        return "1 дн."; // По умолчанию при любой ошибке
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Warehouse warehouse = (Warehouse) o;
        return Objects.equals(code, warehouse.code) && Objects.equals(id, warehouse.id) && Objects.equals(own, warehouse.own) && Objects.equals(shipmentDate, warehouse.shipmentDate) && Objects.equals(price, warehouse.price) && Objects.equals(stock, warehouse.stock) && Objects.equals(notRefund, warehouse.notRefund);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, id, own, shipmentDate, price, stock, notRefund);
    }
}
