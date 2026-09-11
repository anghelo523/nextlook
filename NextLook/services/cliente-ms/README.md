# cliente-ms

| Campo | Valor |
|---|---|
| Responsable | Daniel |
| Tipo | No transaccional |

Gestiona el perfil y las direcciones de cada cliente. Es consultado por `orden-ms` (para validar al cliente antes de confirmar una orden) y por `envio-ms` (para obtener la dirección de envío).

Detalle completo (endpoints, entidades, rol protegido, comunicación): [../../docs/microservicios/cliente-ms.md](../../docs/microservicios/cliente-ms.md).

## Config

`config/cliente-ms-{dev,prod}.yml` — servida por el [Config Server](../../infra/nextlook-config) al arrancar este microservicio.

## Estado actual

Implementado como esqueleto Spring Boot bootable (sin logica de negocio todavia: sin entidades, repositorios ni controladores, ver TODOs en la doc de arriba). Ya se registra en Eureka, expone `/actuator/health` y `/actuator/prometheus`, y se conecta a su base y al Config Server. Se levanta junto con el resto de microservicios:

```
docker compose -f ../../docker/docker-compose.infra.yml -f ../compose-dev.yml up -d --build cliente-ms
```
