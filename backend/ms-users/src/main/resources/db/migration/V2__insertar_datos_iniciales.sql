-- V2: Insertar datos iniciales
INSERT INTO users (username, password, region, commune, email, phone, role) 
VALUES 
('demo', '$2a$10$UvOEw1WVLre78IYoJwYvwOZ6wXjoFOY7PxFBbq2d12FcwTBzp9KWq', 'Valparaiso', 'Valle del Sol', 'demo@valledelsol.cl', '+56911112222', 'USER')
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, region, commune, email, phone, role) 
VALUES 
('brigadista', '$2a$10$UvOEw1WVLre78IYoJwYvwOZ6wXjoFOY7PxFBbq2d12FcwTBzp9KWq', 'Valparaiso', 'Valle del Sol', 'brigada@valledelsol.cl', '+56922223333', 'BRIGADIST')
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, region, commune, email, phone, role) 
VALUES 
('admin', '$2a$10$UvOEw1WVLre78IYoJwYvwOZ6wXjoFOY7PxFBbq2d12FcwTBzp9KWq', 'Valparaiso', 'Valle del Sol', 'admin@valledelsol.cl', '+56933334444', 'ADMINISTRATOR')
ON CONFLICT (username) DO NOTHING;
