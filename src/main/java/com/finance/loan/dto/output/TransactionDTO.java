package com.finance.loan.dto.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.finance.loan.entity.PayoutType;
import com.finance.loan.entity.TransactionStatus;
import com.finance.loan.entity.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDTO {
    private Long transactionId;
    private String senderName;
    private String receiverName;
    private BigDecimal amount;
    private PayoutType paymentMethod;
    private String paymentReference;
    private LocalDateTime transactionDate;
    private String description;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
}