package com.mb.orderflow.payment.api;

import com.mb.orderflow.payment.api.dto.PaymentResponse;
import com.mb.orderflow.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{orderId}/payments")
    @Operation(summary = "Procesar pago de orden", description = "Ejecuta el cobro de una orden de forma idempotente mediante la cabecera Idempotency-Key.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago procesado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Cabecera Idempotency-Key ausente o datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
            @ApiResponse(responseCode = "409", description = "Estado incompatible para pago")
    })
    public ResponseEntity<PaymentResponse> pay(@PathVariable UUID orderId, @RequestHeader("Idempotency-Key") String idempotencyKey) {
        PaymentResponse response = paymentService.processOrderPayment(orderId, idempotencyKey);
        return ResponseEntity.ok(response);
    }
}
