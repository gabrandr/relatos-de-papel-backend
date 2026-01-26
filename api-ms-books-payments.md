# API REST - Microservicio Operador (ms-books-payments)

> **Microservicio encargado de registrar y gestionar las compras de libros en "Relatos de Papel"**

---

## 📋 Información General

| Campo | Valor |
|-------|-------|
| **Nombre** | ms-books-payments |
| **Puerto** | 8082 |
| **Base URL** | `/api/v1/payments` |
| **Base de Datos** | payments_db (H2 o MySQL/PostgreSQL) - **DIFERENTE a catalogue_db** |
| **Nombre Eureka** | ms-books-payments |
| **Dependencia** | Consume API de ms-books-catalogue vía Eureka (sin IP ni puerto) |

---

## 🚀 Spring Initializr

**URL:** https://start.spring.io

| Campo | Valor |
|-------|-------|
| Project | Maven |
| Language | Java |
| Spring Boot | 3.2.x |
| Group | com.relatosdepapel |
| Artifact | ms-books-payments |
| Package name | com.relatosdepapel.payments |
| Java | 17 o 21 |

**Dependencias:**
- ✅ Spring Web
- ✅ Spring Data JPA
- ✅ H2 Database
- ✅ Eureka Discovery Client
- ✅ Spring Boot Actuator
- ✅ Lombok
- ✅ Validation

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
│   ├── PaymentJpaRepository.java     ← Interface JPA (extends JpaRepository)
│   └── PaymentRepository.java        ← Wrapper que usa PaymentJpaRepository
├── entity/
│   └── Payment.java
├── dto/
│   ├── PaymentRequestDTO.java
│   ├── PaymentResponseDTO.java
│   ├── PaymentStatusDTO.java
│   ├── BookAvailabilityDTO.java
│   ├── StockUpdateDTO.java
│   └── UserPaymentsResponseDTO.java
├── client/
│   └── BookCatalogueClient.java
├── config/
│   └── RestTemplateConfig.java
└── exception/
    ├── GlobalExceptionHandler.java
    ├── ResourceNotFoundException.java
    └── PaymentException.java
```

---

## 📦 Entidad: Payment

| Atributo | Tipo | Validaciones | Descripción |
|----------|------|--------------|-------------|
| `id` | Long | Auto-generado | Identificador único |
| `userId` | Long | NotNull, Positive | ID del usuario |
| `bookId` | Long | NotNull, Positive | ID del libro |
| `bookTitle` | String | - | Título (desnormalizado) |
| `bookIsbn` | String | - | ISBN (desnormalizado) |
| `quantity` | Integer | NotNull, Min 1 | Cantidad comprada |
| `unitPrice` | BigDecimal | NotNull, Positive | Precio unitario |
| `totalPrice` | BigDecimal | Calculado | quantity × unitPrice |
| `purchaseDate` | LocalDateTime | Auto-generado | Fecha de compra |
| `status` | String | NotNull | COMPLETED, CANCELLED |

---

## 🔗 Tabla de Endpoints

| Método HTTP | URI | Query Params | Request Body | Response Body | Códigos |
|-------------|-----|--------------|--------------|---------------|---------|
| POST | `/api/v1/payments` | N/A | PaymentRequestDTO | PaymentResponseDTO | 201, 400, 404, 409, 500 |
| GET | `/api/v1/payments/{id}` | N/A | N/A | PaymentResponseDTO | 200, 404, 500 |
| GET | `/api/v1/payments` | userId, bookId, status | N/A | List of PaymentResponseDTO | 200, 400, 500 |
| GET | `/api/v1/users/{userId}/payments` | status | N/A | UserPaymentsResponseDTO | 200, 404, 500 |
| PATCH | `/api/v1/payments/{id}` | N/A | PaymentStatusDTO | PaymentResponseDTO | 200, 400, 404, 500 |
| DELETE | `/api/v1/payments/{id}` | N/A | N/A | Boolean | 200, 404, 409, 500 |

---

## 📝 Detalle de Endpoints

### POST /api/v1/payments - Registrar compra

> ⚠️ **CRÍTICO**: Valida el libro llamando a ms-books-catalogue vía Eureka (sin IP ni puerto).

**Flujo de validación:**
```
1. Recibe: POST /api/v1/payments {userId, bookId, quantity}
2. Llama: GET http://ms-books-catalogue/api/v1/books/{bookId}/availability
3. Verifica: existe, visible=true, stock >= quantity
4. Llama: PATCH http://ms-books-catalogue/api/v1/books/{bookId}/stock {quantity: -N}
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

**Errores:**
- 400 Bad Request: quantity < 1
- 404 Not Found: Libro no existe
- 409 Conflict: Stock insuficiente o libro no visible

---

### GET /api/v1/payments/{id} - Obtener compra por ID

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

---

### GET /api/v1/payments - Listar compras con filtros

**Query Parameters:**

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `userId` | Long | Filtrar por usuario |
| `bookId` | Long | Filtrar por libro |
| `status` | String | Filtrar por estado |

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

### GET /api/v1/users/{userId}/payments - Compras de un usuario

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

### PATCH /api/v1/payments/{id} - Actualizar estado

**Request Body (PaymentStatusDTO):**
```json
{
  "status": "CANCELLED"
}
```

**Lógica:** Si se cancela, restaurar stock en ms-books-catalogue.

---

### DELETE /api/v1/payments/{id} - Cancelar compra

**Lógica:** Cambia estado a CANCELLED y restaura stock.

**Response 200 OK:**
```json
true
```

---

## 🛠️ Implementación - Capa Repository (2 capas)

### PaymentJpaRepository.java (Interface JPA)

```java
package com.relatosdepapel.payments.repository;

import com.relatosdepapel.payments.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Interface JPA con métodos de consulta personalizados
 */
public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByUserId(Long userId);
    
    List<Payment> findByBookId(Long bookId);
    
    List<Payment> findByStatus(String status);
    
    List<Payment> findByUserIdAndStatus(Long userId, String status);
}
```

### PaymentRepository.java (Wrapper/Abstracción)

```java
package com.relatosdepapel.payments.repository;

import com.relatosdepapel.payments.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Clase wrapper que abstrae el acceso a JpaRepository
 */
@Repository
@RequiredArgsConstructor
public class PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    public List<Payment> getAll() {
        return jpaRepository.findAll();
    }

    public Optional<Payment> getById(Long id) {
        return jpaRepository.findById(id);
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

## 🛠️ Implementación - Capa Service (2 capas)

### PaymentService.java (Interface)

```java
package com.relatosdepapel.payments.service;

import com.relatosdepapel.payments.dto.*;
import java.util.List;

/**
 * Interface que define el contrato del servicio de pagos
 */
public interface PaymentService {
    
    // GET /payments
    List<PaymentResponseDTO> getAll();
    
    // GET /payments/{id}
    PaymentResponseDTO getById(Long id);
    
    // GET /payments con filtros
    List<PaymentResponseDTO> search(Long userId, Long bookId, String status);
    
    // GET /users/{userId}/payments
    UserPaymentsResponseDTO getByUserId(Long userId, String status);
    
    // POST /payments
    PaymentResponseDTO create(PaymentRequestDTO dto);
    
    // PATCH /payments/{id}
    PaymentResponseDTO updateStatus(Long id, PaymentStatusDTO dto);
    
    // DELETE /payments/{id}
    Boolean cancel(Long id);
}
```

### PaymentServiceImpl.java (Implementación)

```java
package com.relatosdepapel.payments.service;

import com.relatosdepapel.payments.client.BookCatalogueClient;
import com.relatosdepapel.payments.dto.*;
import com.relatosdepapel.payments.entity.Payment;
import com.relatosdepapel.payments.exception.PaymentException;
import com.relatosdepapel.payments.exception.ResourceNotFoundException;
import com.relatosdepapel.payments.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final BookCatalogueClient catalogueClient;

    @Override
    public List<PaymentResponseDTO> getAll() {
        return repository.getAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponseDTO getById(Long id) {
        Payment payment = repository.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
        return toResponseDTO(payment);
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
    public PaymentResponseDTO create(PaymentRequestDTO dto) {
        // 1. Validar libro en catalogue (usando nombre Eureka, NO IP)
        BookAvailabilityDTO book = catalogueClient.checkAvailability(dto.getBookId());
        
        // 2. Verificar visibilidad
        if (!book.getVisible()) {
            throw new PaymentException("Book is not available for purchase");
        }
        
        // 3. Verificar stock
        if (book.getStock() < dto.getQuantity()) {
            throw new PaymentException("Insufficient stock. Available: " + book.getStock());
        }
        
        // 4. Decrementar stock en catalogue
        catalogueClient.decrementStock(dto.getBookId(), dto.getQuantity());
        
        // 5. Crear registro de pago
        Payment payment = new Payment();
        payment.setUserId(dto.getUserId());
        payment.setBookId(dto.getBookId());
        payment.setBookTitle(book.getTitle());
        payment.setBookIsbn(book.getIsbn());
        payment.setQuantity(dto.getQuantity());
        payment.setUnitPrice(book.getPrice());
        payment.setTotalPrice(book.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
        payment.setPurchaseDate(LocalDateTime.now());
        payment.setStatus("COMPLETED");
        
        Payment saved = repository.save(payment);
        return toResponseDTO(saved);
    }

    @Override
    public PaymentResponseDTO updateStatus(Long id, PaymentStatusDTO dto) {
        Payment payment = repository.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
        
        // Si se cancela, restaurar stock
        if ("CANCELLED".equals(dto.getStatus()) && !"CANCELLED".equals(payment.getStatus())) {
            catalogueClient.restoreStock(payment.getBookId(), payment.getQuantity());
        }
        
        payment.setStatus(dto.getStatus());
        Payment saved = repository.save(payment);
        return toResponseDTO(saved);
    }

    @Override
    public Boolean cancel(Long id) {
        return repository.getById(id)
                .map(payment -> {
                    if ("CANCELLED".equals(payment.getStatus())) {
                        throw new PaymentException("Payment already cancelled");
                    }
                    
                    // Restaurar stock
                    catalogueClient.restoreStock(payment.getBookId(), payment.getQuantity());
                    
                    payment.setStatus("CANCELLED");
                    repository.save(payment);
                    return true;
                })
                .orElse(false);
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

## 🔄 Cliente para ms-books-catalogue

### RestTemplateConfig.java

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

### BookCatalogueClient.java

```java
package com.relatosdepapel.payments.client;

import com.relatosdepapel.payments.dto.BookAvailabilityDTO;
import com.relatosdepapel.payments.dto.StockUpdateDTO;
import com.relatosdepapel.payments.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class BookCatalogueClient {

    private final RestTemplate restTemplate;
    
    // ⚠️ CRÍTICO: Usar nombre de servicio Eureka, NO IP ni puerto
    private static final String CATALOGUE_SERVICE_URL = "http://ms-books-catalogue";

    public BookAvailabilityDTO checkAvailability(Long bookId) {
        String url = CATALOGUE_SERVICE_URL + "/api/v1/books/" + bookId + "/availability";
        
        try {
            return restTemplate.getForObject(url, BookAvailabilityDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Book not found: " + bookId);
        }
    }

    public void decrementStock(Long bookId, Integer quantity) {
        String url = CATALOGUE_SERVICE_URL + "/api/v1/books/" + bookId + "/stock";
        StockUpdateDTO request = new StockUpdateDTO(-quantity);
        restTemplate.patchForObject(url, request, Void.class);
    }

    public void restoreStock(Long bookId, Integer quantity) {
        String url = CATALOGUE_SERVICE_URL + "/api/v1/books/" + bookId + "/stock";
        StockUpdateDTO request = new StockUpdateDTO(quantity);
        restTemplate.patchForObject(url, request, Void.class);
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
    name: ms-books-payments
  
  datasource:
    url: jdbc:h2:mem:payments_db  # BD DIFERENTE a catalogue
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
INSERT INTO payment (user_id, book_id, book_title, book_isbn, quantity, unit_price, total_price, purchase_date, status) VALUES
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
- [ ] Base de datos DIFERENTE a catalogue_db
- [ ] Registro automático en Eureka
