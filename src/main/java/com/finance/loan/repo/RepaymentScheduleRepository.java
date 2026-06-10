package com.finance.loan.repo;

import com.finance.loan.entity.LoanRequest;
import com.finance.loan.entity.RepaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// RepaymentScheduleRepository.java
@Repository
public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {

    List<RepaymentSchedule> findByLoanRequest_requestId(Long loanRequestId);
}
