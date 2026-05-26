package com.finance.loan.repo;

import com.finance.loan.entity.MatchedRequest;
import com.finance.loan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchedRequestRepository extends JpaRepository<MatchedRequest, Long> {

    List<MatchedRequest> findByInvestor(User investor);
}