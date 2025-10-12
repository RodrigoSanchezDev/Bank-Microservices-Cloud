-- Script de inicialización de bases de datos
-- PostgreSQL ejecuta automáticamente los scripts .sql en /docker-entrypoint-initdb.d/

-- Crear base de datos para Account Service
SELECT 'CREATE DATABASE accountdb'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'accountdb')\gexec

-- Crear base de datos para Customer Service
SELECT 'CREATE DATABASE customerdb'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'customerdb')\gexec

-- Crear base de datos para Transaction Service
SELECT 'CREATE DATABASE transactiondb'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'transactiondb')\gexec

-- Crear base de datos para Batch Service (Spring Batch)
SELECT 'CREATE DATABASE batchdb'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'batchdb')\gexec

-- Otorgar todos los privilegios al usuario 'bank'
GRANT ALL PRIVILEGES ON DATABASE accountdb TO bank;
GRANT ALL PRIVILEGES ON DATABASE customerdb TO bank;
GRANT ALL PRIVILEGES ON DATABASE transactiondb TO bank;
GRANT ALL PRIVILEGES ON DATABASE batchdb TO bank;
