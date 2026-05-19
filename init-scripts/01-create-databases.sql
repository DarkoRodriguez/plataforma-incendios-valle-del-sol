-- Crear bases de datos para cada microservicio
CREATE DATABASE ms_usuarios;
CREATE DATABASE ms_mapeo;
CREATE DATABASE demo_db;

-- Asignar permisos
GRANT ALL PRIVILEGES ON DATABASE ms_usuarios TO postgres;
GRANT ALL PRIVILEGES ON DATABASE ms_mapeo TO postgres;
GRANT ALL PRIVILEGES ON DATABASE demo_db TO postgres;
