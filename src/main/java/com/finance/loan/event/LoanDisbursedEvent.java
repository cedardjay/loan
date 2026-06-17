package com.finance.loan.event;

import com.finance.loan.entity.LoanRequest;
import com.finance.loan.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoanDisbursedEvent {
    private final LoanRequest loanRequest;
    private final String actorEmail;
}
