package com.finance.loan.event;

import com.finance.loan.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRoleGrantedEvent {
    private final User user;
    private final String actorEmail;
}
