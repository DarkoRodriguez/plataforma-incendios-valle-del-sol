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

-- Crear procedimiento almacenado para obtener la cantidad de reportes activos por tipo
CREATE OR REPLACE PROCEDURE get_active_reports_count_by_type(
    IN p_type VARCHAR,
    OUT p_count INT
)
LANGUAGE plpgsql
AS $$
BEGIN
    SELECT COUNT(*) INTO p_count
    FROM fire_reports
    WHERE type = p_type AND status = 'ACTIVO';
END;
$$;
