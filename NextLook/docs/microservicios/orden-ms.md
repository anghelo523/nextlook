# orden-ms

| Campo | Valor |
|---|---|
| Responsable | Anghelo |
| Tipo | Transaccional |
| Rol protegido | `CLIENTE` (crea sus propias órdenes) |

## Descripción

Registra órdenes, calcula totales, coordina la reserva de stock y el pago. Es el microservicio que orquesta el flujo de compra: valida al cliente contra `cliente-ms`, solicita la reserva de stock a `inventario-ms`, espera el resultado del pago emitido por `pago-ms` y, si es aprobado, avisa a `envio-ms` para iniciar el envío.

## Comunicación

- **Síncrona (saliente):** `orden-ms` → `cliente-ms` (`GET /api/v1/clientes/me`) y `orden-ms` → `catalogo-ms` (`GET /api/v1/productos/variantes/{id}`), ambas con OpenFeign + Circuit Breaker y propagando el JWT y el `X-Trace-ID`.
- **Asíncrona (saliente):** `orden.creada` en el topic `orden-eventos` (key = `ordenId`), publicada después del commit.
- **Asíncrona (entrante):** `stock.reservado` / `stock.rechazado` (`inventario-eventos`) y `pago.aprobado` / `pago.rechazado` (`pago-eventos`). Pendiente: avisar a `envio-ms` y `notificaciones-ms`.

## Endpoints

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| POST | `/api/v1/ordenes` | `CLIENTE` | Crea la orden. **201** si queda `PENDIENTE_STOCK` (se publica `orden.creada`); **202** si queda en `CARRITO` porque `cliente-ms` o `catalogo-ms` no respondió (fallback). |
| POST | `/api/v1/ordenes/{id}/confirmar` | `CLIENTE` | Reintenta validar una orden en `CARRITO`. |
| GET | `/api/v1/ordenes/mias` | `CLIENTE` | Sus órdenes. |
| GET | `/api/v1/ordenes/{id}` | dueño o `ADMIN` | Detalle con ítems (prenda, talla, color, precio copiado). |
| GET | `/api/v1/ordenes` | `ADMIN` | Todas las órdenes. |

Request de ejemplo:

```json
{ "metodoPago": "TARJETA", "items": [ { "varianteId": 2, "cantidad": 2 }, { "varianteId": 7, "cantidad": 1 } ] }
```

Resiliencia (S06): instancias `cliente` y `catalogo` de Resilience4j (ventana 5, mínimo 3 llamadas, 50 % de fallos, 15 s abierto). Estado visible en `/actuator/health` y `/actuator/circuitbreakers`.

## Entidades

| Tabla | Campos clave |
|---|---|
| `ordenes` | `id`, `keycloak_id`, `id_cliente`, `nombre_cliente`, `direccion_envio`, `estado`, `metodo_pago`, `total`, `observacion`, fechas |
| `orden_items` | `id`, `id_orden`, `variante_id`, `prenda_id`, `nombre_prenda`, `talla`, `color`, `precio_unitario`, `cantidad` |

Estados: `CARRITO` → `PENDIENTE_STOCK` → `PENDIENTE_PAGO` → `PAGADA`, o `CANCELADA` (sin stock / pago rechazado).
