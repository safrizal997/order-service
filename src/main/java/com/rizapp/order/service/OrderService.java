package com.rizapp.order.service;

import com.rizapp.order.dto.request.CreateOrderRequest;
import com.rizapp.order.dto.response.CreateOrderResponse;
import com.rizapp.order.dto.response.OrderDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    CreateOrderResponse createOrder(CreateOrderRequest request);

    OrderDetailResponse getOrderByOrderNumber(String orderNumber);

    OrderDetailResponse getOrderById(UUID id);

    OrderDetailResponse cancelOrder(String orderNumber);

    Page<OrderDetailResponse> listOrders(Pageable pageable);
}
