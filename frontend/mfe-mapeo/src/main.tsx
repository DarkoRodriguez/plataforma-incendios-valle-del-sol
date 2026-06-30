import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App'
import * as Sentry from "@sentry/react";

Sentry.init({
  // Accede a la variable de entorno de Vite
  dsn: "https://2ffab7f13ba94381b3a6f6298319e1e2@app.glitchtip.com/25285", 
  
  integrations: [
    Sentry.browserTracingIntegration(),
    Sentry.extraErrorDataIntegration(),
    Sentry.replayIntegration(), // Opcional: Graba sesiones de usuario si lo tienes activo
  ],
  
  // Configuración de rendimiento
  tracesSampleRate: 1.0,
  
  // Configuración de replays (captura de video de errores)
  replaysSessionSampleRate: 0.1,
  replaysOnErrorSampleRate: 1.0,
});

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Sentry.ErrorBoundary fallback={<div>Ocurrió un error en la aplicación.</div>}>
      <App />
    </Sentry.ErrorBoundary>
  </StrictMode>,
)
