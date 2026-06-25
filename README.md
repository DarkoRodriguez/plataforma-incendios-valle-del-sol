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
│   ├── bff/           # API Gateway / Backend For Frontend (+ k8s/)
│   ├── demo/          # Servicio de demostración / plantilla adicional
│   ├── ms-alerts/     # Microservicio de alertas en tiempo real (+ k8s/)
│   ├── ms-reports/    # Microservicio de reportes y geodatos (+ k8s/)
│   └── ms-users/      # Microservicio de autenticación y usuarios (+ k8s/)
├── docs/              # Documentación y entregables
├── frontend/
│   └── mfe-mapeo/     # Microfrontend React + Vite (+ k8s/)
├── init-scripts/      # Scripts SQL para inicializar PostgreSQL
├── k8s/               # Infra compartida K8s (namespace, postgres, minio, ingress)
├── krakend/           # Configuración del API gateway Krakend (+ k8s/)
├── scripts/           # Scripts de despliegue (Docker Compose y Kubernetes)
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

## 5.1 Despliegue con Kubernetes (kind / Docker Desktop)

El stack se despliega en el namespace `plataforma-incendios`. Cada componente (microservicios, BFF, frontend y Krakend) incluye sus manifiestos en su carpeta `k8s/`; la infraestructura compartida (namespace, Postgres, MinIO e Ingress) vive en `k8s/` en la raíz.

**Flujo de tráfico:**

```
Navegador → Traefik (Ingress) → Frontend (mfe-mapeo)
                              → Krakend (API Gateway) → BFF → microservicios
```

Los scripts de despliegue automatizan: instalación de Traefik, construcción de imágenes Docker, creación de secrets, aplicación de manifiestos y espera de los rollouts.

---

### Requisitos previos

| Herramienta | Linux (kind) | Windows (Docker Desktop) |
|-------------|:------------:|:------------------------:|
| Docker | ✅ | ✅ |
| kubectl | ✅ | ✅ (incluido en Docker Desktop) |
| Helm 3 | ✅ | ✅ |
| kind | ✅ | No necesario |
| Kubernetes activo | Cluster kind | Settings → Kubernetes → Enable |

**Archivos obligatorios en la raíz del repositorio:**

- `.env` con claves VAPID (copiar desde `.env.example`)
- `private_key.pem` y `public_key.pem` (JWT RS256 para ms-users)

> Las claves RSA ya vienen en el repositorio para uso local. Si las rotas, regenera con OpenSSL (ver sección 5).

**Recursos recomendados:** asigna al menos **6–8 GB de RAM** a Docker. El cluster levanta Postgres, MinIO, 4 microservicios Java, Krakend, frontend y Traefik.

---

### Paso 1 — Clonar y preparar el entorno

```bash
git clone <url-del-repositorio>
cd plataforma-incendios-valle-del-sol
cp .env.example .env
```

En **Windows (PowerShell)**:

```powershell
git clone <url-del-repositorio>
cd plataforma-incendios-valle-del-sol
Copy-Item .env.example .env
```

---

### Paso 2 — Configurar variables y claves

Edita `.env` y completa las claves VAPID (necesarias para notificaciones push en `ms-alerts` y el build del frontend):

```env
VAPID_PUBLIC_KEY=<tu-clave-publica-base64-url-safe>
VAPID_PRIVATE_KEY=<tu-clave-privada-base64-url-safe>
VITE_VAPID_PUBLIC_KEY=<misma clave pública>
VAPID_SUBJECT=mailto:tu-email@example.com
```

Para generar claves VAPID puedes usar el script incluido:

```bash
python3 generate_vapid_keys.py
```

Verifica que existan las claves JWT en la raíz:

```bash
ls private_key.pem public_key.pem
```

---

### Paso 3 — Desplegar

#### Linux / macOS (kind)

Instala [kind](https://kind.sigs.k8s.io/) si no lo tienes. El primer despliegue crea el cluster y aplica todo en un solo comando:

```bash
chmod +x scripts/*.sh
./scripts/deploy-k8s.sh --setup-kind
```

El flag `--setup-kind`:

1. Crea el cluster `plataforma` con `kind-config.yaml`
2. Mapea los puertos **80**, **443**, **30090** y **30091** al host
3. Construye las imágenes `plataforma/*:latest`
4. Carga las imágenes en kind
5. Instala Traefik (si no existe)
6. Aplica todos los manifiestos (infra compartida + `k8s/` de cada componente)

**Redespliegues posteriores** (sin recrear el cluster):

```bash
./scripts/deploy-k8s.sh
```

Solo reconstruye imágenes, actualiza secrets y reaplica manifiestos. **Los datos en Postgres y MinIO se conservan** (volúmenes persistentes).

#### Windows (Docker Desktop)

1. Abre **Docker Desktop → Settings → Kubernetes → Enable Kubernetes** y espera a que el cluster esté listo.
2. Instala [Helm 3](https://helm.sh/docs/intro/install/) si no lo tienes.
3. En PowerShell, desde la raíz del proyecto:

```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned   # solo la primera vez, si PowerShell bloquea scripts
.\scripts\deploy-k8s.ps1
```

En Docker Desktop **no hace falta kind**: las imágenes que construye `docker build` las usa el cluster directamente.

---

### Paso 4 — Configurar el archivo hosts (recomendado)

Agrega esta línea para acceder también por nombre de dominio local:

**Linux / macOS** — editar `/etc/hosts`:

```
127.0.0.1 plataforma.local
```

**Windows** — editar `C:\Windows\System32\drivers\etc\hosts` (como administrador):

```
127.0.0.1 plataforma.local
```

---

### Paso 5 — Verificar el despliegue

Comprueba que todos los pods estén en estado `Running`:

```bash
kubectl config current-context
kubectl get pods -n plataforma-incendios
```

Salida esperada: `postgres`, `minio`, `ms-users`, `ms-reports`, `ms-alerts`, `ms-bff`, `krakend` y `mfe-mapeo` en **Running**. El job `minio-setup` debe aparecer como **Completed**.

Prueba rápida desde terminal:

```bash
curl -s -o /dev/null -w "Frontend: HTTP %{http_code}\n" http://localhost/
curl -s -w "Contadores: HTTP %{http_code} → " "http://localhost/api/reports/statistics/count?type=FORESTAL"
echo
```

Abre en el navegador **http://localhost** o **http://plataforma.local** y valida:

1. Registro e inicio de sesión
2. Creación de reportes en el mapa
3. Panel de administrador (contadores por tipo de incendio)
4. Activación de notificaciones push
5. Publicación de alertas (usuario brigadista o administrador)

---

### URLs de acceso

| Recurso | URL |
|---------|-----|
| Aplicación | `http://plataforma.local` o `http://localhost` |
| Swagger UI | `http://plataforma.local/swagger-ui` |
| MinIO API | `http://localhost:30090` |
| MinIO Console | `http://localhost:30091` |

---

### Comandos útiles

```bash
# Ver logs de un servicio
kubectl logs -n plataforma-incendios -l app=ms-users --tail=50
kubectl logs -n plataforma-incendios -l app=krakend --tail=50

# Reiniciar un deployment tras cambiar su manifiesto
kubectl rollout restart deployment/krakend -n plataforma-incendios

# Reconstruir imágenes y redesplegar (Linux)
./scripts/deploy-k8s.sh

# Destruir el cluster kind y empezar de cero (Linux)
kind delete cluster --name plataforma
./scripts/deploy-k8s.sh --setup-kind

# Borrar namespace y todos los recursos (conserva el cluster)
kubectl delete namespace plataforma-incendios
./scripts/deploy-k8s.sh
```

En **Windows**, para un reinicio limpio desactiva y reactiva Kubernetes en Docker Desktop, o elimina el namespace con `kubectl delete namespace plataforma-incendios` y vuelve a ejecutar `.\scripts\deploy-k8s.ps1`.

---

### Estructura de manifiestos

```
k8s/                              # Infraestructura compartida
  00-namespace.yaml
  01-04  postgres (ConfigMap, PVC, Deployment, Service)
  05-08  minio (PVC, Deployment, Service, Job de inicialización)
  09     ingress (Traefik)

backend/ms-users/k8s/             # deployment.yaml, service.yaml
backend/ms-reports/k8s/           # deployment.yaml, service.yaml
backend/ms-alerts/k8s/            # deployment.yaml, service.yaml
backend/bff/k8s/                  # deployment.yaml, service.yaml
krakend/k8s/                      # configmap.yaml, deployment.yaml, service.yaml
frontend/mfe-mapeo/k8s/           # deployment.yaml, service.yaml
```

---

### Solución de problemas frecuentes

| Síntoma | Causa probable | Qué hacer |
|---------|----------------|-----------|
| `kubectl cannot reach a Kubernetes cluster` | Cluster no creado o Docker apagado | Linux: `./scripts/deploy-k8s.sh --setup-kind`. Windows: activar Kubernetes en Docker Desktop |
| Pod en `ImagePullBackOff` | Imagen no disponible en el nodo | Linux kind: `./scripts/deploy-k8s.sh` (recarga imágenes). Windows: reconstruir con el script PS1 |
| Registro devuelve 500 | Krakend no reenvía bien respuestas vacías o 201 | Verifica que `krakend/k8s/configmap.yaml` esté actualizado y reinicia Krakend |
| Contadores del admin en 0 | Query param `type` no llega al backend | Mismo ConfigMap de Krakend; endpoint con `input_query_strings: ["type"]` |
| Push subscriptions devuelve 500 | Respuesta 200 vacía mal manejada por Krakend | Endpoints `/api/alerts/subscriptions` y `/unsubscribe` con `output_encoding: no-op` |
| Puerto 80 ocupado | Otro servicio usa el puerto | Detén nginx/Apache local o cambia `hostPort` en `kind-config.yaml` |
| Lentitud al arrancar | Varios pods Java en un solo nodo | Normal en entorno local; espera 2–5 min en el primer despliegue |

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
