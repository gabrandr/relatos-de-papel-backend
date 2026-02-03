# API REST - Microservicio Operador (ms-books-payments)

> **Microservicio encargado de registrar y gestionar las compras de libros en "Relatos de Papel"**

---

## 📋 Información General

| Campo             | Valor                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **Nombre**        | ms-books-payments                                                  |
| **Puerto**        | 8082                                                               |
| **Base URL**      | `/api/payments`                                                    |
| **Base de Datos** | payments_db (H2 o MySQL/PostgreSQL) - **DIFERENTE a catalogue_db** |
| **Nombre Eureka** | MS-BOOKS-PAYMENTS                                                  |
| **Dependencia**   | Consume API de ms-books-catalogue vía Eureka (sin IP ni puerto)    |

---

## 🚀 Spring Initializr

**URL:** https://start.spring.io

| Campo        | Valor                       |
| ------------ | --------------------------- |
| Project      | Maven                       |
| Language     | Java                        |
| Spring Boot  | 3.2.x                       |
| Group        | com.relatosdepapel          |
| Artifact     | ms-books-payments           |
| Package name | com.relatosdepapel.payments |
| Java         | 17 o 21                     |

**Dependencias:**

- ✅ Spring Web
- ✅ Spring Data JPA
- ✅ H2 Database
- ✅ Eureka Discovery Client
- ✅ Spring Boot Actuator
- ✅ Lombok

---

## 📁 Estructura de Paquetes

```
src/main/java/com/relatosdepapel/payments/
├── MsBooksPaymentsApplication.java
├── controller/
│   └── PaymentController.java
├── service/
│   ├── PaymentService.java           ← Interface
│   └── PaymentServiceImpl.java       ← Implementación
├── repository/
│   ├── PaymentJpaRepository.java     ← Interface JPA
│   └── PaymentRepository.java        ← Wrapper
├── specification/
│   └── PaymentSpecification.java     ← Filtros dinámicos (Criteria API)
├── entity/
│   └── Payment.java
├── dto/
│   ├── PaymentRequestDTO.java
│   ├── PaymentResponseDTO.java
│   ├── PaymentStatusDTO.java
│   ├── BookAvailabilityDTO.java
│   ├── StockUpdateDTO.java
│   ├── UserPaymentsResponseDTO.java
│   └── ErrorResponseDTO.java
├── client/
│   └── BookCatalogueClient.java      ← Cliente HTTP para MS Catalogue
├── config/
│   └── RestTemplateConfig.java       ← Configuración @LoadBalanced
└── utils/
    └── Consts.java                   ← Constantes (nombres de columnas)
```

> ⚠️ **Nota:** No se usa `GlobalExceptionHandler`. El manejo de errores se hace con `ResponseEntity` en el Controller.

---

## 📦 Entidad: Payment

| Atributo       | Tipo          | Descripción                         |
| -------------- | ------------- | ----------------------------------- |
| `id`           | Long          | Identificador único (auto-generado) |
| `userId`       | Long          | ID del usuario                      |
| `bookId`       | Long          | ID del libro                        |
| `bookTitle`    | String        | Título (desnormalizado)             |
| `bookIsbn`     | String        | ISBN (desnormalizado)               |
| `quantity`     | Integer       | Cantidad comprada                   |
| `unitPrice`    | BigDecimal    | Precio unitario                     |
| `totalPrice`   | BigDecimal    | quantity × unitPrice                |
| `purchaseDate` | LocalDateTime | Fecha de compra                     |
| `status`       | String        | COMPLETED, CANCELLED                |

---

## 🔗 Tabla de Endpoints

| Método HTTP | URI                            | Query Params           | Request Body      | Response Body           | Códigos            |
| ----------- | ------------------------------ | ---------------------- | ----------------- | ----------------------- | ------------------ |
| POST        | `/api/payments`                | N/A                    | PaymentRequestDTO | PaymentResponseDTO      | 201, 400, 404, 409 |
| GET         | `/api/payments/{id}`           | N/A                    | N/A               | PaymentResponseDTO      | 200, 404           |
| GET         | `/api/payments`                | userId, bookId, status | N/A               | List                    | 200                |
| GET         | `/api/users/{userId}/payments` | status                 | N/A               | UserPaymentsResponseDTO | 200                |
| PATCH       | `/api/payments/{id}`           | N/A                    | PaymentStatusDTO  | PaymentResponseDTO      | 200, 400, 404      |
| DELETE      | `/api/payments/{id}`           | N/A                    | N/A               | Void                    | 204, 404, 409      |

---

## 📝 Detalle de Endpoints

### POST /api/payments - Registrar compra

> ⚠️ **CRÍTICO**: Valida el libro llamando a ms-books-catalogue vía Eureka (sin IP ni puerto).

**Flujo de validación:**

```
1. Recibe: POST /api/payments {userId, bookId, quantity}
2. Llama: GET http://MS-BOOKS-CATALOGUE/api/books/{bookId}/availability
3. Verifica: existe, visible=true, stock >= quantity
4. Llama: PATCH http://MS-BOOKS-CATALOGUE/api/books/{bookId}/stock {quantity: -N}
5. Guarda: Payment en payments_db
6. Retorna: PaymentResponseDTO
```

**Request Body (PaymentRequestDTO):**

```json
{
  "userId": 123,
  "bookId": 1,
  "quantity": 2
}
```

**Response 201 Created (PaymentResponseDTO):**

```json
{
  "id": 1,
  "userId": 123,
  "bookId": 1,
  "bookTitle": "El Quijote",
  "bookIsbn": "9788467033601",
  "quantity": 2,
  "unitPrice": 19.99,
  "totalPrice": 39.98,
  "purchaseDate": "2024-01-15T14:30:00",
  "status": "COMPLETED"
}
```

**Response 400 Bad Request:**

```json
{
  "code": 400,
  "message": "La cantidad debe ser al menos 1"
}
```

**Response 404 Not Found:**

```json
{
  "code": 404,
  "message": "Libro no encontrado"
}
```

**Response 409 Conflict:**

```json
{
  "code": 409,
  "message": "Stock insuficiente. Disponible: 5"
}
```

---

### GET /api/payments/{id} - Obtener compra por ID

**Response 200 OK (PaymentResponseDTO):**

```json
{
  "id": 1,
  "userId": 123,
  "bookId": 1,
  "bookTitle": "El Quijote",
  "bookIsbn": "9788467033601",
  "quantity": 2,
  "unitPrice": 19.99,
  "totalPrice": 39.98,
  "purchaseDate": "2024-01-15T14:30:00",
  "status": "COMPLETED"
}
```

**Response 404 Not Found:** (sin body)

---

### GET /api/payments - Listar compras con filtros

**Query Parameters:**

| Parámetro | Tipo   | Descripción         |
| --------- | ------ | ------------------- |
| `userId`  | Long   | Filtrar por usuario |
| `bookId`  | Long   | Filtrar por libro   |
| `status`  | String | Filtrar por estado  |

**Response 200 OK:**

```json
[
  {
    "id": 1,
    "userId": 123,
    "bookId": 1,
    "bookTitle": "El Quijote",
    "quantity": 2,
    "totalPrice": 39.98,
    "status": "COMPLETED",
    "purchaseDate": "2024-01-15T14:30:00"
  }
]
```

---

### GET /api/users/{userId}/payments - Compras de un usuario

**Response 200 OK (UserPaymentsResponseDTO):**

```json
{
  "userId": 123,
  "payments": [
    {
      "id": 1,
      "bookId": 1,
      "bookTitle": "El Quijote",
      "quantity": 2,
      "totalPrice": 39.98,
      "status": "COMPLETED",
      "purchaseDate": "2024-01-15T14:30:00"
    }
  ],
  "totalPayments": 1,
  "totalAmountSpent": 39.98
}
```

---

### PATCH /api/payments/{id} - Actualizar estado

**Request Body (PaymentStatusDTO):**

```json
{
  "status": "CANCELLED"
}
```

**Response 200 OK:** PaymentResponseDTO actualizado

**Lógica:** Si se cancela, restaurar stock en ms-books-catalogue.

---

### DELETE /api/payments/{id} - Cancelar compra

**Response 204 No Content:**

(sin body)

**Response 404 Not Found:** (sin body)

**Response 409 Conflict:**

```json
{
  "code": 409,
  "message": "El pago ya está cancelado"
}
```

**Lógica:** Cambia estado a CANCELLED y restaura stock.

---

## 🛠️ Implementación

### Utils - Consts.java

> **Buena práctica:** Usar constantes para nombres de columnas evita errores de tipeo y facilita el mantenimiento.

```java
package com.relatosdepapel.payments.utils;

public class Consts {
    // Campos de la entidad Payment
    public static final String ID = "id";
    public static final String USER_ID = "userId";
    public static final String BOOK_ID = "bookId";
    public static final String BOOK_TITLE = "bookTitle";
    public static final String QUANTITY = "quantity";
    public static final String TOTAL_PRICE = "totalPrice";
    public static final String STATUS = "status";
    public static final String PURCHASE_DATE = "purchaseDate";
    public static final String ORDER_ID = "orderId";
}
```

---

### Entity - Payment.java

```java
package com.relatosdepapel.payments.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long bookId;

    private String bookTitle;

    private String bookIsbn;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private LocalDateTime purchaseDate;

    @Column(nullable = false)
    private String status;
}
```

---

### DTOs

#### PaymentRequestDTO.java

```java
package com.relatosdepapel.payments.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {
    private Long userId;
    private Long bookId;
    private Integer quantity;
}
```

#### PaymentResponseDTO.java

```java
package com.relatosdepapel.payments.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private Long id;
    private Long userId;
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private LocalDateTime purchaseDate;
    private String status;
}
```

#### PaymentStatusDTO.java

```java
package com.relatosdepapel.payments.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusDTO {
    private String status;
}
```

#### BookAvailabilityDTO.java

```java
package com.relatosdepapel.payments.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookAvailabilityDTO {
    private Long id;
    private String title;
    private String isbn;
    private Boolean available;
    private Boolean visible;
    private Integer stock;
    private BigDecimal price;
}
```

#### StockUpdateDTO.java

```java
package com.relatosdepapel.payments.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateDTO {
    private Integer quantity;
}
```

#### UserPaymentsResponseDTO.java

```java
package com.relatosdepapel.payments.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPaymentsResponseDTO {
    private Long userId;
    private List<PaymentResponseDTO> payments;
    private Integer totalPayments;
    private BigDecimal totalAmountSpent;
}
```

#### ErrorResponseDTO.java

```java
package com.relatosdepapel.payments.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    private Integer code;
    private String message;
}
```

---

### Repository - Capa 1: PaymentJpaRepository.java

```java
package com.relatosdepapel.payments.repository;

import com.relatosdepapel.payments.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUserId(Long userId);
    List<Payment> findByBookId(Long bookId);
    List<Payment> findByStatus(String status);
    List<Payment> findByUserIdAndStatus(Long userId, String status);
}
```

---

### Repository - Capa 2: PaymentRepository.java (Wrapper)

```java
package com.relatosdepapel.payments.repository;

import com.relatosdepapel.payments.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    public List<Payment> getAll() {
        return jpaRepository.findAll();
    }

    public Payment getById(Long id) {
        return jpaRepository.findById(id).orElse(null);
    }

    public Payment save(Payment payment) {
        return jpaRepository.save(payment);
    }

    public void delete(Payment payment) {
        jpaRepository.delete(payment);
    }

    public List<Payment> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }

    public List<Payment> findByBookId(Long bookId) {
        return jpaRepository.findByBookId(bookId);
    }

    public List<Payment> findByStatus(String status) {
        return jpaRepository.findByStatus(status);
    }

    public List<Payment> findByUserIdAndStatus(Long userId, String status) {
        return jpaRepository.findByUserIdAndStatus(userId, status);
    }
}
```

---

### Config - RestTemplateConfig.java

```java
package com.relatosdepapel.payments.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced  // Habilita resolución de nombres Eureka
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

---

### Client - BookCatalogueClient.java

```java
package com.relatosdepapel.payments.client;

import com.relatosdepapel.payments.dto.BookAvailabilityDTO;
import com.relatosdepapel.payments.dto.StockUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class BookCatalogueClient {

    private final RestTemplate restTemplate;

    // ⚠️ CRÍTICO: Usar nombre de servicio Eureka, NO IP ni puerto
    private static final String CATALOGUE_SERVICE_URL = "http://MS-BOOKS-CATALOGUE";

    /**
     * Verifica disponibilidad de un libro
     * @return BookAvailabilityDTO o null si no existe
     */
    public BookAvailabilityDTO checkAvailability(Long bookId) {
        String url = CATALOGUE_SERVICE_URL + "/api/books/" + bookId + "/availability";

        try {
            return restTemplate.getForObject(url, BookAvailabilityDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null; // Libro no encontrado
        }
    }

    /**
     * Decrementa el stock de un libro
     */
    public void decrementStock(Long bookId, Integer quantity) {
        String url = CATALOGUE_SERVICE_URL + "/api/books/" + bookId + "/stock";
        StockUpdateDTO request = new StockUpdateDTO(-quantity);
        restTemplate.patchForObject(url, request, Void.class);
    }

    /**
     * Restaura el stock de un libro (cuando se cancela compra)
     */
    public void restoreStock(Long bookId, Integer quantity) {
        String url = CATALOGUE_SERVICE_URL + "/api/books/" + bookId + "/stock";
        StockUpdateDTO request = new StockUpdateDTO(quantity);
        restTemplate.patchForObject(url, request, Void.class);
    }
}
```

---

### Service - Capa 1: PaymentService.java (Interface)

```java
package com.relatosdepapel.payments.service;

import com.relatosdepapel.payments.dto.*;
import java.util.List;

public interface PaymentService {

    List<PaymentResponseDTO> getAll();

    PaymentResponseDTO getById(Long id);

    List<PaymentResponseDTO> search(Long userId, Long bookId, String status);

    UserPaymentsResponseDTO getByUserId(Long userId, String status);

    PaymentResponseDTO create(PaymentRequestDTO dto, BookAvailabilityDTO book);

    PaymentResponseDTO updateStatus(Long id, String status);

    Boolean cancel(Long id);

    Payment getEntityById(Long id);
}
```

---

### Service - Capa 2: PaymentServiceImpl.java (Implementación)

```java
package com.relatosdepapel.payments.service;

import com.relatosdepapel.payments.dto.*;
import com.relatosdepapel.payments.entity.Payment;
import com.relatosdepapel.payments.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;

    @Override
    public List<PaymentResponseDTO> getAll() {
        return repository.getAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponseDTO getById(Long id) {
        Payment payment = repository.getById(id);
        if (payment == null) {
            return null;
        }
        return toResponseDTO(payment);
    }

    @Override
    public Payment getEntityById(Long id) {
        return repository.getById(id);
    }

    @Override
    public List<PaymentResponseDTO> search(Long userId, Long bookId, String status) {
        List<Payment> payments;

        if (userId != null && status != null) {
            payments = repository.findByUserIdAndStatus(userId, status);
        } else if (userId != null) {
            payments = repository.findByUserId(userId);
        } else if (bookId != null) {
            payments = repository.findByBookId(bookId);
        } else if (status != null) {
            payments = repository.findByStatus(status);
        } else {
            payments = repository.getAll();
        }

        return payments.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserPaymentsResponseDTO getByUserId(Long userId, String status) {
        List<Payment> payments;

        if (status != null) {
            payments = repository.findByUserIdAndStatus(userId, status);
        } else {
            payments = repository.findByUserId(userId);
        }

        List<PaymentResponseDTO> paymentDTOs = payments.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        BigDecimal totalAmount = payments.stream()
                .filter(p -> "COMPLETED".equals(p.getStatus()))
                .map(Payment::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new UserPaymentsResponseDTO(
            userId,
            paymentDTOs,
            paymentDTOs.size(),
            totalAmount
        );
    }

    @Override
    public PaymentResponseDTO create(PaymentRequestDTO dto, BookAvailabilityDTO book) {
        Payment payment = Payment.builder()
            .userId(dto.getUserId())
            .bookId(dto.getBookId())
            .bookTitle(book.getTitle())
            .bookIsbn(book.getIsbn())
            .quantity(dto.getQuantity())
            .unitPrice(book.getPrice())
            .totalPrice(book.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity())))
            .purchaseDate(LocalDateTime.now())
            .status("COMPLETED")
            .build();

        Payment saved = repository.save(payment);
        return toResponseDTO(saved);
    }

    @Override
    public PaymentResponseDTO updateStatus(Long id, String status) {
        Payment payment = repository.getById(id);
        if (payment == null) {
            return null;
        }

        payment.setStatus(status);
        Payment saved = repository.save(payment);
        return toResponseDTO(saved);
    }

    @Override
    public Boolean cancel(Long id) {
        Payment payment = repository.getById(id);
        if (payment == null) {
            return false;
        }

        payment.setStatus("CANCELLED");
        repository.save(payment);
        return true;
    }

    // ========== MÉTODOS DE CONVERSIÓN ==========

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
            payment.getStatus()
        );
    }
}
```

---

### Controller - PaymentController.java (con ResponseEntity)

```java
package com.relatosdepapel.payments.controller;

import com.relatosdepapel.payments.client.BookCatalogueClient;
import com.relatosdepapel.payments.dto.*;
import com.relatosdepapel.payments.entity.Payment;
import com.relatosdepapel.payments.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;
    private final BookCatalogueClient catalogueClient;

    // GET /api/payments
    @GetMapping("/payments")
    public ResponseEntity<List<PaymentResponseDTO>> getAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) String status) {

        if (userId == null && bookId == null && status == null) {
            return ResponseEntity.ok(service.getAll()); // 200
        }

        return ResponseEntity.ok(service.search(userId, bookId, status)); // 200
    }

    // GET /api/payments/{id}
    @GetMapping("/payments/{id}")
    public ResponseEntity<PaymentResponseDTO> getById(@PathVariable Long id) {
        PaymentResponseDTO payment = service.getById(id);
        if (payment == null) {
            return ResponseEntity.notFound().build(); // 404
        }
        return ResponseEntity.ok(payment); // 200
    }

    // GET /api/users/{userId}/payments
    @GetMapping("/users/{userId}/payments")
    public ResponseEntity<UserPaymentsResponseDTO> getByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(service.getByUserId(userId, status)); // 200
    }

    // POST /api/payments
    @PostMapping("/payments")
    public ResponseEntity<?> create(@RequestBody PaymentRequestDTO dto) {
        // Validación: userId no null
        if (dto.getUserId() == null) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "El userId no puede ser nulo")); // 400
        }
        // Validación: bookId no null
        if (dto.getBookId() == null) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "El bookId no puede ser nulo")); // 400
        }
        // Validación: quantity >= 1
        if (dto.getQuantity() == null || dto.getQuantity() < 1) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "La cantidad debe ser al menos 1")); // 400
        }

        // Verificar libro en catalogue (usando nombre Eureka)
        BookAvailabilityDTO book = catalogueClient.checkAvailability(dto.getBookId());

        // Validación: libro existe
        if (book == null) {
            return ResponseEntity.status(404)
                .body(new ErrorResponseDTO(404, "Libro no encontrado")); // 404
        }

        // Validación: libro visible
        if (!book.getVisible()) {
            return ResponseEntity.status(409)
                .body(new ErrorResponseDTO(409, "El libro no está disponible para compra")); // 409
        }

        // Validación: stock suficiente
        if (book.getStock() < dto.getQuantity()) {
            return ResponseEntity.status(409)
                .body(new ErrorResponseDTO(409, "Stock insuficiente. Disponible: " + book.getStock())); // 409
        }

        // Decrementar stock en catalogue
        catalogueClient.decrementStock(dto.getBookId(), dto.getQuantity());

        // Crear el pago
        PaymentResponseDTO created = service.create(dto, book);
        return ResponseEntity.status(201).body(created); // 201
    }

    // PATCH /api/payments/{id}
    @PatchMapping("/payments/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody PaymentStatusDTO dto) {
        // Validación: status no vacío
        if (dto.getStatus() == null || dto.getStatus().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "El status no puede estar vacío")); // 400
        }

        // Verificar que existe
        Payment payment = service.getEntityById(id);
        if (payment == null) {
            return ResponseEntity.notFound().build(); // 404
        }

        // Si se cancela y no estaba cancelado, restaurar stock
        if ("CANCELLED".equals(dto.getStatus()) && !"CANCELLED".equals(payment.getStatus())) {
            catalogueClient.restoreStock(payment.getBookId(), payment.getQuantity());
        }

        PaymentResponseDTO updated = service.updateStatus(id, dto.getStatus());
        return ResponseEntity.ok(updated); // 200
    }

    // DELETE /api/payments/{id}
    @DeleteMapping("/payments/{id}")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        // Verificar que existe
        Payment payment = service.getEntityById(id);
        if (payment == null) {
            return ResponseEntity.notFound().build(); // 404
        }

        // Verificar que no está ya cancelado
        if ("CANCELLED".equals(payment.getStatus())) {
            return ResponseEntity.status(409)
                .body(new ErrorResponseDTO(409, "El pago ya está cancelado")); // 409
        }

        // Restaurar stock en catalogue
        catalogueClient.restoreStock(payment.getBookId(), payment.getQuantity());

        // Cancelar el pago
        service.cancel(id);
        return ResponseEntity.ok(true); // 200
    }
}
```

---

## ⚙️ application.yml

```yaml
server:
  port: 8082

spring:
  application:
    name: MS-BOOKS-PAYMENTS

  datasource:
    url: jdbc:h2:mem:payments_db
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

  h2:
    console:
      enabled: true
      path: /h2-console

eureka:
  instance:
    preferIpAddress: true
  client:
    registerWithEureka: true
    fetchRegistry: true
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

---

## 🧪 data.sql

```sql
INSERT INTO payments (user_id, book_id, book_title, book_isbn, quantity, unit_price, total_price, purchase_date, status) VALUES
(1, 1, 'El Quijote', '9788467033601', 2, 19.99, 39.98, '2024-01-10 10:30:00', 'COMPLETED'),
(1, 2, 'Cien años de soledad', '9788437604947', 1, 24.99, 24.99, '2024-01-12 14:20:00', 'COMPLETED'),
(2, 3, '1984', '9788423342150', 3, 15.99, 47.97, '2024-01-15 09:00:00', 'COMPLETED');
```

---

## 🎯 Checklist para Máxima Nota

- [ ] POST para registrar compras
- [ ] GET para obtener compra por ID
- [ ] GET para listar compras con filtros
- [ ] GET para compras de un usuario
- [ ] PATCH para actualizar estado
- [ ] DELETE para cancelar compra
- [ ] **Validación llamando a ms-books-catalogue**
- [ ] **Verificar existencia del libro**
- [ ] **Verificar visibilidad del libro**
- [ ] **Verificar stock disponible**
- [ ] **Comunicación usando nombre Eureka: `http://ms-books-catalogue/...`**
- [ ] Decrementar stock al comprar
- [ ] Restaurar stock al cancelar
- [ ] **2 capas Repository:** PaymentJpaRepository + PaymentRepository
- [ ] **2 capas Service:** PaymentService + PaymentServiceImpl
- [ ] **Manejo de errores con ResponseEntity** (sin GlobalExceptionHandler)
- [ ] Base de datos DIFERENTE a catalogue_db
- [ ] Registro automático en Eureka
