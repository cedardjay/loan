package com.finance.loan.service.interfac;

import com.finance.loan.dto.input.LoanRequestIN;
import com.finance.loan.dto.Response;

public interface ILoanRequestService {

    Response createLoanRequest(LoanRequestIN requestDTO, String email);

    Response getLoanRequestsByBorrowerEmail(String email);

    Response getMyLoanRequestById(Long requestId, String email);

    Response deleteLoanRequest(Long requestId, String email);

    Response getAllLoanRequests();

    Response getLoanRequestById(Long requestId);

    Response getLoanRequestsByBorrowerId(Long borrowerId);

    Response approveLoanRequest(Long requestId);

    Response rejectLoanRequest(Long requestId);

    Response getMarketplaceLoans();

    Response disburseLoan(Long requestId, String adminEmail);

}