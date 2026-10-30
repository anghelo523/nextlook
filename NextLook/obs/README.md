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

## Estado actual (S10)

Todo implementado en [compose-dev.yml](compose-dev.yml):

| Pieza | URL | Config |
|---|---|---|
| Prometheus | http://localhost:19090/targets | [prometheus/prometheus-dev.yml](prometheus/prometheus-dev.yml): microservicios por Eureka, infra y Keycloak por target estático |
| Loki | http://localhost:13100/ready | configuración por defecto de la imagen |
| Promtail | — | [promtail/promtail-config-dev.yml](promtail/promtail-config-dev.yml): consola de los contenedores + `logs/*.log` de lo que corre con Maven |
| Grafana | http://localhost:13000 (admin/admin) | datasources y tablero provisionados en [grafana/](grafana) |

Detalle en [docs/observabilidad.md](../docs/observabilidad.md).
