package com.mb.orderflow.payment.api.dto;

import java.util.UUID;

public record PaymentResponse(
        UUID orderId,
        boolean successful,
        String transactionId
) {}