# Cheatsheet: API RESTful con Spring Boot (Guia Operativa)

> Nivel: Intermedio
> Objetivo: Construir APIs REST de forma ordenada, reusable y consistente
> Uso: Sigue este documento en orden para crear una API desde cero
> Basado en: `ms-books-catalogue` y `ms-books-payments` del proyecto Relatos de Papel

---

## Indice - Orden de Creacion

| Paso | Seccion | Que creas |
| --- | --- | --- |
| 1 | Configuracion inicial | Proyecto en Spring Initializr + dependencias |
| 2 | Estructura de carpetas | Arquitectura 1 capa o 2 capas |
| 3 | Application | Punto de entrada Spring Boot |
| 4 | Constantes | `Consts.java` para columnas/campos |
| 5 | Entity | Modelo de base de datos |
| 6 | DTOs | Contratos de entrada/salida |
| 7 | Repository JPA | `JpaRepository` + `JpaSpecificationExecutor` |
| 8 | Repository Wrapper | Capa 2 opcional/recomendada |
| 9 | Service Interface | Contrato de negocio |
| 10 | Service Implementacion | Logica de negocio + mapeos |
| 11 | Controller | Endpoints y codigos HTTP |
| 12 | Manejo de errores | Opcion 1 o Opcion 2 (`GlobalExceptionHandler`) |
| 13 | Configuracion final y pruebas | `application.yaml`, `data.sql`, smoke tests |

---

## Flujo de una peticion HTTP

```text
Cliente HTTP -> Controller -> Service -> Repository Wrapper -> JPA Repository -> BD
                   |            |             |                     |
                   |            |             |                     -> SQL generado por Spring Data
                   |            |             -> metodos de negocio de acceso a datos
                   |            -> validaciones y reglas de negocio
                   -> contrato REST y codigos HTTP
```

Ejemplo real en tu proyecto:
- `GET /api/books/1`
- `BookController.getBookById()` -> `BookService.getById()` -> `BookRepository.getById()` -> `BookJpaRepository.findById()`

---

## 1. Configuracion Inicial

### 1.1 Spring Initializr - parametros base

Usa estos valores para crear un microservicio API como los tuyos:

| Campo | Valor recomendado |
| --- | --- |
| Project | Maven |
| Language | Java |
| Spring Boot | Compatible con Spring Cloud que uses |
| Group | `com.tuempresa` |
| Artifact | `ms-tu-dominio` |
| Packaging | Jar |
| Java | 21+ (en tu repo: 25) |

### 1.2 Dependencias a seleccionar en Spring Initializr (API REST)

Seleccion minima para un API tipo `ms-books-catalogue`/`ms-books-payments`:

| Categoria | Dependencia Initializr | Uso |
| --- | --- | --- |
| Web | `Spring Web` | Endpoints REST |
| Data | `Spring Data JPA` | Persistencia |
| Validation | `Validation` | Validaciones DTO/entrada |
| Database | `H2 Database` | Desarrollo local |
| Cloud | `Eureka Discovery Client` | Registro en Eureka |
| Ops | `Spring Boot Actuator` | Salud y monitoreo basico |
| Utils | `Lombok` | Reducir boilerplate |
| Dev (opcional) | `Spring Boot DevTools` | Reinicio rapido |

### 1.3 Dependencias manuales adicionales (segun tu proyecto)

Tu proyecto usa ademas:
- `spring-boot-h2console` (si quieres consola H2 dedicada).
- `org.apache.httpcomponents.client5:httpclient5` (en `ms-books-payments`, para `PATCH` con `RestTemplate`).

### 1.4 `pom.xml` base (adaptado al patron de tu repo)

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

## 2. Estructura de Carpetas y Arquitectura

### 2.1 Opcion A - Simple (1 capa de repository)

```text
src/main/java/com/tuempresa/api/
  controller/
  service/
  repository/
    RecursoRepository.java  (interface extends JpaRepository)
  entity/
  dto/
  utils/
```

Usala cuando:
- Proyecto pequeno.
- Pocos endpoints.
- No necesitas aislar Spring Data del service.

### 2.2 Opcion B - Recomendada (2 capas de repository, como tu proyecto)

```text
src/main/java/com/tuempresa/api/
  controller/
    RecursoController.java
  service/
    RecursoService.java
    RecursoServiceImpl.java
  repository/
    RecursoJpaRepository.java   (Capa 1: Spring Data)
    RecursoRepository.java      (Capa 2: Wrapper de negocio)
  specification/
    RecursoSpecification.java
  entity/
    Recurso.java
  dto/
    RecursoRequestDTO.java
    RecursoPatchDTO.java
    RecursoResponseDTO.java
    ErrorResponseDTO.java
  utils/
    Consts.java
  exception/                    (solo si eliges Opcion 2 de errores)
    GlobalExceptionHandler.java
    RecursoNoEncontradoException.java
```

Usala cuando:
- Quieres codigo mas mantenible.
- Tendras busquedas dinamicas/specifications.
- Quieres desacoplar capa service de JPA.

### 2.3 Comparacion 1 capa vs 2 capas

| Criterio | 1 capa | 2 capas |
| --- | --- | --- |
| Complejidad inicial | Baja | Media |
| Escalabilidad | Media | Alta |
| Acoplamiento a Spring Data | Alto | Bajo |
| Claridad de metodos de negocio | Media | Alta |
| Recomendacion para tu estilo actual | No | Si |

---

## 3. Application - Punto de entrada

Archivo: `MsBooksCatalogueApplication.java` / `MsBooksPaymentsApplication.java`

```java
@SpringBootApplication
public class MsBooksCatalogueApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsBooksCatalogueApplication.class, args);
    }
}
```

---

## 4. Constantes - `Consts.java`

Patron de tu repo:
- Definir nombres de campos de entidad para evitar strings repetidos.
- Usarlos en `@Column` y en `Specification`.

```java
public class Consts {
    public static final String TITLE = "title";
    public static final String ISBN = "isbn";
    public static final String STOCK = "stock";

    private Consts() {
        throw new UnsupportedOperationException("Util class");
    }
}
```

---

## 5. Entity - Modelo BD

Patron observado en `Book`:
- Restricciones de datos en anotaciones JPA.
- Metodo `updateFromDTO` para actualizacion controlada.

```java
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

    @Column(name = Consts.ISBN, unique = true, nullable = false)
    private String isbn;

    @Column(name = Consts.STOCK, nullable = false)
    private Integer stock;

    public void updateFromDTO(BookRequestDTO dto) {
        this.title = dto.getTitle();
        this.stock = dto.getStock();
        // id e isbn no se actualizan aqui
    }
}
```

---

## 6. DTOs - Contratos API

Separacion recomendada y aplicada en tu proyecto:

| DTO | Uso |
| --- | --- |
| `RequestDTO` | `POST/PUT` (entrada completa) |
| `PatchDTO` | `PATCH` (entrada parcial) |
| `ResponseDTO` | `GET/POST/PUT/PATCH` (salida) |
| `ErrorResponseDTO` | Respuestas de error |
| DTOs de negocio | Ej: `AvailabilityResponseDTO`, `StockUpdateDTO` |

Regla:
- Nunca exponer `Entity` directamente al cliente.

---

## 7. Repository JPA (Capa 1)

```java
public interface BookJpaRepository
        extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    boolean existsByIsbn(String isbn);
    List<Book> findByVisibleTrue();
    List<Book> findByTitleContainingIgnoreCase(String title);
}
```

Que ganas:
- CRUD automatico.
- Query methods por nombre.
- Soporte a filtros avanzados con `Specification`.

---

## 8. Repository Wrapper (Capa 2)

```java
@Repository
@RequiredArgsConstructor
public class BookRepository {
    private final BookJpaRepository jpaRepository;

    public Book getById(Long id) { return jpaRepository.findById(id).orElse(null); }
    public List<Book> findVisibleBooks() { return jpaRepository.findByVisibleTrue(); }
    public boolean existsByIsbn(String isbn) { return jpaRepository.existsByIsbn(isbn); }
    public Book save(Book book) { return jpaRepository.save(book); }
    public void delete(Book book) { jpaRepository.delete(book); }
    public List<Book> search(Specification<Book> spec) { return jpaRepository.findAll(spec); }
}
```

Idea clave:
- Esta clase traduce metodos tecnicos de JPA a metodos de negocio legibles.

---

## 9. Service Interface (Contrato)

Patron en tu repo:
- CRUD + metodos de negocio + busqueda dinamica.

```java
public interface BookService {
    List<BookResponseDTO> getAll();
    BookResponseDTO getById(Long id);
    BookResponseDTO create(BookRequestDTO dto);
    BookResponseDTO update(Long id, BookRequestDTO dto);
    BookResponseDTO patch(Long id, BookPatchDTO dto);
    boolean delete(Long id);

    List<BookResponseDTO> search(...filtros...);
    AvailabilityResponseDTO checkAvailability(Long id);
    BookResponseDTO updateStock(Long id, StockUpdateDTO dto);
}
```

---

## 10. Service Implementacion (Logica de negocio)

Patrones que debes repetir:
1. Validar reglas de negocio antes de guardar.
2. Convertir `DTO -> Entity` y `Entity -> DTO`.
3. En `PATCH`, modificar solo campos no nulos.
4. En busqueda dinamica, construir `Specification` incremental.

Snippet:

```java
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;

    @Override
    public BookResponseDTO create(BookRequestDTO dto) {
        if (bookRepository.existsByIsbn(dto.getIsbn())) {
            throw new IllegalArgumentException("El ISBN ya existe");
        }
        Book saved = bookRepository.save(toEntity(dto));
        return toResponseDTO(saved);
    }
}
```

---

## 11. Controller (REST + codigos HTTP)

### 11.1 Tabla de endpoints reales de tu proyecto

#### `ms-books-catalogue` (`/api/books`)

| Metodo | URI | Request Body | Response Body | Codigos |
| --- | --- | --- | --- | --- |
| POST | `/api/books` | `BookRequestDTO` | `BookResponseDTO` | 201, 400, 409 |
| GET | `/api/books` | - | `List<BookResponseDTO>` | 200 |
| GET | `/api/books/{id}` | - | `BookResponseDTO` | 200, 404 |
| PUT | `/api/books/{id}` | `BookRequestDTO` | `BookResponseDTO` | 200, 400, 404 |
| PATCH | `/api/books/{id}` | `BookPatchDTO` | `BookResponseDTO` | 200, 404 |
| DELETE | `/api/books/{id}` | - | - | 204, 404 |
| GET | `/api/books/search` | - | `List<BookResponseDTO>` | 200 |
| GET | `/api/books/{id}/availability` | - | `AvailabilityResponseDTO` | 200, 404 |
| PATCH | `/api/books/{id}/stock` | `StockUpdateDTO` | `BookResponseDTO` | 200, 400, 404 |

#### `ms-books-payments` (`/api/payments`)

| Metodo | URI | Request Body | Response Body | Codigos |
| --- | --- | --- | --- | --- |
| POST | `/api/payments` | `PaymentRequestDTO` | `PaymentResponseDTO` | 201, 400, 404, 500 |
| GET | `/api/payments` | - | `List<PaymentResponseDTO>` | 200 |
| GET | `/api/payments/{id}` | - | `PaymentResponseDTO` | 200, 404 |
| GET | `/api/payments/search` | - | `List<PaymentResponseDTO>` | 200 |
| PATCH | `/api/payments/{id}` | `PaymentStatusDTO` | `PaymentResponseDTO` | 200, 400, 404 |
| DELETE | `/api/payments/{id}` | - | - | 204, 404, 409, 500 |

### 11.2 Tabla de ResponseEntity mas usados

| Metodo ResponseEntity | Codigo | Uso |
| --- | --- | --- |
| `ResponseEntity.ok(body)` | 200 | Exito con datos |
| `ResponseEntity.status(201).body(x)` | 201 | Creacion |
| `ResponseEntity.noContent().build()` | 204 | Exito sin body |
| `ResponseEntity.badRequest().body(err)` | 400 | Validacion |
| `ResponseEntity.notFound().build()` | 404 | No existe |
| `ResponseEntity.status(409).body(err)` | 409 | Conflicto |

---

## 12. Manejo de Errores - Dos Opciones

### Opcion 1: Manejar errores en Controller con `ResponseEntity` (simple)

Ventajas:
- Implementacion rapida.
- Facil para proyectos pequenos o medianos.
- Ya esta en tu proyecto actual.

Ejemplo:

```java
@GetMapping("/{id}")
public ResponseEntity<BookResponseDTO> getById(@PathVariable Long id) {
    BookResponseDTO book = bookService.getById(id);
    if (book == null) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(book);
}
```

### Opcion 2: `GlobalExceptionHandler` (centralizado)

Ventajas:
- Respuestas de error uniformes en toda la API.
- Controllers mas limpios.
- Escala mejor cuando hay muchos controllers.

Estructura adicional:

```text
dto/ErrorResponseDTO.java
exception/BookNotFoundException.java
exception/GlobalExceptionHandler.java
```

`ErrorResponseDTO` recomendado:

```java
@Data
@AllArgsConstructor
public class ErrorResponseDTO {
    private Integer code;
    private String message;
    private LocalDateTime timestamp;
}
```

Excepcion custom:

```java
public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String message) {
        super(message);
    }
}
```

`GlobalExceptionHandler`:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(BookNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO(404, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpected(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO(500, "Error interno", LocalDateTime.now()));
    }
}
```

Comparacion:

| Aspecto | Opcion 1 (Controller) | Opcion 2 (GlobalExceptionHandler) |
| --- | --- | --- |
| Complejidad | Baja | Media |
| Repeticion de codigo | Media/Alta | Baja |
| Consistencia de errores JSON | Media | Alta |
| Curva de aprendizaje | Baja | Media |
| Recomendado para empezar | Si | Si, cuando crece la API |

---

## 13. Configuracion Final y Ejecucion

### 13.1 `application.yaml` base para API con Eureka

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
    password: ""
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    defer-datasource-initialization: true
  h2:
    console:
      enabled: true
      path: /h2-console

eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka
```

### 13.2 Orden de arranque (si hay arquitectura completa)
1. `eureka-server`
2. `gateway`
3. `ms-books-catalogue`
4. `ms-books-payments`

### 13.3 Smoke tests minimos

Crear libro:

```bash
curl -X POST "http://localhost:8081/api/books" \
  -H "Content-Type: application/json" \
  -d '{
    "title":"Libro test",
    "author":"Autor",
    "publicationDate":"2024-01-01",
    "category":"Demo",
    "isbn":"TEST-001",
    "rating":5,
    "visible":true,
    "stock":10,
    "price":20.00
  }'
```

Buscar con filtros:

```bash
curl "http://localhost:8081/api/books/search?title=Libro&visible=true"
```

---

## Troubleshooting

| Problema | Causa probable | Solucion |
| --- | --- | --- |
| `409 Conflict` al crear libro | ISBN duplicado | Validar `existsByIsbn` antes de guardar |
| `400 Bad Request` en create/update | Campos invalidos o vacios | Validar en controller/service y devolver `ErrorResponseDTO` |
| `404` por id existente en URL | No existe en BD o no visible en flujo | Verificar metodo de busqueda y datos cargados |
| `PATCH` no actualiza | DTO parcial mal mapeado | Revisar null checks en `patch()` |
| Filtros no retornan datos | `Specification` mal construida | Validar cada filtro y combinacion `and()` |

---

## Buenas Practicas y Anti-Patrones

### Haz esto
- Sigue orden fijo: Entity -> DTO -> Repository -> Service -> Controller.
- Usa arquitectura de 2 capas en repository cuando el servicio crezca.
- Define contratos claros y estables para request/response.
- Separa errores de validacion (`400`) de conflictos (`409`) y no encontrados (`404`).
- Mantiene DTOs de negocio explicitos (`Availability`, `StockUpdate`) para reglas claras.

### Evita esto
- Meter logica de negocio en controller.
- Exponer entities directamente.
- Mezclar ambos modelos de error sin criterio.
- Hardcodear strings repetidos de campos/columnas.

---

## Checklist para Nueva API REST

### Inicializacion
- [ ] Crear proyecto en Spring Initializr.
- [ ] Seleccionar dependencias correctas segun el tipo de API.
- [ ] Configurar `pom.xml` y `application.yaml`.

### Arquitectura
- [ ] Definir si usaras repository 1 capa o 2 capas.
- [ ] Crear estructura de carpetas completa.
- [ ] Crear clase `Application` y `Consts`.

### Implementacion por recurso
- [ ] Entity con restricciones JPA.
- [ ] DTOs (`Request`, `Patch`, `Response`).
- [ ] JPA repository.
- [ ] Wrapper repository (si opcion 2 capas).
- [ ] Service interface + impl.
- [ ] Controller con tabla de codigos HTTP clara.

### Manejo de errores (elige una opcion)
- [ ] Opcion 1: `ResponseEntity` en controller.
- [ ] Opcion 2: `GlobalExceptionHandler` + excepciones custom.

### Validacion final
- [ ] CRUD completo probado.
- [ ] Endpoints de negocio probados.
- [ ] Respuestas de error consistentes.
- [ ] Smoke tests con `curl` o Postman.

---

## Referencias del Proyecto Base

- `ms-books-catalogue/src/main/java/com/relatosdepapel/ms_books_catalogue/controller/BookController.java`
- `ms-books-catalogue/src/main/java/com/relatosdepapel/ms_books_catalogue/service/BookService.java`
- `ms-books-catalogue/src/main/java/com/relatosdepapel/ms_books_catalogue/service/BookServiceImpl.java`
- `ms-books-catalogue/src/main/java/com/relatosdepapel/ms_books_catalogue/repository/BookJpaRepository.java`
- `ms-books-catalogue/src/main/java/com/relatosdepapel/ms_books_catalogue/repository/BookRepository.java`
- `ms-books-catalogue/src/main/java/com/relatosdepapel/ms_books_catalogue/specification/BookSpecification.java`
- `ms-books-catalogue/src/main/resources/application.yaml`
- `ms-books-payments/src/main/java/com/relatosdepapel/ms_books_payments/controller/PaymentController.java`
- `ms-books-payments/src/main/java/com/relatosdepapel/ms_books_payments/service/PaymentServiceImpl.java`
- `ms-books-payments/src/main/resources/application.yaml`

---

## Recursos adicionales

- [Spring Boot Reference](https://docs.spring.io/spring-boot/reference/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/)
- [Spring Cloud Netflix](https://docs.spring.io/spring-cloud-netflix/reference/)
- [HTTP Status Codes](https://developer.mozilla.org/es/docs/Web/HTTP/Status)
