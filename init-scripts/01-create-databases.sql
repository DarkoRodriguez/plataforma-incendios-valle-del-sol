-- Crear bases de datos para cada microservicio
CREATE DATABASE IF NOT EXISTS ms_usuarios;
CREATE DATABASE IF NOT EXISTS ms_mapeo;
CREATE DATABASE IF NOT EXISTS demo_db;

-- Asignar permisos
GRANT ALL PRIVILEGES ON DATABASE ms_usuarios TO postgres;
GRANT ALL PRIVILEGES ON DATABASE ms_mapeo TO postgres;
GRANT ALL PRIVILEGES ON DATABASE demo_db TO postgres;
