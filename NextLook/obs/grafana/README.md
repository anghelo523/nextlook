# grafana

Dashboards y datasources de Grafana para visualizar las métricas (Prometheus) y logs (Loki/Promtail) de todos los microservicios de NextLook en un solo lugar.

```
grafana/
└── provisioning/
    └── datasources/   # Datasources auto-provisionados al arrancar Grafana (ej. datasources-dev.yml)
```

`provisioning/` permite que Grafana quede configurado automáticamente al levantar el contenedor (datasources y dashboards como código), sin tener que configurarlo a mano desde la UI cada vez.

Implementado (S10): datasources con `uid` fijo (`prometheus`, `loki`) y el tablero `dashboards/nextlook-diagnostico.json`, cargado por `provisioning/dashboards/dashboards.yml`.
