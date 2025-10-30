package com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.filter;

import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.config.TmtrFilterConfig;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.model.TmtrGoods;
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

        String normalizedRequestedBrand = normalizeString(requestedBrand);

        List<TmtrGoods> filtered = goods.stream()
                .filter(Objects::nonNull)
                .filter(this::isBrandMatch)
                .filter(goodsItem -> isBrandMatch(goodsItem.getBrand(), normalizedRequestedBrand))
                .filter(this::hasValidStock)
                .filter(this::hasValidPrice)
                .filter(this::isOs1)
                .sorted(Comparator.comparing(TmtrGoods::getPrice))
                .limit(filterConfig.getMaxResults())
                .collect(Collectors.toList());

        log.info("TMTR originals filtered: {} -> {} items", goods.size(), filtered.size());
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
                .sorted(Comparator.comparing(TmtrGoods::getPrice))
                .limit(filterConfig.getMaxResults())
                .collect(Collectors.toList());

        log.info("TMTR all in stock filtered: {} -> {} items", goods.size(), filtered.size());
        return filtered;
    }

    public List<TmtrGoods> filterAllGoods(List<TmtrGoods> goods) {
        if (goods == null || goods.isEmpty()) {
            return List.of();
        }

        List<TmtrGoods> filtered = goods.stream()
                .filter(Objects::nonNull)
                .filter(this::hasValidPrice)
                .sorted(Comparator.comparing(TmtrGoods::getPrice))
                .limit(filterConfig.getMaxResults())
                .collect(Collectors.toList());

        log.info("TMTR all goods filtered: {} -> {} items", goods.size(), filtered.size());
        return filtered;
    }

    private boolean isBrandMatch(TmtrGoods goods) {
        return goods.getBrand() != null && !goods.getBrand().trim().isEmpty();
    }

    private boolean isBrandMatch(String goodsBrand, String requestedBrand) {
        if (goodsBrand == null || requestedBrand == null || requestedBrand.isEmpty()) {
            return false;
        }

        String normalizedGoodsBrand = normalizeString(goodsBrand);
        return normalizedGoodsBrand.equals(requestedBrand);
    }

    private boolean hasValidStock(TmtrGoods goods) {
        if (!filterConfig.isOnlyInStock()) {
            return true;
        }
        return goods.getParsedQuantity() > 0;
    }

    private boolean hasValidPrice(TmtrGoods goods) {
        if (!filterConfig.isOnlyWithPrice()) {
            return true;
        }
        return goods.getPrice() != null && goods.getPrice() > 0;
    }

    private boolean isOs1(TmtrGoods goods) {
        if (!filterConfig.isOnlyOs1()) {
            return true;
        }
        return goods.getOs() != null && goods.getOs() == 1;
    }

    private String normalizeString(String str) {
        if (str == null) {
            return "";
        }
        return str.replaceAll("\\s+", "").toLowerCase();
    }
}
