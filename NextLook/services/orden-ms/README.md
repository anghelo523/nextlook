# orden-ms

| Campo | Valor |
|---|---|
| Responsable | Anghelo |
| Tipo | Transaccional |

Crea y gestiona las órdenes de compra. Valida al cliente contra `cliente-ms` antes de confirmar, pide la reserva de stock a `inventario-ms` por evento, y reacciona al resultado del pago que emite `pago-ms` para confirmar o cancelar la orden.

Detalle completo (endpoints, entidades, rol protegido, comunicación): [../../docs/microservicios/orden-ms.md](../../docs/microservicios/orden-ms.md).

## Config

`config/orden-ms-{dev,prod}.yml` — servida por el [Config Server](../../infra/nextlook-config) al arrancar este microservicio.

## Estado actual

Implementado como esqueleto Spring Boot bootable (sin logica de negocio todavia: sin entidades, repositorios ni controladores, ver TODOs en la doc de arriba). Ya se registra en Eureka, expone `/actuator/health` y `/actuator/prometheus`, y se conecta a su base y al Config Server. Se levanta junto con el resto de microservicios:

```
docker compose -f ../../docker/docker-compose.infra.yml -f ../compose-dev.yml up -d --build orden-ms
```
