-- V2: Insertar datos iniciales
INSERT INTO users (username, password) 
SELECT 'demo', '$2b$12$UvOEw1WVLre78IYoJwYvwOZ6wXjoFOY7PxFBbq2d12FcwTBzp9KWq'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'demo');
