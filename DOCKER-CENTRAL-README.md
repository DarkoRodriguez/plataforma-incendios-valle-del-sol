# 🔥 Plataforma Incendios - Valle del Sol

Plataforma de gestión de incendios con arquitectura de microservicios desplegada con Docker Compose.

## 📊 Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                    DOCKER COMPOSE                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ MS-USUARIOS  │  │  MS-MAPEO    │  │    DEMO      │     │
│  │   :8081      │  │   :8082      │  │   :8083      │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
│         │                 │                 │              │
│         └─────────────────┼─────────────────┘              │
│                           │                                │
│                    ┌──────▼────────┐                       │
│                    │  PostgreSQL   │                       │
│                    │  (Port 5432)  │                       │
│                    ├───────────────┤                       │
│                    │ ms_usuarios   │                       │
│                    │ ms_mapeo      │                       │
│                    │ demo_db       │                       │
│                    └───────────────┘                       │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │            PGADMIN (Port 5050)                       │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 Inicio Rápido

### 1. Requisitos Previos

- Docker
- Docker Compose
- 5GB de espacio en disco (aproximadamente)

### 2. Clonar el Repositorio

```bash
git clone <repo-url>
cd plataforma-incendios-valle-del-sol
```

### 3. Iniciar la Plataforma

```bash
# Opción A: Usar el script maestro (Recomendado)
chmod +x docker.sh
./docker.sh up

# Opción B: Usar docker-compose directamente
docker-compose up --build -d
```

### 4. Verificar Servicios

```bash
./docker.sh status
# o
docker-compose ps
```

## 🔗 Acceso a Servicios

| Servicio | URL | Puerto | Descripción |
|----------|-----|--------|-------------|
| **MS-USUARIOS** | http://localhost:8081 | 8081 | Gestión de usuarios |
| **MS-MAPEO** | http://localhost:8082 | 8082 | Servicio de mapeo |
| **DEMO** | http://localhost:8083 | 8083 | Aplicación de demo |
| **PgAdmin** | http://localhost:5050 | 5050 | Administrador de BD |
| **PostgreSQL** | localhost | 5432 | Base de datos |

## 🗄️ Credenciales

### PostgreSQL
```
Usuario: postgres
Contraseña: postgres
```

### PgAdmin
```
Email: admin@example.com
Contraseña: admin
```

## 📋 Comandos del Script Maestro

```bash
./docker.sh up                    # Iniciar servicios
./docker.sh down                  # Detener servicios
./docker.sh restart               # Reiniciar servicios
./docker.sh status                # Ver estado
./docker.sh logs ms-usuarios      # Ver logs de un servicio
./docker.sh db ms_usuarios        # Acceder a base de datos
./docker.sh clean                 # Limpieza completa
./docker.sh help                  # Mostrar ayuda
```

## 🐳 Comandos de Docker Compose

```bash
# Iniciar servicios
docker-compose up --build -d

# Ver estado
docker-compose ps

# Ver logs
docker-compose logs -f ms-usuarios
docker-compose logs -f postgres

# Detener servicios
docker-compose down

# Detener y eliminar volúmenes
docker-compose down -v

# Reiniciar un servicio
docker-compose restart ms-usuarios
```

## 🗄️ Gestión de Bases de Datos

### Acceder via Docker

```bash
# Opción 1: Script maestro
./docker.sh db ms_usuarios

# Opción 2: docker-compose
docker-compose exec postgres psql -U postgres -d ms_usuarios

# Opción 3: PgAdmin
# Abrir http://localhost:5050
# Email: admin@example.com
# Contraseña: admin
```

### Bases de Datos Creadas Automáticamente

```sql
-- Creadas en init-scripts/01-create-databases.sql
ms_usuarios    -- Microservicio de usuarios
ms_mapeo       -- Microservicio de mapeo
demo_db        -- Base de datos de demostración
```

## 📊 Estructura del Proyecto

```
plataforma-incendios-valle-del-sol/
├── docker-compose.yml          # Configuración centralizada
├── docker.sh                   # Script maestro
├── init-scripts/
│   └── 01-create-databases.sql # Crear BDs automáticamente
├── ms-usuarios/
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/
│   └── src/main/resources/db/migration/  # Migraciones Flyway
├── ms-mapeo/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── demo/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
└── README.md
```

## 🔧 Configuración de Servicios

### MS-USUARIOS

- **Puerto**: 8081
- **BD**: ms_usuarios
- **Migraciones**: Flyway (src/main/resources/db/migration/)
- **Variables de Entorno**:
  ```
  SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/ms_usuarios
  SPRING_DATASOURCE_USERNAME=postgres
  SPRING_DATASOURCE_PASSWORD=postgres
  ```

### MS-MAPEO

- **Puerto**: 8082
- **BD**: ms_mapeo
- **Variables de Entorno**:
  ```
  SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/ms_mapeo
  SPRING_DATASOURCE_USERNAME=postgres
  SPRING_DATASOURCE_PASSWORD=postgres
  ```

### DEMO

- **Puerto**: 8083
- **BD**: demo_db
- **Variables de Entorno**:
  ```
  SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/demo_db
  SPRING_DATASOURCE_USERNAME=postgres
  SPRING_DATASOURCE_PASSWORD=postgres
  ```

## 🚨 Troubleshooting

### Problema: Puertos en uso

```bash
# Ver qué está usando un puerto
lsof -i :8081
lsof -i :5432

# Cambiar puertos en docker-compose.yml
# Ejemplo: "8081:8081" → "8091:8081"
```

### Problema: Base de datos no accesible

```bash
# Verificar que PostgreSQL está listo
./docker.sh logs postgres

# Verificar conexión
docker-compose exec postgres pg_isready -U postgres
```

### Problema: Migraciones no se ejecutan

```bash
# Ver logs de la aplicación
./docker.sh logs ms-usuarios

# Verificar archivos de migración
ls -la ms-usuarios/src/main/resources/db/migration/
```

### Limpiar completamente

```bash
# Eliminar todo: contenedores, volúmenes, imágenes
./docker.sh clean

# O manualmente
docker-compose down -v
docker system prune -a
```

## 📚 APIs Disponibles

### MS-USUARIOS

#### Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "demo",
  "password": "demo"
}
```

**Respuesta:**
```json
{
  "id": 1,
  "username": "demo"
}
```

#### Obtener Usuario
```bash
GET /api/users/{id}
```

#### Actualizar Usuario
```bash
PUT /api/users/{id}
Content-Type: application/json

{
  "username": "nuevo_nombre"
}
```

## 🔐 Seguridad

### Notas Importantes

- **Credenciales por Defecto**: Cambiar en producción
- **Red Compartida**: Los servicios usan la red `plataforma-network`
- **Volumen Persistente**: Los datos de PostgreSQL se guardan en `postgres_data`

### Mejoras Recomendadas para Producción

```yaml
# Variables de entorno segurizadas
environment:
  POSTGRES_PASSWORD: ${DB_PASSWORD}  # Usar variables de entorno
  
# Límites de recursos
resources:
  limits:
    cpus: '1'
    memory: 512M
  
# Reinicio automático
restart_policy:
  condition: on-failure
  delay: 5s
  max_attempts: 3
```

## 📈 Escalabilidad

### Agregar Nuevo Microservicio

1. **Crear carpeta del servicio**
```bash
mkdir ms-nuevo
cd ms-nuevo
```

2. **Agregar Dockerfile**
```dockerfile
FROM maven:3.9-eclipse-temurin-25 as build
# ... configuración ...
EXPOSE 8084
```

3. **Actualizar docker-compose.yml**
```yaml
ms-nuevo:
  build:
    context: ./ms-nuevo
    dockerfile: Dockerfile
  container_name: ms-nuevo
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/ms_nuevo
    SERVER_PORT: 8084
  ports:
    - "8084:8084"
  depends_on:
    postgres:
      condition: service_healthy
  networks:
    - plataforma-network
```

4. **Crear BD en init-scripts**
```sql
CREATE DATABASE IF NOT EXISTS ms_nuevo;
```

5. **Reiniciar servicios**
```bash
./docker.sh clean
./docker.sh up
```

## 📝 Logs y Debugging

```bash
# Ver todos los logs
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f ms-usuarios

# Ver últimas 100 líneas
docker-compose logs --tail 100 ms-usuarios

# Ver logs con timestamp
docker-compose logs -f --timestamps ms-usuarios
```

## 🔄 CI/CD

Para integración continua, puedes usar:

```bash
# Build de imágenes
docker-compose build

# Push a registro (ej: Docker Hub)
docker tag ms-usuarios:latest tu-usuario/ms-usuarios:latest
docker push tu-usuario/ms-usuarios:latest

# Deployar
docker-compose up -d
```

## 📄 Licencia

Propietario

## 🤝 Soporte

Para reportar problemas o sugerencias, contactar al equipo de desarrollo.

---

**Última actualización**: 8 de mayo de 2026  
**Versión**: 1.0.0
