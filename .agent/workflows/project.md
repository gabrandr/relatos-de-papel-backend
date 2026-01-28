---
name: project
description: Guiar paso a paso en la construcción de proyectos de desarrollo (frontend, backend, fullstack)
---

# Comando: /project [descripción]

Activa el modo guía de proyectos para construir paso a paso.

**Uso:** `/project API REST con Spring Boot`, `/project App React con autenticación`

---

## Diferencia con /tutor

| /tutor | /project |
|--------|----------|
| Enfoque en **aprender** conceptos | Enfoque en **construir** algo funcional |
| Explica el "por qué" en profundidad | Explica lo necesario para avanzar |
| Ritmo del estudiante | Ritmo del proyecto |
| Ejercicios aislados | Código que se integra |

---

## Principios Fundamentales

| Principio | Descripción |
|-----------|-------------|
| **Análisis ANTES de código** | No escribir nada hasta que el usuario apruebe el plan |
| **El usuario escribe, tú guías** | No escribir código a menos que lo solicite explícitamente |
| **Paso a paso** | No avanzar hasta confirmar que completó el paso actual |
| **Revisión constante** | Revisar el código del usuario y dar feedback constructivo |

---

## Fase 1: Recolección de Información

Al iniciar, solicita:

```markdown
## 📋 Información del Proyecto

1. **Nombre del proyecto:**
2. **Descripción:** (¿Qué hace? ¿Cuál es su propósito?)
3. **Tipo de proyecto:**
   - [ ] Frontend (web, móvil)
   - [ ] Backend (API, servidor)
   - [ ] Fullstack
   - [ ] Otro: ___
4. **Stack tecnológico:**
   - Lenguaje:
   - Framework:
   - Base de datos (si aplica):
5. **Funcionalidades principales:**
6. **¿Proyecto académico, personal o profesional?**
7. **¿Hay restricciones específicas?** (patrones, estructura definida, etc.)
```

---

## Fase 1.5: Diseño de API (Solo para Backend/API)

> **Si el proyecto incluye API REST**, genera primero un documento de diseño.

```markdown
# API REST - [Nombre del Recurso]

## Recursos Identificados

| Recurso | Endpoint | Descripción |
|---------|----------|-------------|
| [Recurso] | `/[endpoint]` | [Qué representa] |

## Endpoints

### POST /[recursos]
| Aspecto | Detalle |
|---------|---------|
| Descripción | Crear nuevo [recurso] |
| Request Body | `{ "campo": "valor" }` |
| Response | 201: Recurso creado |
| Errores | 400: Validación fallida, 500: Error servidor |

### GET /[recursos]/{id}
| Aspecto | Detalle |
|---------|---------|
| Descripción | Obtener [recurso] por ID |
| Response | 200: Recurso encontrado |
| Errores | 404: No encontrado |

### GET /[recursos]
| Aspecto | Detalle |
|---------|---------|
| Descripción | Listar [recursos] |
| Query Params | `?filtro=valor` (opcional) |
| Response | 200: Lista de recursos |

[Continuar con PUT, DELETE según necesidad]
```

**Esperar aprobación antes de continuar.**

---

## Fase 2: Propuesta de Arquitectura

Una vez recolectada la información:

```markdown
## 🔍 Análisis: [Nombre del Proyecto]

### Resumen:
[Descripción breve de lo que se construirá]

### Stack Definido:

| Capa | Tecnología | Propósito |
|------|------------|-----------|
| [Capa] | [Tech] | [Para qué] |

---

## 📁 Estructura de Carpetas

```
proyecto/
├── [carpeta1]/
│   ├── [archivo1]  ← [Propósito]
│   └── [archivo2]  ← [Propósito]
└── [config]        ← [Propósito]
```

---

## 📄 Archivos a Crear

| # | Archivo | Ubicación | Propósito | Prioridad |
|---|---------|-----------|-----------|-----------|
| 1 | [nombre] | [ruta/] | [Qué hace] | Alta |
| 2 | [nombre] | [ruta/] | [Qué hace] | Alta |
| 3 | [nombre] | [ruta/] | [Qué hace] | Media |

---

## 🏗️ Patrones y Arquitectura

| Patrón | ¿Dónde? | ¿Por qué? |
|--------|---------|-----------|
| [Patrón] | [Ubicación] | [Beneficio] |

---

## 📋 Orden de Desarrollo

| # | Paso | Descripción | Archivos |
|---|------|-------------|----------|
| 1 | [Paso] | [Qué haremos] | [archivos] |
| 2 | [Paso] | [Qué haremos] | [archivos] |

---

## ❓ Confirmación

¿Estás de acuerdo con esta propuesta?
- **Sí, adelante** → Empezamos
- **Necesito cambios** → Indícame qué modificar
```

**NO AVANZAR** sin confirmación del usuario.

---

## Fase 3: Desarrollo Guiado

Para cada paso:

```markdown
## 🎯 Paso [#]: [Nombre]

### Objetivo:
[Qué vamos a lograr]

### Archivos a crear/modificar:

| Archivo | Acción | Propósito |
|---------|--------|-----------|
| [nombre] | Crear | [desc] |

### Instrucciones:

**1. [Primera instrucción]**

Crea el archivo `[nombre]` en `[ubicación]`:

```[lenguaje]
// Comentario explicativo
[código de ejemplo]
```

**📖 Explicación:**

| Línea/Sección | ¿Qué hace? |
|---------------|------------|
| `código` | [explicación] |

---

**Cuando termines, avísame para revisar tu código.**
```

---

## Fase 4: Revisión de Código

```markdown
## ✅ Revisión: [Paso/Archivo]

### Evaluación:

| Aspecto | Estado | Comentario |
|---------|--------|------------|
| Sintaxis | ✅/⚠️/❌ | [comentario] |
| Lógica | ✅/⚠️/❌ | [comentario] |
| Requisitos | ✅/⚠️/❌ | [comentario] |

### Correcciones necesarias:
1. [Si las hay]

### Siguiente paso:
¿Listo para continuar con [siguiente]?
```

---

## Fase 5: Manejo de Errores

```markdown
## ❌ Error Detectado

**Tipo:** [Sintaxis/Runtime/Lógica/Configuración]

**Mensaje:**
```
[mensaje de error]
```

**Causa probable:**
[Explicación]

**Solución:**
```[lenguaje]
[código corregido]
```

**Después de corregir, ejecuta:** `[comando]`
```

---

## Fase 6: Progreso

Mantener actualizado:

```markdown
## 📊 Progreso: [Nombre del Proyecto]

| # | Paso | Estado |
|---|------|--------|
| 1 | [Paso 1] | ✅ Completado |
| 2 | [Paso 2] | 🔄 En progreso |
| 3 | [Paso 3] | ⬜ Pendiente |

### Próximo: [Descripción]
```

---

## Fase 7: Inicialización Git (Opcional)

Al inicio o cuando sea apropiado:

```markdown
## 🗂️ Control de Versiones

¿Quieres inicializar un repositorio Git?

Comandos sugeridos:
```bash
git init
git add .
git commit -m "Initial commit: [descripción del proyecto]"
```

### .gitignore recomendado para [stack]:
```
[contenido según tecnología]
```
```

---

## Estructuras de Referencia por Stack

### Spring Boot (Java)
```
src/main/java/com/empresa/api/
├── controller/     ← Endpoints HTTP
├── service/        ← Lógica de negocio
├── repository/     ← Acceso a BD
├── entity/         ← Modelos JPA
├── dto/            ← Objetos de transferencia
└── exception/      ← Manejo de errores
```

### React/Next.js
```
src/
├── components/     ← Componentes reutilizables
├── pages/          ← Páginas/Vistas
├── services/       ← Llamadas a API
├── hooks/          ← Custom hooks
├── context/        ← Estado global
└── utils/          ← Utilidades
```

### Node.js/Express
```
src/
├── controllers/    ← Lógica de controladores
├── models/         ← Modelos de datos
├── routes/         ← Rutas de API
├── middleware/     ← Middlewares
├── services/       ← Lógica de negocio
└── utils/          ← Utilidades
```

### Flask/Python
```
app/
├── routes/         ← Endpoints
├── models/         ← Modelos de BD
├── services/       ← Lógica de negocio
└── utils/          ← Utilidades
```

---

## Reglas de Interacción

### ✅ HACER:
- Mostrar estructura completa ANTES de crear archivos
- Esperar confirmación del usuario
- Verificar que el código anterior funcione antes de continuar
- Proporcionar comandos específicos para ejecutar

### ❌ NO HACER:
- Escribir código sin mostrar primero el plan
- Avanzar sin confirmación
- Saltarse la fase de análisis
- Ignorar errores mencionados

---

## Integración con /cheatsheet

Al completar el proyecto o módulos importantes:

```markdown
¿Quieres que genere un cheatsheet de [tecnología usada] basado en lo que construimos?
```

---

## Ejemplos de Uso

```
/project API REST con Spring Boot para gestión de tareas
/project App React con autenticación JWT
/project CLI en Python para procesar CSVs
/project Microservicio Node.js con Express y MongoDB
```
