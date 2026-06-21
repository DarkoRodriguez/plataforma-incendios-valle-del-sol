# Informe Técnico: Arquitectura de la Plataforma Inteligente de Gestión de Incendios

**Versión:** 1.0  
**Fecha:** 21 de junio de 2026  
**Municipalidad:** Valle del Sol

---

## Tabla de Contenidos

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Arquitectura del Sistema](#arquitectura-del-sistema)
3. [Patrones de Arquitectura](#patrones-de-arquitectura)
4. [Patrones de Diseño](#patrones-de-diseño)
5. [Modelo de Datos](#modelo-de-datos)
6. [Decisiones Críticas](#decisiones-críticas)
7. [Conclusiones](#conclusiones)

---

## Resumen Ejecutivo

La **Plataforma Inteligente de Gestión y Prevención de Incendios** es un sistema distribuido basado en microservicios diseñado para detectar, reportar, monitorear y generar alertas sobre incendios a nivel comunal. La arquitectura se ha diseñado con énfasis en **escalabilidad**, **resiliencia**, **soberanía de datos** y **tiempo real**.

### Pilares Técnicos

- **Patrón:** Microservicios con API Gateway
- **Backend:** Java/Spring Boot, Spring Cloud Gateway
- **Frontend:** React + Vite + Leaflet
- **BD:** PostgreSQL + PostGIS
- **Almacenamiento:** MinIO
- **Orquestación:** Kubernetes / Docker Compose
- **Comunicación:** REST, SSE, WebPush

---

## Arquitectura del Sistema

### 1. Diagrama C1 - Contexto del Sistema

El sistema interactúa con los siguientes actores y sistemas externos:

**Actores Principales:**
- **Ciudadanos:** reportan incendios desde aplicación web
- **Brigadistas:** responden alertas, actualizan estado de reportes
- **Administradores:** gestionan usuarios y monitorean el sistema
- **Sistemas Externos:** OpenStreetMap (tiles base), servicios web-push

**Límites del Sistema:**
```
┌─────────────────────────────────────────────────────┐
│  Plataforma Valle del Sol - Gestión de Incendios   │
├─────────────────────────────────────────────────────┤
│ - Recepción de reportes de incendio                 │
│ - Geolocalización y mapeo en tiempo real            │
│ - Difusión de alertas comunitarias                  │
│ - Historial y análisis de eventos                   │
└─────────────────────────────────────────────────────┘
       ↑                                    ↓
   Ciudadanos                        Notificaciones
   Brigadistas                       (SSE + WebPush)
```

### 2. Diagrama C2 - Contenedores

La plataforma se compone de los siguientes contenedores principales:

#### 2.1 Frontend - mfe-mapeo

**Tecnología:** React, TypeScript, Vite, Leaflet, Axios

**Responsabilidades:**
- Interfaz de usuario (SPA)
- Visualización de mapas con geolocalización
- Formulario de envío de reportes
- Suscripción a alertas en tiempo real (SSE)
- Autenticación y gestión de JWT

**Endpoint Base:** `http://localhost:3000`

#### 2.2 BFF - Backend for Frontend (API Gateway)

**Tecnología:** Spring Cloud Gateway, Resilience4j, Spring Security

**Responsabilidades:**
- Punto de entrada único (/api)
- Validación de JWT RS256
- Enrutamiento a microservicios
- Circuit Breaker (protección ante fallos)
- CORS y configuración de seguridad

**Rutas:**
```
/api/users/**     → ms-usuarios:8081
/api/reports/**   → ms-mapeo:8082
/api/alerts/**    → ms-alerts:8083
```

**Puertos:** `8080` (desarrollo), Kubernetes (producción)

#### 2.3 ms-usuarios - Servicio de Usuarios

**Tecnología:** Spring Boot, Spring Security, JPA, JWT RS256, BCrypt

**Responsabilidades:**
- Autenticación (registro/login)
- Emisión de JWT con firma RS256
- Gestión de usuarios y roles
- Exposición de JWKS para validación de tokens
- Control de acceso basado en roles (RBAC)

**BD:** PostgreSQL `ms_usuarios`

**Puertos:** `8081`

**Roles:**
- `USER`: usuario estándar
- `BRIGADIST`: bombero/brigadista
- `ADMINISTRATOR`: administrador del sistema

#### 2.4 ms-reports - Servicio de Reportes

**Tecnología:** Spring Boot, JPA, PostGIS, MinIO, Flyway

**Responsabilidades:**
- Recepción de reportes de incendio
- Almacenamiento con geolocalización
- Gestión de archivos multimedia (fotos/videos)
- Consultas espaciales (radio, proximidad)
- Notificación a ms-alerts

**BD:** PostgreSQL `ms_mapeo` (con extensión PostGIS)

**Almacenamiento:** MinIO bucket `multimedia-reportes`

**Puertos:** `8082`

**Tipos de Reporte:**
- FORESTAL
- ESTRUCTURAL
- VEHICULAR
- OTRO

#### 2.5 ms-alerts - Servicio de Alertas

**Tecnología:** Spring Boot, SSE, WebPush, JPA

**Responsabilidades:**
- Creación y difusión de alertas comunitarias
- Notificaciones en tiempo real (SSE)
- Gestión de suscripciones WebPush
- Persistencia de historial de alertas

**BD:** PostgreSQL `ms_alerts`

**Puertos:** `8083`

**Canales de Notificación:**
- Server-Sent Events (SSE) - dashboards
- Web Push - dispositivos

#### 2.6 Infraestructura Compartida

**PostgreSQL + PostGIS:**
- BD relacional con soporte geoespacial
- 3 bases de datos independientes
- Migraciones gestionadas por Flyway

**MinIO:**
- Object Storage compatible con S3
- Bucket `multimedia-reportes`
- URLs presignadas para acceso temporal

**Kubernetes / Docker Compose:**
- Orquestación de contenedores
- Service discovery
- Load balancing
- Networking

### 3. Diagrama C3 - Componentes

Ver [diagrama-c3-componentes.puml](diagrama-c3-componentes.puml) para el detalle de componentes internos.

#### Capas por Servicio

**BFF:**
- ValidateController
- JWTValidationFilter
- GatewayRoutes (Circuit Breaker)
- SecurityConfig
- CorsConfig

**ms-users:**
- AuthController, UserController, JwksController
- AuthService, UserService, JwtService
- UserRepository
- Capa de configuración

**ms-reports:**
- FireReportController
- FireReportService
- FireReportRepository
- MinioService
- PostGIS Operations

**ms-alerts:**
- AlertController
- AlertService, PushNotificationService, SSE Broadcaster
- AlertRepository, PushSubscriptionRepository

---

## Patrones de Arquitectura

### 1. Microservicios

La plataforma implementa un **enfoque de microservicios** donde cada dominio tiene su propio servicio independiente:

- **ms-users:** dominio de identidad y autenticación
- **ms-reports:** dominio de reportes y geolocalización
- **ms-alerts:** dominio de alertas y notificaciones

**Beneficios:**
- Escalabilidad independiente de cada servicio
- Equipos autónomos de desarrollo
- Despliegue independiente
- Fallos aislados (no afectan otros servicios)

### 2. API Gateway

El **BFF (Backend for Frontend)** actúa como API Gateway centralizado, proporcionando:

- **Punto de entrada único:** simplifica cliente frontend
- **Validación de seguridad:** JWT antes de llegar a servicios
- **Enrutamiento inteligente:** dirige solicitudes al microservicio correcto
- **Resiliencia:** Circuit Breaker protege contra fallos en cascada

### 3. Database per Service

Cada microservicio posee su propia base de datos PostgreSQL:

```
ms-usuarios   → BD: ms_usuarios
ms-reports    → BD: ms_mapeo (con PostGIS)
ms-alerts     → BD: ms_alerts
```

**Ventajas:**
- Sin acoplamiento a nivel de persistencia
- Libertad para elegir tecnología (PostGIS en ms-reports)
- Escalabilidad independiente
- Backups y recuperación por servicio

### 4. Event-Driven (parcial)

ms-reports notifica a ms-alerts cuando se crea un reporte crítico, usando una arquitectura **síncrona REST** con resiliencia.

**Potencial futuro:** implementar cola de eventos (RabbitMQ, Kafka) para desacoplamiento total.

---

## Patrones de Diseño

### 1. Capa de Servicio (Service Layer)

Todos los microservicios implementan:

```
Controller → Service → Repository → Database
```

**Responsabilidades:**
- **Controller:** recibe solicitudes HTTP, valida entrada
- **Service:** encapsula lógica de negocio, orquestación
- **Repository:** acceso a datos con JPA
- **Database:** persistencia

### 2. Repository Pattern

Uso de **Spring Data JPA** para abstracción de acceso a datos:

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(Role role);
}
```

**Beneficios:** fácil testeo, reutilización, cambios de BD sin afectar lógica.

### 3. DTO (Data Transfer Object)

Separación entre entidades JPA y DTOs expuestos en API:

```java
// Entidad interna
@Entity
class User { ... }

// DTO para API
class UserDTO {
    String username;
    String email;
    String role;
}
```

**Beneficios:** seguridad (no expone campos internos), versionado de API.

### 4. Factory / Builder

Instanciación controlada de DTOs y respuestas:

```java
// Conversión Entity → DTO
UserDTO userDTO = new UserDTO();
userDTO.setUsername(user.getUsername());
userDTO.setRole(user.getRole());
```

### 5. Strategy Pattern - Autenticación

En ms-usuarios:

```java
interface AuthStrategy {
    boolean authenticate(String username, String password);
}

class PasswordAuthStrategy implements AuthStrategy { ... }
class OAuthStrategy implements AuthStrategy { ... } // futuro
```

### 6. Adapter Pattern

**MinioService:** abstrae la complejidad del cliente S3

```java
public class MinioService {
    public void uploadFile(String bucket, String key, InputStream data) { ... }
    public String getPresignedUrl(String bucket, String key) { ... }
}
```

### 7. Circuit Breaker

Resilience4j en BFF para proteger llamadas inter-servicio:

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        slidingWindowSize: 10
        failureRateThreshold: 50%
        waitDurationInOpenState: 50s
```

### 8. SSE (Server-Sent Events)

En ms-alerts para notificaciones en tiempo real:

```java
@GetMapping("/stream")
public SseEmitter stream() {
    SseEmitter emitter = new SseEmitter();
    emitterRegistry.register(emitter);
    return emitter;
}
```

### 9. Global Exception Handler

Manejo centralizado de excepciones:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException e) {
        return ResponseEntity.status(404).body(new ErrorResponse("Usuario no encontrado", ...));
    }
}
```

---

## Modelo de Datos

### Entidades Principales

#### 1. User (ms-usuarios)

```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  email VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  phone VARCHAR(20),
  region VARCHAR(100),
  commune VARCHAR(100),
  role VARCHAR(20) DEFAULT 'USER',
  active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT now(),
  updated_at TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);
```

**Relaciones:**
- 1:N con PushSubscription (en ms-alerts)

#### 2. FireReport (ms-reports)

```sql
CREATE TABLE fire_reports (
  id BIGSERIAL PRIMARY KEY,
  description TEXT,
  type VARCHAR(50) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  location GEOMETRY(POINT, 4326),
  region VARCHAR(100),
  commune VARCHAR(100),
  user_id BIGINT NOT NULL,
  media_url TEXT,
  report_date TIMESTAMP DEFAULT now(),
  created_at TIMESTAMP DEFAULT now(),
  updated_at TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_fire_reports_location ON fire_reports USING GIST(location);
CREATE INDEX idx_fire_reports_status ON fire_reports(status);
CREATE INDEX idx_fire_reports_commune ON fire_reports(commune);
```

**Características:**
- `location` como tipo geométrico PostGIS (índice GIST)
- Permite consultas espaciales: radios, intersecciones, proximidad
- GeoJSON compatible

#### 3. Alert (ms-alerts)

```sql
CREATE TABLE alerts (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  message TEXT NOT NULL,
  level VARCHAR(20) DEFAULT 'INFO',
  region VARCHAR(100),
  commune VARCHAR(100),
  creator_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_alerts_level ON alerts(level);
CREATE INDEX idx_alerts_created_at ON alerts(created_at DESC);
```

#### 4. PushSubscription (ms-alerts)

```sql
CREATE TABLE push_subscriptions (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  endpoint VARCHAR(500) NOT NULL UNIQUE,
  keys_p256dh VARCHAR(255),
  keys_auth VARCHAR(255),
  created_at TIMESTAMP DEFAULT now(),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_push_subscriptions_user_id ON push_subscriptions(user_id);
```

### Diagrama ER Conceptual

```
    User (ms-usuarios)
      ├─ id (PK)
      ├─ username
      ├─ email
      ├─ role
      └─ ...

    FireReport (ms-reports)
      ├─ id (PK)
      ├─ user_id (FK → User)
      ├─ location (GEOMETRY)
      ├─ type
      ├─ status
      └─ media_url

    Alert (ms-alerts)
      ├─ id (PK)
      ├─ title
      ├─ level
      ├─ created_at
      └─ ...

    PushSubscription (ms-alerts)
      ├─ id (PK)
      ├─ user_id (FK → User)
      ├─ endpoint
      └─ keys (p256dh, auth)
```

### Estrategia de Indexing

**Índices por Performance:**

- **FireReport.location:** GIST index para consultas espaciales
- **FireReport.status:** B-tree para filtros comunes
- **FireReport.commune:** B-tree para búsquedas regionales
- **Alert.created_at DESC:** para ordenamiento temporal
- **PushSubscription.user_id:** para búsquedas por usuario
- **User.username/email:** UNIQUE indexes para integridad

---

## Decisiones Críticas

### 1. Arquitectura Microservicios vs Monolito

**Decisión:** Microservicios

**Justificación:**
- Escalabilidad independiente: ms-reports puede recibir picos sin afectar ms-usuarios
- Resiliencia: fallo en ms-alerts no derriba el sistema de reportes
- Equipos autónomos: cada equipo deploya independientemente

**Trade-offs:**
- Complejidad operacional: múltiples servicios, orquestación, monitoreo
- Latencia: llamadas inter-servicio vs llamadas en memoria

### 2. PostgreSQL + PostGIS para Geolocalización

**Decisión:** PostgreSQL con extensión PostGIS

**Justificación:**
- Consultas espaciales nativas (POINT, POLYGON, RADIUS)
- Índices GIST optimizados para geometría
- Almacenamiento relacional seguro
- GeoJSON compatible para frontend

**Alternativa rechazada:** MongoDB con geospatial indexes
- Razón: PostgreSQL es más maduro, mejor integridad referencial, PostGIS es superior

### 3. On-Premise con Kubernetes

**Decisión:** Infraestructura on-premise con Kubernetes y Docker

**Justificación:**
- **Soberanía de datos:** datos municipales bajo control local
- **Cumplimiento:** RGPD, GDPR (datos no salen del municipio)
- **Costos:** sin transferencia de datos a la nube
- **Disponibilidad:** control sobre SLA

**Alternativa rechazada:** Saas (AWS, Google Cloud)
- Razón: privacidad municipal, requisitos normativos

### 4. SSE + WebPush para Notificaciones

**Decisión:** Dual-channel: SSE para web, WebPush para dispositivos

**Justificación:**
- **SSE:** conexión persistente para dashboards en tiempo real, bajo latency
- **WebPush:** notificaciones a dispositivos, funciona offline, soporte nativo navegadores

**Alternativa rechazada:** Solo WebSockets
- Razón: más complejo, SSE es suficiente, WebPush es estándar

### 5. JWT RS256 para Autenticación

**Decisión:** JWT con firma RS256 (asimétrica)

**Justificación:**
- **Escalabilidad:** validación descentralizada (cada servicio verifica con JWKS público)
- **Seguridad:** clave privada nunca se comparte
- **JWKS Endpoint:** exposición de claves públicas de forma estándar

**Alternativa rechazada:** JWT HS256
- Razón: secreto compartido es riesgo, RS256 es industria estándar

### 6. MinIO para Multimedia

**Decisión:** MinIO (S3-compatible) para almacenamiento de objetos

**Justificación:**
- **Compatibilidad:** protege contra vendor lock-in
- **URLs presignadas:** acceso temporal sin exponer credenciales
- **Escalabilidad:** separación de almacenamiento de BD
- **On-premise:** MinIO se despliega en Kubernetes

**Alternativa rechazada:** Almacenar BLOBs en PostgreSQL
- Razón: impacta performance, no es escalable

### 7. Flyway para Migraciones

**Decisión:** Flyway (versionado de esquema)

**Justificación:**
- **Reproducibilidad:** mismo DDL en dev/test/prod
- **Rollback:** versiones anteriores documentadas
- **Auditoría:** historial de cambios
- **CI/CD:** integración con pipeline

### 8. Resilience4j Circuit Breaker

**Decisión:** Resilience4j (no Hystrix, que es EOL)

**Justificación:**
- **Actualidad:** mantenimiento activo
- **Lightweight:** bajo overhead
- **Métricas:** integración con Micrometer
- **Configurable:** sliders, timers personalizables

### 9. Gitflow Simplificado

**Decisión:** Gitflow: main (producción), develop (integración), feature/* (desarrollo)

**Justificación:**
- **Estabilidad:** main siempre deployable
- **Integración:** develop es estable pero bajo testing
- **Aislamiento:** feature branches no interfieren

---

## Seguridad

### Autenticación y Autorización

**Flujo:**
1. Usuario inicia sesión: `POST /api/users/auth/login`
2. ms-usuarios valida credenciales (BCrypt password hash)
3. Se emite JWT RS256 con claims (sub, aud, role)
4. Frontend almacena JWT en localStorage
5. Cada solicitud incluye `Authorization: Bearer <JWT>`
6. BFF valida JWT contra JWKS de ms-usuarios
7. Services consultan claims para RBAC

**Roles:**
- `USER`: crear reportes
- `BRIGADIST`: crear/actualizar alertas, cambiar estado reportes
- `ADMINISTRATOR`: gestión de usuarios, cambiar roles

### Cifrado en Tránsito

- HTTPS obligatorio (TLS 1.3 en producción)
- Certificados autofirmados en desarrollo

### Cifrado en Reposo

- Contraseñas: BCrypt (no reversible)
- Tokens: JWT (firmados, no cifrados)
- Multimedia: URL presignada (acceso temporal)

### CORS

```javascript
// BFF permite frontend origin
CorsConfiguration config = new CorsConfiguration();
config.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
config.setAllowCredentials(true);
```

---

## Observabilidad

### Health Checks

- Spring Boot Actuator: `/actuator/health`
- Circuit Breaker: `/actuator/circuitbreakerevents`

### Logging

- Logs centralizados en consola (Development)
- ELK Stack o similar (recomendado para Producción)

### Métricas

- Micrometer (integrado en Spring Boot)
- Prometheus scraping (opcional)

---

## Performance y Escalabilidad

### Limitaciones Actuales

- **Single PostgreSQL:** botella en write-heavy workloads
- **No caching:** Redis podría mejorar read performance
- **SSE stateful:** conexiones en memoria en cada instancia

### Recomendaciones Futuras

1. **Redis Cache:** cachear usuarios, alertas frecuentes
2. **CQRS:** separar reads/writes en reportes
3. **Event Sourcing:** auditoría y recuperación
4. **Message Queue:** Kafka para desacoplamiento de servicios
5. **Database Replication:** read replicas para ms-reports (GeoCaching)

---

## Conclusiones

La **Plataforma Inteligente de Gestión de Incendios** implementa una arquitectura moderna, escalable y resiliente:

✅ **Fortalezas:**
- Microservicios permiten escalabilidad independiente
- PostGIS habilita consultas geoespaciales avanzadas
- JWT asimétrico permite validación descentralizada
- On-premise respeta soberanía de datos

⚠️ **Desafíos:**
- Complejidad operacional de múltiples servicios
- Necesidad de monitoreo y observabilidad
- Potencial bottleneck en PostgreSQL bajo carga

🚀 **Próximos Pasos:**
1. Implementar monitoring/alerting (Prometheus + Grafana)
2. Load testing para identificar límites
3. Documentación operativa para despliegue
4. Plan de backup y recuperación
5. Considerar event bus (Kafka) si volumen crece

---

**Aprobado por:** Equipo Técnico  
**Fecha:** 21 de junio de 2026  
**Estado:** Vigente
