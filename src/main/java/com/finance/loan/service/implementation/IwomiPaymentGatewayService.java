package com.finance.loan.service.implementation;

import com.finance.loan.dto.input.IwomiPayoutResponse;
import com.finance.loan.dto.internal.PaymentPayoutRequest;
import com.finance.loan.dto.output.IwomiPayoutRequest;
import com.finance.loan.dto.internal.PaymentGatewayResponse;
import com.finance.loan.entity.TransactionStatus;
import com.finance.loan.service.interfac.IPaymentGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class IwomiPaymentGatewayService implements IPaymentGatewayService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IwomiPayTokenManager tokenManager;

    @Value("${payment.gateway.base-url}")
    private String baseUrl;

    @Value("${payment.gateway.callback-url:http://localhost:8081/webhooks/payment-gateway}") //spaceholder actually for now
    private String callbackUrl;

    @Autowired
    private IwomiGatewayCredentials credentials;

    @Override
    public PaymentGatewayResponse makePayment(PaymentPayoutRequest payload) {

        // --- BUILD RAW PAYLOAD ---
        IwomiPayoutRequest body = IwomiPayoutRequest.builder()
                .opType(String.valueOf(payload.getOperationType()).toLowerCase())
                .type(String.valueOf(payload.getPaymentMethod()).toLowerCase())
                .amount(payload.getAmount().toString())
                .externalId(payload.getExternalId())
                .motif(payload.getMotif())
                .tel(payload.getTel())
                .country(payload.getCountry())
                .callbackUrl(callbackUrl)
                .build();


        // --- HEADERS (with token) ---
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(tokenManager.getToken());
        headers.set("AccountKey", buildAccountKey(String.valueOf(payload.getPaymentMethod()).toLowerCase()));

        HttpEntity<IwomiPayoutRequest> request = new HttpEntity<>(body, headers);

        // --- CALL ---
        ResponseEntity<IwomiPayoutResponse> response = restTemplate.postForEntity(
                baseUrl + "/iwomipay", request, IwomiPayoutResponse.class);

        IwomiPayoutResponse raw = response.getBody();

        return PaymentGatewayResponse.builder()
                .externalId(raw != null ? raw.getExternalId() : null)
                .internalId(raw != null ? raw.getInternalId() : null)
                .message(raw != null ? raw.getMessage() : "No response from gateway")
                .status(mapStatus(raw != null ? raw.getStatus() : null))
                .build();
    }


    public String buildAccountKey(String type) {
        IwomiGatewayCredentials.TypeCredentials creds = credentials.getTypes().get(type);

        if (creds == null) {
            throw new IllegalStateException("No gateway credentials configured for type: " + type);
        }

        String raw = creds.getApiKey() + ":" + creds.getApiSecret();
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public TransactionStatus mapStatus(String gatewayStatus) {
        if (gatewayStatus == null) {
            return TransactionStatus.FAILED;
        }

        return switch (gatewayStatus) {
            case "01" -> TransactionStatus.COMPLETED;
            case "1000" -> TransactionStatus.PENDING;
            default -> TransactionStatus.FAILED;
        };
    }


    @Override
    public IwomiPayoutResponse checkStatus(String internalId) {

        ResponseEntity<IwomiPayoutResponse> response = restTemplate.getForEntity(
                baseUrl + "/iwomipayStatus/" + internalId,
                IwomiPayoutResponse.class
        );

        return response.getBody();
    }
}



