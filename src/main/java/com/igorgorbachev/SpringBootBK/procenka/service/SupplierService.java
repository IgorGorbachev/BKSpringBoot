package com.igorgorbachev.SpringBootBK.procenka.service;




import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;

import java.util.List;

public interface SupplierService {

    String getSupplierName();

    List<PartOfferDto> searchParts(String article, String brand);

    boolean isAvailable();
}
