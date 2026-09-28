package com.mb.orderflow.payment.service;

import com.mb.orderflow.order.domain.Order;
import com.mb.orderflow.order.domain.OrderRepository;
import com.mb.orderflow.payment.api.dto.PaymentResponse;
import com.mb.orderflow.payment.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private PaymentIdempotencyRepository idempotencyRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("Debe procesar el pago y guardar registro de idempotencia en la primera llamada")
    void shouldProcessPaymentFirstTime() {
        UUID orderId = UUID.randomUUID();
        String idempotencyKey = "key-abc-123";
        Order order = new Order(UUID.randomUUID());
        order.addItem(UUID.randomUUID(), 1, new BigDecimal("100.00"));

        when(idempotencyRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.empty());
        when(orderRepository.findByIdWithItems(orderId))
                .thenReturn(Optional.of(order));
        when(paymentGateway.process(eq(order.getId()), any(BigDecimal.class)))
                .thenReturn(new PaymentResult(true, "TX-999", null));

        PaymentResponse response = paymentService.processOrderPayment(orderId, idempotencyKey);

        assertThat(response.successful()).isTrue();
        assertThat(response.transactionId()).isEqualTo("TX-999");

        verify(paymentGateway, times(1)).process(any(), any());
        verify(idempotencyRepository, times(1)).save(any(PaymentIdempotencyRecord.class));
        verify(eventPublisher, times(1)).publishEvent(any(PaymentApprovedEvent.class));
    }

    @Test
    @DisplayName("Debe retornar la respuesta previa sin volver a cobrar si la clave de idempotencia ya existe")
    void shouldReturnCachedResponseWhenIdempotencyKeyAlreadyExists() {
        UUID orderId = UUID.randomUUID();
        String idempotencyKey = "key-abc-123";

        PaymentIdempotencyRecord existingRecord = new PaymentIdempotencyRecord(
                idempotencyKey,
                orderId,
                true,
                "TX-999"
        );

        when(idempotencyRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.of(existingRecord));

        PaymentResponse response = paymentService.processOrderPayment(orderId, idempotencyKey);

        assertThat(response.successful()).isTrue();
        assertThat(response.transactionId()).isEqualTo("TX-999");
        assertThat(response.orderId()).isEqualTo(orderId);

        verify(paymentGateway, never()).process(any(), any());
        verify(orderRepository, never()).findByIdWithItems(any());
        verify(idempotencyRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}