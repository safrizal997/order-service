package com.rizapp.order.publisher;

import com.rizapp.order.entity.Order;
import com.rizapp.order.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);
    private static final String TOPIC = "order-events";

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(Order order, String eventType) {
        OrderEvent event = new OrderEvent(
                eventType,
                order.getOrderNumber(),
                order.getId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getCustomerName(),
                order.getCustomerEmail()
        );

        kafkaTemplate.send(TOPIC, order.getOrderNumber(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish order event: {} for order: {}", eventType, order.getOrderNumber(), ex);
                    } else {
                        log.info("Published order event: {} for order: {}", eventType, order.getOrderNumber());
                    }
                });
    }
}
