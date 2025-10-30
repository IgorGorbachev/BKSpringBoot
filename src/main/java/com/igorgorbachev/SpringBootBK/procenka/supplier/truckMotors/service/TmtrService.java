package com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.service;

import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;


import java.util.List;

public interface TmtrService {
    List<String> getBrands(String article);
    List<PartOfferDto> getAllTmtrParts(String article, String brand);
    String getLastRawResponse();
}
