package com.finance.loan.dto.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GatewayWebhookPayload {
    private String reference;  // matches paymentReference on Transaction
    private String status;     // "success" or "failed"
}