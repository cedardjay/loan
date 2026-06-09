package com.finance.loan.controller;

import com.finance.loan.dto.Response;
import com.finance.loan.dto.output.TransactionDTO;
import com.finance.loan.service.interfac.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;



@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private ITransactionService transactionService ;

    @GetMapping("/loan/{loanRequestId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Response<List<TransactionDTO>>> getTransactionsByLoanRequest(
            @PathVariable Long loanRequestId) {
        Response<List<TransactionDTO>> response = transactionService
                .getTransactionsByLoanRequest(loanRequestId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
