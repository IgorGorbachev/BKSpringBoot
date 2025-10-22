package com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.service;



import com.igorgorbachev.SpringBootBK.procenka.supplier.favorite.model.FavoritePartsGoods;

import java.util.List;

public interface FavoriteService {
    List<FavoritePartsGoods> getPrice(String number, String brand, Boolean analogues, Boolean info);
}
