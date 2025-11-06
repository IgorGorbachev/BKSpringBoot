package com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.converter;

import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.dto.Warehouse;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.model.ArmtekGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.service.ArmtekDataProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArmtekConverter {

    private final ArmtekDataProcessor dataProcessor;

    public PartOfferDto toPartOfferDto(ArmtekGoods goods) {
        try {
            PartOfferDto dto = new PartOfferDto();

            // Используем ArmtekDataProcessor для расчетов
            Integer parsedQuantity = dataProcessor.parseQuantity(goods);
            Integer deliveryDays = dataProcessor.parseDeliveryDays(goods);
            String formattedDelivery = dataProcessor.formatDelivery(goods);

            // Основные поля
            dto.setSupplierName("Armtek");
            dto.setBrand(goods.getBrand());
            dto.setOriginalArticle(goods.getPin());
            dto.setPartName(goods.getName());
            dto.setPrice(goods.getPrice());
            dto.setQuantityAvailable(parsedQuantity);
            dto.setDeliveryDays(deliveryDays);

            // Создаем Warehouse с правильными датами
            List<Warehouse> warehouses = new ArrayList<>();
            Warehouse warehouse = new Warehouse();

            warehouse.setCode(goods.getKeyzak());
            warehouse.setPrice(goods.getPrice());
            warehouse.setStock(parsedQuantity);
            warehouse.setShipmentDate(goods.getDeliveryDate());
            warehouse.setStartDate(goods.getDeliveryDate());
            warehouse.setEndDate(goods.getGuaranteedDeliveryDate());
            warehouse.setDeliveryDays(deliveryDays);
            warehouse.setFormattedDelivery(formattedDelivery);
            warehouse.setNotRefund(goods.getReturnDays() != null && goods.getReturnDays() == 0);
            warehouse.setOwn(isMovWarehouse(goods.getKeyzak()));

            warehouses.add(warehouse);
            dto.setWarehouses(warehouses);

            // Для обратной совместимости
            dto.setWarehouse(goods.getKeyzak());
            dto.setIsReturnable(goods.getReturnDays() != null && goods.getReturnDays() > 0);
            dto.setReturnInfo(goods.getReturnDays() != null && goods.getReturnDays() > 0 ?
                    "Возврат " + goods.getReturnDays() + " дн." : "Без возврата");
            dto.setWarranty(goods.getReturnDays() != null ? goods.getReturnDays() + " дней" : "14 дней");

            log.debug("Armtek DTO created: article='{}', deliveryDays={}, formatted='{}'",
                    dto.getOriginalArticle(), dto.getDeliveryDays(), formattedDelivery);

            return dto;

        } catch (Exception e) {
            log.error("Error converting Armtek goods to offer: {}", e.getMessage());
            return null;
        }
    }

    private boolean isMovWarehouse(String warehouseCode) {
        return warehouseCode != null && warehouseCode.toUpperCase().startsWith("MOV");
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

        return offers;
    }
}