package com.finance.loan.controller;


import com.finance.loan.dto.Response;
import com.finance.loan.dto.output.InvestmentDTO;
import com.finance.loan.dto.output.PortfolioSummaryDTO;
import com.finance.loan.service.interfac.IMatchedRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/match-request")
@Validated
public class MatchedRequestController {

    @Autowired
    private IMatchedRequestService matchedRequestService;


    @GetMapping("/portfolio-summary")
    public ResponseEntity<Response<PortfolioSummaryDTO>> getInvestorPortfolioSummary() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Response<PortfolioSummaryDTO> response = matchedRequestService.getInvestorPortfolioSummary(email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/my-investments")
    public ResponseEntity<List<InvestmentDTO>> getMyInvestments() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(matchedRequestService.getMyInvestments(email)
        );
    }

    @GetMapping("/investments/all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<InvestmentDTO>> getAllInvestments() {
        return ResponseEntity.ok(matchedRequestService.getAllInvestments());
    }

    @GetMapping("/{investorId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Response<List<InvestmentDTO>>> getInvestmentsByInvestorId(@PathVariable Long investorId) {
        Response<List<InvestmentDTO>> response = matchedRequestService.getInvestmentsByInvestorId(investorId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


}