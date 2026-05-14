# MS-USUARIOS - Microservicio de Gestión de Usuarios

Microservicio de autenticación y gestión de usuarios con Spring Boot, PostgreSQL y Flyway.

## 📋 Prerrequisitos

- Docker y Docker Compose
- Java 25 (si ejecutas localmente sin Docker)
- Maven 3.9+ (si compilas localmente)

## 🚀 Inicio Rápido con Docker

### 1. Opción Automática (Recomendado)

```bash
cd ms-usuarios
chmod +x start.sh
./start.sh
```

### 2. Opción Manual

```bash
cd ms-usuarios
docker-compose up --build -d
```

## 🛑 Detener Servicios

```bash
./start.sh down
# o
docker-compose down
```

## 📝 Comandos Disponibles

### Usar el script start.sh:

```bash
./start.sh up       # Iniciar servicios
./start.sh down     # Detener servicios
./start.sh logs ms-usuarios  # Ver logs de la app
./start.sh logs postgres     # Ver logs de la BD
./start.sh restart  # Reiniciar servicios
./start.sh clean    # Limpiar todo (contenedores, volúmenes, imágenes)
```

### Usar docker-compose directamente:

```bash
docker-compose up --build -d      # Iniciar
docker-compose down                # Detener
docker-compose logs -f ms-usuarios # Ver logs
docker-compose ps                  # Estado de servicios
```

## 🗄️ Configuración de Base de Datos

**PostgreSQL:**
- Host: localhost (desde host) o postgres (desde Docker)
- Puerto: 5432
- Usuario: postgres
- Contraseña: postgres
- BD: ms_usuarios

**Migraciones Flyway:**
- Ubicación: `src/main/resources/db/migration/`
- V1: Crear tabla `users`
- V2: Insertar datos iniciales (usuario demo/demo)

## 🔗 Endpoints API

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login con usuario/contraseña |
| GET | `/api/users/{id}` | Obtener datos del usuario |
| PUT | `/api/users/{id}` | Actualizar usuario |

### Ejemplo de Login:

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "demo",
    "password": "demo"
  }'
```

Respuesta exitosa:
```json
{
  "id": 1,
  "username": "demo"
}
```

## 📊 Acceso a la Base de Datos

### Via Docker:

```bash
# Acceder a la consola de PostgreSQL
docker-compose exec postgres psql -U postgres -d ms_usuarios

# Dentro de psql
SELECT * FROM users;
```

### Via localhost (si tienes PostgreSQL instalado):

```bash
psql -h localhost -U postgres -d ms_usuarios
```

## 🔧 Configuración

### Variables de Entorno (.env):

```env
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=ms_usuarios
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/ms_usuarios
SERVER_PORT=8081
```

### application.properties:

```properties
spring.application.name=ms-usuarios
spring.datasource.url=jdbc:postgresql://localhost:5432/ms_usuarios
spring.flyway.enabled=true
spring.jpa.hibernate.ddl-auto=validate
server.port=8081
```

## 🐛 Troubleshooting

### Problema: Puerto 5432 ya en uso
```bash
# Ver qué está usando el puerto
lsof -i :5432

# Usar otro puerto en docker-compose.yml
# Cambiar: "5432:5432" a "5433:5432"
```

### Problema: Error de conexión a BD
```bash
# Verificar estado de contenedores
docker-compose ps

# Ver logs de PostgreSQL
./start.sh logs postgres
```

### Problema: Migraciones no se ejecutan
```bash
# Verificar que los archivos están en lugar correcto
ls src/main/resources/db/migration/

# Revisar logs de la aplicación
./start.sh logs ms-usuarios
```

### Limpiar completamente y reiniciar:
```bash
./start.sh clean
./start.sh up
```

## 📁 Estructura del Proyecto

```
ms-usuarios/
├── src/main/java/com/example/ms_usuarios/
│   ├── auth/              # Estrategias de autenticación
│   ├── config/            # Configuración
│   ├── controller/        # Controladores REST
│   ├── dto/               # Data Transfer Objects
│   ├── model/             # Entidades JPA
│   ├── repository/        # Acceso a datos
│   └── service/           # Lógica de negocio
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/      # Scripts SQL de Flyway
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── start.sh
```

## 📚 Tecnologías

- **Framework:** Spring Boot 4.0.6
- **BD:** PostgreSQL 15
- **ORM:** Hibernate/JPA
- **Migraciones:** Flyway
- **Build:** Maven
- **Container:** Docker & Docker Compose
- **Java:** 25

## 🤝 Notas de Desarrollo

- Las migraciones se ejecutan automáticamente al iniciar la aplicación
- El usuario demo/demo se crea automáticamente en la primera ejecución
- Hibernate valida el schema contra las migraciones
- El servicio espera a que PostgreSQL esté listo antes de iniciar

## 📄 Licencia

Propietario
