package com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@ConfigurationProperties(prefix = "app.tm")
@Setter
@Getter
public class TmtrConfig {
    private String baseUrl;
    private String login;
    private String password;
    private int timeoutSeconds = 30;


    @Bean("tmtrWebClient")
    public WebClient tmtrWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "text/plain")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }
}
