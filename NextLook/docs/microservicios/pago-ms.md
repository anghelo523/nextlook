# pago-ms

| Campo | Valor |
|---|---|
| Responsable | Daniel |
| Tipo | Transaccional |
| Rol protegido | `CLIENTE` (paga sus propias órdenes) |

## Descripción

Integra la API real de Mercado Pago (Sandbox) y registra las transacciones. Recibe la solicitud de cobro, llama directamente a Mercado Pago, registra el ID de transacción que devuelve la pasarela y emite un evento con el resultado del pago (aprobado/rechazado) para que `orden-ms` e `inventario-ms` actualicen su estado. Ver el flujo completo en [Integración de pago](../integracion-pago.md).

## Comunicación

- **Asíncrona (entrante):** `orden.creada` → registra el pago `PENDIENTE`; `stock.reservado` / `stock.rechazado` → habilita o anula el cobro.
- **Síncrona (saliente):** `pago-ms` → pasarela (hoy simulada; Mercado Pago Sandbox pendiente).
- **Asíncrona (saliente):** `pago.aprobado` / `pago.rechazado` en `pago-eventos`, después del commit.

## Endpoints

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| POST | `/api/v1/pagos/procesar` | `CLIENTE` (dueño) | Cobra una orden en `LISTO_PARA_PAGAR`. Body `{ "ordenId": 1, "titular": "APRO" }`. |
| GET | `/api/v1/pagos/orden/{ordenId}` | dueño o `ADMIN` | Estado del pago de una orden. |
| GET | `/api/v1/pagos` | `ADMIN` | Todos los pagos. |

!!! warning "Pasarela simulada"
    Por ahora `PasarelaSimulada` reemplaza a Mercado Pago, con la misma convención de titulares de prueba del Sandbox: `APRO` aprueba, `FUND` rechaza por fondos, `OTHE` u otro rechaza. La integración real se conecta implementando la interfaz `PasarelaPago`.

## Entidades

| Tabla | Campos clave |
|---|---|
| `pagos` | `id`, `orden_id` (**UNIQUE**), `keycloak_id`, `id_cliente`, `monto`, `metodo_pago`, `estado`, `titular`, `id_transaccion`, `motivo` |

Estados: `PENDIENTE` (llegó `orden.creada`) → `LISTO_PARA_PAGAR` (llegó `stock.reservado`) → `APROBADO` / `RECHAZADO`; o `ANULADO` si llegó `stock.rechazado`.
