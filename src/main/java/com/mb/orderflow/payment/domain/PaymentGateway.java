package com.mb.orderflow.payment.domain;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentGateway {
    PaymentResult process(UUID orderId, BigDecimal amount);
}