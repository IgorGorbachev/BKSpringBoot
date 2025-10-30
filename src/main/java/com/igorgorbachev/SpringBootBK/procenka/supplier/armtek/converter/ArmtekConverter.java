package com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.converter;

import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.dto.Warehouse;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.model.ArmtekGoods;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ArmtekConverter {

    public PartOfferDto toPartOfferDto(ArmtekGoods goods) {
        try {
            PartOfferDto dto = new PartOfferDto();

            // Основные поля
            dto.setSupplierName("Armtek");
            dto.setBrand(goods.getBrand());
            dto.setOriginalArticle(goods.getPin());
            dto.setPartName(goods.getName());
            dto.setPrice(goods.getPrice());
            dto.setQuantityAvailable(goods.getParsedQuantity());

            // Создаем Warehouse с правильными датами
            List<Warehouse> warehouses = new ArrayList<>();
            Warehouse warehouse = new Warehouse();

            warehouse.setCode(goods.getKeyzak());
            warehouse.setPrice(goods.getPrice());
            warehouse.setStock(goods.getParsedQuantity());

            // ВАЖНО: правильное маппинг дат
            warehouse.setShipmentDate(goods.getDeliveryDate()); // DLVDT
            warehouse.setStartDate(goods.getDeliveryDate());    // DLVDT -> startDate
            warehouse.setEndDate(goods.getGuaranteedDeliveryDate()); // WRNTDT -> endDate

            // ДОБАВЬТЕ ЭТУ СТРОКУ:
            warehouse.setDeliveryDays(goods.getDeliveryDays());

            // Дополнительные поля
            warehouse.setNotRefund(goods.getReturnDays() != null && goods.getReturnDays() == 0);

            warehouses.add(warehouse);
            dto.setWarehouses(warehouses);

            // Для обратной совместимости
            dto.setWarehouse(goods.getKeyzak());
            dto.setDeliveryDays(goods.getDeliveryDays());
            dto.setIsReturnable(goods.getReturnDays() != null && goods.getReturnDays() > 0);
            dto.setReturnInfo(goods.getReturnDays() != null && goods.getReturnDays() > 0 ?
                    "Возврат " + goods.getReturnDays() + " дн." : "Без возврата");

            return dto;

        } catch (Exception e) {
            log.error("Error converting Armtek goods to offer: {}", e.getMessage());
            return null;
        }
    }

    public List<PartOfferDto> toPartOfferDtos(List<ArmtekGoods> goods) {
        if (goods == null || goods.isEmpty()) {
            return List.of();
        }

        List<PartOfferDto> offers = goods.stream()
                .filter(Objects::nonNull)
                .map(this::toPartOfferDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Converted {} Armtek goods to {} offers", goods.size(), offers.size());

        // Логируем первые несколько для дебага
        for (int i = 0; i < Math.min(3, offers.size()); i++) {
            PartOfferDto offer = offers.get(i);
            if (offer.getWarehouses() != null && !offer.getWarehouses().isEmpty()) {
                Warehouse w = offer.getWarehouses().get(0);
                log.debug("Converted offer {}: startDate='{}', endDate='{}', deliveryDays={}, formatted='{}'",
                        i, w.getStartDate(), w.getEndDate(), w.getDeliveryDays(), w.getFormattedDelivery());
            }
        }

        return offers;
    }
}
