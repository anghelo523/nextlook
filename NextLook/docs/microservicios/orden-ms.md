# orden-ms

| Campo | Valor |
|---|---|
| Responsable | Anghelo |
| Tipo | Transaccional |
| Rol protegido | `CLIENTE` (crea sus propias órdenes) |

## Descripción

Registra órdenes, calcula totales, coordina la reserva de stock y el pago. Es el microservicio que orquesta el flujo de compra: valida al cliente contra `cliente-ms`, solicita la reserva de stock a `inventario-ms`, espera el resultado del pago emitido por `pago-ms` y, si es aprobado, avisa a `envio-ms` para iniciar el envío.

## Comunicación

- **Síncrona (saliente):** `orden-ms` → `cliente-ms` (REST/Feign), valida al cliente antes de registrar la orden.
- **Asíncrona (saliente):** `orden-ms` → `inventario-ms` (evento "Orden Creada", solicita reserva de stock); `orden-ms` → `envio-ms` (evento "Pago Aprobado", inicia envío); `orden-ms` → `notificaciones-ms` (eventos del ciclo de vida de la orden).
- **Asíncrona (entrante):** `pago-ms` → `orden-ms` (evento con el resultado del pago; `orden-ms` actualiza el estado de la orden).

## Endpoints

<!-- TODO: completar cuando el servicio esté implementado -->

## Entidades

<!-- TODO: completar cuando el servicio esté implementado -->
