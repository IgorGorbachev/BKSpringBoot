package com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.service;

import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.model.ForumAutoGoods;
import reactor.core.publisher.Mono;


import java.util.List;

public interface ForumAutoService{
    List<ForumAutoGoods> listGoods(String article, String brand, Boolean cross, String gid);
    Mono<List<ForumAutoGoods>> listGoodsReactive(String article, String brand, Boolean cross, String gid);
    Mono<String> getClientInfo();
}
