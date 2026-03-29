package com.rizapp.order.listener;

import com.rizapp.order.entity.Order;
import com.rizapp.order.enums.OrderStatus;
import com.rizapp.order.event.PaymentEvent;
import com.rizapp.order.publisher.OrderEventPublisher;
import com.rizapp.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);
    private static final Set<OrderStatus> TERMINAL_STATUSES = Set.of(
            OrderStatus.PAID, OrderStatus.FAILED, OrderStatus.CANCELLED, OrderStatus.EXPIRED
    );

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    public PaymentEventListener(OrderRepository orderRepository, OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.orderEventPublisher = orderEventPublisher;
    }

    @KafkaListener(topics = "payment-events", groupId = "order-service")
    @Transactional
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Received payment event: type={}, orderId={}, paymentId={}",
                event.getEventType(), event.getOrderId(), event.getPaymentId());

        Optional<Order> optionalOrder = orderRepository.findByOrderNumber(event.getOrderId());
        if (optionalOrder.isEmpty()) {
            log.warn("Order not found for payment event. orderId={}", event.getOrderId());
            return;
        }

        Order order = optionalOrder.get();

        if (TERMINAL_STATUSES.contains(order.getStatus())) {
            log.info("Order {} already in terminal status {}. Skipping payment event.", order.getOrderNumber(), order.getStatus());
            return;
        }

        switch (event.getEventType()) {
            case "COMPLETED" -> {
                order.setStatus(OrderStatus.PAID);
                if (event.getPaymentId() != null) {
                    try {
                        order.setPaymentId(UUID.fromString(event.getPaymentId()));
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid paymentId format: {}", event.getPaymentId());
                    }
                }
                orderRepository.save(order);
                log.info("Order {} updated to PAID", order.getOrderNumber());
                orderEventPublisher.publish(order, "PAID");
            }
            case "FAILED" -> {
                order.setStatus(OrderStatus.FAILED);
                orderRepository.save(order);
                log.info("Order {} updated to FAILED", order.getOrderNumber());
                orderEventPublisher.publish(order, "FAILED");
            }
            default -> log.warn("Unknown payment event type: {} for order: {}", event.getEventType(), order.getOrderNumber());
        }
    }
}
