package com.finance.loan.controller;

import com.finance.loan.dto.InvestRequest;
import com.finance.loan.dto.Response;
import com.finance.loan.service.impl.MatchedRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/match-request")
public class MatchedRequestController {

    @Autowired
    private MatchedRequestService matchedRequestService;

    @PostMapping("/invest")
    public ResponseEntity<Response> investInLoan(@RequestBody InvestRequest investmentRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Response response = matchedRequestService.investInLoan(investmentRequest, email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
