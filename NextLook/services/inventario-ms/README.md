# inventario-ms

| Campo | Valor |
|---|---|
| Responsable | Yeins |
| Tipo | Transaccional |

Controla el stock en tiempo real. Reserva stock cuando `orden-ms` emite el evento de orden creada, y actualiza el estado de la reserva (confirmar o liberar) según el resultado del pago que emite `pago-ms`.

Detalle completo (endpoints, entidades, rol protegido, comunicación): [../../docs/microservicios/inventario-ms.md](../../docs/microservicios/inventario-ms.md).

## Config

`config/inventario-ms-{dev,prod}.yml` — servida por el [Config Server](../../infra/nextlook-config) al arrancar este microservicio.

## Estado actual

Implementado como esqueleto Spring Boot bootable (sin logica de negocio todavia: sin entidades, repositorios ni controladores, ver TODOs en la doc de arriba). Ya se registra en Eureka, expone `/actuator/health` y `/actuator/prometheus`, y se conecta a su base y al Config Server. Se levanta junto con el resto de microservicios:

```
docker compose -f ../../docker/docker-compose.infra.yml -f ../compose-dev.yml up -d --build inventario-ms
```
