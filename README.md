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
<img width="824" height="747" alt="image" src="https://github.com/user-attachments/assets/0d6726bf-f121-47aa-9d35-baadc4395e62" />

```

### B. Diagrama de Contexto (Modelo C4)
Define los límites lógicos de la plataforma frente a los usuarios finales y los sistemas externos integrados (como OpenStreetMap para el despliegue cartográfico base).

```
<img width="731" height="663" alt="image" src="https://github.com/user-attachments/assets/c3027c48-d8e4-473e-ac1c-a0c92c5d8044" />

```

### C. Diagrama de Despliegue (On-Premise)
Describe la infraestructura virtualizada mediante contenedores Docker y orquestación con Kubernetes para su instalación en los servidores municipales, asegurando soberanía de datos locales y escalamiento dinámico (HPA).

```
<img width="1938" height="2736" alt="Diagrama de Despliegue" src="https://github.com/user-attachments/assets/b4ddc83e-ef40-443d-a2b8-5a5c1e687e3f" />

```
### C. Diagrama de Contenedores 
Representa visualmente la arquitectura de software, descompone el sistema  en sus piezas tecnológicas fundamentales.

```
<img width="3843" height="2523" alt="Diagrama de Contenedores" src="https://github.com/user-attachments/assets/20bed0ec-0e43-46ad-a148-15fa614cd324" />

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

# 5. Análisis de Patrones y Arquetipos

Este documento detalla los patrones de diseño y arquetipos arquitectónicos seleccionados para el desarrollo de la **Plataforma Inteligente para la Gestión y Prevención de Incendios** (Evaluación Parcial 2).

## 1. Arquetipos y Arquitectura Base
El sistema se ha construido siguiendo una arquitectura de **Microservicios** conectada mediante un **API Gateway / Backend For Frontend (BFF)**. Se ha desarrollado un Arquetipo Maven (`demo-archetype`) para garantizar que la creación de futuros microservicios siga una estructura coherente, incluyendo dependencias estandarizadas como Spring Boot Web, Data JPA, PostgreSQL y Flyway.

- **BFF (ms-bff):** Actúa como el punto de entrada único para el frontend. Enruta las solicitudes a `ms-usuarios` o `ms-mapeo`. Mejora la seguridad y consolida las respuestas.
- **Microservicios Independientes:** `ms-usuarios` gestiona la identidad y autenticación. `ms-mapeo` se encarga de la gestión geoespacial (PostGIS) de los focos de incendio.

## 2. Patrones de Diseño (Backend)

### 2.1 Pattern: API Gateway / BFF
Implementado usando `Spring Cloud Gateway` en el componente `ms-bff`. Desacopla al frontend de la complejidad de conocer la ubicación y los puertos de cada microservicio interno.

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
Implementado a través de `useReportes.js`. Extrae la lógica de fetching (Axios) hacia el BFF, el manejo del estado (`loading`, `error`, `reportes`) y hace que el componente principal (`App.jsx`) sea extremadamente limpio y fácil de testear.

## En Conclusion
La combinación de estos patrones garantiza que la plataforma de la Municipalidad Valle del Sol sea altamente escalable, resiliente frente a fallos (crucial en emergencias) y tenga un código fácil de mantener y probar.

# 6. Estrategia de Branching

Para el desarrollo de la "Plataforma Inteligente para la Gestión y Prevención de Incendios", se ha implementado la estrategia de **GitHub Flow** adaptada para soportar un monorepositorio con múltiples componentes (Frontend, BFF, Microservicios).

## 1. Ramas Principales

- **`main`**: Es la rama principal. Contiene el código listo para producción. Cualquier commit en `main` debe ser estable, pasar las pruebas unitarias y ser desplegable.
- **`develop`**: Rama de integración. Es donde confluyen todas las nuevas funcionalidades antes de pasar a `main`. 

## 2. Ramas de Soporte

- **`feature/nombre-de-la-feature`**: Creadas a partir de `develop`. Se utilizan para desarrollar nuevas funcionalidades (ej. `feature/ms-usuarios`, `feature/mfe-mapeo`).
- **`bugfix/nombre-del-bug`**: Creadas a partir de `develop` o `main` para resolver problemas no críticos encontrados durante el desarrollo. (No implementada aun)
- **`hotfix/nombre-del-hotfix`**: Creadas directamente desde `main` para solucionar incidencias críticas en producción. Se integran de vuelta tanto a `main` como a `develop`. (No implementada aun)

## 3. Flujo de Trabajo (Workflow)

1. **Creación**: Un desarrollador crea una rama `feature/ms-mapeo` desde `develop`.
2. **Desarrollo**: Se realizan commits atómicos y descriptivos.
3. **Pull Request (PR)**: Una vez finalizada la tarea, se abre un PR hacia `develop`.
4. **Code Review**: Otro miembro del equipo (o el mismo desarrollador si trabaja solo) revisa el código, asegurando la cobertura de pruebas unitarias (>60%).
5. **Merge**: Se aprueba el PR y se hace merge a `develop`, resolviendo cualquier conflicto de integración.
6. **Release**: Cuando `develop` alcanza un estado maduro y estable (como la finalización de la Evaluación Parcial 2), se genera un PR hacia `main` y se crea un *Tag* de versión (ej. `v2.0.0`).

## 4. Gestión de Conflictos
En caso de que ocurran conflictos al integrar ramas (ej. dos desarrolladores modifican `docker-compose.yml`), el desarrollador encargado de la rama de la característica debe hacer un `git pull origin develop` hacia su rama, resolver el conflicto localmente en su IDE, confirmar los cambios y luego actualizar el PR.

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
4. Accede a la interfaz del frontend:
   - **Frontend (MFE Mapeo):** [http://localhost:3000](http://localhost:3000)


## 8. Visualizacion de la ejecucion
<img width="1915" height="992" alt="image" src="https://github.com/user-attachments/assets/3dbc1adb-696d-4592-9433-31dca37ed19a" />

---

<img width="1915" height="992" alt="image" src="https://github.com/user-attachments/assets/f98ee444-2f9c-4443-a6d8-602780aeed9d" />

---

<img width="1915" height="992" alt="image" src="https://github.com/user-attachments/assets/962f8993-e435-4380-8b6a-e4a5742052c4" />

---

<img width="1920" height="993" alt="image" src="https://github.com/user-attachments/assets/7cc438c4-9e9c-4592-aa36-de9cf24e6823" />



