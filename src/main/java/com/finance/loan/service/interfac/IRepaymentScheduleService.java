package com.finance.loan.service.interfac;

import com.finance.loan.dto.Response;
import com.finance.loan.entity.LoanRequest;

public interface IRepaymentScheduleService {
    void generateSchedule(LoanRequest loanRequest);
    Response getRepaymentScheduleByLoanRequest(long loanRequestId);
}
