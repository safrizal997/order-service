package com.rizapp.order.dto.request;

import java.math.BigDecimal;

public class PaymentRequest {

    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String description;

    public PaymentRequest() {
    }

    public PaymentRequest(String orderId, BigDecimal amount, String currency, String description) {
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
