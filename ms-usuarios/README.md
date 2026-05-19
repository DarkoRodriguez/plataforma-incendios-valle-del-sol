# MS-Usuarios (Microservicio de Usuarios)

Gestiona la información de usuarios, autenticación básica y perfiles de los ciudadanos y brigadistas.

## Requisitos previos
- Java 25
- Maven
- Base de datos PostgreSQL.

## Instalación y Ejecución

1. Navegar al directorio `ms-usuarios`.
2. Asegurar que la base de datos PostgreSQL está en funcionamiento (recomendado: usar `docker-compose`).
3. Ejecutar la aplicación:
   ```bash
   mvn spring-boot:run
   ```
El servicio estará escuchando en el puerto `8081`.

## Tecnologías y Patrones
- **Spring Boot Data JPA**: Persistencia.
- **Flyway**: Migraciones de base de datos.
- **Patrones**: ControllerAdvice (Manejo de Errores Global), Repository, DTO.

## Pruebas
Para ejecutar las pruebas unitarias:
```bash
mvn test
```
