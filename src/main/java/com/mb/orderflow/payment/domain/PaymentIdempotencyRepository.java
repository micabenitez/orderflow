package com.mb.orderflow.payment.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PaymentIdempotencyRepository extends JpaRepository<PaymentIdempotencyRecord, UUID> {
    Optional<PaymentIdempotencyRecord> findByIdempotencyKey(String idempotencyKey);
}