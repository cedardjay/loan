package com.finance.loan.service.interfac;

import com.finance.loan.dto.internal.PaymentPayoutRequest;
import com.finance.loan.dto.internal.PaymentGatewayResponse;


public interface IPaymentGatewayService {
    PaymentGatewayResponse payout(PaymentPayoutRequest paymentPayoutRequest);

}