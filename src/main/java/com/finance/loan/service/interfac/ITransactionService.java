package com.finance.loan.service.interfac;

import com.finance.loan.dto.input.GatewayWebhookPayload;
import com.finance.loan.dto.output.TransactionDTO;
import com.finance.loan.entity.LoanRequest;
import com.finance.loan.entity.RepaymentSchedule;
import com.finance.loan.entity.Transaction;
import com.finance.loan.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface ITransactionService {

    void recordPendingDisbursement(User admin, User borrower,
                                          LoanRequest loanRequest,
                                          BigDecimal amount,
                                          String paymentReference);

    List<TransactionDTO> getTransactionsByLoanRequest(Long loanRequestId);

    List<TransactionDTO> getAllTransactions();

    List<TransactionDTO> getMyLoanTransactions(Long loanRequestId, String email);

    List<TransactionDTO> getMyTransactions(String email);

    void recordPendingRepayment(User borrower, User platformAccount, LoanRequest loanRequest, RepaymentSchedule schedule, BigDecimal paymentAmount, String paymentReference);

    Transaction settleTransaction(String paymentReference, boolean success);

    }
