package com.finance.loan.dto.output;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResult {
    private boolean successful;
    private String reference;
    private String errorMessage;
}