# Análisis de Patrones y Arquetipos

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

## Conclusión
La combinación de estos patrones garantiza que la plataforma de la Municipalidad Valle del Sol sea altamente escalable, resiliente frente a fallos (crucial en emergencias) y tenga un código fácil de mantener y probar.
