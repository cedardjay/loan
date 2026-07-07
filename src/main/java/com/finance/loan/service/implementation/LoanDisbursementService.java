package com.finance.loan.service.implementation;

import com.finance.loan.dto.output.LoanDisbursementResult;
import com.finance.loan.dto.output.PaymentGatewayResponse;
import com.finance.loan.entity.*;
import com.finance.loan.event.LoanDisbursedEvent;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.LoanRequestRepository;
import com.finance.loan.repo.UserRepository;
import com.finance.loan.service.interfac.ILoanDisbursementService;
import com.finance.loan.service.interfac.IRepaymentScheduleService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class LoanDisbursementService implements ILoanDisbursementService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRequestRepository loanRequestRepository;

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private IRepaymentScheduleService repaymentScheduleService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public LoanDisbursementResult disburseLoan(Long loanId, String adminEmail) {

        // --- FETCH ---
        LoanRequest loanRequest = loanRequestRepository.findById(loanId)
                .orElseThrow(() -> new OurException("Loan not found with id: " + loanId, 404));

        User borrower = loanRequest.getBorrower();

        User platformAccount = userRepository.findByEmail("platform@system.internal")
                .orElseThrow(() -> new IllegalStateException("Platform account not seeded"));

        // --- VALIDATE ---
        if (loanRequest.getStatus() != LoanStatus.FULLY_FUNDED) {
            throw new OurException("Only fully funded loans can be disbursed", 400);
        }


        // --- EXECUTE ---
        BigDecimal amount = loanRequest.getRequestedAmount();
        PaymentGatewayResponse result = paymentGatewayService.disburse( platformAccount, borrower, amount);

        if (!result.isAccepted()) {
            throw new OurException("Disbursement gateway rejected the request: " + result.getErrorMessage(), 400);
        }

        // --- PERSIST (PENDING — gateway will later confirm via webhook) ---
        transactionService.recordPendingDisbursement(
                platformAccount, borrower, loanRequest,
                amount, result.getReference()
        );

        // --- RETURN ---
        return LoanDisbursementResult.builder()
                .loanId(loanId)
                .borrowerEmail(borrower.getEmail())
                .amount(amount)
                .paymentReference(result.getReference())
                .status(TransactionStatus.PENDING)
                .build();

        // loan status stays APPROVED until webhook confirms
    }

    @Override
    public void onDisbursementSettled(Transaction tx) {

        LoanRequest loanRequest = tx.getLoanRequest();

        // --- PERSIST ---
        loanRequest.setStatus(LoanStatus.ACTIVE);
        loanRequestRepository.save(loanRequest);
        repaymentScheduleService.generateSchedule(loanRequest);

        // --- PUBLISH ---
        eventPublisher.publishEvent(new LoanDisbursedEvent(loanRequest, tx.getSender().getEmail()));
    }
}
