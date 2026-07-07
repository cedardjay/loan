package com.finance.loan.service.interfac;

import com.finance.loan.dto.output.LoanPaymentResult;
import com.finance.loan.entity.Transaction;

public interface ILoanRepaymentService {
        LoanPaymentResult loanPayment(Long loanId, String borrowerEmail);
        void onRepaymentSettled(Transaction tx);

    }
