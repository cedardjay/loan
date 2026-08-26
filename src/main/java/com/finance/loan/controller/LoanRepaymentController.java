package com.finance.loan.controller;


import com.finance.loan.dto.input.PayoutAccountIN;
import com.finance.loan.dto.output.LoanPaymentResult;
import com.finance.loan.service.interfac.ILoanRepaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class LoanRepaymentController {
    @Autowired
    private ILoanRepaymentService loanRepaymentService;
    //request payment
    @PostMapping("/{loanId}")
    public ResponseEntity<LoanPaymentResult> loanPayment(@PathVariable Long loanId, @RequestBody PayoutAccountIN payerDetails) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(loanRepaymentService.loanPayment(loanId, payerDetails, email));
    }


}
