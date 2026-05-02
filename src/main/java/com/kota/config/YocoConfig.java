package com.kota.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.yoco")
public class YocoConfig {

    private String secretKey;
    private String checkoutUrl;
    private String refundUrl;
    private String webhookSecret;

    @Bean(name = "yocoRestTemplate")
    public RestTemplate yocoRestTemplate() {
        return new RestTemplate();
    }
}
