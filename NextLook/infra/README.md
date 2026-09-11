# infra

Infraestructura compartida de NextLook: las piezas de plataforma de las que dependen todos los microservicios de negocio (que viven en [../services](../services)).

```
infra/
├── config-repo/       # Config compartida (application.yml) que sirve el Config Server
├── nextlook-config/   # Spring Cloud Config Server: configuracion centralizada
├── nextlook-eureka/   # Service Registry (Eureka): descubrimiento de microservicios
└── nextlook-gateway/  # API Gateway: punto de entrada unico hacia los microservicios
```

## Cómo encajan las piezas

1. **nextlook-config** arranca primero y expone por HTTP toda la configuración (dev/prod) de cada componente, leyéndola de archivos locales (perfil `native`):
   - `config-repo/application.yml` → config compartida por todos los clientes.
   - `nextlook-eureka/config/` y `nextlook-gateway/config/` → config propia de cada uno.
   - `../services/{application}/config/` → config propia de cada microservicio de negocio.
2. **nextlook-eureka** se registra contra el Config Server para obtener su config, y luego actúa como Service Registry: cada microservicio se registra en él para ser descubierto por nombre en vez de por IP/puerto fijo.
3. **nextlook-gateway** también obtiene su config del Config Server, consulta a Eureka para saber dónde está cada microservicio, y enruta hacia ellos como punto único de entrada para el cliente.

Ver [../docker/docker-compose.infra.yml](../docker/docker-compose.infra.yml) para cómo se levantan y conectan estos tres componentes en desarrollo.

## Estado actual

- `nextlook-config`: implementado (Spring Boot + Spring Cloud Config Server).
- `nextlook-eureka` y `nextlook-gateway`: por implementar — hoy solo contienen su carpeta de configuración (`config/`), lista para cuando se cree el módulo Spring Boot correspondiente.
