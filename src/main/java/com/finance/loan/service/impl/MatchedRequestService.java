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
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MatchedRequestService {

    @Autowired
    private MatchedRequestRepository matchedRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRequestRepository loanRequestRepository;

    // INVEST IN LOAN
    @Transactional
    public Response investInLoan(long loanRequestId, InvestRequest investmentRequest, String email) {
        Response response = new Response();
        try {
            User investor = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("User not found"));

            LoanRequest loanRequest = loanRequestRepository.findById(loanRequestId)
                    .orElseThrow(() -> new OurException("Loan request not found"));

            if (loanRequest.getBorrower().getId().equals(investor.getId())) {
                throw new OurException("You cannot invest in your own loan request");
            }

            if (loanRequest.getStatus() != LoanStatus.APPROVED &&
                    loanRequest.getStatus() != LoanStatus.PARTIALLY_FUNDED) {
                throw new OurException("Loan request is not open for investment");
            }

            BigDecimal remaining = loanRequest.getRequestedAmount()
                    .subtract(loanRequest.getAmountFunded());

            if (investmentRequest.getAmount().compareTo(remaining) > 0) {
                throw new OurException("Investment amount exceeds remaining capacity of $" + remaining);
            }

            MatchedRequest match = new MatchedRequest();
            match.setLoanRequest(loanRequest);
            match.setInvestor(investor);
            match.setInvestorAmount(investmentRequest.getAmount());
            match.setMatchDate(LocalDateTime.now());
            matchedRequestRepository.save(match);

            loanRequest.setAmountFunded(loanRequest.getAmountFunded().add(investmentRequest.getAmount()));

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

    //INVESTOR PORTFOLIO SUMMARY
    public Response getInvestorPortfolioSummary(String email) {
        Response response = new Response();
        try {
            User investor = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("User not found"));

            List<MatchedRequest> investments = matchedRequestRepository.findByInvestor(investor);

            BigDecimal totalInvested = investments.stream()
                    .map(MatchedRequest::getInvestorAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avgApy = investments.isEmpty() ? BigDecimal.ZERO :
                    investments.stream()
                            .map(m -> m.getLoanRequest().getInterestRate())
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(investments.size()), 2, RoundingMode.HALF_UP);

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalInvested", totalInvested);
            summary.put("currentValue", totalInvested);   // update when you track repayments
            summary.put("totalReturns", BigDecimal.ZERO); // update when you track repayments
            summary.put("avgApy", avgApy);

            response.setStatusCode(200);
            response.setMessage("Portfolio summary retrieved successfully");
            response.setData(summary);

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error retrieving portfolio summary: " + e.getMessage());
        }
        return response;
    }

//GET USER INVESTMENTS
    public Response getMyInvestments(String email) {
        Response response = new Response();
        try {
            User investor = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("User not found"));

            List<MatchedRequest> investments = matchedRequestRepository.findByInvestor(investor);

            List<Map<String, Object>> investmentList = investments.stream().map(match -> {
                LoanRequest loan = match.getLoanRequest();

                BigDecimal expectedReturn = match.getInvestorAmount()
                        .multiply(loan.getInterestRate()
                                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP))
                        .multiply(BigDecimal.valueOf(loan.getTermMonths())
                                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP))
                        .setScale(2, RoundingMode.HALF_UP);

                Map<String, Object> item = new HashMap<>();
                item.put("id", match.getMatchId());
                item.put("name", loan.getPurpose());
                item.put("amount", match.getInvestorAmount());
                item.put("interest", loan.getInterestRate());
                item.put("status", loan.getStatus().name());
                item.put("investedDate", match.getMatchDate().toLocalDate().toString());
                item.put("expectedReturn", expectedReturn);
                //will add nextPayment field later
                return item;
            }).toList();

            Map<String, Object> result = new HashMap<>();
            result.put("investments", investmentList);

            response.setStatusCode(200);
            response.setMessage("Investments retrieved successfully");
            response.setData(result);

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error retrieving investments: " + e.getMessage());
        }
        return response;
    }
}
