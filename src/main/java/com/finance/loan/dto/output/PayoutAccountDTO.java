package com.finance.loan.dto.output;

import com.finance.loan.entity.PayoutType;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PayoutAccountDTO {

    private Long id;

    private PayoutType type;

    private String accountNumber;

    private Boolean isDefault;
}

