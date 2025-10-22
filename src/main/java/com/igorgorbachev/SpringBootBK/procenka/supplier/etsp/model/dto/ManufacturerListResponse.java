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
public class ManufacturerListResponse {
    private List<String> Errors;
    private Boolean Success;
    private List<String> Warnings;
    private List<Manufacturer> Data;
}
