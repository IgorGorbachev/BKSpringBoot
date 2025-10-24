package com.igorgorbachev.SpringBootBK.procenka.dto;

import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.model.ArmtekGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.etsp.model.EtspGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.model.FavoritePartsGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.model.Warehouse;
import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.model.ForumAutoGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.model.TmtrGoods;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;


import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PartOfferDto {
    private String supplierName = "Forum-Auto";
    private String partName;
    private String originalArticle;
    private Double price;
    private Integer quantityAvailable;
    private Integer deliveryDays;
    private String warranty = "14 дней";
    private String brand;
    private String warehouse;
    private List<Warehouse> warehouses;
    private List<FavoritePartsGoods> analogues;

    // НОВОЕ ПОЛЕ: информация о возвратности
    private String returnInfo;
    private Boolean isReturnable; // для удобства фильтрации

    // Конструктор для ForumAuto
    public PartOfferDto(ForumAutoGoods goods) {
        this.partName = goods.getName();
        this.originalArticle = goods.getArt();
        this.price = goods.getPrice();
        this.quantityAvailable = goods.getQuantity();
        this.deliveryDays = goods.getDeliveryDays();
        this.brand = goods.getBrand();
        this.warehouse = goods.getWarehouse();

        // ИНФОРМАЦИЯ О ВОЗВРАТНОСТИ
        this.isReturnable = goods.getIsReturnable() != null && goods.getIsReturnable() == 1;
        this.returnInfo = this.isReturnable ? "Возвратная" : "Без возврата";
    }

    // Конструктор для Favorite Parts
    public PartOfferDto(FavoritePartsGoods goods) {
        log.info("Creating PartOfferDto from FavoritePartsGoods: brand={}, number={}, warehouses={}, analogues={}",
                goods.getBrand(), goods.getNumber(),
                goods.getWarehouses() != null ? goods.getWarehouses().size() : 0,
                goods.getAnalogues() != null ? goods.getAnalogues().size() : 0);

        this.supplierName = "Favorite Parts";
        this.brand = goods.getBrand();
        this.originalArticle = goods.getNumber();
        this.partName = goods.getName();

        // Фильтруем только свои склады
        List<Warehouse> ownWarehouses = goods.getWarehouses().stream()
                .filter(w -> Boolean.TRUE.equals(w.getOwn()))
                .collect(Collectors.toList());

        // Если есть свои склады - используем их, иначе все склады
        List<Warehouse> warehousesForCalculation = !ownWarehouses.isEmpty() ? ownWarehouses : goods.getWarehouses();

        // Берем минимальную цену
        this.price = warehousesForCalculation.stream()
                .map(Warehouse::getPrice)
                .filter(Objects::nonNull)
                .min(Double::compare)
                .orElse(0.0);

        // Суммируем количество
        this.quantityAvailable = warehousesForCalculation.stream()
                .mapToInt(w -> w.getStock() != null ? w.getStock() : 0)
                .sum();

        // Если использовали не свои склады, но они есть - количество 0
        if (ownWarehouses.isEmpty() && !goods.getWarehouses().isEmpty()) {
            this.quantityAvailable = 0;
        }

        this.deliveryDays = calculateDeliveryDaysFromShipment(goods.getWarehouses());
        this.warehouses = goods.getWarehouses();
        this.analogues = goods.getAnalogues();

        // ИНФОРМАЦИЯ О ВОЗВРАТНОСТИ
        this.isReturnable = true; // Favorite Parts обычно возвратные
        this.returnInfo = "Возвратная";
    }


    // Конструктор для Armtek - с поддержкой складов
    public PartOfferDto(ArmtekGoods goods) {
        this.supplierName = "Armtek";
        this.partName = goods.getName();
        this.originalArticle = goods.getPin();
        this.price = goods.getPrice();
        this.quantityAvailable = goods.getParsedQuantity();
        this.deliveryDays = goods.getDeliveryDays();
        this.brand = goods.getBrand();
        this.warehouse = goods.getKeyzak() != null ? goods.getKeyzak() : "Armtek";
        this.warranty = goods.getReturnDays() != null ? goods.getReturnDays() + " дней" : "14 дней";

        // ИНФОРМАЦИЯ О ВОЗВРАТНОСТИ
        this.isReturnable = goods.getReturnDays() != null && goods.getReturnDays() > 0;
        this.returnInfo = goods.getReturnDays() != null ? goods.getReturnDays() + " дней" : "Без возврата";

        // СОЗДАЕМ СПИСОК СКЛАДОВ ИЗ warehouseGoods
        if (goods.getWarehouseGoods() != null && !goods.getWarehouseGoods().isEmpty()) {
            this.warehouses = goods.getWarehouseGoods().stream()
                    .map(warehouseGoods -> {
                        Warehouse warehouse = new Warehouse();
                        warehouse.setCode(warehouseGoods.getKeyzak() != null ? warehouseGoods.getKeyzak() : "Armtek");
                        warehouse.setPrice(warehouseGoods.getPrice());
                        warehouse.setStock(warehouseGoods.getParsedQuantity());
                        warehouse.setOwn(true);
                        warehouse.setNotRefund(warehouseGoods.getReturnDays() == null || warehouseGoods.getReturnDays() <= 0);

                        // Устанавливаем shipmentDate для совместимости
                        warehouse.setShipmentDate(warehouseGoods.getDeliveryDate());

                        // ВАЖНО: ЯВНО УСТАНАВЛИВАЕМ formattedDelivery из метода ArmtekGoods
                        // который имеет доступ к обоим датам (DLVDT и WRNTDT)
                        warehouse.setFormattedDelivery(warehouseGoods.getFormattedDelivery());

                        return warehouse;
                    })
                    .collect(Collectors.toList());

            log.debug("Armtek DTO created: article='{}', deliveryDays={}, formatted='{}', warehouses={}",
                    this.originalArticle, this.deliveryDays, this.getFormattedDelivery(), this.warehouses.size());
        } else {
            // Если нет warehouseGoods, создаем один склад из текущего товара
            Warehouse armtekWarehouse = new Warehouse();
            armtekWarehouse.setCode(goods.getKeyzak() != null ? goods.getKeyzak() : "Armtek");
            armtekWarehouse.setPrice(goods.getPrice());
            armtekWarehouse.setStock(goods.getParsedQuantity());
            armtekWarehouse.setOwn(true);
            armtekWarehouse.setNotRefund(!this.isReturnable);

            // Устанавливаем shipmentDate
            armtekWarehouse.setShipmentDate(goods.getDeliveryDate());

            // ВАЖНО: ЯВНО УСТАНАВЛИВАЕМ formattedDelivery
            armtekWarehouse.setFormattedDelivery(goods.getFormattedDelivery());

            this.warehouses = List.of(armtekWarehouse);

            log.debug("Armtek DTO created (single warehouse): article='{}', deliveryDays={}, formatted='{}'",
                    this.originalArticle, this.deliveryDays, this.getFormattedDelivery());
        }
    }

    // Конструктор для TMTR
    public PartOfferDto(TmtrGoods goods) {
        this.supplierName = "TMTR";
        this.partName = goods.getName() != null ? goods.getName() : "Не указано";
        this.originalArticle = goods.getNumber() != null ? goods.getNumber() : "Не указан";
        this.price = goods.getPrice() != null ? goods.getPrice() : 0.0;
        this.quantityAvailable = goods.getParsedQuantity() != null ? goods.getParsedQuantity() : 0;
        this.deliveryDays = goods.getDeliveryPeriod() != null ? goods.getDeliveryPeriod() : 1;
        this.brand = goods.getBrand() != null ? goods.getBrand() : "Не указан";

        // ОТОБРАЖАЕМ КОНКРЕТНОЕ НАЗВАНИЕ СКЛАДА ИЗ StockName
        if (goods.getStockName() != null && !goods.getStockName().trim().isEmpty()) {
            this.warehouse = goods.getStockName(); // "Владимир", "Москва" и т.д.
        } else if (goods.getWarehouse() != null && !goods.getWarehouse().trim().isEmpty()) {
            this.warehouse = goods.getWarehouse(); // fallback на Warehouse
        } else {
            this.warehouse = goods.getWarehouseName(); // последний fallback
        }

        this.warranty = goods.isReturnable() ? "14 дней" : "Без возврата";
        this.isReturnable = true;
        this.returnInfo = this.isReturnable ? "Возвратная" : "Без возврата";
        this.warehouses = null;

        log.debug("TMTR offer created: article={}, brand={}, stockName={}, warehouse={}",
                this.originalArticle, this.brand, goods.getStockName(), this.warehouse);
    }
    // Конструктор для ETSP
    public PartOfferDto(EtspGoods goods) {
        this.supplierName = "ETSP";
        this.partName = goods.getName() != null ? goods.getName() : "Не указано";
        this.originalArticle = goods.getNumber() != null ? goods.getNumber() : "Не указан";
        this.price = goods.getPrice() != null ? goods.getPrice().doubleValue() : 0.0;
        this.quantityAvailable = goods.getQuantity() != null ? goods.getQuantity() : 0;
        this.brand = goods.getBrand() != null ? goods.getBrand() : "Не указан";
        this.warehouse = "ETSP";

        // Расчет дней доставки
        this.deliveryDays = calculateEtspDeliveryDays(goods);

        // Информация о возвратности
        this.isReturnable = true;
        this.returnInfo = "Возвратная";
        this.warranty = "14 дней";
    }

    private Integer calculateDeliveryDaysFromShipment(List<Warehouse> warehouses) {
        // Здесь можно добавить логику расчета дней доставки из shipmentDate
        // Пока возвращаем 0 или минимальное значение
        return 0;
    }

    private Integer calculateEtspDeliveryDays(EtspGoods goods) {
        // Логика расчета дней доставки для ETSP
        if (goods.getShipmentDate() != null) {
            // Можно добавить расчет на основе shipmentDate
            return 1;
        }
        return 1; // по умолчанию
    }

    public String getFormattedDelivery() {
        // Для Armtek и TMTR используем специальную логику
        if (supplierName.equals("Armtek")) {
            if (deliveryDays != null && deliveryDays > 0) {
                return deliveryDays + " дн.";
            } else {
                return "1 дн."; // По умолчанию для Armtek
            }
        }

        if (supplierName.equals("TMTR")) {
            // Для TMTR используем форматированную дату из модели TmtrGoods
            // Временное решение - можно улучшить, передавая TmtrGoods в DTO
            return deliveryDays != null ? deliveryDays + " дн." : "Нет данных";
        }

        // Логика для других поставщиков (Favorite Parts, Forum-Auto)
        if (warehouses == null || warehouses.isEmpty()) {
            return deliveryDays != null ? deliveryDays + " дн." : "Нет данных";
        }

        // Ищем самый ранний срок доставки среди своих складов
        Optional<Warehouse> earliestOwnWarehouse = warehouses.stream()
                .filter(w -> Boolean.TRUE.equals(w.getOwn()))
                .filter(w -> w.getShipmentDate() != null)
                .min(Comparator.comparing(Warehouse::getShipmentDate));

        if (earliestOwnWarehouse.isPresent()) {
            return earliestOwnWarehouse.get().getFormattedDelivery();
        }

        // Если своих складов нет, ищем самый ранний срок среди всех складов
        Optional<Warehouse> earliestWarehouse = warehouses.stream()
                .filter(w -> w.getShipmentDate() != null)
                .min(Comparator.comparing(Warehouse::getShipmentDate));

        if (earliestWarehouse.isPresent()) {
            return earliestWarehouse.get().getFormattedDelivery();
        }

        // Если нет дат доставки, возвращаем дни
        return deliveryDays != null ? deliveryDays + " дн." : "Нет данных";
    }


    /**
     * Форматированная строка доставки в формате "13.10 - 15.10" или "1 дн."
     */


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PartOfferDto that = (PartOfferDto) o;
        return Objects.equals(supplierName, that.supplierName) &&
                Objects.equals(partName, that.partName) &&
                Objects.equals(originalArticle, that.originalArticle) &&
                Objects.equals(price, that.price) &&
                Objects.equals(quantityAvailable, that.quantityAvailable) &&
                Objects.equals(deliveryDays, that.deliveryDays) &&
                Objects.equals(warranty, that.warranty) &&
                Objects.equals(brand, that.brand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supplierName, partName, originalArticle, price, quantityAvailable, deliveryDays, warranty, brand);
    }
}