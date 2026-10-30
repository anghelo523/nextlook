# consolas

Sistema distribuido de comercio electrónico — proyecto del curso **Desarrollo de Aplicaciones Distribuidas** (DIST), UPeU 2026-2.

## Documentación

La documentación completa del curso (sílabo, guías de sesión paso a paso, arquitectura del sistema) vive en [`docs/`](docs/index.md) y se publica como sitio con MkDocs:

**[262dist.github.io/consolas](https://262dist.github.io/consolas/)**

## Estructura del repositorio

```text
consolas/
├── docs/                    # Documentación del curso (MkDocs)
│   ├── sesiones/             # Guías paso a paso (S01, S02, ...)
│   └── silabo_dist_2026_2.md # Sílabo vigente
├── services/                 # Microservicios
│   ├── consolas-catalogo-ms/   # Categorías y productos (S1)
│   ├── consolas-orden-ms/      # Órdenes de compra (S2)
│   └── consolas-cliente-ms/    # Perfil de cliente + RENIEC/SUNAT (S2)
├── infra/                    # Infraestructura que los microservicios necesitan para funcionar
│   ├── consolas-config/        # Config Server (S2, listo)
│   ├── consolas-eureka/        # Service Registry (S3, listo)
│   └── (Gateway, S4, pendiente)
├── obs/                       # Prometheus + Loki + Promtail (S3, opcional) — observa infra/ y services/ desde afuera, no es una dependencia de arranque
└── clients/                  # Frontend Angular (pendiente, desde S11)
```

## Arquitectura

Diagramas C4 (contexto y contenedores) en [`docs/index.md`](docs/index.md#arquitectura-consolas-v2026).

## Cómo ejecutar un microservicio en DEV

Cada microservicio en `services/` sigue el mismo patrón: PostgreSQL en Docker, la aplicación con Maven Wrapper desde el host.

```powershell
cd services/consolas-catalogo-ms
docker compose -f compose-dev.yml up -d
.\mvnw.cmd spring-boot:run
```

El detalle completo (puertos, variables de entorno, producción local con Docker) está en la guía de sesión correspondiente dentro de `docs/sesiones/`.

## Tecnologías

- Java 21, Spring Boot 4.0.7
- PostgreSQL, Flyway (migraciones versionadas)
- MapStruct (mapeo DTO ↔ entidad)
- Docker / Docker Compose
- Spring Cloud Config (S2), Eureka (S3), Gateway (S4)
- Angular 21 (S11)
