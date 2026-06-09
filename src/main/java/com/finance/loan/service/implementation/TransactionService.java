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
                    .orElseThrow(() -> new OurException("Loan request not found with id: " + loanRequestId));

            List<Transaction> transactions = transactionRepository
                    .findByLoanRequest_RequestId(loanRequestId);

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
}
