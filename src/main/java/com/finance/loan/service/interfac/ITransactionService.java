package com.finance.loan.service.interfac;

import com.finance.loan.entity.LoanRequest;
import com.finance.loan.entity.User;

import java.math.BigDecimal;

public interface ITransactionService {
    void recordDisbursement(User admin, User borrower,
                                   LoanRequest loanRequest,
                                   BigDecimal amount,
                                   String paymentReference);

}
