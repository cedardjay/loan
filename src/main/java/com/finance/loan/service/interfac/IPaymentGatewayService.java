package com.finance.loan.service.interfac;

import com.finance.loan.dto.input.IwomiPayoutResponse;
import com.finance.loan.dto.internal.PaymentPayoutRequest;
import com.finance.loan.dto.internal.PaymentGatewayResponse;
import com.finance.loan.entity.TransactionStatus;


public interface IPaymentGatewayService {
    PaymentGatewayResponse payout(PaymentPayoutRequest paymentPayoutRequest);
    TransactionStatus mapStatus(String gatewayStatus);


    IwomiPayoutResponse checkStatus(String internalId);
}