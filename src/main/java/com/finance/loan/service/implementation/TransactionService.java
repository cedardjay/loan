package com.finance.loan.service.implementation;

import com.finance.loan.dto.output.TransactionDTO;
import com.finance.loan.entity.*;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.LoanRequestRepository;
import com.finance.loan.repo.TransactionRepository;
import com.finance.loan.service.interfac.ITransactionService;
import com.finance.loan.utils.TransactionUtils;
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


    public void recordDisbursement(User admin, User borrower,
                                   LoanRequest loanRequest,
                                   BigDecimal amount,
                                   String paymentReference) {
        transactionRepository.save(Transaction.builder()
                .sender(admin)
                .receiver(borrower)
                .loanRequest(loanRequest)
                .amount(amount)
                .paymentMethod("MOCK_GATEWAY")
                .transactionDate(LocalDateTime.now())
                .description("Loan disbursement - Ref: " + paymentReference)
                .transactionType(TransactionType.DISBURSEMENT)
                .transactionStatus(TransactionStatus.COMPLETED)
                .build());
    }


    public List<TransactionDTO> getTransactionsByLoanRequest(Long loanRequestId) {
        // --- FETCH ---
        loanRequestRepository.findById(loanRequestId)
                .orElseThrow(() -> new OurException("Loan request not found with id: " + loanRequestId, 404));

        // --- RETURN ---
        return TransactionUtils.mapTransactionListToOutput(
                transactionRepository.findByLoanRequest_requestId(loanRequestId));
    }


    public List<TransactionDTO> getAllTransactions() {
        return TransactionUtils.mapTransactionListToOutput(transactionRepository.findAll());
    }


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