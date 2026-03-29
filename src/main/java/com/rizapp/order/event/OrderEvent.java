package com.rizapp.order.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class OrderEvent {

    private String eventId;
    private String eventType;
    private String orderNumber;
    private UUID orderId;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private String customerName;
    private String customerEmail;
    private Instant timestamp;

    public OrderEvent() {
    }

    public OrderEvent(String eventType, String orderNumber, UUID orderId, String status,
                      BigDecimal totalAmount, String currency, String customerName, String customerEmail) {
        this.eventId = "evt-" + UUID.randomUUID();
        this.eventType = eventType;
        this.orderNumber = orderNumber;
        this.orderId = orderId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.timestamp = Instant.now();
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
