# Plataforma de Gestión y Prevención de Incendios - Municipalidad Valle del Sol

Este repositorio contiene la solución actual para la detección temprana, reporte geoespacial y difusión de alertas de incendios en la comuna de Valle del Sol. El sistema se basa en una arquitectura modular de microservicios con frontend React, backend Spring Boot y persistencia en PostgreSQL/PostGIS.

---

## 1. Contexto del problema

Las temporadas de alta sequía y calor extremo han aumentado los incendios forestales y de interfase urbana. El desafío principal es transformar los reportes dispersos y manuales en información estructurada y en tiempo real para brigadistas y autoridades municipales.

### Problemas que aborda la plataforma
- Reportes manuales y canales fragmentados que dificultan la precisión de coordenadas.
- Falta de visualización geoespacial centralizada.
- Comunicación ineficiente entre ciudadanos, brigadistas y administradores.
- Ausencia de historial geoespacial para análisis preventivo.

### Propuesta tecnológica actual
La plataforma ofrece:
- Reporte de focos con coordenadas GPS.
- Visualización en mapa interactivo.
- Gestión de usuarios y roles con JWT.
- Alertas en tiempo real mediante SSE.
- Persistencia de datos geoespaciales y multimedia.

---

## 2. Arquitectura general

La solución se despliega como un conjunto de servicios desacoplados:
- `backend/ms-users`: autenticación y gestión de usuarios.
- `backend/ms-reports`: reportes de incendios y gestión geoespacial.
- `backend/ms-alerts`: generación de alertas y transmisión en tiempo real.
- `backend/bff`: Backend For Frontend que orquesta llamadas entre frontend y microservicios.
- `frontend/mfe-mapeo`: aplicación React que permite visualizar y gestionar los focos.
- `postgres`: base de datos PostGIS.
- `minio-storage`: almacenamiento de archivos multimedia.

El archivo `docker-compose.yml` define el despliegue completo del stack con volúmenes persistentes y dependencias entre contenedores.

## 2.2 Diagramas de arquitectura

La plataforma se describe mediante tres diagramas principales:

- **C1 — Diagrama de Contexto:** muestra los actores externos, los límites del sistema y cómo interactúan los usuarios con los servicios del repositorio.
- **C2 — Diagrama de Contenedores:** describe los componentes desplegables principales como el frontend, el BFF, los microservicios, la base de datos y MinIO.
- **C3 — Diagrama de Componentes:** detalla la arquitectura interna de los principales servicios, incluyendo los flujos de datos y las dependencias entre capas.

### C1 — Diagrama de Contexto

![Diagrama de Contexto C1](docs/diagramas/Diagrama%20de%20Contextomod%20C1.jpg)

### C2 — Diagrama de Contenedores

![Diagrama de Contenedores C2](docs/diagramas/Diagrama%20de%20Contenedores%20C2.jpg)

### C3 — Diagrama de Componentes

![Diagrama de Componentes C3](docs/diagramas/Diagrama%20de%20Componentes%20C3.jpg)

## 2.1 Software y versiones principales

| Software | Uso | Versión / Imagen |
|---|---|---|
| Java | Runtime backend | `21` (BFF), `25` (ms-users, ms-reports, ms-alerts) |
| Spring Boot | Framework backend | `3.3.1` (BFF), `4.0.6` (ms-users, ms-reports, ms-alerts) |
| React | Frontend | `19.2.5` |
| Vite | Frontend dev/build | `8.0.10` |
| TypeScript | Frontend tipos | `5.3.3` |
| PostgreSQL/PostGIS | Base de datos geoespacial | `postgis/postgis:16-3.4-alpine` |
| MinIO | Almacenamiento de objetos | `minio/minio:latest` |
| pgAdmin | Administración de PostgreSQL | `dpage/pgadmin4:latest` |
| Docker Compose | Orquestación de servicios | definido por `docker-compose.yml` |
| Maven Wrapper | Construcción de backend | incluido en cada módulo backend |

---

## 3. Servicios y puertos

| Servicio | Descripción | Puerto local |
|---|---|---|
| `mfe-mapeo` | Frontend React/Vite - mapa de incendios y alertas | `3000` |
| `ms-bff` | BFF Spring Boot - orquesta peticiones y valida JWT | `8090` |
| `ms-users` | Auth y usuarios | `8081` |
| `ms-reports` | Reportes y PostGIS | `8082` |
| `ms-alerts` | Alertas SSE | `8083` |
| `postgres` | Base de datos Postgres/PostGIS | `5432` |
| `minio-storage` | Almacenamiento de objetos | `9000`, `9001` |
| `pgadmin` | Interfaz opcional de administración | `5050` |

---

## 4. Estructura del proyecto

```
plataforma-incendios-valle-del-sol/
├── backend/
│   ├── bff/           # API Gateway / Backend For Frontend
│   ├── demo/          # Servicio de demostración / plantilla adicional
│   ├── ms-alerts/     # Microservicio de alertas en tiempo real
│   ├── ms-reports/    # Microservicio de reportes y geodatos
│   └── ms-users/      # Microservicio de autenticación y usuarios
├── docs/              # Documentación y entregables
├── frontend/
│   └── mfe-mapeo/     # Microfrontend React + Vite
├── init-scripts/      # Scripts SQL para inicializar PostgreSQL
├── krakend/           # Configuración de gateway alternativa
├── docker-compose.yml # Orquestación del stack completo
├── private_key.pem    # Clave privada JWT (montada en ms-users)
└── public_key.pem     # Clave pública JWT (montada en ms-users)
```

---

## 5. Despliegue rápido

### Requisitos
- Docker
- Docker Compose
- OpenSSL (para generar claves RSA si no existen)

### Pasos

1. Copia el archivo de ejemplo y crea el entorno local:

```bash
cp .env.example .env
```

2. Genera o valida las claves RSA necesarias para `ms-usuarios`:

```bash
openssl genrsa -out private_key.pem 2048
openssl rsa -in private_key.pem -pubout -out public_key.pem
```

> Nota: `private_key.pem` y `public_key.pem` ya están presentes en la raíz del repositorio para que el proyecto sea plug-and-play. Si quieres rotar las claves, genera nuevas y reemplaza estos archivos.

3. Actualiza `.env` con las variables de VAPID y los valores necesarios:

```env
VAPID_PUBLIC_KEY=<tu-public-key>
VAPID_PRIVATE_KEY=<tu-private-key>
VITE_VAPID_PUBLIC_KEY=<tu-public-key>
VAPID_SUBJECT=mailto:tu-email@example.com
```

4. Inicia el stack completo:

```bash
docker compose up --build -d
```

5. Verifica que los contenedores estén activos:

```bash
docker compose ps
```

### Accesos
- Frontend: `http://localhost:3000`
- BFF/API interna: `http://localhost:8090`
- Servicio de usuarios: `http://localhost:8081`
- Servicio de reportes: `http://localhost:8082`
- Servicio de alertas: `http://localhost:8083`
- MinIO Console: `http://localhost:9001`
- pgAdmin: `http://localhost:5050`

### Nota de Docker centralizado
Usa siempre el `docker-compose.yml` de la raíz del proyecto. Los archivos `docker-compose.yml` locales dentro de los microservicios son obsoletos y no se deben ejecutar.

### Push Notifications (Resumen)
- Genera y configura las VAPID keys en `.env`.
- Registra usuarios en dos navegadores o perfiles distintos.
- Autoriza notificaciones en ambos navegadores.
- Crea una alerta o reporte desde un usuario y verifica que el otro reciba la notificación.
- Si necesitas detalles avanzados, consulta `TESTING_PUSH_NOTIFICATIONS.md`.

### Documentación complementaria
- `TESTING_PUSH_NOTIFICATIONS.md` - Guía de pruebas de push notifications.
- `backend/ms-users/KEYS_README.md` - Detalles de generación y montaje de claves RSA.
- `backend/ms-users/NOTA-DOCKER-CENTRALIZADO.md` - Indicaciones de uso del `docker-compose.yml` central.
- `docs/extras/Informe Parcial 2 - (ex readme).md` - Contexto de negocio y arquitectura extendida.

## 5.1 Despliegue Kubernetes local (Docker Desktop)

Si estás usando Docker Desktop + Kubernetes en Windows 11, el proyecto incluye un script de despliegue que construye las imágenes y aplica los manifiestos usando `kubectl`.

Pasos rápidos:

1. Asegúrate de tener Docker Desktop activado y Kubernetes habilitado.
2. Copia `.env.example` a `.env` y completa `VAPID_PUBLIC_KEY`, `VAPID_PRIVATE_KEY` y opcionalmente `VAPID_SUBJECT`.
3. Genera o copia `private_key.pem` y `public_key.pem` en la raíz del repositorio.
4. Ejecuta el script correspondiente:
   - Bash/macOS/Linux: `./scripts/deploy-k8s.sh`
   - PowerShell/Windows: `./scripts/deploy-k8s.ps1`

El script crea automáticamente los secretos `ms-usuarios-keys` y `ms-alerts-keys`, luego aplica los recursos en el namespace `plataforma-incendios`.

URLs de acceso local con NodePort:

- Frontend: `http://localhost:30080`
- BFF gateway: `http://localhost:30081`
- MinIO API: `http://localhost:30090`
- MinIO Console: `http://localhost:30091`
- BFF Swagger/OpenAPI: `http://localhost:30081/swagger-ui`

> Nota: `k8s/ingress/ingress.yaml` usa `plataforma.local`. Si deseas usarlo, agrega `127.0.0.1 plataforma.local` a tu archivo `hosts`.

---

## 6. Arquitectura y patrones aplicados

- **Microservicios** para separar responsabilidades.
- **BFF (`ms-bff`)** para centralizar la orquestación de las APIs.
- **JWT RS256** con endpoint `JWKS` para validación de tokens.
- **Postgres + PostGIS** para datos relacionales y espaciales.
- **MinIO** para persistir archivos multimedia asociados a reportes.
- **SSE** en `ms-alerts` para envío de alertas en tiempo real.
- **Docker Compose** para levantar el entorno completo con sus dependencias.

---

## 7. Flujo de uso principal

1. El usuario se registra o inicia sesión desde el frontend.
2. El frontend solicita token al microservicio de usuarios.
3. El usuario reporta un foco de incendio en el mapa.
4. `ms-reports` almacena el reporte y la información geoespacial.
5. Brigadistas o administradores pueden publicar alertas desde `ms-alerts`.
6. El frontend recibe las alertas en vivo y actualiza la interfaz.

---

## 8. Ejecución de pruebas

### Backend

```bash
cd backend/ms-users && ./mvnw test
cd backend/ms-reports && ./mvnw test
cd backend/ms-alerts && ./mvnw test
cd backend/bff && ./mvnw test
```

### Frontend

```bash
cd frontend/mfe-mapeo && npm install
cd frontend/mfe-mapeo && npm run build
```

---

## 9. Variables de entorno principales

### `ms-users`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/ms_usuarios`
- `SPRING_DATASOURCE_USERNAME=postgres`
- `SPRING_DATASOURCE_PASSWORD=postgres`
- `SERVER_PORT=8081`
- `JWT_PRIVATE_KEY_PATH=/run/keys/private_key.pem`
- `JWT_PUBLIC_KEY_PATH=/run/keys/public_key.pem`

### `ms-reports`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/ms_mapeo`
- `SERVER_PORT=8082`
- `JWK_SET_URI=http://ms-users:8081/.well-known/jwks.json`
- `MINIO_ENDPOINT=http://minio-storage:9000`
- `MINIO_EXTERNAL_URL=http://localhost:9000`
- `MINIO_ACCESS_KEY=minio_userlog`
- `MINIO_SECRET_KEY=minio_passlog`
- `MINIO_BUCKET=multimedia-reportes`

### `ms-alerts`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/ms_alerts`
- `SERVER_PORT=8083`
- `JWK_SET_URI=http://ms-users:8081/.well-known/jwks.json`
- `VAPID_PUBLIC_KEY=<one-line URL-safe base64 public key>`
- `VAPID_PRIVATE_KEY=<one-line URL-safe base64 private key>`
- `VAPID_SUBJECT=mailto:admin@example.com`

### `ms-bff`
- `SERVER_PORT=8080`
- `JWT_ISSUER=http://ms-users:8081`
- `JWK_SET_URI=http://ms-users:8081/.well-known/jwks.json`

---

## 10. Persistencia de datos

El proyecto usa volúmenes Docker para conservar los datos entre reinicios:
- `postgres_data` para la base de datos Postgres/PostGIS.
- `minio_data` para archivos multimedia almacenados en MinIO.

Si necesitas reiniciar el entorno completamente:

```bash
docker compose down -v
```

Antes de levantar el stack, copia el archivo de ejemplo de variables de entorno:

```bash
cp .env.example .env
```

Edita `.env` y completa `VAPID_PUBLIC_KEY` y `VAPID_PRIVATE_KEY` con valores válidos de Web Push.

Luego vuelve a levantar el sistema:

```bash
docker compose up --build -d
```

---

## 11. Notificaciones Push (Web Push API)

La plataforma implementa un sistema de notificaciones push que permite a los usuarios recibir alertas incluso cuando la aplicación web está cerrada.

### Cómo funcionan las notificaciones push

1. **Registro del Service Worker**: Al cargar la aplicación, el frontend registra automáticamente un service worker (`/public/service-worker.js`) que corre en segundo plano.

2. **Solicitud de permisos**: El navegador solicita permiso al usuario para mostrar notificaciones.

3. **Suscripción a push**: Si el usuario otorga permisos, el frontend:
   - Genera una suscripción push con la VAPID public key
   - Envía la suscripción al backend en `POST /api/alerts/subscriptions`
   - Incluye región y comuna del usuario (si está disponible)

4. **Almacenamiento de suscripción**: El backend almacena la suscripción en la base de datos con el endpoint, claves de cifrado y ubicación del usuario.

5. **Envío de notificaciones**: Cuando se crea una alerta:
   - El backend consulta todas las suscripciones activas que coincidan con la región/comuna de la alerta
   - Envía notificaciones push a cada suscriptor usando el servicio web-push
   - Las notificaciones aparecen en el escritorio del usuario, incluso si la app está cerrada

### Configuración requerida

Para que funcionen las notificaciones push, necesitas VAPID keys válidas:

1. **Genera las VAPID keys** (si no las tienes):
   ```bash
   # Usando Python:
   python3 -c "
   from cryptography.hazmat.primitives.asymmetric import ec
   from cryptography.hazmat.primitives import serialization
   from cryptography.hazmat.backends import default_backend
   import base64
   
   key = ec.generate_private_key(ec.SECP256R1(), default_backend())
   private_key_pem = key.private_bytes(
       encoding=serialization.Encoding.PEM,
       format=serialization.PrivateFormat.PKCS8,
       encryption_algorithm=serialization.NoEncryption()
   )
   public_key_pem = key.public_key().public_bytes(
       encoding=serialization.Encoding.PEM,
       format=serialization.PublicFormat.SubjectPublicKeyInfo
   )
   
   # Convierte a base64 URL-safe para VAPID
   # (Este es un proceso que normalmente haría una librería especializada)
   print('Private Key (PEM):', private_key_pem)
   print('Public Key (PEM):', public_key_pem)
   "
   ```

2. **Actualiza `.env`**:
   ```
   VAPID_PUBLIC_KEY=<your-base64-url-safe-public-key>
   VAPID_PRIVATE_KEY=<your-base64-url-safe-private-key>
   VITE_VAPID_PUBLIC_KEY=<same as VAPID_PUBLIC_KEY>
   VAPID_SUBJECT=mailto:tu-email@example.com
   ```

3. **Las VAPID keys se comparten entre instancias**: Si despliegas la plataforma en múltiples servidores, usa las mismas VAPID keys en todas para que las suscripciones sean válidas.

### Testing de notificaciones push

1. Abre la aplicación en una pestaña y autoriza las notificaciones
2. Abre otra pestaña con otra cuenta de usuario (en modo incógnito)
3. En la primera pestaña, crea una alerta/reporte con la región y comuna de la segunda cuenta
4. Cierra la primera pestaña
5. La segunda pestaña debe recibir la notificación (si está permitida) o verla en el historial

### Troubleshooting

- **Notificaciones no aparecen**: Verifica que:
  - Los permisos de notificación estén autorizados en el navegador
  - VAPID_PUBLIC_KEY esté configurada correctamente en `.env`
  - El service worker se registre correctamente (revisa la consola del navegador)
  - La región/comuna de la suscripción coincida con la de la alerta

- **Error "PushService not initialized"**: Verifica que VAPID keys estén configuradas en `.env`

- **Error "BouncyCastle provider not found"**: Reinicia el contenedor de ms-alerts

---

## 12. Notas adicionales

- El directorio `krakend/` contiene archivos de configuración de un gateway alternativo, pero el `docker-compose.yml` actual utiliza `backend/bff` como entrada principal.
- `backend/demo/` es un módulo de demostración que puede usarse como referencia adicional.

---

## 12. Licencia

Documentación orientada al desarrollo interno y académico de la plataforma municipal de Valle del Sol.
