package com.igorgorbachev.SpringBootBK.procenka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    private List<PartOfferDto> exactMatches;
    private List<PartOfferDto> forumAutoOffers;
    private List<PartOfferDto> favoritePartsOffers;
    private List<PartOfferDto> armtekOffers;
    private List<PartOfferDto> armtekRawOffers;
    private List<PartOfferDto> tmtrOffers;
    private List<PartOfferDto> processedOffers;
    private int totalOffersCount;
}
