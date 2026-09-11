# notificaciones-ms

| Campo | Valor |
|---|---|
| Responsable | Johan |
| Tipo | No transaccional |
| Rol protegido | `ADMIN` (gestión global de notificaciones) |

## Descripción

Avisos de orden, pago e inventario vía eventos. Escucha los eventos emitidos por `orden-ms`, `pago-ms`, `inventario-ms` y `envio-ms`, y registra cada notificación con su estado de envío. Ver el detalle completo en [Notificaciones](../notificaciones.md).

## Comunicación

- **Asíncrona (entrante):** `orden-ms` → `notificaciones-ms`; `pago-ms` → `notificaciones-ms`; `inventario-ms` → `notificaciones-ms`; `envio-ms` → `notificaciones-ms`.

## Capacidades (confirmadas en el brief)

- Registra cada notificación con su estado de envío.
- Configura plantillas por tipo de evento y canal.
- Reintenta envíos fallidos con un límite configurable.
- Conserva la referencia al evento de negocio de origen.

## Endpoints

<!-- TODO: completar cuando el servicio esté implementado -->

## Entidades

<!-- TODO: completar cuando el servicio esté implementado -->
