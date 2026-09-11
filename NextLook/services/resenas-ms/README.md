# resenas-ms

| Campo | Valor |
|---|---|
| Responsable | Yeins |
| Tipo | No transaccional |

Gestiona las reseñas que los clientes dejan sobre productos comprados. Valida contra `catalogo-ms` que el producto reseñado exista y contra `cliente-ms` que la compra sea real.

Detalle completo (endpoints, entidades, rol protegido, comunicación): [../../docs/microservicios/resenas-ms.md](../../docs/microservicios/resenas-ms.md).

## Config

`config/resenas-ms-{dev,prod}.yml` — servida por el [Config Server](../../infra/nextlook-config) al arrancar este microservicio.

## Estado actual

Implementado como esqueleto Spring Boot bootable (sin logica de negocio todavia: sin entidades, repositorios ni controladores, ver TODOs en la doc de arriba). Ya se registra en Eureka, expone `/actuator/health` y `/actuator/prometheus`, y se conecta a su base y al Config Server. Se levanta junto con el resto de microservicios:

```
docker compose -f ../../docker/docker-compose.infra.yml -f ../compose-dev.yml up -d --build resenas-ms
```
