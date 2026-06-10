package com.finance.loan.service.interfac;

import com.finance.loan.dto.Response;
import com.finance.loan.dto.output.TransactionDTO;
import com.finance.loan.entity.LoanRequest;
import com.finance.loan.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface ITransactionService {
    void recordDisbursement(User admin, User borrower,
                                   LoanRequest loanRequest,
                                   BigDecimal amount,
                                   String paymentReference);

    Response<List<TransactionDTO>> getTransactionsByLoanRequest(Long loanRequestId);

    Response<List<TransactionDTO>> getAllTransactions();

    Response<List<TransactionDTO>> getMyLoanTransactions(Long loanRequestId, String email);

    Response<List<TransactionDTO>> getMyTransactions(String email);
}
