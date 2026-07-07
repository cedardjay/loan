package com.finance.loan.service.implementation;

import com.finance.loan.dto.input.GatewayWebhookPayload;
import com.finance.loan.entity.Transaction;
import com.finance.loan.entity.TransactionStatus;
import com.finance.loan.exception.OurException;
import com.finance.loan.service.interfac.ILoanDisbursementService;
import com.finance.loan.service.interfac.ILoanRepaymentService;
import com.finance.loan.service.interfac.ITransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentReconciliationService {

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private ILoanRepaymentService loanRepaymentService;

    @Autowired
    private ILoanDisbursementService loanDisbursementService;

    @Transactional
    public void handleGatewayWebhook(GatewayWebhookPayload payload) {

        Transaction tx = transactionService.settleTransaction(
                payload.getReference(),
                "success".equals(payload.getStatus())
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