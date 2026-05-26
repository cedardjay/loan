package com.finance.loan.controller;

import com.finance.loan.dto.InvestRequest;
import com.finance.loan.dto.Response;
import com.finance.loan.service.impl.MatchedRequestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/match-request")
@Validated
public class MatchedRequestController {

    @Autowired
    private MatchedRequestService matchedRequestService;

    @PostMapping("/invest/{loanRequestId}")
    public ResponseEntity<Response> investInLoan(
            @PathVariable @NotNull @Positive Long loanRequestId,
           @Valid @RequestBody InvestRequest investmentRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Response response = matchedRequestService.investInLoan(loanRequestId, investmentRequest, email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/portfolio/summary")
    public ResponseEntity<Response> getInvestorPortfolioSummary() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Response response = matchedRequestService.getInvestorPortfolioSummary(email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/investments")
    public ResponseEntity<Response> getMyInvestments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Response response = matchedRequestService.getMyInvestments(email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }





}
