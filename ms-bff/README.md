# MS-BFF (Backend For Frontend / API Gateway)

Este microservicio actúa como punto único de entrada para el frontend de la plataforma. Está construido con Spring Cloud Gateway.

## Requisitos previos
- Java 25 (o Java 21 LTS)
- Maven

## Instalación y Ejecución

1. Navegar al directorio `ms-bff`.
2. Asegurarse de que los microservicios internos (`ms-usuarios` y `ms-mapeo`) estén ejecutándose, o levantarlos mediante Docker Compose.
3. Ejecutar la aplicación:
   ```bash
   mvn spring-boot:run
   ```
El API Gateway estará escuchando en el puerto `8080`.

## Tecnologías y Patrones
- **Spring Cloud Gateway MVC**: Enrutamiento de peticiones.
- **Resilience4j**: Patrón Circuit Breaker implementado para garantizar resiliencia ante fallos en los microservicios de backend.
