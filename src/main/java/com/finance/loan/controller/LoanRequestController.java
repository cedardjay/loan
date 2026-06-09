package com.finance.loan.controller;

import com.finance.loan.dto.input.LoanRequestIN;
import com.finance.loan.dto.Response;
import com.finance.loan.dto.output.LoanRequestDTO;
import com.finance.loan.service.interfac.ILoanRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loan-requests")
public class LoanRequestController {

    @Autowired
    private ILoanRequestService loanRequestService;

    // CREATE Loan request
    @PostMapping("/create")
    public ResponseEntity<Response<LoanRequestDTO>> createLoanRequest(@Valid @RequestBody LoanRequestIN loanRequestIN) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Response<LoanRequestDTO> response = loanRequestService.createLoanRequest(loanRequestIN, email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // GET MY REQUESTS
    @GetMapping("/my-requests/all")
    public ResponseEntity<Response<List<LoanRequestDTO>>> getMyLoanRequests() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Response<List<LoanRequestDTO>> response = loanRequestService.getLoanRequestsByBorrowerEmail(email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // GET MY REQUEST BY ID
    @GetMapping("/my-requests/{id}")
    public ResponseEntity<Response<LoanRequestDTO>> getMyLoanRequestById(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Response<LoanRequestDTO> response = loanRequestService.getMyLoanRequestById(id, email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // DELETE
    @DeleteMapping("/my-requests/delete/{id}")
    public ResponseEntity<Response<Void>> deleteLoanRequest(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Response<Void> response = loanRequestService.deleteLoanRequest(id, email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // GET ALL LOAN REQUESTS (ADMIN and SUPERADMIN)
    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Response<List<LoanRequestDTO>>> getAllLoanRequests() {
        Response<List<LoanRequestDTO>> response = loanRequestService.getAllLoanRequests();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // GET BY REQUEST ID (ADMIN and SUPERADMIN)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Response<LoanRequestDTO>> getLoanRequestById(@PathVariable Long id) {
        Response<LoanRequestDTO> response = loanRequestService.getLoanRequestById(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // GET LOAN REQUESTS BY BORROWER ID (ADMIN and SUPERADMIN)
    @GetMapping("/borrower/{borrowerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Response<List<LoanRequestDTO>>> getLoanRequestsByBorrowerId(@PathVariable Long borrowerId) {
        Response<List<LoanRequestDTO>> response = loanRequestService.getLoanRequestsByBorrowerId(borrowerId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // APPROVE (ADMIN and SUPERADMIN)
    @PutMapping("/approve/{requestId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Response<LoanRequestDTO>> approveLoanRequest(@PathVariable Long requestId) {
        Response<LoanRequestDTO> response = loanRequestService.approveLoanRequest(requestId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // REJECT (ADMIN and SUPERADMIN)
    @PutMapping("/reject/{requestId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Response<LoanRequestDTO>> rejectLoanRequest(@PathVariable Long requestId) {
        Response<LoanRequestDTO> response = loanRequestService.rejectLoanRequest(requestId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // GET MARKETPLACE LOANS
    @GetMapping("/marketplace")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Response<List<LoanRequestDTO>>> getMarketplaceLoans() {
        Response<List<LoanRequestDTO>> response = loanRequestService.getMarketplaceLoans();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // DISBURSE (SUPERADMIN)
    @PutMapping("/disburse/{requestId}")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<Response<LoanRequestDTO>> disburseLoan(@PathVariable Long requestId) {
        String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Response<LoanRequestDTO> response = loanRequestService.disburseLoan(requestId, adminEmail);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}


