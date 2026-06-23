-- Habilitar extensión PostGIS
CREATE EXTENSION IF NOT EXISTS postgis;

-- Crear tabla de reportes de incendios
CREATE TABLE IF NOT EXISTS fire_reports (
    id SERIAL PRIMARY KEY,
    description TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    location geometry(Point, 4326) NOT NULL,
    user_id BIGINT,
    report_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    media_url VARCHAR(500)
);

-- Crear función para obtener la cantidad de reportes activos por tipo
CREATE OR REPLACE FUNCTION get_active_reports_count_by_type(
    report_type VARCHAR
)
RETURNS INT
LANGUAGE sql
AS $$
    SELECT COUNT(*) FROM fire_reports
    WHERE fire_reports.type = report_type
      AND status = 'ACTIVO';
$$;
