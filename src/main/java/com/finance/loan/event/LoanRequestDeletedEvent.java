package com.finance.loan.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoanRequestDeletedEvent {
    private final Long loanRequestId;
    private final String actorEmail;

}
