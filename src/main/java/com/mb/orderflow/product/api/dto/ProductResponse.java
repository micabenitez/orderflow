package com.mb.orderflow.product.api.dto;

import com.mb.orderflow.product.domain.Product;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        BigDecimal price,
        int stock
) {
    public static ProductResponse fromEntity(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock()
        );
    }
}