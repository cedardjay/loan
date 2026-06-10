package com.finance.loan.service.interfac;

import com.finance.loan.dto.input.InvestRequest;
import com.finance.loan.dto.Response;
import com.finance.loan.dto.output.InvestmentDTO;
import com.finance.loan.dto.output.PortfolioSummaryDTO;

import java.util.List;

public interface IMatchedRequestService {

    Response<Void> investInLoan(Long loanRequestId, InvestRequest investmentRequest, String email);

    Response<PortfolioSummaryDTO> getInvestorPortfolioSummary(String email);

    Response<List<InvestmentDTO>> getMyInvestments(String email);

}
