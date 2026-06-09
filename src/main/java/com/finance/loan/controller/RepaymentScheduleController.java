package com.finance.loan.controller;

import com.finance.loan.dto.Response;
import com.finance.loan.dto.output.RepaymentScheduleDTO;
import com.finance.loan.service.interfac.IRepaymentScheduleService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/repayment")
@Validated
public class RepaymentScheduleController {

    @Autowired
    private IRepaymentScheduleService repaymentScheduleService;


    //GET THE REPAYMENT SCHEDULE FOR A PARTICULAR LOAN REQUEST
    @GetMapping("/schedule/{loanRequestId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Response<List<RepaymentScheduleDTO>>> getRepaymentScheduleByLoanRequest(@PathVariable @NotNull @Positive Long loanRequestId) {
        Response<List<RepaymentScheduleDTO>> response = repaymentScheduleService.getRepaymentScheduleByLoanRequest(loanRequestId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
