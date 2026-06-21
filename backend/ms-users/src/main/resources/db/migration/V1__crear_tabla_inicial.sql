-- V1: Crear tabla inicial de usuarios
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    region VARCHAR(255),
    commune VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    role VARCHAR(255) NOT NULL DEFAULT 'USER'
);

-- Crear índice en username para optimizar búsquedas
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
