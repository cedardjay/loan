package com.finance.loan.service.implementation;

import com.finance.loan.dto.input.DisbursalRequestIN;
import com.finance.loan.dto.internal.PaymentPayoutRequest;
import com.finance.loan.dto.output.LoanDisbursementResult;
import com.finance.loan.dto.output.LoanRequestDTO;
import com.finance.loan.dto.internal.PaymentGatewayResponse;
import com.finance.loan.entity.*;
import com.finance.loan.event.LoanDisbursedEvent;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.LoanRequestRepository;
import com.finance.loan.repo.PaymentAccountRepository;
import com.finance.loan.repo.UserRepository;
import com.finance.loan.service.interfac.ILoanDisbursementService;
import com.finance.loan.service.interfac.IPaymentGatewayService;
import com.finance.loan.service.interfac.IRepaymentScheduleService;
import com.finance.loan.service.interfac.ITransactionService;
import com.finance.loan.utils.LoanRequestUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class LoanDisbursementService implements ILoanDisbursementService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRequestRepository loanRequestRepository;

    @Autowired
    private IPaymentGatewayService paymentGatewayService;

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private IRepaymentScheduleService repaymentScheduleService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private PaymentAccountRepository payoutAccountRepository;



    public LoanRequestDTO requestDisbursal(DisbursalRequestIN requestDTO, String email) {

        // --- FETCH ---
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OurException("User not found", 404));

        LoanRequest loanRequest = loanRequestRepository.findById(requestDTO.getLoanRequestId())
                .orElseThrow(() -> new OurException("Loan request not found", 404));

        PaymentAccount payoutAccount = payoutAccountRepository.findById(requestDTO.getPayoutAccountId())
                .orElseThrow(() -> new OurException("Payout account not found", 404));

        // --- VALIDATE ---
        if (!loanRequest.getBorrower().getId().equals(user.getId())) {
            throw new OurException("You do not own this loan request", 403);
        }

        if (!payoutAccount.getUser().getId().equals(user.getId())) {
            throw new OurException("You do not own this payout account", 403);
        }

        if (loanRequest.getStatus() != LoanStatus.FULLY_FUNDED) {
            throw new OurException("Loan request must be fully funded before disbursal", 400);
        }

        // --- EXECUTE ---
        loanRequest.setPaymentAccount(payoutAccount);
        loanRequest.setStatus(LoanStatus.DISBURSAL_REQUESTED);

        // --- PERSIST ---
        LoanRequest saved = loanRequestRepository.save(loanRequest);

        // --- RETURN ---
        return LoanRequestUtils.mapLoanRequestEntityToOutput(saved);
    }



    @Transactional
    public LoanDisbursementResult disburseLoan(Long loanId, String adminEmail) {

        // --- FETCH ---
        LoanRequest loanRequest = loanRequestRepository.findById(loanId)
                .orElseThrow(() -> new OurException("Loan not found with id: " + loanId, 404));

        User borrower = loanRequest.getBorrower();

        PaymentAccount payoutAccount = loanRequest.getPaymentAccount();

        User platform = userRepository.findByEmail("platform@system.internal")
                .orElseThrow(() -> new IllegalStateException("Platform account not seeded"));

// --- VALIDATE ---
        if (loanRequest.getStatus() != LoanStatus.DISBURSAL_REQUESTED) {
            throw new OurException("loan cannot be disbursed, invalid status", 400);
        }

        if (payoutAccount == null) {
            throw new OurException("Borrower has not selected a payout account for this loan", 400);
        }

// --- EXECUTE ---  PERSIST
        BigDecimal amount = loanRequest.getRequestedAmount();

        //record a pending transaction
       Transaction tx = transactionService.recordDisbursement(
                platform, borrower, loanRequest,
                amount, loanRequest.getPaymentAccount().getPaymentMethod(), loanRequest.getPaymentAccount().getAccountNumber()
        );

        PaymentPayoutRequest payload = PaymentPayoutRequest.builder()
                .operationType(OperationType.CREDIT)
                .paymentMethod(payoutAccount.getPaymentMethod())
                .amount(amount)
                .externalId(tx.getPaymentReference())
                .motif("Loan disbursal")
                .tel(payoutAccount.getAccountNumber())
                .country("cm - Cameroon")
                .build();

        PaymentGatewayResponse result = paymentGatewayService.makePayment(payload);

        if (result.getStatus() == TransactionStatus.FAILED) {
            log.warn("Disbursement failed for loanId={}, reference={}, gatewayMessage={}",
                    loanId, tx.getPaymentReference(), result.getMessage());
        }

       Transaction updatedTx = transactionService.updateTransactionResult(tx, result.getInternalId(), result.getStatus());


        // --- RETURN ---
        return LoanDisbursementResult.builder()
                .loanId(loanId)
                .borrowerEmail(borrower.getEmail())
                .amount(amount)
                .paymentReference(updatedTx.getPaymentReference())
                .status(updatedTx.getTransactionStatus())
                .build();

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
