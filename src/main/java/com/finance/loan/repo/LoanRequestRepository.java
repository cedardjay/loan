package com.finance.loan.repo;

import com.finance.loan.entity.User;
import com.finance.loan.entity.LoanRequest;
import com.finance.loan.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRequestRepository extends JpaRepository<LoanRequest, Long> {
    List<LoanRequest> findByBorrower(User borrower);

    List<LoanRequest> findByStatus(LoanStatus status);


    List<LoanRequest> findByBorrowerEmail(String email);
}