package com.finance.loan.controller;

import com.finance.loan.dto.input.GatewayWebhookPayload;
import com.finance.loan.service.implementation.PaymentReconciliationService;
import com.finance.loan.service.implementation.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class PaymentWebhookController {

    //THIS IS JUST MOCK IMPLEMENTATION

    @Autowired
    private PaymentReconciliationService paymentReconciliationService;

    @PostMapping("/payment-gateway")
    public ResponseEntity<Void> handleGatewayWebhook(@RequestBody GatewayWebhookPayload payload) {
        paymentReconciliationService.handleGatewayWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
