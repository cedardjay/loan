package com.finance.loan.service.interfac;

import com.finance.loan.dto.input.PayoutAccountIN;
import com.finance.loan.dto.output.PaymentAccountDTO;

public interface IPayoutAccountService {
    PaymentAccountDTO createPayoutAccount(PayoutAccountIN payoutAccountIN, String email) ;

    PaymentAccountDTO getPayoutAccount(String email);

    PaymentAccountDTO getPayoutAccountByUserId(Long userId);

    PaymentAccountDTO getDefaultPayoutAccount(String email);
}
