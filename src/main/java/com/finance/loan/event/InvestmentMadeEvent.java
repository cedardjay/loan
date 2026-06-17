package com.finance.loan.event;

import com.finance.loan.entity.MatchedRequest;
import com.finance.loan.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InvestmentMadeEvent {
    private final MatchedRequest matchedRequest;
    private final String actorEmail;
}
