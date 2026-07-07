package com.finance.loan.service.implementation;

import com.finance.loan.dto.output.PaymentGatewayResponse;
import com.finance.loan.entity.User;
import com.finance.loan.service.interfac.IPaymentGatewayService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentGatewayService implements IPaymentGatewayService {

    //MOCK IMPLEMENTATION OF PAYMENT GATEWAY

    @Override
    public PaymentGatewayResponse disburse(User platformAccount, User borrower, BigDecimal amount) {

        String paymentReference = "DIS-" + UUID.randomUUID().toString().toUpperCase();

        return PaymentGatewayResponse.builder()
                .accepted(true)
                .reference(paymentReference)
                .errorMessage(null)
                .build();
    }

    @Override
    public PaymentGatewayResponse collect(User borrower, User platformAccount, BigDecimal amount) {

        String paymentReference = "PAY-" + UUID.randomUUID().toString().toUpperCase();

        return PaymentGatewayResponse.builder()
                .accepted(true)
                .reference(paymentReference)
                .errorMessage(null)
                .build();
    }
}
