package com.igorgorbachev.SpringBootBK.procenka.supplier.etsp.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "app.etsp")
public class EtspConfig {
    private String username;
    private String password;
    private String baseUrl = "https://ws.etsp.ru";
    private String hashSession;
}