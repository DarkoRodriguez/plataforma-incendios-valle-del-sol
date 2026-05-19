# Municipalidad Valle del Sol 
## Plataforma Inteligente para la Gestión y Prevención de Incendios

Este repositorio contiene la solución modular basada en microservicios desarrollada para la **Subdirección de Gestión de Emergencias y Prevención de Desastres** de la Municipalidad Valle del Sol. Esta plataforma tecnológica tiene como objetivo modernizar los mecanismos de prevención, detección temprana, monitoreo geoespacial y comunicación de emergencias en el territorio comunal.

---

## 1. Contextualización del Caso de Negocio

En las últimas temporadas de sequía y calor extremo, los incendios forestales y de interfase urbana han representado una amenaza crítica para la seguridad física de los ciudadanos, la infraestructura local y la biodiversidad en la comuna de Valle del Sol. 

### Limitaciones Identificadas en la Gestión Anterior
- **Procesamiento Manual y Canales Fragmentados:** La mayor parte de los reportes iniciales se recibían por llamadas de voz, redes sociales y avisos de viva voz, perdiendo precisión de coordenadas y ralentizando el despacho.
- **Inexistencia de Visualización Espacial Activa:** Los operadores no contaban con herramientas para georreferenciar en tiempo real los focos de incendio reportados.
- **Baja Integración Institucional:** Desconexión comunicativa rápida entre vecinos, guardabosques municipales y brigadas forestales profesionales.
- **Falta de Historial de Patrones:** Ausencia de bases de datos centralizadas con soporte para análisis geoespacial histórico que facilite la planificación preventiva.

### Propuesta Tecnológica Integral
La plataforma propone una solución desacoplada, escalable y tolerante a fallos, construida bajo el ecosistema de **Microservicios**, **Micro-frontends** y un **API Gateway (BFF)**. Permite a los ciudadanos reportar focos geográficos de manera móvil inmediata ("Web-First"), mientras que a los Brigadistas y Administradores les proporciona herramientas de Control de Acceso Basado en Roles (RBAC) para el monitoreo interactivo y la actualización activa de estados de emergencia en tiempo real.

---

## 2. Diagramas Generales del Sistema

*(A continuación se presentan las secciones correspondientes para insertar los diagramas e imágenes de arquitectura)*

### A. Diagrama de Casos de Uso
Este diagrama modela cómo interactúan los diversos actores del negocio (Ciudadanos, Brigadistas y Administradores) con los casos de uso principales de la plataforma (Registro de reportes, Monitoreo de focos, Edición de perfiles y Modificación de estados de incendio).

```
[ INSERTAR AQUÍ EL DIAGRAMA DE CASOS DE USO ]
```

### B. Diagrama de Contexto (Modelo C4)
Define los límites lógicos de la plataforma frente a los usuarios finales y los sistemas externos integrados (como OpenStreetMap para el despliegue cartográfico base).

```
[ INSERTAR AQUÍ EL DIAGRAMA DE CONTEXTO C4 ]
```

### C. Diagrama de Despliegue (Kubernetes On-Premise)
Describe la infraestructura virtualizada mediante contenedores Docker y orquestación con Kubernetes para su instalación en los servidores municipales, asegurando soberanía de datos locales y escalamiento dinámico (HPA).

```
[ INSERTAR AQUÍ EL DIAGRAMA DE DESPLIEGUE ]
```

---

## 3. Arquitectura y Patrones del Sistema

Para garantizar mantenibilidad, resiliencia y alta cohesión con bajo acoplamiento, la plataforma implementa los siguientes patrones de diseño y arquitectura:

1. **Backend For Frontend (BFF):** Centraliza la interfaz de entrada para el cliente, aislando los microservicios internos y controlando transversalmente la resiliencia y políticas de orígenes cruzados (CORS).
2. **Database per Service:** Cada microservicio controla e interactúa con su propio esquema de base de datos aislada (PostgreSQL), evitando acoplamientos a nivel de persistencia de datos.
3. **Persistencia Espacial (PostGIS):** El microservicio de mapeo aprovecha la extensión espacial de PostgreSQL para almacenar ubicaciones precisas como puntos geográficos y realizar consultas espaciales nativas rápidas.
4. **Seguridad Centralizada (JWT & RBAC):** Control de acceso basado en tokens seguros que inyectan el Rol de usuario (`USUARIO`, `BRIGADISTA`, `ADMINISTRADOR`) permitiendo restringir rutas administrativas y de personal calificado.
5. **Circuit Breaker (Resiliencia):** Tolerancia a fallas en cascada integrada en la pasarela de enrutamiento para asegurar que la caída de un servicio secundario no colapse el flujo principal.

---

## 4. Componentes y Estructura del Proyecto

El repositorio está estructurado bajo una arquitectura modular (Monorrepo) que contiene los siguientes módulos y piezas desplegables de manera independiente:

*   **[`/mfe-mapeo`](./mfe-mapeo):** Interfaz Web responsiva (React, Vite, react-leaflet). Permite visualizar el mapa comunal interactivo, posicionar marcadores y reportar incendios.
*   **[`/ms-bff`](./ms-bff):** Backend For Frontend desarrollado en Spring Cloud Gateway. Centraliza el CORS y la resiliencia mediante disyuntores.
*   **[`/ms-usuarios`](./ms-usuarios):** Microservicio Spring Boot que administra el registro, autenticación cifrada, JWT y perfiles de usuarios.
*   **[`/ms-mapeo`](./ms-mapeo):** Microservicio Spring Boot que administra los reportes de incendios y georreferenciación espacial mediante Hibernate Spatial y PostGIS.
*   **[`/krakend`](./krakend):** API Gateway secundario e interno para ruteo de bajo nivel y reenvío de encabezados seguros entre piezas.
*   **[`/archetypes`](./archetypes):** Arquetipo Maven personalizado que estandariza la creación de nuevos servicios de la plataforma.

---

## 5. Guía de Ejecución Rápida (Entorno Dockerizado)

Para levantar toda la suite de servicios e infraestructura integrada en un solo comando:

### Requisitos previos
- **Docker** y **Docker Compose** instalados en el sistema local.

### Instrucciones de Inicio
1. Clona el repositorio y sitúate en la raíz del proyecto.
2. Inicia todos los servicios e infraestructura en segundo plano:
   ```bash
   docker compose up -d --build
   ```
3. Verifica el correcto inicio de los contenedores:
   ```bash
   docker compose ps
   ```
4. Accede a las interfaces disponibles:
   - **Frontend (MFE Mapeo):** [http://localhost:3000](http://localhost:3000)
   - **Pasarela Central (BFF):** [http://localhost:8080](http://localhost:8080)
   - **API Gateway Interno (KrakenD):** [http://localhost:8000](http://localhost:8000)
   - **Administración DB (pgAdmin4):** [http://localhost:5050](http://localhost:5050) (Usuario: `admin@example.com` / Contraseña: `admin`)
