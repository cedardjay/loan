package com.finance.loan.service.implementation;

import com.finance.loan.dto.input.DisbursalRequestIN;
import com.finance.loan.dto.input.LoanRequestIN;
import com.finance.loan.dto.output.LoanRequestDTO;
import com.finance.loan.entity.*;
import com.finance.loan.event.*;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.LoanRequestRepository;
import com.finance.loan.repo.PayoutAccountRepository;
import com.finance.loan.repo.UserRepository;
import com.finance.loan.service.interfac.ILoanRequestService;
import com.finance.loan.service.interfac.IPaymentGatewayService;
import com.finance.loan.service.interfac.IRepaymentScheduleService;
import com.finance.loan.service.interfac.ITransactionService;
import com.finance.loan.utils.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;



@Service
public class LoanRequestService implements ILoanRequestService {

    @Autowired
    private LoanRequestRepository loanRequestRepository;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private ApplicationEventPublisher eventPublisher;


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

        // --- PUBLISH EVENT---
        eventPublisher.publishEvent(new LoanRequestCreatedEvent(savedRequest, email));

       //return
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
    public LoanRequestDTO approveLoanRequest(Long requestId, String adminEmail) {
        // --- FETCH ---
        LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new OurException("Loan request not found with id: " + requestId, 404));

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new OurException("Admin not found with email: " + adminEmail, 404));

        // --- VALIDATE ---
        if (loanRequest.getStatus() != LoanStatus.PENDING_APPROVAL) {
            throw new OurException("Only pending loan requests can be approved", 400);
        }


        // --- PERSIST ---
        loanRequest.setStatus(LoanStatus.APPROVED);
        loanRequest.setApproval(admin);
        LoanRequest updatedRequest = loanRequestRepository.save(loanRequest);

        // --- PUBLISH EVENT---
        eventPublisher.publishEvent(new LoanRequestApprovedEvent(updatedRequest, adminEmail));

        // --- RETURN ---
        return LoanRequestUtils.mapLoanRequestEntityToOutput(updatedRequest);
    }


    // REJECT LOAN REQUEST (ADMIN / SUPERADMIN)
    public LoanRequestDTO rejectLoanRequest(Long requestId, String adminEmail) {
        // --- FETCH ---
        LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new OurException("Loan request not found with id: " + requestId, 404));

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new OurException("Admin not found with email: " + adminEmail, 404));

        // --- VALIDATE ---
        if (loanRequest.getStatus() != LoanStatus.PENDING_APPROVAL) {
            throw new OurException("Only pending loan requests can be rejected", 400);
        }

        // --- PERSIST ---
        loanRequest.setStatus(LoanStatus.REJECTED);
        loanRequest.setApproval(admin);
        LoanRequest updatedRequest = loanRequestRepository.save(loanRequest);

        // --- PUBLISH ---
        eventPublisher.publishEvent(new LoanRequestRejectedEvent(updatedRequest, adminEmail));


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

    // GET MY ACTIVE LOANS (BORROWER)
    public List<LoanRequestDTO> getActiveLoansByBorrowerEmail(String email) {
        // --- FETCH ---
        List<LoanRequest> activeLoans = loanRequestRepository
                .findByBorrowerEmailAndStatus(email, LoanStatus.ACTIVE);

        // --- RETURN ---
        return LoanRequestUtils.mapLoanRequestListToOutput(activeLoans);
    }

    // GET MY MARKETPLACE LOANS (BORROWER'S OWN LISTED LOANS)
    public List<LoanRequestDTO> getMyMarketplaceLoans(String email) {
        // --- FETCH ---
        List<LoanRequest> loans = loanRequestRepository.findByBorrowerEmailAndStatusIn(
                email,
                List.of(
                        LoanStatus.APPROVED,
                        LoanStatus.PARTIALLY_FUNDED,
                        LoanStatus.FULLY_FUNDED
                ));

        // --- RETURN ---
        return LoanRequestUtils.mapLoanRequestListToOutput(loans);
    }



}