package com.finance.loan.service.interfac;

import com.finance.loan.dto.input.IwomiPayoutResponse;

public interface ITransactionReconcilerService {
    void reconcileTransaction(IwomiPayoutResponse payload);
}
