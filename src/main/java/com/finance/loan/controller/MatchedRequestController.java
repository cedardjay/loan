package com.finance.loan.controller;

import com.finance.loan.dto.input.InvestRequest;
import com.finance.loan.dto.Response;
import com.finance.loan.dto.output.InvestmentDTO;
import com.finance.loan.dto.output.PortfolioSummaryDTO;
import com.finance.loan.service.interfac.IMatchedRequestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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


    @GetMapping("/portfolio/summary")
    public ResponseEntity<Response<PortfolioSummaryDTO>> getInvestorPortfolioSummary() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Response<PortfolioSummaryDTO> response = matchedRequestService.getInvestorPortfolioSummary(email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/investments")
    public ResponseEntity<Response<List<InvestmentDTO>>> getMyInvestments() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Response<List<InvestmentDTO>> response = matchedRequestService.getMyInvestments(email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Response<List<InvestmentDTO>>> getAllInvestments() {
        Response<List<InvestmentDTO>> response = matchedRequestService.getAllInvestments();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}