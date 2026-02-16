# Cheatsheet: Eureka + Spring Cloud Gateway (Guia Operativa)

> Nivel: Intermedio
> Objetivo: Montar microservicios con service discovery y gateway como punto unico de entrada
> Uso: Sigue este documento en orden para configurar todo desde cero
> Basado en: `eureka-server`, `gateway`, `ms-books-catalogue`, `ms-books-payments`

---

## Indice - Orden de Creacion

| Paso | Seccion | Que creas |
| --- | --- | --- |
| 1 | Inicializacion de modulos | Proyectos en Spring Initializr |
| 2 | Eureka Server | Registro de servicios |
| 3 | Microservicios cliente | Registro en Eureka |
| 4 | API Gateway | Enrutamiento centralizado |
| 5 | Request Translation | Filtro global POST -> metodo real |
| 6 | Comunicacion inter-servicios | `RestTemplate` con `@LoadBalanced` |
| 7 | Ejecucion y validacion | Orden de arranque y smoke tests |
| 8 | Troubleshooting y checklist | Correccion rapida de errores comunes |

---

## Flujo de arquitectura

```text
Cliente
  |
  v
Gateway (:8762) ---------------------> Eureka (:8761)
  |                                       ^
  | route lb://MS-BOOKS-CATALOGUE         |
  v                                        |
MS-BOOKS-CATALOGUE (:8081) ----------------
  ^
  | llamada interna por nombre de servicio (RestTemplate @LoadBalanced)
  |
MS-BOOKS-PAYMENTS (:8082)
```

Flujo real en tu proyecto:
1. Cliente pega a `gateway`.
2. Gateway enruta por `Path` hacia `lb://MS-...`.
3. Resolver `lb://` depende de registro activo en Eureka.
4. `payments` llama a `catalogue` por nombre `http://MS-BOOKS-CATALOGUE`.

---

## 1. Inicializacion de Proyectos (Spring Initializr)

### 1.1 Parametros base (todos los modulos)

| Campo | Valor recomendado |
| --- | --- |
| Project | Maven |
| Language | Java |
| Packaging | Jar |
| Java | 21+ (en tu repo: 25) |
| Group | `com.tuempresa` |
| Artifact | segun modulo |

### 1.2 Dependencias para cada modulo

#### A) `eureka-server`

Selecciona en Initializr:
- `Eureka Server`
- `Spring Boot Actuator` (recomendado)
- `Lombok` (opcional)
- `Spring Boot DevTools` (opcional)

#### B) `gateway`

Selecciona en Initializr:
- `Gateway`
- `Eureka Discovery Client`
- `Spring Boot Actuator`
- `Lombok` (opcional)
- `Spring Boot DevTools` (opcional)

Agrega manualmente si no viene:
- `spring-cloud-starter-loadbalancer`
- Si quieres exactamente tu stack: `spring-cloud-starter-gateway-server-webflux` (v4.3.0 en tu repo)

#### C) Microservicios (`ms-books-catalogue`, `ms-books-payments`)

Selecciona en Initializr:
- `Spring Web`
- `Spring Data JPA`
- `Validation`
- `H2 Database`
- `Eureka Discovery Client`
- `Spring Boot Actuator`
- `Lombok`

Adicionales de tu repo:
- `spring-boot-h2console`
- `httpclient5` (en `ms-books-payments`, para `PATCH` via `RestTemplate`)

### 1.3 BOM de Spring Cloud

Usa `dependencyManagement` de Spring Cloud en todos los modulos cloud:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

En tu repo:
- `spring-cloud.version = 2025.1.0`
- `spring-boot-starter-parent = 4.0.2`

---

## 2. Eureka Server

### 2.1 Main class

Archivo real: `EurekaServerApplication.java`

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

### 2.2 `application.yaml`

```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

Regla clave:
- El propio servidor Eureka no debe registrarse como cliente.

---

## 3. Configurar Microservicios como clientes Eureka

### 3.1 `application.yaml` (patron de catalogue/payments)

```yaml
server:
  port: 8081

spring:
  application:
    name: MS-BOOKS-CATALOGUE

eureka:
  instance:
    preferIpAddress: false
    hostname: localhost
    instance-id: ${spring.application.name}:${server.port}
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka
```

Para `payments`, mismo patron cambiando `name` y `port`.

### 3.2 Convenciones recomendadas

| Elemento | Recomendacion |
| --- | --- |
| `spring.application.name` | Estable y unico por servicio |
| `instance-id` | `${name}:${port}` para identificar instancias |
| `defaultZone` | URL unica del servidor Eureka |

---

## 4. API Gateway

### 4.1 Main class

Archivo real: `GatewayAndFiltersApplication.java`

```java
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayAndFiltersApplication {
    public static void main(String[] args) {
        String profile = System.getenv("PROFILE");
        System.setProperty("spring.profiles.active", profile != null ? profile : "default");
        SpringApplication.run(GatewayAndFiltersApplication.class, args);
    }
}
```

### 4.2 `application.yml` de gateway (rutas reales)

```yaml
server:
  port: ${PORT:8762}

spring:
  application:
    name: gateway
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: ms-books-catalogue
              uri: lb://MS-BOOKS-CATALOGUE
              predicates:
                - Path=/api/books/**
            - id: ms-books-payments
              uri: lb://MS-BOOKS-PAYMENTS
              predicates:
                - Path=/api/payments/**
```

### 4.3 Tabla de rutas

| Ruta de entrada gateway | Destino |
| --- | --- |
| `/api/books/**` | `lb://MS-BOOKS-CATALOGUE` |
| `/api/payments/**` | `lb://MS-BOOKS-PAYMENTS` |

---

## 5. Request Translation (patron de tu proyecto)

Tu gateway usa un contrato para aceptar `POST` siempre y convertir internamente al metodo real.

### 5.1 Contrato `GatewayRequest`

```json
{
  "targetMethod": "GET|POST|PUT|PATCH|DELETE",
  "queryParams": { "clave": ["valor1", "valor2"] },
  "body": {}
}
```

### 5.2 Filtro global

Archivo real: `RequestTranslationFilter.java`

Comportamiento:
1. Si no es `POST` con `Content-Type`, responde `400`.
2. Lee body y lo parsea a `GatewayRequest`.
3. Usa `RequestDecoratorFactory` para mutar metodo/uri/body.
4. Continua cadena del gateway con request mutado.

### 5.3 Decorators por metodo

Archivos reales:
- `GetRequestDecorator.java`
- `PostRequestDecorator.java`
- `PutRequestDecorator.java`
- `PatchRequestDecorator.java`
- `DeleteRequestDecorator.java`

Idea:
- Mantener encapsulado como construir request segun metodo objetivo.

### 5.4 Opcion simple vs opcion avanzada (gateway)

| Opcion | Caracteristicas | Cuando usar |
| --- | --- | --- |
| Opcion 1: Gateway REST normal | Cliente usa verbos HTTP reales | APIs publicas normales |
| Opcion 2: Request translation (tu caso) | Cliente envia `POST` + `targetMethod` | Requisitos academicos o compatibilidad especial |

---

## 6. Comunicacion Inter-Servicios (`payments` -> `catalogue`)

### 6.1 `RestTemplate` con balanceo

Archivo real: `RestTemplateConfig.java`

```java
@Configuration
public class RestTemplateConfig {
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        return restTemplate;
    }
}
```

### 6.2 Cliente HTTP por nombre de servicio

Archivo real: `BookCatalogueClient.java`

```java
private static final String CATALOGUE_SERVICE_URL = "http://MS-BOOKS-CATALOGUE";
```

Ejemplos reales:
- `GET /api/books/{id}/availability`
- `PATCH /api/books/{id}/stock` (decrementa/restaura stock)

### 6.3 Regla importante
- Nunca hardcodear IP/puerto de un servicio si usas Eureka.
- Usa siempre nombre logico (`MS-BOOKS-CATALOGUE`).

---

## 7. Configuracion Final y Ejecucion

### 7.1 Orden correcto de arranque

1. `eureka-server`
2. `gateway`
3. `ms-books-catalogue`
4. `ms-books-payments`

### 7.2 Comandos ejemplo

```bash
cd eureka-server && ./mvnw spring-boot:run
cd gateway && ./mvnw spring-boot:run
cd ms-books-catalogue && ./mvnw spring-boot:run
cd ms-books-payments && ./mvnw spring-boot:run
```

### 7.3 Verificaciones minimas

| Verificacion | URL esperada |
| --- | --- |
| Eureka UI | `http://localhost:8761` |
| Health gateway | `http://localhost:8762/actuator/health` |
| Rutas gateway | `http://localhost:8762/actuator/gateway/routes` |
| Servicios registrados | `MS-BOOKS-CATALOGUE`, `MS-BOOKS-PAYMENTS` visibles en Eureka |

---

## 8. Smoke Tests via Gateway (Request Translation)

### 8.1 Crear libro

```bash
curl -X POST "http://localhost:8762/api/books" \
  -H "Content-Type: application/json" \
  -d '{
    "targetMethod":"POST",
    "queryParams":{},
    "body":{
      "title":"Libro Gateway",
      "author":"Autor",
      "publicationDate":"2024-01-01",
      "category":"Demo",
      "isbn":"GW-001",
      "rating":5,
      "visible":true,
      "stock":10,
      "price":30.00
    }
  }'
```

### 8.2 Buscar libros (GET interno)

```bash
curl -X POST "http://localhost:8762/api/books/search" \
  -H "Content-Type: application/json" \
  -d '{
    "targetMethod":"GET",
    "queryParams":{"visible":["true"],"minPrice":["20"]},
    "body":null
  }'
```

### 8.3 Crear pago

```bash
curl -X POST "http://localhost:8762/api/payments" \
  -H "Content-Type: application/json" \
  -d '{
    "targetMethod":"POST",
    "queryParams":{},
    "body":{"userId":1,"bookId":1,"quantity":2}
  }'
```

---

## Troubleshooting

| Problema | Causa probable | Solucion |
| --- | --- | --- |
| Servicio no aparece en Eureka | `spring.application.name`/`defaultZone` mal | Revisar config del microservicio |
| `UnknownHostException: MS-...` | Falta `@LoadBalanced` | Marcar bean de `RestTemplate` |
| Gateway responde `400` | Request no cumple formato translation | Enviar `POST` JSON con `targetMethod` |
| `404` al enrutar | Ruta `Path` o `uri: lb://` mal | Corregir `application.yml` del gateway |
| `PATCH` entre servicios falla | Falta `httpclient5` o request factory | Agregar dependencia y `HttpComponentsClientHttpRequestFactory` |
| `payments` no descuenta stock | Fallo en cliente `BookCatalogueClient` | Verificar endpoint `/stock` y manejo de excepciones |

---

## Buenas Practicas y Anti-Patrones

### Haz esto
- Mantener `spring.application.name` estable.
- Exponer Actuator para diagnostico.
- Encapsular llamadas internas en clientes dedicados.
- Documentar contrato de `GatewayRequest`.
- Probar flujo completo de compra (catalogue + payments) por gateway.

### Evita esto
- Saltarte Eureka usando URLs fijas entre microservicios.
- Mezclar logica de negocio pesada dentro del filtro de gateway.
- Cambiar nombres de servicios sin actualizar rutas `lb://`.
- No validar estructura del body en request translation.

---

## Checklist para Nuevo Proyecto con Eureka + Gateway

### Inicializacion
- [ ] Crear modulo `eureka-server` con dependencia correcta.
- [ ] Crear modulo `gateway` con Gateway + Eureka Client.
- [ ] Crear microservicios con Eureka Discovery Client.

### Configuracion
- [ ] Configurar `defaultZone` en todos los clientes.
- [ ] Definir nombres de servicio consistentes.
- [ ] Definir rutas gateway por prefijo.

### Ejecucion
- [ ] Arrancar en orden correcto.
- [ ] Verificar registro en Eureka.
- [ ] Verificar rutas de gateway en actuator.

### Flujo funcional
- [ ] Consumir endpoints via gateway.
- [ ] Validar comunicacion interna por nombre de servicio.
- [ ] Validar errores comunes (400/404/500) con mensajes claros.

---

## Referencias del Proyecto Base

- `eureka-server/src/main/java/com/relatosdepapel/eureka_server/EurekaServerApplication.java`
- `eureka-server/src/main/resources/application.yaml`
- `gateway/src/main/java/com/unir/gateway/GatewayAndFiltersApplication.java`
- `gateway/src/main/resources/application.yml`
- `gateway/src/main/java/com/unir/gateway/filter/RequestTranslationFilter.java`
- `gateway/src/main/java/com/unir/gateway/model/GatewayRequest.java`
- `gateway/src/main/java/com/unir/gateway/decorator/RequestDecoratorFactory.java`
- `gateway/src/main/java/com/unir/gateway/decorator/GetRequestDecorator.java`
- `gateway/src/main/java/com/unir/gateway/decorator/PostRequestDecorator.java`
- `gateway/src/main/java/com/unir/gateway/decorator/PutRequestDecorator.java`
- `gateway/src/main/java/com/unir/gateway/decorator/PatchRequestDecorator.java`
- `gateway/src/main/java/com/unir/gateway/decorator/DeleteRequestDecorator.java`
- `ms-books-payments/src/main/java/com/relatosdepapel/ms_books_payments/config/RestTemplateConfig.java`
- `ms-books-payments/src/main/java/com/relatosdepapel/ms_books_payments/client/BookCatalogueClient.java`
- `ms-books-catalogue/src/main/resources/application.yaml`
- `ms-books-payments/src/main/resources/application.yaml`

---

## Recursos adicionales

- [Spring Cloud Gateway](https://docs.spring.io/spring-cloud-gateway/reference/)
- [Spring Cloud Netflix Eureka](https://docs.spring.io/spring-cloud-netflix/reference/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/)
