package com.finance.loan.dto.output;

import com.finance.loan.entity.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;


@Data
@Builder
public class LoanPaymentResult {
    private Long loanId;
    private BigDecimal paymentAmount;
    private TransactionStatus status; //
    private String paymentReference;
}