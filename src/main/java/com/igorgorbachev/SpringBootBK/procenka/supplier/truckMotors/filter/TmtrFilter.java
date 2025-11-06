package com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.filter;

import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.config.TmtrFilterConfig;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.model.TmtrGoods;
import com.igorgorbachev.SpringBootBK.procenka.util.StringNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmtrFilter {

    private final TmtrFilterConfig filterConfig;

    public List<TmtrGoods> filterOriginals(List<TmtrGoods> goods, String requestedArticle, String requestedBrand) {
        if (goods == null || goods.isEmpty()) {
            return List.of();
        }

        String normalizedRequestedArticle = StringNormalizer.normalizeArticle(requestedArticle);
        String normalizedRequestedBrand = StringNormalizer.normalizeBrand(requestedBrand);

        log.debug("TMTR filtering originals - Requested: article='{}', brand='{}'. Total goods: {}",
                requestedArticle, requestedBrand, goods.size());

        List<TmtrGoods> filtered = goods.stream()
                .filter(Objects::nonNull)
                .filter(this::hasValidBrand)
                .filter(goodsItem -> isBrandMatch(goodsItem.getBrand(), normalizedRequestedBrand))
                .filter(goodsItem -> isArticleMatch(goodsItem.getNumber(), normalizedRequestedArticle))
                .filter(this::hasValidStock)
                .filter(this::hasValidPrice)
                .filter(this::isOs1)
                .filter(this::hasValidDeliveryProbability)
                .sorted(buildComparator())
                .limit(filterConfig.getMaxResults())
                .collect(Collectors.toList());

        logFilterDetails(goods, filtered);

        return filtered;
    }

    public List<TmtrGoods> filterAllInStock(List<TmtrGoods> goods) {
        if (goods == null || goods.isEmpty()) {
            return List.of();
        }

        List<TmtrGoods> filtered = goods.stream()
                .filter(Objects::nonNull)
                .filter(this::hasValidStock)
                .filter(this::hasValidPrice)
                .filter(this::isOs1)
                .filter(this::hasValidDeliveryProbability)
                .sorted(buildComparator())
                .limit(filterConfig.getMaxResults())
                .collect(Collectors.toList());

        return filtered;
    }

    public List<TmtrGoods> filterAllGoods(List<TmtrGoods> goods) {
        if (goods == null || goods.isEmpty()) {
            return List.of();
        }

        List<TmtrGoods> filtered = goods.stream()
                .filter(Objects::nonNull)
                .filter(this::hasValidPrice)
                .filter(this::hasValidDeliveryProbability)
                .sorted(buildComparator())
                .limit(filterConfig.getMaxResults())
                .collect(Collectors.toList());

        return filtered;
    }

    public List<TmtrGoods> filterWithCustomRules(List<TmtrGoods> goods,
                                                 String requestedArticle,
                                                 String requestedBrand,
                                                 boolean requireExactMatch) {
        if (goods == null || goods.isEmpty()) {
            return List.of();
        }

        String normalizedRequestedArticle = StringNormalizer.normalizeArticle(requestedArticle);
        String normalizedRequestedBrand = StringNormalizer.normalizeBrand(requestedBrand);

        return goods.stream()
                .filter(Objects::nonNull)
                .filter(this::hasValidBrand)
                .filter(goodsItem -> isBrandMatch(goodsItem.getBrand(), normalizedRequestedBrand))
                .filter(goodsItem -> requireExactMatch ?
                        isExactArticleMatch(goodsItem.getNumber(), normalizedRequestedArticle) :
                        isArticleMatch(goodsItem.getNumber(), normalizedRequestedArticle))
                .filter(this::hasValidStock)
                .filter(this::hasValidPrice)
                .filter(this::isOs1)
                .filter(this::hasValidDeliveryProbability)
                .sorted(buildComparator())
                .limit(filterConfig.getMaxResults())
                .collect(Collectors.toList());
    }

    private boolean isArticleMatch(String goodsArticle, String requestedArticle) {
        if (goodsArticle == null || requestedArticle == null || requestedArticle.isEmpty()) {
            return false;
        }

        if (filterConfig.useStrictArticleMatch()) {
            return isExactArticleMatch(goodsArticle, requestedArticle);
        }

        String normalizedGoodsArticle = StringNormalizer.normalizeArticle(goodsArticle);

        // 1. Прямое сравнение
        if (normalizedGoodsArticle.equals(requestedArticle)) {
            return true;
        }

        // 2. Сравнение только цифр
        return StringNormalizer.isDigitsMatch(goodsArticle, requestedArticle);
    }

    private boolean isExactArticleMatch(String goodsArticle, String requestedArticle) {
        if (goodsArticle == null || requestedArticle == null) {
            return false;
        }
        String normalizedGoodsArticle = StringNormalizer.normalizeArticle(goodsArticle);
        return normalizedGoodsArticle.equals(requestedArticle);
    }

    private boolean isBrandMatch(String goodsBrand, String requestedBrand) {
        if (goodsBrand == null || requestedBrand == null || requestedBrand.isEmpty()) {
            return false;
        }

        String normalizedGoodsBrand = StringNormalizer.normalizeBrand(goodsBrand);
        return normalizedGoodsBrand.equals(requestedBrand);
    }

    private boolean hasValidBrand(TmtrGoods goods) {
        return goods.getBrand() != null && !goods.getBrand().trim().isEmpty();
    }

    private boolean hasValidStock(TmtrGoods goods) {
        if (!filterConfig.shouldFilterStock()) {
            return true;
        }
        return goods.getParsedQuantity() > 0;
    }

    private boolean hasValidPrice(TmtrGoods goods) {
        if (!filterConfig.shouldFilterPrice()) {
            return true;
        }
        return goods.getPrice() != null && goods.getPrice() > 0;
    }

    private boolean isOs1(TmtrGoods goods) {
        if (!filterConfig.shouldFilterByOs()) {
            return true;
        }
        return goods.getOs() != null && goods.getOs() == 1;
    }

    private boolean hasValidDeliveryProbability(TmtrGoods goods) {
        if (!filterConfig.shouldFilterByDeliveryProbability()) {
            return true;
        }
        return goods.getDeliveryProbability() != null &&
                goods.getDeliveryProbability() >= filterConfig.getMinDeliveryProbability();
    }

    private Comparator<TmtrGoods> buildComparator() {
        return Comparator
                .comparing((TmtrGoods goods) -> goods.getPrice() != null ? goods.getPrice() : Double.MAX_VALUE)
                .thenComparing(goods -> goods.getParsedQuantity() != null ? goods.getParsedQuantity() : 0, Comparator.reverseOrder())
                .thenComparing(goods -> goods.getDeliveryProbability() != null ? goods.getDeliveryProbability() : 0.0, Comparator.reverseOrder());
    }

    private void logFilterDetails(List<TmtrGoods> original, List<TmtrGoods> filtered) {
        if (log.isDebugEnabled()) {
            long withStock = original.stream().filter(this::hasValidStock).count();
            long withPrice = original.stream().filter(this::hasValidPrice).count();
            long os1 = original.stream().filter(this::isOs1).count();

            log.debug("TMTR filter details - Total: {}, WithStock: {}, WithPrice: {}, OS1: {}, Filtered: {}",
                    original.size(), withStock, withPrice, os1, filtered.size());
        }
    }
}
