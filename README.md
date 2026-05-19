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
La plataforma propone una solución desacoplada, escalable y tolerante a fallos, construida bajo el ecosistema de **API Gateway (KrakenD)**, **Backend For Frontend (ms-bff)**, y **Microservicios**. Permite a los ciudadanos reportar focos geográficos de manera móvil inmediata ("Web-First"), mientras que a los Brigadistas y Administradores les proporciona herramientas de Control de Acceso Basado en Roles (RBAC) para el monitoreo interactivo y la actualización activa de estados de emergencia en tiempo real.

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

1. **API Gateway (KrakenD):** Centraliza la interfaz de entrada directa para el cliente en el puerto `8080`, controlando transversalmente la autenticación, reenvío de encabezados seguros y políticas de orígenes cruzados (CORS).
2. **Backend For Frontend (BFF):** Actúa como el puente orquestador y de control intermedio. Recibe las llamadas validadas de KrakenD y gestiona la resiliencia y lógica de integración hacia los microservicios de negocio.
3. **Database per Service:** Cada microservicio controla e interactúa con su propio esquema de base de datos aislada (PostgreSQL), evitando acoplamientos a nivel de persistencia de datos.
4. **Persistencia Espacial (PostGIS):** El microservicio de mapeo aprovecha la extensión espacial de PostgreSQL para almacenar ubicaciones precisas como puntos geográficos y realizar consultas espaciales nativas rápidas.
5. **Seguridad Centralizada (JWT & RBAC):** Control de acceso basado en tokens seguros que inyectan el Rol de usuario (`USUARIO`, `BRIGADISTA`, `ADMINISTRADOR`) permitiendo restringir rutas administrativas y de personal calificado.
6. **Circuit Breaker (Resiliencia):** Tolerancia a fallas en cascada integrada en el BFF (Resilience4j) para asegurar que la caída de un servicio secundario no colapse el flujo principal.

---

## 4. Componentes y Estructura del Proyecto

El repositorio está estructurado bajo una arquitectura modular (Monorrepo) que contiene los siguientes módulos y piezas desplegables de manera independiente:

*   **[`/mfe-mapeo`](./mfe-mapeo):** Interfaz Web responsiva (React, Vite, react-leaflet). Permite visualizar el mapa comunal interactivo, posicionar marcadores y reportar incendios.
*   **[`/krakend`](./krakend):** API Gateway frontal que expone los endpoints en el puerto `8080` de cara al navegador, centralizando la seguridad y CORS.
*   **[`/ms-bff`](./ms-bff):** Backend For Frontend desarrollado en Spring Cloud Gateway. Centraliza la resiliencia mediante disyuntores de Resilience4j, canalizando las peticiones de KrakenD a los microservicios.
*   **[`/ms-usuarios`](./ms-usuarios):** Microservicio Spring Boot que administra el registro, autenticación cifrada, JWT y perfiles de usuarios.
*   **[`/ms-mapeo`](./ms-mapeo):** Microservicio Spring Boot que administra los reportes de incendios y georreferenciación espacial mediante Hibernate Spatial y PostGIS.
*   **[`/archetypes`](./archetypes):** Arquetipo Maven personalizado que estandariza la creación de nuevos servicios de la plataforma.

---

## 5. Análisis de Patrones y Arquetipos

Este documento detalla los patrones de diseño y arquetipos arquitectónicos seleccionados para el desarrollo de la **Plataforma Inteligente para la Gestión y Prevención de Incendios** (Evaluación Parcial 2).

## 1. Arquetipos y Arquitectura Base
El sistema se ha construido siguiendo una arquitectura de **Microservicios** conectada mediante un **API Gateway (KrakenD)** y un **Backend For Frontend (BFF)**. Se ha desarrollado un Arquetipo Maven (`demo-archetype`) para garantizar que la creación de futuros microservicios siga una estructura coherente, incluyendo dependencias estandarizadas como Spring Boot Web, Data JPA, PostgreSQL y Flyway.

- **API Gateway (KrakenD):** Único punto de entrada seguro expuesto al frontend. Valida cabeceras y previene accesos indebidos antes de pasar las peticiones a la capa de integración.
- **BFF (ms-bff):** Gestiona la lógica intermedia del frontend, canalizando peticiones y protegiendo el ecosistema interno con disyuntores de fallo.
- **Microservicios Independientes:** `ms-usuarios` gestiona la identidad y autenticación. `ms-mapeo` se encarga de la gestión geoespacial (PostGIS) de los focos de incendio.

## 2. Patrones de Diseño (Backend)

### 2.1 Pattern: API Gateway & BFF
Implementado en conjunto: **KrakenD** en la frontera externa y **Spring Cloud Gateway (ms-bff)** en el núcleo. Desacopla al frontend de la complejidad de conocer la ubicación y puertos de cada microservicio interno.

### 2.2 Pattern: Circuit Breaker
Implementado mediante **Resilience4j** en el `ms-bff`. Si el `ms-mapeo` se vuelve inaccesible o responde con lentitud debido a una consulta pesada, el Circuit Breaker "abre" el circuito, fallando rápidamente y evitando la saturación del sistema. 

### 2.3 Pattern: Data Transfer Object (DTO)
Utilizado intensamente en `ms-usuarios` y `ms-mapeo` (`UserDTO`, `ReporteIncendioDTO`). Evita la exposición directa de las Entidades JPA (como coordenadas exactas de PostGIS o passwords) al exterior, enviando solo la información necesaria.

### 2.4 Pattern: Repository
Abstraído mediante Spring Data JPA (`JpaRepository`). Oculta la complejidad de las consultas SQL/PostGIS, proporcionando una interfaz orientada a objetos para acceder a los datos.

### 2.5 Pattern: ControllerAdvice (Manejo de Errores Global)
Implementado en `ms-usuarios` (`GlobalExceptionHandler.java`). Centraliza la captura de excepciones, devolviendo respuestas HTTP y JSON consistentes en lugar de trazas de error confusas de Java.

## 3. Patrones de Diseño (Frontend)

### 3.1 Pattern: Container/Presenter
Separamos la vista (`MapView.jsx`) de la lógica de obtención de datos y manejo de estado. La vista se encarga exclusivamente de renderizar el mapa Leaflet, mientras que la obtención de datos se delega.

### 3.2 Pattern: Custom Hooks
Implementado a través de `useReportes.js`. Extrae la lógica de fetching (Axios) hacia el API Gateway / BFF, el manejo del estado (`loading`, `error`, `reportes`) y hace que el componente principal (`App.jsx`) sea extremadamente limpio y fácil de testear.

## En Conclusion
La combinación de estos patrones garantiza que la plataforma de la Municipalidad Valle del Sol sea altamente escalable, resiliente frente a fallos (crucial en emergencias) y tenga un código fácil de mantener y probar.

---

## 6. Estrategia de Branching

Para el desarrollo de la "Plataforma Inteligente para la Gestión y Prevención de Incendios", se ha implementado la estrategia de **GitHub Flow** adaptada para soportar un monorepositorio con múltiples componentes (Frontend, BFF, Microservicios).

## 1. Ramas Principales

- **`main`**: Es la rama principal. Contiene el código listo para producción. Cualquier commit en `main` debe ser estable, pasar las pruebas unitarias y ser desplegable.
- **`develop`**: Rama de integración. Es donde confluyen todas las nuevas funcionalidades antes de pasar a `main`. 

## 2. Ramas de Soporte

- **`feature/nombre-de-la-feature`**: Creadas a partir de `develop`. Se utilizan para desarrollar nuevas funcionalidades (ej. `feature/mapa-leaflet`, `feature/ms-usuarios-circuitbreaker`).
- **`bugfix/nombre-del-bug`**: Creadas a partir de `develop` o `main` para resolver problemas no críticos encontrados durante el desarrollo.
- **`hotfix/nombre-del-hotfix`**: Creadas directamente desde `main` para solucionar incidencias críticas en producción. Se integran de vuelta tanto a `main` como a `develop`.

## 3. Flujo de Trabajo (Workflow)

1. **Creación**: Un desarrollador crea una rama `feature/reporte-incendios` desde `develop`.
2. **Desarrollo**: Se realizan commits atómicos y descriptivos.
3. **Pull Request (PR)**: Una vez finalizada la tarea, se abre un PR hacia `develop`.
4. **Code Review**: Otro miembro del equipo (o el mismo desarrollador si trabaja solo) revisa el código, asegurando la cobertura de pruebas unitarias (>60%).
5. **Merge**: Se aprueba el PR y se hace merge a `develop`, resolviendo cualquier conflicto de integración.
6. **Release**: Cuando `develop` alcanza un estado maduro y estable (como la finalización de la Evaluación Parcial 2), se genera un PR hacia `main` y se crea un *Tag* de versión (ej. `v2.0.0`).

## 4. Gestión de Conflictos
En caso de que ocurran conflictos al integrar ramas (ej. dos desarrolladores modifican `docker-compose.yml`), el desarrollador encargado de la rama de la característica debe hacer un `git pull origin develop` hacia su rama, resolver el conflicto localmente en su IDE, confirmar los cambios y luego actualizar el PR.

---

## 7. Guía de Ejecución Rápida (Entorno Dockerizado)

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
   - **API Gateway (KrakenD):** [http://localhost:8080](http://localhost:8080)
   - **Backend For Frontend (ms-bff):** [http://localhost:8090](http://localhost:8090) (Internal/Admin Port)
   - **Administración DB (pgAdmin4):** [http://localhost:5050](http://localhost:5050) (Usuario: `admin@example.com` / Contraseña: `admin`)
