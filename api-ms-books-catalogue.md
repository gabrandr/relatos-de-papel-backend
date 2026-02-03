# API REST - Microservicio Buscador (ms-books-catalogue)

> **Microservicio encargado de gestionar el catálogo de libros de "Relatos de Papel"**

---

## 📋 Información General

| Campo             | Valor                                |
| ----------------- | ------------------------------------ |
| **Nombre**        | ms-books-catalogue                   |
| **Puerto**        | 8081                                 |
| **Base URL**      | `/api/books`                         |
| **Base de Datos** | catalogue_db (H2 o MySQL/PostgreSQL) |
| **Nombre Eureka** | ms-books-catalogue                   |

---

## 🚀 Spring Initializr

**URL:** https://start.spring.io

| Campo        | Valor                        |
| ------------ | ---------------------------- |
| Project      | Maven                        |
| Language     | Java                         |
| Spring Boot  | 3.2.x                        |
| Group        | com.relatosdepapel           |
| Artifact     | ms-books-catalogue           |
| Package name | com.relatosdepapel.catalogue |
| Java         | 17 o 21                      |

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
src/main/java/com/relatosdepapel/catalogue/
├── MsBooksCatalogueApplication.java
├── controller/
│   └── BookController.java
├── service/
│   ├── BookService.java              ← Interface
│   └── BookServiceImpl.java          ← Implementación
├── repository/
│   ├── BookJpaRepository.java        ← Interface JPA
│   └── BookRepository.java           ← Wrapper
├── entity/
│   └── Book.java
├── dto/
│   ├── BookRequestDTO.java
│   ├── BookResponseDTO.java
│   ├── BookPatchDTO.java
│   ├── AvailabilityResponseDTO.java
│   ├── StockUpdateDTO.java
│   └── ErrorResponseDTO.java
└── utils/
    └── Consts.java                   ← Constantes (nombres de columnas)
```

> ⚠️ **Nota:** No se usa `GlobalExceptionHandler`. El manejo de errores se hace con `ResponseEntity` en el Controller.

---

## 📦 Entidad: Book

| Atributo          | Tipo       | Descripción                         |
| ----------------- | ---------- | ----------------------------------- |
| `id`              | Long       | Identificador único (auto-generado) |
| `title`           | String     | Título del libro                    |
| `author`          | String     | Autor del libro                     |
| `publicationDate` | LocalDate  | Fecha de publicación                |
| `category`        | String     | Categoría/Género                    |
| `isbn`            | String     | Código ISBN (único)                 |
| `rating`          | Integer    | Valoración (1-5)                    |
| `visible`         | Boolean    | Visibilidad en frontend             |
| `stock`           | Integer    | Cantidad disponible                 |
| `price`           | BigDecimal | Precio del libro                    |

---

## 🔗 Tabla de Endpoints

| Método HTTP | URI                            | Query Params                                                                                                                       | Request Body   | Response Body           | Códigos       |
| ----------- | ------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------- | -------------- | ----------------------- | ------------- |
| POST        | `/api/books`                   | N/A                                                                                                                                | BookRequestDTO | BookResponseDTO         | 201, 400      |
| GET         | `/api/books`                   | N/A                                                                                                                                | N/A            | List<BookResponseDTO>   | 200           |
| GET         | `/api/books/search`            | title, author, category, isbn, ratingMin, ratingMax, visible, minPrice, maxPrice, minStock, publicationDateFrom, publicationDateTo | N/A            | List<BookResponseDTO>   | 200           |
| GET         | `/api/books/{id}`              | N/A                                                                                                                                | N/A            | BookResponseDTO         | 200, 404      |
| PUT         | `/api/books/{id}`              | N/A                                                                                                                                | BookRequestDTO | BookResponseDTO         | 200, 400, 404 |
| PATCH       | `/api/books/{id}`              | N/A                                                                                                                                | BookPatchDTO   | BookResponseDTO         | 200, 400, 404 |
| DELETE      | `/api/books/{id}`              | N/A                                                                                                                                | N/A            | Void                    | 204, 404      |
| GET         | `/api/books/{id}/availability` | N/A                                                                                                                                | N/A            | AvailabilityResponseDTO | 200, 404      |
| PATCH       | `/api/books/{id}/stock`        | N/A                                                                                                                                | StockUpdateDTO | BookResponseDTO         | 200, 400, 404 |

---

## 📝 Detalle de Endpoints

### POST /api/books - Crear libro

**Request Body (BookRequestDTO):**

```json
{
  "title": "El Quijote",
  "author": "Miguel de Cervantes",
  "publicationDate": "1605-01-16",
  "category": "Clásicos",
  "isbn": "9788467033601",
  "rating": 5,
  "visible": true,
  "stock": 100,
  "price": 19.99
}
```

**Response 201 Created (BookResponseDTO):**

```json
{
  "id": 1,
  "title": "El Quijote",
  "author": "Miguel de Cervantes",
  "publicationDate": "1605-01-16",
  "category": "Clásicos",
  "isbn": "9788467033601",
  "rating": 5,
  "visible": true,
  "stock": 100,
  "price": 19.99
}
```

**Response 400 Bad Request (ErrorResponseDTO):**

```json
{
  "code": 400,
  "message": "El título no puede estar vacío"
}
```

---

### GET /api/books/{id} - Obtener libro por ID

**Response 200 OK (BookResponseDTO):**

```json
{
  "id": 1,
  "title": "El Quijote",
  "author": "Miguel de Cervantes",
  "publicationDate": "1605-01-16",
  "category": "Clásicos",
  "isbn": "9788467033601",
  "rating": 5,
  "visible": true,
  "stock": 100,
  "price": 19.99
}
```

**Response 404 Not Found:** (sin body)

---

### GET /api/books - Obtener todos los libros

**Sin query parameters** (retorna todos los libros del catálogo)

**Response 200 OK:**

```json
[
  {
    "id": 1,
    "title": "Don Quijote de la Mancha",
    "author": "Miguel de Cervantes",
    "publicationDate": "1605-01-16",
    "category": "Clásicos",
    "isbn": "9788467033601",
    "rating": 5,
    "visible": true,
    "stock": 10,
    "price": 19.99
  },
  {
    "id": 2,
    "title": "Cien años de soledad",
    "author": "Gabriel García Márquez",
    "publicationDate": "1967-05-30",
    "category": "Realismo mágico",
    "isbn": "9780307474728",
    "rating": 5,
    "visible": true,
    "stock": 15,
    "price": 24.99
  }
]
```

---

### GET /api/books/search - Buscar libros con filtros

> ⚠️ **CRÍTICO**: Búsqueda por TODOS los atributos de forma INDIVIDUAL y COMBINADA.

**Query Parameters:**

| Parámetro             | Tipo       | Descripción             |
| --------------------- | ---------- | ----------------------- |
| `title`               | String     | Búsqueda parcial (LIKE) |
| `author`              | String     | Búsqueda parcial (LIKE) |
| `category`            | String     | Categoría exacta        |
| `isbn`                | String     | ISBN exacto             |
| `ratingMin`           | Integer    | Valoración mínima       |
| `ratingMax`           | Integer    | Valoración máxima       |
| `visible`             | Boolean    | Filtrar visibilidad     |
| `minPrice`            | BigDecimal | Precio mínimo           |
| `maxPrice`            | BigDecimal | Precio máximo           |
| `minStock`            | Integer    | Stock mínimo            |
| `publicationDateFrom` | LocalDate  | Fecha desde             |
| `publicationDateTo`   | LocalDate  | Fecha hasta             |

**Ejemplos de búsqueda individual:**

```
GET /api/books/search?author=cervantes
GET /api/books/search?category=Clásicos
GET /api/books/search?ratingMin=4
GET /api/books/search?visible=true
```

**Ejemplos de búsqueda combinada:**

```
GET /api/books/search?author=cervantes&category=Clásicos&ratingMin=4&visible=true
GET /api/books/search?minPrice=10&maxPrice=30&minStock=5
```

**Response 200 OK:**

```json
[
  {
    "id": 1,
    "title": "El Quijote",
    "author": "Miguel de Cervantes",
    "category": "Clásicos",
    "isbn": "9788467033601",
    "rating": 5,
    "visible": true,
    "stock": 100,
    "price": 19.99
  }
]
```

---

### PUT /api/books/{id} - Actualizar libro completo

**Request Body:** Igual que POST (todos los campos)

**Response 200 OK:** BookResponseDTO actualizado

**Response 404 Not Found:** (sin body)

---

### PATCH /api/books/{id} - Actualizar libro parcial

**Request Body (BookPatchDTO):** Solo campos a modificar

```json
{
  "price": 24.99,
  "stock": 150
}
```

**Response 200 OK:** BookResponseDTO actualizado

---

### DELETE /api/books/{id} - Eliminar libro

**Response 204 No Content:** (sin body)

**Si no existiera:** `404 Not Found` (sin body)

---

### GET /api/books/{id}/availability - Verificar disponibilidad

> 🔗 **Usado por ms-books-payments** para validar antes de comprar.

**Response 200 OK (AvailabilityResponseDTO):**

```json
{
  "id": 1,
  "title": "El Quijote",
  "isbn": "9788467033601",
  "available": true,
  "visible": true,
  "stock": 100,
  "price": 19.99
}
```

**Response 404 Not Found:** (sin body)

---

### PATCH /api/books/{id}/stock - Actualizar stock

> 🔗 **Usado por ms-books-payments** para decrementar/incrementar stock.

**Request Body (StockUpdateDTO):**

```json
{
  "quantity": -2
}
```

**Response 200 OK:** BookResponseDTO con stock actualizado

**Response 400 Bad Request:**

```json
{
  "code": 400,
  "message": "Stock insuficiente"
}
```

---

## 🛠️ Implementación

### Utils - Consts.java

> **Buena práctica:** Usar constantes para nombres de columnas evita errores de tipeo y facilita el mantenimiento.

```java
package com.relatosdepapel.catalogue.utils;

public class Consts {
    // Campos de la entidad Book
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String AUTHOR = "author";
    public static final String PUBLICATION_DATE = "publicationDate";
    public static final String CATEGORY = "category";
    public static final String ISBN = "isbn";
    public static final String RATING = "rating";
    public static final String VISIBLE = "visible";
    public static final String STOCK = "stock";
    public static final String PRICE = "price";
}
```

---

### Entity - Book.java

```java
package com.relatosdepapel.catalogue.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.relatosdepapel.catalogue.utils.Consts;
import com.relatosdepapel.catalogue.dto.BookRequestDTO;

/**
 * Entidad que representa un libro
 */
@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = Consts.TITLE, nullable = false)
    private String title;

    @Column(name = Consts.AUTHOR, nullable = false)
    private String author;

    @Column(name = Consts.PUBLICATION_DATE)
    private LocalDate publicationDate;

    @Column(name = Consts.CATEGORY)
    private String category;

    @Column(name = Consts.ISBN, unique = true, nullable = false)
    private String isbn;

    @Column(name = Consts.RATING)
    private Integer rating;

    @Column(name = Consts.VISIBLE, nullable = false)
    private Boolean visible;

    @Column(name = Consts.STOCK, nullable = false)
    private Integer stock;

    @Column(name = Consts.PRICE, nullable = false)
    private BigDecimal price;

    /**
     * Método para actualizar Book desde un BookRequestDTO.
     * Nota: El ISBN NO se actualiza (es inmutable).
     */
    public void updateFromDTO(BookRequestDTO dto) {
        this.title = dto.getTitle();
        this.author = dto.getAuthor();
        this.publicationDate = dto.getPublicationDate();
        this.category = dto.getCategory();
        this.rating = dto.getRating();
        this.visible = dto.getVisible();
        this.stock = dto.getStock();
        this.price = dto.getPrice();
    }
}
```

---

### DTOs

#### BookRequestDTO.java

```java
package com.relatosdepapel.catalogue.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookRequestDTO {
    private String title;
    private String author;
    private LocalDate publicationDate;
    private String category;
    private String isbn;
    private Integer rating;
    private Boolean visible;
    private Integer stock;
    private BigDecimal price;
}
```

#### BookResponseDTO.java

```java
package com.relatosdepapel.catalogue.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookResponseDTO {
    private Long id;
    private String title;
    private String author;
    private LocalDate publicationDate;
    private String category;
    private String isbn;
    private Integer rating;
    private Boolean visible;
    private Integer stock;
    private BigDecimal price;
}
```

#### BookPatchDTO.java

```java
package com.relatosdepapel.catalogue.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookPatchDTO {
    private String title;
    private String author;
    private LocalDate publicationDate;
    private String category;
    private Integer rating;
    private Boolean visible;
    private Integer stock;
    private BigDecimal price;
}
```

#### AvailabilityResponseDTO.java

```java
package com.relatosdepapel.catalogue.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponseDTO {
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
package com.relatosdepapel.catalogue.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateDTO {
    private Integer quantity;
}
```

#### ErrorResponseDTO.java

```java
package com.relatosdepapel.catalogue.dto;

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

### Repository - Capa 1: BookJpaRepository.java

```java
package com.relatosdepapel.catalogue.repository;

import com.relatosdepapel.catalogue.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface BookJpaRepository extends JpaRepository<Book, Long>,
                                           JpaSpecificationExecutor<Book> {

    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorContainingIgnoreCase(String author);
    List<Book> findByCategory(String category);
    List<Book> findByIsbn(String isbn);
    List<Book> findByVisibleTrue();
    boolean existsByIsbn(String isbn);
}
```

---

### Repository - Capa 2: BookRepository.java (Wrapper)

```java
package com.relatosdepapel.catalogue.repository;

import com.relatosdepapel.catalogue.entity.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookRepository {

    private final BookJpaRepository jpaRepository;

    public List<Book> getAll() {
        return jpaRepository.findAll();
    }

    public Book getById(Long id) {
        return jpaRepository.findById(id).orElse(null);
    }

    public Book save(Book book) {
        return jpaRepository.save(book);
    }

    public void delete(Book book) {
        jpaRepository.delete(book);
    }

    public boolean existsByIsbn(String isbn) {
        return jpaRepository.existsByIsbn(isbn);
    }

    public List<Book> findByTitle(String title) {
        return jpaRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Book> findByAuthor(String author) {
        return jpaRepository.findByAuthorContainingIgnoreCase(author);
    }

    public List<Book> findByCategory(String category) {
        return jpaRepository.findByCategory(category);
    }

    public List<Book> search(Specification<Book> spec) {
        return jpaRepository.findAll(spec);
    }
}
```

---

### Service - Capa 1: BookService.java (Interface)

```java
package com.relatosdepapel.catalogue.service;

import com.relatosdepapel.catalogue.dto.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BookService {

    List<BookResponseDTO> getAll();

    BookResponseDTO getById(Long id);

    List<BookResponseDTO> search(
        String title, String author, String category, String isbn,
        Integer ratingMin, Integer ratingMax, Boolean visible,
        BigDecimal minPrice, BigDecimal maxPrice, Integer minStock,
        LocalDate publicationDateFrom, LocalDate publicationDateTo
    );

    BookResponseDTO create(BookRequestDTO dto);

    BookResponseDTO update(Long id, BookRequestDTO dto);

    BookResponseDTO patch(Long id, BookPatchDTO dto);

    boolean delete(Long id);

    AvailabilityResponseDTO checkAvailability(Long id);

    BookResponseDTO updateStock(Long id, StockUpdateDTO dto);
}
```

---

### Service - Capa 2: BookServiceImpl.java (Implementación)

```java
package com.relatosdepapel.catalogue.service;

import com.relatosdepapel.catalogue.dto.*;
import com.relatosdepapel.catalogue.entity.Book;
import com.relatosdepapel.catalogue.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository repository;

    @Override
    public List<BookResponseDTO> getAll() {
        return repository.getAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookResponseDTO getById(Long id) {
        Book book = repository.getById(id);
        if (book == null) {
            return null;
        }
        return toResponseDTO(book);
    }

    @Override
    public List<BookResponseDTO> search(
            String title, String author, String category, String isbn,
            Integer ratingMin, Integer ratingMax, Boolean visible,
            BigDecimal minPrice, BigDecimal maxPrice, Integer minStock,
            LocalDate publicationDateFrom, LocalDate publicationDateTo) {

        Specification<Book> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (title != null && !title.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")),
                    "%" + title.toLowerCase() + "%"));
            }
            if (author != null && !author.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("author")),
                    "%" + author.toLowerCase() + "%"));
            }
            if (category != null && !category.isEmpty()) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (isbn != null && !isbn.isEmpty()) {
                predicates.add(cb.equal(root.get("isbn"), isbn));
            }
            if (ratingMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), ratingMin));
            }
            if (ratingMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rating"), ratingMax));
            }
            if (visible != null) {
                predicates.add(cb.equal(root.get("visible"), visible));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (minStock != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("stock"), minStock));
            }
            if (publicationDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("publicationDate"), publicationDateFrom));
            }
            if (publicationDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("publicationDate"), publicationDateTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repository.search(spec).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookResponseDTO create(BookRequestDTO dto) {
        Book book = toEntity(dto);
        Book saved = repository.save(book);
        return toResponseDTO(saved);
    }

    @Override
    public BookResponseDTO update(Long id, BookRequestDTO dto) {
        Book book = repository.getById(id);
        if (book == null) {
            return null;
        }

        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setPublicationDate(dto.getPublicationDate());
        book.setCategory(dto.getCategory());
        book.setIsbn(dto.getIsbn());
        book.setRating(dto.getRating());
        book.setVisible(dto.getVisible());
        book.setStock(dto.getStock());
        book.setPrice(dto.getPrice());

        Book saved = repository.save(book);
        return toResponseDTO(saved);
    }

    @Override
    public BookResponseDTO patch(Long id, BookPatchDTO dto) {
        Book book = repository.getById(id);
        if (book == null) {
            return null;
        }

        if (dto.getTitle() != null) book.setTitle(dto.getTitle());
        if (dto.getAuthor() != null) book.setAuthor(dto.getAuthor());
        if (dto.getPublicationDate() != null) book.setPublicationDate(dto.getPublicationDate());
        if (dto.getCategory() != null) book.setCategory(dto.getCategory());
        if (dto.getRating() != null) book.setRating(dto.getRating());
        if (dto.getVisible() != null) book.setVisible(dto.getVisible());
        if (dto.getStock() != null) book.setStock(dto.getStock());
        if (dto.getPrice() != null) book.setPrice(dto.getPrice());

        Book saved = repository.save(book);
        return toResponseDTO(saved);
    }

    @Override
    public boolean delete(Long id) {
        Book book = repository.getById(id);
        if (book == null) {
            return false;
        }
        repository.delete(book);
        return true;
    }

    @Override
    public AvailabilityResponseDTO checkAvailability(Long id) {
        Book book = repository.getById(id);
        if (book == null) {
            return null;
        }

        return new AvailabilityResponseDTO(
            book.getId(),
            book.getTitle(),
            book.getIsbn(),
            book.getVisible() && book.getStock() > 0,
            book.getVisible(),
            book.getStock(),
            book.getPrice()
        );
    }

    @Override
    public BookResponseDTO updateStock(Long id, StockUpdateDTO dto) {
        Book book = repository.getById(id);
        if (book == null) {
            return null;
        }

        int newStock = book.getStock() + dto.getQuantity();
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }

        book.setStock(newStock);
        Book saved = repository.save(book);
        return toResponseDTO(saved);
    }

    // ========== MÉTODOS DE CONVERSIÓN ==========

    private BookResponseDTO toResponseDTO(Book book) {
        return new BookResponseDTO(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getPublicationDate(),
            book.getCategory(),
            book.getIsbn(),
            book.getRating(),
            book.getVisible(),
            book.getStock(),
            book.getPrice()
        );
    }

    private Book toEntity(BookRequestDTO dto) {
        return Book.builder()
            .title(dto.getTitle())
            .author(dto.getAuthor())
            .publicationDate(dto.getPublicationDate())
            .category(dto.getCategory())
            .isbn(dto.getIsbn())
            .rating(dto.getRating())
            .visible(dto.getVisible())
            .stock(dto.getStock())
            .price(dto.getPrice())
            .build();
    }
}
```

---

### Controller - BookController.java (con ResponseEntity)

```java
package com.relatosdepapel.catalogue.controller;

import com.relatosdepapel.catalogue.dto.*;
import com.relatosdepapel.catalogue.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService service;

    // GET /api/books - Obtener todos los libros
    @GetMapping
    public ResponseEntity<List<BookResponseDTO>> getAllBooks() {
        List<BookResponseDTO> books = service.getAll();
        return ResponseEntity.ok(books); // 200
    }

    // GET /api/books/search - Búsqueda con filtros
    @GetMapping("/search")
    public ResponseEntity<List<BookResponseDTO>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Integer ratingMin,
            @RequestParam(required = false) Integer ratingMax,
            @RequestParam(required = false) Boolean visible,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publicationDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publicationDateTo) {

        List<BookResponseDTO> result = service.search(
            title, author, category, isbn,
            ratingMin, ratingMax, visible,
            minPrice, maxPrice, minStock,
            publicationDateFrom, publicationDateTo
        );
        return ResponseEntity.ok(result); // 200
    }

    // GET /api/books/{id}
    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getById(@PathVariable Long id) {
        BookResponseDTO book = service.getById(id);
        if (book == null) {
            return ResponseEntity.notFound().build(); // 404
        }
        return ResponseEntity.ok(book); // 200
    }

    // POST /api/books
    @PostMapping
    public ResponseEntity<?> create(@RequestBody BookRequestDTO dto) {
        // Validación: título no vacío
        if (dto.getTitle() == null || dto.getTitle().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "El título no puede estar vacío")); // 400
        }
        // Validación: autor no vacío
        if (dto.getAuthor() == null || dto.getAuthor().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "El autor no puede estar vacío")); // 400
        }
        // Validación: ISBN no vacío
        if (dto.getIsbn() == null || dto.getIsbn().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "El ISBN no puede estar vacío")); // 400
        }
        // Validación: precio positivo
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "El precio debe ser mayor a 0")); // 400
        }
        // Validación: stock no negativo
        if (dto.getStock() == null || dto.getStock() < 0) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "El stock no puede ser negativo")); // 400
        }

        return ResponseEntity.status(201).body(service.create(dto)); // 201
    }

    // PUT /api/books/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody BookRequestDTO dto) {
        // Validación 1: título no vacío
        if (dto.getTitle() == null || dto.getTitle().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "El título no puede estar vacío")); // 400
        }
        // Validación 2: autor no vacío
        if (dto.getAuthor() == null || dto.getAuthor().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "El autor no puede estar vacío")); // 400
        }
        // Validación 3: ISBN no vacío
        if (dto.getIsbn() == null || dto.getIsbn().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "El ISBN no puede estar vacío")); // 400
        }
        // Validación 4: precio positivo
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "El precio debe ser mayor a 0")); // 400
        }
        // Validación 5: stock no negativo
        if (dto.getStock() == null || dto.getStock() < 0) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "El stock no puede ser negativo")); // 400
        }

        BookResponseDTO updated = service.update(id, dto);
        if (updated == null) {
            return ResponseEntity.notFound().build(); // 404
        }
        return ResponseEntity.ok(updated); // 200
    }

    // PATCH /api/books/{id}
    @PatchMapping("/{id}")
    public ResponseEntity<?> patchBook(@PathVariable Long id, @RequestBody BookPatchDTO dto) {
        BookResponseDTO updated = service.patch(id, dto);
        if (updated == null) {
            return ResponseEntity.notFound().build(); // 404
        }
        return ResponseEntity.ok(updated); // 200
    }

    // DELETE /api/books/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        boolean deleted = service.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build(); // 404
        }
        return ResponseEntity.noContent().build(); // 204
    }

    // GET /api/books/{id}/availability
    @GetMapping("/{id}/availability")
    public ResponseEntity<AvailabilityResponseDTO> checkAvailability(@PathVariable Long id) {
        AvailabilityResponseDTO availability = service.checkAvailability(id);
        if (availability == null) {
            return ResponseEntity.notFound().build(); // 404
        }
        return ResponseEntity.ok(availability); // 200
    }

    // PATCH /api/books/{id}/stock
    @PatchMapping("/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable Long id, @RequestBody StockUpdateDTO dto) {
        // Validación: quantity no null
        if (dto.getQuantity() == null) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "La cantidad no puede ser nula")); // 400
        }

        // Verificar que el libro existe
        BookResponseDTO book = service.getById(id);
        if (book == null) {
            return ResponseEntity.notFound().build(); // 404
        }

        // Verificar stock suficiente si es decremento
        if (dto.getQuantity() < 0 && book.getStock() + dto.getQuantity() < 0) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "Stock insuficiente")); // 400
        }

        BookResponseDTO updated = service.updateStock(id, dto.getQuantity());
        return ResponseEntity.ok(updated); // 200
    }
}
```

---

## ⚙️ application.yml

```yaml
server:
  port: 8081

spring:
  application:
    name: MS-BOOKS-CATALOGUE

  datasource:
    url: jdbc:h2:mem:catalogue_db
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
INSERT INTO books (title, author, publication_date, category, isbn, rating, visible, stock, price) VALUES
('El Quijote', 'Miguel de Cervantes', '1605-01-16', 'Clásicos', '9788467033601', 5, true, 100, 19.99),
('Cien años de soledad', 'Gabriel García Márquez', '1967-05-30', 'Realismo Mágico', '9788437604947', 5, true, 50, 24.99),
('1984', 'George Orwell', '1949-06-08', 'Distopía', '9788423342150', 4, true, 75, 15.99),
('El Señor de los Anillos', 'J.R.R. Tolkien', '1954-07-29', 'Fantasía', '9788445071779', 5, true, 30, 29.99),
('Libro Oculto', 'Autor Desconocido', '2020-01-01', 'Misterio', '9788412345678', 3, false, 10, 9.99);
```

---

## 🎯 Checklist para Máxima Nota

- [ ] CRUD completo (POST, GET, PUT, PATCH, DELETE)
- [ ] Búsqueda por título (parcial, case-insensitive)
- [ ] Búsqueda por autor (parcial, case-insensitive)
- [ ] Búsqueda por fecha de publicación (rango)
- [ ] Búsqueda por categoría
- [ ] Búsqueda por ISBN
- [ ] Búsqueda por valoración (rango)
- [ ] Búsqueda por visibilidad
- [ ] Búsqueda por precio (rango)
- [ ] Búsqueda por stock (mínimo)
- [ ] **Búsqueda COMBINADA de múltiples filtros**
- [ ] Endpoint `/availability` para ms-payments
- [ ] Endpoint `/stock` para ms-payments
- [ ] **2 capas Repository:** BookJpaRepository + BookRepository
- [ ] **2 capas Service:** BookService + BookServiceImpl
- [ ] **Manejo de errores con ResponseEntity** (sin GlobalExceptionHandler)
- [ ] Registro automático en Eureka
