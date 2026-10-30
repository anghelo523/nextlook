# Comunicación entre servicios

## Síncrono (REST/Feign) — S06

| Origen | Destino | Endpoint | Protección | Estado |
|---|---|---|---|---|
| `orden-ms` | `cliente-ms` | `GET /api/v1/clientes/me` | Circuit Breaker `cliente` | ✅ |
| `orden-ms` | `catalogo-ms` | `GET /api/v1/productos/variantes/{id}` | Circuit Breaker `catalogo` | ✅ |
| `envio-ms` | `cliente-ms` | dirección de envío | — | ⬜ |
| `resenas-ms` | `catalogo-ms` / `cliente-ms` | valida prenda y compra | — | ⬜ |
| `pago-ms` | Mercado Pago | API externa | — | 🟡 simulada |

Cada llamada tiene **timeout** (2 s conexión, 3 s lectura), **3 intentos** con *backoff* exponencial (400 → 800 ms) solo ante fallos de infraestructura (conexión rechazada, 5xx, sin instancias) y un **Circuit Breaker** por destino; el *Retry* envuelve al circuito, así que con el circuito abierto no se reintenta. Los nombres de servicio se resuelven en Eureka (`@FeignClient(name = "cliente-ms")`). Cada llamada reenvía el `Authorization: Bearer …` del comprador y el `X-Trace-ID`. Si una dependencia falla o el circuito está abierto, la orden **no se pierde**: queda en `CARRITO` (HTTP 202) y se confirma luego con `POST /api/v1/ordenes/{id}/confirmar`.

## Asíncrono (Kafka) — S08

Tecnología elegida: **Apache Kafka 4 (modo KRaft)**, contenedor `nextlook-kafka` (`localhost:19092` desde el host, `nextlook-kafka:9092` dentro de Docker) y **Kafka UI** en http://localhost:18085.

| Topic | Productor | Eventos | Consumidores (group-id) |
|---|---|---|---|
| `orden-eventos` | `orden-ms` | `orden.creada` | `inventario-ms`, `pago-ms` |
| `inventario-eventos` | `inventario-ms` | `stock.reservado`, `stock.rechazado` | `orden-ms`, `pago-ms` |
| `pago-eventos` | `pago-ms` | `pago.aprobado`, `pago.rechazado` | `orden-ms`, `inventario-ms` |

Reglas comunes:

- **key = `ordenId`**: todos los eventos de una orden caen en la misma partición y se leen en orden.
- Se publica **después del commit** (`TransactionSynchronization.afterCommit`): si la base falla, no sale un evento falso.
- JSON sin cabeceras de tipo; cada consumidor tiene su **propia copia del contrato** (`spring.json.value.default.type` por listener) e ignora campos que no conoce.
- `ErrorHandlingDeserializer`: un mensaje mal formado se registra y se salta, no bloquea la partición.
- Topics con 3 particiones, creados por cada servicio (`KafkaTopicsConfig`); `auto.create.topics` está desactivado en el broker.

### Contratos

```json
// orden.creada  (orden-eventos)
{ "tipoEvento": "orden.creada", "ordenId": 12, "idCliente": 3, "keycloakId": "8f1c…",
  "total": 229.70, "metodoPago": "TARJETA",
  "items": [ { "varianteId": 2, "cantidad": 2 }, { "varianteId": 7, "cantidad": 1 } ],
  "origen": "orden-ms", "timestamp": "2026-10-16T15:04:05Z" }

// stock.reservado | stock.rechazado  (inventario-eventos)
{ "tipoEvento": "stock.rechazado", "ordenId": 12, "motivo": "NL-003-M-NEG pide 2, hay 1",
  "origen": "inventario-ms", "timestamp": "…" }

// pago.aprobado | pago.rechazado  (pago-eventos)
{ "tipoEvento": "pago.aprobado", "ordenId": 12, "pagoId": 5, "monto": 229.70,
  "idTransaccion": "SIM-1A2B3C4D", "motivo": null, "origen": "pago-ms", "timestamp": "…" }
```

Formato de log común: `component=… eventType=… ordenId=… status=published|consumed|processed|ignored|compensated`.

Pendiente (U3): eventos hacia `envio-ms` y `notificaciones-ms`.
