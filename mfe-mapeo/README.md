# MFE-Mapeo (Frontend Component - Micro-Frontend)
## Plataforma de Visualización y Detección de Focos de Incendio - Comuna Valle del Sol

Este componente representa la interfaz de usuario de la plataforma, diseñada bajo un esquema responsivo y optimizado ("Web-First") para permitir un reporte veloz y sin barreras en situaciones de emergencia. Consiste en una aplicación moderna en React SPA empaquetada e integrada que provee visualización geográfica interactiva y control de accesos basados en roles.

---

## 1. Arquitectura y Patrones de Diseño

El frontend implementa una serie de patrones de diseño específicos para garantizar la reactividad, modularidad y fácil mantenibilidad de sus vistas:

1. **Patrón Container/Presenter:** 
   - **`App.jsx`** actúa como el contenedor principal de datos y estados (carga de reportes, datos de sesión activa, etc.).
   - Componentes puros como **`MapView.jsx`**, **`Navbar.jsx`** y **`ProfileModal.jsx`** actúan como presentadores que reciben funciones callback y estados como propiedades (Props), garantizando la reutilización.
2. **Custom Hooks:** 
   - Centralización del consumo de datos geográficos para mantener el ciclo de vida React limpio e independiente del origen de datos.
3. **Capa de Abstracción de API (Axios Interceptors):**
   - Implementado en `api.js`. Un interceptor inyecta dinámicamente el token `Authorization: Bearer <JWT>` guardado en `localStorage` en cada petición HTTP saliente hacia el BFF, facilitando el control de seguridad de forma automática.
4. **Estado Reactivo Global Compartido:**
   - La sesión del usuario se distribuye hacia las vistas del mapa y barra de navegación en tiempo real, permitiendo cambios dinámicos de UI (ej: habilitar el dropdown de cambio de estado a brigadistas y actualizar el Avatar circular al instante).

---



## 2. Tecnologías y Librerías Clave

- **React 18 & Vite:** Entorno de compilación ultra veloz y servidor de desarrollo ágil.
- **Leaflet & react-leaflet (1.9.4):** Motor de mapas de código abierto integrado con capas cartográficas base de **OpenStreetMap**.
- **Axios:** Cliente HTTP para la comunicación con el Backend.
- **Glassmorphic Glass Styling (Vanilla CSS):** Diseño de UI premium responsivo basado en translucidez de cristal y efectos de desenfoque.

---

## 3. Configuración y Setup del Servicio

### Requisitos previos
- **Node.js:** Versión 20.x LTS ("Iron").
- **NPM** instalado.

### Instalación Individual
1. Navega al directorio `/mfe-mapeo`:
   ```bash
   cd mfe-mapeo
   ```
2. Instala las dependencias y librerías declaradas en `package.json`:
   ```bash
   npm install
   ```
3. Ejecuta el servidor de desarrollo de Vite:
   ```bash
   npm run dev
   ```
4. El frontend estará disponible localmente en: [http://localhost:5173](http://localhost:5173) (o el puerto configurado por Vite).

### Variables de Entorno
Por defecto, la API se conecta al BFF central en `http://localhost:8080/api` mediante el archivo `src/api.js`. Si deseas modificar la dirección del gateway, edita la base URL del cliente Axios.

---

## 4. Descripción del Flujo y Características de la UI

- **Mapa de Pantalla Completa:** Al abrir la aplicación, el usuario se encuentra con un mapa Leaflet interactivo cargado con todos los pines activos georreferenciados mediante sus coordenadas espaciales.
- **Pines por Código de Colores:** Los focos se visualizan dinámicamente según su estado:
  - 🔴 **Rojo (ACTIVO):** Peligro inminente sin control.
  - 🟠 **Naranja (CONTROLADO):** Contenido por brigadas, bajo observación.
  - ⚪ **Gris (EXTINGUIDO):** Superado por completo.
- **Reporte Fácil:** Un ciudadano puede hacer doble clic en cualquier punto geográfico del mapa o presionar **"Reportar Incendio"** en la barra de navegación para abrir un formulario responsivo de reporte directo.
- **Cambio de Estado Seguro (RBAC):** Si un usuario inicia sesión con rol de `BRIGADISTA` o `ADMINISTRADOR`, al presionar sobre cualquier pin de incendio se despliega un selector interactivo para modificar el estado del incendio. La actualización se refleja reactivamente en el mapa sin necesidad de recargar la página completa.
- **Gestión de Perfil:** El modal de **Editar Perfil** permite a los usuarios alterar sus datos de localización (región, comuna), contacto (correo, teléfono) y credenciales en tiempo real.
