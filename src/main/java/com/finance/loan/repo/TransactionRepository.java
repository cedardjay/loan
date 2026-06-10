package com.finance.loan.repo;

import com.finance.loan.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// TransactionRepository.java
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByLoanRequest_requestId(Long loanRequestId);

    List<Transaction> findBySender_EmailOrReceiver_Email(String email, String email1);
}