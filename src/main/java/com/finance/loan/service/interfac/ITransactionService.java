package com.finance.loan.service.interfac;

import com.finance.loan.dto.output.TransactionDTO;
import com.finance.loan.entity.*;

import java.math.BigDecimal;
import java.util.List;

public interface ITransactionService {


    Transaction recordDisbursement(User admin, User borrower,
                                   LoanRequest loanRequest,
                                   BigDecimal amount,
                                   String paymentReference, TransactionStatus status);

    Transaction settleTransaction(String paymentReference, TransactionStatus status);

    List<TransactionDTO> getTransactionsByLoanRequest(Long loanRequestId);

    List<TransactionDTO> getAllTransactions();

    List<TransactionDTO> getMyLoanTransactions(Long loanRequestId, String email);

    List<TransactionDTO> getMyTransactions(String email);

    void recordPendingRepayment(User borrower, User platformAccount, LoanRequest loanRequest, RepaymentSchedule schedule, BigDecimal paymentAmount, String paymentReference);

    }
