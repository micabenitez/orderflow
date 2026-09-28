package com.mb.orderflow.order.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    protected OrderItem() {
    }

    public OrderItem(Order order, UUID productId, int quantity, BigDecimal unitPrice) {
        this.order = Objects.requireNonNull(order, "Order no puede ser nulo");
        this.productId = Objects.requireNonNull(productId, "ProductId no puede ser nulo");

        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor a cero");
        }

        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public BigDecimal calculateSubtotal() {
        return this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }

    public UUID getId() { return id; }
    public Order getOrder() { return order; }
    public UUID getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}