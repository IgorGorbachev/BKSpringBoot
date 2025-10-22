package com.igorgorbachev.SpringBootBK.procenka.supplier.etsp.service;

import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;

import java.util.List;

public interface EtspService {

    List<PartOfferDto> searchPartsAsOffers(String article, String brand);

    String getSupplierName();

    boolean isAvailable();
}
