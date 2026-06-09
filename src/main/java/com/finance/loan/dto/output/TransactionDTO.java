package com.finance.loan.dto.output;

import com.finance.loan.entity.TransactionStatus;
import com.finance.loan.entity.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private Long transactionId;
    private String senderName;
    private String receiverName;
    private BigDecimal amount;
    private String paymentMethod;
    private LocalDateTime transactionDate;
    private String description;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
}