package com.finance.loan.service.interfac;

import com.finance.loan.dto.output.PaymentGatewayResponse;
import com.finance.loan.entity.User;

import java.math.BigDecimal;

public interface IPaymentGatewayService {
    PaymentGatewayResponse payout(User platformAccount, User borrower, BigDecimal amount);

    PaymentGatewayResponse collect(User borrower, User platformAccount, BigDecimal amount);
}