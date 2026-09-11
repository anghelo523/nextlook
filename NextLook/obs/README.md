# obs

Observabilidad de NextLook: métricas, logs y sus dashboards, más los overrides de despliegue que ajustan la infraestructura para un entorno concreto.

```
obs/
├── grafana/      # Dashboards y datasources para visualizar métricas y logs
├── prometheus/   # Scrape de métricas de cada microservicio (via Actuator)
├── promtail/     # Recolección de logs de los contenedores hacia Loki/Grafana
└── overrides/    # Archivos que sobreescriben config de despliegue por entorno
```

## Cómo encajan las piezas

- **Prometheus** hace scraping periódico del endpoint `/actuator/prometheus` de cada microservicio y lo guarda como serie de tiempo.
- **Grafana** consulta a Prometheus (métricas) y Loki (logs, vía Promtail) para armar los dashboards.
- **Promtail** lee los logs de los contenedores Docker y los envía a Loki, que Grafana usa como datasource de logs.
- **overrides** guarda archivos (ej. `docker-compose.override.yml`, variables por entorno) que se aplican encima de la infraestructura base sin modificarla directamente — útil para diferencias puntuales entre máquinas o entornos.

## Estado actual

**Prometheus** ya está implementado y corriendo (ver [compose-dev.yml](compose-dev.yml) y [prometheus/](prometheus)): descubre automáticamente los 8 microservicios vía Eureka y scrapea también Config Server, Eureka y Gateway. Dashboard: http://localhost:19090/targets

**Grafana, Promtail y Loki** siguen pendientes — el datasource de Grafana ya está preparado en [grafana/](grafana) para cuando se agreguen.
