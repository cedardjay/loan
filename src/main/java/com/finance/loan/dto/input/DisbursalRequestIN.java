package com.finance.loan.dto.input;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DisbursalRequestIN {

    @NotNull(message = "Loan request id is required")
    private Long loanRequestId;

    @NotNull(message = "Payout account id is required")
    private Long payoutAccountId;
}