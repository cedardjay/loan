package com.finance.loan.service.implementation;

import com.finance.loan.dto.input.InvestRequest;
import com.finance.loan.dto.Response;
import com.finance.loan.dto.output.InvestmentDTO;
import com.finance.loan.dto.output.PortfolioSummaryDTO;
import com.finance.loan.entity.LoanRequest;
import com.finance.loan.entity.LoanStatus;
import com.finance.loan.entity.MatchedRequest;
import com.finance.loan.entity.User;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.LoanRequestRepository;
import com.finance.loan.repo.MatchedRequestRepository;
import com.finance.loan.repo.UserRepository;
import com.finance.loan.service.interfac.IMatchedRequestService;
import com.finance.loan.utils.LoanCalculatorUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchedRequestService implements IMatchedRequestService {

    @Autowired
    private MatchedRequestRepository matchedRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRequestRepository loanRequestRepository;

    @Transactional
    public Response<Void> investInLoan(long loanRequestId, InvestRequest investmentRequest, String email) {
        Response<Void> response = new Response<>();
        try {
            // --- FETCH ---
            User investor = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("User not found"));

            LoanRequest loanRequest = loanRequestRepository.findById(loanRequestId)
                    .orElseThrow(() -> new OurException("Loan request not found"));

            // --- VALIDATE ---
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
                throw new OurException("Investment amount exceeds remaining capacity of " + remaining);
            }

            // --- EXECUTE ---
            BigDecimal newAmountFunded = loanRequest.getAmountFunded()
                    .add(investmentRequest.getAmount());

            LoanStatus newStatus = newAmountFunded.compareTo(loanRequest.getRequestedAmount()) == 0
                    ? LoanStatus.FULLY_FUNDED
                    : LoanStatus.PARTIALLY_FUNDED;

            // --- PERSIST ---
            MatchedRequest match = new MatchedRequest();
            match.setLoanRequest(loanRequest);
            match.setInvestor(investor);
            match.setInvestorAmount(investmentRequest.getAmount());
            match.setMatchDate(LocalDateTime.now());
            matchedRequestRepository.save(match);

            loanRequest.setAmountFunded(newAmountFunded);
            loanRequest.setStatus(newStatus);
            loanRequestRepository.save(loanRequest);

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("Investment successful");

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error processing investment: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return response;
    }

    // INVESTOR PORTFOLIO SUMMARY
    public Response<PortfolioSummaryDTO> getInvestorPortfolioSummary(String email) {
        Response<PortfolioSummaryDTO> response = new Response<>();
        try {
            // --- FETCH ---
            User investor = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("User not found"));

            List<MatchedRequest> investments = matchedRequestRepository.findByInvestor(investor);

            // --- EXECUTE ---
            BigDecimal totalInvested = investments.stream()
                    .map(MatchedRequest::getInvestorAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avgApy = investments.isEmpty() ? BigDecimal.ZERO :
                    investments.stream()
                            .map(m -> m.getLoanRequest().getInterestRate())
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(investments.size()), 2, RoundingMode.HALF_UP);

            PortfolioSummaryDTO summary = PortfolioSummaryDTO.builder()
                    .totalInvested(totalInvested)
                    .currentValue(totalInvested)
                    .totalReturns(BigDecimal.ZERO)
                    .avgApy(avgApy)
                    .build();

            // --- RETURN ---
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


    // GET USER INVESTMENTS
    public Response<List<InvestmentDTO>> getMyInvestments(String email) {
        Response<List<InvestmentDTO>> response = new Response<>();
        try {
            // --- FETCH ---
            User investor = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("User not found"));

            List<MatchedRequest> investments = matchedRequestRepository.findByInvestor(investor);

            // --- EXECUTE ---
            List<InvestmentDTO> investmentList = investments.stream().map(match -> {
                LoanRequest loan = match.getLoanRequest();

                BigDecimal expectedReturn = LoanCalculatorUtils.calculateExpectedReturn(
                        match.getInvestorAmount(),
                        loan.getInterestRate(),
                        loan.getTermMonths()
                );

                return InvestmentDTO.builder()
                        .id(match.getMatchId())
                        .name(loan.getPurpose())
                        .amount(match.getInvestorAmount())
                        .interest(loan.getInterestRate())
                        .status(loan.getStatus().name())
                        .investedDate(match.getMatchDate().toLocalDate().toString())
                        .expectedReturn(expectedReturn)
                        .build();
            }).toList();

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("Investments retrieved successfully");
            response.setData(investmentList);

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
