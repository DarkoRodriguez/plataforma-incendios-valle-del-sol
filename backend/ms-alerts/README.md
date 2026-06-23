# ms-alerts — Servicio de alertas y notificaciones SSE

Servicio Spring Boot encargado de difundir alertas oficiales a la comunidad en tiempo real mediante Server-Sent Events (SSE).

## Ejecución

1. Iniciar PostgreSQL y la base de datos `ms_alerts`.
2. Configurar las variables de entorno o el archivo `application.yml`:
   - `SERVER_PORT=8083`
   - `JWK_SET_URI=http://ms-usuarios:8081/.well-known/jwks.json`
3. Ejecutar el servicio:

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
| Framework | Spring Boot |
| Librerías principales | Spring Web, Spring Security, Spring Data JPA, Flyway, Resilience4j |
| Patrones de diseño | Capa de servicio, Repository, Event-driven (SSE), DTO, Control de acceso por roles |

## Endpoints (`/api/alerts`)

| Método | Ruta | Autenticación | Descripción |
|---|---|---|---|
| `GET` | `/api/alerts` | Pública | Obtener todas las alertas (más recientes primero) |
| `POST` | `/api/alerts` | `BRIGADIST` / `ADMINISTRATOR` | Crear y transmitir una nueva alerta |
| `GET` | `/api/alerts/stream` | Pública | Suscribirse al stream SSE en tiempo real |

## Payload de alerta

```json
{
  "title": "Aviso de evacuación preventiva",
  "message": "Residentes del sector B deben evacuar de inmediato por la ruta norte.",
  "level": "DANGER",
  "commune": "Valle del Sol"
}
```

## Niveles de alerta

| Nivel | Significado |
|---|---|
| `INFO` | Información general |
| `WARNING` | Advertencia preventiva |
| `DANGER` | Riesgo inmediato — puede requerir evacuación |

## Stream SSE

El endpoint `/api/alerts/stream` mantiene una conexión HTTP larga con `text/event-stream`.

Al conectar, el servidor puede enviar un evento `INIT` para confirmar el canal activo. Cuando se publica una alerta autorizada, **los clientes suscritos reciben un evento `ALERT`** con el objeto `AlertDTO` en JSON.

### Ejemplo de suscripción en JavaScript

```javascript
const eventSource = new EventSource('/api/alerts/stream');

eventSource.addEventListener('ALERT', (event) => {
  const alerta = JSON.parse(event.data);
  console.log('Alerta recibida:', alerta);
});

eventSource.addEventListener('INIT', (event) => {
  console.log('Conexión SSE activa:', event.data);
});
```

## Base de datos

- Base de datos: `ms_alerts`
- Tabla principal: `alerts` con columnas `id`, `title`, `message`, `level`, `commune`, `created_at`
- Migraciones gestionadas por Flyway (`V1__init_alerts.sql`)

## Configuración

```yaml
server:
  port: 8083

security.jwt.jwks-url: ${JWK_SET_URI:http://ms-usuarios:8081/.well-known/jwks.json}
```

## Swagger / OpenAPI

- Swagger UI: `http://localhost:8083/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8083/api/alerts/v3/api-docs`

## Pruebas

```bash
./mvnw test
```

Pruebas principales:
- `AlertServiceTest`: valida la lógica de creación y manejo de alertas.
- `AlertControllerTest`: verifica los endpoints de alertas y SSE.
- `PushNotificationServiceTest`: comprueba el envío de notificaciones y el control de suscripciones.

Cobertura general:
- INSTRUCTION: 67.2%
- BRANCH: 59.4%
- LINE: 72.9%

Fuente de análisis: `target/site/jacoco/jacoco.xml`
