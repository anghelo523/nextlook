# catalogo-ms

| Campo | Valor |
|---|---|
| Responsable | Anghelo |
| Tipo | No transaccional |
| Rol protegido | `ADMIN` (gestión global del catálogo) |

## Descripción

Administra prendas, categorías y variantes de talla/color. Es la fuente de verdad del catálogo que consultan tanto el cliente (para armar su selección) como `resenas-ms` (para validar que el producto reseñado existe).

## Comunicación

- **Síncrona (entrante):** `orden-ms` → `catalogo-ms` (Feign + Circuit Breaker `catalogo`), obtiene precio, talla y color de cada variante; `resenas-ms` → `catalogo-ms`, valida el producto reseñado (pendiente).

## Endpoints

Base: `/api/v1` (vía Gateway `http://localhost:18080`). Lectura pública; escritura solo `ADMIN`.

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| GET | `/categorias` | público | Lista categorías (Polos, Pantalones, Casacas, Vestidos, Zapatillas). |
| POST | `/categorias` | `ADMIN` | Crea una categoría (nombre único). |
| GET | `/productos?categoriaId=` | público | Prendas activas con sus variantes de talla/color. |
| GET | `/productos/{id}` | público | Detalle de una prenda. |
| GET | `/productos/variantes/{varianteId}` | público | Variante con precio y nombre de prenda — **la consume `orden-ms` por Feign**. |
| POST | `/productos` | `ADMIN` | Crea prenda con variantes (`sku` único, talla+color único por prenda). |
| PUT | `/productos/{id}` | `ADMIN` | Actualiza datos de la prenda. |
| DELETE | `/productos/{id}` | `ADMIN` | Baja lógica (`activo = false`): deja de venderse. |

## Entidades

| Tabla | Campos clave |
|---|---|
| `categorias` | `id`, `nombre` (único) |
| `prendas` | `id`, `nombre`, `descripcion`, `marca`, `genero` (HOMBRE/MUJER/UNISEX), `precio`, `activo`, `id_categoria` |
| `variantes` | `id`, `id_prenda`, `talla`, `color`, `sku` (único), `activo` — UNIQUE(`id_prenda`,`talla`,`color`) |

Datos semilla (`V2__seed_catalogo.sql`): 5 prendas y 16 variantes, con los mismos `id` que usa `inventario-ms` para su stock.
