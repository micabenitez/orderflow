package com.mb.orderflow.payment.domain;

public record PaymentResult(
        boolean approved,
        String externalTransactionId,
        String failureReason
) {}