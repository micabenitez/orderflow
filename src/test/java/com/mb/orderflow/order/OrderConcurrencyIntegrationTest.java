package com.mb.orderflow.order;

import com.mb.orderflow.order.api.dto.CreateOrderRequest;
import com.mb.orderflow.order.api.dto.OrderItemRequest;
import com.mb.orderflow.order.service.OrderService;
import com.mb.orderflow.product.domain.Product;
import com.mb.orderflow.product.domain.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OrderConcurrencyIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Debe permitir solo 1 compra y rechazar 9 cuando 10 hilos compiten por 1 unidad de stock")
    void shouldPreventOverbookingUnderHighConcurrency() throws InterruptedException {
        Product product = new Product("Mouse Gamer Edición Limitada", new BigDecimal("80.00"), 1);
        Product savedProduct = productRepository.saveAndFlush(product);

        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successfulOrders = new AtomicInteger(0);
        AtomicInteger failedOrders = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();

                    CreateOrderRequest request = new CreateOrderRequest(
                            UUID.randomUUID(),
                            List.of(new OrderItemRequest(savedProduct.getId(), 1, new BigDecimal("80.00")))
                    );

                    orderService.createOrder(request);
                    successfulOrders.incrementAndGet();
                } catch (Exception e) {
                    failedOrders.incrementAndGet();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        boolean completedInTime = completionLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(completedInTime).as("Los hilos debieron terminar dentro del tiempo límite").isTrue();
        assertThat(successfulOrders.get()).as("Solo un pedido debió concretarse").isEqualTo(1);
        assertThat(failedOrders.get()).as("Nueve pedidos debieron ser rechazados").isEqualTo(9);

        Product updatedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertThat(updatedProduct.getStock()).as("El stock final no puede ser negativo").isZero();
    }
}