package com.finance.loan.utils;

import com.finance.loan.dto.output.PayoutAccountDTO;
import com.finance.loan.entity.PayoutAccount;

public class PayoutAccountUtils {

    public static PayoutAccountDTO mapEntityToOutput(PayoutAccount payoutAccount) {
        return PayoutAccountDTO.builder()
                .id(payoutAccount.getId())
                .type(payoutAccount.getType())
                .provider(payoutAccount.getProvider())
                .accountNumber(payoutAccount.getAccountNumber())
                .isDefault(payoutAccount.getIsDefault())
                .build();
    }
}
