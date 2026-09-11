# docker

Orquestación de infraestructura de NextLook con Docker Compose.

- **[docker-compose.infra.yml](docker-compose.infra.yml):** infraestructura de **desarrollo** — Config Server, Eureka, Gateway, una base Postgres por microservicio y Keycloak. Pensado para que cada integrante corra solo sus microservicios asignados con `mvn spring-boot:run` contra esta infraestructura ya dockerizada, en vez de instalar todo localmente.

```
docker compose -f docker-compose.infra.yml up -d
```

La pila completa dockerizada (infra + los 8 microservicios + Kafka + observabilidad de [../obs](../obs)) se arma más adelante, en la fase de producción local del proyecto, como un `docker-compose.yml` aparte.
