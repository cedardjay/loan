package com.finance.loan.dto.output;

import com.finance.loan.entity.PaymentMethod;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PaymentAccountDTO {

    private Long id;

    private PaymentMethod paymentMethod;

    private String accountNumber;

    private Boolean isDefault;
}

