package com.finance.loan.service.implementation;

import com.finance.loan.dto.output.LoanPaymentResult;
import com.finance.loan.dto.internal.PaymentGatewayResponse;
import com.finance.loan.entity.*;
import com.finance.loan.event.LoanPaymentEvent;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.LoanRequestRepository;
import com.finance.loan.repo.RepaymentScheduleRepository;
import com.finance.loan.repo.UserRepository;
import com.finance.loan.service.interfac.ILoanRepaymentService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class LoanRepaymentService implements ILoanRepaymentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRequestRepository loanRequestRepository;

    @Autowired
    private RepaymentScheduleRepository repaymentScheduleRepository;

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;


    @Override
    @Transactional
    public LoanPaymentResult loanPayment(Long loanId, String borrowerEmail) {

        // --- FETCH ---
        User borrower = userRepository.findByEmail(borrowerEmail)
                .orElseThrow(() -> new OurException("Borrower not found", 404));

        LoanRequest loanRequest = loanRequestRepository.findById(loanId)
                .orElseThrow(() -> new OurException("Loan not found with id: " + loanId, 404));

        RepaymentSchedule schedule = repaymentScheduleRepository
                .findFirstByLoanRequest_requestIdAndStatusInOrderByDueDateAsc(
                        loanId,
                        List.of(ScheduleStatus.PENDING, ScheduleStatus.PARTIAL, ScheduleStatus.LATE)
                )
                .orElseThrow(() -> new OurException("No pending installments for this loan", 404));

        User platformAccount = userRepository.findByEmail("platform@system.internal")
                .orElseThrow(() -> new IllegalStateException("Platform account not seeded"));

        // --- VALIDATE ---
        if (loanRequest.getStatus() != LoanStatus.ACTIVE) {
            throw new OurException("Only active loans can receive payments", 400);
        }

        if (!loanRequest.getBorrower().getId().equals(borrower.getId())) {
            throw new OurException("You are not the borrower of this loan", 403);
        }

        BigDecimal alreadyPaid = schedule.getAmountPaid() != null
                ? schedule.getAmountPaid()
                : BigDecimal.ZERO;
        BigDecimal paymentAmount = schedule.getAmountDue().subtract(alreadyPaid);

        // --- EXECUTE (submit only — gateway confirms later via webhook) ---
        PaymentGatewayResponse result = paymentGatewayService.collect(borrower, platformAccount, paymentAmount);

        // VALIDATE — only catches immediate rejection (e.g. invalid card), not final outcome
        if (!result.isAccepted()) {
            throw new OurException("Payment gateway rejected the request: " + result.getErrorMessage(), 400);
        }

        // --- PERSIST (PERSIST A PENDING TRANSACTION WHILE THE GATEWAY PROCESSES THE ACTUAL MONEY MOVEMENT) ---
        transactionService.recordPendingRepayment(
                borrower, platformAccount, loanRequest, schedule,
                paymentAmount, result.getReference()
        );

        // --- RETURN ---
        return LoanPaymentResult.builder()
                .scheduleId(schedule.getScheduleId())
                .installmentNumber(schedule.getInstallmentNumber())
                .amountPaid(paymentAmount)
                .newStatus(TransactionStatus.PENDING)
                .paymentReference(result.getReference())
                .build();
    }

    //SETTLE THE REPAYMENT IF THE PAYMENT GATEWAY DOES THE CASH MOVEMENT
    @Override
    public void onRepaymentSettled(Transaction tx) {
        RepaymentSchedule schedule = tx.getRepaymentSchedule();
        schedule.setStatus(ScheduleStatus.PAID);
        schedule.setPaidDate(LocalDate.now());
        repaymentScheduleRepository.save(schedule);

        boolean allPaid = repaymentScheduleRepository
                .findByLoanRequest_requestId(tx.getLoanRequest().getRequestId())
                .stream()
                .allMatch(s -> s.getStatus() == ScheduleStatus.PAID);

        if (allPaid) {
            LoanRequest loanRequest = tx.getLoanRequest();
            loanRequest.setStatus(LoanStatus.COMPLETED);
            loanRequestRepository.save(loanRequest);
        }

        eventPublisher.publishEvent(new LoanPaymentEvent(
                tx.getLoanRequest(), schedule,
                tx.getSender().getEmail(), allPaid));
    }
}
