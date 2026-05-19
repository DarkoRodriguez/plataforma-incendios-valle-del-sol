# MFE-Mapeo (Frontend)

Este es el micro-frontend (MFE) encargado de la visualización del mapa y el reporte de incendios para la Municipalidad Valle del Sol. Está construido utilizando React, Vite y react-leaflet.

## Requisitos previos
- Node.js 20.x LTS
- NPM

## Instalación y Ejecución

1. Navegar al directorio `mfe-mapeo`.
2. Instalar las dependencias:
   ```bash
   npm install
   ```
3. Ejecutar el servidor de desarrollo:
   ```bash
   npm run dev
   ```
4. Acceder en el navegador a `http://localhost:5173` (o el puerto que indique Vite).

## Tecnologías y Patrones
- **React & Vite**: Para una construcción rápida y empaquetamiento optimizado.
- **react-leaflet**: Integración con OpenStreetMap.
- **Axios**: Para la comunicación HTTP con el API Gateway (BFF).
- **Patrones**: Container/Presenter, Custom Hooks (`useReportes.js`).
