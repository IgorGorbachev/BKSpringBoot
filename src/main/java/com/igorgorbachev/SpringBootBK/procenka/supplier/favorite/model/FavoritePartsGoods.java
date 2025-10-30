package com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.igorgorbachev.SpringBootBK.procenka.dto.Warehouse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FavoritePartsGoods {
    @JsonProperty("goodsID")
    private String goodsID;

    @JsonProperty("brand")
    private String brand;

    @JsonProperty("number")
    private String number;

    @JsonProperty("name")
    private String name;

    @JsonProperty("count")
    private Integer count;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("rate")
    private Integer rate;

    @JsonProperty("analogues")
    private List<FavoritePartsGoods> analogues;

    @JsonProperty("warehouses")
    private List<Warehouse> warehouses;

    @JsonProperty("notRefund")
    private Boolean notRefund;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FavoritePartsGoods goods = (FavoritePartsGoods) o;
        return Objects.equals(goodsID, goods.goodsID) && Objects.equals(brand, goods.brand) && Objects.equals(number, goods.number) && Objects.equals(name, goods.name) && Objects.equals(count, goods.count) && Objects.equals(price, goods.price) && Objects.equals(rate, goods.rate) && Objects.equals(analogues, goods.analogues) && Objects.equals(warehouses, goods.warehouses) && Objects.equals(notRefund, goods.notRefund);
    }

    @Override
    public int hashCode() {
        return Objects.hash(goodsID, brand, number, name, count, price, rate, analogues, warehouses, notRefund);
    }
}
