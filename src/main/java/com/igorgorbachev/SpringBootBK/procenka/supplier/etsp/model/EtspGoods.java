package com.igorgorbachev.SpringBootBK.procenka.supplier.etsp.model;


import com.igorgorbachev.SpringBootBK.procenka.supplier.etsp.model.dto.EtspWarehouse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EtspGoods {
    private String number;
    private String brand;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private List<EtspWarehouse> warehouses;  // Теперь использует публичный класс
    private LocalDateTime shipmentDate;
    private String supplierCode;

    // Добавляем конструктор для удобства
    public EtspGoods(String number, String brand, String name, BigDecimal price, Integer quantity) {
        this.number = number;
        this.brand = brand;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EtspGoods etspGoods = (EtspGoods) o;
        return Objects.equals(number, etspGoods.number) &&
                Objects.equals(brand, etspGoods.brand) &&
                Objects.equals(name, etspGoods.name) &&
                Objects.equals(price, etspGoods.price) &&
                Objects.equals(quantity, etspGoods.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, brand, name, price, quantity);
    }
}

