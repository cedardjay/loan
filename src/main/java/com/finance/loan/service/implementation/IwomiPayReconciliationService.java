package com.finance.loan.service.implementation;

import com.finance.loan.dto.input.IwomiPayoutResponse;
import com.finance.loan.entity.Transaction;
import com.finance.loan.entity.TransactionStatus;
import com.finance.loan.repo.TransactionRepository;
import com.finance.loan.service.interfac.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@RequiredArgsConstructor
public class IwomiPayReconciliationService {


    @Autowired
    private IPaymentGatewayService paymentGatewayService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionReconciler transactionReconciler;

    // --- POLLING ---
    @Scheduled(fixedDelay = 10000) // every 10 seconds, adjust as needed
    public void pollPendingTransactions() {
        List<Transaction> pending = transactionRepository.findByTransactionStatus(TransactionStatus.PENDING);

        for (Transaction transaction : pending) {
            IwomiPayoutResponse result = paymentGatewayService.checkStatus(transaction.getInternalId());
            transactionReconciler.reconcileTransaction(result);
        }

    }
}