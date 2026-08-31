package com.finance.loan.utils;

import com.finance.loan.dto.output.PaymentAccountDTO;
import com.finance.loan.entity.PaymentAccount;

public class PaymentAccountUtils {

    public static PaymentAccountDTO mapEntityToOutput(PaymentAccount payoutAccount) {
        return PaymentAccountDTO.builder()
                .id(payoutAccount.getId())
                .paymentMethod(payoutAccount.getPaymentMethod())
                .accountNumber(payoutAccount.getAccountNumber())
                .isDefault(payoutAccount.getIsDefault())
                .build();
    }
}
