package com.finance.loan.repo;


import com.finance.loan.entity.RepaymentSchedule;
import com.finance.loan.entity.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// RepaymentScheduleRepository.java
@Repository
public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {
    Optional<RepaymentSchedule> findFirstByLoanRequest_requestIdAndStatusInOrderByDueDateAsc(Long loanId, List<ScheduleStatus> pending);

    List<RepaymentSchedule> findByLoanRequest_requestId(Long loanRequestId);
}


