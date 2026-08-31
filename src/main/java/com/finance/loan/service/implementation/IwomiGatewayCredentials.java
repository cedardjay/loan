package com.finance.loan.service.implementation;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "payment.gateway")
@Data
public class IwomiGatewayCredentials {
    private Map<String, TypeCredentials> types;

    @Data
    public static class TypeCredentials {
        private String apiKey;
        private String apiSecret;
    }
}
