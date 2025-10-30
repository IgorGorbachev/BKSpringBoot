package com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.service;

import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.model.ArmtekGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.model.ArmtekDetailedResult;
import reactor.core.publisher.Mono;


import java.util.List;

public interface ArmtekService {
    ArmtekDetailedResult searchPartsDetailed(String article, String brand);
    List<ArmtekGoods> searchArmtekGoods(String article, String brand);
    Mono<ArmtekDetailedResult> searchPartsReactive(String article, String brand);

    // Опционально: метод для получения последних сырых данных
    List<ArmtekGoods> getLastRawGoods();
}
