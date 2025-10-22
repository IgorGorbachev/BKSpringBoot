package com.igorgorbachev.SpringBootBK.procenka.supplier.etsp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoodsUnit {
    private String Code;
    private String Name;
    private String ManufacturerNumber;
    private String ManufacturerName;
    private List<PriceInfo> Prices;
    private List<StockInfo> Stocks;
}
