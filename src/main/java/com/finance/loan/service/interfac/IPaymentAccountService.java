package com.finance.loan.service.interfac;

import com.finance.loan.dto.input.PayoutAccountIN;
import com.finance.loan.dto.output.PaymentAccountDTO;

import java.util.List;

public interface IPaymentAccountService {
    PaymentAccountDTO createPayoutAccount(PayoutAccountIN payoutAccountIN, String email) ;

    PaymentAccountDTO getPayoutAccountByUserId(Long userId);

    PaymentAccountDTO getDefaultPayoutAccount(String email);

    List<PaymentAccountDTO> getPaymentAccounts(String email);
}
