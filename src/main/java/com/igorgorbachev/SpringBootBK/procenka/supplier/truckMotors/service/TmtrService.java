package com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.service;

import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.model.TmtrGoods;
import reactor.core.publisher.Mono;


import java.util.List;

public interface TmtrService {
    List<String> getBrands(String article);
    List<TmtrGoods> searchTmtrParts(String article, String brand);
    List<TmtrGoods> getAllTmtrGoods(String article, String brand); // новый метод
    List<PartOfferDto> getAllTmtrParts(String article, String brand);
    List<PartOfferDto> getAllTmtrPartsRaw(String article, String brand);
    Mono<List<TmtrGoods>> searchPartsReactive(String article, String brand);

}
