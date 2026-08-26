package com.finance.loan.service.implementation;

import com.finance.loan.dto.input.PayoutAccountIN;
import com.finance.loan.dto.internal.PaymentPayoutRequest;
import com.finance.loan.dto.output.LoanPaymentResult;
import com.finance.loan.dto.internal.PaymentGatewayResponse;
import com.finance.loan.entity.*;
import com.finance.loan.event.LoanPaymentEvent;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.LoanRequestRepository;
import com.finance.loan.repo.RepaymentScheduleRepository;
import com.finance.loan.repo.UserRepository;
import com.finance.loan.service.interfac.ILoanRepaymentService;
import com.finance.loan.service.interfac.IPaymentGatewayService;
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
    private IPaymentGatewayService paymentGatewayService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;


    @Transactional
    @Override
    public LoanPaymentResult loanPayment(Long loanId, PayoutAccountIN payerDetails, String borrowerEmail) {

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


        // --- PERSIST (PERSIST A PENDING TRANSACTION WHILE THE GATEWAY PROCESSES THE ACTUAL MONEY MOVEMENT) ---
       Transaction tx = transactionService.recordRepayment(
                borrower, platformAccount, loanRequest,
                paymentAmount, payerDetails.getType(), payerDetails.getAccountNumber()
        );


        PaymentPayoutRequest payload = PaymentPayoutRequest.builder()
                .operationType(OperationType.DEBIT)
                .paymentMethod(payerDetails.getType())
                .amount(paymentAmount)
                .externalId(tx.getPaymentReference())
                .motif("Loan repayment")
                .tel(payerDetails.getAccountNumber())
                .country("cm - Cameroon")
                .build();


        // --- EXECUTE (submit only — gateway confirms later via webhook or backend polling) ---
        PaymentGatewayResponse result = paymentGatewayService.makePayment(payload);

        Transaction updatedTx = transactionService.updateTransactionResult(tx, result.getInternalId(), result.getStatus());


        // --- RETURN ---
        return LoanPaymentResult.builder()
                .loanId(loanId)
                .paymentAmount(paymentAmount)
                .status(updatedTx.getTransactionStatus())
                .paymentReference(updatedTx.getPaymentReference())
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
