# Bank Microservices Cloud

> Sistema bancario distribuido de alto rendimiento construido con arquitectura de microservicios

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.0-blue.svg)](https://spring.io/projects/spring-cloud)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![Resilience4j](https://img.shields.io/badge/Resilience4j-2.x-green.svg)](https://resilience4j.readme.io/)

---

## ❓ Preguntas Frecuentes (FAQ)

<details>
<summary><strong>¿Por qué usar microservicios en lugar de un monolito?</strong></summary>

**Ventajas**:

- **Escalabilidad independiente**: Escala solo los servicios que lo necesitan
- **Despliegue independiente**: Actualiza sin afectar todo el sistema
- **Tecnología heterogénea**: Usa el stack más adecuado para cada servicio
- **Resiliencia**: Fallas aisladas, no colapsa todo el sistema
- **Equipos autónomos**: Desarrollo y despliegue descentralizado

**Desventajas**:

- Mayor complejidad operacional
- Necesidad de herramientas de orquestación (Docker, Kubernetes)
- Debugging distribuido más complejo

</details>

<details>
<summary><strong>¿Cómo funciona el Circuit Breaker?</strong></summary>

El **Circuit Breaker** monitorea las peticiones a servicios externos:

1. **CLOSED** (Estado Normal):

   - Todas las peticiones pasan normalmente
   - Registra éxitos y fallos

2. **OPEN** (Servicio Caído):

   - Se alcanza el umbral de fallos (50% en 10 peticiones)
   - Peticiones fallan inmediatamente sin llamar al servicio
   - Espera 10 segundos antes de intentar recuperación

3. **HALF_OPEN** (Prueba de Recuperación):
   - Permite 3 peticiones de prueba
   - Si tienen éxito → CLOSED
   - Si fallan → OPEN

**Beneficio**: Evita sobrecargar servicios caídos y falla rápidamente

</details>

<details>
<summary><strong>¿Es necesario usar Docker?</strong></summary>

**No es obligatorio**, pero es altamente recomendado:

**Sin Docker**:

```bash
# Iniciar cada servicio manualmente
cd config-server && mvn spring-boot:run
cd eureka-server && mvn spring-boot:run
cd account-service && mvn spring-boot:run
```

**Con Docker**:

```bash
# Un solo comando
docker-compose up -d
```

**Ventajas de Docker**:

- Entorno consistente (desarrollo = producción)
- Networking automático entre servicios
- Gestión de dependencias (PostgreSQL, Redis)
- Escalabilidad horizontal simple

</details>

<details>
<summary><strong>¿Cómo agrego un nuevo microservicio?</strong></summary>

**Paso 1**: Crear el módulo Maven

```bash
cd bank-microservices-cloud
mkdir customer-service
cd customer-service
# Copiar estructura de account-service
```

**Paso 2**: Configurar `pom.xml`

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-config</artifactId>
    </dependency>
</dependencies>
```

**Paso 3**: Crear configuración en Config Server

```yaml
# config-server/src/main/resources/config-repo/customer-service.yml
spring:
  application:
    name: customer-service
server:
  port: 8082
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

**Paso 4**: Registrar en Eureka

```java
@SpringBootApplication
@EnableDiscoveryClient
public class CustomerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}
```

**Paso 5**: Agregar a Docker Compose

```yaml
customer-service:
  build: ./customer-service
  ports:
    - "8082:8082"
  depends_on:
    - config-server
    - eureka-server
```

</details>

<details>
<summary><strong>¿Cómo funciona la autenticación JWT?</strong></summary>

**Flujo de Autenticación**:

1. **Login**:

   ```bash
   POST /api/auth/login
   Body: { "username": "user1", "password": "password123" }
   ```

2. **Respuesta con Token**:

   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "expiresAt": "2024-12-31T23:59:59Z"
   }
   ```

3. **Uso del Token**:
   ```bash
   GET /api/accounts
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

**Componentes**:

- `JwtTokenProvider`: Genera y valida tokens
- `JwtAuthenticationFilter`: Intercepta peticiones y valida tokens
- `SecurityConfig`: Define rutas protegidas

**Expiración**: Tokens válidos por 24 horas

</details>

<details>
<summary><strong>¿Qué hacer si Eureka no muestra los servicios?</strong></summary>

**Checklist de Diagnóstico**:

1. **Verificar que Eureka esté corriendo**:

   ```bash
   curl http://localhost:8761
   # Debe retornar la UI de Eureka
   ```

2. **Revisar logs del servicio**:

   ```bash
   docker logs account-service
   # Buscar: "DiscoveryClient_ACCOUNT-SERVICE"
   ```

3. **Verificar configuración**:

   ```yaml
   eureka:
     client:
       service-url:
         defaultZone: http://eureka-server:8761/eureka/
       fetch-registry: true
       register-with-eureka: true
   ```

4. **Esperar el registro**:

   - Los servicios tardan ~30 segundos en aparecer
   - Eureka tiene un mecanismo de caché

5. **Revisar networking (Docker)**:
   ```bash
   docker network inspect bank-microservices-cloud_default
   # Verificar que todos los servicios estén en la misma red
   ```

</details>

---

## 🗺️ Roadmap

### 📌 v1.0.0 - Sistema Base (Actual)

- ✅ Arquitectura de Microservicios
- ✅ Config Server (Centralizado)
- ✅ Eureka Discovery Service
- ✅ Account Service (CRUD + CSV Migration)
- ✅ Patrones de Resiliencia (Resilience4j)
- ✅ Autenticación JWT
- ✅ Containerización Docker
- ✅ Spring Boot 3.5.0
- ✅ Spring Cloud 2024.0.0
- ✅ Java 21

### 🚀 v1.1.0 - Mejoras de Infraestructura (Próximo)

- 🔄 **API Gateway (Spring Cloud Gateway)**

  - Enrutamiento centralizado
  - Rate limiting global
  - Autenticación unificada

- 🔄 **Tracing Distribuido**

  - Micrometer Tracing
  - Zipkin para visualización
  - Correlación de requests entre servicios

- 🔄 **Monitoreo Avanzado**

  - Prometheus para métricas
  - Grafana dashboards
  - Alertas automatizadas

- 🔄 **Caché Distribuido**

  - Redis para sesiones
  - Caché de consultas frecuentes

- 🔄 **Mensajería Asíncrona**
  - Apache Kafka / RabbitMQ
  - Event-driven architecture
  - SAGA Pattern para transacciones distribuidas

### 🌟 v2.0.0 - Escalabilidad y Cloud Native (Futuro)

- ⏳ **Programación Reactiva**

  - Spring WebFlux
  - Non-blocking I/O
  - Backpressure handling

- ⏳ **API GraphQL**

  - Consultas flexibles
  - Reducción de overfetching

- ⏳ **SAGA Pattern**

  - Transacciones distribuidas
  - Compensación automática

- ⏳ **Service Mesh**

  - Istio / Linkerd
  - mTLS automático
  - Observabilidad avanzada

- ⏳ **Kubernetes**

  - Orquestación de contenedores
  - Auto-scaling
  - Self-healing

- ⏳ **CI/CD**
  - GitHub Actions / GitLab CI
  - Despliegue automatizado
  - Tests de integración

---

## 📚 Recursos y Referencias

### Documentación Oficial

- [Spring Boot 3.5.x Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Cloud 2024.0.x Documentation](https://docs.spring.io/spring-cloud/docs/current/reference/html/)
- [Resilience4j Official Guide](https://resilience4j.readme.io/)
- [Netflix Eureka Wiki](https://github.com/Netflix/eureka/wiki)

### Artículos Recomendados

- [Microservices Patterns - Chris Richardson](https://microservices.io/patterns/index.html)
- [The Twelve-Factor App](https://12factor.net/)
- [Circuit Breaker Pattern - Martin Fowler](https://martinfowler.com/bliki/CircuitBreaker.html)
- [SAGA Pattern Explained](https://microservices.io/patterns/data/saga.html)

### Tutoriales

- [Spring Cloud Netflix Eureka Tutorial](https://spring.io/guides/gs/service-registration-and-discovery/)
- [Resilience4j with Spring Boot](https://resilience4j.readme.io/docs/getting-started-3)
- [Docker Compose for Microservices](https://docs.docker.com/compose/gettingstarted/)

---

## 📑 Tabla de Contenidos

- [Descripción](#-descripción)
- [Arquitectura](#️-arquitectura)
- [Componentes](#-componentes)
- [Modelo de Datos](#-modelo-de-datos)
- [Stack Tecnológico](#️-stack-tecnológico)
- [Inicio Rápido](#-inicio-rápido)
- [Seguridad](#-seguridad)
- [Patrones de Resiliencia](#️-patrones-de-resiliencia)
- [Testing y Calidad](#-testing-y-calidad)
- [Monitoreo y Observabilidad](#-monitoreo-y-observabilidad)
- [Preguntas Frecuentes (FAQ)](#-preguntas-frecuentes-faq)
- [Roadmap](#️-roadmap)
- [Recursos y Referencias](#-recursos-y-referencias)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Patrones Implementados](#-patrones-implementados)
- [Despliegue en Producción](#-despliegue-en-producción)
- [Contribuciones](#-contribuciones)
- [Contacto](#-contacto)
- [Licencia](#-licencia)

---

## 📋 Descripción

Plataforma empresarial de microservicios para gestión bancaria que implementa patrones avanzados de resiliencia, configuración centralizada, descubrimiento de servicios y seguridad distribuida mediante Spring Cloud y Resilience4j.

### Características Principales

- ✅ **Arquitectura de Microservicios** escalable y distribuida
- ✅ **Configuración Centralizada** con Spring Cloud Config
- ✅ **Service Discovery** con Netflix Eureka
- ✅ **Autenticación JWT** y seguridad distribuida
- ✅ **Patrones de Resiliencia** (Circuit Breaker, Retry, Rate Limiting)
- ✅ **Contenedorización** con Docker
- ✅ **API RESTful** documentada con Swagger/OpenAPI
- ✅ **Monitoreo** con Spring Actuator

---

## 🏗️ Arquitectura

### Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────┐
│              API Gateway (8080)                          │
│         Autenticación JWT + Enrutamiento                │
└───────────────────────┬─────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
┌───────▼──────┐ ┌─────▼──────┐ ┌─────▼──────┐
│Config Server │ │   Eureka   │ │  Account   │
│   (8888)     │ │   Server   │ │  Service   │
│              │ │   (8761)   │ │   (8081)   │
└──────────────┘ └────────────┘ └─────┬──────┘
                                       │
                                ┌──────▼──────┐
                                │ PostgreSQL  │
                                │   (5432)    │
                                └─────────────┘
```

### Principios Arquitectónicos

- **Independencia de Servicios**: Cada microservicio puede desplegarse independientemente
- **Configuración Externalizada**: Configuraciones centralizadas en Config Server
- **Descubrimiento Dinámico**: Registro automático de servicios en Eureka
- **Resiliencia**: Implementación de Circuit Breaker, Retry y Rate Limiting
- **Seguridad Distribuida**: Autenticación JWT en cada microservicio

---

## 🚀 Componentes

### Config Server (Puerto 8888)

**Servidor de configuración centralizada**

- Gestiona configuraciones de todos los microservicios
- Soporte para perfiles de ambiente (dev, prod)
- Actualización de configuración en tiempo real

### Eureka Server (Puerto 8761)

**Service Discovery y Service Registry**

- Registro automático de microservicios
- Dashboard web para monitoreo
- Detección de servicios caídos (heartbeat)
- Balanceo de carga del lado del cliente

### Account Service (Puerto 8081)

**Microservicio de gestión bancaria**

**Características**:

- API RESTful para operaciones CRUD de cuentas
- Procesamiento de datos legacy del sistema bancario
- Autenticación y autorización JWT
- Integración con PostgreSQL mediante JPA
- Documentación Swagger/OpenAPI
- Métricas y health checks con Actuator

**Patrones de Resiliencia**:

- Circuit Breaker (protección contra fallos en cascada)
- Retry (reintentos automáticos)
- Rate Limiter (control de tráfico: 10 req/min)
- Time Limiter (timeout en operaciones)

### API Gateway (Puerto 8080)

**Punto de entrada unificado** _(Opcional - Recomendado para producción)_

- Enrutamiento inteligente de peticiones
- Autenticación centralizada
- Rate limiting global
- Logging y monitoreo centralizado

---

## 📊 Modelo de Datos

### Entidades Principales

**Cuentas Bancarias** (`accounts`)

- Gestión completa de cuentas
- Tipos: Ahorros, Corriente, Nómina
- Control de saldos y estados

**Transacciones** (`transactions`)

- Registro de movimientos financieros
- Tipos: Depósito, Retiro, Transferencia
- Validación de reglas de negocio

**Intereses** (`interests`)

- Cálculo automático de intereses
- Aplicación mensual según tipo de cuenta
- Historial de aplicaciones

**Usuarios** (`users`)

- Autenticación y autorización
- Roles: ADMIN, USER
- Gestión de credenciales JWT

### Referencia de Datos Legacy

Basado en el dataset [bank_legacy_data](https://github.com/KariVillagran/bank_legacy_data) para procesamiento de información histórica.

---

## 🛠️ Stack Tecnológico

| Tecnología      | Versión  | Propósito                   |
| --------------- | -------- | --------------------------- |
| Java            | 21       | Lenguaje de programación    |
| Spring Boot     | 3.5.0    | Framework de aplicación     |
| Spring Cloud    | 2024.0.0 | Framework de microservicios |
| Spring Security | 6.x      | Seguridad y autenticación   |
| Resilience4j    | 2.x      | Patrones de resiliencia     |
| PostgreSQL      | 15+      | Base de datos relacional    |
| Docker          | Latest   | Contenedorización           |
| Maven           | 3.8+     | Gestión de dependencias     |
| Lombok          | 1.18.34  | Reducción de boilerplate    |

---

## 🚀 Inicio Rápido

### Requisitos Previos

- **JDK**: 21 o superior
- **Maven**: 3.8 o superior
- **Docker**: 20.10 o superior
- **Docker Compose**: 2.0 o superior

### Instalación

#### Opción 1: Docker Compose (Recomendado)

```bash
# Clonar el repositorio
git clone https://github.com/RodrigoSanchezDev/bank-microservices-cloud.git
cd bank-microservices-cloud

# Construir y levantar todos los servicios
docker-compose up -d

# Verificar estado
docker-compose ps

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down
```

#### Opción 2: Ejecución Local

```bash
# 1. Compilar
mvn clean install -DskipTests

# 2. Iniciar Config Server (Terminal 1)
cd config-server && mvn spring-boot:run

# 3. Iniciar Eureka Server (Terminal 2)
cd eureka-server && mvn spring-boot:run

# 4. Iniciar Account Service (Terminal 3)
cd account-service && mvn spring-boot:run
```

### Verificación

Espera ~60 segundos para que los servicios se registren.

- **Config Server**: http://localhost:8888/actuator/health
- **Eureka Dashboard**: http://localhost:8761
- **Account Service**: http://localhost:8081/actuator/health
- **Swagger UI**: http://localhost:8081/swagger-ui.html

---

## 📍 Endpoints Principales

### Eureka Dashboard

```
http://localhost:8761
```

### Config Server

```
http://localhost:8888/account-service/default
```

### Account Service API

**Autenticación (Obtener JWT)**

```bash
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

### Operaciones de Cuentas (requiere JWT)

```bash
# Listar cuentas
GET http://localhost:8081/api/accounts
Authorization: Bearer {token}

# Crear cuenta
POST http://localhost:8081/api/accounts
Authorization: Bearer {token}
Content-Type: application/json

{
  "accountNumber": "1234567890",
  "accountHolder": "Juan Pérez",
  "balance": 1000.00,
  "accountType": "SAVINGS"
}

# Obtener cuenta por ID
GET http://localhost:8081/api/accounts/{id}
Authorization: Bearer {token}
```

### Health & Monitoreo

```bash
# Health check
GET http://localhost:8081/actuator/health

# Circuit Breaker estado
GET http://localhost:8081/actuator/health/circuitbreakers

# Métricas
GET http://localhost:8081/actuator/metrics
```

---

## 🔒 Seguridad

### Autenticación JWT

**Configuración**:

- Algoritmo: HS512
- Tiempo de vida: 24 horas
- Claims: username, roles, authorities

**Credenciales por Defecto**:

- Username: `admin`
- Password: `admin123`
- Rol: `ADMIN`

⚠️ **Importante**: Cambia las credenciales en producción y utiliza variables de entorno.

### Endpoints Protegidos

- ✅ Todos los endpoints bajo `/api/accounts` requieren autenticación
- ✅ Endpoints administrativos requieren rol `ADMIN`
- ✅ Endpoints de lectura disponibles para rol `USER`
- ❌ Endpoints públicos: `/api/auth/login`, `/actuator/health`

---

## 🛡️ Patrones de Resiliencia

### Circuit Breaker (Resilience4j)

**Configuración**:

```yaml
slidingWindowSize: 10
failureRateThreshold: 50
waitDurationInOpenState: 10s
permittedNumberOfCallsInHalfOpenState: 3
```

**Estados**:

- **CLOSED**: Operación normal, todas las peticiones pasan
- **OPEN**: Umbral alcanzado, peticiones fallan rápidamente
- **HALF_OPEN**: Prueba si el servicio se recuperó

### Retry Pattern

**Configuración**:

```yaml
maxAttempts: 3
waitDuration: 2s
retryExceptions:
  - java.io.IOException
  - java.util.concurrent.TimeoutException
```

**Estrategia**: Exponential backoff con jitter

### Rate Limiter

**Configuración**:

```yaml
limitForPeriod: 10
limitRefreshPeriod: 1m
timeoutDuration: 0
```

**Protección**: Límite de 10 peticiones por minuto por endpoint

### Time Limiter

**Configuración**:

```yaml
timeoutDuration: 5s
cancelRunningFuture: true
```

**Objetivo**: Prevenir operaciones de larga duración

---

## 🧪 Testing y Calidad

```bash
# Ejecutar tests
mvn clean test

# Tests de un módulo
cd account-service && mvn test

# Reporte de cobertura
mvn clean test jacoco:report
```

### Colección Postman

Importa `postman-collection.json` para probar todos los endpoints.

---

## 📊 Monitoreo y Observabilidad

### Spring Actuator

```bash
# Health check
curl http://localhost:8081/actuator/health

# Métricas
curl http://localhost:8081/actuator/metrics

# Info
curl http://localhost:8081/actuator/info
```

---

## 📁 Estructura del Proyecto

```
bank-microservices-cloud/
├── config-server/              # Configuración centralizada
├── eureka-server/              # Service Discovery
├── account-service/            # Microservicio de cuentas
│   ├── src/main/java/
│   │   └── com/duoc/bank/account/
│   │       ├── config/         # Configuración
│   │       ├── controller/     # REST Controllers
│   │       ├── dto/            # Data Transfer Objects
│   │       ├── model/          # Entidades JPA
│   │       ├── repository/     # Repositorios
│   │       ├── security/       # JWT Security
│   │       └── service/        # Lógica de negocio
│   └── src/main/resources/
│       ├── application.yml
│       └── data/               # Datos legacy CSV
├── docker-compose.yml
├── pom.xml
├── LICENSE
└── README.md
```

---

## 📝 Patrones Implementados

- ✅ Configuración Centralizada (Spring Cloud Config)
- ✅ Service Discovery (Netflix Eureka)
- ✅ Circuit Breaker (Resilience4j)
- ✅ Retry Pattern (Resilience4j)
- ✅ Rate Limiting (Resilience4j)
- ✅ Time Limiter (Resilience4j)
- ✅ Authentication & Authorization (Spring Security + JWT)
- ✅ API Gateway Pattern (Routing centralizado)

---

## 🚀 Despliegue en Producción

### Variables de Entorno

**Config Server**:

```bash
SPRING_PROFILES_ACTIVE=prod
CONFIG_GIT_URI=https://github.com/your-org/config-repo.git
CONFIG_GIT_USERNAME=your-username
CONFIG_GIT_PASSWORD=your-token
```

**Eureka Server**:

```bash
SPRING_PROFILES_ACTIVE=prod
EUREKA_INSTANCE_HOSTNAME=eureka-prod.yourdomain.com
EUREKA_CLIENT_REGISTER_WITH_EUREKA=false
```

**Account Service**:

```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db.yourdomain.com:5432/bankdb
SPRING_DATASOURCE_USERNAME=${DB_USERNAME}
SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
JWT_SECRET=${JWT_SECRET_KEY}
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-prod.yourdomain.com:8761/eureka/
```

### Checklist Pre-Producción

**Seguridad**:

- [ ] Cambiar credenciales por defecto (admin/admin123)
- [ ] Configurar JWT secret fuerte (mínimo 256 bits)
- [ ] Habilitar HTTPS/TLS en todos los servicios
- [ ] Configurar CORS apropiadamente
- [ ] Revisar roles y permisos de usuarios
- [ ] Implementar rate limiting agresivo

**Base de Datos**:

- [ ] Backups automáticos configurados
- [ ] Índices en columnas de búsqueda frecuente
- [ ] Pools de conexiones optimizados
- [ ] Logging de queries lentas activado

**Configuración**:

- [ ] Profiles de producción activados
- [ ] Timeouts configurados apropiadamente
- [ ] Circuit breaker thresholds ajustados
- [ ] Retry strategies validadas

**Monitoreo**:

- [ ] Actuator endpoints asegurados
- [ ] APM configurado (New Relic, Datadog, etc.)
- [ ] Logging centralizado (ELK, Splunk)
- [ ] Alertas configuradas (PagerDuty, Slack)

**Infraestructura**:

- [ ] Auto-scaling configurado
- [ ] Load balancers en lugar
- [ ] Health checks configurados
- [ ] Disaster recovery plan documentado

### Docker en Producción

**Optimización de Imágenes**:

```dockerfile
# Multi-stage build para reducir tamaño
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]
```

**Docker Compose Production**:

```yaml
services:
  account-service:
    image: registry.yourdomain.com/account-service:1.0.0
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - JAVA_OPTS=-Xmx1g -Xms512m
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: "1"
          memory: 1G
        reservations:
          cpus: "0.5"
          memory: 512M
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

### Escalado Horizontal

**Docker Compose Scale**:

```bash
# Escalar Account Service a 3 instancias
docker-compose up -d --scale account-service=3
```

**Kubernetes (futuro)**:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: account-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: account-service
  template:
    metadata:
      labels:
        app: account-service
    spec:
      containers:
        - name: account-service
          image: account-service:1.0.0
          resources:
            requests:
              memory: "512Mi"
              cpu: "500m"
            limits:
              memory: "1Gi"
              cpu: "1000m"
```

---

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

## 📧 Contacto

**Rodrigo Sanchez**

- Email: [rodrigo@sanchezdev.com](mailto:rodrigo@sanchezdev.com)
- Website: [sanchezdev.com](https://sanchezdev.com)
- GitHub: [@RodrigoSanchezDev](https://github.com/RodrigoSanchezDev)

---

## 📄 Licencia

Este proyecto está licenciado bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

---

<div align="center">

**Desarrollado por [Rodrigo Sanchez](https://sanchezdev.com)**

Copyright © 2025 Rodrigo Sanchez. Todos los derechos reservados.

</div>
