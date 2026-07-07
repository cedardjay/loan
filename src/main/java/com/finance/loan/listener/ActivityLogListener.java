package com.finance.loan.listener;

import com.finance.loan.entity.ActivityLog;
import com.finance.loan.event.*;
import com.finance.loan.repo.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ActivityLogListener {

    private final ActivityLogRepository activityLogRepository;
    private final ObjectMapper objectMapper;

    @Async
    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        log("USER_REGISTERED", "User", event.getUser().getId(),
                event.getActorEmail(),
                Map.of(
                        "email", event.getUser().getEmail(),
                        "name", event.getUser().getName(),
                        "role", event.getUser().getRole().name()
                ));
    }

    @Async
    @EventListener
    public void onUserRoleGranted(UserRoleGrantedEvent event) {
        log("USER_ROLE_GRANTED", "User", event.getUser().getId(),
                event.getActorEmail(),
                Map.of(
                        "targetEmail", event.getUser().getEmail(),
                        "newRole", event.getUser().getRole().name()
                ));
    }

    @Async
    @EventListener
    public void onLoanRequestCreated(LoanRequestCreatedEvent event) {
        log("LOAN_REQUEST_CREATED", "LoanRequest", event.getLoanRequest().getRequestId(),
                event.getActorEmail(),
                Map.of(
                        "requestedAmount", event.getLoanRequest().getRequestedAmount(),
                        "purpose", event.getLoanRequest().getPurpose()
                ));
    }

    @Async
    @EventListener
    public void onLoanRequestApproved(LoanRequestApprovedEvent event) {
        log("LOAN_REQUEST_APPROVED", "LoanRequest", event.getLoanRequest().getRequestId(),
                event.getActorEmail(),
                Map.of(
                        "borrowerEmail", event.getLoanRequest().getBorrower().getEmail(),
                        "requestedAmount", event.getLoanRequest().getRequestedAmount(),
                        "previousStatus", "PENDING_APPROVAL",
                        "newStatus", "APPROVED"
                ));
    }

    @Async
    @EventListener
    public void onLoanRequestRejected(LoanRequestRejectedEvent event) {
        log("LOAN_REQUEST_REJECTED", "LoanRequest", event.getLoanRequest().getRequestId(),
                event.getActorEmail(),
                Map.of(
                        "borrowerEmail", event.getLoanRequest().getBorrower().getEmail(),
                        "previousStatus", "PENDING_APPROVAL",
                        "newStatus", "REJECTED"
                ));
    }

    @Async
    @EventListener
    public void onLoanRequestDeleted(LoanRequestDeletedEvent event) {
        log("LOAN_REQUEST_DELETED", "LoanRequest", event.getLoanRequestId(),
                event.getActorEmail(),
                Map.of(
                        "loanRequestId", event.getLoanRequestId()
                ));
    }

    @Async
    @EventListener
    public void onLoanDisbursed(LoanDisbursedEvent event) {
        log("LOAN_DISBURSED", "LoanRequest", event.getLoanRequest().getRequestId(),
                event.getActorEmail(),
                Map.of(
                        "borrowerEmail", event.getLoanRequest().getBorrower().getEmail(),
                        "amount", event.getLoanRequest().getRequestedAmount(),
                        "newStatus", "ACTIVE"
                ));
    }

    @Async
    @EventListener
    public void onInvestmentMade(InvestmentMadeEvent event) {
        log("INVESTMENT_MADE", "MatchedRequest", event.getMatchedRequest().getMatchId(),
                event.getActorEmail(),
                Map.of(
                        "investorEmail", event.getMatchedRequest().getInvestor().getEmail(),
                        "loanRequestId", event.getMatchedRequest().getLoanRequest().getRequestId(),
                        "amount", event.getMatchedRequest().getInvestorAmount()
                ));
    }

    @Async
    @EventListener
    public void onLoanFullyFunded(LoanFullyFundedEvent event) {
        log("LOAN_FULLY_FUNDED", "LoanRequest", event.getLoanRequest().getRequestId(),
                event.getActorEmail(),
                Map.of(
                        "borrowerEmail", event.getLoanRequest().getBorrower().getEmail(),
                        "totalAmount", event.getLoanRequest().getRequestedAmount()
                ));
    }

    @Async
    @EventListener
    public void onLoanPayment(LoanPaymentEvent event) {
        String action = event.isFinalPayment() ? "LOAN_FULLY_REPAID" : "LOAN_PAYMENT_MADE";

        log(action, "LoanRequest", event.getLoanRequest().getRequestId(),
                event.getActorEmail(),
                Map.of(
                        "borrowerEmail", event.getLoanRequest().getBorrower().getEmail(),
                        "scheduleId", event.getSchedule().getScheduleId(),
                        "amountPaid", event.getSchedule().getAmountPaid(),
                        "dueDate", event.getSchedule().getDueDate().toString(),
                        "finalPayment", event.isFinalPayment()
                ));
    }


    private void log(String action, String entityType, Long entityId,
                     String actorEmail, Map<String, Object> details) {
        try {
            activityLogRepository.save(ActivityLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .actorEmail(actorEmail)
                    .details(objectMapper.writeValueAsString(details))
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize activity log details", e);
        }
    }
}
