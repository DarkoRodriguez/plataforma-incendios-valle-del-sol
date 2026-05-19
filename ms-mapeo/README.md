# MS-Mapeo (Mapping Microservice Component)
## Módulo de Registro Espacial y Visualización de Incendios - Comuna Valle del Sol

El microservicio **`ms-mapeo`** es el componente core encargado de la georreferenciación, almacenamiento y actualización en tiempo real de los focos de incendio en la comuna. Se integra nativamente con la extensión espacial **PostGIS** de PostgreSQL, permitiendo el guardado y consulta de geometrías espaciales complejas.

---

## 1. Arquitectura y Patrones de Diseño

Este microservicio implementa patrones específicos de la ingeniería de software y sistemas de información geográfica (SIG):

1. **Persistencia Espacial (Hibernate Spatial & JTS):**
   - Utiliza la biblioteca **JTS (Java Topology Suite)** para representar ubicaciones físicas como objetos de tipo **`Point`** con sistema de coordenadas **SRID 4326 (WGS84)** de forma nativa en Java.
   - **`hibernate-spatial`** traduce de manera transparente los objetos `Point` en datos espaciales binarios binarios de PostGIS al interactuar con la base de datos, optimizando las consultas geográficas.
2. **Repository Pattern (Patrón Repositorio):**
   - Implementado en **`ReporteIncendioRepository`**. Gestiona de forma limpia la persistencia de las entidades espaciales, heredando métodos avanzados de búsqueda y guardado nativo de Spring Data.
3. **RBAC Descentralizado por Token (JWT Claim validation):**
   - En lugar de comunicarse síncronamente con el servicio de usuarios para autorizar cambios (lo que acoplaría los servicios e incrementaría la latencia), utiliza la clase local **`JwtUtil.java`** que valida la firma criptográfica del token usando la clave estática simétrica compartida y extrae el claim de rol (`role`) para verificar privilegios de brigadista de forma autónoma.
4. **Patrón DTO (Data Transfer Object):**
   - Expone **`ReporteIncendioDTO`**, el cual traduce la propiedad espacial `Point` interna de Hibernate en atributos planos legibles por la API REST (`latitud`, `longitud`), facilitando su renderización en la interfaz.

---


## 2. Tecnologías y Librerías Clave

- **Spring Boot 3.3.x:** Base estructural del servicio.
- **Hibernate Spatial / JTS Core:** Mapeo y manipulación nativa de geometrías espaciales.
- **PostGIS Extension (Base de Datos):** Extensión de PostgreSQL que habilita tipos de objetos geográficos y queries de distancia, polígonos y cercanía.
- **jjwt (Java JWT):** Decodificación y validación de tokens Bearer.

---

## 3. Configuración y Setup del Servicio

### Requisitos previos
- **Java 21 / 25 LTS** instalado.
- **Maven 3.8+** instalado.
- Servidor **PostgreSQL** con la extensión **PostGIS** habilitada.

### Instalación Individual
1. Navega al directorio `/ms-mapeo`:
   ```bash
   cd ms-mapeo
   ```
2. Asegura la inicialización de PostGIS en la base de datos de PostgreSQL:
   ```sql
   CREATE DATABASE ms_mapeo;
   \c ms_mapeo;
   CREATE EXTENSION postgis;
   ```
3. Configura las variables de conexión en `src/main/resources/application.yml` o inyéctalas como entorno:
   - `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ms_mapeo`
   - `SPRING_DATASOURCE_USERNAME=postgres`
   - `SPRING_DATASOURCE_PASSWORD=postgres`
4. Ejecuta la compilación y empaquetado del código utilizando Maven:
   ```bash
   mvn clean package -DskipTests
   ```
5. Levanta el microservicio:
   ```bash
   mvn spring-boot:run
   ```
6. El servicio estará activo y escuchando en el puerto: **`8082`**.

---

## 4. Detalles de Endpoints de la API

El microservicio expone de forma directa los siguientes endpoints:

| Método | Endpoint | Cabecera Auth | Descripción |
| :--- | :--- | :--- | :--- |
| **GET** | `/api/mapeo/reportes` | No requerida | Obtiene la lista completa de todos los focos de incendio en el mapa comunal. |
| **POST** | `/api/mapeo/reportes` | No requerida | Reporta un foco de incendio (recibe latitud/longitud y detalles). |
| **PUT** | `/api/mapeo/reportes/{id}/estado` | Requerida (Bearer) | Actualiza el estado operativo (Solo personal con rol `BRIGADISTA` o `ADMINISTRADOR`). |
