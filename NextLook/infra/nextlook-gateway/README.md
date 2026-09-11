# nextlook-gateway

API Gateway de NextLook (Spring Cloud Gateway). Punto único de entrada para el cliente: recibe todas las peticiones HTTP y las enruta al microservicio correcto, resolviendo su ubicación a través de [nextlook-eureka](../nextlook-eureka).

- **Puerto:** `18080`
- **Config:** obtenida del Config Server ([nextlook-config](../nextlook-config)) al arrancar — ver [config/](config).

```
nextlook-gateway/
└── config/
    ├── nextlook-gateway-dev.yml
    └── nextlook-gateway-prod.yml
```

## Estado actual

Implementado con Spring Cloud Gateway MVC (`spring-cloud-starter-gateway-server-webmvc`) — las rutas hacia cada microservicio se definen por config (`nextlook-gateway-{dev,prod}.yml`, no en Java), así que agregar o cambiar una ruta no requiere tocar código. Se levanta junto con el resto de la infra:

```
docker compose -f ../../docker/docker-compose.infra.yml up -d --build nextlook-gateway
```

Punto de entrada: http://localhost:18080
