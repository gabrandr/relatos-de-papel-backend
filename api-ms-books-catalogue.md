# API REST - Microservicio Buscador (ms-books-catalogue)

> **Microservicio encargado de gestionar el catálogo de libros de "Relatos de Papel"**

---

## 📋 Información General

| Campo | Valor |
|-------|-------|
| **Nombre** | ms-books-catalogue |
| **Puerto** | 8081 |
| **Base URL** | `/api/v1/books` |
| **Base de Datos** | catalogue_db (H2 o MySQL/PostgreSQL) |
| **Nombre Eureka** | ms-books-catalogue |

---

## 🚀 Spring Initializr

**URL:** https://start.spring.io

| Campo | Valor |
|-------|-------|
| Project | Maven |
| Language | Java |
| Spring Boot | 3.2.x |
| Group | com.relatosdepapel |
| Artifact | ms-books-catalogue |
| Package name | com.relatosdepapel.catalogue |
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
src/main/java/com/relatosdepapel/catalogue/
├── MsBooksCatalogueApplication.java
├── controller/
│   └── BookController.java
├── service/
│   ├── BookService.java              ← Interface
│   └── BookServiceImpl.java          ← Implementación
├── repository/
│   ├── BookJpaRepository.java        ← Interface JPA (extends JpaRepository)
│   └── BookRepository.java           ← Wrapper que usa BookJpaRepository
├── entity/
│   └── Book.java
├── dto/
│   ├── BookRequestDTO.java
│   ├── BookResponseDTO.java
│   ├── BookPatchDTO.java
│   ├── AvailabilityResponseDTO.java
│   └── StockUpdateDTO.java
└── exception/
    ├── GlobalExceptionHandler.java
    └── ResourceNotFoundException.java
```

---

## 📦 Entidad: Book

| Atributo | Tipo | Validaciones | Descripción |
|----------|------|--------------|-------------|
| `id` | Long | Auto-generado | Identificador único |
| `title` | String | NotBlank, max 255 | Título del libro |
| `author` | String | NotBlank, max 255 | Autor del libro |
| `publicationDate` | LocalDate | PastOrPresent | Fecha de publicación |
| `category` | String | NotBlank | Categoría/Género |
| `isbn` | String | NotBlank, unique | Código ISBN |
| `rating` | Integer | Min 1, Max 5 | Valoración (1-5) |
| `visible` | Boolean | NotNull | Visibilidad en frontend |
| `stock` | Integer | Min 0 | Cantidad disponible |
| `price` | BigDecimal | Positive | Precio del libro |

---

## 🔗 Tabla de Endpoints

| Método HTTP | URI | Query Params | Request Body | Response Body | Códigos |
|-------------|-----|--------------|--------------|---------------|---------|
| POST | `/api/v1/books` | N/A | BookRequestDTO | BookResponseDTO | 201, 400, 500 |
| GET | `/api/v1/books/{id}` | N/A | N/A | BookResponseDTO | 200, 404, 500 |
| GET | `/api/v1/books` | title, author, category, isbn, ratingMin, ratingMax, visible, minPrice, maxPrice, minStock, publicationDateFrom, publicationDateTo | N/A | List of BookResponseDTO | 200, 400, 500 |
| PUT | `/api/v1/books/{id}` | N/A | BookRequestDTO | BookResponseDTO | 200, 400, 404, 500 |
| PATCH | `/api/v1/books/{id}` | N/A | BookPatchDTO | BookResponseDTO | 200, 400, 404, 500 |
| DELETE | `/api/v1/books/{id}` | N/A | N/A | Boolean | 200, 404, 500 |
| GET | `/api/v1/books/{id}/availability` | N/A | N/A | AvailabilityResponseDTO | 200, 404, 500 |
| PATCH | `/api/v1/books/{id}/stock` | N/A | StockUpdateDTO | BookResponseDTO | 200, 400, 404, 500 |

---

## 📝 Detalle de Endpoints

### POST /api/v1/books - Crear libro

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

---

### GET /api/v1/books/{id} - Obtener libro por ID

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

---

### GET /api/v1/books - Buscar libros con filtros

> ⚠️ **CRÍTICO**: Búsqueda por TODOS los atributos de forma INDIVIDUAL y COMBINADA.

**Query Parameters:**

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `title` | String | Búsqueda parcial (LIKE) |
| `author` | String | Búsqueda parcial (LIKE) |
| `category` | String | Categoría exacta |
| `isbn` | String | ISBN exacto |
| `ratingMin` | Integer | Valoración mínima |
| `ratingMax` | Integer | Valoración máxima |
| `visible` | Boolean | Filtrar visibilidad |
| `minPrice` | BigDecimal | Precio mínimo |
| `maxPrice` | BigDecimal | Precio máximo |
| `minStock` | Integer | Stock mínimo |
| `publicationDateFrom` | LocalDate | Fecha desde |
| `publicationDateTo` | LocalDate | Fecha hasta |

**Ejemplos de búsqueda individual:**
```
GET /api/v1/books?author=cervantes
GET /api/v1/books?category=Clásicos
GET /api/v1/books?ratingMin=4
GET /api/v1/books?visible=true
```

**Ejemplos de búsqueda combinada:**
```
GET /api/v1/books?author=cervantes&category=Clásicos&ratingMin=4&visible=true
GET /api/v1/books?minPrice=10&maxPrice=30&minStock=5
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

### PUT /api/v1/books/{id} - Actualizar libro completo

**Request Body:** Igual que POST (todos los campos)

---

### PATCH /api/v1/books/{id} - Actualizar libro parcial

**Request Body (BookPatchDTO):** Solo campos a modificar
```json
{
  "price": 24.99,
  "stock": 150
}
```

---

### DELETE /api/v1/books/{id} - Eliminar libro

**Response 200 OK:**
```json
true
```

---

### GET /api/v1/books/{id}/availability - Verificar disponibilidad

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

---

### PATCH /api/v1/books/{id}/stock - Actualizar stock

> 🔗 **Usado por ms-books-payments** para decrementar/incrementar stock.

**Request Body (StockUpdateDTO):**
```json
{
  "quantity": -2
}
```

**Response 200 OK (BookResponseDTO):** Libro con stock actualizado

---

## 🛠️ Implementación - Capa Repository (2 capas)

### BookJpaRepository.java (Interface JPA)

```java
package com.relatosdepapel.catalogue.repository;

import com.relatosdepapel.catalogue.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

/**
 * Interface JPA con métodos de consulta personalizados
 */
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

### BookRepository.java (Wrapper/Abstracción)

```java
package com.relatosdepapel.catalogue.repository;

import com.relatosdepapel.catalogue.entity.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Clase wrapper que abstrae el acceso a JpaRepository
 */
@Repository
@RequiredArgsConstructor
public class BookRepository {

    private final BookJpaRepository jpaRepository;

    public List<Book> getAll() {
        return jpaRepository.findAll();
    }

    public Optional<Book> getById(Long id) {
        return jpaRepository.findById(id);
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
    
    public List<Book> findByVisibleTrue() {
        return jpaRepository.findByVisibleTrue();
    }

    // Búsqueda con Specifications (para filtros combinados)
    public List<Book> search(Specification<Book> spec) {
        return jpaRepository.findAll(spec);
    }
}
```

---

## 🛠️ Implementación - Capa Service (2 capas)

### BookService.java (Interface)

```java
package com.relatosdepapel.catalogue.service;

import com.relatosdepapel.catalogue.dto.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface que define el contrato del servicio de libros
 */
public interface BookService {
    
    // GET /books
    List<BookResponseDTO> getAll();
    
    // GET /books/{id}
    BookResponseDTO getById(Long id);
    
    // GET /books con filtros
    List<BookResponseDTO> search(
        String title,
        String author,
        String category,
        String isbn,
        Integer ratingMin,
        Integer ratingMax,
        Boolean visible,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer minStock,
        LocalDate publicationDateFrom,
        LocalDate publicationDateTo
    );
    
    // POST /books
    BookResponseDTO create(BookRequestDTO dto);
    
    // PUT /books/{id}
    BookResponseDTO update(Long id, BookRequestDTO dto);
    
    // PATCH /books/{id}
    BookResponseDTO partialUpdate(Long id, BookPatchDTO dto);
    
    // DELETE /books/{id}
    Boolean delete(Long id);
    
    // GET /books/{id}/availability
    AvailabilityResponseDTO checkAvailability(Long id);
    
    // PATCH /books/{id}/stock
    BookResponseDTO updateStock(Long id, StockUpdateDTO dto);
}
```

### BookServiceImpl.java (Implementación)

```java
package com.relatosdepapel.catalogue.service;

import com.relatosdepapel.catalogue.dto.*;
import com.relatosdepapel.catalogue.entity.Book;
import com.relatosdepapel.catalogue.exception.ResourceNotFoundException;
import com.relatosdepapel.catalogue.repository.BookRepository;
import com.relatosdepapel.catalogue.specification.BookSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
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
        Book book = repository.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
        return toResponseDTO(book);
    }

    @Override
    public List<BookResponseDTO> search(
            String title, String author, String category, String isbn,
            Integer ratingMin, Integer ratingMax, Boolean visible,
            BigDecimal minPrice, BigDecimal maxPrice, Integer minStock,
            LocalDate publicationDateFrom, LocalDate publicationDateTo) {
        
        Specification<Book> spec = BookSpecification.withFilters(
            title, author, category, isbn, ratingMin, ratingMax, 
            visible, minPrice, maxPrice, minStock, 
            publicationDateFrom, publicationDateTo
        );
        
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
        Book book = repository.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
        
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
    public BookResponseDTO partialUpdate(Long id, BookPatchDTO dto) {
        Book book = repository.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
        
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
    public Boolean delete(Long id) {
        return repository.getById(id)
                .map(book -> {
                    repository.delete(book);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public AvailabilityResponseDTO checkAvailability(Long id) {
        Book book = repository.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
        
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
        Book book = repository.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
        
        int newStock = book.getStock() + dto.getQuantity();
        if (newStock < 0) {
            throw new IllegalArgumentException("Insufficient stock");
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
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setPublicationDate(dto.getPublicationDate());
        book.setCategory(dto.getCategory());
        book.setIsbn(dto.getIsbn());
        book.setRating(dto.getRating());
        book.setVisible(dto.getVisible());
        book.setStock(dto.getStock());
        book.setPrice(dto.getPrice());
        return book;
    }
}
```

---

## 🔍 BookSpecification.java (Para búsquedas combinadas)

```java
package com.relatosdepapel.catalogue.specification;

import com.relatosdepapel.catalogue.entity.Book;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> withFilters(
            String title, String author, String category, String isbn,
            Integer ratingMin, Integer ratingMax, Boolean visible,
            BigDecimal minPrice, BigDecimal maxPrice, Integer minStock,
            LocalDate publicationDateFrom, LocalDate publicationDateTo) {
        
        return (root, query, cb) -> {
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
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("publicationDate"), publicationDateFrom));
            }
            
            if (publicationDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("publicationDate"), publicationDateTo));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
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
    name: ms-books-catalogue
  
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
INSERT INTO book (title, author, publication_date, category, isbn, rating, visible, stock, price) VALUES
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
- [ ] Registro automático en Eureka
