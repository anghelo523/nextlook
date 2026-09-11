# inventario-ms

| Campo | Valor |
|---|---|
| Responsable | Yeins |
| Tipo | Transaccional |
| Rol protegido | `ADMIN` (gestión global de inventario) |

## Descripción

Gestiona stock, movimientos y reservas asociadas a órdenes. Escucha el evento "Orden Creada" de `orden-ms` para verificar y reservar stock, y luego confirma el descuento definitivo o libera la reserva según el resultado del pago emitido por `pago-ms`. Ver el detalle de estados en [Consistencia de datos](../consistencia.md).

## Comunicación

- **Asíncrona (entrante):** `orden-ms` → `inventario-ms` (evento "Orden Creada", solicita reserva); `pago-ms` → `inventario-ms` (evento con el resultado del pago: confirma descuento o libera reserva).
- **Asíncrona (saliente):** `inventario-ms` → `notificaciones-ms` (eventos de inventario).

## Endpoints

<!-- TODO: completar cuando el servicio esté implementado -->

## Entidades

<!-- TODO: completar cuando el servicio esté implementado -->

### `reserva_stock`

Entidad ya confirmada en el brief (ver [Consistencia de datos](../consistencia.md) para el diagrama de estados completo):

| Campo | Descripción |
|---|---|
| `id` | Identificador de la reserva. |
| `orden_id` | Orden asociada. |
| `producto_id` | Producto/variante reservado. |
| `cantidad` | Cantidad reservada. |
| `estado` | Estado de la reserva (ver diagrama de estados). |

<!-- TODO: completar tipos de dato, claves y demás campos cuando el servicio esté implementado -->
