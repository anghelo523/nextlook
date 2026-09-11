# nextlook-config

Spring Cloud Config Server de NextLook. Es la fuente única de configuración (dev/prod) para todos los demás componentes: no cada microservicio lee su propio `application.yml` local, sino que se lo pide a este servidor por HTTP al arrancar.

- **Puerto:** `18888`
- **Perfil activo:** `native` (lee configuración de archivos locales en vez de un repositorio Git remoto)
- **Fuentes de configuración** (ver `src/main/resources/application.yml`, propiedad `search-locations`):
  - `../config-repo/application.yml` — config compartida por todos los clientes.
  - `../nextlook-eureka/config/` y `../nextlook-gateway/config/` — config propia de esos dos componentes de infra.
  - `../../services/{application}/config/` — config propia de cada microservicio (Spring sustituye `{application}` por el `spring.application.name` de quien pide la config; ej. `catalogo-ms` busca en `services/catalogo-ms/config/`).

## Cómo correrlo

```
mvn spring-boot:run
```

o vía Docker, usando [../../docker/docker-compose.infra.yml](../../docker/docker-compose.infra.yml) (servicio `nextlook-config`), que monta las tres carpetas de config como volúmenes de solo lectura.

## Probarlo

```
curl http://localhost:18888/catalogo-ms/dev
```

Debe devolver el contenido combinado de `services/catalogo-ms/config/catalogo-ms-dev.yml` + `config-repo/application.yml`.
