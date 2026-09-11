# catalogo-ms

| Campo | Valor |
|---|---|
| Responsable | Anghelo |
| Tipo | No transaccional |
| Rol protegido | `ADMIN` (gestión global del catálogo) |

## Descripción

Administra prendas, categorías y variantes de talla/color. Es la fuente de verdad del catálogo que consultan tanto el cliente (para armar su selección) como `resenas-ms` (para validar que el producto reseñado existe).

## Comunicación

- **Síncrona (entrante):** `resenas-ms` → `catalogo-ms` (REST/Feign), valida el producto reseñado.

## Endpoints

<!-- TODO: completar cuando el servicio esté implementado -->

## Entidades

<!-- TODO: completar cuando el servicio esté implementado -->
