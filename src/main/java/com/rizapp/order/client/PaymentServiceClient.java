package com.rizapp.order.client;

import com.rizapp.order.dto.response.PaymentResponse;

import java.math.BigDecimal;

public interface PaymentServiceClient {

    PaymentResponse createPayment(String orderId, BigDecimal amount, String currency, String description);

    PaymentResponse getPaymentStatus(String orderId);
}
