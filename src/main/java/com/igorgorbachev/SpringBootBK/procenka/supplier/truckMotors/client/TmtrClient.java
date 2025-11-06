package com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igorgorbachev.SpringBootBK.exception.TmtrException;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.config.TmtrConfig;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.model.TmtrBrand;
import com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.model.TmtrGoods;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component

public class TmtrClient {

    private final WebClient webClient;
    private final TmtrConfig tmtrConfig;
    private final ObjectMapper objectMapper;
    private final TmtrRequestBuilder requestBuilder;

    @Autowired
    public TmtrClient(@Qualifier("tmtrWebClient") WebClient webClient,
                      TmtrConfig tmtrConfig,
                      ObjectMapper objectMapper,
                      TmtrRequestBuilder requestBuilder) {
        this.webClient = webClient;
        this.tmtrConfig = tmtrConfig;
        this.objectMapper = objectMapper;
        this.requestBuilder = requestBuilder;
    }

    public List<TmtrBrand> fetchBrands(String article) {
        try {
            String requestBody = requestBuilder.buildPreProboyRequest(article);

            String responseBody = webClient.post()
                    .uri("/API.asmx/PreProboy")
                    .header("login", tmtrConfig.getLogin())
                    .header("password", tmtrConfig.getPassword())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(tmtrConfig.getTimeoutSeconds()))
                    .block();

            return parsePreProboyResponse(responseBody);

        } catch (WebClientResponseException e) {
            log.error("TMTR PreProboy API Error - Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new TmtrException("HTTP error " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("TMTR PreProboy service error: {}", e.getMessage());
            throw new TmtrException("Service error: " + e.getMessage());
        }
    }

    public List<TmtrGoods> fetchGoods(String article, String brand) {
        try {
            String requestBody = requestBuilder.buildProboyRequest(article, brand);

            log.debug("TMTR Proboy API Request - Article: {}, Brand: {}", article, brand);

            String responseBody = webClient.post()
                    .uri("/API.asmx/Proboy")
                    .header("login", tmtrConfig.getLogin())
                    .header("password", tmtrConfig.getPassword())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(tmtrConfig.getTimeoutSeconds()))
                    .block();

            return parseProboyResponse(responseBody);

        } catch (WebClientResponseException e) {
            log.error("TMTR Proboy API Error - Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new TmtrException("HTTP error " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("TMTR Proboy service error: {}", e.getMessage());
            throw new TmtrException("Service error: " + e.getMessage());
        }
    }

    private List<TmtrBrand> parsePreProboyResponse(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            log.warn("Empty response from TMTR PreProboy API");
            return Collections.emptyList();
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            if (root.isArray()) {
                TmtrBrand[] brandArray = objectMapper.treeToValue(root, TmtrBrand[].class);
                List<TmtrBrand> brands = brandArray != null ? Arrays.asList(brandArray) : Collections.emptyList();

                return brands;
            }

            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to parse TMTR PreProboy response: {}", e.getMessage());
            throw new TmtrException("Failed to parse PreProboy response: " + e.getMessage());
        }
    }

    private List<TmtrGoods> parseProboyResponse(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            log.warn("Empty response from TMTR Proboy API");
            return Collections.emptyList();
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            if (root.isArray()) {
                TmtrGoods[] goodsArray = objectMapper.treeToValue(root, TmtrGoods[].class);
                List<TmtrGoods> allGoods = goodsArray != null ? Arrays.asList(goodsArray) : Collections.emptyList();

                return allGoods;
            }

            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to parse TMTR Proboy response: {}", e.getMessage());
            throw new TmtrException("Failed to parse Proboy response: " + e.getMessage());
        }
    }
}