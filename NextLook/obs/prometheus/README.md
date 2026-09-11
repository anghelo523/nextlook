# prometheus

Configuración de Prometheus: qué endpoints scrapea y con qué frecuencia.

- `prometheus-dev.yml`: usa `eureka_sd_configs` para descubrir automáticamente los 8 microservicios registrados en [nextlook-eureka](../../infra/nextlook-eureka) (no hay que listarlos a mano, ni actualizar nada cuando se agregue uno nuevo), más un `static_configs` fijo para Config Server, Eureka y Gateway (que no se registran a sí mismos en Eureka).

Se levanta junto con el resto del stack (ver [../compose-dev.yml](../compose-dev.yml)):

```
docker compose -f ../../docker/docker-compose.infra.yml -f ../../services/compose-dev.yml -f ../compose-dev.yml up -d --build
```

UI: http://localhost:19090 — target status: http://localhost:19090/targets
