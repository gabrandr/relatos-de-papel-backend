package com.relatosdepapel.ms_books_payments.controller;

import com.relatosdepapel.ms_books_payments.dto.PaymentRequestDTO;
import com.relatosdepapel.ms_books_payments.dto.PaymentResponseDTO;
import com.relatosdepapel.ms_books_payments.dto.PaymentStatusDTO;
import com.relatosdepapel.ms_books_payments.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    /**
     * GET /api/payments
     * Obtiene todos los pagos del sistema
     *
     * @return 200 OK con lista de todos los pagos
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> getAll() {
        return ResponseEntity.ok(paymentService.getAll()); // 200 OK
    }

    /**
     * GET /api/payments/search
     * Busca pagos aplicando filtros opcionales
     *
     * @param userId ID del usuario (opcional)
     * @param bookId ID del libro (opcional)
     * @param status Estado del pago (opcional)
     * @return 200 OK con lista de pagos filtrada
     */
    @GetMapping("/search")
    public ResponseEntity<List<PaymentResponseDTO>> search(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) String status) {

        List<PaymentResponseDTO> payments = paymentService.search(userId, bookId, status);
        return ResponseEntity.ok(payments); // 200 OK
    }

    /**
     * GET /api/payments/{id}
     * Obtiene un pago por su ID
     *
     * @param id ID del pago
     * @return 200 OK con el pago encontrado
     *         404 Not Found si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getById(@PathVariable Long id) {
        PaymentResponseDTO payment = paymentService.getById(id);
        if (payment == null) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
        return ResponseEntity.ok(payment); // 200 OK
    }

    /**
     * POST /api/payments
     * Crea un nuevo pago
     *
     * @param dto Datos del pago a crear
     * @return 201 Created con el pago creado
     *         400 Bad Request si hay error de validación
     */
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> create(@RequestBody PaymentRequestDTO dto) {
        PaymentResponseDTO createdPayment = paymentService.create(dto);
        return ResponseEntity.status(201).body(createdPayment);
    }

    /**
     * PATCH /api/payments/{id}
     * Actualiza el estado de un pago
     *
     * @param id  ID del pago
     * @param dto Nuevo estado
     * @return 200 OK con el pago actualizado
     *         404 Not Found si no existe
     */
    @PatchMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody PaymentStatusDTO dto) {

        PaymentResponseDTO updatedPayment = paymentService.updateStatus(id, dto);
        return ResponseEntity.ok(updatedPayment); // 200 OK
    }

    /**
     * DELETE /api/payments/{id}
     * Cancela un pago
     *
     * @param id ID del pago a cancelar
     * @return 204 No Content si se canceló correctamente
     *         404 Not Found si el pago no existe
     *         409 Conflict si ya estaba cancelado
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        // Primero verificamos si existe (opcional, pero buena práctica REST)
        if (paymentService.getById(id) == null) {
            return ResponseEntity.notFound().build(); // 404
        }
        // Intentamos cancelar llamando al servicio
        boolean cancelled = paymentService.cancelPayment(id);
        if (!cancelled) {
            // Si devuelve false, es porque ya estaba cancelado (o no se pudo)
            return ResponseEntity.status(409).build(); // 409 Conflict
        }
        // Si todo salió bien, devolvemos 204 No Content
        return ResponseEntity.noContent().build();
    }
}