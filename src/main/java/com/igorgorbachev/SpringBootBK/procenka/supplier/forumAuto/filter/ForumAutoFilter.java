package com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.filter;

import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.config.ForumAutoFilterConfig;
import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.model.ForumAutoGoods;
import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.strategy.ArticleMatchStrategy;
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
public class ForumAutoFilter {

    private final ForumAutoFilterConfig filterConfig;
    private final ArticleMatchStrategy articleMatchStrategy;

    public List<ForumAutoGoods> filterOriginals(List<ForumAutoGoods> goods, String requestedArticle, String requestedBrand) {
        if (goods == null || goods.isEmpty()) {
            return List.of();
        }

        log.info("=== DIAGNOSTICS: Checking each goods ===");
        goods.stream()
                .filter(Objects::nonNull)
                .forEach(good -> {
                    boolean hasStock = hasValidStock(good);
                    boolean hasPrice = hasValidPrice(good);
                    boolean isOriginal = isOriginalGood(good, requestedArticle, requestedBrand);

                    log.info("Goods: art='{}', brand='{}', stock={}, price={}, isOriginal={}, PASS_ALL={}",
                            good.getArt(), good.getBrand(), hasStock, hasPrice, isOriginal,
                            (hasStock && hasPrice && isOriginal));
                });

        List<ForumAutoGoods> filtered = goods.stream()
                .filter(Objects::nonNull)
                .filter(this::hasValidStock)
                .filter(this::hasValidPrice)
                .filter(good -> isOriginalGood(good, requestedArticle, requestedBrand))
                .sorted(Comparator.comparing(ForumAutoGoods::getPrice))
                .limit(filterConfig.getMaxOriginals())
                .collect(Collectors.toList());

        logFilterResults("originals", goods.size(), filtered.size());

        // Дополнительно логируем какие товары прошли фильтрацию
        log.info("=== FILTERED GOODS ===");
        filtered.forEach(good ->
                log.info(" - art='{}', brand='{}'", good.getArt(), good.getBrand())
        );

        return filtered;
    }

    public List<ForumAutoGoods> filterAnalogues(List<ForumAutoGoods> goods) {
        if (goods == null || goods.isEmpty()) {
            return List.of();
        }

        log.info("Filtering analogues from {} total goods", goods.size());

        List<ForumAutoGoods> filtered = goods.stream()
                .filter(Objects::nonNull)
                .filter(this::hasValidPrice)
                .filter(this::isInAllowedWarehouse)
                .filter(good -> filterConfig.isIncludeOutOfStockAnalogues() || hasValidStock(good))
                .sorted(Comparator.comparing(ForumAutoGoods::getPrice))
                .collect(Collectors.toList());

        logFilterResults("analogues", goods.size(), filtered.size());
        return filtered;
    }

    public List<ForumAutoGoods> prioritizeAnalogues(List<ForumAutoGoods> analogues) {
        if (analogues == null || analogues.isEmpty()) {
            return List.of();
        }

        if (!filterConfig.isPrioritizeReturnable()) {
            return analogues;
        }

        // Группируем по наличию и возвратности
        List<ForumAutoGoods> inStockReturnable = analogues.stream()
                .filter(this::hasValidStock)
                .filter(ForumAutoGoods::isReturnable)
                .collect(Collectors.toList());

        List<ForumAutoGoods> outOfStockReturnable = analogues.stream()
                .filter(good -> !hasValidStock(good))
                .filter(ForumAutoGoods::isReturnable)
                .collect(Collectors.toList());

        List<ForumAutoGoods> outOfStockNonReturnable = analogues.stream()
                .filter(good -> !hasValidStock(good))
                .filter(good -> !good.isReturnable())
                .collect(Collectors.toList());

        List<ForumAutoGoods> result = inStockReturnable;
        result.addAll(outOfStockReturnable);
        result.addAll(outOfStockNonReturnable);

        log.info("Analogues prioritized: {} in stock returnable, {} out of stock returnable, {} out of stock non-returnable",
                inStockReturnable.size(), outOfStockReturnable.size(), outOfStockNonReturnable.size());

        return result;
    }

    private boolean isOriginalGood(ForumAutoGoods good, String requestedArticle, String requestedBrand) {
        boolean articleMatches = articleMatchStrategy.matches(good.getArt(), requestedArticle);
        boolean brandMatches = isBrandMatch(good.getBrand(), requestedBrand);

        log.debug("Original check - Article: {} -> {}, Brand: {} -> {}",
                good.getArt(), articleMatches, good.getBrand(), brandMatches);

        return articleMatches && brandMatches;
    }

    private boolean isBrandMatch(String goodsBrand, String requestedBrand) {
        if (requestedBrand == null) {
            return true;
        }
        if (goodsBrand == null) {
            return false;
        }

        String normalizedGoods = articleMatchStrategy.normalize(goodsBrand);
        String normalizedRequested = articleMatchStrategy.normalize(requestedBrand);

        return normalizedGoods.equals(normalizedRequested);
    }

    private boolean hasValidStock(ForumAutoGoods good) {
        return good.getQuantity() != null && good.getQuantity() > 0;
    }

    private boolean hasValidPrice(ForumAutoGoods good) {
        return good.getPrice() != null && good.getPrice() > 0;
    }

    private boolean isInAllowedWarehouse(ForumAutoGoods good) {
        return good.getWarehouse() != null &&
                filterConfig.getAllowedWarehouses().contains(good.getWarehouse());
    }

    private void logFilterResults(String type, int total, int filtered) {
        log.info("{} filtering: {} -> {} items", type, total, filtered);
    }
}
