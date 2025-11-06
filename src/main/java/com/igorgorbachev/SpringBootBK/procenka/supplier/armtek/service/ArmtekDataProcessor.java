package com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.service;

import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.model.ArmtekGoods;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Component
public class ArmtekDataProcessor {

    private static final DateTimeFormatter ARMTEK_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter OUTPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM");

    public Integer parseDeliveryDays(ArmtekGoods goods) {
        if (goods.getDeliveryDate() == null || goods.getDeliveryDate().trim().isEmpty()) {
            return 1; // значение по умолчанию
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            LocalDateTime deliveryDateTime = LocalDateTime.parse(goods.getDeliveryDate(), formatter);

            LocalDate deliveryDateOnly = deliveryDateTime.toLocalDate();
            LocalDate today = LocalDate.now();

            long days = java.time.temporal.ChronoUnit.DAYS.between(today, deliveryDateOnly);
            return (int) Math.max(1, days);
        } catch (DateTimeParseException e) {
            log.debug("Failed to parse delivery date: {}", goods.getDeliveryDate());
            return 1;
        }
    }

    public String formatDelivery(ArmtekGoods goods) {
        if (goods.getDeliveryDate() != null && !goods.getDeliveryDate().trim().isEmpty() &&
                goods.getGuaranteedDeliveryDate() != null && !goods.getGuaranteedDeliveryDate().trim().isEmpty()) {

            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd.MM");

                LocalDateTime startDate = LocalDateTime.parse(goods.getDeliveryDate(), inputFormatter);
                LocalDateTime endDate = LocalDateTime.parse(goods.getGuaranteedDeliveryDate(), inputFormatter);

                if (startDate.getMonth() == endDate.getMonth()) {
                    return startDate.format(DateTimeFormatter.ofPattern("dd")) + "-" +
                            endDate.format(outputFormatter);
                } else {
                    return startDate.format(outputFormatter) + " - " +
                            endDate.format(outputFormatter);
                }
            } catch (DateTimeParseException e) {
                log.debug("Failed to parse delivery date range: start={}, end={}",
                        goods.getDeliveryDate(), goods.getGuaranteedDeliveryDate());
            }
        }

        if (goods.getDeliveryDate() != null && !goods.getDeliveryDate().trim().isEmpty()) {
            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd.MM");

                LocalDateTime deliveryDateTime = LocalDateTime.parse(goods.getDeliveryDate(), inputFormatter);
                return deliveryDateTime.format(outputFormatter);
            } catch (DateTimeParseException e) {
                log.debug("Failed to parse delivery date: {}", goods.getDeliveryDate());
            }
        }

        Integer days = parseDeliveryDays(goods);
        return days + " дн.";
    }

    public Integer parseQuantity(ArmtekGoods goods) {
        if (goods.getQuantity() == null || goods.getQuantity().trim().isEmpty()) {
            return 0;
        }

        String quantityStr = goods.getQuantity().trim();

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

    public boolean hasValidPrice(ArmtekGoods goods) {
        return goods.getPrice() != null && goods.getPrice() > 0;
    }

    public boolean hasValidStock(ArmtekGoods goods) {
        return parseQuantity(goods) > 0;
    }
}
