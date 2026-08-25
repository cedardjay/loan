package com.finance.loan.service.interfac;

import com.finance.loan.dto.input.PayoutAccountIN;
import com.finance.loan.dto.output.PayoutAccountDTO;

public interface IPayoutAccountService {
    PayoutAccountDTO createPayoutAccount(PayoutAccountIN payoutAccountIN, String email) ;

    PayoutAccountDTO getPayoutAccount(String email);

    PayoutAccountDTO getPayoutAccountByUserId(Long userId);

    PayoutAccountDTO getDefaultPayoutAccount(String email);
}
