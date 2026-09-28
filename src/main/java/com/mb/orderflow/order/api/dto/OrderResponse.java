package com.mb.orderflow.order.api.dto;
import com.mb.orderflow.order.domain.Order;
import com.mb.orderflow.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        OrderStatus status,
        BigDecimal total,
        Instant createdAt,
        List<OrderItemResponse> items
) {

    public static OrderResponse fromEntity(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.calculateSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.calculateTotal(),
                order.getCreatedAt(),
                itemResponses
        );
    }
}