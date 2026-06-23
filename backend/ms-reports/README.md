# ms-mapeo — Servicio de reportes de incendios y GIS

Servicio Spring Boot para enviar, almacenar y consultar reportes de incendios con soporte geoespacial PostGIS y almacenamiento multimedia en MinIO.

## Ejecución

1. Iniciar PostgreSQL con la extensión PostGIS y la base de datos `ms_mapeo`.
2. Iniciar MinIO y el bucket `multimedia-reportes`.
3. Configurar las variables de entorno o el archivo `application.yml`:
   - `SERVER_PORT=8082`
   - `MINIO_ENDPOINT=http://localhost:9000`
   - `MINIO_ACCESS_KEY=minio_userlog`
   - `MINIO_SECRET_KEY=minio_passlog`
   - `MINIO_BUCKET=multimedia-reportes`
   - `MINIO_EXTERNAL_URL=http://localhost:9000`
   - `JWK_SET_URI=http://ms-usuarios:8081/.well-known/jwks.json`
4. Ejecutar el servicio:

```bash
./mvnw spring-boot:run
```

5. Ejecutar pruebas unitarias:

```bash
./mvnw test
```

## Tabla técnica

| Aspecto | Detalle |
|---|---|
| Lenguaje | Java |
| Framework | Spring Boot |
| Librerías principales | Spring Web, Spring Data JPA, Flyway, MinIO SDK, PostGIS, Spring Security |
| Patrones de diseño | Capa de servicio, Repository, DTO, Storage Adapter, Validación de datos |

## Endpoints (`/api/reports`)

| Método | Ruta | Autenticación | Descripción |
|---|---|---|---|
| `GET` | `/api/reports` | Pública | Listar todos los reportes de incendio |
| `POST` | `/api/reports` | Pública | Enviar un reporte en JSON |
| `POST` | `/api/reports/upload` | Pública | Enviar un reporte con archivo multimedia |
| `PUT` | `/api/reports/{id}/status` | `BRIGADIST` / `ADMINISTRATOR` | Actualizar el estado de un reporte |
| `GET` | `/api/reports/statistics/count` | Pública | Contar reportes activos por tipo |

## Payload de reporte

```json
{
  "description": "Incendio avanzando hacia zona residencial",
  "type": "FORESTAL",
  "status": "ACTIVE",
  "latitude": -33.4489,
  "longitude": -70.6693,
  "userId": 1
}
```

## Tipos de reporte

| Código | Descripción |
|---|---|
| `FORESTAL` | Incendio forestal |
| `ESTRUCTURAL` | Incendio estructural o en edificación |
| `VEHICULAR` | Incendio de vehículo |
| `OTRO` | Otro tipo de incidente |

## Estados del reporte

| Código | Descripción |
|---|---|
| `ACTIVE` | Incendio activo |
| `CONTROLLED` | Incendio controlado |
| `EXTINGUISHED` | Incendio extinguido |

## Carga de multimedia (multipart/form-data)

```bash
curl -X POST http://localhost:8082/api/reports/upload \
  -F "description=Humo visible" \
  -F "type=FORESTAL" \
  -F "status=ACTIVE" \
  -F "latitude=-33.44" \
  -F "longitude=-70.66" \
  -F "file=@foto.jpg"
```

Los archivos se almacenan en el bucket `multimedia-reportes` de MinIO y la respuesta incluye la URL de acceso.

## Procedimiento de estadísticas

El servicio dispone de un procedimiento PostgreSQL `count_active_reports_by_type(p_type TEXT)` definido en Flyway (`V2__add_statistics_procedure.sql`).

Ejemplo de consulta:

```bash
GET /api/reports/statistics/count?type=FORESTAL
```

## Base de datos

- Base de datos: `ms_mapeo`
- Columna geoespacial: `location` `Point` SRID 4326
- Migraciones Flyway: `V1__init_schema.sql`, `V2__add_statistics_procedure.sql`

## Configuración

```yaml
server:
  port: 8082

minio:
  endpoint: ${MINIO_ENDPOINT:http://localhost:9000}
  accessKey: ${MINIO_ACCESS_KEY:minio_userlog}
  secretKey: ${MINIO_SECRET_KEY:minio_passlog}
  bucketName: ${MINIO_BUCKET:multimedia-reportes}
  externalUrl: ${MINIO_EXTERNAL_URL:http://localhost:9000}
```

## Swagger / OpenAPI

- Swagger UI: `http://localhost:8082/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8082/api/reports/v3/api-docs`

security.jwt.jwks-url: ${JWK_SET_URI:http://ms-usuarios:8081/.well-known/jwks.json}
```

## Pruebas

```bash
./mvnw test
```

Pruebas principales:
- `FireReportServiceTest`: valida la lógica de reporte y actualización de estado.
- `FireReportControllerTest`: verifica los endpoints REST de reportes.
- `MinioServiceTest`: comprueba la integración con almacenamiento de archivos.
- `JwtUtilTest`: valida el parseo de JWT para la autenticación.

Cobertura general:
- INSTRUCTION: 73.5%
- BRANCH: 47.7%
- LINE: 75.3%

Fuente de análisis: `target/site/jacoco/jacoco.xml`
