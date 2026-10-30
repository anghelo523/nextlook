# frontend — NextLook Web (S11)

SPA Angular 22 (standalone, signals, zoneless) que habla **solo con el Gateway** (`http://localhost:18080`) e inicia sesión en Keycloak con **Authorization Code + PKCE** (cliente `nextlook-web`, sin *password grant*).

```
frontend/
├── crear-frontend.ps1      # ng new nextlook-web + copia el código de abajo
└── nextlook-web-src/src/   # código propio (app/, environments/, styles.css)
```

## Levantar

1. Backend completo arriba (`docker compose -f docker/docker-compose.infra.yml -f services/compose-dev.yml up -d --build`). Si Keycloak ya estaba corriendo antes de S11, recréalo para que importe el realm nuevo: `docker compose -f docker/docker-compose.infra.yml up -d --force-recreate keycloak`.
2. `cd frontend; pwsh ./crear-frontend.ps1`
3. `cd nextlook-web; npm start` → http://localhost:4200

Usuarios: `cliente@nextlook.pe` / `cliente123` (talla preferida M), `cliente2@nextlook.pe` / `cliente123` (L), `admin@nextlook.pe` / `admin123`.

## Pantallas

| Ruta | Rol | Qué hace |
|---|---|---|
| `/` | público | Inicio (muestra la talla preferida si hay sesión) |
| `/catalogo` | público | Prendas por categoría; elige talla/color (preselecciona `talla_preferida`) |
| `/bolsa` | CLIENTE | Bolsa en memoria → `POST /api/v1/ordenes`, muestra el estado REAL (201 `PENDIENTE_STOCK` / 202 `CARRITO`) y el `X-Trace-ID` |
| `/mis-pedidos` | CLIENTE | Seguimiento con *polling* adaptativo, botón Pagar solo con `LISTO_PARA_PAGAR` |
| `/perfil` | CLIENTE | Registro del perfil y dirección en `cliente-ms` |
| `/admin/pagos` | ADMIN | Todos los pagos |

Prueba rápida de Keycloak/CORS sin navegador: `pwsh scripts/verificar-s11.ps1`.
