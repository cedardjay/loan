package com.finance.loan.service.interfac;

import com.finance.loan.dto.input.PayoutAccountIN;
import com.finance.loan.dto.output.LoanPaymentResult;
import com.finance.loan.entity.Transaction;
import jakarta.transaction.Transactional;

public interface ILoanRepaymentService {

    LoanPaymentResult loanPayment(Long loanId, PayoutAccountIN payerAccount, String borrowerEmail);

    void onRepaymentSettled(Transaction tx);

    }
