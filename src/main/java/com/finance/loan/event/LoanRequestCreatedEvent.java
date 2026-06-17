package com.finance.loan.event;

import com.finance.loan.entity.LoanRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoanRequestCreatedEvent {
    private final LoanRequest loanRequest;
    private final String actorEmail;

}
