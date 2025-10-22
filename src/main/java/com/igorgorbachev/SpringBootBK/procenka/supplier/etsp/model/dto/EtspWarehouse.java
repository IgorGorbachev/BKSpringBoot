package com.igorgorbachev.SpringBootBK.procenka.supplier.etsp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EtspWarehouse {
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private LocalDateTime shipmentDate;
    private Boolean isOwn;
}
