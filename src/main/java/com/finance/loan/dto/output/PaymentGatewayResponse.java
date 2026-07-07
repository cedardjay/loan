package com.finance.loan.dto.output;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentGatewayResponse {
    private boolean accepted;
    private String reference;
    private String errorMessage;
}