# NextLook

E-commerce de moda basado en microservicios. Ver [docs/index.md](docs/index.md) para la arquitectura completa (diagramas C4, flujo de dominio, seguridad, comunicación entre servicios).

## Estructura del repositorio

```
NextLook/
├── docker/     # Orquestación local de toda la infraestructura (docker-compose)
├── docs/       # Documentación de arquitectura (servida con MkDocs)
├── infra/      # Piezas de infraestructura compartida: Config Server, Eureka, Gateway
├── obs/        # Observabilidad (Grafana, Prometheus, Promtail) y overrides de despliegue
└── services/   # Los 8 microservicios de negocio, cada uno con su propio código y config
```

Cada carpeta principal tiene su propio `README.md` con el detalle de qué contiene y cómo se usa.

## Arranque rápido (entorno de desarrollo)

```
docker compose \
  -f docker/docker-compose.infra.yml \
  -f services/compose-dev.yml \
  -f obs/compose-dev.yml \
  up -d --build
```

Levanta el Config Server, Eureka, el Gateway, una base Postgres por microservicio, Keycloak, los 8 microservicios (como esqueleto bootable, ver [Estado de avance](docs/avance.md)) y Prometheus.

| Servicio | URL |
|---|---|
| Gateway | http://localhost:18080 |
| Eureka | http://localhost:18761 |
| Config Server | http://localhost:18888 |
| Prometheus | http://localhost:19090/targets |
| catalogo-ms / inventario-ms / resenas-ms / orden-ms / pago-ms / cliente-ms / envio-ms / notificaciones-ms | 8081-8088 |

También se puede levantar solo la infra (sin microservicios) con `docker compose -f docker/docker-compose.infra.yml up -d`, y correr un microservicio suelto localmente con `mvn spring-boot:run` apuntando a esa infra ya dockerizada.

> **Desde el navegador, siempre `localhost`, nunca el nombre del contenedor.** `http://orden-ms:8084/...` solo resuelve *dentro* de la red de Docker (por eso Eureka y Prometheus lo usan entre ellos); desde tu PC es `http://localhost:8084/...`.

## Documentación

```
docker run --rm -it -p 8000:8000 -v "${PWD}:/docs" squidfunk/mkdocs-material
```

Sirve la documentación completa (arquitectura, seguridad, cada microservicio, estado de avance) en http://localhost:8000.

## Identidades y relaciones

Vista técnica actual (complementa el [C4 de dominio](docs/arquitectura.md), que todavía no dibuja Gateway/Eureka/Config Server por estar "pendientes de confirmar" en el brief — acá ya son reales):

```mermaid
flowchart TB
    Cliente(["Cliente"])
    Keycloak["Keycloak<br/>(Identity Provider)"]
    Gateway["nextlook-gateway<br/>:18080"]
    Eureka["nextlook-eureka<br/>:18761"]
    Config["nextlook-config<br/>:18888"]
    MercadoPago["Mercado Pago<br/>(Sandbox)"]

    Cliente -->|"1. login"| Keycloak
    Cliente -->|"2. request + JWT"| Gateway
    Gateway -->|"3. resuelve destino"| Eureka

    subgraph IdentidadCliente["Identidad y Cliente"]
        ClienteMS["cliente-ms :8086"]
        ClienteDB[("cliente_db")]
        ClienteMS --> ClienteDB
    end

    subgraph CatalogoInventario["Catálogo e Inventario"]
        CatalogoMS["catalogo-ms :8081"]
        CatalogoDB[("catalogo_db")]
        InventarioMS["inventario-ms :8082"]
        InventarioDB[("inventario_db")]
        ResenasMS["resenas-ms :8083"]
        ResenasDB[("resenas_db")]
        CatalogoMS --> CatalogoDB
        InventarioMS --> InventarioDB
        ResenasMS --> ResenasDB
    end

    subgraph OrdenPago["Órdenes y Pago"]
        OrdenMS["orden-ms :8084"]
        OrdenDB[("orden_db")]
        PagoMS["pago-ms :8085"]
        PagoDB[("pago_db")]
        OrdenMS --> OrdenDB
        PagoMS --> PagoDB
    end

    subgraph EnvioNotif["Envío y Notificaciones"]
        EnvioMS["envio-ms :8087"]
        EnvioDB[("envio_db")]
        NotifMS["notificaciones-ms :8088"]
        NotifDB[("notificaciones_db")]
        EnvioMS --> EnvioDB
        NotifMS --> NotifDB
    end

    Gateway -->|"/api/v1/**"| ClienteMS
    Gateway -->|"/api/v1/**"| CatalogoMS
    Gateway -->|"/api/v1/**"| InventarioMS
    Gateway -->|"/api/v1/**"| ResenasMS
    Gateway -->|"/api/v1/**"| OrdenMS
    Gateway -->|"/api/v1/**"| PagoMS
    Gateway -->|"/api/v1/**"| EnvioMS
    Gateway -->|"/api/v1/**"| NotifMS

    ClienteMS -.->|"registro / config"| Eureka
    CatalogoMS -.->|"registro / config"| Eureka
    InventarioMS -.->|"registro / config"| Eureka
    ResenasMS -.->|"registro / config"| Eureka
    OrdenMS -.->|"registro / config"| Eureka
    PagoMS -.->|"registro / config"| Eureka
    EnvioMS -.->|"registro / config"| Eureka
    NotifMS -.->|"registro / config"| Eureka
    Eureka -.-> Config

    OrdenMS -->|"REST: valida cliente"| ClienteMS
    EnvioMS -->|"REST: obtiene dirección"| ClienteMS
    ResenasMS -->|"REST: valida producto"| CatalogoMS
    ResenasMS -->|"REST: valida compra"| ClienteMS
    PagoMS -->|"REST"| MercadoPago

    OrdenMS -.->|"evento: Orden Creada"| InventarioMS
    PagoMS -.->|"evento: resultado pago"| OrdenMS
    PagoMS -.->|"evento: resultado pago"| InventarioMS
    OrdenMS -.->|"evento: Pago Aprobado"| EnvioMS
    OrdenMS -.->|"evento"| NotifMS
    PagoMS -.->|"evento"| NotifMS
    InventarioMS -.->|"evento"| NotifMS
    EnvioMS -.->|"evento"| NotifMS

    classDef infra fill:#eef6ff,stroke:#2b6cb0,stroke-width:2px,color:#111;
    classDef external fill:#fff3cd,stroke:#b7791f,stroke-width:2px,color:#5f370e;
    class Gateway,Eureka,Config infra;
    class Keycloak,MercadoPago external;
```

Flechas continuas = síncrono (REST) o ruteo; flechas punteadas = eventos asíncronos (todavía sin tecnología de mensajería elegida, ver [Comunicación](docs/comunicacion.md)) y registro/descubrimiento en Eureka.

## Modelo entidad-relación

> ⚠️ **Propuesta, no un modelo confirmado.** Ningún microservicio tiene entidades implementadas todavía (la sección "Entidades" de cada [ficha en docs/microservicios](docs/microservicios/orden-ms.md) sigue en TODO). Este diagrama es un punto de partida razonable a partir de lo que el [brief](docs/brief.md) sí describe (qué administra cada servicio), para que el equipo lo discuta y ajuste, no un esquema ya acordado.

Cada microservicio es dueño de su propia base — no hay FK real entre servicios (líneas punteadas abajo = referencia lógica por id, resuelta por REST/eventos, no por JOIN):

```mermaid
erDiagram
    CLIENTE ||--o{ DIRECCION : tiene
    CLIENTE {
        uuid id PK
        string nombre
        string email
        string rol
    }
    DIRECCION {
        uuid id PK
        uuid cliente_id FK
        string calle
        string ciudad
        boolean predeterminada
    }

    CATEGORIA ||--o{ PRODUCTO : agrupa
    PRODUCTO ||--o{ VARIANTE : tiene
    CATEGORIA {
        uuid id PK
        string nombre
    }
    PRODUCTO {
        uuid id PK
        uuid categoria_id FK
        string nombre
        string descripcion
        decimal precio_base
    }
    VARIANTE {
        uuid id PK
        uuid producto_id FK
        string talla
        string color
        string sku
    }

    VARIANTE ||--o| STOCK : "referencia (otro servicio)"
    STOCK ||--o{ RESERVA_STOCK : tiene
    STOCK {
        uuid id PK
        uuid variante_id "ref. catalogo-ms"
        int cantidad_disponible
    }
    RESERVA_STOCK {
        uuid id PK
        uuid stock_id FK
        uuid orden_id "ref. orden-ms"
        int cantidad
        string estado
    }

    PRODUCTO ||--o{ RESENA : "referencia (otro servicio)"
    CLIENTE ||--o{ RESENA : "referencia (otro servicio)"
    RESENA {
        uuid id PK
        uuid producto_id "ref. catalogo-ms"
        uuid cliente_id "ref. cliente-ms"
        int calificacion
        string comentario
    }

    CLIENTE ||--o{ ORDEN : "referencia (otro servicio)"
    ORDEN ||--o{ ITEM_ORDEN : tiene
    ORDEN {
        uuid id PK
        uuid cliente_id "ref. cliente-ms"
        string estado
        decimal total
        datetime fecha
    }
    ITEM_ORDEN {
        uuid id PK
        uuid orden_id FK
        uuid variante_id "ref. catalogo-ms"
        int cantidad
        decimal precio_unitario
    }

    ORDEN ||--o| PAGO : "referencia (otro servicio)"
    PAGO {
        uuid id PK
        uuid orden_id "ref. orden-ms"
        decimal monto
        string estado
        string mercado_pago_id
    }

    ORDEN ||--o| ENVIO : "referencia (otro servicio)"
    DIRECCION ||--o{ ENVIO : "referencia (otro servicio)"
    ENVIO {
        uuid id PK
        uuid orden_id "ref. orden-ms"
        uuid direccion_id "ref. cliente-ms"
        string estado
        datetime fecha_envio
    }

    CLIENTE ||--o{ NOTIFICACION : "referencia (otro servicio)"
    NOTIFICACION {
        uuid id PK
        uuid cliente_id "ref. cliente-ms"
        string tipo
        string mensaje
        boolean leido
    }
```

## Roadmap

```mermaid
flowchart TD
    F0["Fase 0 — Infra base ✅<br/>Config Server, Eureka, Gateway,<br/>Postgres x8, Keycloak, Prometheus"]
    F1["Fase 1 — Núcleo del dominio 🟡<br/>catalogo-ms + orden-ms"]
    F2["Fase 2 — Resto de microservicios 🟡<br/>cliente, envio, inventario,<br/>notificaciones, pago, resenas"]
    F3["Fase 3 — Seguridad ⬜<br/>JWT por microservicio + TLS Keycloak"]
    F4["Fase 4 (propuesta) 🟣<br/>Mensajería asíncrona"]
    F5["Fase 5 — Integración de pago ⬜<br/>Mercado Pago Sandbox"]
    F6["Fase 6 (propuesta) 🟣<br/>Observabilidad completa"]
    F7["Fase 7 — Stack completo ⬜<br/>infra + microservicios + Kafka + observabilidad"]

    F0 --> F1 --> F2 --> F3 --> F4 --> F5 --> F6 --> F7

    classDef done fill:#d4f4dd,stroke:#2f9e57,stroke-width:2px,color:#0b3d1f;
    classDef progress fill:#fff3cd,stroke:#b7791f,stroke-width:2px,color:#5f370e;
    classDef pending fill:#eef0f2,stroke:#8a94a3,stroke-width:2px,color:#333;
    classDef proposed fill:#e6e6fa,stroke:#6a5acd,stroke-width:2px,stroke-dasharray: 4 3,color:#2e2e5c;

    class F0 done;
    class F1,F2 progress;
    class F3,F5,F7 pending;
    class F4,F6 proposed;
```

Fases 1, 2, 3, 5, 7 confirmadas (vienen de comentarios ya presentes en `services/*/config/*.yml`); 4 y 6 son propuesta a confirmar con el equipo. Detalle completo, con lo que falta en cada una: [docs/avance.md](docs/avance.md).

## Equipo

Anghelo, Yeins, Daniel, Johan — Docente: Abel Angel Sullon Macalupu.
