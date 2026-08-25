package com.finance.loan.dto.input;

import com.finance.loan.entity.PayoutType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class PayoutAccountIN {

    @NotNull(message = "Payout type is required")
    private PayoutType type;

    @NotBlank(message = "Account number is required")
    private String accountNumber;
}