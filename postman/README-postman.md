# Postman QA - Relatos de Papel (Gateway CRUD)

## Archivos
- `postman/relatos-gateway-crud.postman_collection.json`
- `postman/relatos-gateway-local.postman_environment.json`

## Importar en Postman
1. Abrir Postman.
2. `Import` -> seleccionar la colección `postman/relatos-gateway-crud.postman_collection.json`.
3. `Import` -> seleccionar el environment `postman/relatos-gateway-local.postman_environment.json`.
4. Activar el environment **Relatos Gateway Local**.

## Ejecutar en Collection Runner
1. Abrir la colección **Relatos Gateway CRUD**.
2. Clic en `Run collection`.
3. Seleccionar el environment **Relatos Gateway Local**.
4. Ejecutar en orden completo:
   - Carpeta `Books CRUD` (pasos 1 a 7).
   - Carpeta `Payments` (paso 8 opcional).

## Ejecutar por CLI con Newman
Requisitos:
- Node.js instalado.
- Newman instalado global o local.

Instalación rápida:
```bash
npm install -g newman newman-reporter-htmlextra
```

Ejecución básica:
```bash
newman run postman/relatos-gateway-crud.postman_collection.json \
  -e postman/relatos-gateway-local.postman_environment.json
```

Ejecución con reportes JSON + HTML:
```bash
mkdir -p postman/reports
newman run postman/relatos-gateway-crud.postman_collection.json \
  -e postman/relatos-gateway-local.postman_environment.json \
  -r cli,json,htmlextra \
  --reporter-json-export postman/reports/newman-report.json \
  --reporter-htmlextra-export postman/reports/newman-report.html
```

## Interpretación de resultados pass/fail
- **Pass**: todas las aserciones `pm.test(...)` del request pasan.
- **Fail**: al menos una aserción falla (status, content-type o validación de campos).
- El flujo CRUD valida:
  - listado inicial,
  - create + persistencia de `book_id`,
  - get por id,
  - patch,
  - get actualizado,
  - delete,
  - verificación post-delete tolerante (`404`, `204` o `200` con body vacío/sin recurso).

## Ajustes aplicados según el repo
- Se usaron rutas reales detectadas en el proyecto:
  - `POST {{gateway_url}}/api/books` con `targetMethod` para `GET/POST/PATCH/DELETE`.
  - `POST {{gateway_url}}/api/payments` con `targetMethod: POST`.
- El paso de pagos opcional usa `bookId=1` (dato semilla de `ms-books-catalogue/src/main/resources/data.sql`).
