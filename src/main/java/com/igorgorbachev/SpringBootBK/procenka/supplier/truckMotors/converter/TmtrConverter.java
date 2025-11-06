package com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.converter;

import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.model.TmtrGoods;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TmtrConverter {

    public PartOfferDto toPartOfferDto(TmtrGoods goods) {
        try {
            PartOfferDto offer = new PartOfferDto(goods);

            // Дополнительная настройка для TMTR
            if (goods.getOs() != null && goods.getOs() == 1) {
                offer.setDeliveryDays(1); // Быстрая доставка для OS=1
            }

            return offer;
        } catch (Exception e) {
            log.error("Error converting TMTR goods to offer: {}", e.getMessage());
            return null;
        }
    }

    public List<PartOfferDto> toPartOfferDtos(List<TmtrGoods> goods) {
        if (goods == null || goods.isEmpty()) {
            return List.of();
        }

        List<PartOfferDto> offers = goods.stream()
                .filter(Objects::nonNull)
                .map(this::toPartOfferDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return offers;
    }
}
