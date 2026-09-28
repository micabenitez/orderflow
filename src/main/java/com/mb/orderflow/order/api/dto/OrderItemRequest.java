package com.mb.orderflow.order.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;


public record OrderItemRequest(
        @NotNull(message = "El ID del producto es obligatorio")
        UUID productId,

        @Positive(message = "La cantidad debe ser mayor a cero")
        int quantity,

        @NotNull(message = "El precio unitario es obligatorio")
        @Positive(message = "El precio unitario debe ser mayor a cero")
        BigDecimal unitPrice
) {}
