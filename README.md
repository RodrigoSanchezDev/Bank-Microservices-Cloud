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
<summary><strong>¿Cómo funciona el API Gateway BFF con HTTPS y JWT?</strong></summary>

**Arquitectura del Gateway**:

El **API Gateway BFF** (Backend For Frontend) centraliza todas las peticiones usando **Spring Cloud Gateway Reactive**:

- **Puerto HTTPS**: 8443 (certificado SSL auto-firmado)
- **Autenticación**: JWT centralizada mediante `GlobalFilter` (sin Spring Security)
- **Enrutamiento**: Basado en paths con Service Discovery vía Eureka
- **Resiliencia**: Circuit Breaker integrado en cada ruta

**Flujo de Autenticación**:

1. **Login** (endpoint público):

   ```bash
   curl -k -X POST https://localhost:8443/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "admin", "password": "admin123"}'
   ```

2. **Respuesta con Token**:

   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiJ9...",
     "username": "admin"
   }
   ```

3. **Uso del Token**:
   ```bash
   curl -k -X GET https://localhost:8443/api/customers \
     -H "Authorization: Bearer {token}"
   ```

**Componentes del Gateway**:

- **`JwtAuthenticationFilter`** (GlobalFilter con orden -100):

  - Valida tokens JWT en cada petición
  - Rechaza con 401 si el token es inválido
  - Agrega header `X-User-Id` con el username extraído
  - Rutas públicas: `/api/auth/login`, `/actuator/health`, `/swagger-ui`

- **`JwtTokenUtil`**:

  - Genera tokens con expiración de 24 horas
  - Valida firma y expiración
  - Extrae claims (username, roles)
  - Secret key: `YourSuperSecretKeyForJWTTokenGeneration123456789`

- **`AuthController`**:
  - Login con usuarios hardcodeados:
    - `admin` / `admin123` (roles: ADMIN, USER)
    - `user` / `user123` (rol: USER)

**Configuración de Rutas**:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: account-service-route
          uri: lb://account-service
          predicates:
            - Path=/api/accounts/**
          filters:
            - name: CircuitBreaker
              args:
                name: accountServiceCircuitBreaker
                fallbackUri: forward:/fallback

        - id: customer-service-route
          uri: lb://customer-service
          predicates:
            - Path=/api/customers/**

        - id: transaction-service-route
          uri: lb://transaction-service
          predicates:
            - Path=/api/transactions/**
```

**Certificado SSL**:

- Keystore: `classpath:keystore/bank-bff.p12`
- Password: `bankbff123`
- Alias: `bank-bff`
- Tipo: PKCS12

**Ventajas**:

- ✅ Punto único de entrada (Single Entry Point)
- ✅ Autenticación centralizada (un solo lugar para JWT)
- ✅ HTTPS/TLS para todas las comunicaciones externas
- ✅ Service Discovery automático con Eureka
- ✅ Circuit Breaker para resiliencia
- ✅ Microservicios sin lógica de autenticación (confianza en BFF)

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
- ✅ **API Gateway (BFF) con Spring Cloud Gateway**
- ✅ **HTTPS/TLS en puerto 8443 (certificado SSL)**
- ✅ **JWT Authentication centralizado en BFF (GlobalFilter)**
- ✅ Config Server (Centralizado)
- ✅ Eureka Discovery Service
- ✅ Account Service (CRUD + CSV Migration + 11 endpoints legacy)
- ✅ Customer Service (CRUD completo - 8 endpoints)
- ✅ Transaction Service (CRUD completo - 8 endpoints)
- ✅ Patrones de Resiliencia (Resilience4j)
- ✅ PostgreSQL (3 bases de datos independientes)
- ✅ Containerización Docker
- ✅ **Apache Kafka + Zookeeper + Kafka UI**
- ✅ **Event-Driven Architecture (Customer Events)**
- ✅ **Kafka Producer (Customer Service)**
- ✅ **Kafka Consumer (Transaction Service)**
- ✅ **Suite de Tests REST (test-all-endpoints.sh - 100% funcional)**
- ✅ **Suite de Tests Kafka (test-kafka.sh - 100% funcional)**
- ✅ Spring Boot 3.5.0
- ✅ Spring Cloud 2024.0.0
- ✅ Java 21

### 🚀 v1.1.0 - Mejoras de Infraestructura (Próximo)

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

- ✅ **Mensajería Asíncrona** _(Completado)_
  - ✅ Apache Kafka implementado
  - ✅ Event-driven architecture
  - ✅ Customer events (create, update, delete)
  - ⏳ SAGA Pattern para transacciones distribuidas
  - ⏳ Dead Letter Queue (DLQ)
  - ⏳ Event Sourcing completo

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

## � Evidencias del Proyecto

### 🔐 OAuth 2.0 & JWT Authentication

#### Evidencia 1: Generador de Tokens JWT (JwtUtil.java)

![Evidencia 1: JwtUtil.java](evidencias/evidencia_1_oauth_jwt_util.png)

_Implementación completa del generador y validador de tokens JWT con firma HMAC-SHA256_

**Características**:

- ✅ Generación de tokens JWT con expiración configurable (24h)
- ✅ Validación y verificación de firma
- ✅ Extracción de claims (username, roles)
- ✅ Manejo de excepciones de tokens inválidos/expirados

---

#### Evidencia 2: Filtro de Autenticación JWT (JwtAuthenticationFilter.java)

![Evidencia 2: JwtAuthenticationFilter.java](evidencias/evidencia_2_oauth_filter.png)

_Filtro global en API Gateway BFF que intercepta y valida todas las peticiones_

**Características**:

- ✅ Interceptación de peticiones HTTP en Spring Cloud Gateway
- ✅ Validación de token en header `Authorization: Bearer <token>`
- ✅ Exclusión de rutas públicas (`/auth/**`, `/actuator/**`)
- ✅ Respuesta 401 para tokens inválidos

---

### 🐳 Dockerización de Microservicios

#### Evidencia 3: Dockerfile Optimizado (Customer Service)

![Evidencia 3: Dockerfile](evidencias/evidencia_3_dockerfile.png)

_Dockerfile con imagen Alpine Linux ultra-ligera para máxima eficiencia_

**Características**:

- ✅ Imagen base: `eclipse-temurin:21-jre-alpine` (solo JRE, no JDK)
- ✅ Tamaño reducido: ~215 MB vs ~500 MB con JDK completo
- ✅ Variables de entorno configurables (`JAVA_OPTS`)
- ✅ Security: superficie de ataque mínima

---

#### Evidencia 4: Compilación Maven Exitosa

![Evidencia 4: Maven Build](evidencias/evidencia_4_maven_build.png)

_Compilación exitosa de los 7 módulos del proyecto con Maven_

**Módulos compilados**:

1. ✅ bank-microservices-cloud (parent)
2. ✅ config-server
3. ✅ eureka-server
4. ✅ api-gateway-bff
5. ✅ account-service
6. ✅ customer-service
7. ✅ transaction-service

---

#### Evidencia 5: Imágenes Docker Creadas

![Evidencia 5: Docker Images](evidencias/evidencia_5_docker_images.png)

_6 imágenes Docker optimizadas con Alpine Linux (180-653 MB cada una)_

**Imágenes generadas**:

- `bank-microservices-cloud-config-server` (565 MB)
- `bank-microservices-cloud-eureka-server` (591 MB)
- `bank-microservices-cloud-api-gateway-bff` (392 MB)
- `bank-microservices-cloud-account-service` (653 MB)
- `bank-microservices-cloud-customer-service` (480 MB)
- `bank-microservices-cloud-transaction-service` (480 MB)

---

### 🎼 Orquestación con Docker Compose

#### Evidencia 6: Configuración docker-compose.yml

![Evidencia 6: docker-compose.yml](evidencias/evidencia_6_docker_compose.png)

_Orquestación de 11 contenedores con dependencias y health checks_

**Servicios configurados**:

- **Infrastructure Layer**: postgres, zookeeper, kafka, kafka-ui
- **Spring Cloud Layer**: config-server, eureka-server
- **Microservices Layer**: api-gateway-bff, account-service, customer-service, transaction-service

---

#### Evidencia 7: Contenedores en Ejecución

![Evidencia 7: Docker Containers Running](evidencias/evidencia_7_containers_running.png)

_11 contenedores corriendo exitosamente, 10 con estado healthy_

**Estado de contenedores**:

- ✅ 10 contenedores **healthy** (con health checks configurados)
- ⚠️ 1 contenedor **running** (api-gateway-bff - reactive gateway)
- ✅ Todos los puertos mapeados correctamente
- ✅ Red `bank-network` funcionando

---

#### Evidencia 8: Eureka Service Discovery Dashboard

![Evidencia 8: Eureka Server](evidencias/evidencia_8_eureka_dashboard.png)

_Dashboard de Eureka mostrando los 5 microservicios registrados dinámicamente_

**Microservicios registrados**:

- ✅ **API-GATEWAY-BFF** (puerto 8443) - HTTPS Gateway
- ✅ **ACCOUNT-SERVICE** (puerto 8081) - Gestión de cuentas
- ✅ **CUSTOMER-SERVICE** (puerto 8082) - Gestión de clientes
- ✅ **TRANSACTION-SERVICE** (puerto 8083) - Gestión de transacciones
- ✅ **BATCH-SERVICE** (puerto 8084) - Procesamiento por lotes

---

#### Evidencia 9: Kafka UI - Event Streaming

![Evidencia 9: Kafka UI](evidencias/evidencia_9_kafka_ui.png)

_Interfaz de Kafka UI mostrando el cluster y topic de eventos de clientes_

**Configuración Kafka**:

- ✅ **Cluster**: bank-cluster
- ✅ **Topic**: customer-created-events (3 partitions)
- ✅ **Zookeeper**: coordinación del cluster (puerto 2181)
- ✅ **Kafka Broker**: puerto 9092 (interno) y 29092 (externo)
- ✅ **Kafka UI**: interfaz web en puerto 8090

---

### 🚀 Funcionalidades Extra - Apache Kafka

#### Evidencia 10: Tests de Kafka - Event-Driven Architecture

![Evidencia 10: Kafka Tests](evidencias/evidencia_10_kafka_test.png)

_Suite de pruebas automatizadas de Kafka con 8/8 tests exitosos_

**Tests ejecutados**:

1. ✅ Verificación de contenedores Kafka (Zookeeper, Kafka, Kafka UI)
2. ✅ Conectividad a Kafka UI (HTTP 200)
3. ✅ Obtención de token JWT del BFF
4. ✅ Creación de cliente de prueba (HTTP 201)
5. ✅ Publicación de evento CustomerCreated
6. ✅ Consumo de evento en Transaction Service
7. ✅ Creación de 3 clientes adicionales
8. ✅ Verificación de 4 eventos en total

**Arquitectura Event-Driven**:

- ✅ **Producer**: Customer Service publica eventos al crear clientes
- ✅ **Consumer**: Transaction Service escucha y procesa eventos
- ✅ **Event**: `CustomerCreatedEvent` con datos completos del cliente
- ✅ **Asincronía**: Desacoplamiento entre servicios

---

### 📊 Resumen de Evidencias

| Categoría          | Evidencias                                          | Estado                  |
| ------------------ | --------------------------------------------------- | ----------------------- |
| **OAuth 2.0**      | 2 evidencias (JwtUtil, JwtAuthenticationFilter)     | ✅ Completado           |
| **Dockerización**  | 3 evidencias (Dockerfile, Build, Images)            | ✅ Completado           |
| **Docker Compose** | 4 evidencias (Config, Containers, Eureka, Kafka UI) | ✅ Completado           |
| **Kafka (Extra)**  | 1 evidencia (Tests automatizados)                   | ✅ Completado           |
| **Total**          | **10 evidencias**                                   | ✅ **100% Documentado** |

---

## �📚 Recursos y Referencias

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
- [Evidencias del Proyecto](#-evidencias-del-proyecto)
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
- ✅ **API Gateway (BFF)** con Spring Cloud Gateway Reactive
- ✅ **HTTPS/TLS** con certificado SSL auto-firmado (puerto 8443)
- ✅ **Autenticación JWT Centralizada** en API Gateway (sin Spring Security)
- ✅ **Event-Driven Architecture** con Apache Kafka
- ✅ **Kafka UI** para visualización de eventos en tiempo real (puerto 8090)
- ✅ **4 Microservicios de Negocio** (Account, Customer, Transaction, Batch)
- ✅ **27 Endpoints Funcionales** (11 Account + 8 Customer + 8 Transaction)
- ✅ **Spring Batch** para procesamiento de datos legacy y jobs programados
- ✅ **Mensajería Asíncrona** (Producer/Consumer con Spring Kafka)
- ✅ **Configuración Centralizada** con Spring Cloud Config
- ✅ **Service Discovery** con Netflix Eureka
- ✅ **Patrones de Resiliencia** (Circuit Breaker, Retry, Rate Limiting)
- ✅ **Contenedorización** con Docker y Docker Compose (11 contenedores)
- ✅ **API RESTful** documentada con Swagger/OpenAPI
- ✅ **Monitoreo** con Spring Actuator
- ✅ **Bases de Datos Independientes** por microservicio
- ✅ **Suite de Tests Automatizada** (REST + Kafka - 100% funcional)

---

## 🏗️ Arquitectura

### Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────┐
│          API Gateway BFF (8443 HTTPS)                   │
│      JWT Authentication + Routing + Circuit Breaker     │
└───────────────────────┬─────────────────────────────────┘
                        │
        ┌───────────────┴───────────────────────────┐
        │                                           │
┌───────▼──────┐                          ┌────────▼────────┐
│Config Server │                          │ Eureka Server   │
│   (8888)     │                          │    (8761)       │
└──────────────┘                          └────────┬────────┘
                                                   │
                        ┌──────────────────────────┼──────────────────────┐
                        │                          │                      │
                 ┌──────▼──────┐         ┌────────▼──────┐ ┌────▼────────┐ ┌─────▼──────┐
                 │   Account   │         │   Customer    │ │Transaction  │ │   Batch    │
                 │   Service   │         │    Service    │ │   Service   │ │  Service   │
                 │   (8081)    │         │    (8082)     │ │   (8083)    │ │  (8084)    │
                 └──────┬──────┘         └───────┬───────┘ └─────┬───────┘ └────┬───────┘
                        │                        │               │              │
                        │                        │ Kafka         │ Kafka        │ Spring
                        │                        │ Producer      │ Consumer     │ Batch
                        │                        │               │              │ Jobs
                        │                 ┌──────▼───────────────▼──────────────▼──┐
                        │                 │       Apache Kafka (9092)              │
                        │                 │     customer-created-events            │
                        │                 └──────┬─────────┬────────────┬──────────┘
                        │                        │         │            │
                        │                 ┌──────▼──┐  ┌───▼────────┐   │
                        │                 │Zookeeper│  │  Kafka UI  │   │
                        │                 │ (2181)  │  │   (8090)   │   │
                        │                 └─────────┘  └────────────┘   │
                        │                                                │
                        └────────────────────────┼───────────────────────┘
                                                 │
                                          ┌──────▼──────┐
                                          │ PostgreSQL  │
                                          │   (5432)    │
                                          │  4 Databases│
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
- Rate Limiter (control de tráfico: 10 req/s)
- Time Limiter (timeout en operaciones)

### Customer Service (Puerto 8082)

**Microservicio de gestión de clientes**

**Características**:

- API RESTful para gestión completa de clientes
- Validación de RUT único
- Estados de cliente (ACTIVE, INACTIVE, SUSPENDED, BLOCKED)
- Autenticación y autorización JWT
- Integración con PostgreSQL mediante JPA
- Métricas y health checks con Actuator

**Endpoints Principales**:

- `GET /api/customers` - Listar todos los clientes
- `GET /api/customers/{id}` - Obtener cliente por ID
- `GET /api/customers/rut/{rut}` - Buscar por RUT
- `GET /api/customers/email/{email}` - Buscar por email
- `POST /api/customers` - Crear cliente
- `PUT /api/customers/{id}` - Actualizar cliente
- `DELETE /api/customers/{id}` - Eliminar cliente

**Patrones de Resiliencia**:

- Circuit Breaker (protección contra fallos en cascada)
- Retry (reintentos automáticos con backoff exponencial)
- Rate Limiter (control de tráfico: 10 req/s)
- Time Limiter (timeout de 3 segundos)

### Transaction Service (Puerto 8083)

**Microservicio de gestión de transacciones bancarias**

**Características**:

- API RESTful para procesamiento de transacciones
- Tipos: DEPOSIT, WITHDRAWAL, TRANSFER, PAYMENT, FEE
- Estados: PENDING, COMPLETED, FAILED, CANCELLED, REVERSED
- Autenticación y autorización JWT
- Integración con PostgreSQL mediante JPA
- Métricas y health checks con Actuator

**Endpoints Principales**:

- `GET /api/transactions` - Listar todas las transacciones
- `GET /api/transactions/{id}` - Obtener transacción por ID
- `GET /api/transactions/account/{accountId}` - Por cuenta
- `GET /api/transactions/customer/{customerId}` - Por cliente
- `POST /api/transactions` - Crear transacción
- `PUT /api/transactions/{id}` - Actualizar transacción
- `DELETE /api/transactions/{id}` - Eliminar transacción

**Patrones de Resiliencia**:

- Circuit Breaker (protección contra fallos en cascada)
- Retry (reintentos automáticos con backoff exponencial)
- Rate Limiter (control de tráfico: 10 req/s)
- Time Limiter (timeout de 3 segundos)

### Batch Service (Puerto 8084)

**Microservicio de procesamiento por lotes con Spring Batch**

**Características**:

- Migración de procesos COBOL legacy a Spring Batch
- Procesamiento masivo de transacciones bancarias
- Jobs configurables con steps y chunks
- Validación automática de datos
- Manejo de errores y reintentos
- Reportes de ejecución y métricas

**Procesos Batch Implementados**:

1. **Validación de Transacciones Legacy** (`validateTransactionsJob`)

   - Lee transacciones desde archivo CSV legacy
   - Valida formato y reglas de negocio
   - Genera reporte de transacciones inválidas
   - Chunk size: 100 registros

2. **Carga de Cuentas Legacy** (`loadLegacyAccountsJob`)

   - Importa cuentas desde sistema COBOL
   - Normaliza formatos de datos
   - Valida integridad referencial
   - Chunk size: 50 registros

3. **Cálculo de Intereses Mensuales** (`calculateInterestsJob`)
   - Procesa todas las cuentas activas
   - Aplica tasas según tipo de cuenta
   - Genera movimientos de interés
   - Ejecución programada: Último día del mes

**Endpoints REST**:

- `POST /batch/jobs/{jobName}` - Ejecutar job manualmente
- `GET /batch/jobs` - Listar todos los jobs
- `GET /batch/jobs/{jobName}/executions` - Historial de ejecuciones
- `GET /batch/jobs/executions/{executionId}` - Detalle de ejecución

**Características Spring Batch**:

- JobRepository para tracking de ejecuciones
- ItemReader/ItemProcessor/ItemWriter pattern
- Skip logic para errores no críticos
- Restart capability para jobs fallidos
- Listeners para logging y métricas

**Ejemplo de Ejecución**:

```bash
# Ejecutar job de validación
curl -X POST http://localhost:8084/batch/jobs/validateTransactionsJob

# Ver resultado
{
  "jobId": 1,
  "jobName": "validateTransactionsJob",
  "status": "COMPLETED",
  "startTime": "2025-10-11T10:00:00",
  "endTime": "2025-10-11T10:05:30",
  "exitCode": "COMPLETED",
  "itemsRead": 1020,
  "itemsProcessed": 1015,
  "itemsWritten": 1015,
  "skipCount": 5
}
```

**Migración Legacy**:

- ✅ Reemplaza scripts COBOL batch nocturnos
- ✅ Procesamiento paralelo con partitioning
- ✅ Monitoreo en tiempo real vs. logs offline
- ✅ Rollback automático ante fallos

### Apache Kafka (Puertos 9092/29092)

**Sistema de mensajería distribuida para Event-Driven Architecture**

**Características**:

- Event Streaming para comunicación asíncrona entre microservicios
- Arquitectura Pub/Sub de alto rendimiento
- Persistencia de eventos para auditoría
- Procesamiento de eventos en tiempo real

**Componentes Kafka**:

1. **Zookeeper** (Puerto 2181)

   - Coordinación de cluster Kafka
   - Gestión de metadatos
   - Healthcheck: `cub zk-ready`

2. **Kafka Broker** (Puertos 9092/29092)

   - Broker principal de mensajes
   - Auto-creación de topics habilitada
   - Replication factor: 1 (desarrollo)
   - Healthcheck: `cub kafka-ready`

3. **Kafka UI** (Puerto 8090)
   - Interfaz web para visualización de mensajes
   - Monitoreo de topics y consumers
   - Visualización de eventos en tiempo real
   - Acceso: `http://localhost:8090`

**Topics Implementados**:

- **`customer-created-events`** (3 particiones)
  - Eventos de creación de clientes
  - Producer: Customer Service
  - Consumer: Transaction Service
  - Formato: JSON con CustomerCreatedEvent

**Flujo de Eventos**:

```
┌──────────────────┐         ┌───────────────┐         ┌────────────────────┐
│ Customer Service │────────>│  Kafka Topic  │────────>│Transaction Service │
│   (Producer)     │ Publish │customer-created│Consume │   (Consumer)       │
└──────────────────┘         │    -events    │        └────────────────────┘
                             └───────────────┘
```

**Event Schema** (CustomerCreatedEvent):

```json
{
  "customerId": 123,
  "rut": "12345678-9",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+56912345678",
  "status": "ACTIVE",
  "createdAt": "2025-10-06T21:18:41.350236046"
}
```

**Uso**:

1. **Verificar Kafka UI**:

   ```bash
   # Abrir en navegador
   open http://localhost:8090
   ```

2. **Ver topics desde terminal**:

   ```bash
   docker exec -it bank-kafka kafka-topics \
     --bootstrap-server localhost:9092 --list
   ```

3. **Consumir mensajes**:

   ```bash
   docker exec -it bank-kafka kafka-console-consumer \
     --bootstrap-server localhost:9092 \
     --topic customer-created-events \
     --from-beginning
   ```

4. **Ver logs del consumer**:
   ```bash
   docker logs bank-transaction-service | grep "CustomerCreated"
   ```

**Beneficios**:

- ✅ **Desacoplamiento**: Servicios no dependen directamente entre sí
- ✅ **Escalabilidad**: Procesamiento asíncrono de eventos
- ✅ **Auditoría**: Todos los eventos quedan registrados
- ✅ **Resiliencia**: Eventos persistentes ante fallos
- ✅ **Event Sourcing**: Reconstrucción del estado desde eventos

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
- Base de datos: `bankdb`

**Clientes** (`customers`)

- Gestión de información de clientes
- Validación de RUT único
- Email único por cliente
- Estados: ACTIVE, INACTIVE, SUSPENDED, BLOCKED
- Base de datos: `customerdb`

**Transacciones** (`transactions`)

- Registro de movimientos financieros
- Tipos: DEPOSIT, WITHDRAWAL, TRANSFER, PAYMENT, FEE
- Estados: PENDING, COMPLETED, FAILED, CANCELLED, REVERSED
- Relación con cuentas y clientes
- Base de datos: `transactiondb`

**Intereses** (`interests`)

- Cálculo automático de intereses
- Aplicación mensual según tipo de cuenta
- Historial de aplicaciones
- Base de datos: `bankdb`

**Usuarios** (`users`)

- Autenticación y autorización
- Roles: ADMIN, USER
- Gestión de credenciales JWT
- Base de datos: compartida

### Referencia de Datos Legacy

Basado en el dataset [bank_legacy_data](https://github.com/KariVillagran/bank_legacy_data) para procesamiento de información histórica.

---

## 🛠️ Stack Tecnológico

| Tecnología      | Versión  | Propósito                     |
| --------------- | -------- | ----------------------------- |
| Java            | 21       | Lenguaje de programación      |
| Spring Boot     | 3.5.0    | Framework de aplicación       |
| Spring Cloud    | 2024.0.0 | Framework de microservicios   |
| Spring Security | 6.x      | Seguridad y autenticación     |
| Spring Kafka    | 3.3.6    | Integración con Apache Kafka  |
| Resilience4j    | 2.x      | Patrones de resiliencia       |
| Apache Kafka    | 3.9.1    | Event Streaming Platform      |
| Zookeeper       | 7.5.0    | Coordinación de Kafka cluster |
| Kafka UI        | Latest   | Interfaz web para Kafka       |
| PostgreSQL      | 15+      | Base de datos relacional      |
| Docker          | Latest   | Contenedorización             |
| Maven           | 3.8+     | Gestión de dependencias       |
| Lombok          | 1.18.34  | Reducción de boilerplate      |

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

# 5. Iniciar Customer Service (Terminal 4)
cd customer-service && mvn spring-boot:run

# 6. Iniciar Transaction Service (Terminal 5)
cd transaction-service && mvn spring-boot:run
```

### Verificación

Espera ~60 segundos para que los servicios se registren.

**Servicios Core**:

- **Config Server**: http://localhost:8888/actuator/health
- **Eureka Dashboard**: http://localhost:8761
- **API Gateway BFF**: https://localhost:8443/actuator/health

**Microservicios**:

- **Account Service**: http://localhost:8081/actuator/health
- **Customer Service**: http://localhost:8082/actuator/health
- **Transaction Service**: http://localhost:8083/actuator/health

**Mensajería y UI**:

- **Kafka UI**: http://localhost:8090 (Visualización de eventos)
- **Zookeeper**: localhost:2181 (Coordinación Kafka)
- **Kafka Broker**: localhost:9092 (Interno), localhost:29092 (Externo)

**Base de Datos**:

- **PostgreSQL**: localhost:5432 (bankdb, customerdb, transactiondb)

**Swagger UI**:

- http://localhost:8081/swagger-ui.html (Account)
- http://localhost:8082/swagger-ui.html (Customer)
- http://localhost:8083/swagger-ui.html (Transaction)

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

> ⚠️ **IMPORTANTE**: Todos los endpoints ahora están expuestos a través del **API Gateway BFF** en puerto **8443 (HTTPS)**

```bash
# Login - Obtener JWT (usuario admin)
curl -k -X POST https://localhost:8443/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# Login - Obtener JWT (usuario regular)
curl -k -X POST https://localhost:8443/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "user123"
  }'
```

**Respuesta**:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin"
}
```

### Operaciones de Cuentas (requiere JWT)

```bash
# Listar cuentas
curl -k -X GET https://localhost:8443/api/accounts \
  -H "Authorization: Bearer {token}"

# Crear cuenta
curl -k -X POST https://localhost:8443/api/accounts \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "1234567890",
    "accountHolder": "Juan Pérez",
    "balance": 1000.00,
    "accountType": "SAVINGS"
  }'

# Obtener cuenta por ID
curl -k -X GET https://localhost:8443/api/accounts/{id} \
  -H "Authorization: Bearer {token}"

# Account Legacy Endpoints (11 endpoints)
curl -k -X GET https://localhost:8443/api/accounts/legacy/transacciones \
  -H "Authorization: Bearer {token}"

curl -k -X GET https://localhost:8443/api/accounts/legacy/intereses \
  -H "Authorization: Bearer {token}"

curl -k -X GET https://localhost:8443/api/accounts/legacy/cuentas-anuales \
  -H "Authorization: Bearer {token}"

curl -k -X GET https://localhost:8443/api/accounts/legacy/resumen-general \
  -H "Authorization: Bearer {token}"
```

### Customer Service API

**Operaciones de Clientes (8 endpoints - requiere JWT)**

```bash
# Listar todos los clientes
curl -k -X GET https://localhost:8443/api/customers \
  -H "Authorization: Bearer {token}"

# Crear cliente
curl -k -X POST https://localhost:8443/api/customers \
  -H "Authorization: Bearer {token}" \
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

# Buscar por RUT
curl -k -X GET https://localhost:8443/api/customers/rut/12345678-9 \
  -H "Authorization: Bearer {token}"

# Buscar por email
curl -k -X GET https://localhost:8443/api/customers/email/juan.perez@example.com \
  -H "Authorization: Bearer {token}"

# Actualizar cliente
curl -k -X PUT https://localhost:8443/api/customers/{id} \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Juan Carlos",
    "lastName": "Pérez González",
    "status": "ACTIVE"
  }'

# Eliminar cliente
curl -k -X DELETE https://localhost:8443/api/customers/{id} \
  -H "Authorization: Bearer {token}"

# Health check
curl -k -X GET https://localhost:8443/api/customers/health \
  -H "Authorization: Bearer {token}"
```

### Transaction Service API

**Operaciones de Transacciones (8 endpoints - requiere JWT)**

```bash
# Listar todas las transacciones
curl -k -X GET https://localhost:8443/api/transactions \
  -H "Authorization: Bearer {token}"

# Crear transacción
curl -k -X POST https://localhost:8443/api/transactions \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": 1,
    "customerId": 1,
    "type": "DEPOSIT",
    "amount": 50000.00,
    "description": "Depósito inicial",
    "status": "PENDING"
  }'

# Buscar transacciones por cuenta
curl -k -X GET https://localhost:8443/api/transactions/account/{accountId} \
  -H "Authorization: Bearer {token}"

# Buscar transacciones por cliente
curl -k -X GET https://localhost:8443/api/transactions/customer/{customerId} \
  -H "Authorization: Bearer {token}"

# Actualizar transacción
curl -k -X PUT https://localhost:8443/api/transactions/{id} \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "COMPLETED"
  }'

# Eliminar transacción
curl -k -X DELETE https://localhost:8443/api/transactions/{id} \
  -H "Authorization: Bearer {token}"

# Health check
curl -k -X GET https://localhost:8443/api/transactions/health \
  -H "Authorization: Bearer {token}"
```

---

## 🧪 Suite de Tests Automatizada

El proyecto incluye **dos scripts completos de tests**:

### 1. Test de Endpoints REST (test-all-endpoints.sh)

Valida **todos los 27 endpoints** a través del API Gateway BFF con HTTPS y JWT.

```bash
# Ejecutar suite completa de tests
./test-all-endpoints.sh

# Ver logs en tiempo real
./test-all-endpoints.sh 2>&1 | tee test-results.log
```

### 2. Test de Mensajería Kafka (test-kafka.sh)

Valida **Event-Driven Architecture** con Apache Kafka.

```bash
# Ejecutar tests de Kafka
./test-kafka.sh

# Resultado esperado:
# ✅ 4 clientes creados
# ✅ 4 eventos publicados a Kafka
# ✅ 4 eventos consumidos correctamente
# ✅ Kafka UI disponible en http://localhost:8090
```

**Qué valida**:

- ✅ Contenedores Kafka, Zookeeper y Kafka UI están healthy
- ✅ Topic `customer-created-events` creado con 3 particiones
- ✅ Producer en Customer Service publica eventos
- ✅ Consumer en Transaction Service procesa eventos
- ✅ Kafka UI muestra mensajes en tiempo real

**Verificación manual**:

```bash
# Ver mensajes en Kafka UI
open http://localhost:8090

# Ver eventos desde terminal
docker exec -it bank-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic customer-created-events \
  --from-beginning

# Ver logs del consumer
docker logs bank-transaction-service | grep "CustomerCreated"
```

### Resumen de Endpoints

**Account Service (11 endpoints legacy)**:

- ✅ GET `/api/accounts/legacy/transacciones` - Lista transacciones válidas/inválidas
- ✅ GET `/api/accounts/legacy/transacciones-validas` - Solo transacciones válidas
- ✅ GET `/api/accounts/legacy/transacciones-invalidas` - Solo transacciones inválidas
- ✅ GET `/api/accounts/legacy/intereses` - Cálculos de intereses
- ✅ GET `/api/accounts/legacy/intereses-validos` - Intereses válidos
- ✅ GET `/api/accounts/legacy/intereses-invalidos` - Intereses inválidos
- ✅ GET `/api/accounts/legacy/cuentas-anuales` - Cuentas anuales
- ✅ GET `/api/accounts/legacy/cuentas-validas` - Cuentas válidas
- ✅ GET `/api/accounts/legacy/cuentas-invalidas` - Cuentas inválidas
- ✅ GET `/api/accounts/legacy/resumen-general` - Resumen completo
- ✅ GET `/api/accounts/legacy/health` - Health check

**Customer Service (8 endpoints CRUD)**:

- ✅ GET `/api/customers` - Listar todos los clientes
- ✅ GET `/api/customers/{id}` - Obtener cliente por ID
- ✅ GET `/api/customers/rut/{rut}` - Buscar por RUT
- ✅ GET `/api/customers/email/{email}` - Buscar por email
- ✅ POST `/api/customers` - Crear cliente
- ✅ PUT `/api/customers/{id}` - Actualizar cliente
- ✅ DELETE `/api/customers/{id}` - Eliminar cliente
- ✅ GET `/api/customers/health` - Health check

**Transaction Service (8 endpoints CRUD)**:

- ✅ GET `/api/transactions` - Listar todas las transacciones
- ✅ GET `/api/transactions/{id}` - Obtener transacción por ID
- ✅ GET `/api/transactions/account/{accountId}` - Transacciones por cuenta
- ✅ GET `/api/transactions/customer/{customerId}` - Transacciones por cliente
- ✅ POST `/api/transactions` - Crear transacción
- ✅ PUT `/api/transactions/{id}` - Actualizar transacción
- ✅ DELETE `/api/transactions/{id}` - Eliminar transacción
- ✅ GET `/api/transactions/health` - Health check

### Resultado Esperado

```
╔═══════════════════════════════════════════════════════╗
║                  RESUMEN DE TESTS                     ║
╚═══════════════════════════════════════════════════════╝

Total de tests ejecutados: 17
Tests exitosos: 17
Tests fallidos: 0

✓ ¡Todos los tests pasaron exitosamente!
✓ BFF HTTPS funcionando correctamente
✓ JWT authentication funcionando
✓ Routing a microservicios funcionando

ℹ ENDPOINTS TOTALES DISPONIBLES:
  - Account Service (Legacy): 11 endpoints
  - Customer Service (CRUD): 8 endpoints
  - Transaction Service (CRUD): 8 endpoints
  Total: 27 endpoints expuestos a través del BFF
PUT http://localhost:8083/api/transactions/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "status": "COMPLETED",
  "description": "Transacción completada exitosamente"
}
```

### Health & Monitoreo

```bash
# Health check - Account Service
GET http://localhost:8081/actuator/health

# Health check - Customer Service
GET http://localhost:8082/actuator/health

# Health check - Transaction Service
GET http://localhost:8083/actuator/health

# Circuit Breaker estado
GET http://localhost:8081/actuator/health/circuitbreakers
GET http://localhost:8082/actuator/health/circuitbreakers
GET http://localhost:8083/actuator/health/circuitbreakers

# Métricas
GET http://localhost:8081/actuator/metrics
GET http://localhost:8082/actuator/metrics
GET http://localhost:8083/actuator/metrics
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

### Suite de Tests Automatizados

```bash
# Ejecutar tests unitarios
mvn clean test

# Tests de un módulo específico
cd account-service && mvn test

# Reporte de cobertura
mvn clean test jacoco:report
```

### Script de Evaluación Completa (Alternativa)

Para una **verificación integral del sistema** con interfaz interactiva:

```bash
# Dar permisos de ejecución
chmod +x test-evaluacion-final.sh

# Ejecutar menú interactivo
./test-evaluacion-final.sh
```

**Incluye 22 tests organizados en 6 categorías**:

- ✅ Migración de Procesos Batch (4 tests)
- ✅ Patrón BFF Multi-Canal (5 tests)
- ✅ Microservicios Resilientes (3 tests)
- ✅ Seguridad Distribuida JWT/HTTPS (3 tests)
- ✅ Mensajería Asíncrona Kafka (3 tests)
- ✅ Containerización Docker (4 tests)

Ver [GUIA-RAPIDA-USO.md](GUIA-RAPIDA-USO.md) para más detalles.

### Colección Postman

Importa `postman-collection.json` para probar todos los endpoints.

---

## 📊 Monitoreo y Observabilidad

### Spring Actuator

**Account Service**:

```bash
# Health check
curl http://localhost:8081/actuator/health

# Métricas
curl http://localhost:8081/actuator/metrics

# Info
curl http://localhost:8081/actuator/info

# Circuit Breaker
curl http://localhost:8081/actuator/health/circuitbreakers
```

**Customer Service**:

```bash
# Health check
curl http://localhost:8082/actuator/health

# Métricas
curl http://localhost:8082/actuator/metrics

# Circuit Breaker
curl http://localhost:8082/actuator/health/circuitbreakers
```

**Transaction Service**:

```bash
# Health check
curl http://localhost:8083/actuator/health

# Métricas
curl http://localhost:8083/actuator/metrics

# Circuit Breaker
curl http://localhost:8083/actuator/health/circuitbreakers
```

### Eureka Dashboard

```bash
# Ver todos los servicios registrados
curl http://localhost:8761/eureka/apps

# Ver servicio específico
curl http://localhost:8761/eureka/apps/ACCOUNT-SERVICE
curl http://localhost:8761/eureka/apps/CUSTOMER-SERVICE
curl http://localhost:8761/eureka/apps/TRANSACTION-SERVICE
```

---

## 📁 Estructura del Proyecto

```
bank-microservices-cloud/
├── config-server/              # Configuración centralizada
│   └── src/main/resources/
│       └── config-repo/        # Configuraciones de servicios
│           ├── account-service.yml
│           ├── customer-service.yml
│           └── transaction-service.yml
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
├── customer-service/           # Microservicio de clientes
│   ├── src/main/java/
│   │   └── com/duoc/bank/customer/
│   │       ├── controller/     # REST Controllers
│   │       ├── model/          # Entidad Customer
│   │       ├── repository/     # CustomerRepository
│   │       ├── security/       # JWT Security
│   │       └── service/        # CustomerService
│   └── src/main/resources/
│       └── application.yml
├── transaction-service/        # Microservicio de transacciones
│   ├── src/main/java/
│   │   └── com/duoc/bank/transaction/
│   │       ├── controller/     # REST Controllers
│   │       ├── model/          # Entidad Transaction
│   │       ├── repository/     # TransactionRepository
│   │       ├── security/       # JWT Security
│   │       └── service/        # TransactionService
│   └── src/main/resources/
│       └── application.yml
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

**Customer Service**:

```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db.yourdomain.com:5432/customerdb
SPRING_DATASOURCE_USERNAME=${DB_USERNAME}
SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
JWT_SECRET=${JWT_SECRET_KEY}
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-prod.yourdomain.com:8761/eureka/
```

**Transaction Service**:

```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db.yourdomain.com:5432/transactiondb
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

# Escalar Customer Service a 2 instancias
docker-compose up -d --scale customer-service=2

# Escalar Transaction Service a 3 instancias
docker-compose up -d --scale transaction-service=3

# Escalar todos los servicios
docker-compose up -d --scale account-service=3 --scale customer-service=2 --scale transaction-service=3
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
