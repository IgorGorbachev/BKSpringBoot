package com.igorgorbachev.SpringBootBK.procenka.supplier.forumAuto.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.forum-auto")
@Setter
@Getter
public class ForumAutoConfig {
    private String baseUrl;
    private String login;
    private String password;
    private int timeout = 30000;
}
