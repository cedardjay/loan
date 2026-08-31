package com.finance.loan.dto.input;

import com.finance.loan.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class PayoutAccountIN {

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotBlank(message = "Account number is required")
    private String accountNumber;
}