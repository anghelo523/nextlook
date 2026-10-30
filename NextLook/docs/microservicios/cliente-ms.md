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

La identidad sale del JWT (`sub` = `keycloak_id`); el cliente nunca envía su propio id.

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| POST | `/api/v1/clientes` | `CLIENTE` | Registra **su** perfil (nombres, apellidos, DNI, celular). Email tomado del token. |
| GET | `/api/v1/clientes/me` | `CLIENTE` | Su perfil + dirección principal — **lo consume `orden-ms` por Feign** reenviando el JWT. |
| PUT | `/api/v1/clientes/me` | `CLIENTE` | Actualiza su perfil. |
| GET | `/api/v1/clientes/me/direcciones` | `CLIENTE` | Sus direcciones de envío. |
| POST | `/api/v1/clientes/me/direcciones` | `CLIENTE` | Agrega dirección; la primera queda como principal. |
| GET | `/api/v1/clientes` | `ADMIN` | Lista todos los clientes. |
| GET | `/api/v1/clientes/{id}` | `ADMIN` | Detalle de un cliente. |

## Entidades

| Tabla | Campos clave |
|---|---|
| `clientes` | `id`, `keycloak_id` (único), `nombres`, `apellidos`, `email` (único), `dni` (único), `telefono`, `fecha_registro` |
| `direcciones` | `id`, `id_cliente`, `alias`, `direccion`, `distrito`, `ciudad`, `referencia`, `principal` |
