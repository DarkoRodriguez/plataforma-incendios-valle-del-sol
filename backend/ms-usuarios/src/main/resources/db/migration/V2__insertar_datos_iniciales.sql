-- V2: Insertar datos iniciales
INSERT INTO users (username, password) 
SELECT 'demo', 'demo'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'demo');
