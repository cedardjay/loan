package com.finance.loan.service.interfac;

import com.finance.loan.dto.input.LoanRequestIN;
import com.finance.loan.dto.Response;
import com.finance.loan.dto.output.LoanRequestDTO;

import java.util.List;

public interface ILoanRequestService {

    LoanRequestDTO createLoanRequest(LoanRequestIN requestDTO, String email);

    List<LoanRequestDTO> getLoanRequestsByBorrowerEmail(String email);

    LoanRequestDTO getMyLoanRequestById(Long requestId, String email);

    Void deleteLoanRequest(Long requestId, String email);

    Response<List<LoanRequestDTO>> getAllLoanRequests();

    Response<LoanRequestDTO> getLoanRequestById(Long requestId);

    Response<List<LoanRequestDTO>> getLoanRequestsByBorrowerId(Long borrowerId);

    Response<LoanRequestDTO> approveLoanRequest(Long requestId);

    Response<LoanRequestDTO> rejectLoanRequest(Long requestId);

    Response<List<LoanRequestDTO>> getMarketplaceLoans();

    Response<LoanRequestDTO> disburseLoan(Long requestId, String adminEmail);
}