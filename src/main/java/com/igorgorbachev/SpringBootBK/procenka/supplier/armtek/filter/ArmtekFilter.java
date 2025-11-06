package com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.filter;

import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.config.ArmtekFilterConfig;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.model.ArmtekGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.service.ArmtekDataProcessor;
import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.strategy.ArticleMatchStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArmtekFilter {

    private final ArmtekFilterConfig filterConfig;
    private final ArticleMatchStrategy articleMatchStrategy;
    private final ArmtekDataProcessor dataProcessor;

    public List<ArmtekGoods> filterOriginals(List<ArmtekGoods> allGoods, String article, String brand) {
        if (allGoods == null || allGoods.isEmpty()) {
            return Collections.emptyList();
        }

        List<ArmtekGoods> filtered = allGoods.stream()
                .filter(Objects::nonNull)
                .filter(this::applyBaseFilters)
                .filter(goods -> isBrandMatch(goods, brand))
                .filter(goods -> isArticleMatch(goods, article))
                .collect(Collectors.toList());

        return sortGoods(filtered);
    }

    private boolean applyBaseFilters(ArmtekGoods goods) {
        boolean valid = true;

        if (filterConfig.isExcludeAnalogs()) {
            valid = valid && !goods.isAnalog();
        }
        if (filterConfig.isOnlyWithPrice()) {
            valid = valid && dataProcessor.hasValidPrice(goods);
        }
        if (filterConfig.isOnlyInStock()) {
            valid = valid && dataProcessor.hasValidStock(goods);
        }

        return valid && goods.getPin() != null && !goods.getPin().trim().isEmpty();
    }

    private boolean isBrandMatch(ArmtekGoods goods, String requestedBrand) {
        if (requestedBrand == null || requestedBrand.trim().isEmpty()) {
            return true;
        }
        if (goods.getBrand() == null) {
            return false;
        }

        String normalizedGoodsBrand = articleMatchStrategy.normalize(goods.getBrand());
        String normalizedRequestedBrand = articleMatchStrategy.normalize(requestedBrand);

        return normalizedGoodsBrand.contains(normalizedRequestedBrand) ||
                normalizedRequestedBrand.contains(normalizedGoodsBrand);
    }

    private boolean isArticleMatch(ArmtekGoods goods, String requestedArticle) {
        return articleMatchStrategy.matches(goods.getPin(), requestedArticle);
    }

    private List<ArmtekGoods> sortGoods(List<ArmtekGoods> goods) {
        return goods.stream()
                .sorted(Comparator
                        .comparing((ArmtekGoods g) -> g.getPrice() != null ? g.getPrice() : Double.MAX_VALUE)
                        .thenComparing(g -> dataProcessor.parseQuantity(g), Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }
}