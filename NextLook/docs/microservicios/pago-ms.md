# pago-ms

| Campo | Valor |
|---|---|
| Responsable | Daniel |
| Tipo | Transaccional |
| Rol protegido | `CLIENTE` (paga sus propias órdenes) |

## Descripción

Integra la API real de Mercado Pago (Sandbox) y registra las transacciones. Recibe la solicitud de cobro, llama directamente a Mercado Pago, registra el ID de transacción que devuelve la pasarela y emite un evento con el resultado del pago (aprobado/rechazado) para que `orden-ms` e `inventario-ms` actualicen su estado. Ver el flujo completo en [Integración de pago](../integracion-pago.md).

## Comunicación

- **Síncrona (saliente):** `pago-ms` → Mercado Pago (API externa real, Sandbox), llamada directa para procesar el pago.
- **Asíncrona (saliente):** `pago-ms` → `orden-ms` (evento con el resultado del pago); `pago-ms` → `inventario-ms` (evento con el resultado del pago); `pago-ms` → `notificaciones-ms` (evento de pago).

## Endpoints

<!-- TODO: completar cuando el servicio esté implementado -->

### `POST /pagos/procesar`

Ya confirmado en el brief: llama a la API real de Mercado Pago (Sandbox) y devuelve un estado `APROBADO`/`RECHAZADO`.

<!-- TODO: completar request/response exactos cuando el servicio esté implementado -->

## Entidades

<!-- TODO: completar cuando el servicio esté implementado -->
