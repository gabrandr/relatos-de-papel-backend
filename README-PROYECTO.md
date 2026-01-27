# 📚 Relatos de Papel - Backend Microservicios

> **Proyecto Transversal del Máster - Actividad 2: Desarrollo de back-end utilizando Java y Spring**

---

## 📋 Información del Proyecto

| Campo | Valor |
|-------|-------|
| **Nombre** | Relatos de Papel - Backend |
| **Tipo** | Backend (Arquitectura de Microservicios) |
| **Stack** | Java 17+ / Spring Boot 3.x / Spring Cloud |
| **Bases de Datos** | H2 o MySQL/PostgreSQL (una por microservicio) |
| **Contexto** | Proyecto académico de maestría |

---

## 🎯 Objetivo Principal

Desarrollar el backend de una aplicación web de librería online utilizando arquitectura de microservicios con Java y Spring, que permita:
- Gestionar un catálogo de libros (CRUD + búsquedas avanzadas)
- Registrar compras de libros
- Comunicación entre microservicios vía Eureka
- Gateway como punto de entrada único que **transcriba peticiones POST a los métodos HTTP correspondientes**

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              CLIENTE                                     │
│                        (Postman / Swagger / Frontend)                    │
│                                                                          │
│   Envía SIEMPRE POST con JSON:                                          │
│   {                                                                      │
│     "targetMethod": "GET|POST|PUT|PATCH|DELETE",                        │
│     "queryParams": { "key": ["value"] },                                │
│     "body": { ... }                                                     │
│   }                                                                      │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼ (Solo peticiones POST)
┌─────────────────────────────────────────────────────────────────────────┐
│                     SPRING CLOUD GATEWAY (Puerto 8762)                   │
│                                                                          │
│   RequestTranslationFilter → Lee targetMethod del JSON                   │
│   RequestDecoratorFactory  → Crea decorator según método                 │
│   *RequestDecorator        → Transforma la petición                      │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
┌───────────────────────────────────┐   ┌───────────────────────────────┐
│     EUREKA SERVER                 │◄──│     Registro automático       │
│     (Puerto 8761)                 │   │     de microservicios         │
└───────────────────────────────────┘   └───────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
┌───────────────────────────────────┐   ┌───────────────────────────────┐
│   MS-BOOKS-CATALOGUE              │   │   MS-BOOKS-PAYMENTS           │
│   (Puerto 8081)                   │   │   (Puerto 8082)               │
│   • Catálogo de libros            │◄──│   • Registro de compras       │
│   • CRUD completo                 │   │   • Valida libros via HTTP    │
│   • Búsquedas avanzadas           │   │   • Usa nombre Eureka         │
│   • BD: catalogue_db              │   │   • BD: payments_db           │
└───────────────────────────────────┘   └───────────────────────────────┘
```

---

## 🚀 Creación de Proyectos con Spring Initializr

### 🌐 URL: https://start.spring.io

---

### 1️⃣ Eureka Server

| Campo | Valor |
|-------|-------|
| **Project** | Maven |
| **Language** | Java |
| **Spring Boot** | 3.2.x (o la más reciente estable) |
| **Group** | com.relatosdepapel |
| **Artifact** | eureka-server |
| **Name** | eureka-server |
| **Package name** | com.relatosdepapel.eureka |
| **Packaging** | Jar |
| **Java** | 17 o 21 |

**Dependencias a seleccionar:**
- ✅ Eureka Server
- ✅ Spring Boot Actuator

---

### 2️⃣ API Gateway (Ya tienes el código)

| Campo | Valor |
|-------|-------|
| **Project** | Maven |
| **Language** | Java |
| **Spring Boot** | 3.2.x |
| **Group** | com.relatosdepapel |
| **Artifact** | gateway |
| **Name** | gateway |
| **Package name** | com.relatosdepapel.gateway |
| **Packaging** | Jar |
| **Java** | 17 o 21 |

**Dependencias a seleccionar:**
- ✅ Gateway (Spring Cloud Gateway)
- ✅ Eureka Discovery Client
- ✅ Spring Boot Actuator
- ✅ Lombok

---

### 3️⃣ MS Books Catalogue (Microservicio Buscador)

| Campo | Valor |
|-------|-------|
| **Project** | Maven |
| **Language** | Java |
| **Spring Boot** | 3.2.x |
| **Group** | com.relatosdepapel |
| **Artifact** | ms-books-catalogue |
| **Name** | ms-books-catalogue |
| **Package name** | com.relatosdepapel.catalogue |
| **Packaging** | Jar |
| **Java** | 17 o 21 |

**Dependencias a seleccionar:**
- ✅ Spring Web
- ✅ Spring Data JPA
- ✅ H2 Database
- ✅ Eureka Discovery Client
- ✅ Spring Boot Actuator
- ✅ Lombok

---

### 4️⃣ MS Books Payments (Microservicio Operador)

| Campo | Valor |
|-------|-------|
| **Project** | Maven |
| **Language** | Java |
| **Spring Boot** | 3.2.x |
| **Group** | com.relatosdepapel |
| **Artifact** | ms-books-payments |
| **Name** | ms-books-payments |
| **Package name** | com.relatosdepapel.payments |
| **Packaging** | Jar |
| **Java** | 17 o 21 |

**Dependencias a seleccionar:**
- ✅ Spring Web
- ✅ Spring Data JPA
- ✅ H2 Database
- ✅ Eureka Discovery Client
- ✅ Spring Boot Actuator
- ✅ Lombok

---

## 📁 Estructura de Carpetas del Proyecto

```
relatos-de-papel-backend/
├── README-PROYECTO.md
├── api-ms-books-catalogue.md
├── api-ms-books-payments.md
├── api-gateway.md
│
├── eureka-server/
│   ├── src/main/java/com/relatosdepapel/eureka/
│   │   └── EurekaServerApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
│
├── gateway/                        ← YA IMPLEMENTADO
│   ├── src/main/java/com/unir/gateway/
│   │   ├── GatewayAndFiltersApplication.java
│   │   ├── config/
│   │   │   └── MapperConfig.java
│   │   ├── decorator/
│   │   │   ├── RequestDecoratorFactory.java
│   │   │   ├── GetRequestDecorator.java
│   │   │   ├── PostRequestDecorator.java
│   │   │   ├── PutRequestDecorator.java
│   │   │   ├── PatchRequestDecorator.java
│   │   │   └── DeleteRequestDecorator.java
│   │   ├── filter/
│   │   │   └── RequestTranslationFilter.java
│   │   ├── model/
│   │   │   └── GatewayRequest.java
│   │   └── utils/
│   │       └── RequestBodyExtractor.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
│
├── ms-books-catalogue/
│   ├── src/main/java/com/relatosdepapel/catalogue/
│   │   ├── MsBooksCatalogueApplication.java
│   │   ├── controller/
│   │   │   └── BookController.java
│   │   ├── service/
│   │   │   ├── BookService.java              ← Interface
│   │   │   └── BookServiceImpl.java          ← Implementación
│   │   ├── repository/
│   │   │   ├── BookJpaRepository.java        ← Interface JPA
│   │   │   └── BookRepository.java           ← Wrapper
│   │   ├── entity/
│   │   │   └── Book.java
│   │   └── dto/
│   │       ├── BookRequestDTO.java
│   │       ├── BookResponseDTO.java
│   │       ├── BookPatchDTO.java
│   │       ├── AvailabilityResponseDTO.java
│   │       ├── StockUpdateDTO.java
│   │       └── ErrorResponseDTO.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── data.sql
│   └── pom.xml
│
├── ms-books-payments/
│   ├── src/main/java/com/relatosdepapel/payments/
│   │   ├── MsBooksPaymentsApplication.java
│   │   ├── controller/
│   │   │   └── PaymentController.java
│   │   ├── service/
│   │   │   ├── PaymentService.java           ← Interface
│   │   │   └── PaymentServiceImpl.java       ← Implementación
│   │   ├── repository/
│   │   │   ├── PaymentJpaRepository.java     ← Interface JPA
│   │   │   └── PaymentRepository.java        ← Wrapper
│   │   ├── entity/
│   │   │   └── Payment.java
│   │   ├── dto/
│   │   │   ├── PaymentRequestDTO.java
│   │   │   ├── PaymentResponseDTO.java
│   │   │   ├── PaymentStatusDTO.java
│   │   │   ├── BookAvailabilityDTO.java
│   │   │   ├── StockUpdateDTO.java
│   │   │   ├── UserPaymentsResponseDTO.java
│   │   │   └── ErrorResponseDTO.java
│   │   ├── client/
│   │   │   └── BookCatalogueClient.java
│   │   └── config/
│   │       └── RestTemplateConfig.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── data.sql
│   └── pom.xml
│
└── sql/
    ├── catalogue_db_schema.sql
    └── payments_db_schema.sql
```

> ⚠️ **Nota:** No se usa `GlobalExceptionHandler` ni excepciones personalizadas. El manejo de errores se hace directamente en el Controller con `ResponseEntity`.

---

## 📊 Rúbrica de Calificación (Máxima Nota: 10 puntos)

| # | Criterio | Puntos | Peso | Archivo Referencia |
|---|----------|--------|------|-------------------|
| 1 | **Definición API REST del buscador** | 2.0 | 20% | `api-ms-books-catalogue.md` |
| 2 | **Implementación buscador con Spring Data JPA** (búsqueda por todos los atributos, individual o combinada) | 2.0 | 20% | `api-ms-books-catalogue.md` |
| 3 | **Definición API REST del operador** | 1.0 | 10% | `api-ms-books-payments.md` |
| 4 | **Implementación del operador** | 1.0 | 10% | `api-ms-books-payments.md` |
| 5 | **Balanceo Eureka** (peticiones sin IP ni puerto) | 0.75 | 7.5% | Configuración `application.yml` |
| 6 | **Servidor Eureka** | 0.25 | 2.5% | `eureka-server/` |
| 7 | **Gateway con transcripción POST** ✅ YA IMPLEMENTADO | 2.0 | 20% | `api-gateway.md` |
| 8 | **Videomemoria** (15 min máx) | 1.0 | 10% | Guía al final |
| | **TOTAL** | **10** | **100%** | |

---

## 📖 Archivos de Diseño de API

| Archivo | Descripción | Prioridad |
|---------|-------------|-----------|
| `api-ms-books-catalogue.md` | API REST completa del microservicio buscador | 🔴 Alta |
| `api-ms-books-payments.md` | API REST completa del microservicio operador | 🔴 Alta |
| `api-gateway.md` | Diseño del Gateway (ya implementado) | 🟡 Media |

---

## 🔄 Orden de Desarrollo Recomendado

| # | Paso | Descripción | Estado |
|---|------|-------------|--------|
| 1 | **Eureka Server** | Crear con Spring Initializr + configurar | ⬜ Pendiente |
| 2 | **Gateway** | Copiar código existente | ✅ Listo |
| 3 | **MS Catalogue** | Crear con Spring Initializr | ⬜ Pendiente |
| 4 | **MS Catalogue - Entity** | Crear entidad Book | ⬜ Pendiente |
| 5 | **MS Catalogue - Repository** | 2 capas: JpaRepository + Wrapper | ⬜ Pendiente |
| 6 | **MS Catalogue - Service** | 2 capas: Interface + Impl | ⬜ Pendiente |
| 7 | **MS Catalogue - Controller** | Endpoints REST con ResponseEntity | ⬜ Pendiente |
| 8 | **MS Payments** | Crear con Spring Initializr | ⬜ Pendiente |
| 9 | **MS Payments - Entity** | Crear entidad Payment | ⬜ Pendiente |
| 10 | **MS Payments - Repository** | 2 capas: JpaRepository + Wrapper | ⬜ Pendiente |
| 11 | **MS Payments - Client** | Cliente HTTP para Catalogue | ⬜ Pendiente |
| 12 | **MS Payments - Service** | 2 capas: Interface + Impl | ⬜ Pendiente |
| 13 | **MS Payments - Controller** | Endpoints REST con ResponseEntity | ⬜ Pendiente |
| 14 | **Pruebas** | Probar flujo completo | ⬜ Pendiente |
| 15 | **Videomemoria** | Grabar demostración 15 min | ⬜ Pendiente |

---

## 📦 Entidades del Sistema

### Book (ms-books-catalogue)

| Atributo | Tipo | Descripción | Búsqueda |
|----------|------|-------------|----------|
| `id` | Long | Identificador único | ❌ |
| `title` | String | Título del libro | ✅ Individual y combinada |
| `author` | String | Autor del libro | ✅ Individual y combinada |
| `publicationDate` | LocalDate | Fecha de publicación | ✅ Individual y combinada |
| `category` | String | Categoría/Género | ✅ Individual y combinada |
| `isbn` | String | Código ISBN único | ✅ Individual y combinada |
| `rating` | Integer | Valoración (1-5) | ✅ Individual y combinada |
| `visible` | Boolean | Visibilidad | ✅ Individual y combinada |
| `stock` | Integer | Cantidad disponible | ✅ Para validación |
| `price` | BigDecimal | Precio del libro | ✅ Individual y combinada |

### Payment (ms-books-payments)

| Atributo | Tipo | Descripción |
|----------|------|-------------|
| `id` | Long | Identificador único |
| `userId` | Long | ID del usuario que compra |
| `bookId` | Long | ID del libro comprado |
| `bookTitle` | String | Título (desnormalizado) |
| `quantity` | Integer | Cantidad comprada |
| `unitPrice` | BigDecimal | Precio unitario |
| `totalPrice` | BigDecimal | Precio total |
| `purchaseDate` | LocalDateTime | Fecha de compra |
| `status` | String | COMPLETED, CANCELLED |

---

## ⚙️ Configuración de Puertos

| Componente | Puerto | URL Base |
|------------|--------|----------|
| Eureka Server | 8761 | http://localhost:8761 |
| API Gateway | 8762 | http://localhost:8762 |
| MS Books Catalogue | 8081 | http://localhost:8081 |
| MS Books Payments | 8082 | http://localhost:8082 |

---

## 🔑 Puntos Críticos para Máxima Nota

### ✅ Criterios 1 y 2: API del Buscador (4 puntos - 40%)
- [ ] API REST siguiendo recomendaciones del curso
- [ ] CRUD completo de libros
- [ ] **Búsqueda por TODOS los atributos de forma individual**
- [ ] **Búsqueda COMBINADA de múltiples atributos**
- [ ] 2 capas Repository: JpaRepository + Wrapper
- [ ] 2 capas Service: Interface + Impl
- [ ] Manejo de errores con ResponseEntity

### ✅ Criterios 3 y 4: API del Operador (2 puntos - 20%)
- [ ] API REST siguiendo recomendaciones
- [ ] Registrar compras
- [ ] **Validar libros llamando a ms-books-catalogue**
- [ ] Verificar: existencia, visibilidad, stock
- [ ] 2 capas Repository + 2 capas Service
- [ ] Manejo de errores con ResponseEntity

### ✅ Criterios 5 y 6: Eureka (1 punto - 10%)
- [ ] Servidor Eureka funcionando
- [ ] **Peticiones usando NOMBRE Eureka, no IP:puerto**
- [ ] Ejemplo: `http://ms-books-catalogue/api/v1/books/{id}`

### ✅ Criterio 7: Gateway (2 puntos - 20%) - YA IMPLEMENTADO
- [x] Gateway registrado en Eureka
- [x] Transcripción POST → GET/POST/PUT/PATCH/DELETE
- [x] RequestTranslationFilter + Decorators

### ✅ Criterio 8: Videomemoria (1 punto - 10%)
- [ ] Duración máxima: 15 minutos
- [ ] Mostrar inicio sin componentes desplegados
- [ ] Explicar API del buscador
- [ ] Explicar API del operador
- [ ] Desplegar y mostrar dashboard Eureka
- [ ] Mostrar rutas del Gateway
- [ ] **Demostrar llamada que implique comunicación entre microservicios**
- [ ] Todos los integrantes deben participar

---

## 🚀 Cómo Usar el Gateway

El Gateway espera peticiones POST con este formato JSON:

```json
{
  "targetMethod": "GET",
  "queryParams": {
    "author": ["cervantes"],
    "visible": ["true"]
  },
  "body": null
}
```

**Ejemplo para crear un libro:**
```json
{
  "targetMethod": "POST",
  "queryParams": null,
  "body": {
    "title": "El Quijote",
    "author": "Miguel de Cervantes",
    "isbn": "9788467033601",
    "price": 19.99,
    "stock": 100,
    "visible": true,
    "rating": 5,
    "category": "Clásicos"
  }
}
```

---

## 📹 Guía para Videomemoria (15 min máx)

1. **Introducción** (1 min) - Presentar equipo, mostrar nada desplegado
2. **API del Buscador** (3 min) - Endpoints y búsquedas
3. **API del Operador** (2 min) - Compras y validación
4. **Despliegue** (5 min) - Eureka → Gateway → Microservicios
5. **Demo en Postman** (3 min) - Petición al Gateway con comunicación entre microservicios
6. **Conclusiones** (1 min)

---

## 📝 Notas Finales

- **Cada microservicio usa su PROPIA base de datos**
- **NO usar IP ni puerto** en comunicaciones entre microservicios
- **El Gateway YA transcribe peticiones POST** ✅
- Usar **2 capas en Repository** (JpaRepository + Wrapper)
- Usar **2 capas en Service** (Interface + Impl)
- **Manejo de errores con ResponseEntity** (sin GlobalExceptionHandler)
- Entrega: **único archivo ZIP** sin carpetas `target`
