---
name: cheatsheet-master
description: Prompt maestro para generar cheatsheets tecnicos con formato de guia operativa paso a paso y nivel de detalle alto.
---

# Prompt Maestro Para Crear Cheatsheets Tecnicos

## Objetivo
Generar cheatsheets que sirvan como manual de ejecucion: leer, seguir y construir sin improvisar.

## Entradas Minimas
- `tema`: tecnologia, framework o problema a documentar.
- `nivel`: `basico`, `intermedio`, `avanzado` (default: `intermedio`).
- `idioma`: idioma de salida (default: espanol tecnico).
- `contexto`: opcional. Puede ser codigo real de un proyecto o `sin proyecto`.
- `output`: ruta y nombre del archivo final.

## Modos De Uso (Dos Escenarios)
### Escenario A: Con proyecto base
Cuando el usuario diga algo como "en base a este proyecto", el cheatsheet debe:
- usar evidencia real del repo abierto (archivos, clases, config, contratos, comandos),
- extraer patrones reutilizables desde ese codigo,
- mantener trazabilidad a archivos concretos.

### Escenario B: Sin proyecto base
Cuando el usuario pida un tema puntual (ejemplo: `useState` de React) sin repo de referencia, el cheatsheet debe:
- construirse con conocimiento general estable y convenciones estandar,
- priorizar documentacion oficial y patrones ampliamente aceptados,
- declarar supuestos minimos cuando falte contexto especifico.

## Principios De Calidad (Obligatorios)
1. Si hay proyecto, basarse en evidencia real del proyecto (archivos, clases, configs, comandos).
2. Si no hay proyecto, basarse en estandares tecnicos y documentacion oficial.
3. No inventar versiones, endpoints, nombres de clases o dependencias.
4. Si falta un dato, marcar `pendiente de confirmar` o declarar supuestos.
5. Explicar tanto "que hacer" como "por que".
6. Mantener orden de construccion y trazabilidad por pasos.
7. Incluir ejemplos listos para copiar.
8. Incluir alternativas cuando existan (opcion simple vs opcion avanzada).
9. Cerrar con checklist utilizable en proyectos nuevos.

## Estilo Esperado
- Formato didactico y operativo, no solo descriptivo.
- Secciones en orden de implementacion real.
- Tablas de referencia rapida para no depender de memoria.
- Snippets pequenos pero completos.

## Flujo De Generacion
1. Detectar escenario:
   - con proyecto base, o
   - sin proyecto base.
2. Si hay proyecto, leer contexto real y extraer patrones.
3. Si no hay proyecto, definir baseline tecnico con buenas practicas estandar.
4. Definir orden de implementacion (pasos ejecutables).
5. Redactar estructura con secciones obligatorias.
6. Incluir opciones de arquitectura/manejo (cuando aplique).
7. Verificar consistencia: nombres, rutas, codigos, contratos.
8. Validar que el documento sirva como guia de construccion desde cero.

## Estructura Obligatoria Del Cheatsheet
Usar esta estructura en este orden:

```markdown
# Cheatsheet: [TEMA]

> Nivel: [BASICO|INTERMEDIO|AVANZADO]
> Objetivo: [QUE PERMITE CONSTRUIR]
> Uso: Sigue este documento en orden para implementar desde cero
> Basado en: [PROYECTO O MODULO FUENTE]

## Indice - Orden de creacion
(tabla paso | seccion | resultado)

## Flujo conceptual (request/response o runtime)
(diagrama ASCII o mermaid)

## 1. Inicializacion del proyecto
### 1.1 Spring Initializr / scaffolding
### 1.2 Dependencias a seleccionar (por caso)
### 1.3 Dependencias manuales adicionales (si aplica)
### 1.4 Configuracion inicial minima

## 2. Estructura de carpetas y arquitectura
### 2.1 Opcion simple (1 capa)
### 2.2 Opcion recomendada/avanzada (2+ capas)
### 2.3 Cuando elegir cada opcion

## 3. Paso a paso de implementacion
(secciones detalladas por artefacto: main, utils, entity, dto, repo, service, controller, etc.)

## 4. Contratos y tablas de referencia
### 4.1 Matriz de endpoints/metodos (si aplica)
### 4.2 Tabla de anotaciones/clases clave
### 4.3 Tabla de codigos de estado o eventos

## 5. Opciones de diseno (A/B)
### Opcion 1 (simple)
### Opcion 2 (avanzada)
### Comparacion y criterio de eleccion

## 6. Configuracion final y ejecucion
### 6.1 Archivos de config
### 6.2 Orden de arranque
### 6.3 Smoke tests

## 7. Troubleshooting
(problema | causa | solucion)

## 8. Buenas practicas y anti-patrones

## 9. Checklist para nuevo proyecto

## 10. Referencias del proyecto base

## 11. Recursos adicionales
```

## Reglas Especificas Para APIs/Microservicios
Si el tema es backend API o microservicios, agregar obligatoriamente:
- Dependencias de Spring Initializr por modulo (`api`, `eureka`, `gateway`, etc.).
- Tabla completa de endpoints y codigos HTTP.
- Seccion de manejo de errores con 2 opciones:
  - `Opcion 1`: manejo en controller (`ResponseEntity`).
  - `Opcion 2`: `GlobalExceptionHandler` con excepciones personalizadas.
- Estructura de codigo para arquitectura en 2 capas donde aplique.

## Ajuste Por Nivel
- `basico`: menos variantes, mas guia de arranque.
- `intermedio`: equilibrio entre teoria, practica y troubleshooting.
- `avanzado`: resiliencia, observabilidad, seguridad, edge cases.

## Validacion Antes De Entregar
- [ ] Hay orden de creacion por pasos.
- [ ] Hay dependencias iniciales claras (scaffolding/Initializr).
- [ ] Hay estructura de carpetas y criterios de arquitectura.
- [ ] Hay ejemplos coherentes con el contexto real.
- [ ] Hay comparacion de opciones cuando aplica.
- [ ] Hay checklist accionable para repetir en otro proyecto.

## Prompt Listo Para Copiar
```text
Genera un cheatsheet tecnico usando EXACTAMENTE la estructura y reglas de este archivo.

Entradas:
- Tema: [TEMA]
- Nivel: [BASICO|INTERMEDIO|AVANZADO]
- Idioma: [IDIOMA]
- Contexto real: [RUTA DEL PROYECTO / MODULOS]
- Salida: [RUTA_ARCHIVO]

Condiciones:
1) Debe quedar util para construir desde cero, en orden.
2) Debe incluir dependencias iniciales por caso (Initializr/scaffolding).
3) Si es API/microservicios, incluir endpoints, errores opcion 1/2 y arquitectura por capas.
4) Si hay proyecto, usar evidencia del repo; si no hay proyecto, usar estandares y fuentes oficiales.
5) No inventar datos fuera del contexto disponible.
```
