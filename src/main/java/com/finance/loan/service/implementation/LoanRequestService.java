package com.finance.loan.service.implementation;

import com.finance.loan.dto.input.LoanRequestIN;
import com.finance.loan.dto.output.LoanRequestDTO;
import com.finance.loan.dto.output.PaymentResult;
import com.finance.loan.entity.*;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.LoanRequestRepository;
import com.finance.loan.repo.UserRepository;
import com.finance.loan.service.interfac.ILoanRequestService;
import com.finance.loan.service.interfac.IPaymentGatewayService;
import com.finance.loan.service.interfac.IRepaymentScheduleService;
import com.finance.loan.service.interfac.ITransactionService;
import com.finance.loan.utils.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Service
public class LoanRequestService implements ILoanRequestService {

    @Autowired
    private LoanRequestRepository loanRequestRepository;

    @Autowired
    private UserRepository userRepository;

@Autowired
private ITransactionService transactionService;

@Autowired
private IRepaymentScheduleService repaymentScheduleService;

@Autowired
private IPaymentGatewayService paymentGatewayService;

    // CREATE A LOAN REQUEST
    public LoanRequestDTO createLoanRequest(LoanRequestIN requestDTO, String email) {

            // --- FETCH ---
            User borrower = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("User not found",404));

            // --- EXECUTE ---
            BigDecimal interestRate = LoanCalculatorUtils.calculateInterestRate(
                    requestDTO.getRequestedAmount(),
                    requestDTO.getTermMonths()
            );

            // --- PERSIST ---
            LoanRequest loanRequest = new LoanRequest();
            loanRequest.setBorrower(borrower);
            loanRequest.setRequestedAmount(requestDTO.getRequestedAmount());
            loanRequest.setDescription(requestDTO.getDescription());
            loanRequest.setTermMonths(requestDTO.getTermMonths());
            loanRequest.setPurpose(requestDTO.getPurpose());
            loanRequest.setAmountFunded(BigDecimal.ZERO);
            loanRequest.setStatus(LoanStatus.PENDING_APPROVAL);
            loanRequest.setRequestDate(LocalDateTime.now());
            loanRequest.setInterestRate(interestRate);

            LoanRequest savedRequest = loanRequestRepository.save(loanRequest);

        return LoanRequestUtils.mapLoanRequestEntityToOutput(savedRequest);
    }

    // GET MY REQUESTS
    public List<LoanRequestDTO> getLoanRequestsByBorrowerEmail(String email) {

            // --- FETCH ---
            List<LoanRequest> loanRequests = loanRequestRepository.findByBorrowerEmail(email);

            // --- RETURN ---
        return LoanRequestUtils.mapLoanRequestListToOutput(loanRequests);
    }


    // GET MY REQUEST BY REQUEST ID
    public LoanRequestDTO getMyLoanRequestById(Long requestId, String email) {
            // --- FETCH ---
            LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                    .orElseThrow(() -> new OurException("Loan request not found",404));

        // --- VALIDATE ---
        if (!loanRequest.getBorrower().getEmail().equals(email)) {
            throw new AccessDeniedException("You are not authorized to view this loan request");
        }

            // --- RETURN ---
        return LoanRequestUtils.mapLoanRequestEntityToOutput(loanRequest);
    }


    // DELETE
    public void deleteLoanRequest(Long requestId, String email) {

        // --- FETCH ---
        LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new OurException("Loan request not found", 404));

        // --- VALIDATE ---
        if (!loanRequest.getBorrower().getEmail().equals(email)) {
            throw new AccessDeniedException("You are not authorized to delete this loan request");
        }

        if (loanRequest.getStatus() == LoanStatus.FULLY_FUNDED ||
                loanRequest.getAmountFunded().compareTo(BigDecimal.ZERO) > 0) {
            throw new OurException("Cannot delete a loan request that has been funded", 400);
        }

        // --- PERSIST ---
        loanRequestRepository.delete(loanRequest);

    }

    // GET ALL LOAN REQUESTS (ADMIN and SUPERADMIN)
    public List<LoanRequestDTO> getAllLoanRequests() {
        return LoanRequestUtils.mapLoanRequestListToOutput(loanRequestRepository.findAll());
    }


    // GET REQUEST BY ID OF REQUEST (ADMIN and SUPERADMIN)
    public LoanRequestDTO getLoanRequestById(Long requestId) {
        // --- FETCH ---
        LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new OurException("Loan request not found with id: " + requestId, 404));

        // --- RETURN ---
        return LoanRequestUtils.mapLoanRequestEntityToOutput(loanRequest);
    }


    // GET LOAN REQUESTS BY BORROWER ID (ADMIN and SUPERADMIN)
    public List<LoanRequestDTO> getLoanRequestsByBorrowerId(Long borrowerId) {
        // --- FETCH ---
        if (!userRepository.existsById(borrowerId)) {
            throw new OurException("User not found with id: " + borrowerId, 404);
        }

        // --- RETURN ---
        return LoanRequestUtils.mapLoanRequestListToOutput(
                loanRequestRepository.findByBorrower_Id(borrowerId));
    }


    // APPROVE LOAN REQUEST (ADMIN / SUPERADMIN)
    public LoanRequestDTO approveLoanRequest(Long requestId) {
        // --- FETCH ---
        LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new OurException("Loan request not found with id: " + requestId, 404));

        // --- VALIDATE ---
        if (loanRequest.getStatus() != LoanStatus.PENDING_APPROVAL) {
            throw new OurException("Only pending loan requests can be approved", 400);
        }

        // --- PERSIST ---
        loanRequest.setStatus(LoanStatus.APPROVED);
        LoanRequest updatedRequest = loanRequestRepository.save(loanRequest);

        // --- RETURN ---
        return LoanRequestUtils.mapLoanRequestEntityToOutput(updatedRequest);
    }


    // REJECT LOAN REQUEST (ADMIN / SUPERADMIN)
    public LoanRequestDTO rejectLoanRequest(Long requestId) {
        // --- FETCH ---
        LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new OurException("Loan request not found with id: " + requestId, 404));

        // --- VALIDATE ---
        if (loanRequest.getStatus() != LoanStatus.PENDING_APPROVAL) {
            throw new OurException("Only pending loan requests can be rejected", 400);
        }

        // --- PERSIST ---
        loanRequest.setStatus(LoanStatus.REJECTED);
        LoanRequest updatedRequest = loanRequestRepository.save(loanRequest);

        // --- RETURN ---
        return LoanRequestUtils.mapLoanRequestEntityToOutput(updatedRequest);
    }


    // GET MARKETPLACE LOANS
    public List<LoanRequestDTO> getMarketplaceLoans() {
        return LoanRequestUtils.mapLoanRequestListToOutput(
                loanRequestRepository.findByStatusIn(List.of(
                        LoanStatus.APPROVED,
                        LoanStatus.PARTIALLY_FUNDED,
                        LoanStatus.FULLY_FUNDED
                )));
    }


    // DISBURSE LOAN
    @Transactional
    public LoanRequestDTO disburseLoan(Long requestId, String adminEmail) {
        // --- FETCH ---
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new OurException("Admin user not found", 404));

        LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new OurException("Loan request not found with id: " + requestId, 404));

        // --- VALIDATE ---
        if (loanRequest.getStatus() != LoanStatus.FULLY_FUNDED) {
            throw new OurException("Only fully funded loan requests can be disbursed", 400);
        }

        if (loanRequest.getBorrower() == null) {
            throw new OurException("No borrower linked to this loan request", 400);
        }

        // --- EXECUTE ---
        String paymentReference = "DISB-" + UUID.randomUUID().toString().toUpperCase();
        PaymentResult result = paymentGatewayService.disburse(
                loanRequest.getRequestedAmount(),
                loanRequest.getBorrower(),
                paymentReference
        );

        if (!result.isSuccessful()) {
            throw new OurException("Payment gateway failed: " + result.getErrorMessage(), 400);
        }

        // --- PERSIST ---
        transactionService.recordDisbursement(
                admin, loanRequest.getBorrower(), loanRequest,
                loanRequest.getRequestedAmount(), paymentReference);

        repaymentScheduleService.generateSchedule(loanRequest);

        loanRequest.setStatus(LoanStatus.ACTIVE);
        LoanRequest updatedLoan = loanRequestRepository.save(loanRequest);

        // --- RETURN ---
        return LoanRequestUtils.mapLoanRequestEntityToOutput(updatedLoan);
    }

}