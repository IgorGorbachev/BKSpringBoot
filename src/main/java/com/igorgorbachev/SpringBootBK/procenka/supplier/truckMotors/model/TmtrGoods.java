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
    private Integer os; // Тип поставки: 1-Склад ТракМоторс, 2-В пути, 3-Склад Партнерской сети

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

    @JsonProperty("Producer") // ИСПРАВЛЕНО: было "Brand"
    private String brand;

    @JsonProperty("Article") // ИСПРАВЛЕНО: было "Number"
    private String number;

    @JsonProperty("Nomenclature") // ИСПРАВЛЕНО: было "Name"
    private String name;

    @JsonProperty("Warehouse")
    private String warehouse;

    @JsonProperty("DeliveryPeriod")
    private Integer deliveryPeriod;

    @JsonProperty("VerVsrok")
    private Double deliveryProbability; // Вероятность поставки в срок

    @JsonProperty("IsReturn")
    private Boolean isReturn; // Возвратность

    // Остальные поля без изменений
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

    // Дополнительные вычисляемые поля остаются без изменений
    public Integer getParsedQuantity() {
        if (showedQuantity == null || showedQuantity.trim().isEmpty()) {
            return 0;
        }

        try {
            String quantityStr = showedQuantity.trim();
            if (quantityStr.startsWith(">")) {
                quantityStr = quantityStr.substring(1);
            }
            if (quantityStr.endsWith("+")) {
                quantityStr = quantityStr.substring(0, quantityStr.length() - 1);
            }

            return Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
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
            return deliveryPeriod != null ? deliveryPeriod + " дн." : "Нет данных";
        }
    }

    public String getWarehouseName() {
        if (os == null) return "TMTR";

        switch (os) {
            case 1: return "Склад ТракМоторс";
            case 2: return "В пути";
            case 3: return "Склад ПС";
            default: return "TMTR";
        }
    }

    public boolean isReturnable() {
        return isReturn != null && isReturn;
    }

}
