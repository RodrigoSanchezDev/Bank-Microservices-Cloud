-- Script de inicialización de bases de datos
-- PostgreSQL ejecuta automáticamente los scripts .sql en /docker-entrypoint-initdb.d/

-- Crear base de datos para Account Service
CREATE DATABASE accountdb;

-- Crear base de datos para Customer Service
CREATE DATABASE customerdb;

-- Crear base de datos para Transaction Service
CREATE DATABASE transactiondb;

-- Otorgar todos los privilegios al usuario 'bank'
GRANT ALL PRIVILEGES ON DATABASE accountdb TO bank;
GRANT ALL PRIVILEGES ON DATABASE customerdb TO bank;
GRANT ALL PRIVILEGES ON DATABASE transactiondb TO bank;
