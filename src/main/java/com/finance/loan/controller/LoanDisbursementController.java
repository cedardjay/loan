package com.finance.loan.controller;

import com.finance.loan.dto.input.DisbursalRequestIN;
import com.finance.loan.dto.output.LoanDisbursementResult;
import com.finance.loan.dto.output.LoanRequestDTO;
import com.finance.loan.service.interfac.ILoanDisbursementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/disbursal")
public class LoanDisbursementController {
    @Autowired
    ILoanDisbursementService loanDisbursementService;
//request disbursal
    @PostMapping("/request")
    public ResponseEntity<LoanRequestDTO> requestDisbursal(@RequestBody DisbursalRequestIN disbursalRequestIN) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(loanDisbursementService.requestDisbursal(disbursalRequestIN, email));
    }

    // DISBURSE (SUPERADMIN)
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<LoanDisbursementResult> disburseLoan(@PathVariable Long id) {
        String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(loanDisbursementService.disburseLoan(id, adminEmail));
    }
}
