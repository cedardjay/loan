package com.finance.loan.dto.output;

import com.finance.loan.entity.TransactionStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentGatewayResponse {
    private String externalId; //
    private String internalId;
    private String message;
    private TransactionStatus status; //pending, completed, failed
}