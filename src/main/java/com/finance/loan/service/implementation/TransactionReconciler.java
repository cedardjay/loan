package com.finance.loan.service.implementation;

import com.finance.loan.dto.input.IwomiPayoutResponse;
import com.finance.loan.entity.Transaction;
import com.finance.loan.entity.TransactionStatus;
import com.finance.loan.exception.OurException;
import com.finance.loan.service.interfac.IPaymentGatewayService;
import com.finance.loan.service.interfac.ITransactionReconcilerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionReconciler implements ITransactionReconcilerService {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private IPaymentGatewayService paymentGatewayService;
    @Autowired
    private LoanRepaymentService loanRepaymentService;
    @Autowired
    private LoanDisbursementService loanDisbursementService;

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
}