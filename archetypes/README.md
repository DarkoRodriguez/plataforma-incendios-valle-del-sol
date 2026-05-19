# Base Microservice Archetype

Este arquetipo de Maven permite generar nuevos microservicios con la estructura y configuración base estandarizada para la plataforma de la Municipalidad Valle del Sol.

## Características incluidas:
- Spring Boot 3.3.4 (o superior según configuración).
- Java 25.
- Dependencias base: WebMVC, Data JPA, Validation.
- Base de Datos: PostgreSQL.
- Migración de datos: Flyway.
- Herramientas: Lombok.

## Cómo instalar el arquetipo localmente

1. Navega a la carpeta `archetypes`.
2. Ejecuta el comando de instalación de Maven:
   ```bash
   mvn clean install
   ```

## Cómo generar un nuevo microservicio

Desde la raíz del proyecto (o donde desees crear el microservicio), ejecuta:

```bash
mvn archetype:generate \
  -DarchetypeGroupId=com.example \
  -DarchetypeArtifactId=demo-archetype \
  -DarchetypeVersion=0.0.1-SNAPSHOT \
  -DgroupId=com.example \
  -DartifactId=mi-nuevo-microservicio \
  -Dversion=0.0.1-SNAPSHOT \
  -interactiveMode=false
```

Esto generará la carpeta `mi-nuevo-microservicio` con la configuración inicial lista para comenzar el desarrollo.
