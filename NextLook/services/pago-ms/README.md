# pago-ms

| Campo | Valor |
|---|---|
| Responsable | Daniel |
| Tipo | Transaccional |

Procesa el pago de una orden llamando directamente a la API de Mercado Pago (Sandbox). Emite un evento con el resultado (aprobado/rechazado) que consumen `orden-ms` e `inventario-ms`.

Detalle completo (endpoints, entidades, rol protegido, comunicación): [../../docs/microservicios/pago-ms.md](../../docs/microservicios/pago-ms.md) y [../../docs/integracion-pago.md](../../docs/integracion-pago.md).

## Config

`config/pago-ms-{dev,prod}.yml` — servida por el [Config Server](../../infra/nextlook-config) al arrancar este microservicio.

## Estado actual

Implementado como esqueleto Spring Boot bootable (sin logica de negocio todavia: sin entidades, repositorios ni controladores, ver TODOs en la doc de arriba). Ya se registra en Eureka, expone `/actuator/health` y `/actuator/prometheus`, y se conecta a su base y al Config Server. Se levanta junto con el resto de microservicios:

```
docker compose -f ../../docker/docker-compose.infra.yml -f ../compose-dev.yml up -d --build pago-ms
```
