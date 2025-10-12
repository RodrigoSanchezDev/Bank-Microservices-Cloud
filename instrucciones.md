# Instrucciones de Ejecución y Prueba - Bank Microservices Cloud

> Guía paso a paso para ejecutar y probar cada componente del sistema de microservicios bancarios

---

## 📋 Tabla de Contenidos

1. [Requisitos Previos](#requisitos-previos)
2. [Opción 1: Ejecución con Docker Compose](#opción-1-ejecución-con-docker-compose-recomendado)
3. [Opción 2: Ejecución Local Manual](#opción-2-ejecución-local-manual)
4. [Verificación de Servicios](#verificación-de-servicios)
5. [Pruebas de Funcionalidad](#pruebas-de-funcionalidad)
6. [Pruebas con Postman](#pruebas-con-postman)
7. [Pruebas de Kafka](#pruebas-de-kafka)
8. [Troubleshooting](#troubleshooting)

---

## Requisitos Previos

### Software Necesario

| Software       | Versión Mínima | Comando de Verificación    |
| -------------- | -------------- | -------------------------- |
| Java JDK       | 21             | `java -version`            |
| Maven          | 3.8+           | `mvn -version`             |
| Docker         | 20.10+         | `docker --version`         |
| Docker Compose | 2.0+           | `docker-compose --version` |
| Git            | 2.0+           | `git --version`            |
| curl           | 7.0+           | `curl --version`           |

### Verificar Instalaciones

```bash
# Verificar todas las herramientas
java -version
mvn -version
docker --version
docker-compose --version
git --version
curl --version
```

### Recursos Mínimos Requeridos

- **RAM**: 8 GB (recomendado 16 GB)
- **CPU**: 4 cores
- **Disco**: 5 GB libres
- **Puertos disponibles**: 8081, 8082, 8083, 8084, 8443, 8761, 8888, 8090, 5432, 9092, 2181

---

## Opción 1: Ejecución con Docker Compose (Recomendado)

### Paso 1: Clonar el Repositorio

```bash
# Clonar desde GitHub
git clone https://github.com/RodrigoSanchezDev/bank-microservices-cloud.git
cd bank-microservices-cloud
```

### Paso 2: Compilar el Proyecto

```bash
# Compilar todos los módulos con Maven
mvn clean package -DskipTests

# Verificar que los JARs se generaron correctamente
ls -lh */target/*.jar | grep -v '.original'
```

**Salida esperada**: 7 archivos JAR generados:

- ✅ account-service-1.0.0.jar
- ✅ api-gateway-bff-1.0.0.jar
- ✅ batch-service-1.0.0.jar
- ✅ config-server-1.0.0.jar
- ✅ customer-service-1.0.0.jar
- ✅ eureka-server-1.0.0.jar
- ✅ transaction-service-1.0.0.jar

### Paso 3: Iniciar Todos los Servicios

```bash
# Iniciar todos los contenedores en modo detached
docker-compose up -d

# Ver el estado de los contenedores
docker-compose ps

# Ver logs en tiempo real
docker-compose logs -f
```

### Paso 4: Esperar a que los Servicios Estén Listos

**Tiempo estimado**: 5-7 minutos

```bash
# Monitorear el estado de los contenedores
watch -n 10 'docker-compose ps'

# Verificar logs de un servicio específico
docker-compose logs -f customer-service
```

**Estado esperado**: 11 contenedores healthy/running:

- ✅ bank-config-server (healthy)
- ✅ bank-eureka-server (healthy)
- ✅ bank-postgres (healthy)
- ✅ bank-zookeeper (healthy)
- ✅ bank-kafka (healthy)
- ✅ bank-kafka-ui (healthy)
- ✅ bank-account-service (healthy)
- ✅ bank-customer-service (healthy)
- ✅ bank-transaction-service (healthy)
- ✅ bank-batch-service (healthy)
- ✅ bank-api-gateway-bff (running)

### Paso 5: Detener los Servicios

```bash
# Detener todos los contenedores
docker-compose down

# Detener y eliminar volúmenes (limpieza completa)
docker-compose down -v
```

---

## Opción 2: Ejecución Local Manual

### Paso 1: Compilar el Proyecto

```bash
# Navegar al directorio del proyecto
cd bank-microservices-cloud

# Compilar todos los módulos
mvn clean install -DskipTests
```

### Paso 2: Iniciar PostgreSQL (Terminal 1)

```bash
# Opción A: Usando Docker
docker run --name bank-postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_USER=postgres \
  -p 5432:5432 \
  -d postgres:15-alpine

# Opción B: Instalación local de PostgreSQL
# Asegúrate de tener PostgreSQL instalado y corriendo en puerto 5432
```

### Paso 3: Iniciar Config Server (Terminal 2)

```bash
cd config-server
mvn spring-boot:run

# Esperar mensaje: "Started ConfigServerApplication"
# URL: http://localhost:8888
```

### Paso 4: Iniciar Eureka Server (Terminal 3)

```bash
cd eureka-server
mvn spring-boot:run

# Esperar mensaje: "Started EurekaServerApplication"
# URL: http://localhost:8761
```

### Paso 5: Iniciar Account Service (Terminal 4)

```bash
cd account-service
mvn spring-boot:run

# Esperar mensaje: "Started AccountServiceApplication"
# URL: http://localhost:8081
```

### Paso 6: Iniciar Customer Service (Terminal 5)

```bash
cd customer-service
mvn spring-boot:run

# Esperar mensaje: "Started CustomerServiceApplication"
# URL: http://localhost:8082
```

### Paso 7: Iniciar Transaction Service (Terminal 6)

```bash
cd transaction-service
mvn spring-boot:run

# Esperar mensaje: "Started TransactionServiceApplication"
# URL: http://localhost:8083
```

### Paso 8: Iniciar Batch Service (Terminal 7)

```bash
cd batch-service
mvn spring-boot:run

# Esperar mensaje: "Started BatchServiceApplication"
# URL: http://localhost:8084
```

### Paso 9: Iniciar API Gateway BFF (Terminal 8)

```bash
cd api-gateway-bff
mvn spring-boot:run

# Esperar mensaje: "Started BffGatewayApplication"
# URL: https://localhost:8443
```

### Paso 10: Iniciar Kafka (Opcional - para eventos)

```bash
# Iniciar Zookeeper
docker run --name zookeeper -p 2181:2181 -d confluentinc/cp-zookeeper:7.5.0

# Iniciar Kafka
docker run --name kafka \
  -p 9092:9092 \
  -p 29092:29092 \
  -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 \
  -d confluentinc/cp-kafka:7.5.0

# Iniciar Kafka UI
docker run --name kafka-ui \
  -p 8090:8080 \
  -e KAFKA_CLUSTERS_0_NAME=bank-cluster \
  -e KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=localhost:9092 \
  -d provectuslabs/kafka-ui:latest
```

---

## Verificación de Servicios

### 1. Verificar Config Server

```bash
# Health check
curl http://localhost:8888/actuator/health

# Obtener configuración de un servicio
curl http://localhost:8888/account-service/default
curl http://localhost:8888/customer-service/default
curl http://localhost:8888/transaction-service/default
```

**Respuesta esperada**:

```json
{
  "status": "UP"
}
```

### 2. Verificar Eureka Server

```bash
# Abrir en navegador
open http://localhost:8761

# O verificar desde terminal
curl http://localhost:8761/eureka/apps
```

**Servicios esperados registrados en Eureka**:

- ✅ API-GATEWAY-BFF
- ✅ ACCOUNT-SERVICE
- ✅ CUSTOMER-SERVICE
- ✅ TRANSACTION-SERVICE
- ✅ BATCH-SERVICE

### 3. Verificar API Gateway BFF

```bash
# Health check (HTTPS con certificado autofirmado)
curl -k https://localhost:8443/actuator/health

# Verificar endpoint de login
curl -k -X POST https://localhost:8443/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**Respuesta esperada del login**:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin"
}
```

### 4. Verificar Account Service

```bash
# Health check
curl http://localhost:8081/actuator/health

# Verificar Circuit Breakers
curl http://localhost:8081/actuator/health/circuitbreakers
```

### 5. Verificar Customer Service

```bash
# Health check
curl http://localhost:8082/actuator/health

# Verificar Swagger UI
open http://localhost:8082/swagger-ui.html
```

### 6. Verificar Transaction Service

```bash
# Health check
curl http://localhost:8083/actuator/health

# Verificar métricas
curl http://localhost:8083/actuator/metrics
```

### 7. Verificar Kafka UI (si está configurado)

```bash
# Abrir interfaz web
open http://localhost:8090

# Verificar topics desde terminal
docker exec -it bank-kafka kafka-topics \
  --bootstrap-server localhost:9092 --list
```

### 8. Verificar PostgreSQL

```bash
# Conectarse a la base de datos
docker exec -it bank-postgres psql -U postgres

# Listar bases de datos
\l

# Salir
\q
```

**Bases de datos esperadas**:

- ✅ bankdb (Account Service)
- ✅ customerdb (Customer Service)
- ✅ transactiondb (Transaction Service)

---

## Pruebas de Funcionalidad

### 1. Autenticación JWT

#### Obtener Token de Acceso

```bash
# Login como administrador
curl -k -X POST https://localhost:8443/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# Guardar el token en una variable (bash)
TOKEN=$(curl -k -s -X POST https://localhost:8443/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.token')

echo $TOKEN
```

### 2. Pruebas de Account Service

#### Listar Cuentas

```bash
curl -k -X GET https://localhost:8443/api/accounts \
  -H "Authorization: Bearer $TOKEN"
```

#### Crear Cuenta

```bash
curl -k -X POST https://localhost:8443/api/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "1234567890",
    "accountHolder": "Juan Pérez",
    "balance": 1000.00,
    "accountType": "SAVINGS"
  }'
```

#### Obtener Cuenta por ID

```bash
curl -k -X GET https://localhost:8443/api/accounts/1 \
  -H "Authorization: Bearer $TOKEN"
```

#### Actualizar Cuenta

```bash
curl -k -X PUT https://localhost:8443/api/accounts/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountHolder": "Juan Carlos Pérez",
    "balance": 2000.00
  }'
```

#### Eliminar Cuenta

```bash
curl -k -X DELETE https://localhost:8443/api/accounts/1 \
  -H "Authorization: Bearer $TOKEN"
```

#### Endpoints Legacy de Account Service

```bash
# Listar transacciones
curl -k -X GET https://localhost:8443/api/accounts/legacy/transacciones \
  -H "Authorization: Bearer $TOKEN"

# Listar intereses
curl -k -X GET https://localhost:8443/api/accounts/legacy/intereses \
  -H "Authorization: Bearer $TOKEN"

# Cuentas anuales
curl -k -X GET https://localhost:8443/api/accounts/legacy/cuentas-anuales \
  -H "Authorization: Bearer $TOKEN"

# Resumen general
curl -k -X GET https://localhost:8443/api/accounts/legacy/resumen-general \
  -H "Authorization: Bearer $TOKEN"
```

### 3. Pruebas de Customer Service

#### Listar Clientes

```bash
curl -k -X GET https://localhost:8443/api/customers \
  -H "Authorization: Bearer $TOKEN"
```

#### Crear Cliente

```bash
curl -k -X POST https://localhost:8443/api/customers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "rut": "12345678-9",
    "firstName": "Juan",
    "lastName": "Pérez",
    "email": "juan.perez@example.com",
    "phone": "+56912345678",
    "address": "Santiago, Chile",
    "status": "ACTIVE"
  }'
```

#### Buscar Cliente por RUT

```bash
curl -k -X GET https://localhost:8443/api/customers/rut/12345678-9 \
  -H "Authorization: Bearer $TOKEN"
```

#### Buscar Cliente por Email

```bash
curl -k -X GET https://localhost:8443/api/customers/email/juan.perez@example.com \
  -H "Authorization: Bearer $TOKEN"
```

#### Actualizar Cliente

```bash
curl -k -X PUT https://localhost:8443/api/customers/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Juan Carlos",
    "lastName": "Pérez González",
    "phone": "+56987654321",
    "status": "ACTIVE"
  }'
```

#### Eliminar Cliente

```bash
curl -k -X DELETE https://localhost:8443/api/customers/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Pruebas de Transaction Service

#### Listar Transacciones

```bash
curl -k -X GET https://localhost:8443/api/transactions \
  -H "Authorization: Bearer $TOKEN"
```

#### Crear Transacción

```bash
curl -k -X POST https://localhost:8443/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": 1,
    "customerId": 1,
    "type": "DEPOSIT",
    "amount": 50000.00,
    "description": "Depósito inicial",
    "status": "PENDING"
  }'
```

#### Buscar Transacciones por Cuenta

```bash
curl -k -X GET https://localhost:8443/api/transactions/account/1 \
  -H "Authorization: Bearer $TOKEN"
```

#### Buscar Transacciones por Cliente

```bash
curl -k -X GET https://localhost:8443/api/transactions/customer/1 \
  -H "Authorization: Bearer $TOKEN"
```

#### Actualizar Transacción

```bash
curl -k -X PUT https://localhost:8443/api/transactions/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "COMPLETED",
    "description": "Transacción completada exitosamente"
  }'
```

#### Eliminar Transacción

```bash
curl -k -X DELETE https://localhost:8443/api/transactions/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

## Pruebas con Postman

### Importar Colección

1. Abrir Postman
2. Click en **Import**
3. Seleccionar archivo `postman-collection.json`
4. La colección incluye todos los endpoints del sistema

### Configurar Variables de Entorno

Crear un entorno en Postman con las siguientes variables:

| Variable   | Valor                           |
| ---------- | ------------------------------- |
| `base_url` | `https://localhost:8443`        |
| `token`    | (se auto-genera al hacer login) |

### Ejecutar Tests

1. **Autenticación**: Ejecutar el request de Login primero
2. El token JWT se guarda automáticamente en la variable `token`
3. Todos los demás requests usan este token automáticamente

### Colecciones Disponibles

- ✅ **Authentication** (1 request)
- ✅ **Account Service** (11 requests)
- ✅ **Customer Service** (8 requests)
- ✅ **Transaction Service** (8 requests)
- ✅ **Health Checks** (6 requests)

---

## Pruebas de Kafka

### Script Automatizado de Tests

```bash
# Dar permisos de ejecución
chmod +x test-kafka.sh

# Ejecutar tests de Kafka
./test-kafka.sh
```

### Tests Manuales de Kafka

#### 1. Verificar Kafka UI

```bash
# Abrir en navegador
open http://localhost:8090
```

#### 2. Crear Cliente y Verificar Evento

```bash
# Crear un cliente
curl -k -X POST https://localhost:8443/api/customers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "rut": "98765432-1",
    "firstName": "María",
    "lastName": "González",
    "email": "maria.gonzalez@example.com",
    "phone": "+56923456789",
    "address": "Valparaíso, Chile",
    "status": "ACTIVE"
  }'

# Ver logs del Consumer (Transaction Service)
docker logs bank-transaction-service | grep "CustomerCreated"
```

#### 3. Ver Topics de Kafka

```bash
# Listar topics
docker exec -it bank-kafka kafka-topics \
  --bootstrap-server localhost:9092 --list

# Describir topic de eventos de clientes
docker exec -it bank-kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic customer-created-events
```

#### 4. Consumir Mensajes de Kafka

```bash
# Consumir todos los mensajes desde el inicio
docker exec -it bank-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic customer-created-events \
  --from-beginning

# Ctrl+C para salir
```

#### 5. Verificar Consumer Groups

```bash
# Listar consumer groups
docker exec -it bank-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 --list

# Ver detalles del consumer group
docker exec -it bank-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group customer-consumer-group
```

---

## Troubleshooting

### Problema: Puerto ya en uso

```bash
# Identificar proceso usando el puerto
lsof -i :8081  # Cambiar por el puerto problemático

# Matar el proceso
kill -9 <PID>
```

### Problema: Contenedores no inician

```bash
# Ver logs detallados
docker-compose logs -f <nombre-servicio>

# Reiniciar un servicio específico
docker-compose restart <nombre-servicio>

# Reiniciar todos los servicios
docker-compose down
docker-compose up -d
```

### Problema: Error de conexión a PostgreSQL

```bash
# Verificar que PostgreSQL está corriendo
docker-compose ps postgres

# Ver logs de PostgreSQL
docker-compose logs -f postgres

# Reiniciar PostgreSQL
docker-compose restart postgres
```

### Problema: Servicios no se registran en Eureka

```bash
# Esperar 2-3 minutos más (los servicios tardan en registrarse)

# Verificar logs de Eureka
docker-compose logs -f eureka-server

# Verificar logs del microservicio
docker-compose logs -f customer-service

# Reiniciar Eureka
docker-compose restart eureka-server
```

### Problema: Error 401 Unauthorized

```bash
# Verificar que el token es válido
echo $TOKEN

# Obtener un nuevo token
TOKEN=$(curl -k -s -X POST https://localhost:8443/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.token')

# Verificar credenciales
# Usuario: admin
# Password: admin123
```

### Problema: Certificado SSL no confiable

```bash
# Usar la opción -k en curl para ignorar verificación SSL
curl -k https://localhost:8443/...

# En navegador: Click en "Avanzado" → "Continuar de todos modos"
```

### Problema: Out of Memory

```bash
# Liberar memoria
docker system prune -f

# Reiniciar Docker
# macOS: Restart Docker Desktop
# Linux: sudo systemctl restart docker

# Aumentar memoria asignada a Docker
# Docker Desktop → Preferences → Resources → Memory: 8GB+
```

### Problema: Kafka no funciona

```bash
# Verificar contenedores de Kafka
docker-compose ps zookeeper kafka kafka-ui

# Reiniciar stack de Kafka
docker-compose restart zookeeper kafka kafka-ui

# Ver logs de Kafka
docker-compose logs -f kafka
```

---

## Comandos Útiles de Gestión

### Docker Compose

```bash
# Ver estado de todos los contenedores
docker-compose ps

# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f customer-service

# Reiniciar un servicio
docker-compose restart customer-service

# Detener todos los servicios
docker-compose down

# Detener y eliminar volúmenes
docker-compose down -v

# Reconstruir imágenes
docker-compose build

# Iniciar con reconstrucción
docker-compose up -d --build
```

### Docker

```bash
# Listar contenedores en ejecución
docker ps

# Listar todas las imágenes
docker images

# Ver uso de recursos
docker stats

# Limpiar sistema
docker system prune -f

# Limpiar volúmenes
docker volume prune -f

# Ver logs de un contenedor
docker logs -f <container-name>

# Ejecutar comando en contenedor
docker exec -it <container-name> /bin/sh
```

### Maven

```bash
# Compilar sin tests
mvn clean package -DskipTests

# Compilar con tests
mvn clean test

# Ejecutar un servicio específico
cd <servicio> && mvn spring-boot:run

# Ver árbol de dependencias
mvn dependency:tree

# Actualizar dependencias
mvn versions:display-dependency-updates
```

---

## Resumen de Endpoints

### Infraestructura

| Servicio        | URL                    | Descripción                |
| --------------- | ---------------------- | -------------------------- |
| Config Server   | http://localhost:8888  | Configuración centralizada |
| Eureka Server   | http://localhost:8761  | Service Discovery          |
| API Gateway BFF | https://localhost:8443 | Punto de entrada HTTPS     |
| Kafka UI        | http://localhost:8090  | Visualización de eventos   |

### Microservicios (acceso directo)

| Servicio            | URL                   | Swagger UI                            |
| ------------------- | --------------------- | ------------------------------------- |
| Account Service     | http://localhost:8081 | http://localhost:8081/swagger-ui.html |
| Customer Service    | http://localhost:8082 | http://localhost:8082/swagger-ui.html |
| Transaction Service | http://localhost:8083 | http://localhost:8083/swagger-ui.html |
| Batch Service       | http://localhost:8084 | http://localhost:8084/swagger-ui.html |

### Endpoints de Negocio (a través del BFF)

| Categoría           | Endpoints | Base URL                                |
| ------------------- | --------- | --------------------------------------- |
| Autenticación       | 1         | https://localhost:8443/api/auth         |
| Account Service     | 11        | https://localhost:8443/api/accounts     |
| Customer Service    | 8         | https://localhost:8443/api/customers    |
| Transaction Service | 8         | https://localhost:8443/api/transactions |

**Total**: 28 endpoints funcionales

---

## Conclusión

Este documento proporciona todas las instrucciones necesarias para ejecutar y probar el sistema de microservicios bancarios. Para desplegar en un entorno de nube, consultar el archivo **`despliegue.md`**.

**Desarrollado por Rodrigo Sanchez**  
Copyright © 2025 - Todos los derechos reservados
