# MS-Mapeo (Microservicio de Mapeo)

Este microservicio gestiona el registro y almacenamiento geoespacial de los focos de incendio reportados.

## Requisitos previos
- Java 25
- Maven
- Base de datos PostgreSQL con extensión PostGIS. (Se recomienda usar `docker-compose up` desde la raíz).

## Instalación y Ejecución

1. Navegar al directorio `ms-mapeo`.
2. Ejecutar la aplicación:
   ```bash
   mvn spring-boot:run
   ```
El servicio estará escuchando en el puerto `8082`.
Si se ejecuta localmente, asegúrese de tener configurado PostGIS en su base de datos local o de haber levantado el contenedor de Docker.

## Tecnologías y Patrones
- **PostGIS & hibernate-spatial**: Manejo nativo de datos espaciales.
- **Flyway**: Migraciones automáticas de base de datos.
- **Patrones**: Repository, DTO.
