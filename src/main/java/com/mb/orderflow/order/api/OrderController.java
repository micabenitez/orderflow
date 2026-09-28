package com.mb.orderflow.order.api;

import com.mb.orderflow.order.api.dto.CreateOrderRequest;
import com.mb.orderflow.order.api.dto.OrderResponse;
import com.mb.orderflow.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Órdenes", description = "Endpoints para creación, consulta y ciclo de vida de órdenes")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Crear una nueva orden", description = "Crea un pedido en estado PENDIENTE_DE_PAGO y reserva stock de forma pesimista.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orden creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o stock insuficiente (RFC 7807)")
    })
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Consultar orden por ID", description = "Recupera una orden con todos sus ítems asociados mediante JOIN FETCH.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden encontrada"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada (RFC 7807)")
    })
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "UUID de la orden a consultar", required = true)
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @PatchMapping("/{orderId}/cancel")
    @Operation(summary = "Cancelar orden", description = "Cancela una orden pendiente y restituye el stock reservado de los productos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden cancelada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada (RFC 7807)"),
            @ApiResponse(responseCode = "409", description = "Conflicto: la orden no se encuentra en estado PENDIENTE_DE_PAGO (RFC 7807)")
    })
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(description = "UUID de la orden a cancelar", required = true)
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
}