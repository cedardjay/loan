package com.finance.loan.utils;

import com.finance.loan.dto.output.InvestmentDTO;
import com.finance.loan.entity.LoanRequest;
import com.finance.loan.entity.MatchedRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class MatchedRequestUtils {

    public static InvestmentDTO mapMatchedRequestEntityToOutput(MatchedRequest match) {
        InvestmentDTO dto = new InvestmentDTO();
        LoanRequest loan = match.getLoanRequest();

        // Basic fields
        dto.setId(match.getMatchId());
        dto.setInvestedDate(match.getMatchDate().toLocalDate().toString());
        dto.setAmount(match.getInvestorAmount());

        // Investor info
        if (match.getInvestor() != null) {
            dto.setInvestorEmail(match.getInvestor().getEmail());
        }

        // Loan info
        if (loan != null) {
            dto.setName(loan.getPurpose());
            dto.setInterest(loan.getInterestRate());
            dto.setStatus(loan.getStatus().name());

            // Calculated fields
            if (match.getInvestorAmount() != null &&
                    loan.getInterestRate() != null &&
                    loan.getTermMonths() > 0) {

                dto.setExpectedReturn(LoanCalculatorUtils.calculateExpectedReturn(
                        match.getInvestorAmount(),
                        loan.getInterestRate(),
                        loan.getTermMonths()
                ));
            } else {
                dto.setExpectedReturn(BigDecimal.ZERO);
            }
        }

        return dto;
    }

    public static List<InvestmentDTO> mapMatchedRequestListToOutput(List<MatchedRequest> matches) {
        return matches.stream()
                .map(MatchedRequestUtils::mapMatchedRequestEntityToOutput)
                .collect(Collectors.toList());
    }
}

