package com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArmtekDetailedResult {
    private List<ArmtekGoods> filteredGoods;
    private List<ArmtekGoods> rawGoods;
    private String searchArticle;
    private String searchBrand;
    private int totalRawResults;
    private int totalFilteredResults;

    public ArmtekDetailedResult(List<ArmtekGoods> filteredGoods, List<ArmtekGoods> rawGoods,
                                String searchArticle, String searchBrand) {
        this.filteredGoods = filteredGoods != null ? filteredGoods : Collections.emptyList();
        this.rawGoods = rawGoods != null ? rawGoods : Collections.emptyList();
        this.searchArticle = searchArticle;
        this.searchBrand = searchBrand;
        this.totalRawResults = this.rawGoods.size();
        this.totalFilteredResults = this.filteredGoods.size();
    }
}