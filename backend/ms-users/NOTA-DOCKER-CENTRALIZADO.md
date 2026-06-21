# ⚠️ NOTA IMPORTANTE

## Uso de Docker Centralizado

Este microservicio ahora se gestiona desde el **docker-compose.yml centralizado** ubicado en la raíz del proyecto:

```
/plataforma-incendios-valle-del-sol/docker-compose.yml
```

### Cambio de Arquitectura

- **Antes**: Cada microservicio tenía su propio `docker-compose.yml` individual
- **Ahora**: Un único `docker-compose.yml` centralizado gestiona todos los servicios

### Cómo Ejecutar

#### Opción 1: Desde la Raíz (Recomendado)

```bash
cd ../..  # Ir a plataforma-incendios-valle-del-sol/
./docker.sh up
```

#### Opción 2: Desde cualquier lugar

```bash
cd plataforma-incendios-valle-del-sol/
docker-compose up --build -d
```

#### NO Usar

⚠️ NO ejecutes `docker-compose` desde esta carpeta (ms-usuarios/). El `docker-compose.yml` local aún existe pero es **obsoleto**.

### Estructura Actualizada

```
plataforma-incendios-valle-del-sol/
├── docker-compose.yml          ← CENTRALIZADO (USAR ESTE)
├── docker.sh                   ← Script maestro
├── init-scripts/
│   └── 01-create-databases.sql ← Crear todas las BDs
├── ms-usuarios/
│   ├── docker-compose.yml      ← ⚠️ OBSOLETO (no usar)
│   ├── Dockerfile              ← ✅ Sigue usándose
│   └── src/
├── ms-mapeo/
│   ├── Dockerfile              ← ✅ Nuevo Dockerfile
│   └── src/
└── demo/
    ├── Dockerfile              ← ✅ Nuevo Dockerfile
    └── src/
```

### Beneficios del Cambio

✅ **Único punto de entrada** para toda la plataforma  
✅ **Red compartida** entre microservicios  
✅ **Base de datos centralizada** pero con BDs separadas por servicio  
✅ **Escalabilidad** - Agregar nuevos servicios fácilmente  
✅ **Gestión simplificada** - Un comando para todo  

### Migraciones Flyway

Las migraciones siguen siendo **locales en cada servicio**:

```
ms-usuarios/src/main/resources/db/migration/
├── V1__crear_tabla_inicial.sql
└── V2__insertar_datos_iniciales.sql
```

Se ejecutan automáticamente al iniciar cada servicio.

### Comandos Principales

```bash
# Iniciar TODO
./docker.sh up

# Ver estado
./docker.sh status

# Ver logs
./docker.sh logs ms-usuarios

# Acceder a BD
./docker.sh db ms_usuarios

# Reiniciar
./docker.sh restart

# Limpiar
./docker.sh clean
```

### Acceso al Servicio

- **URL**: http://localhost:8081
- **BD**: PostgreSQL en localhost:5432
- **Administrador BD**: PgAdmin en http://localhost:5050

---

Para más detalles, consulta: `DOCKER-CENTRAL-README.md` en la raíz del proyecto.
