package com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Component
@ConfigurationProperties(prefix = "app.armtek")
@Setter
@Getter
public class ArmtekConfig {
    private String baseUrl;
    private String login;
    private String password;
    private String vkorg;
    private String kunnrRg;
    private int timeoutSeconds = 10;
    private int maxRetryAttempts = 3;
    private int retryDelaySeconds = 2;

    @Bean("armtekWebClient")
    public WebClient armtekWebClient(WebClient.Builder webClientBuilder) {
        String credentials = login + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        return webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Basic " + encodedCredentials)
                .defaultHeader("Content-Type", "application/x-www-form-urlencoded")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }
}
