package com.mb.orderflow.infrastructure;
import com.mb.orderflow.payment.domain.PaymentGateway;
import com.mb.orderflow.payment.domain.PaymentResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class SimulatedPaymentGateway implements PaymentGateway {

    @Override
    public PaymentResult process(UUID orderId, BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("1000000")) > 0) {
            return new PaymentResult(false, null, "Límite de crédito excedido");
        }

        String fakeExternalId = "PAY-" + UUID.randomUUID();
        return new PaymentResult(true, fakeExternalId, null);
    }
}