package com.finance.loan.controller;


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
@RequestMapping("/match-requests")
@Validated
public class MatchedRequestController {

    @Autowired
    private IMatchedRequestService matchedRequestService;


    @GetMapping("/portfolio-summary")
    public ResponseEntity<PortfolioSummaryDTO> getInvestorPortfolioSummary() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(matchedRequestService.getInvestorPortfolioSummary(email));
    }

    @GetMapping("/my-investments")
    public ResponseEntity<List<InvestmentDTO>> getMyInvestments() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(matchedRequestService.getMyInvestments(email));
    }

    @GetMapping("/investments/all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<InvestmentDTO>> getAllInvestments() {
        return ResponseEntity.ok(matchedRequestService.getAllInvestments());
    }

    @GetMapping("/{investorId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<InvestmentDTO>> getInvestmentsByInvestorId(@PathVariable Long investorId) {
        return ResponseEntity.ok(matchedRequestService.getInvestmentsByInvestorId(investorId));
    }
}