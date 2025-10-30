package com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@ConfigurationProperties(prefix = "app.forum-auto")
@Setter
@Getter
public class ForumAutoConfig {
    private String baseUrl;
    private String login;
    private String password;
    private int timeout = 30000;


    @Bean("forumAutoWebClient")
    public WebClient forumAutoWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

}
