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
import com.finance.loan.repo.PayoutAccountRepository;
import com.finance.loan.repo.UserRepository;
import com.finance.loan.service.interfac.ILoanDisbursementService;
import com.finance.loan.service.interfac.IPaymentGatewayService;
import com.finance.loan.service.interfac.IRepaymentScheduleService;
import com.finance.loan.utils.LoanRequestUtils;
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
    private IPaymentGatewayService paymentGatewayService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private IRepaymentScheduleService repaymentScheduleService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private PayoutAccountRepository payoutAccountRepository;



    public LoanRequestDTO requestDisbursal(DisbursalRequestIN requestDTO, String email) {

        // --- FETCH ---
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OurException("User not found", 404));

        LoanRequest loanRequest = loanRequestRepository.findById(requestDTO.getLoanRequestId())
                .orElseThrow(() -> new OurException("Loan request not found", 404));

        PayoutAccount payoutAccount = payoutAccountRepository.findById(requestDTO.getPayoutAccountId())
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
        loanRequest.setPayoutAccount(payoutAccount);
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

        PayoutAccount payoutAccount = loanRequest.getPayoutAccount();

        User platform = userRepository.findByEmail("platform@system.internal")
                .orElseThrow(() -> new IllegalStateException("Platform account not seeded"));

        PayoutAccount platformPayoutAccount = payoutAccountRepository.findByUserId(platform.getId())
                .orElseThrow(() -> new IllegalStateException("Platform payout account not set"));

// --- VALIDATE ---
        if (loanRequest.getStatus() != LoanStatus.FULLY_FUNDED) {
            throw new OurException("Only fully funded loans can be disbursed", 400);
        }

        if (payoutAccount == null) {
            throw new OurException("Borrower has not selected a payout account for this loan", 400);
        }

// --- EXECUTE ---  PERSIST
        BigDecimal amount = loanRequest.getRequestedAmount();

        PaymentPayoutRequest payload = PaymentPayoutRequest.builder()
                .opType("credit")
                .type(String.valueOf(payoutAccount.getType()))
                .amount(amount)
                .externalId(loanRequest.getRequestId().toString())
                .motif("Loan disbursal")
                .tel(payoutAccount.getAccountNumber())
                .country("cm - Cameroon")
                .build();

        PaymentGatewayResponse result = paymentGatewayService.payout(payload);

//record a pending transaction immediately
      transactionService.recordDisbursement(
                platform, borrower, loanRequest,
                amount, result.getInternalId(), TransactionStatus.PENDING
        );

        switch (result.getStatus()) {
            case COMPLETED:
               Transaction completedTx = transactionService.settleTransaction(result.getInternalId(), result.getStatus());
                onDisbursementSettled(completedTx);
                break;

            case FAILED:
               transactionService.settleTransaction(result.getInternalId(), result.getStatus());
                throw new OurException("rejected the request: " + result.getMessage(), 400);

            default:
                throw new OurException("Unknown gateway status: " + result.getStatus(), 500);
        }

        // --- RETURN ---
        return LoanDisbursementResult.builder()
                .loanId(loanId)
                .borrowerEmail(borrower.getEmail())
                .amount(amount)
                .paymentReference(result.getInternalId())
                .status(result.getStatus())
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
