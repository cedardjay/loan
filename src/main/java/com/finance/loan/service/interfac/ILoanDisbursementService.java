package com.finance.loan.service.interfac;

import com.finance.loan.dto.output.LoanDisbursementResult;
import com.finance.loan.entity.Transaction;

public interface ILoanDisbursementService {
    LoanDisbursementResult disburseLoan(Long loanId, String adminEmail);

    void onDisbursementSettled(Transaction tx);
}
