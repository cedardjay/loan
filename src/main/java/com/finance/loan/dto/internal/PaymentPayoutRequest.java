package com.finance.loan.dto.internal;

import com.finance.loan.entity.OperationType;
import com.finance.loan.entity.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentPayoutRequest {
    private OperationType operationType;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private String externalId;
    private String motif;
    private String tel;
    private String country;
}