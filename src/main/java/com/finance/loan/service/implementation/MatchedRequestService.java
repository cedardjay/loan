package com.finance.loan.service.implementation;

import com.finance.loan.dto.input.InvestRequest;
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
import com.finance.loan.utils.MatchedRequestUtils;
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
    public void investInLoan(Long loanRequestId, InvestRequest investmentRequest, String email) {
        // --- FETCH ---
        User investor = userRepository.findByEmail(email)
                .orElseThrow(() -> new OurException("User not found", 404));

        LoanRequest loanRequest = loanRequestRepository.findById(loanRequestId)
                .orElseThrow(() -> new OurException("Loan request not found", 404));

        // --- VALIDATE ---
        if (loanRequest.getBorrower().getId().equals(investor.getId())) {
            throw new OurException("You cannot invest in your own loan request", 400);
        }

        if (loanRequest.getStatus() != LoanStatus.APPROVED &&
                loanRequest.getStatus() != LoanStatus.PARTIALLY_FUNDED) {
            throw new OurException("Loan request is not open for investment", 400);
        }

        BigDecimal remaining = loanRequest.getRequestedAmount()
                .subtract(loanRequest.getAmountFunded());

        if (investmentRequest.getAmount().compareTo(remaining) > 0) {
            throw new OurException("Investment amount exceeds remaining capacity of " + remaining, 422);
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
    }


    public PortfolioSummaryDTO getInvestorPortfolioSummary(String email) {
        // --- FETCH ---
        if (!userRepository.existsByEmail(email)) {
            throw new OurException("User not found", 404);
        }

        List<MatchedRequest> investments = matchedRequestRepository.findByInvestor_email(email);

        // --- EXECUTE ---
        BigDecimal totalInvested = investments.stream()
                .map(MatchedRequest::getInvestorAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgApy = investments.isEmpty() ? BigDecimal.ZERO :
                investments.stream()
                        .map(m -> m.getLoanRequest().getInterestRate())
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(investments.size()), 2, RoundingMode.HALF_UP);

        // --- RETURN ---
        return PortfolioSummaryDTO.builder()
                .totalInvested(totalInvested)
                .currentValue(totalInvested)
                .totalReturns(BigDecimal.ZERO)
                .avgApy(avgApy)
                .build();
    }


    public List<InvestmentDTO> getMyInvestments(String email) {
        return MatchedRequestUtils.mapMatchedRequestListToOutput(
                matchedRequestRepository.findByInvestor_email(email)
        );
    }


    public List<InvestmentDTO> getAllInvestments() {
        return MatchedRequestUtils.mapMatchedRequestListToOutput(
                matchedRequestRepository.findAll()
        );
    }


    public List<InvestmentDTO> getInvestmentsByInvestorId(Long investorId) {
        if (!userRepository.existsById(investorId)) {
            throw new OurException("User not found", 404);
        }

        return MatchedRequestUtils.mapMatchedRequestListToOutput(
                matchedRequestRepository.findByInvestor_id(investorId)
        );
    }
}
