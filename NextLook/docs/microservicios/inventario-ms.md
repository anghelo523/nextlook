# inventario-ms

| Campo | Valor |
|---|---|
| Responsable | Yeins |
| Tipo | Transaccional |
| Rol protegido | `ADMIN` (gestión global de inventario) |

## Descripción

Gestiona stock, movimientos y reservas asociadas a órdenes. Escucha el evento "Orden Creada" de `orden-ms` para verificar y reservar stock, y luego confirma el descuento definitivo o libera la reserva según el resultado del pago emitido por `pago-ms`. Ver el detalle de estados en [Consistencia de datos](../consistencia.md).

## Comunicación

- **Asíncrona (entrante):** `orden.creada` (`orden-eventos`) → reserva; `pago.aprobado` → confirma; `pago.rechazado` → libera (compensación).
- **Asíncrona (saliente):** `stock.reservado` / `stock.rechazado` en `inventario-eventos`. Pendiente: eventos hacia `notificaciones-ms`.

## Endpoints

Solo `ADMIN`: el stock lo mueven los eventos de la Saga, no el comprador.

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/inventario` | Stock de todas las variantes (`disponible`, `reservado`). |
| GET | `/api/v1/inventario/{varianteId}` | Stock de una variante. |
| PUT | `/api/v1/inventario/{varianteId}` | Registra o ajusta stock (`{ "sku": "...", "disponible": 20 }`). |
| GET | `/api/v1/inventario/reservas/{ordenId}` | Reservas de una orden y su estado. |

## Entidades

| Tabla | Campos clave |
|---|---|
| `stock_variante` | `variante_id` (PK, id de catalogo-ms), `sku`, `disponible`, `reservado` |
| `orden_procesada` | `orden_id` (**UNIQUE** — consumidor idempotente), `resultado` (RESERVADO/RECHAZADO), `motivo` |
| `reserva_stock` | `orden_id`, `variante_id` (UNIQUE juntos), `cantidad`, `estado` (RESERVADA/CONFIRMADA/LIBERADA) |

La reserva es *todo o nada* y bloquea filas (`SELECT … FOR UPDATE`) en orden de `variante_id` para que dos compradores no se lleven la última talla.
