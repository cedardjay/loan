package com.finance.loan.service.implementation;

import com.finance.loan.dto.output.TransactionDTO;
import com.finance.loan.entity.*;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.LoanRequestRepository;
import com.finance.loan.repo.TransactionRepository;
import com.finance.loan.repo.UserRepository;
import com.finance.loan.service.interfac.ITransactionService;
import com.finance.loan.utils.TransactionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService implements ITransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LoanRequestRepository loanRequestRepository;

    @Autowired
    private UserRepository userRepository;


    @Override
    public Transaction recordDisbursement(User platformAccount, User borrower, LoanRequest loanRequest,
                                          BigDecimal amount, TransactionStatus status) {

        String paymentReference = UUID.randomUUID().toString();

        return transactionRepository.save(Transaction.builder()
                .sender(platformAccount)
                .receiver(borrower)
                .loanRequest(loanRequest)
                .amount(amount)
                .payoutType(loanRequest.getPayoutAccount().getType())
                .transactionDate(LocalDateTime.now())
                .description("Loan disbursement - Ref: " + paymentReference)
                .transactionType(TransactionType.DISBURSEMENT)
                .transactionStatus(status)
                .paymentReference(paymentReference)
                .build());
    }


    public Transaction updateTransactionResult(Transaction tx, String internalId, TransactionStatus status) {
        tx.setInternalId(internalId);
        tx.setTransactionStatus(status);
        return transactionRepository.save(tx);
    }


    @Override
    public Transaction settleTransaction(String paymentReference, TransactionStatus status) {
        Transaction tx = transactionRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new OurException("Unknown payment reference: " + paymentReference, 404));

        // duplicate webhook guard — gateway often retries
        if (tx.getTransactionStatus() != TransactionStatus.PENDING) {
            return tx;
        }

        tx.setTransactionStatus(status);
        if (status == TransactionStatus.COMPLETED) {
            tx.setSettledAt(LocalDateTime.now());
        }

        return transactionRepository.save(tx);
    }

    @Override
    public TransactionDTO getStatusByReference(String paymentReference, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OurException("User not found", 404));

        Transaction tx = transactionRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new OurException("Transaction not found", 404));

        if (!tx.getReceiver().getId().equals(user.getId()) && !tx.getSender().getId().equals(user.getId())) {
            throw new OurException("You do not have access to this transaction", 403);
        }

        return TransactionDTO.builder()
                .paymentReference(tx.getPaymentReference())
                .transactionStatus(tx.getTransactionStatus())
                .build();
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