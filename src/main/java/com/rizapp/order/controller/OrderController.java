package com.rizapp.order.controller;

import com.rizapp.order.dto.request.CreateOrderRequest;
import com.rizapp.order.dto.response.ApiResponse;
import com.rizapp.order.dto.response.CreateOrderResponse;
import com.rizapp.order.dto.response.OrderDetailResponse;
import com.rizapp.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", response));
    }

    @GetMapping("/{orderNumber}")
    @Operation(summary = "Get order by order number")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        OrderDetailResponse response = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success("Order found", response));
    }

    @GetMapping("/id/{orderId}")
    @Operation(summary = "Get order by UUID")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderById(@PathVariable UUID orderId) {
        OrderDetailResponse response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order found", response));
    }

    @PatchMapping("/{orderNumber}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> cancelOrder(@PathVariable String orderNumber) {
        OrderDetailResponse response = orderService.cancelOrder(orderNumber);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", response));
    }

    @GetMapping
    @Operation(summary = "List orders with pagination")
    public ResponseEntity<ApiResponse<Page<OrderDetailResponse>>> listOrders(Pageable pageable) {
        Page<OrderDetailResponse> response = orderService.listOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved", response));
    }
}
