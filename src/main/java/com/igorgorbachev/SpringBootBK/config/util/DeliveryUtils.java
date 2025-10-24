package com.igorgorbachev.SpringBootBK.config.util;

import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import org.springframework.stereotype.Component;

@Component
public class DeliveryUtils {
    public boolean hasFastDelivery(PartOfferDto offer, int maxDays) {
        if (offer.getDeliveryDays() != null) {
            return offer.getDeliveryDays() <= maxDays;
        }

        // Если deliveryDays не указан, проверяем formattedDelivery
        if (offer.getFormattedDelivery() != null) {
            String delivery = offer.getFormattedDelivery().toLowerCase();
            // Проверяем наличие цифр 1-2 в описании доставки
            if (delivery.contains("1 дн") || delivery.contains("1дн") ||
                    delivery.contains("2 дн") || delivery.contains("2дн") ||
                    delivery.contains("сегодня") || delivery.contains("завтра")) {
                return true;
            }
        }

        // По умолчанию считаем доставку медленной
        return false;
    }
}
