package com.finance.loan.controller;

import com.finance.loan.dto.LoanRequestIN;
import com.finance.loan.service.impl.LoanRequestService;
import com.finance.loan.dto.Response;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loan-requests")
public class LoanRequestController {

    @Autowired
    private LoanRequestService loanRequestService;

    // CREATE Loan request
    @PostMapping("/create")
    public ResponseEntity<Response> createLoanRequest(@Valid @RequestBody LoanRequestIN loanRequestIN) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Response response = loanRequestService.createLoanRequest(loanRequestIN, email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    // GET MY REQUESTS - (by authenticated email)
    @GetMapping("/my-requests/all")
    public ResponseEntity<Response> getMyLoanRequests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Response response = loanRequestService.getLoanRequestsByBorrowerEmail(email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // GET MY REQUEST BY ID
    @GetMapping("/my-requests/{id}")
    public ResponseEntity<Response> getMyLoanRequestById(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Response response = loanRequestService.getMyLoanRequestById(id, email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // DELETE - owner only (service handles ownership check)
    @DeleteMapping("/my-requests/delete/{id}")
    public ResponseEntity<Response> deleteLoanRequest(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Response response = loanRequestService.deleteLoanRequest(id, email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }



    //ADMIN and SUPERADMIN

    // find all existing loan requests
    @GetMapping("/all")
   @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Response> getAllLoanRequests() {
        Response response = loanRequestService.getAllLoanRequests();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // find loan requests by id of the request
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Response> getLoanRequestById(@PathVariable Long id) {
        Response response = loanRequestService.getLoanRequestById(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // GET LOAN REQUESTS BY BORROWER ID
    @GetMapping("/borrower/{borrowerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Response> getLoanRequestsByBorrowerId(@PathVariable Long borrowerId) {
        Response response = loanRequestService.getLoanRequestsByBorrowerId(borrowerId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/approve/{requestId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Response> approveLoanRequest(@PathVariable Long requestId) {
        Response response = loanRequestService.approveLoanRequest(requestId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/reject/{requestId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Response> rejectLoanRequest(@PathVariable Long requestId) {
        Response response = loanRequestService.rejectLoanRequest(requestId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/marketplace")
    public ResponseEntity<Response> getMarketplaceLoans() {
        Response response = loanRequestService.getMarketplaceLoans();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}