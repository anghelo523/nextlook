# Brief técnico

!!! success "Aprobado 27/08/2026"
    Brief técnico aprobado por el docente **Abel Angel Sullon Macalupu** el 27/08/2026.

## Equipo

| Integrante | Microservicios a cargo |
|---|---|
| Anghelo | `orden-ms`, `catalogo-ms` |
| Yeins | `inventario-ms`, `resenas-ms` |
| Daniel | `pago-ms`, `cliente-ms` |
| Johan | `envio-ms`, `notificaciones-ms` |

## Dominio del proyecto

**Nombre:** NextLook — Arquitectura de Microservicios para E-Commerce de Moda.

**Reto de negocio:** un e-commerce de moda necesita consultar catálogo y variantes, gestionar órdenes, controlar inventario en tiempo real y procesar pagos de forma segura, sin que un componente sobrecargado bloquee a los demás.

**Flujo de dominio:** Catálogo → Selección → Orden → Reserva de stock → Pago → Confirmación.

**Integración externa real:** Mercado Pago (Sandbox).

## Microservicios previstos

**Tabla 1. Ficha de microservicios**

| Microservicio | Responsable | Tipo | Descripción |
|---|---|---|---|
| `orden-ms` | Anghelo | Transaccional | Registra órdenes, calcula totales, coordina reserva de stock y pago. |
| `catalogo-ms` | Anghelo | No transaccional | Administra prendas, categorías y variantes de talla/color. |
| `inventario-ms` | Yeins | Transaccional | Gestiona stock, movimientos y reservas asociadas a órdenes. |
| `resenas-ms` | Yeins | No transaccional | Administra reseñas y calificaciones de productos. |
| `pago-ms` | Daniel | Transaccional | Integra la API real de Mercado Pago y registra transacciones. |
| `cliente-ms` | Daniel | No transaccional | Perfiles de compradores y direcciones de envío. |
| `envio-ms` | Johan | Transaccional | Coordina el envío de las órdenes y el seguimiento de la entrega. |
| `notificaciones-ms` | Johan | No transaccional | Avisos de orden, pago e inventario vía eventos. |

## Comunicación entre servicios

Ver el detalle completo en [Comunicación entre servicios](comunicacion.md). Resumen:

- **Síncrona (REST/Feign):** `orden-ms` ↔ `cliente-ms`, `envio-ms` ↔ `cliente-ms`, `resenas-ms` ↔ `catalogo-ms`, `resenas-ms` ↔ `cliente-ms`, `pago-ms` ↔ Mercado Pago.
- **Asíncrona (eventos):** `orden-ms` → `inventario-ms`, `pago-ms` → `orden-ms` / `inventario-ms`, `orden-ms` → `envio-ms`, y `orden-ms`, `pago-ms`, `inventario-ms`, `envio-ms` → `notificaciones-ms`.

## Seguridad y protección por rol

Keycloak actúa como Identity Provider centralizado y emite JWT. Cada microservicio valida el token de forma independiente (no hay un componente central que intercepte cada request por el equipo).

**Tabla 2. Roles**

| Rol | Puede |
|---|---|
| `CLIENTE` | Crear órdenes, gestionar su propio perfil, sus direcciones y sus reseñas. |
| `ADMIN` | Gestión global de catálogo, inventario, clientes y notificaciones. |

Más detalle en [Seguridad](seguridad.md).

## Alcance

Resumen rápido — detalle completo en [Alcance](alcance.md).

**Sí cubre:** catálogo de prendas con categorías y variantes; perfiles de cliente y direcciones de envío; órdenes con detalle, cálculo de totales y estado; verificación y reserva transaccional de stock; pagos reales mediante Mercado Pago (Sandbox); gestión de envíos y notificaciones del flujo; reseñas y calificaciones de producto; seguridad centralizada con Keycloak, JWT y roles.

**No cubre (fuera de alcance, explícito):** devoluciones físicas de prendas; chat en vivo para atención al cliente; facturación electrónica oficial con SUNAT; integración con empresas de transporte reales; recomendaciones basadas en inteligencia artificial.
