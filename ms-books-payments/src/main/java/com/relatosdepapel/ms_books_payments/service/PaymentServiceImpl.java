package com.relatosdepapel.ms_books_payments.service;

import com.relatosdepapel.ms_books_payments.client.BookCatalogueClient;
import com.relatosdepapel.ms_books_payments.dto.*;
import com.relatosdepapel.ms_books_payments.entity.Payment;
import com.relatosdepapel.ms_books_payments.repository.PaymentRepository;
import com.relatosdepapel.ms_books_payments.specification.PaymentSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de pagos.
 * Contiene la lógica de negocio para gestionar pagos y su integración con MS
 * Catalogue.
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository; // Inyección de dependencias Repository
    private final BookCatalogueClient catalogueClient; // Inyección de dependencias Client peticiones a MS Catalogue

    // METODOS CRUD

    /**
     * Obtiene todos los pagos del sistema.
     * 
     * @return Lista de todos los pagos como PaymentResponseDTO
     */
    @Override
    public List<PaymentResponseDTO> getAll() {
        return paymentRepository.getAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Busca un pago por ID.
     * 
     * @param id ID del pago a buscar
     * @return PaymentResponseDTO si existe, null si no existe
     */
    @Override
    public PaymentResponseDTO getById(Long id) {
        Payment payment = paymentRepository.getById(id);
        if (payment == null) {
            return null;
        }
        return toResponseDTO(payment);
    }

    /**
     * Obtiene todos los pagos de un usuario con estadísticas.
     * Incluye total de pagos y monto total gastado.
     * 
     * @param userId ID del usuario
     * @return UserPaymentsResponseDTO con pagos y totales
     */
    @Override
    public UserPaymentsResponseDTO getByUserId(Long userId) {
        List<Payment> payments = paymentRepository.getByUserId(userId);
        return toUserPaymentsDTO(userId, payments);
    }

    /**
     * Crea un nuevo pago.
     * 
     * @param dto Datos del pago a crear (userId, bookId, quantity)
     * @return PaymentResponseDTO del pago creado
     * @throws IllegalArgumentException si los datos son inválidos o stock
     *                                  insuficiente
     * @throws RuntimeException         si el libro no existe o servicio no
     *                                  disponible
     */
    @Override
    public PaymentResponseDTO create(PaymentRequestDTO dto) {
        // Validación 1: userId debe ser mayor a 0
        if (dto.getUserId() == null || dto.getUserId() <= 0) {
            throw new IllegalArgumentException("userId debe ser mayor a 0");
        }

        // Validación 2: bookId debe ser mayor a 0
        if (dto.getBookId() == null || dto.getBookId() <= 0) {
            throw new IllegalArgumentException("bookId debe ser mayor a 0");
        }

        // Validación 3: quantity debe ser mayor a 0
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new IllegalArgumentException("quantity debe ser mayor a 0");
        }

        // 1. Llamar a MS Catalogue para verificar disponibilidad
        BookAvailabilityDTO book;
        try {
            book = catalogueClient.checkAvailability(dto.getBookId());
        } catch (Exception e) {
            // Si el libro no existe (404) o hay error de conexión
            throw new IllegalArgumentException(
                    "El libro con ID " + dto.getBookId() + " no fue encontrado o el servicio no está disponible");
        }
        // 2. Validar que el libro esté marcado como visible/disponible
        if (Boolean.FALSE.equals(book.getAvailable())) {
            throw new IllegalArgumentException("El libro '" + book.getTitle() + "' no está disponible para la venta");
        }
        // 3. Validar Stock suficiente
        if (book.getStock() < dto.getQuantity()) {
            throw new IllegalArgumentException("Stock insuficiente para el libro '" + book.getTitle() +
                    "'. Solicitado: " + dto.getQuantity() + ", Disponible: " + book.getStock());
        }
        // Usar datos REALES del libro
        String bookTitle = book.getTitle();
        String bookIsbn = book.getIsbn();
        BigDecimal unitPrice = book.getPrice();

        // Usar el helper toEntity() para crear el Payment
        Payment payment = toEntity(dto, bookTitle, bookIsbn, unitPrice);

        // Guardar en la base de datos
        Payment savedPayment = paymentRepository.save(payment);

        // Decremento del stock en MS Catalogue
        try {
            // Llamar a MS Catalogue para restar el stock
            catalogueClient.decrementStock(dto.getBookId(), dto.getQuantity());
        } catch (Exception e) {
            // FALLÓ LA ACTUALIZACIÓN DE STOCK
            // ROLLBACK MANUAL: Borrar el pago que acabamos de crear para no dejar datos
            // inconsistentes
            paymentRepository.delete(savedPayment.getId());

            throw new RuntimeException(
                    "Error al actualizar el stock. Se ha cancelado el pago. Error: " + e.getMessage());
        }
        // Si todo salió bien, retornamos el pago creado
        return toResponseDTO(savedPayment);
    }

    @Override
    public PaymentResponseDTO updateStatus(Long id, PaymentStatusDTO dto) {
        // TODO: Etapa 7
        return null;
    }

    @Override
    public boolean cancelPayment(Long id) {
        // TODO: Etapa 8
        return false;
    }

    @Override
    public List<PaymentResponseDTO> search(Long userId, Long bookId, String status) {
        // TODO: Etapa 9
        return null;
    }
    // MÉTODOS HELPER

    /**
     * Convierte una entidad Payment a PaymentResponseDTO.
     *
     * @param payment Entidad Payment de la base de datos
     * @return PaymentResponseDTO para enviar al cliente
     */
    private PaymentResponseDTO toResponseDTO(Payment payment) {
        return new PaymentResponseDTO(
                payment.getId(),
                payment.getUserId(),
                payment.getBookId(),
                payment.getBookTitle(),
                payment.getBookIsbn(),
                payment.getQuantity(),
                payment.getUnitPrice(),
                payment.getTotalPrice(),
                payment.getPurchaseDate(),
                payment.getStatus());
    }

    /**
     * Convierte un PaymentRequestDTO a entidad Payment.
     * 
     * @param dto       DTO recibido del cliente
     * @param bookTitle Título del libro (obtenido de MS Catalogue)
     * @param bookIsbn  ISBN del libro (obtenido de MS Catalogue)
     * @param unitPrice Precio unitario (obtenido de MS Catalogue)
     * @return Entidad Payment lista para guardar en BD
     */
    private Payment toEntity(PaymentRequestDTO dto, String bookTitle, String bookIsbn, BigDecimal unitPrice) {
        // Multiplica precio por cantidad
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(dto.getQuantity()));

        return Payment.builder()
                .userId(dto.getUserId())
                .bookId(dto.getBookId())
                .bookTitle(bookTitle)
                .bookIsbn(bookIsbn)
                .quantity(dto.getQuantity())
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .purchaseDate(LocalDateTime.now())
                .status("PENDING")
                .build();
    }

    /**
     * Convierte una lista de pagos a UserPaymentsResponseDTO.
     * Calcula totales automáticamente.
     *
     * @param userId   ID del usuario
     * @param payments Lista de pagos del usuario
     * @return UserPaymentsResponseDTO con pagos y estadísticas
     */
    private UserPaymentsResponseDTO toUserPaymentsDTO(Long userId, List<Payment> payments) {
        // Convierte cada payment a DTO
        List<PaymentResponseDTO> paymentDTOs = payments.stream()
                .map(this::toResponseDTO)
                .toList();

        // Cuenta el número de pagos
        int totalPayments = payments.size();

        // Suma el total gastado
        BigDecimal totalAmountSpent = payments.stream()
                .map(Payment::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new UserPaymentsResponseDTO(
                userId, // ID del usuario
                paymentDTOs, // Lista de pagos del usuario
                totalPayments, // Número de pagos
                totalAmountSpent); // Total gastado
    }
}
