package com.mb.orderflow.payment.service;

import com.mb.orderflow.order.domain.Order;
import com.mb.orderflow.order.domain.OrderRepository;
import com.mb.orderflow.payment.api.dto.PaymentResponse;
import com.mb.orderflow.payment.domain.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentIdempotencyRepository idempotencyRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentService(
            OrderRepository orderRepository,
            PaymentGateway paymentGateway,
            PaymentIdempotencyRepository idempotencyRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
        this.idempotencyRepository = idempotencyRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(rollbackFor = Exception.class)
    public PaymentResponse processOrderPayment(UUID orderId, String idempotencyKey) {
        Optional<PaymentIdempotencyRecord> existingRecord = idempotencyRepository.findByIdempotencyKey(idempotencyKey);
        if (existingRecord.isPresent()) {
            PaymentIdempotencyRecord record = existingRecord.get();
            return new PaymentResponse(record.getOrderId(), record.isSuccessful(), record.getTransactionId());
        }

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + orderId));

        PaymentResult result = paymentGateway.process(order.getId(), order.calculateTotal());

        if (result.approved()) {
            order.approvePayment();
            eventPublisher.publishEvent(
                    new PaymentApprovedEvent(order.getId(), result.externalTransactionId(), order.calculateTotal())
            );
        } else {
            order.rejectPayment();
        }

        PaymentIdempotencyRecord idempotencyRecord = new PaymentIdempotencyRecord(
                idempotencyKey,
                order.getId(),
                result.approved(),
                result.externalTransactionId()
        );
        idempotencyRepository.save(idempotencyRecord);

        return new PaymentResponse(order.getId(), result.approved(), result.externalTransactionId());
    }
}