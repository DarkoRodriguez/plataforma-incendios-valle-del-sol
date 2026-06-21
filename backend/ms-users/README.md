# ms-usuarios — Servicio de usuarios y autenticación

Servicio Spring Boot responsable de registrar usuarios, autenticar, emitir JWT y gestionar roles.

## Ejecución

1. Iniciar PostgreSQL y la base de datos `ms_usuarios`.
2. Configurar las variables de entorno o el archivo `application.yml`:
   - `SERVER_PORT=8081`
   - `JWT_PRIVATE_KEY_PATH=/run/keys/private_key.pem`
   - `JWT_PUBLIC_KEY_PATH=/run/keys/public_key.pem`
   - `JWT_ISSUER=http://ms-usuarios:8081`
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
| Librerías principales | Spring Web, Spring Security, Spring Data JPA, Flyway, BCrypt, JWT/RSA |
| Patrones de diseño | Capa de servicio, Repository, DTO, Autenticación JWT, RBAC |

## Endpoints

### Autenticación (`/api/users/auth`)

| Método | Ruta | Autenticación | Descripción |
|---|---|---|---|
| `POST` | `/api/users/auth/register` | Pública | Registrar un nuevo usuario |
| `POST` | `/api/users/auth/login` | Pública | Iniciar sesión y recibir un token JWT |
| `GET` | `/.well-known/jwks.json` | Pública | Exponer las claves públicas para validar JWT |

### Gestión de usuarios (`/api/users`)

| Método | Ruta | Autenticación | Descripción |
|---|---|---|---|
| `GET` | `/api/users` | `ADMINISTRATOR` | Listar todos los usuarios registrados |
| `GET` | `/api/users/{id}` | Pública | Obtener perfil de usuario por ID |
| `PUT` | `/api/users/{id}` | Propietario o `ADMINISTRATOR` | Actualizar datos de usuario |
| `PUT` | `/api/users/{id}/role` | `ADMINISTRATOR` | Cambiar rol de usuario |

## Ejemplo de payload de registro

```json
{
  "username": "juan.perez",
  "password": "secure123",
  "email": "juan@valledelsol.cl",
  "phone": "+56912345678",
  "region": "Metropolitana",
  "commune": "Valle del Sol",
  "role": "USER"
}
```

## Configuración

```yaml
server:
  port: 8081

JWT_PRIVATE_KEY_PATH: /run/keys/private_key.pem
JWT_PUBLIC_KEY_PATH: /run/keys/public_key.pem
JWT_ISSUER: http://ms-usuarios:8081
```

## Pruebas

```bash
./mvnw test
```

Pruebas principales: `UserControllerTest`, `AuthControllerTest`.
