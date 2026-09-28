package com.mb.orderflow.order.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {
    }

    public Order(UUID customerId) {
        this.customerId = Objects.requireNonNull(customerId, "CustomerId no puede ser nulo");
        this.status = OrderStatus.PENDIENTE_DE_PAGO;
        this.createdAt = Instant.now();
    }

    public void addItem(UUID productId, int quantity, BigDecimal unitPrice) {
        if (this.status != OrderStatus.PENDIENTE_DE_PAGO) {
            throw new IllegalStateException("No se pueden agregar ítems a un pedido en estado: " + this.status);
        }
        OrderItem item = new OrderItem(this, productId, quantity, unitPrice);
        this.items.add(item);
    }

    public BigDecimal calculateTotal() {
        return this.items.stream()
                .map(OrderItem::calculateSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    public void approvePayment() {
        if (this.status != OrderStatus.PENDIENTE_DE_PAGO) {
            throw new IllegalStateException(
                    "No se puede aprobar el pago de un pedido en estado: " + this.status
            );
        }
        this.status = OrderStatus.PAGO_APROBADO;
    }

    public void rejectPayment() {
        if (this.status != OrderStatus.PENDIENTE_DE_PAGO) {
            throw new IllegalStateException(
                    "No se puede rechazar el pago de un pedido en estado: " + this.status
            );
        }
        this.status = OrderStatus.PAGO_RECHAZADO;
    }

    public void cancel() {
        if (this.status != OrderStatus.PENDIENTE_DE_PAGO) {
            throw new IllegalStateException(
                    "Solo se pueden cancelar pedidos en estado PENDIENTE_DE_PAGO. Estado actual: " + this.status
            );
        }
        this.status = OrderStatus.CANCELADO;
    }

    public UUID getId() { return id; }
    public OrderStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public UUID getCustomerId() {
        return customerId;
    }
}