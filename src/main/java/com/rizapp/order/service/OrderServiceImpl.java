package com.rizapp.order.service;

import com.rizapp.order.client.PaymentServiceClient;
import com.rizapp.order.dto.request.CreateOrderRequest;
import com.rizapp.order.dto.request.OrderItemRequest;
import com.rizapp.order.dto.response.CreateOrderResponse;
import com.rizapp.order.dto.response.OrderDetailResponse;
import com.rizapp.order.dto.response.PaymentResponse;
import com.rizapp.order.entity.Order;
import com.rizapp.order.entity.OrderItem;
import com.rizapp.order.enums.OrderStatus;
import com.rizapp.order.exception.InvalidOrderStateException;
import com.rizapp.order.exception.OrderNotFoundException;
import com.rizapp.order.mapper.OrderMapper;
import com.rizapp.order.publisher.OrderEventPublisher;
import com.rizapp.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    private static final DateTimeFormatter ORDER_NUMBER_FORMAT = DateTimeFormatter
            .ofPattern("yyyyMMddHHmmss")
            .withZone(ZoneId.systemDefault());

    private final OrderRepository orderRepository;
    private final PaymentServiceClient paymentServiceClient;
    private final OrderEventPublisher orderEventPublisher;
    private final OrderMapper orderMapper;
    private final Random random = new Random();

    public OrderServiceImpl(OrderRepository orderRepository,
                            PaymentServiceClient paymentServiceClient,
                            OrderEventPublisher orderEventPublisher,
                            OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.paymentServiceClient = paymentServiceClient;
        this.orderEventPublisher = orderEventPublisher;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setCurrency(request.getCurrency() != null ? request.getCurrency() : "IDR");
        order.setNotes(request.getNotes());
        order.setStatus(OrderStatus.CREATED);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest itemReq : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setProductName(itemReq.getProductName());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());
            BigDecimal subtotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            item.setSubtotal(subtotal);
            order.addItem(item);
            totalAmount = totalAmount.add(subtotal);
        }
        order.setTotalAmount(totalAmount);

        order = orderRepository.save(order);
        log.info("Order created: {}", order.getOrderNumber());

        String paymentRedirectUrl = null;
        try {
            PaymentResponse paymentResponse = paymentServiceClient.createPayment(
                    order.getOrderNumber(),
                    order.getTotalAmount(),
                    order.getCurrency(),
                    "Payment for Order " + order.getOrderNumber()
            );
            order.setPaymentId(paymentResponse.getPaymentId());
            order.setStatus(OrderStatus.AWAITING_PAYMENT);
            paymentRedirectUrl = paymentResponse.getRedirectUrl();
            order = orderRepository.save(order);
            log.info("Payment initiated for order: {}, paymentId: {}", order.getOrderNumber(), paymentResponse.getPaymentId());
        } catch (Exception e) {
            log.error("Failed to initiate payment for order: {}. Order remains in CREATED status.", order.getOrderNumber(), e);
        }

        orderEventPublisher.publish(order, "CREATED");

        return orderMapper.toCreateOrderResponse(order, paymentRedirectUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));
        return orderMapper.toOrderDetailResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        return orderMapper.toOrderDetailResponse(order);
    }

    @Override
    @Transactional
    public OrderDetailResponse cancelOrder(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));

        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new InvalidOrderStateException(
                    "Cannot cancel order with status " + order.getStatus() +
                    ". Only CREATED or AWAITING_PAYMENT orders can be cancelled.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);
        log.info("Order cancelled: {}", orderNumber);

        orderEventPublisher.publish(order, "CANCELLED");

        return orderMapper.toOrderDetailResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDetailResponse> listOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(orderMapper::toOrderDetailResponse);
    }

    private String generateOrderNumber() {
        String timestamp = ORDER_NUMBER_FORMAT.format(Instant.now());
        int randomNum = 1000 + random.nextInt(9000);
        return "ORD-" + timestamp + "-" + randomNum;
    }
}
