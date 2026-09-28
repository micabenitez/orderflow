package com.mb.orderflow.order;

import com.mb.orderflow.order.api.dto.CreateOrderRequest;
import com.mb.orderflow.order.api.dto.OrderItemRequest;
import com.mb.orderflow.order.api.dto.OrderResponse;
import com.mb.orderflow.order.domain.Order;
import com.mb.orderflow.order.domain.OrderRepository;
import com.mb.orderflow.order.domain.OrderStatus;
import com.mb.orderflow.order.service.OrderService;
import com.mb.orderflow.product.domain.Product;
import com.mb.orderflow.product.domain.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("Debe crear un pedido exitosamente cuando los datos son válidos")
    void shouldCreateOrderSuccessfully() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("100.50");
        int initialStock = 10;
        int requestedQuantity = 2;

        Product product = new Product(productId, "Teclado Mecánico", price, initialStock);
        CreateOrderRequest request = new CreateOrderRequest(
                customerId,
                List.of(new OrderItemRequest(productId, requestedQuantity, price))
        );

        when(productRepository.findByIdWithLock(productId))
                .thenReturn(Optional.of(product));

        UUID generatedOrderId = UUID.randomUUID();
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            ReflectionTestUtils.setField(o, "id", generatedOrderId);
            return o;
        });

        OrderResponse response = orderService.createOrder(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(generatedOrderId);
        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.status()).isEqualTo(OrderStatus.PENDIENTE_DE_PAGO);
        assertThat(response.items()).hasSize(1);
        assertThat(response.total()).isEqualByComparingTo(new BigDecimal("201.00"));
        assertThat(product.getStock()).isEqualTo(8);
        verify(productRepository, times(1)).findByIdWithLock(productId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }
    @Test
    @DisplayName("Debe lanzar excepción si un ítem tiene cantidad inválida")
    void shouldThrowExceptionWhenItemHasInvalidQuantity() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Product product = new Product("Monitor", new BigDecimal("50.00"), 5);

        CreateOrderRequest request = new CreateOrderRequest(
                customerId,
                List.of(new OrderItemRequest(productId, 0, new BigDecimal("50.00")))
        );

        when(productRepository.findByIdWithLock(productId))
                .thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La cantidad a descontar debe ser mayor a cero");
        verify(orderRepository, never()).save(any(Order.class));
    }
}