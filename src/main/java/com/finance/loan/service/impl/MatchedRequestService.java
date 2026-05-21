package com.finance.loan.service.impl;

import com.finance.loan.dto.InvestRequest;
import com.finance.loan.dto.Response;
import com.finance.loan.entity.LoanRequest;
import com.finance.loan.entity.LoanStatus;
import com.finance.loan.entity.MatchedRequest;
import com.finance.loan.entity.User;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.LoanRequestRepository;
import com.finance.loan.repo.MatchedRequestRepository;
import com.finance.loan.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class MatchedRequestService {

    @Autowired
    private MatchedRequestRepository matchedRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRequestRepository loanRequestRepository;

    // INVEST IN LOAN
    public Response investInLoan(InvestRequest request, String email) {
        Response response = new Response();
        try {
            User investor = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("User not found"));

            LoanRequest loanRequest = loanRequestRepository.findById(request.getLoanRequestId())
                    .orElseThrow(() -> new OurException("Loan request not found"));

            if (loanRequest.getStatus() != LoanStatus.APPROVED &&
                    loanRequest.getStatus() != LoanStatus.PARTIALLY_FUNDED) {
                throw new OurException("Loan request is not open for investment");
            }

            BigDecimal remaining = loanRequest.getRequestedAmount()
                    .subtract(loanRequest.getAmountFunded());

            if (request.getAmount().compareTo(remaining) > 0) {
                throw new OurException("Investment amount exceeds remaining capacity of $" + remaining);
            }

            MatchedRequest match = new MatchedRequest();
            match.setLoanRequest(loanRequest);
            match.setInvestor(investor);
            match.setInvestorAmount(request.getAmount());
            match.setMatchDate(LocalDateTime.now());
            matchedRequestRepository.save(match);

            loanRequest.setAmountFunded(loanRequest.getAmountFunded().add(request.getAmount()));

            BigDecimal newRemaining = loanRequest.getRequestedAmount()
                    .subtract(loanRequest.getAmountFunded());

            if (newRemaining.compareTo(BigDecimal.ZERO) == 0) {
                loanRequest.setStatus(LoanStatus.FULLY_FUNDED);
            } else {
                loanRequest.setStatus(LoanStatus.PARTIALLY_FUNDED);
            }

            loanRequestRepository.save(loanRequest);

            response.setStatusCode(200);
            response.setMessage("Investment successful");

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error processing investment: " + e.getMessage());
        }
        return response;
    }



}
