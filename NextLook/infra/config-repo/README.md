# config-repo

Configuración **compartida** que el [Config Server](../nextlook-config) aplica a todos los clientes que le piden configuración, sin importar cuál sean (microservicio, Eureka o Gateway).

- `application.yml`: config global por defecto (ej. propiedades comunes a todo el sistema). Cualquier propiedad específica de un componente va en su propia carpeta (`../nextlook-eureka/config`, `../nextlook-gateway/config`, `../../services/<nombre>/config`), no aquí.
