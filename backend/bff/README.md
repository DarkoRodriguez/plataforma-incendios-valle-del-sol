# ms-bff — Backend for Frontend / API Gateway

Servicio Spring Cloud Gateway que actúa como punto de entrada único para el frontend. Valida tokens JWT RS256 y enruta las solicitudes hacia los microservicios correspondientes.

## Ejecución

1. Asegurarse de que los microservicios `ms-usuarios`, `ms-mapeo` y `ms-alerts` estén disponibles.
2. Configurar las variables de entorno o el archivo `application.yml`:
   - `SERVER_PORT=8080`
   - `JWT_ISSUER=http://ms-usuarios:8081`
   - `JWK_SET_URI=http://ms-usuarios:8081/.well-known/jwks.json`
3. Ejecutar el gateway:

```bash
./mvnw spring-boot:run
```

4. Ejecutar pruebas unitarias:

```bash
./mvnw test
```

## Tabla técnica

| Aspecto | Detalle |
|---|---|
| Lenguaje | Java |
| Framework | Spring Cloud Gateway |
| Librerías principales | Spring Cloud Gateway, Spring Security, Spring WebFlux, Resilience4j |
| Patrones de diseño | API Gateway, Proxy inverso, Autenticación JWT, Circuit Breaker |

## Enrutamiento principal

| Ruta frontend | Servicio destino | Descripción |
|---|---|---|
| `/api/users/**` | `http://ms-usuarios:8081` | Gestión de usuarios y autenticación |
| `/api/reports/**` | `http://ms-mapeo:8082` | Reportes de incendios y GIS |
| `/api/alerts/**` | `http://ms-alerts:8083` | Alertas comunitarias y SSE |

## Endpoints relevantes

| Método | Ruta | Autenticación | Descripción |
|---|---|---|---|
| `GET` | `/api/validate` | JWT requerido | Devuelve claims JWT decodificados para diagnóstico |

## Swagger / OpenAPI

La BFF puede agregar los OpenAPI de los servicios bajo un solo Swagger UI.

- Swagger UI: `http://localhost:8090/swagger-ui/index.html`
- Documentación agregada desde:
  - `/v3/api-docs`
  - `http://ms-usuarios:8081/api/users/v3/api-docs`
  - `http://ms-mapeo:8082/api/reports/v3/api-docs`
  - `http://ms-alerts:8083/api/alerts/v3/api-docs`

## Servicios individuales compatibles

- `ms-users`
  - Swagger UI: `http://localhost:8081/swagger-ui/index.html`
  - OpenAPI JSON: `http://localhost:8081/api/users/v3/api-docs`
- `ms-reports`
  - Swagger UI: `http://localhost:8082/swagger-ui/index.html`
  - OpenAPI JSON: `http://localhost:8082/api/reports/v3/api-docs`
- `ms-alerts`
  - Swagger UI: `http://localhost:8083/swagger-ui/index.html`
  - OpenAPI JSON: `http://localhost:8083/api/alerts/v3/api-docs`

## Configuración

```yaml
server:
  port: 8080

JWT_ISSUER: http://ms-usuarios:8081
JWK_SET_URI: http://ms-usuarios:8081/.well-known/jwks.json
```

## Pruebas

```bash
./mvnw test
```

Pruebas principales:
- `ValidateControllerTest`: valida el endpoint de verificación JWT.

Cobertura general:
- INSTRUCTION: 94.5%
- BRANCH: 75.0%
- LINE: 91.4%

Fuente de análisis: `target/site/jacoco/jacoco.xml`
