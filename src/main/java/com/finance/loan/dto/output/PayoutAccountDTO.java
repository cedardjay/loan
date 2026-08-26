package com.finance.loan.dto.output;

import com.finance.loan.entity.PaymentMethod;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PayoutAccountDTO {

    private Long id;

    private PaymentMethod type;

    private String accountNumber;

    private Boolean isDefault;
}

