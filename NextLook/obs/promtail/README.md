# promtail

Configuración de Promtail: recolecta los logs de los contenedores Docker de NextLook y los envía a Loki para que Grafana los muestre como datasource de logs. Aquí va el `promtail-config.yml` con los `scrape_configs` de logs.

Implementado (S10): `promtail-config-dev.yml` con dos caminos — consola de los contenedores de `nextlook-dev-net` (`docker_sd_configs`) y archivos `logs/<servicio>.log` de los servicios corridos con Maven.
