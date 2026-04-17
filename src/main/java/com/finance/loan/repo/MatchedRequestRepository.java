package com.finance.loan.repo;

import com.finance.loan.entity.MatchedRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchedRequestRepository extends JpaRepository<MatchedRequest, Long> {
    List<MatchedRequest> findByLoanRequest_RequestId(Long requestId);
    List<MatchedRequest> findByInvestor_UserId(Long investorId);
}