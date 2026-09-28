package com.mb.orderflow.payment.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentApprovedEvent(
        UUID orderId,
        String transactionId,
        BigDecimal amount,
        Instant occurredAt
) {
    public PaymentApprovedEvent(UUID orderId, String transactionId, BigDecimal amount) {
        this(orderId, transactionId, amount, Instant.now());
    }
}