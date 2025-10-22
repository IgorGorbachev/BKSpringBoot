package com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.service;

import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.model.ArmtekGoods;
import reactor.core.publisher.Mono;


import java.util.List;

public interface ArmtekService {
    List<ArmtekGoods> searchParts(String article, String brand, String queryType, String program);
    Mono<List<ArmtekGoods>> searchPartsReactive(String article, String brand, String queryType, String program);
}
