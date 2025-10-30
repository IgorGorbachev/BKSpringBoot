package com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.strategy;

import org.springframework.stereotype.Component;

@Component
public class RelaxedArticleMatch implements ArticleMatchStrategy {

    @Override
    public boolean matches(String goodsArticle, String requestedArticle) {
        if (goodsArticle == null || requestedArticle == null) {
            return false;
        }

        String normalizedGoods = normalize(goodsArticle);
        String normalizedRequested = normalize(requestedArticle);

        // 1. Прямое сравнение
        if (normalizedGoods.equals(normalizedRequested)) {
            return true;
        }

        // 2. Содержание цифр
        String digitsGoods = normalizedGoods.replaceAll("[^0-9]", "");
        String digitsRequested = normalizedRequested.replaceAll("[^0-9]", "");

        if (!digitsRequested.isEmpty() && digitsGoods.contains(digitsRequested)) {
            return true;
        }

        // 3. Взаимное содержание
        if (normalizedGoods.contains(normalizedRequested) ||
                normalizedRequested.contains(normalizedGoods)) {
            return true;
        }

        // 4. Точное совпадение цифр
        return digitsGoods.equals(digitsRequested) && !digitsGoods.isEmpty();
    }

    @Override
    public String normalize(String input) {
        return input != null ? input.replaceAll("\\s+", "").toLowerCase() : "";
    }
}
