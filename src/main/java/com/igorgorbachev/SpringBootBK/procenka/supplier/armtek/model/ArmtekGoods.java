package com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArmtekGoods {
    @JsonProperty("PIN")
    private String pin;

    @JsonProperty("BRAND")
    private String brand;

    @JsonProperty("NAME")
    private String name;

    @JsonProperty("ARTID")
    private String artId;

    @JsonProperty("PARNR")
    private String parnr;

    @JsonProperty("KEYZAK")
    private String keyzak;

    @JsonProperty("RVALUE")
    private String quantity;

    @JsonProperty("RDPRF")
    private Integer multiplicity;

    @JsonProperty("MINBM")
    private Double minQuantity;

    @JsonProperty("RETDAYS")
    private Integer returnDays;

    @JsonProperty("PRICE")
    private Double price;

    @JsonProperty("WAERS")
    private String currency;

    @JsonProperty("DLVDT")
    private String deliveryDate;

    @JsonProperty("WRNTDT")
    private String guaranteedDeliveryDate;

    @JsonProperty("ANALOG")
    private String analog;

    @JsonProperty("TYPEB")
    private String typeB;

    @JsonProperty("DSPEC")
    private String dspec;

    @JsonProperty("RCOST")
    private Double retailPrice;

    @JsonProperty("MRKBY")
    private Double markup;

    @JsonProperty("PNOTE")
    private String note;

    @JsonProperty("IMP_ADD")
    private Double importerAdd;

    @JsonProperty("SELLP")
    private Double sellerPrice;

    @JsonProperty("REST_ADD")
    private Double remainingAdd;

    @JsonProperty("REST_ADD_P")
    private Double remainingAddPercent;

    private List<ArmtekGoods> warehouseGoods;

    public boolean isAnalog() {
        return "X".equals(analog);
    }

}
