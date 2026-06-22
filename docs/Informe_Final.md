Informe final — Plataforma "Valle del Sol" (lectura ~10 minutos)

Resumen ejecutivo
- Propósito: plataforma microservicios para detección, reporte, monitoreo y alertas de incendios a nivel comunal.
- Alcance: cobertura técnica (C1/C2/C3), patrones de diseño aplicados, modelo de datos y decisiones críticas (seguridad, despliegue, comunicaciones).

1. Arquitectura (Vistas C1, C2, C3)

1.1 C1 — Diagrama de contexto
- Actores: Ciudadanos (reportes), Brigadistas (respuesta), Operadores/Administradores (gestión), Sistemas externos (OpenStreetMap tiles, push/web-push).
- Interacciones principales:
  - Ciudadano -> Frontend/BFF -> API Gateway -> Servicios (Reportes, Alertas, Usuarios).
  - ms-reports y ms-alerts se comunican mediante REST y notificaciones web push para coordinar alertas en tiempo real.
- Objetivo: dejar claros límites del sistema, dependencias externas y canales de entrada/salida.

1.2 C2 — Diagrama de contenedores
- API Gateway / BFF: punto de acceso central, autenticación JWT, enrutamiento y resiliencia con Circuit Breaker.
- Frontend: `mfe-mapeo` usa React + Vite + Leaflet con mapas base OpenStreetMap.
- Microservicios reales:
  - ms-users: gestión de usuarios, autenticación, roles y emisión de JWT con JWKS.
  - ms-reports: recepción de reportes, persistencia espacial en PostgreSQL+PostGIS, subida de multimedia a MinIO y notificación a ms-alerts.
  - ms-alerts: creación de alertas, persistencia y difusión en tiempo real por SSE + notificaciones web-push.
- Infra: PostgreSQL (PostGIS), MinIO para objetos multimedia, Kubernetes para orquestación y Docker Compose para desarrollo local.

1.3 C3 — Diagrama de componentes (ejemplo: `ms-reports`)
- Componentes internos:
  - Controller layer: expone REST/multipart endpoints (ya delegando lógica a servicios).
  - Service layer: encierra reglas de negocio (validación, autorización, creación de entidades, notificaciones internas y lógica de estado).
  - Repository layer: JPA + Queries/PostGIS; uso de consultas espaciales y almacenamiento relacional.
  - Integration adapters: MinIO client, Web Push client (`nl.martijndwars:web-push`), RestTemplate para invocar `ms-alerts` desde `ms-reports`.
- Razonamiento: separar responsabilidades facilita pruebas, despliegue y permite reemplazar adaptadores sin tocar la lógica de negocio.

2. Patrones de diseño aplicados
- API Gateway Pattern: acceso central y control de seguridad/resiliencia.
- Database per Service: límites claros, cada servicio selecciona la mejor tecnología (PostGIS donde corresponde).
- Repository Pattern: desacopla acceso a datos de la lógica de negocio con JPA/DAOs.
- Circuit Breaker: patrón de resiliencia usado en el BFF para proteger llamadas a microservicios vulnerables.
- Factory/Builder (mínimo): instanciación controlada de DTOs/entidades en servicios (más claro para tests).
- Frontend independiente: la aplicación `mfe-mapeo` es un cliente React/Vite especializado en mapeo y reportes.

3. Modelo de datos (resumen)
- Entidades principales:
  - User (id, username, email, role, region, commune, phone, hashed_password)
  - FireReport (id, description, type, status, location POINT(lat,lng), region, commune, user_id, media_url, created_at)
  - Alert (id, title, message, level, region, commune, created_at)
  - PushSubscription (id, user_id, endpoint, keys, created_at)
- Decisiones clave:
  - Almacenar `location` como tipo espacial (`geometry` / `geography`) en PostGIS para poder indexar y ejecutar consultas espaciales (radius search, containment, distance).
  - Multimedia: almacenar en MinIO con referencia URL en la entidad (no BLOB en la BD).
  - Historial/Analytics: datos de reportes y alertas almacenados en PostgreSQL para análisis histórico y consulta operativa.

4. Decisiones críticas y justificación
- On-Premise con Kubernetes y MinIO
  - Justificación: soberanía de datos municipal, cumplimiento normativo y reducción de costos por transferencia de datos.
- PostGIS para datos espaciales
  - Justificación: consultas espaciales eficientes (proximidad, intersecciones) y soporte de GeoJSON para frontend.
- Comunicación interna: REST entre microservicios y notificaciones web push para la difusión en tiempo real.
- Notificaciones: SSE + Push (web-push con VAPID)
  - SSE para dashboards y conexiones en tiempo real; web-push para notificaciones a dispositivos.
- Resiliencia: el BFF usa Circuit Breaker para evitar cascadas de fallos cuando `ms-users`, `ms-reports` o `ms-alerts` no responden.
- Seguridad: JWT + JWKS
  - El sistema usa JWT para autenticación y exposición de JWKS por `ms-users` para validación por otros servicios. Roles (RBAC) limitan acciones sensibles (enviar alertas, cambiar estados).

5. Aspectos de seguridad, privacidad y ética
- Datos sensibles: ubicación y multimedia cifrados en tránsito (HTTPS) y en reposo cuando aplique.
- RBAC: roles `USER`, `BRIGADIST`, `ADMINISTRATOR` para autorizar operaciones críticas.
- Minimización de datos: almacenar sólo metadatos y URLs para multimedia.
- Gobernanza: políticas de retención y acceso, logs de auditoría para cambios de estado y envíos de alertas.

6. Operaciones y despliegue
- Despliegue: Docker Compose para desarrollo local y manifiestos Kubernetes para orquestación on-premise.
- Observabilidad: Actuator health y salud de Circuit Breaker en el BFF, junto con logs estándar de Spring Boot.
- Backups: copias periódicas de Postgres y MinIO con snapshots y procedimientos de recuperación.

7. Estrategia de Branching
Para el desarrollo de la plataforma se recomienda una estrategia de branching basada en un flujo estilo **Gitflow** adaptado a un monorepositorio con múltiples componentes.

7.1 Ramas principales
- `main`: rama de producción. Contiene el código estable y listo para desplegar.
- `develop`: rama de integración. Aquí confluyen las nuevas funcionalidades y cambios antes de preparar una versión para `main`.

7.2 Ramas de soporte
- `feature/nombre-de-la-feature`: creadas desde `develop` para implementar nuevas funcionalidades, por ejemplo `feature/ms-usuarios` o `feature/mfe-mapeo`.
- `bugfix/nombre-del-bug`: creadas desde `develop` o `main` para corregir errores detectados durante el desarrollo.
- `release/x.y.z`: opcionales para estabilizar versiones antes de mergear a `main` y `develop`.
- `hotfix/nombre-del-hotfix`: creadas desde `main` para solucionar incidentes críticos en producción y luego fusionadas hacia `main` y `develop`.

7.3 Flujo de trabajo
1. Crear la rama `feature` desde `develop`.
2. Desarrollar con commits atómicos y mensajes claros.
3. Abrir un Pull Request hacia `develop` cuando la característica esté lista.
4. Revisar el código, validar pruebas unitarias y resolver conflictos.
5. Hacer merge a `develop` tras la aprobación.
6. Cuando `develop` esté estable, preparar un `release` o abrir un PR hacia `main` y crear un tag de versión.

7.4 Gestión de conflictos
Si aparece un conflicto durante la integración, la rama de la característica debe sincronizarse con `develop`, resolver los conflictos localmente y actualizar el PR.

8. Extractos relevantes de informes previos (resumen breve)
- Requisitos funcionales: detección/reporte, mapeo en tiempo real, sistema de alertas y almacenamiento histórico.
- Patrones ya identificados: API Gateway, Database per Service, Repository Pattern, Circuit Breaker, Frontend independiente.
- Tecnologías implementadas: Java/Spring Boot, PostgreSQL+PostGIS, MinIO, SSE/web-push, Spring Cloud Gateway MVC, React/Vite/Leaflet, Docker Compose/Kubernetes.

8. Riesgos y mitigaciones
- Riesgo: exposición de datos de ubicación -> Mitigación: cifrado, control de accesos y anonimización para análisis.
- Riesgo: picos de tráfico en emergencias -> Mitigación: HPA en Kubernetes, buen dimensionamiento de la base de datos y gestión de conexiones.
- Riesgo: pérdida de datos multimedia -> Mitigación: replicación en MinIO/backup y políticas de retención.

9. Conclusión y siguientes pasos
- El sistema propuesto cubre los requerimientos funcionales y no funcionales: resiliencia, escalabilidad y soberanía de datos.
- Siguientes pasos recomendados:
  1. Validación de la API y contratos (OpenAPI) entre servicios.
  2. Implementación de un pipeline CI/CD mínimo y pruebas de carga para el flujo de reportes.
  3. Prueba de integración de notificaciones (SSE + Web Push) y validación de latencias.
  4. Documentación operativa para despliegue On-Premise y plan de backups.
  5. Evaluar el uso de eventos asíncronos en una fase posterior si la plataforma crece.

Apéndice: Endpoints relevantes
- `POST /api/users/auth/register` — registrar un usuario.
- `POST /api/users/auth/login` — autenticación y emisión de JWT.
- `GET /.well-known/jwks.json` — JWKS para validación de tokens.
- `GET /api/users` — listado de usuarios (requiere rol ADMINISTRATOR).
- `GET /api/users/{id}` — obtener datos públicos de un usuario.
- `PUT /api/users/{id}` — actualizar datos de usuario autenticado.
- `PUT /api/users/{id}/role` — actualizar rol de usuario (requiere ADMINISTRATOR).
- `POST /api/reports` — crear reporte sin archivo.
- `POST /api/reports/upload` — crear reporte con archivo multipart.
- `GET /api/reports` — listar todos los reportes.
- `PUT /api/reports/{id}/status` — actualizar estado del reporte (requiere rol BRIGADIST/ADMINISTRATOR).
- `POST /api/alerts` — publicar alerta comunal (requiere rol BRIGADIST/ADMINISTRATOR).
- `POST /api/alerts/subscriptions` — registrar suscripción de push.
- `GET /api/alerts/stream` — conexión SSE para recibir alertas en tiempo real.
- `POST /api/alerts/internal/report-notification` — endpoint interno para notificación de reportes desde `ms-reports`.

Ejemplo de migración/DDL mínima (PostGIS):

CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE fire_reports (
  id BIGSERIAL PRIMARY KEY,
  description TEXT,
  type VARCHAR(50),
  status VARCHAR(20),
  location GEOMETRY(POINT,4326),
  region VARCHAR(100),
  commune VARCHAR(100),
  user_id BIGINT,
  media_url TEXT,
  report_date TIMESTAMP,
  created_at TIMESTAMP DEFAULT now()
);

- Recomendaciones de formato y entregable:
  - Incluir diagramas C1/C2/C3 en anexos (SVG o PNG). Puedes añadirlos directamente al `.docx` cuando lo descargues.
  - Mantener la sección de decisiones críticas y riesgos tal cual para la evaluación.

---
Generaré ahora el archivo `doc/Informe_Final.docx` con todo lo anterior.
¿Qué prefieres a continuación? (generar `.docx` / acortar a 2 páginas / añadir diagramas y ERD)