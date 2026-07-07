package com.finance.loan.event;

import com.finance.loan.entity.LoanRequest;
import com.finance.loan.entity.RepaymentSchedule;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoanPaymentEvent {
    private final LoanRequest loanRequest;
    private final RepaymentSchedule schedule;
    private final String actorEmail;
    private final boolean finalPayment; //this can be true or false
}