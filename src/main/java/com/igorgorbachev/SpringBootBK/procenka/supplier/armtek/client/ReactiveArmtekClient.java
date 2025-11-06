package com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igorgorbachev.SpringBootBK.exception.ArmtekException;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.config.ArmtekConfig;
import com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.model.ArmtekGoods;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component

public class ReactiveArmtekClient {

    private final WebClient webClient;
    private final ArmtekConfig armtekConfig;
    private final ObjectMapper objectMapper;
    private final ArmtekRequestBuilder requestBuilder;

    public ReactiveArmtekClient(@Qualifier("armtekWebClient") WebClient webClient,
                                ArmtekConfig armtekConfig,
                                ObjectMapper objectMapper,
                                ArmtekRequestBuilder requestBuilder) {
        this.webClient = webClient;
        this.armtekConfig = armtekConfig;
        this.objectMapper = objectMapper;
        this.requestBuilder = requestBuilder;
    }

    public Mono<List<ArmtekGoods>> fetchGoods(String article, String brand) {
        MultiValueMap<String, String> formData = requestBuilder.buildFormData(article, brand);

        return webClient.post()
                .uri("/api/ws_search/search?format=json")
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(armtekConfig.getTimeoutSeconds()))
                .retryWhen(Retry.fixedDelay(armtekConfig.getMaxRetryAttempts(),
                        Duration.ofSeconds(armtekConfig.getRetryDelaySeconds())))
                .map(this::parseResponse)
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("Armtek API Error - Status: {}, Response: {}",
                            e.getStatusCode(), e.getResponseBodyAsString());
                    return Mono.error(new ArmtekException("HTTP error " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString()));
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("Armtek service error: {}", e.getMessage());
                    return Mono.error(new ArmtekException("Service error: " + e.getMessage()));
                });
    }

    private List<ArmtekGoods> parseResponse(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            log.warn("Empty response from Armtek API");
            return Collections.emptyList();
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode statusNode = root.get("STATUS");
            if (statusNode != null && statusNode.asInt() != 200 && statusNode.asInt() != 208) {
                handleApiError(root);
            }

            JsonNode respNode = root.get("RESP");
            if (respNode != null && respNode.isArray() && respNode.size() > 0) {
                JsonNode firstElement = respNode.get(0);
                if (firstElement.has("MSG")) {
                    return Collections.emptyList();
                } else {
                    ArmtekGoods[] goodsArray = objectMapper.treeToValue(respNode, ArmtekGoods[].class);
                    List<ArmtekGoods> result = goodsArray != null ? Arrays.asList(goodsArray) : Collections.emptyList();
                    return result;
                }
            }

            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to parse Armtek response: {}", e.getMessage());
            throw new ArmtekException("Failed to parse response: " + e.getMessage());
        }
    }

    private void handleApiError(JsonNode root) {
        JsonNode errorNode = root.get("RESP");
        String errorMessage = "Unknown error";

        if (errorNode != null && errorNode.has("ERROR")) {
            errorMessage = errorNode.get("ERROR").asText();
        }

        JsonNode messagesNode = root.get("MESSAGES");
        if (messagesNode != null && messagesNode.isArray() && messagesNode.size() > 0) {
            errorMessage = messagesNode.get(0).get("TEXT").asText();
        }

        throw new ArmtekException("API error: " + errorMessage);
    }
}