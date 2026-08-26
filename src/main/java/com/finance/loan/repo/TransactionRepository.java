package com.finance.loan.repo;

import com.finance.loan.entity.Transaction;
import com.finance.loan.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// TransactionRepository.java
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByLoanRequest_requestId(Long loanRequestId);

    List<Transaction> findBySender_EmailOrReceiver_Email(String email, String email1);

    Optional<Transaction> findByPaymentReference(String paymentReference);

    List<Transaction> findByTransactionStatus(TransactionStatus transactionStatus);
}