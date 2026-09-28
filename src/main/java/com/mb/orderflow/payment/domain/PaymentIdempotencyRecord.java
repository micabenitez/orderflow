package com.mb.orderflow.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_idempotency_records")
public class PaymentIdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private boolean successful;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected PaymentIdempotencyRecord() {
    }

    public PaymentIdempotencyRecord(String idempotencyKey, UUID orderId, boolean successful, String transactionId) {
        this.idempotencyKey = idempotencyKey;
        this.orderId = orderId;
        this.successful = successful;
        this.transactionId = transactionId;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public UUID getOrderId() { return orderId; }
    public boolean isSuccessful() { return successful; }
    public String getTransactionId() { return transactionId; }
}