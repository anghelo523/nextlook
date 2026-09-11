# envio-ms

| Campo | Valor |
|---|---|
| Responsable | Johan |
| Tipo | Transaccional |

Gestiona el envío de una orden una vez que el pago fue aprobado. Consulta a `cliente-ms` para obtener la dirección de entrega y emite eventos que consume `notificaciones-ms`.

Detalle completo (endpoints, entidades, rol protegido, comunicación): [../../docs/microservicios/envio-ms.md](../../docs/microservicios/envio-ms.md).

## Config

`config/envio-ms-{dev,prod}.yml` — servida por el [Config Server](../../infra/nextlook-config) al arrancar este microservicio.

## Estado actual

Implementado como esqueleto Spring Boot bootable (sin logica de negocio todavia: sin entidades, repositorios ni controladores, ver TODOs en la doc de arriba). Ya se registra en Eureka, expone `/actuator/health` y `/actuator/prometheus`, y se conecta a su base y al Config Server. Se levanta junto con el resto de microservicios:

```
docker compose -f ../../docker/docker-compose.infra.yml -f ../compose-dev.yml up -d --build envio-ms
```
