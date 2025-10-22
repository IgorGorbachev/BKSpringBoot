package com.igorgorbachev.SpringBootBK.procenka.supplier.armtek.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "app.armtek")
@Setter
@Getter
public class ArmtekConfig {
    private String baseUrl = "http://ws.armtek.ru";
    private String username = "ZAP37RUS@MAIL.RU";
    private String password = "ZRaBhez4"; // Убедитесь что это новый пароль!
    private String vkorg = "4000";
    private String kunnrRg = "43286514";
    private Integer timeoutSeconds = 30;
}
