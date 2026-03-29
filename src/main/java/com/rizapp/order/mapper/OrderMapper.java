package com.rizapp.order.mapper;

import com.rizapp.order.dto.response.CreateOrderResponse;
import com.rizapp.order.dto.response.OrderDetailResponse;
import com.rizapp.order.entity.Order;
import com.rizapp.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.status", target = "status")
    @Mapping(source = "paymentRedirectUrl", target = "paymentRedirectUrl")
    CreateOrderResponse toCreateOrderResponse(Order order, String paymentRedirectUrl);

    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "status", target = "status")
    OrderDetailResponse toOrderDetailResponse(Order order);

    @Mapping(source = "id", target = "itemId")
    OrderDetailResponse.OrderItemResponse toOrderItemResponse(OrderItem item);
}
