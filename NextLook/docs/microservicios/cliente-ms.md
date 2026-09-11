# cliente-ms

| Campo | Valor |
|---|---|
| Responsable | Daniel |
| Tipo | No transaccional |
| Rol protegido | `CLIENTE` (gestiona su propio perfil y direcciones) |

## Descripción

Perfiles de compradores y direcciones de envío. Es consultado de forma síncrona por `orden-ms` (para validar al cliente antes de registrar una orden), por `envio-ms` (para obtener la dirección de envío) y por `resenas-ms` (para validar que el cliente compró el producto reseñado).

## Comunicación

- **Síncrona (entrante):** `orden-ms` → `cliente-ms`, valida cliente; `envio-ms` → `cliente-ms`, obtiene dirección de envío; `resenas-ms` → `cliente-ms`, valida que el cliente compró el producto.

## Endpoints

<!-- TODO: completar cuando el servicio esté implementado -->

## Entidades

<!-- TODO: completar cuando el servicio esté implementado -->
