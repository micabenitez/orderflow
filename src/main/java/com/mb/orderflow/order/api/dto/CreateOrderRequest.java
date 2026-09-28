package com.mb.orderflow.order.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull(message = "El ID del cliente es obligatorio")
        UUID customerId,

        @NotNull(message = "La lista de ítems es obligatoria")
        @NotEmpty(message = "El pedido debe contener al menos un ítem")
        List<@Valid OrderItemRequest> items
) {}