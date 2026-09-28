package com.mb.orderflow.order.service;

import com.mb.orderflow.order.api.dto.CreateOrderRequest;
import com.mb.orderflow.order.api.dto.OrderItemRequest;
import com.mb.orderflow.order.api.dto.OrderResponse;
import com.mb.orderflow.order.domain.Order;
import com.mb.orderflow.order.domain.OrderItem;
import com.mb.orderflow.order.domain.OrderRepository;
import com.mb.orderflow.infrastructure.exception.ResourceNotFoundException;
import com.mb.orderflow.product.domain.Product;
import com.mb.orderflow.product.domain.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order(request.customerId());

        List<OrderItemRequest> sortedItems = request.items().stream()
                .sorted(Comparator.comparing(OrderItemRequest::productId))
                .toList();

        for (var itemRequest : sortedItems) {
            Product product = productRepository.findByIdWithLock(itemRequest.productId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Producto no encontrado: " + itemRequest.productId()
                    ));

            product.reduceStock(itemRequest.quantity());
            order.addItem(product.getId(), itemRequest.quantity(), product.getPrice());
        }

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        return orderRepository.findByIdWithItems(orderId)
                .map(OrderResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + orderId));
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponse cancelOrder(UUID orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + orderId));

        order.cancel();

        List<OrderItem> sortedItems = order.getItems().stream()
                .sorted(Comparator.comparing(OrderItem::getProductId))
                .toList();

        for (OrderItem item : sortedItems) {
            Product product = productRepository.findByIdWithLock(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + item.getProductId()));

            product.addStock(item.getQuantity());
        }

        return OrderResponse.fromEntity(order);
    }
}
