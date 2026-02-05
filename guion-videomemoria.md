# Guion de videomemoria - Relatos de Papel Backend (15 min max)

## Reparto y tiempos aproximados
1. Persona 1 - Introduccion y arquitectura general (2:30)
2. Persona 2 - API REST del buscador (catalogue) (3:00)
3. Persona 3 - API REST del operador (payments) (3:00)
4. Persona 4 - Despliegue y verificacion (3:00)
5. Persona 5 - Prueba end-to-end y conclusiones (3:30)

## Antes de grabar (visible en pantalla)
1. Confirmar entorno limpio, sin servicios en ejecucion.
2. Tener el IDE abierto en el repositorio.
3. Tener a mano los archivos Postman: `MS-Books-Catalogue-Postman.json` y `MS-Books-Payments-Postman.json`.
4. Tener 4 terminales listas para arrancar servicios.

## Persona 1 - Introduccion y arquitectura general (0:00-2:30)

Acciones en pantalla:
1. Mostrar el explorador del proyecto y la estructura de carpetas.
2. Mostrar terminales sin procesos en ejecucion.
3. Mostrar el diagrama de arquitectura o un esquema propio de la arquitectura.

Texto a leer:
"Hola, somos el equipo [Nombre del equipo]. Esta es la videomemoria de la actividad de backend en Spring.

Empezamos con el entorno limpio, sin componentes desplegados. La arquitectura se basa en dos microservicios: el buscador `ms-books-catalogue` y el operador `ms-books-payments`, cada uno con su propia base de datos relacional. Usamos Eureka para registro y descubrimiento, y Spring Cloud Gateway como punto unico de entrada.

En esta videomemoria seguiremos el orden exigido por la rubrica: primero explicamos las APIs REST de ambos microservicios, luego desplegamos Eureka, Gateway y microservicios, verificamos el dashboard y el visor de rutas, y finalmente realizamos una prueba end-to-end con Postman."

## Persona 2 - API REST del buscador (catalogue) (2:30-5:30)

Acciones en pantalla:
1. Abrir `ms-books-catalogue/src/main/java/com/relatosdepapel/ms_books_catalogue/controller/BookController.java`.
2. Abrir `ms-books-catalogue/src/main/java/com/relatosdepapel/ms_books_catalogue/specification/BookSpecification.java`.
3. Abrir `ms-books-catalogue/src/main/java/com/relatosdepapel/ms_books_catalogue/config/OpenApiConfig.java`.

Texto a leer:
"Ahora explico la API REST del microservicio buscador, `ms-books-catalogue`.

En `BookController` se exponen los endpoints REST para crear, modificar completo o parcial, eliminar y consultar libros. Tambien tenemos endpoints de consulta por criterios. El requisito de busqueda por todos los atributos se implementa con JPA Specifications en `BookSpecification`, lo que permite filtros individuales o combinados por titulo, autor, categoria, ISBN, precio, visibilidad y otros campos.

Operaciones expuestas (base `/api/books`):
1. POST `/api/books` para crear un libro.
2. GET `/api/books` para listar solo libros visibles.
3. GET `/api/books/search` para busqueda avanzada por filtros: title, author, category, isbn, ratingMin, ratingMax, visible, minPrice, maxPrice, minStock, publicationDateFrom y publicationDateTo.
4. GET `/api/books/{id}` para obtener un libro por id.
5. PUT `/api/books/{id}` para actualizar completo.
6. PATCH `/api/books/{id}` para actualizar parcial.
7. DELETE `/api/books/{id}` para eliminar.
8. GET `/api/books/{id}/availability` para consultar disponibilidad.
9. PATCH `/api/books/{id}/stock` para actualizar stock.

Este microservicio tiene su propia base de datos, separada del operador.

Ademas, documentamos los endpoints con OpenAPI/Swagger. Esto se ve en `OpenApiConfig` y en la dependencia de Springdoc del proyecto. En la videomemoria solo lo mencionamos; las pruebas se haran con Postman."

## Persona 3 - API REST del operador (payments) (5:30-8:30)

Acciones en pantalla:
1. Abrir `ms-books-payments/src/main/java/com/relatosdepapel/ms_books_payments/controller/PaymentController.java`.
2. Abrir `ms-books-payments/src/main/java/com/relatosdepapel/ms_books_payments/service/PaymentServiceImpl.java`.
3. Abrir `ms-books-payments/src/main/java/com/relatosdepapel/ms_books_payments/client/BookCatalogueClient.java`.
4. Abrir `ms-books-payments/src/main/java/com/relatosdepapel/ms_books_payments/config/OpenApiConfig.java`.

Texto a leer:
"Continuamos con la API REST del microservicio operador, `ms-books-payments`. En `PaymentController` estan los endpoints de gestion de compras: crear compra, consultar pagos, actualizar estado y cancelar.

En `PaymentServiceImpl` se ve el flujo principal: antes de crear una compra, el operador valida que el libro exista y tenga stock. Esa comunicacion se realiza mediante `BookCatalogueClient`, que llama al buscador usando el nombre del servicio registrado en Eureka, sin IPs fijas.

Operaciones expuestas (base `/api/payments`):
1. POST `/api/payments` para crear un pago.
2. GET `/api/payments` para listar pagos.
3. GET `/api/payments/{id}` para consultar un pago por id.
4. GET `/api/payments/search` para buscar por userId, bookId o status.
5. PATCH `/api/payments/{id}` para actualizar estado del pago.
6. DELETE `/api/payments/{id}` para cancelar el pago.

Este microservicio tambien tiene su propia base de datos, independiente del catalogo.

Igual que en el buscador, documentamos los endpoints con OpenAPI/Swagger, visible en `OpenApiConfig`. No haremos pruebas con Swagger; solo lo mencionamos."

## Persona 4 - Despliegue y verificacion (8:30-11:30)

Acciones en pantalla:
1. Arrancar `eureka-server` desde IDE o terminal.
2. Arrancar `gateway` desde IDE o terminal.
3. Arrancar `ms-books-catalogue` y `ms-books-payments`.
4. Abrir el dashboard de Eureka: `http://localhost:8761`.
5. Abrir el visor de rutas de Gateway: `http://localhost:8762/actuator/gateway/routes`.

Texto a leer:
"Ahora desplegamos los componentes en el orden recomendado.

Primero levantamos el servidor Eureka. Luego arrancamos el Gateway. Por ultimo, iniciamos los microservicios catalogue y payments.

Verificamos en el dashboard de Eureka que ambos servicios esten registrados correctamente. Se deben ver `MS-BOOKS-CATALOGUE` y `MS-BOOKS-PAYMENTS`.

Despues, comprobamos el visor de rutas de Cloud Gateway en `/actuator/gateway/routes`, donde aparecen las rutas hacia ambos microservicios. Esto confirma que el Gateway enruta correctamente las peticiones."

## Persona 5 - Prueba end-to-end y conclusiones (11:30-15:00)

Acciones en pantalla:
1. Abrir Postman e importar `MS-Books-Catalogue-Postman.json` y `MS-Books-Payments-Postman.json`.
2. Verificar que la variable `gateway_url` apunta a `http://localhost:8762`.
3. En la coleccion de Payments ejecutar `POST Create Payment (Happy Path)`.
4. Mostrar la respuesta 201 y explicar la comunicacion con Catalogue.
5. Opcional: ejecutar `GET Book by ID` desde la coleccion de Catalogue para evidenciar el cambio de stock.
6. Cerrar con conclusiones.

Texto a leer:
"Para la prueba funcional usamos Postman con las colecciones del proyecto. Importamos `MS-Books-Catalogue-Postman.json` y `MS-Books-Payments-Postman.json`.

Realizamos la llamada `POST Create Payment (Happy Path)` a traves del Gateway. En el body se ve la estructura de tunneling con `targetMethod` y el `body` de la compra. Al enviar, obtenemos `201 Created`.

Este resultado demuestra la comunicacion entre microservicios: Payments consulta a Catalogue para validar disponibilidad y stock, y luego registra la compra.

Si hacemos un `GET Book by ID` desde la coleccion de Catalogue, podemos ver el stock actualizado, confirmando el flujo end-to-end.

Conclusiones: la arquitectura cumple con la rubrica. Tenemos dos microservicios con bases de datos separadas, registro en Eureka, Gateway con rutas y transcripcion de peticiones, endpoints REST documentados con OpenAPI/Swagger y pruebas realizadas mediante Postman. Gracias por su atencion."
