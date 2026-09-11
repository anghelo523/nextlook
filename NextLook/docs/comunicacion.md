# Comunicación entre servicios

## Síncrono (REST/Feign)

| Origen | Destino | Motivo |
|---|---|---|
| `orden-ms` | `cliente-ms` | Valida el cliente antes de registrar la orden. |
| `envio-ms` | `cliente-ms` | Obtiene la dirección de envío. |
| `resenas-ms` | `catalogo-ms` | Valida el producto reseñado. |
| `resenas-ms` | `cliente-ms` | Valida que el cliente compró el producto. |
| `pago-ms` | Mercado Pago | Llamada directa a la API externa real (Sandbox). |

## Asíncrono (eventos)

| Origen | Destino | Evento |
|---|---|---|
| `orden-ms` | `inventario-ms` | "Orden Creada" — solicita reserva de stock. |
| `pago-ms` | `orden-ms` | Resultado del pago (aprobado/rechazado). |
| `pago-ms` | `inventario-ms` | Resultado del pago (aprobado/rechazado). |
| `orden-ms` | `envio-ms` | "Pago Aprobado" — inicia envío. |
| `orden-ms` | `notificaciones-ms` | Eventos del ciclo de vida de la orden. |
| `pago-ms` | `notificaciones-ms` | Eventos de pago. |
| `inventario-ms` | `notificaciones-ms` | Eventos de inventario. |
| `envio-ms` | `notificaciones-ms` | Eventos de envío/seguimiento. |

!!! warning "Dato pendiente de confirmar"
    El brief no especifica la tecnología de mensajería (Kafka, RabbitMQ u otra) para la comunicación asíncrona. No se asume ninguna hasta que el equipo la confirme.
