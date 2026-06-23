-- V2: Agregar campos de región y comuna a los reportes de incendios
ALTER TABLE fire_reports
    ADD COLUMN region VARCHAR(100);

ALTER TABLE fire_reports
    ADD COLUMN commune VARCHAR(100);
