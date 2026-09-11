# Integración de pago

`pago-ms` integra la API real de Mercado Pago (Sandbox). No simula el pago: llama directamente a la pasarela y registra lo que esta devuelve.

```mermaid
sequenceDiagram
    participant Cliente
    participant OrdenMS as orden-ms
    participant PagoMS as pago-ms
    participant MercadoPago as Mercado Pago (Sandbox)
    participant InventarioMS as inventario-ms

    Cliente->>PagoMS: POST /pagos/procesar
    PagoMS->>MercadoPago: procesa el pago (API real)
    MercadoPago-->>PagoMS: estado APROBADO / RECHAZADO + ID de transacción
    PagoMS->>PagoMS: registra el ID de transacción
    PagoMS-->>OrdenMS: evento con el resultado del pago
    PagoMS-->>InventarioMS: evento con el resultado del pago
    OrdenMS->>OrdenMS: actualiza el estado de la orden
    InventarioMS->>InventarioMS: confirma el descuento o libera la reserva de stock
```

La llamada `pago-ms → Mercado Pago` es síncrona (petición directa a la API externa). Los avisos de `pago-ms` hacia `orden-ms` e `inventario-ms` son asíncronos (eventos).

## Requisitos clave de `pago-ms`

- `POST /pagos/procesar` llama a la API real de Mercado Pago (Sandbox); el resultado es `APROBADO` o `RECHAZADO`.
- Registra el ID de transacción que devuelve la pasarela.
- Emite un evento con el resultado del pago.
- `orden-ms` actualiza el estado de la orden a partir de ese evento.
- `inventario-ms` confirma el descuento definitivo de stock (pago aprobado) o libera la reserva (pago rechazado u orden cancelada) a partir de ese mismo evento.

!!! warning "Datos pendientes de confirmar"
    El brief no detalla el contrato exacto de `POST /pagos/procesar` (request/response), ni las credenciales/configuración de Sandbox, ni la tecnología de mensajería usada para emitir el evento de resultado. Completar cuando el servicio esté implementado.
