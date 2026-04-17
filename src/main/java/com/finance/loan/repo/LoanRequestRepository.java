package com.finance.loan.repo;

import com.finance.loan.entity.LoanRequest;
import com.finance.loan.entity.LoanState;
import com.finance.loan.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRequestRepository extends JpaRepository<LoanRequest, Long> {
    List<LoanRequest> findByBorrower_UserId(Long borrowerId);
    List<LoanRequest> findByState(LoanState state);
    List<LoanRequest> findByStatus(LoanStatus status);
    List<LoanRequest> findByBorrower_UserIdAndState(Long borrowerId, LoanState state);
}