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

Implementado (U2): categorías, prendas y variantes de talla/color con datos semilla de ropa; lectura pública y escritura solo `ADMIN` (JWT de Keycloak). Expone `GET /api/v1/productos/variantes/{id}`, que `orden-ms` consume por Feign. Ya se registra en Eureka, expone `/actuator/health` y `/actuator/prometheus`, y se conecta a su base y al Config Server. Se levanta junto con el resto de microservicios:

```
docker compose -f ../../docker/docker-compose.infra.yml -f ../compose-dev.yml up -d --build catalogo-ms
```
