package com.finance.loan.dto.input;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LoanRequestIN {

    @NotNull(message = "Requested amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal requestedAmount;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "purpose is required")
    private String purpose;

    @NotNull(message = "Term months is required")
    @Positive(message = "Term months must be positive")
    private Integer termMonths;

}