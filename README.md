# 🔥 Valle del Sol — Fire Focus Management Platform

**Municipalidad Valle del Sol** — Intelligent platform for wildfire detection, geographic monitoring, and community alert broadcasting.

---

## 📐 Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Browser (React SPA)                      │
│  mfe-mapeo  •  Leaflet Map  •  Dashboard  •  Alert Feed    │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTP / SSE  (port 3000 → nginx)
                       ▼
┌──────────────────────────────────────────────────────────────┐
│               ms-bff  (Spring Cloud Gateway)                 │
│                        port 8080                             │
│  /api/users/**  →  ms-usuarios                              │
│  /api/reports/**→  ms-mapeo                                 │
│  /api/alerts/** →  ms-alerts                                │
└───────┬────────────────┬─────────────────┬──────────────────┘
        │                │                 │
        ▼                ▼                 ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────────┐
│ ms-usuarios  │ │   ms-mapeo   │ │    ms-alerts     │
│   port 8081  │ │  port 8082   │ │    port 8083     │
│  JWT RS256   │ │  PostGIS     │ │  SSE Broadcast   │
│  JWKS .json  │ │  MinIO media │ │  Community Alerts│
└──────┬───────┘ └──────┬───────┘ └──────┬───────────┘
       │                │                 │
       └────────────────┴─────────────────┘
                        │
               ┌────────▼────────┐
               │   PostgreSQL    │
               │  (PostGIS 16)   │
               │  ms_usuarios    │
               │  ms_mapeo       │
               │  ms_alerts      │
               └─────────────────┘
                        │
               ┌────────▼────────┐
               │  MinIO Storage  │
               │  port 9000/9001 │
               │  multimedia-    │
               │  reportes       │
               └─────────────────┘
```

---

## 📦 Services

| Service | Technology | Port | Responsibility |
|---|---|---|---|
| `mfe-mapeo` | React 19 + TypeScript + Leaflet | 3000 | Interactive map, fire reporting, community alerts, dashboard |
| `ms-bff` | Spring Cloud Gateway + OAuth2 RS256 | 8080 | API Gateway, JWT validation, route proxying |
| `ms-usuarios` | Spring Boot 3 + JPA + BCrypt | 8081 | Registration, login, JWKS endpoint, user/role management |
| `ms-mapeo` | Spring Boot 3 + PostGIS + MinIO | 8082 | Fire focus reports, geospatial storage, media uploads, statistics |
| `ms-alerts` | Spring Boot 3 + SSE | 8083 | Alert creation, Server-Sent Events real-time broadcast |
| `postgres` | PostGIS 16 Alpine | 5432 | Shared persistent database |
| `minio-storage` | MinIO | 9000/9001 | Object storage for uploaded photos/videos |
| `pgadmin` | pgAdmin 4 | 5050 | Optional DB administration UI |

---

## 🚀 Quick Start

### Prerequisites
- Docker ≥ 24 and Docker Compose ≥ 2.20
- RSA key pair for JWT signing (see below)

### 1. Generate RSA Key Pair

```bash
# In the project root
openssl genrsa -out private_key.pem 2048
openssl rsa -in private_key.pem -pubout -out public_key.pem
```

### 2. Create Databases Init Script

The `init-scripts/01-create-databases.sql` file is already configured. On first run PostgreSQL will automatically create all databases.

### 3. Start All Services

```bash
docker compose up --build -d
```

Services start in the correct dependency order:  
`postgres` → `ms-usuarios` + `ms-mapeo` + `ms-alerts` → `ms-bff` + `minio-storage` → `mfe-mapeo`

### 4. Access the Platform

| Resource | URL |
|---|---|
| **Frontend App** | http://localhost:3000 |
| **Swagger UI (all APIs)** | http://localhost:8090/swagger-ui/index.html |
| **MinIO Console** | http://localhost:9001 (user: `minio_userlog`, pass: `minio_passlog`) |
| **pgAdmin** | http://localhost:5050 (user: `admin@example.com`, pass: `admin`) |

---

## 🗂️ Project Structure

```
plataforma-incendios-valle-del-sol/
├── backend/
│   ├── bff/             # Spring Cloud Gateway — API entry point
│   ├── ms-users/        # Authentication & user management
│   ├── ms-reports/      # Fire reports & geospatial GIS
│   └── ms-alerts/       # Community alerts & SSE stream
├── frontend/
│   └── mfe-mapeo/       # React TypeScript SPA
├── k8s/                 # Kubernetes manifests
├── init-scripts/        # PostgreSQL database init SQL
├── docker-compose.yml   # Full stack orchestration
├── private_key.pem      # RSA private key (git-ignored)
└── public_key.pem       # RSA public key (git-ignored)
```

---

## 🔐 Authentication & Authorization

The platform uses **asymmetric RS256 JWT** tokens:

- `ms-usuarios` signs tokens with the **private key** and exposes a **JWKS endpoint** at `http://ms-usuarios:8081/.well-known/jwks.json`
- `ms-bff` and `ms-mapeo` validate tokens by fetching the public key from the JWKS endpoint
- `ms-alerts` validates tokens the same way for protected write endpoints

### Roles

| Role | Capabilities |
|---|---|
| `USER` | View map, submit fire reports |
| `BRIGADIST` | All USER permissions + update report status + publish community alerts |
| `ADMINISTRATOR` | All BRIGADIST permissions + manage all users and roles |

---

## 🗺️ Three Core Modules

### 1. Detection & Reporting
- Citizens and brigadists submit fire focus reports via the interactive map
- Supports **photo and video uploads** stored in MinIO
- Reports include description, fire type (Forestal/Estructural/Vehicular), GPS coordinates

### 2. Geographic Monitoring
- **Leaflet map** with layer controls showing:
  - 🔴 Active fire focuses (color-coded by status)
  - 🔵 Active brigade positions
  - 🟢 Evacuation routes
  - 🔺 Wildfire risk zones (forest-urban interface)
- Brigadists/Admins can update report status directly on the map popup

### 3. Community Alert System
- Admins and Brigadists broadcast **official emergency notices** via the left panel form
- Alerts are persisted in PostgreSQL and **pushed in real time** to all connected browsers via **Server-Sent Events (SSE)**
- Clients receive toast-style push notifications without polling

---

## 📊 Admin Dashboard

The Dashboard view (accessible to Brigadists and Administrators) includes:

- **Fire statistics cards** — active counts per fire type (via PostgreSQL stored procedure)
- **Historical reports log** — filterable table with media attachment links and inline status selector
- **User role management** — Administrator-only table to promote/demote user roles

---

## 🧪 Running Tests

```bash
# ms-usuarios
cd backend/ms-usuarios && mvn test

# ms-mapeo
cd backend/ms-mapeo && mvn test

# ms-alerts
cd backend/ms-alerts && mvn test

# ms-bff
cd backend/ms-bff && mvn test

# Frontend TypeScript check
cd frontend/mfe-mapeo && npx tsc --noEmit
```

---

## ☸️ Kubernetes Deployment

Each service has manifests in its `k8s/` directory. A shared namespace manifest is in `k8s/namespace.yaml`.

```bash
# Create namespace
kubectl apply -f k8s/namespace.yaml

# Deploy database and storage
kubectl apply -f k8s/postgres/
kubectl apply -f k8s/minio/

# Deploy microservices
kubectl apply -f backend/ms-usuarios/k8s/
kubectl apply -f backend/ms-mapeo/k8s/
kubectl apply -f backend/ms-alerts/k8s/
kubectl apply -f backend/ms-bff/k8s/

# Apply ingress
kubectl apply -f k8s/ingress/
```

---

## 🌐 Environment Variables

### ms-usuarios

| Variable | Default | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/ms_usuarios` | PostgreSQL connection |
| `JWT_ISSUER` | `http://ms-usuarios:8081` | JWT issuer claim |
| `JWT_PRIVATE_KEY_PATH` | `/run/keys/private_key.pem` | RSA private key path |
| `JWT_PUBLIC_KEY_PATH` | `/run/keys/public_key.pem` | RSA public key path |

### ms-mapeo

| Variable | Default | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/ms_mapeo` | PostgreSQL connection |
| `JWK_SET_URI` | `http://ms-usuarios:8081/.well-known/jwks.json` | Token validation |
| `MINIO_ENDPOINT` | `http://localhost:9000` | MinIO internal URL |
| `MINIO_EXTERNAL_URL` | `http://localhost:9000` | MinIO external (browser) URL |
| `MINIO_ACCESS_KEY` | `minio_userlog` | MinIO access key |
| `MINIO_SECRET_KEY` | `minio_passlog` | MinIO secret key |
| `MINIO_BUCKET` | `multimedia-reportes` | Bucket name |

### ms-alerts

| Variable | Default | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/ms_alerts` | PostgreSQL connection |
| `JWK_SET_URI` | `http://ms-usuarios:8081/.well-known/jwks.json` | Token validation |

### ms-bff

| Variable | Default | Description |
|---|---|---|
| `JWT_ISSUER` | `http://ms-usuarios:8081` | Expected JWT issuer |
| `JWK_SET_URI` | `http://ms-usuarios:8081/.well-known/jwks.json` | JWKS endpoint |

---

## 📝 License

Universidad — Caso Semestral. Municipalidad Valle del Sol.
