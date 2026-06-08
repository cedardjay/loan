package com.finance.loan.service.implementation;

import com.finance.loan.dto.output.PaymentResult;
import com.finance.loan.entity.User;
import com.finance.loan.service.interfac.IPaymentGatewayService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentGatewayService implements IPaymentGatewayService {

    //MOCK IMPLEMENTATION OF PAYMENT GATEWAY
    @Override
    public PaymentResult disburse(BigDecimal amount, User recipient, String reference) {
        return PaymentResult.builder()
                .successful(true)
                .reference(reference)
                .errorMessage(null)
                .build();
    }
}
