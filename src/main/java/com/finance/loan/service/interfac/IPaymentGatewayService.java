package com.finance.loan.service.interfac;

import com.finance.loan.dto.output.PaymentResult;
import com.finance.loan.entity.User;

import java.math.BigDecimal;

public interface IPaymentGatewayService {
    PaymentResult disburse(BigDecimal amount, User recipient, String paymentReference);
}
