# overrides

Archivos que sobreescriben la configuración base de despliegue para un entorno o máquina concreta (ej. un `docker-compose.override.yml` que cambia puertos o rutas de volúmenes en el equipo de un integrante), sin tener que tocar los archivos base en [../../docker](../../docker) o [../../infra](../../infra).

Docker Compose aplica automáticamente cualquier `docker-compose.override.yml` que esté junto al compose base; los que viven aquí son variantes explícitas que se referencian a mano con `-f` cuando se necesitan.

Pendiente de implementar.
