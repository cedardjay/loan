package com.finance.loan.service.interfac;

import com.finance.loan.dto.output.TransactionDTO;
import com.finance.loan.entity.*;

import java.math.BigDecimal;
import java.util.List;

public interface ITransactionService {


    Transaction recordDisbursement(User platformAccount, User borrower, LoanRequest loanRequest,
                                   BigDecimal amount, PaymentMethod paymentMethod, String accountNumber);

    Transaction recordRepayment(User borrower, User platformAccount, LoanRequest loanRequest,
                                RepaymentSchedule schedule,
                                BigDecimal amount, PaymentMethod paymentMethod, String accountNumber);

    Transaction updateTransactionResult(Transaction tx, String internalId, TransactionStatus status);


        Transaction settleTransaction(String paymentReference, TransactionStatus status);

    TransactionDTO getStatusByReference(String paymentReference, String email);

    List<TransactionDTO> getTransactionsByLoanRequest(Long loanRequestId);

    List<TransactionDTO> getAllTransactions();

    List<TransactionDTO> getMyLoanTransactions(Long loanRequestId, String email);

    List<TransactionDTO> getMyTransactions(String email);


    }
