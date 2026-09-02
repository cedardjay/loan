package com.finance.loan.controller;

import com.finance.loan.dto.input.DisbursalRequestIN;
import com.finance.loan.dto.input.InvestRequest;
import com.finance.loan.dto.input.LoanRequestIN;
import com.finance.loan.dto.output.*;
import com.finance.loan.service.interfac.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loan-requests")
public class LoanRequestController {

    @Autowired
    private ILoanRequestService loanRequestService;

    @Autowired
    private ILoanDisbursementService loanDisbursementService;

    @Autowired
    private ILoanRepaymentService loanRepaymentService;

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private IRepaymentScheduleService repaymentScheduleService;

    @Autowired
    private IMatchedRequestService matchedRequestService;

    // CREATE Loan request
    @PostMapping("/create")
    public ResponseEntity<LoanRequestDTO> createLoanRequest(@Valid @RequestBody LoanRequestIN loanRequestIN) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(loanRequestService.createLoanRequest(loanRequestIN, email));
    }

    // GET MY REQUESTS
    @GetMapping("/my-requests/all")
    public ResponseEntity<List<LoanRequestDTO>> getMyLoanRequests() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(loanRequestService.getLoanRequestsByBorrowerEmail(email));
    }

    // GET MY REQUEST BY ID
    @GetMapping("/my-requests/{id}")
    public ResponseEntity<LoanRequestDTO> getMyLoanRequestById(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(loanRequestService.getMyLoanRequestById(id, email));
    }


    //invest in a loan
    @PostMapping("/{loanRequestId}/invest")
    public ResponseEntity<Void> investInLoan(
            @PathVariable @NotNull @Positive Long loanRequestId,
            @Valid @RequestBody InvestRequest investmentRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        matchedRequestService.investInLoan(loanRequestId, investmentRequest, email);
        return ResponseEntity.status(201).build();
    }

    // GET ALL LOAN REQUESTS (ADMIN and SUPERADMIN)
    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<LoanRequestDTO>> getAllLoanRequests() {
        return ResponseEntity.ok(loanRequestService.getAllLoanRequests());
    }

    // GET BY REQUEST ID (ADMIN and SUPERADMIN)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<LoanRequestDTO> getLoanRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(loanRequestService.getLoanRequestById(id));
    }

    // GET LOAN REQUESTS BY BORROWER ID (ADMIN and SUPERADMIN)
    @GetMapping("/borrower/{borrowerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<LoanRequestDTO>> getLoanRequestsByBorrowerId(@PathVariable Long borrowerId) {
        return ResponseEntity.ok(loanRequestService.getLoanRequestsByBorrowerId(borrowerId));
    }

    // APPROVE (ADMIN and SUPERADMIN)
    @PutMapping("/{requestId}/approve")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<LoanRequestDTO> approveLoanRequest(@PathVariable Long requestId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(loanRequestService.approveLoanRequest(requestId, email));
    }

    // REJECT (ADMIN and SUPERADMIN)
    @PutMapping("/{requestId}/reject")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<LoanRequestDTO> rejectLoanRequest(@PathVariable Long requestId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(loanRequestService.rejectLoanRequest(requestId, email));
    }

    // GET MARKETPLACE LOANS
    @GetMapping("/marketplace")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<LoanRequestDTO>> getMarketplaceLoans() {
        return ResponseEntity.ok(loanRequestService.getMarketplaceLoans());
    }



    @GetMapping("/{loanRequestId}/transactions")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByLoanRequest(
            @PathVariable Long loanRequestId) {
        return ResponseEntity.ok(transactionService.getTransactionsByLoanRequest(loanRequestId));
    }

    @GetMapping("/my-requests/{loanRequestId}/transactions")
    public ResponseEntity<List<TransactionDTO>> getMyLoanTransactions(
            @PathVariable Long loanRequestId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(transactionService.getMyLoanTransactions(loanRequestId, email));
    }

    @GetMapping("/{loanRequestId}/repayment-schedule")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<RepaymentScheduleDTO>> getRepaymentScheduleByLoanRequest(
            @PathVariable Long loanRequestId) {
        return ResponseEntity.ok(repaymentScheduleService.getRepaymentScheduleByLoanRequest(loanRequestId));
    }

    @GetMapping("/my-requests/{loanRequestId}/repayment-schedule")
    public ResponseEntity<List<RepaymentScheduleDTO>> getMyRepaymentSchedule(
            @PathVariable Long loanRequestId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(repaymentScheduleService.getMyRepaymentSchedule(loanRequestId, email));
    }


    @GetMapping("/my-active")
    public ResponseEntity<List<LoanRequestDTO>> getMyActiveLoans() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(loanRequestService.getActiveLoansByBorrowerEmail(email));
    }

    @GetMapping("/my-marketplace")
    public ResponseEntity<List<LoanRequestDTO>> getMyMarketplaceLoans() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(loanRequestService.getMyMarketplaceLoans(email));
    }



}


