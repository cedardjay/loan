package com.finance.loan.service.implementation;

import com.finance.loan.dto.Response;
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


    //GET TRANSACTIONS RELATED TO A LOAN REQUEST
    public Response<List<TransactionDTO>> getTransactionsByLoanRequest(Long loanRequestId) {
        Response<List<TransactionDTO>> response = new Response<>();
        try {
            // --- FETCH ---
            loanRequestRepository.findById(loanRequestId)
                    .orElseThrow(() -> new OurException("Loan request not found with id: " + loanRequestId,404));

            List<Transaction> transactions = transactionRepository
                    .findByLoanRequest_requestId(loanRequestId);

            // --- VALIDATE ---
            if (transactions.isEmpty()) {
                response.setStatusCode(404);
                response.setMessage("No transactions found for loan request: " + loanRequestId);
                return response;
            }

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("Transactions retrieved successfully");
            response.setData(TransactionUtils.mapTransactionListToOutput(transactions));

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error retrieving transactions: " + e.getMessage());
        }
        return response;
    }

    public Response<List<TransactionDTO>> getAllTransactions() {
        Response<List<TransactionDTO>> response = new Response<>();
        try {
            // --- FETCH ---
            List<Transaction> transactions = transactionRepository.findAll();

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("Transactions retrieved successfully");
            response.setData(TransactionUtils.mapTransactionListToOutput(transactions));

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error retrieving transactions: " + e.getMessage());
        }
        return response;
    }


    //USER GETS TRANSACTIONS RELATED TO THEIR LOAN REQUEST
    public Response<List<TransactionDTO>> getMyLoanTransactions(Long loanRequestId, String email) {
        Response<List<TransactionDTO>> response = new Response<>();
        try {
            // --- FETCH ---
            LoanRequest loanRequest = loanRequestRepository.findById(loanRequestId)
                    .orElseThrow(() -> new OurException("Loan request not found with id: " + loanRequestId,404));

            // --- VALIDATE ---
            if (!loanRequest.getBorrower().getEmail().equals(email)) {
                response.setStatusCode(403);
                response.setMessage("You are not authorized to view transactions for this loan");
                return response;
            }

            List<Transaction> transactions = transactionRepository
                    .findByLoanRequest_requestId(loanRequestId);

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("Transactions retrieved successfully");
            response.setData(TransactionUtils.mapTransactionListToOutput(transactions));

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error retrieving transactions: " + e.getMessage());
        }
        return response;
    }

    //USER GETS ALL THEIR TRANSACTIONS
    public Response<List<TransactionDTO>> getMyTransactions(String email) {
        Response<List<TransactionDTO>> response = new Response<>();
        try {
            // --- FETCH ---
            List<Transaction> transactions = transactionRepository
                    .findBySender_EmailOrReceiver_Email(email,email);

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("Transactions retrieved successfully");
            response.setData(TransactionUtils.mapTransactionListToOutput(transactions));

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error retrieving transactions: " + e.getMessage());
        }
        return response;
    }
}
