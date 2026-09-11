# nextlook-eureka

Service Registry de NextLook (Netflix Eureka vía Spring Cloud Netflix). Cada microservicio se registra aquí al arrancar; el [nextlook-gateway](../nextlook-gateway) y los microservicios que se llaman entre sí (ej. `orden-ms` → `cliente-ms`) lo consultan para resolver la ubicación real (host/puerto) de un servicio a partir de su nombre lógico, en vez de tener esa dirección hardcodeada.

- **Puerto:** `18761`
- **Config:** obtenida del Config Server ([nextlook-config](../nextlook-config)) al arrancar, no de un archivo local — ver [config/](config).

```
nextlook-eureka/
└── config/
    ├── nextlook-eureka-dev.yml
    └── nextlook-eureka-prod.yml
```

## Estado actual

Implementado como servidor Eureka bootable (`@EnableEurekaServer`), sin lógica de negocio propia — solo se registra el servidor. Se levanta junto con el resto de la infra:

```
docker compose -f ../../docker/docker-compose.infra.yml up -d --build nextlook-eureka
```

Dashboard: http://localhost:18761
