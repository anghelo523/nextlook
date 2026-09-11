# services

Los 8 microservicios de negocio de NextLook. Cada uno es independiente: su propio código, su propia base de datos y su propia configuración; lo único que comparten es la infraestructura de [../infra](../infra) (Config Server, Eureka, Gateway).

```
services/
├── catalogo-ms/
├── cliente-ms/
├── envio-ms/
├── inventario-ms/
├── notificaciones-ms/
├── orden-ms/
├── pago-ms/
└── resenas-ms/
```

Cada carpeta sigue la misma forma:

```
<nombre>-ms/
├── config/
│   ├── <nombre>-ms-dev.yml
│   └── <nombre>-ms-prod.yml
├── src/main/java/com/nextlook/<nombre>/<Nombre>MsApplication.java
├── src/main/resources/application.yml
├── pom.xml
└── Dockerfile
```

La carpeta `config/` es la que el [Config Server](../infra/nextlook-config) lee para servirle su configuración a este microservicio al arrancar (ver `search-locations` en [../infra/nextlook-config/src/main/resources/application.yml](../infra/nextlook-config/src/main/resources/application.yml)). El resto (`src/`, `pom.xml`, `Dockerfile`) ya existe como esqueleto Spring Boot bootable, igual que [nextlook-config](../infra/nextlook-config); ahí va la lógica de negocio de cada uno cuando se implemente.

Para el detalle de negocio de cada uno (responsable, tipo transaccional, endpoints, entidades, comunicación con otros servicios) ver [../docs/microservicios](../docs/microservicios).

## Puertos (dev)

| Servicio | Puerto | Base de datos |
|---|---|---|
| catalogo-ms | 8081 | catalogo_db (15432) |
| inventario-ms | 8082 | inventario_db (15433) |
| resenas-ms | 8083 | resenas_db (15434) |
| orden-ms | 8084 | orden_db (15435) |
| pago-ms | 8085 | pago_db (15436) |
| cliente-ms | 8086 | cliente_db (15437) |
| envio-ms | 8087 | envio_db (15438) |
| notificaciones-ms | 8088 | notificaciones_db (15439) |

Con todo levantado (ver [compose-dev.yml](compose-dev.yml)), cada uno responde en `http://localhost:<puerto>/actuator/health` y `http://localhost:<puerto>/swagger-ui.html`.

## Levantar todo junto

```
docker compose -f ../docker/docker-compose.infra.yml -f compose-dev.yml up -d --build
```

Se registran solos en [Eureka](../infra/nextlook-eureka) (http://localhost:18761) y quedan visibles en [Prometheus](../obs/prometheus) (http://localhost:19090/targets).
