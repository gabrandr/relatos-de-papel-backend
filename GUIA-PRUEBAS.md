# Guía de Prueba - MS Books Catalogue

Guía rápida para probar el microservicio MS Books Catalogue con Gateway y Eureka en local.

---

## 📋 Requisitos Previos

- Java 17 o superior
- Maven instalado
- Postman instalado

---

## 🚀 Pasos para Ejecutar

### 1. Iniciar Eureka Server (Puerto 8761)

```bash
cd eureka-server
./mvnw spring-boot:run
```

**Espera a ver:** `Started EurekaServerApplication`

**Verificar:** http://localhost:8761 (debe mostrar la consola de Eureka)

---

### 2. Iniciar Gateway (Puerto 8762)

```bash
cd gateway
./mvnw spring-boot:run
```

**Espera a ver:** `Started GatewayAndFiltersApplication`

**Verificar en Eureka:** GATEWAY debe aparecer como UP en http://localhost:8761

---

### 3. Iniciar MS Books Catalogue (Puerto 8081)

```bash
cd ms-books-catalogue
./mvnw spring-boot:run
```

**Espera a ver:** `Started MsBooksCatalogueApplication`

**Verificar en Eureka:** MS-BOOKS-CATALOGUE debe aparecer como UP en http://localhost:8761

---

## 📦 Importar Colección Postman

1. Abrir Postman
2. Click en **Import**
3. Seleccionar el archivo `MS-Books-Catalogue-Postman.json` del proyecto
4. Confirmar importación

**Carpetas que verás:**

- 1. CRUD Operations (6 requests)
- 2. Advanced Operations (7 requests)
- 3. Validation Tests (4 requests)

---

## 🧪 Pruebas Básicas

### Test 1: Ver todos los libros

1. En Postman, abrir: **1. CRUD Operations → GET All Books**
2. Click **Send**
3. **Resultado esperado:** 200 OK con 8 libros

### Test 2: Buscar libros por categoría

1. Abrir: **2. Advanced Operations → SEARCH - By Category**
2. Click **Send**
3. **Resultado esperado:** 200 OK con libros de categoría "Clásicos"

### Test 3: Crear un libro

1. Abrir: **1. CRUD Operations → POST Create Book**
2. Click **Send**
3. **Resultado esperado:** 201 Created con datos del libro creado

### Test 4: Validación de error

1. Abrir: **3. Validation Tests → POST - Invalid (Empty Title)**
2. Click **Send**
3. **Resultado esperado:** 400 Bad Request con mensaje de error

---

## ⚠️ Importante: Formato de Peticiones

**El Gateway SOLO acepta peticiones POST** con este formato:

```json
{
  "targetMethod": "GET|POST|PUT|PATCH|DELETE",
  "queryParams": {
    "param": ["valor"]
  },
  "body": { ... } // o null
}
```

**Todos los requests de la colección ya están en este formato.**

---

## 🔍 Verificación de Estado

### Verificar en Eureka (http://localhost:8761)

Debes ver 3 aplicaciones registradas:

- ✅ EUREKA-SERVER (8761)
- ✅ GATEWAY (8762)
- ✅ MS-BOOKS-CATALOGUE (8081)

### Verificar Base de Datos H2

URL: http://localhost:8081/h2-console

- **JDBC URL:** `jdbc:h2:mem:catalogue_db`
- **User:** sa
- **Password:** (vacío)

**Query de prueba:**

```sql
SELECT * FROM books;
```

Debe retornar 8 libros.

---

## 🛠️ Solución de Problemas

### Error 400 Bad Request

**Causa:** Petición no tiene formato GatewayRequest  
**Solución:** Usar la colección de Postman actualizada

### Error 404 Not Found

**Causa:** Gateway no encuentra el microservicio  
**Solución:** Verificar que MS-BOOKS-CATALOGUE esté UP en Eureka

### Error "Connection refused"

**Causa:** Un servicio no está corriendo  
**Solución:** Verificar orden de inicio (Eureka → Gateway → MS Catalogue)

### Gateway no aparece en Eureka

**Causa:** Gateway inició antes que Eureka  
**Solución:** Reiniciar Gateway después de que Eureka esté UP

---

## 📊 Datos de Prueba

La base de datos H2 se carga automáticamente con 8 libros:

1. Don Quijote de la Mancha (Clásicos)
2. Cien Años de Soledad (Clásicos)
3. 1984 (Ficción)
4. Orgullo y Prejuicio (Romance)
5. El Señor de los Anillos (Fantasía)
6. Dune (Ciencia Ficción)
7. Moby Dick (Clásicos, NO visible)
8. El Principito (Ficción)

---

## ✅ Checklist de Validación

- [ ] Eureka corriendo en 8761
- [ ] Gateway registrado en Eureka
- [ ] MS Books Catalogue registrado en Eureka
- [ ] Colección Postman importada
- [ ] TEST: GET All Books → 8 libros
- [ ] TEST: SEARCH by Category → Resultados filtrados
- [ ] TEST: POST Create Book → 201 Created
- [ ] TEST: Validación error → 400 Bad Request

---

## 📧 Soporte

Si tienes problemas, revisa:

1. Logs de cada aplicación en la consola
2. Estado en Eureka (http://localhost:8761)
3. Formato de peticiones en Postman

**¡Listo para probar!** 🚀
