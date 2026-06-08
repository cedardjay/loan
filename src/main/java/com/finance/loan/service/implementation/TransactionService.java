package com.finance.loan.service.implementation;

import com.finance.loan.entity.*;
import com.finance.loan.repo.TransactionRepository;
import com.finance.loan.service.interfac.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService implements ITransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public void recordDisbursement(User admin, User borrower,
                                          LoanRequest loanRequest,
                                          BigDecimal amount,
                                          String paymentReference) {
        Transaction transaction = Transaction.builder()
                .sender(admin)
                .receiver(borrower)
                .loanRequest(loanRequest)
                .amount(amount)
                .paymentMethod("MOCK_GATEWAY")
                .transactionDate(LocalDateTime.now())
                .description("Loan disbursement - Ref: " + paymentReference)
                .transactionType(TransactionType.DISBURSEMENT)
                .transactionStatus(TransactionStatus.COMPLETED)
                .build();

        transactionRepository.save(transaction);
    }

}
