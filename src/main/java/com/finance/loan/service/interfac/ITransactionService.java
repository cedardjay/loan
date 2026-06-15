package com.finance.loan.service.interfac;

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

    List<TransactionDTO> getTransactionsByLoanRequest(Long loanRequestId);

    List<TransactionDTO> getAllTransactions();

    List<TransactionDTO> getMyLoanTransactions(Long loanRequestId, String email);

    List<TransactionDTO> getMyTransactions(String email);
}
