# envio-ms

| Campo | Valor |
|---|---|
| Responsable | Johan |
| Tipo | Transaccional |
| Rol protegido | `CLIENTE` (consulta el seguimiento de su envío) / `ADMIN` (gestión global) |

## Descripción

Coordina el envío de las órdenes y el seguimiento de la entrega. Se activa con el evento "Pago Aprobado" emitido por `orden-ms`, y consulta la dirección de envío directamente a `cliente-ms`.

## Comunicación

- **Síncrona (saliente):** `envio-ms` → `cliente-ms` (REST/Feign), obtiene la dirección de envío.
- **Asíncrona (entrante):** `orden-ms` → `envio-ms` (evento "Pago Aprobado", inicia envío).
- **Asíncrona (saliente):** `envio-ms` → `notificaciones-ms` (eventos de envío/seguimiento).

## Endpoints

<!-- TODO: completar cuando el servicio esté implementado -->

## Entidades

<!-- TODO: completar cuando el servicio esté implementado -->
