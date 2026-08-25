package com.finance.loan.dto.internal;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentPayoutRequest {
    private String opType;
    private String type;
    private BigDecimal amount;
    private String externalId;
    private String motif;
    private String tel;
    private String country;
}