package com.finance.loan.service.interfac;

import com.finance.loan.dto.Response;
import com.finance.loan.dto.output.RepaymentScheduleDTO;
import com.finance.loan.entity.LoanRequest;
import com.finance.loan.entity.RepaymentSchedule;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public interface IRepaymentScheduleService {
    void generateSchedule(LoanRequest loanRequest);

    Response<List<RepaymentScheduleDTO>> getMyRepaymentSchedule(Long loanRequestId, String email);

    Response<List<RepaymentScheduleDTO>> getRepaymentScheduleByLoanRequest(@NotNull @Positive Long loanRequestId);
}
