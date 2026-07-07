package com.finance.loan.dto.output;

import com.finance.loan.entity.ScheduleStatus;
import com.finance.loan.entity.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class LoanPaymentResult {
    private Long scheduleId;
    private int installmentNumber;
    private BigDecimal amountPaid;
    private TransactionStatus newStatus; //
    private LocalDate paidDate;
    private String paymentReference;
    private boolean loanCompleted;
}