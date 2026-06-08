package com.finance.loan.service.interfac;

import com.finance.loan.dto.input.InvestRequest;
import com.finance.loan.dto.Response;

public interface IMatchedRequestService {

    Response investInLoan(long loanRequestId, InvestRequest investmentRequest, String email);

    Response getInvestorPortfolioSummary(String email);

    Response getMyInvestments(String email);

}
