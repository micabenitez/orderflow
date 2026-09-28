package com.mb.orderflow.infrastructure.notification.service;

import com.mb.orderflow.payment.domain.PaymentApprovedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ConsoleNotificationListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentApproved(PaymentApprovedEvent event) {
        System.out.printf(
                "[NOTIFICACIÓN] Pago exitoso para pedido %s por $%s (Tx: %s)%n",
                event.orderId(),
                event.amount(),
                event.transactionId()
        );
    }
}