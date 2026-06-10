package com.finance.loan.controller;

import com.finance.loan.dto.input.InvestRequest;
import com.finance.loan.dto.input.LoanRequestIN;
import com.finance.loan.dto.Response;
import com.finance.loan.dto.output.LoanRequestDTO;
import com.finance.loan.dto.output.RepaymentScheduleDTO;
import com.finance.loan.dto.output.TransactionDTO;
import com.finance.loan.service.interfac.ILoanRequestService;
import com.finance.loan.service.interfac.IMatchedRequestService;
import com.finance.loan.service.interfac.IRepaymentScheduleService;
import com.finance.loan.service.interfac.ITransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private IRepaymentScheduleService repaymentScheduleService;

    @Autowired
    private IMatchedRequestService matchedRequestService;

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

    // DELETE my loan request
    @DeleteMapping("/my-requests/{id}/delete")
    public ResponseEntity<Response<Void>> deleteLoanRequest(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Response<Void> response = loanRequestService.deleteLoanRequest(id, email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    //invest in a loan
    @PostMapping("/{loanRequestId}/invest")
    public ResponseEntity<Response<Void>> investInLoan(
            @PathVariable @NotNull @Positive Long loanRequestId,
            @Valid @RequestBody InvestRequest investmentRequest) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Response<Void> response = matchedRequestService.investInLoan(loanRequestId, investmentRequest, email);
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
    @PutMapping("/{requestId}/approve/")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Response<LoanRequestDTO>> approveLoanRequest(@PathVariable Long requestId) {
        Response<LoanRequestDTO> response = loanRequestService.approveLoanRequest(requestId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // REJECT (ADMIN and SUPERADMIN)
    @PutMapping("/{requestId}/reject")
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
    @PutMapping("/{requestId}/disburse")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<Response<LoanRequestDTO>> disburseLoan(@PathVariable Long requestId) {
        String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Response<LoanRequestDTO> response = loanRequestService.disburseLoan(requestId, adminEmail);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    @GetMapping("/{loanRequestId}/transactions")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Response<List<TransactionDTO>>> getTransactionsByLoanRequest(
            @PathVariable Long loanRequestId) {
        Response<List<TransactionDTO>> response = transactionService
                .getTransactionsByLoanRequest(loanRequestId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{loanRequestId}/my-transactions")
    public ResponseEntity<Response<List<TransactionDTO>>> getMyLoanTransactions(
            @PathVariable Long loanRequestId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Response<List<TransactionDTO>> response = transactionService
                .getMyLoanTransactions(loanRequestId, email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    //GET THE REPAYMENT SCHEDULE FOR A PARTICULAR LOAN REQUEST
    @GetMapping("/{loanRequestId}/repayment-schedule")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Response<List<RepaymentScheduleDTO>>> getRepaymentScheduleByLoanRequest(@PathVariable Long loanRequestId) {
        Response<List<RepaymentScheduleDTO>> response = repaymentScheduleService.getRepaymentScheduleByLoanRequest(loanRequestId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    //USER TO GET REPAYMENT SCHEDULE FOR THEIR LOAN REQUEST
    @GetMapping("/{loanRequestId}/my-repayment-schedule/")
    public ResponseEntity<Response<List<RepaymentScheduleDTO>>> getMyRepaymentSchedule(
            @PathVariable Long loanRequestId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Response<List<RepaymentScheduleDTO>> response = repaymentScheduleService
                .getMyRepaymentSchedule(loanRequestId, email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}


