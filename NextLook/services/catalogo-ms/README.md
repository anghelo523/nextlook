# catalogo-ms

| Campo | Valor |
|---|---|
| Responsable | Anghelo |
| Tipo | No transaccional |

Administra prendas, categorías y variantes de talla/color. Es la fuente de verdad del catálogo que consultan tanto el cliente (para armar su selección) como `resenas-ms` (para validar que el producto reseñado existe).

Detalle completo (endpoints, entidades, rol protegido, comunicación): [../../docs/microservicios/catalogo-ms.md](../../docs/microservicios/catalogo-ms.md).

## Config

`config/catalogo-ms-{dev,prod}.yml` — servida por el [Config Server](../../infra/nextlook-config) al arrancar este microservicio.

## Estado actual

Implementado como esqueleto Spring Boot bootable (sin logica de negocio todavia: sin entidades, repositorios ni controladores, ver TODOs en la doc de arriba). Ya se registra en Eureka, expone `/actuator/health` y `/actuator/prometheus`, y se conecta a su base y al Config Server. Se levanta junto con el resto de microservicios:

```
docker compose -f ../../docker/docker-compose.infra.yml -f ../compose-dev.yml up -d --build catalogo-ms
```
