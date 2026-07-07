package com.finance.loan.dto.output;

import com.finance.loan.entity.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class LoanDisbursementResult {
    private Long loanId;
    private String borrowerEmail;
    private BigDecimal amount;
    private String paymentReference;
    private TransactionStatus status;  // always PENDING at this point until payment gateway webhook fires back
}