# resenas-ms

| Campo | Valor |
|---|---|
| Responsable | Yeins |
| Tipo | No transaccional |
| Rol protegido | `CLIENTE` (gestiona sus propias reseñas) |

## Descripción

Administra reseñas y calificaciones de productos. Antes de aceptar una reseña, valida contra `catalogo-ms` que el producto exista y contra `cliente-ms` que el cliente efectivamente haya comprado ese producto.

## Comunicación

- **Síncrona (saliente):** `resenas-ms` → `catalogo-ms` (REST/Feign), valida el producto reseñado; `resenas-ms` → `cliente-ms` (REST/Feign), valida que el cliente compró el producto.

## Endpoints

<!-- TODO: completar cuando el servicio esté implementado -->

## Entidades

<!-- TODO: completar cuando el servicio esté implementado -->
