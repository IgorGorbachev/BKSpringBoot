package com.igorgorbachev.SpringBootBK.procenka.supplier.etsp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriceInfo {
    private BigDecimal Price;
    private String Currency;
    private Integer Quantity;
    private String PriceType;
}
