# notificaciones-ms

| Campo | Valor |
|---|---|
| Responsable | Johan |
| Tipo | No transaccional |

Escucha los eventos de `orden-ms`, `pago-ms`, `inventario-ms` y `envio-ms` para avisar al cliente en cada paso de su compra. No expone flujo de negocio propio, solo procesa eventos del resto del sistema.

Detalle completo (endpoints, entidades, rol protegido, comunicación): [../../docs/microservicios/notificaciones-ms.md](../../docs/microservicios/notificaciones-ms.md).

## Config

`config/notificaciones-ms-{dev,prod}.yml` — servida por el [Config Server](../../infra/nextlook-config) al arrancar este microservicio.

## Estado actual

Implementado como esqueleto Spring Boot bootable (sin logica de negocio todavia: sin entidades, repositorios ni controladores, ver TODOs en la doc de arriba). Ya se registra en Eureka, expone `/actuator/health` y `/actuator/prometheus`, y se conecta a su base y al Config Server. Se levanta junto con el resto de microservicios:

```
docker compose -f ../../docker/docker-compose.infra.yml -f ../compose-dev.yml up -d --build notificaciones-ms
```
