package com.finance.loan.service.implementation;

import com.finance.loan.dto.input.IwomiAuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class IwomiPayTokenManager {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${payment.gateway.base-url}")
    private String baseUrl;

    @Value("${payment.gateway.username}")
    private String username;

    @Value("${payment.gateway.password}")
    private String password;

    private String cachedToken;
    private Instant expiresAt;

    public synchronized String getToken() {
        if (cachedToken == null || Instant.now().isAfter(expiresAt)) {
            refreshToken();
        }
        return cachedToken;
    }

    private void refreshToken() {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<IwomiAuthResponse> response = restTemplate.postForEntity(
                baseUrl + "/authenticate", request, IwomiAuthResponse.class);

        IwomiAuthResponse authResponse = response.getBody();

        if (authResponse == null || authResponse.getToken() == null) {
            throw new IllegalStateException("Gateway authentication failed: " +
                    (authResponse != null ? authResponse.getMessage() : "no response body"));
        }
        this.cachedToken = authResponse.getToken();
        this.expiresAt = Instant.now().plus(Duration.ofMinutes(1000)); // adjust — see note below
    }
}