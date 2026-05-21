package com.finance.loan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvestRequest {

    @NotBlank(message = "loanRequestId is required")
    private Long loanRequestId;

    @NotBlank(message = "amount is required")
    private BigDecimal amount;
}