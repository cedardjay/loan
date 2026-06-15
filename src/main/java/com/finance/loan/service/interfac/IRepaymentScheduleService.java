package com.finance.loan.service.interfac;

import com.finance.loan.dto.output.RepaymentScheduleDTO;
import com.finance.loan.entity.LoanRequest;

import java.util.List;

public interface IRepaymentScheduleService {

    void generateSchedule(LoanRequest loanRequest);

    List<RepaymentScheduleDTO> getMyRepaymentSchedule(Long loanRequestId, String email);

    List<RepaymentScheduleDTO> getRepaymentScheduleByLoanRequest(Long loanRequestId);
}
