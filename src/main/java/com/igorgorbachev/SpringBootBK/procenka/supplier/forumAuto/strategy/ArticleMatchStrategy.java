package com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.strategy;

public interface ArticleMatchStrategy {
    boolean matches(String goodsArticle, String requestedArticle);

    String normalize(String input);
}
