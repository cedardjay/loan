package com.finance.loan.service.interfac;

import com.finance.loan.dto.output.TransactionDTO;
import com.finance.loan.entity.*;

import java.math.BigDecimal;
import java.util.List;

public interface ITransactionService {


    Transaction recordDisbursement(User admin, User borrower,
                                   LoanRequest loanRequest,
                                   BigDecimal amount,
                                    TransactionStatus status);

    Transaction updateTransactionResult(Transaction tx, String internalId, TransactionStatus status);


        Transaction settleTransaction(String paymentReference, TransactionStatus status);

    TransactionDTO getStatusByReference(String paymentReference, String email);

    List<TransactionDTO> getTransactionsByLoanRequest(Long loanRequestId);

    List<TransactionDTO> getAllTransactions();

    List<TransactionDTO> getMyLoanTransactions(Long loanRequestId, String email);

    List<TransactionDTO> getMyTransactions(String email);


    }
