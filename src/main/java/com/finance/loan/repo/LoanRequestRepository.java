package com.finance.loan.repo;

import com.finance.loan.entity.LoanRequest;
import com.finance.loan.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRequestRepository extends JpaRepository<LoanRequest, Long> {
    List<LoanRequest> findByBorrower_Id(long borrowerId);

    List<LoanRequest> findByStatusIn(List<LoanStatus> statuses);

    List<LoanRequest> findByBorrowerEmail(String email);

    List<LoanRequest> findByBorrowerEmailAndStatus(String email, LoanStatus status);

    List<LoanRequest> findByBorrowerEmailAndStatusIn(String email, List<LoanStatus> approved);
}