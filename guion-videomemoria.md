# 🎬 Guion de Videomemoria - Relatos de Papel Backend (15 mins máx)

> **Instrucciones:** Este guion está diseñado para 5 personas. Asegúrense de tener el entorno limpio (sin servicios corriendo) antes de empezar.

---

## 🙋‍♂️ Persona 1: Introducción y Microservicio Catálogo (3 min)

**[Acción Visual]:** Mostrar el diagrama de arquitectura (si lo tienen) o el IDE abierto en `ms-books-catalogue`.

**Texto:**
"¡Hola a todos! Somos el equipo [Nombre del Equipo] y vamos a presentar el backend desarrollado para la librería 'Relatos de Papel'.

Para este proyecto hemos utilizado **Java 21** (o 25) y **Spring Boot 3.4.1**. Hemos implementado una arquitectura de microservicios completa que cumple con todos los requisitos de la práctica.

Nuestra solución se compone de dos dominios principales. Yo empezaré explicando el **Microservicio Buscador (`ms-books-catalogue`)**. Este servicio se encarga de gestionar todo el inventario de libros.

**[Acción Visual]:** Abrir `BookController.java` y `BookService.java`.

Como pueden ver en el código, hemos seguido rigurosamente las recomendaciones REST.

1.  **API REST:** Tenemos endpoints bien definidos para crear, editar, eliminar y consultar libros. Usamos `ResponseEntity` para manejar los códigos HTTP correctamente.
2.  **Búsqueda Avanzada:** Para el requisito de búsqueda por múltiples criterios (título, autor, ISBN, precio, etc.), hemos implementado **JPA Specifications**. Esto nos permite filtrar por uno o varios campos a la vez de forma dinámica, algo crucial para el frontend.
3.  **Persistencia:** Este servicio cuenta con su propia base de datos H2 en memoria, llamada `catalogue_db`, garantizando el aislamiento de datos."

---

## 🙋‍♀️ Persona 2: Microservicio Operador y Comunicación (3 min)

**[Acción Visual]:** Cambiar a `ms-books-payments` en el IDE. Mostrar `PaymentController.java` y `BookCatalogueClient.java`.

**Texto:**
"Continuando con la lógica de negocio, yo les hablaré del **Microservicio Operador (`ms-books-payments`)**.

Este servicio es el responsable de registrar las compras. Lo más importante aquí es su independencia: tiene su propia base de datos (`payments_db`), separada de la del catálogo.

**[Acción Visual]:** Mostrar el método de creación de compra en `PaymentServiceImpl`.

El desafío técnico aquí es la **comunicación entre microservicios**. Cuando un usuario quiere comprar un libro, no podemos 'confiar ciegamente'.
Hemos implementado un cliente HTTP (`BookCatalogueClient`) que se comunica con el Catálogo para:

1.  Verificar que el libro existe (`checkAvailability`).
2.  Validar que tenga stock suficiente.
3.  Si la compra procede, ordenamos al catálogo descontar el stock.

Todo esto ocurre de forma síncrona y transaccional. Si el catálogo dice 'no hay stock', el pago no se crea."

---

## 🙋‍♂️ Persona 3: Infraestructura (Eureka y Gateway) (3 min)

**[Acción Visual]:** Mostrar `application.yml` de Gateway y luego la clase `RequestTranslationFilter.java`.

**Texto:**
"Para que todo esto funcione como un sistema distribuido, hemos implementado dos piezas clave de infraestructura: **Service Discovery** y **API Gateway**.

Primero, usamos un servidor **Eureka**.
**[Punto Clave]:** Nuestros microservicios NO usan IPs fijas para hablarse entre sí. Usan sus nombres lógicos (`MS-BOOKS-CATALOGUE`). Eureka resuelve dinámicamente dónde están, lo que nos da escalabilidad real.

Segundo, y para cumplir con la máxima valoración técnica, tenemos el **Spring Cloud Gateway**.
Este es el 'portero' de nuestra aplicación. Corre en el puerto **8762**.
Lo más destacado es nuestra implementación de **Tunneling**. Como pueden ver en esta clase `RequestTranslationFilter`, interceptamos las peticiones POST que vienen del frontal. El Gateway lee el cuerpo del mensaje, extrae el método real (GET, PUT, DELETE) y redirige la petición al microservicio correspondiente. Esto añade una capa extra de seguridad y abstracción."

---

## 🙋‍♀️ Persona 4: Despliegue en Vivo (3 min)

**[Acción Visual]:** Tener todas las terminales limpias. Empezar a ejecutar los comandos uno por uno.

**Texto:**
"Ahora, ¡vamos a verlo en acción! Partimos de un entorno limpio, sin nada corriendo.

1.  **Paso 1:** Levanto el **Eureka Server**. (Esperar a que arranque).
2.  **Paso 2:** Levanto nuestro **API Gateway**.
3.  **Paso 3:** Finalmente, levanto los microservicios: **Catalogue** y **Payments**.

**[Acción Visual]:** Abrir el navegador en `http://localhost:8761`.

Si recargo el Dashboard de Eureka... ¡Aquí están! Vemos `MS-BOOKS-CATALOGUE` y `MS-BOOKS-PAYMENTS` registrados correctamente. Esto confirma que el ecosistema está conectado y listo para recibir peticiones."

---

## 🙋‍♂️ Persona 5: Prueba de Funcionalidad y Conclusión (3 min)

**[Acción Visual]:** Abrir Postman. Cargar la colección `MS-Books-Payments`.

**Texto:**
"Para cerrar, demostraremos el flujo completo de una compra ('Happy Path') usando el Gateway.

**[Acción Visual]:** Seleccionar la request 'POST Create Payment'.

Voy a simular ser un cliente. Envío esta petición POST al Gateway (puerto 8762).
Fíjense en el body: estamos usando la estructura de **Tunneling** que explicó mi compañero.

```json
{
  "targetMethod": "POST",
  "body": { "userId": 1, "bookId": 1, "quantity": 1 }
}
```

Enviamos...

**[Acción Visual]:** Pulsar Send. Mostrar el status 201 Created.

¡Éxito! Hemos recibido un **201 Created**.
¿Qué acaba de pasar?

1.  El Gateway recibió el POST, lo 'desempaquetó' y lo mandó a Payments.
2.  Payments consultó a Catalogue (vía Eureka).
3.  Catalogue confirmó stock y lo descontó.
4.  Payments guardó la compra.

Con esto demostramos que todos los componentes (Gateway, Eureka, dos bases de datos y dos microservicios) funcionan en perfecta armonía. ¡Muchas gracias!"
