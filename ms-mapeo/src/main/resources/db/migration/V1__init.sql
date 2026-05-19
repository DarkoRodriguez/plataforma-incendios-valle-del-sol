-- Habilitar extensión PostGIS (asumiendo que el usuario tiene permisos o se ejecutó como superusuario)
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE reportes_incendio (
    id SERIAL PRIMARY KEY,
    descripcion TEXT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    ubicacion geometry(Point, 4326) NOT NULL,
    fecha_reporte TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
