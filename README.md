# NextLook

**E-commerce de moda** basado en arquitectura de microservicios.

NextLook es un sistema distribuido orientado a la venta de productos de moda. El proyecto está diseñado siguiendo principios de **arquitectura de microservicios**, con separación clara de responsabilidades, configuración centralizada, descubrimiento de servicios, seguridad distribuida y observabilidad.

> Documentación completa de arquitectura, diagramas C4, flujos de dominio y estado de avance en [`docs/index.md`](docs/index.md).

---

## Tabla de contenidos

- [Estructura del repositorio](#estructura-del-repositorio)
- [Arquitectura general](#arquitectura-general)
- [Arranque rápido](#arranque-rápido-entorno-de-desarrollo)
- [URLs principales](#urls-principales)
- [Documentación](#documentación)
- [Seguridad](#seguridad)
- [Estado de avance de esta rama](#estado-de-avance-de-esta-rama)
- [Cómo trabajar con la rama](#cómo-trabajar-con-la-rama)
- [Equipo](#equipo)

---

## Estructura del repositorio

```text
NextLook/
├── docker/          # Orquestación local de toda la infraestructura (docker-compose)
├── docs/            # Documentación de arquitectura (servida con MkDocs)
├── infra/           # Piezas de infraestructura compartida
│   ├── consolas-config/     # Config Server
│   ├── consolas-eureka/     # Service Discovery
│   └── consolas-gateway/    # API Gateway
├── obs/             # Observabilidad (Grafana, Prometheus, Promtail)
└── services/        # Microservicios de negocio
    ├── consolas-auth-ms/
    ├── consolas-catalogo-ms/
    ├── consolas-orden-ms/
    ├── consolas-cliente-ms/
    └── ...Cada carpeta principal incluye su propio README.md con el detalle de uso.Arquitectura generalEl sistema está compuesto por:








































ComponenteResponsabilidadAPI GatewayPunto único de entrada, enrutamiento y seguridadConfig ServerConfiguración centralizada de todos los serviciosEurekaRegistro y descubrimiento de instanciasauth-msAutenticación, emisión de JWT y gestión de usuarioscatalogo-msGestión de productos y categoríasorden-msCreación y consulta de órdenescliente-msGestión de clientesOtros microserviciosPago, envío, notificaciones, reseñas, inventarioComunicación
Síncrona: REST a través del Gateway (lb://nombre-servicio)
Asíncrona: eventos (planificado con Kafka en sesiones posteriores)
Descubrimiento: Eureka
Configuración: Config Server + perfiles dev / prod
Arranque rápido (entorno de desarrollo)Opción 1 – Levantar todo el stackBashdocker compose \
  -f docker/docker-compose.infra.yml \
  -f services/compose-dev.yml \
  -f obs/compose-dev.yml \
  up -d --buildEsto levanta:
Config Server
Eureka Server
API Gateway
PostgreSQL (una base de datos por microservicio)
Los microservicios de negocio
Prometheus + Grafana
Opción 2 – Solo infraestructuraBashdocker compose -f docker/docker-compose.infra.yml up -dLuego puedes ejecutar un microservicio individualmente:Bashcd services/consolas-auth-ms
./mvnw spring-boot:runImportante: desde el navegador usa siempre localhost.

Los nombres de contenedor (orden-ms, auth-ms, etc.) solo resuelven dentro de la red Docker.URLs principales







































ServicioURLDescripciónGatewayhttp://localhost:18080Punto único de accesoEurekahttp://localhost:18761Panel de instancias registradasConfig Serverhttp://localhost:18888Configuración centralizadaPrometheushttp://localhost:19090/targetsMétricasauth-mshttp://localhost:8085Autenticación y emisión de JWTMicroservicioshttp://localhost:8081 → 8088Servicios de negocioDocumentaciónPara servir la documentación completa con MkDocs:Bashdocker run --rm -it -p 8000:8000 -v "${PWD}:/docs" squidfunk/mkdocs-materialLuego abre:http://localhost:8000La documentación incluye:
Diagramas C4
Flujo de dominio
Modelo de seguridad
Fichas de cada microservicio
Estado de avance del proyecto
SeguridadModelo actualEl sistema utiliza autenticación stateless con JWT firmado con algoritmo RS256 (clave asimétrica).Componentes de seguridad




















ComponenteResponsabilidadauth-msAutentica usuarios, firma el JWT con clave privada y publica la clave públicaAPI GatewayResource Server: valida la firma del token y aplica reglas por rolMicroserviciosTambién validan el JWT por su cuenta (defensa en profundidad)Flujo de autenticación
El cliente envía email + password a POST /api/v1/auth/login
auth-ms valida las credenciales y emite un JWT firmado
El cliente envía el token en el header: Authorization: Bearer <token>
El Gateway valida la firma usando la clave pública (JWKS)
Si el token es válido y el rol tiene permiso, la petición continúa
Roles
















RolPermisos principalesADMINGestión de productos, categorías y órdenesCLIENTECrear y consultar sus propias órdenesEndpoints de autenticación
























MétodoRutaDescripciónPOST/api/v1/auth/loginIniciar sesión y obtener JWTPOST/api/v1/auth/registroRegistrar un nuevo usuario (CLIENTE)GET/.well-known/jwks.jsonClave pública para validar tokensUsuarios de prueba (datos semilla)



















EmailContraseñaRoladmin@consolas.comadmin123ADMINcliente@consolas.comcliente123CLIENTEEstado de avance de esta ramaLa rama avance-emer contiene el avance de la sesión de Seguridad distribuida y control de acceso.Completado
 Microservicio auth-ms funcional
 Tablas de usuarios, roles y relación muchos a muchos
 Login con emisión de JWT firmado (RS256)
 Endpoint de registro de usuarios
 Publicación de clave pública (JWKS)
 Gateway configurado como Resource Server
 Control de acceso por roles en el Gateway
 Validación de tokens en el Gateway
Próximos pasos
 Convertir orden-ms en Resource Server y tomar idCliente del JWT
 Proteger cliente-ms con el mismo patrón
 Reemplazo futuro de auth-ms por Keycloak (solo cambio de configuración)
Cómo trabajar con la ramaBash# Clonar el repositorio
git clone https://github.com/anghelo523/nextlook.git
cd nextlook

# Cambiar a esta rama
git checkout avance-emer

# Verificar el estado
git statusPara contribuir:Bashgit checkout -b feature/mi-cambio
# ... realizar cambios ...
git add .
git commit -m "descripción del cambio"
git push origin feature/mi-cambioLuego crear un Pull Request hacia avance-emer o main.Equipo




























IntegranteAngheloYeinsDanielJohanDocenteAbel Angel Sullon MacalupuLicenciaProyecto académico – Curso de Sistemas Distribuidos.text---

### Después de pegarlo y guardar (`Ctrl + S`):

```powershell
git add README.md
git commit -m "docs: README detallado de la rama avance-emer"
git push origin avance-emer