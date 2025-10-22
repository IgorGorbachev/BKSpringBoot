package com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ForumAutoGoods {
    @JsonProperty("gid")
    private String gid;

    @JsonProperty("brand")
    private String brand;

    @JsonProperty("art")
    private String art;

    @JsonProperty("name")
    private String name;

    @JsonProperty("d_deliv")
    private Integer deliveryDays;

    @JsonProperty("h_deliv")
    private Integer deliveryHours;

    @JsonProperty("kr")
    private Integer multiplicity;

    @JsonProperty("num")
    private Integer quantity;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("whse")
    private String warehouse;

    @JsonProperty("is_returnable")
    private Integer isReturnable;


    public boolean isReturnable() {
        return isReturnable != null && isReturnable == 1;
    }
}
