package com.finance.loan.service.interfac;

import com.finance.loan.dto.input.LoanRequestIN;
import com.finance.loan.dto.output.LoanRequestDTO;

import java.util.List;

public interface ILoanRequestService {

    LoanRequestDTO createLoanRequest(LoanRequestIN requestDTO, String email);

    List<LoanRequestDTO> getLoanRequestsByBorrowerEmail(String email);

    LoanRequestDTO getMyLoanRequestById(Long requestId, String email);


    List<LoanRequestDTO> getAllLoanRequests();

    LoanRequestDTO getLoanRequestById(Long requestId);

    List<LoanRequestDTO> getLoanRequestsByBorrowerId(Long borrowerId);

    LoanRequestDTO approveLoanRequest(Long requestId);

    LoanRequestDTO rejectLoanRequest(Long requestId);

    List<LoanRequestDTO> getMarketplaceLoans();

    LoanRequestDTO disburseLoan(Long requestId, String adminEmail);

    void deleteLoanRequest(Long id, String email);
}