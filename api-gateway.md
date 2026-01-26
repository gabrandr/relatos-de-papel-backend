# API Gateway - Spring Cloud Gateway con Transcripción POST

> **Gateway perimetral que transcribe peticiones POST a los métodos HTTP correspondientes**
>
> ✅ **CÓDIGO YA IMPLEMENTADO** - Solo copiar al proyecto

---

## 📋 Información General

| Campo | Valor |
|-------|-------|
| **Nombre** | gateway |
| **Puerto** | 8762 |
| **Tecnología** | Spring Cloud Gateway WebFlux |
| **Nombre Eureka** | gateway |
| **Función** | Transcribe POST → GET/POST/PUT/PATCH/DELETE |

---

## 🚀 Spring Initializr (si necesitas recrear)

**URL:** https://start.spring.io

| Campo | Valor |
|-------|-------|
| Project | Maven |
| Language | Java |
| Spring Boot | 3.2.x |
| Group | com.relatosdepapel |
| Artifact | gateway |
| Package name | com.relatosdepapel.gateway |
| Java | 17 o 21 |

**Dependencias:**
- ✅ Gateway (Spring Cloud Gateway)
- ✅ Eureka Discovery Client
- ✅ Spring Boot Actuator
- ✅ Lombok

---

## 🏗️ Arquitectura del Gateway

```
┌─────────────────────────────────────────────────────────────────────┐
│                        POST Request                                  │
│  {                                                                   │
│    "targetMethod": "GET",                                           │
│    "queryParams": {"author": ["cervantes"]},                        │
│    "body": null                                                     │
│  }                                                                   │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    RequestTranslationFilter                          │
│  • Verifica que sea POST con Content-Type                           │
│  • Extrae el body JSON                                              │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    RequestBodyExtractor                              │
│  • Parsea JSON a GatewayRequest                                     │
│  • Configura headers                                                │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    RequestDecoratorFactory                           │
│  • Lee targetMethod del GatewayRequest                              │
│  • Crea el decorator correspondiente                                │
└─────────────────────────────────────────────────────────────────────┘
                                │
        ┌───────────┬───────────┼───────────┬───────────┐
        ▼           ▼           ▼           ▼           ▼
   GetRequest  PostRequest  PutRequest  PatchRequest  DeleteRequest
   Decorator   Decorator    Decorator   Decorator     Decorator
                                │
                                ▼
                    Microservicio Destino
```

---

## 📁 Estructura del Gateway

```
gateway/
├── src/main/java/com/unir/gateway/
│   ├── GatewayAndFiltersApplication.java
│   ├── config/
│   │   └── MapperConfig.java
│   ├── decorator/
│   │   ├── RequestDecoratorFactory.java
│   │   ├── GetRequestDecorator.java
│   │   ├── PostRequestDecorator.java
│   │   ├── PutRequestDecorator.java
│   │   ├── PatchRequestDecorator.java
│   │   └── DeleteRequestDecorator.java
│   ├── filter/
│   │   └── RequestTranslationFilter.java
│   ├── model/
│   │   └── GatewayRequest.java
│   └── utils/
│       └── RequestBodyExtractor.java
├── src/main/resources/
│   └── application.yml
└── pom.xml
```

---

## 📦 Modelo GatewayRequest

El cliente envía peticiones POST con este formato JSON:

```json
{
  "targetMethod": "GET | POST | PUT | PATCH | DELETE",
  "queryParams": {
    "param1": ["value1"],
    "param2": ["value2"]
  },
  "body": { }
}
```

**Campos:**
- `targetMethod`: Método HTTP real
- `queryParams`: Parámetros de consulta (formato array)
- `body`: Cuerpo de la petición

---

## ⚙️ application.yml

```yaml
server:
  port: ${PORT:8762}
        
eureka:
  instance:
    preferIpAddress: true
    hostname: ${HOSTNAME:localhost}
  client:
    registerWithEureka: true
    fetchRegistry: true
    serviceUrl:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka}

spring:
  application:
    name: gateway
  cloud:
    gateway:
      server:
        webflux:
          discovery:
            locator:
              enabled: true
              lower-case-service-id: true
          default-filters:
            - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin
          globalcors:
            cors-configurations:
              '[/**]':
                allowedOrigins: ${ALLOWED_ORIGINS:*}
                allowedHeaders: "*"
                allowedMethods:
                  - POST

management:
  endpoint:
    gateway:
      access: ${ROUTE_TABLES_ENABLED:read_only}
  endpoints:
    web:
      exposure:
        include:
          - '*'
```

---

## 🔗 URLs del Gateway

Con `discovery.locator.enabled: true`, el Gateway crea rutas automáticas:

| Servicio Eureka | URL Gateway |
|-----------------|-------------|
| ms-books-catalogue | `http://localhost:8762/ms-books-catalogue/**` |
| ms-books-payments | `http://localhost:8762/ms-books-payments/**` |

---

## 📝 Tabla de Transcripción - Catalogue

| Operación | URL Gateway | targetMethod | queryParams | body |
|-----------|-------------|--------------|-------------|------|
| Listar libros | POST `/ms-books-catalogue/api/v1/books` | GET | null | null |
| Buscar libros | POST `/ms-books-catalogue/api/v1/books` | GET | `{"author":["x"]}` | null |
| Obtener libro | POST `/ms-books-catalogue/api/v1/books/1` | GET | null | null |
| Crear libro | POST `/ms-books-catalogue/api/v1/books` | POST | null | BookDTO |
| Actualizar | POST `/ms-books-catalogue/api/v1/books/1` | PUT | null | BookDTO |
| Patch | POST `/ms-books-catalogue/api/v1/books/1` | PATCH | null | campos |
| Eliminar | POST `/ms-books-catalogue/api/v1/books/1` | DELETE | null | null |
| Disponibilidad | POST `/ms-books-catalogue/api/v1/books/1/availability` | GET | null | null |
| Stock | POST `/ms-books-catalogue/api/v1/books/1/stock` | PATCH | null | `{"quantity":-1}` |

---

## 📝 Tabla de Transcripción - Payments

| Operación | URL Gateway | targetMethod | queryParams | body |
|-----------|-------------|--------------|-------------|------|
| Listar | POST `/ms-books-payments/api/v1/payments` | GET | null | null |
| Filtrar | POST `/ms-books-payments/api/v1/payments` | GET | `{"userId":["1"]}` | null |
| Obtener | POST `/ms-books-payments/api/v1/payments/1` | GET | null | null |
| Crear compra | POST `/ms-books-payments/api/v1/payments` | POST | null | PaymentDTO |
| Actualizar | POST `/ms-books-payments/api/v1/payments/1` | PATCH | null | `{"status":"..."}` |
| Cancelar | POST `/ms-books-payments/api/v1/payments/1` | DELETE | null | null |
| Por usuario | POST `/ms-books-payments/api/v1/users/1/payments` | GET | null | null |

---

## 🧪 Ejemplos de Uso con Postman

### Ejemplo 1: Listar todos los libros

**POST** `http://localhost:8762/ms-books-catalogue/api/v1/books`

**Headers:** `Content-Type: application/json`

```json
{
  "targetMethod": "GET",
  "queryParams": null,
  "body": null
}
```

---

### Ejemplo 2: Buscar libros con filtros

**POST** `http://localhost:8762/ms-books-catalogue/api/v1/books`

```json
{
  "targetMethod": "GET",
  "queryParams": {
    "author": ["cervantes"],
    "visible": ["true"],
    "ratingMin": ["4"]
  },
  "body": null
}
```

---

### Ejemplo 3: Crear un libro

**POST** `http://localhost:8762/ms-books-catalogue/api/v1/books`

```json
{
  "targetMethod": "POST",
  "queryParams": null,
  "body": {
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
}
```

---

### Ejemplo 4: Actualizar libro (PUT)

**POST** `http://localhost:8762/ms-books-catalogue/api/v1/books/1`

```json
{
  "targetMethod": "PUT",
  "queryParams": null,
  "body": {
    "title": "Don Quijote de la Mancha",
    "author": "Miguel de Cervantes Saavedra",
    "publicationDate": "1605-01-16",
    "category": "Clásicos",
    "isbn": "9788467033601",
    "rating": 5,
    "visible": true,
    "stock": 150,
    "price": 24.99
  }
}
```

---

### Ejemplo 5: Actualizar parcial (PATCH)

**POST** `http://localhost:8762/ms-books-catalogue/api/v1/books/1`

```json
{
  "targetMethod": "PATCH",
  "queryParams": null,
  "body": {
    "price": 29.99
  }
}
```

---

### Ejemplo 6: Eliminar libro

**POST** `http://localhost:8762/ms-books-catalogue/api/v1/books/1`

```json
{
  "targetMethod": "DELETE",
  "queryParams": null,
  "body": null
}
```

---

### Ejemplo 7: Registrar compra (comunicación entre microservicios)

**POST** `http://localhost:8762/ms-books-payments/api/v1/payments`

```json
{
  "targetMethod": "POST",
  "queryParams": null,
  "body": {
    "userId": 1,
    "bookId": 1,
    "quantity": 2
  }
}
```

**Flujo interno:**
1. Gateway → ms-books-payments (POST)
2. ms-books-payments → ms-books-catalogue (GET /availability)
3. ms-books-payments → ms-books-catalogue (PATCH /stock)
4. Retorna PaymentResponseDTO

---

### Ejemplo 8: Compras de un usuario

**POST** `http://localhost:8762/ms-books-payments/api/v1/users/1/payments`

```json
{
  "targetMethod": "GET",
  "queryParams": null,
  "body": null
}
```

---

## 📊 Endpoints de Monitoreo

| Endpoint | Descripción |
|----------|-------------|
| `GET /actuator/health` | Estado del gateway |
| `GET /actuator/gateway/routes` | Rutas registradas |

---

## 🎯 Checklist (Criterio 7: 2 puntos)

- [x] Gateway en puerto 8762
- [x] Registrado en Eureka
- [x] Discovery locator habilitado
- [x] RequestTranslationFilter
- [x] Decorators (GET, POST, PUT, PATCH, DELETE)
- [x] Transcripción POST funcionando
- [x] Query params soportados
- [x] Body soportado
- [x] Actuator habilitado

---

## 📹 Demo para Videomemoria

1. Mostrar Eureka con gateway registrado
2. `GET http://localhost:8762/actuator/gateway/routes`
3. Petición de búsqueda de libros
4. **Petición de compra** (muestra comunicación entre microservicios)
5. Mostrar respuesta exitosa

---

## ⚠️ Notas Importantes

1. **Todas las peticiones al Gateway son POST** con `Content-Type: application/json`
2. `targetMethod` determina el método HTTP real
3. `queryParams` usa formato array: `{"key": ["value"]}`
4. Sin `Content-Type` o sin POST → retorna 400 Bad Request
5. Los microservicios deben estar en Eureka con nombres en minúsculas
