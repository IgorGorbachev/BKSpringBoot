package com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.service.impl;

import com.igorgorbachev.SpringBootBK.procenka.dto.PartOfferDto;
import com.igorgorbachev.SpringBootBK.procenka.service.SupplierService;
import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.client.ForumAutoClient;
import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.converter.ForumAutoConverter;
import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.filter.ForumAutoFilter;
import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.model.ForumAutoGoods;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForumAutoServiceImpl implements SupplierService {

    private final ForumAutoClient forumAutoClient;
    private final ForumAutoFilter forumAutoFilter;
    private final ForumAutoConverter forumAutoConverter;

    @Override
    public String getSupplierName() {
        return "Forum-Auto";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public List<PartOfferDto> searchParts(String article, String brand) {
        try {
            log.info("ForumAuto searching parts: article='{}', brand='{}'", article, brand);

            List<ForumAutoGoods> allGoods = forumAutoClient.fetchGoods(article, brand, false);
            List<ForumAutoGoods> originals = forumAutoFilter.filterOriginals(allGoods, article, brand);
            List<PartOfferDto> result = forumAutoConverter.toPartOfferDtos(originals);

            log.info("ForumAuto parts search completed: {} originals found", result.size());
            return result;

        } catch (Exception e) {
            log.error("Error searching parts in Forum-Auto for article: {}, brand: {}. Error: {}",
                    article, brand, e.getMessage(), e);
            return List.of();
        }
    }

    public List<PartOfferDto> searchAnalogues(String article, String brand) {
        try {
            log.info("ForumAuto searching analogues: article='{}', brand='{}'", article, brand);

            List<ForumAutoGoods> allGoods = forumAutoClient.fetchGoods(article, brand, true);
            List<ForumAutoGoods> filteredAnalogues = forumAutoFilter.filterAnalogues(allGoods);
            List<ForumAutoGoods> prioritizedAnalogues = forumAutoFilter.prioritizeAnalogues(filteredAnalogues);
            List<PartOfferDto> result = forumAutoConverter.toPartOfferDtos(prioritizedAnalogues);

            log.info("ForumAuto analogues search completed: {} analogues found", result.size());

            return result;

        } catch (Exception e) {
            log.error("Error searching analogues in Forum-Auto for article: {}, brand: {}. Error: {}",
                    article, brand, e.getMessage(), e);
            return List.of();
        }
    }


}