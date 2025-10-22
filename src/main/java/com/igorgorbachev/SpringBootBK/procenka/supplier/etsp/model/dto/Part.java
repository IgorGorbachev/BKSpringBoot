package com.igorgorbachev.SpringBootBK.procenka.supplier.etsp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Part {
    private String ClientArticle;
    private String Code;
    private String CodeImagePart;
    private String Group;
    private Boolean HasPartAttendant;
    private Boolean IsAnalog;
    private Boolean IsOutside;
    private Boolean IsShipment;
    private Boolean IsShops;
    private Boolean IsSklad;
    private String Name;
    private String Note;
    private String OmegaNumber;
    private String SkubaNumber;
    private String Subgroup;
    private String UniqueNumber;
    private String manufacturerNumber;
    private String manufacturerName;

}
