package com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igorgorbachev.SpringBootBK.exception.ForumAutoException;
import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.config.ForumAutoConfig;
import com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.model.ForumAutoGoods;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ForumAutoClient {

    private final WebClient webClient;
    private final ForumAutoConfig forumAutoConfig;
    private final ObjectMapper objectMapper;


    @Autowired
    public ForumAutoClient(@Qualifier("forumAutoWebClient") WebClient webClient,
                           ForumAutoConfig forumAutoConfig,
                           ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.forumAutoConfig = forumAutoConfig;
        this.objectMapper = objectMapper;
    }

    public List<ForumAutoGoods> fetchGoods(String article, String brand, Boolean cross) {
        try {

            String responseBody = webClient.get()
                    .uri(uriBuilder -> buildUri(uriBuilder, article, brand, cross))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            return parseResponse(responseBody);
        } catch (Exception e) {
            log.error("API request failed: {}", e.getMessage());
            return List.of();
        }
    }

    private List<ForumAutoGoods> parseResponse(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return List.of();
        }

        try {
            // Пытаемся разобрать как массив
            ForumAutoGoods[] goodsArray = objectMapper.readValue(responseBody, ForumAutoGoods[].class);
            return Arrays.asList(goodsArray);
        } catch (Exception e) {
            // Если не получилось как массив, пробуем как объект с полем goods
            return parseAsObjectWithGoods(responseBody);
        }
    }

    private List<ForumAutoGoods> parseAsObjectWithGoods(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode goodsNode = root.get("goods");

            if (goodsNode != null && goodsNode.isArray()) {
                ForumAutoGoods[] goodsArray = objectMapper.treeToValue(goodsNode, ForumAutoGoods[].class);
                return Arrays.asList(goodsArray);
            }

            JsonNode errorNode = root.get("error");
            if (errorNode != null) {
                throw new ForumAutoException("API Error: " + errorNode.asText());
            }

            return List.of();
        } catch (ForumAutoException e) {
            throw e;
        } catch (Exception e) {
            throw new ForumAutoException("JSON parsing error: " + e.getMessage());
        }
    }

    private URI buildUri(UriBuilder uriBuilder, String article, String brand, Boolean cross) {
        UriBuilder builder = uriBuilder
                .path("/listGoods")
                .queryParam("login", forumAutoConfig.getLogin())
                .queryParam("pass", forumAutoConfig.getPassword());

        if (article != null && !article.trim().isEmpty()) {
            builder.queryParam("art", article.trim());
        }
        if (brand != null && !brand.trim().isEmpty()) {
            builder.queryParam("br", brand.trim());
        }
        if (cross != null) {
            builder.queryParam("cross", cross ? 1 : 0);
        }

        return builder.build();
    }
}