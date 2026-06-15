package com.finance.loan.service.interfac;

import com.finance.loan.dto.input.InvestRequest;
import com.finance.loan.dto.output.InvestmentDTO;
import com.finance.loan.dto.output.PortfolioSummaryDTO;

import java.util.List;

public interface IMatchedRequestService {



    PortfolioSummaryDTO getInvestorPortfolioSummary(String email);

    List<InvestmentDTO> getMyInvestments(String email);

    List<InvestmentDTO> getAllInvestments();

    List<InvestmentDTO> getInvestmentsByInvestorId(Long investorId);

    void investInLoan(Long loanRequestId,  InvestRequest investmentRequest, String email);
}