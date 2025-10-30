package com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igorgorbachev.SpringBootBK.exception.TmtrException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TmtrRequestBuilder {

    private final ObjectMapper objectMapper;

    public String buildPreProboyRequest(String article) {
        try {
            PreProboyRequestBody requestBody = new PreProboyRequestBody(article);
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new TmtrException("Failed to build PreProboy request body: " + e.getMessage());
        }
    }

    public String buildProboyRequest(String article, String brand) {
        try {
            ProboyRequestBody requestBody = new ProboyRequestBody(article, brand);
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new TmtrException("Failed to build Proboy request body: " + e.getMessage());
        }
    }

    private static class PreProboyRequestBody {
        private String article;

        public PreProboyRequestBody(String article) {
            this.article = article;
        }

        public String getArticle() { return article; }
    }

    private static class ProboyRequestBody {
        private String article;
        private String brand;

        public ProboyRequestBody(String article, String brand) {
            this.article = article;
            this.brand = brand;
        }

        public String getArticle() { return article; }
        public String getBrand() { return brand; }
    }
}
