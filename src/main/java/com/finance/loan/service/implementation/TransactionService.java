package com.finance.loan.service.implementation;

import com.finance.loan.dto.input.GatewayWebhookPayload;
import com.finance.loan.dto.output.TransactionDTO;
import com.finance.loan.entity.*;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.LoanRequestRepository;
import com.finance.loan.repo.TransactionRepository;
import com.finance.loan.service.interfac.ITransactionService;
import com.finance.loan.utils.TransactionUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService implements ITransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LoanRequestRepository loanRequestRepository;


    @Override
    public void recordPendingDisbursement(User platformAccount, User borrower,
                                          LoanRequest loanRequest,
                                          BigDecimal amount,
                                          String paymentReference) {
        transactionRepository.save(Transaction.builder()
                .sender(platformAccount)
                .receiver(borrower)
                .loanRequest(loanRequest)
                .amount(amount)
                .paymentMethod("MOCK_GATEWAY")
                .transactionDate(LocalDateTime.now())
                .description("Loan disbursement - Ref: " + paymentReference)
                .transactionType(TransactionType.DISBURSEMENT)
                .transactionStatus(TransactionStatus.PENDING)
                .paymentReference(paymentReference)
                .build());
    }

     @Override
    public void recordPendingRepayment(User borrower, User platformAccount, LoanRequest loanRequest, RepaymentSchedule schedule,
                                BigDecimal amount, String paymentReference) {
        transactionRepository.save(Transaction.builder()
                .sender(borrower)
                .receiver(platformAccount)
                .loanRequest(loanRequest)
                .repaymentSchedule(schedule)
                .amount(amount)
                .paymentMethod("MOCK_GATEWAY")
                .transactionDate(LocalDateTime.now())
                .paymentReference(paymentReference)
                .description("Loan repayment - Ref:" + paymentReference)
                .transactionType(TransactionType.REPAYMENT)
                .transactionStatus(TransactionStatus.PENDING)
                .build());
    }


    @Override
    public Transaction settleTransaction(String paymentReference, boolean success) {
        Transaction tx = transactionRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new OurException("Unknown payment reference: " + paymentReference, 404));

        // duplicate webhook guard — gateway often retries
        if (tx.getTransactionStatus() != TransactionStatus.PENDING) {
            return tx;
        }

        tx.setTransactionStatus(success ? TransactionStatus.COMPLETED : TransactionStatus.FAILED);
        tx.setSettledAt(LocalDateTime.now());
        return transactionRepository.save(tx);
    }

    @Override
    public List<TransactionDTO> getTransactionsByLoanRequest(Long loanRequestId) {
        // --- FETCH ---
        loanRequestRepository.findById(loanRequestId)
                .orElseThrow(() -> new OurException("Loan request not found with id: " + loanRequestId, 404));

        // --- RETURN ---
        return TransactionUtils.mapTransactionListToOutput(
                transactionRepository.findByLoanRequest_requestId(loanRequestId));
    }

    @Override
    public List<TransactionDTO> getAllTransactions() {
        return TransactionUtils.mapTransactionListToOutput(transactionRepository.findAll());
    }

    @Override
    public List<TransactionDTO> getMyLoanTransactions(Long loanRequestId, String email) {
        // --- FETCH ---
        LoanRequest loanRequest = loanRequestRepository.findById(loanRequestId)
                .orElseThrow(() -> new OurException("Loan request not found with id: " + loanRequestId, 404));

        // --- VALIDATE ---
        if (!loanRequest.getBorrower().getEmail().equals(email)) {
            throw new OurException("You are not authorized to view transactions for this loan", 403);
        }

        // --- RETURN ---
        return TransactionUtils.mapTransactionListToOutput(
                transactionRepository.findByLoanRequest_requestId(loanRequestId));
    }


    public List<TransactionDTO> getMyTransactions(String email) {
        return TransactionUtils.mapTransactionListToOutput(
                transactionRepository.findBySender_EmailOrReceiver_Email(email, email));
    }
}