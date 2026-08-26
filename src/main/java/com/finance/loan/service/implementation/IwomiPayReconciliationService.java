package com.finance.loan.service.implementation;

import com.finance.loan.dto.input.IwomiPayoutResponse;
import com.finance.loan.entity.Transaction;
import com.finance.loan.entity.TransactionStatus;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.TransactionRepository;
import com.finance.loan.service.interfac.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IwomiPayReconciliationService implements IPaymentReconciliationService {

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private IPaymentGatewayService paymentGatewayService;

    @Autowired
    private ILoanRepaymentService loanRepaymentService;

    @Autowired
    private ILoanDisbursementService loanDisbursementService;

    @Autowired
    private TransactionRepository transactionRepository;

    // --- SHARED LOGIC (called by webhook controller AND by polling) ---
    @Transactional
    public void reconcileTransaction(IwomiPayoutResponse payload) {

        Transaction tx = transactionService.settleTransaction(
                payload.getInternalId(), paymentGatewayService.mapStatus(payload.getStatus())
        );

        if (tx.getTransactionStatus() != TransactionStatus.COMPLETED) {
            return;
        }

        switch (tx.getTransactionType()) {
            case REPAYMENT -> loanRepaymentService.onRepaymentSettled(tx);
            case DISBURSEMENT -> loanDisbursementService.onDisbursementSettled(tx);
            default -> throw new OurException("Unhandled transaction type: " + tx.getTransactionType(), 500);
        }

    }

    // --- POLLING ---
    @Scheduled(fixedDelay = 10000) // every 10 seconds, adjust as needed
    public void pollPendingTransactions() {
        List<Transaction> pending = transactionRepository.findByTransactionStatus(TransactionStatus.PENDING);

        for (Transaction transaction : pending) {
            IwomiPayoutResponse result = paymentGatewayService.checkStatus(transaction.getInternalId());
            reconcileTransaction(result);
        }

    }
}