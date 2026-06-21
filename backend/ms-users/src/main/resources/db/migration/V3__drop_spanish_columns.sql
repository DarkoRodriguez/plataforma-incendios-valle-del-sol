-- V3: Eliminar columnas legacy en español que duplican columnas en inglés
-- Eliminar columnas introducidas por error: rol, comuna, correo, telefono
-- Flyway aplicará este script en el arranque del servicio ms-users

ALTER TABLE users DROP COLUMN IF EXISTS rol;
ALTER TABLE users DROP COLUMN IF EXISTS comuna;
ALTER TABLE users DROP COLUMN IF EXISTS correo;
ALTER TABLE users DROP COLUMN IF EXISTS telefono;
