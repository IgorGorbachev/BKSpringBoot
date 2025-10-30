package com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.converter;

import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.model.ForumAutoGoods;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ForumAutoConverter {

    public PartOfferDto toPartOfferDto(ForumAutoGoods goods) {
        return new PartOfferDto(goods);
    }

    public List<PartOfferDto> toPartOfferDtos(List<ForumAutoGoods> goods) {
        return goods.stream()
                .map(this::toPartOfferDto)
                .collect(Collectors.toList());
    }
}
