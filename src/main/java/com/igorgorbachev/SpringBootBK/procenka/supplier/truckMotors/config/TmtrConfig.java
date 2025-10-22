package com.igorgorbachev.SpringBootBK.procenka.supplier.truckMotors.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.tm")
@Setter
@Getter
public class TmtrConfig {
    private String baseUrl;
    private String login;
    private String password;
    private int timeoutSeconds = 30;
}
