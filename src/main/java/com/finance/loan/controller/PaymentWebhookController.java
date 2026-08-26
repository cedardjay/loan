package com.finance.loan.controller;

import com.finance.loan.dto.input.IwomiPayoutResponse;
import com.finance.loan.service.interfac.IPaymentReconciliationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks")
public class PaymentWebhookController {


    @Autowired
    private IPaymentReconciliationService paymentReconciliationService;

    @PostMapping("/payment-gateway")
    public ResponseEntity<Void> reconcileTransaction(@RequestBody IwomiPayoutResponse payload) {
        paymentReconciliationService.reconcileTransaction(payload);
        return ResponseEntity.ok().build();
    }
}
