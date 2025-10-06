# 📸 Evidencias del Informe Semana 8

Esta carpeta contiene las **10 capturas de pantalla esenciales** que demuestran la implementación de los requisitos de la Semana 8.

## 📋 Checklist de Capturas (10 evidencias)

### 🔐 Requisito 1: OAuth 2.0 (2 evidencias)

- [ ] `evidencia_1_oauth_jwt_util.png` - Código completo de JwtUtil.java (generación y validación de tokens)
- [ ] `evidencia_2_oauth_filter.png` - Código del JwtAuthenticationFilter.java (validación en gateway)

### 🐳 Requisito 2: Dockerización (3 evidencias)

- [ ] `evidencia_3_dockerfile.png` - Dockerfile de Customer Service
- [ ] `evidencia_4_maven_build.png` - Compilación Maven exitosa (BUILD SUCCESS de los 7 módulos)
- [ ] `evidencia_5_docker_images.png` - Imágenes Docker creadas (6 microservicios)

### 🎼 Requisito 3: Docker Compose (4 evidencias)

- [ ] `evidencia_6_docker_compose.png` - Archivo docker-compose.yml (servicios principales)
- [ ] `evidencia_7_containers_running.png` - Estado de los 10 contenedores (docker-compose ps)
- [ ] `evidencia_8_eureka_dashboard.png` - Dashboard Eureka mostrando microservicios registrados
- [ ] `evidencia_9_kafka_ui.png` - Kafka UI mostrando cluster y topic

### 🚀 Funcionalidades Extra (1 evidencia)

- [ ] `evidencia_10_kafka_test.png` - Ejecución exitosa de test-kafka.sh (8/8 tests passed)

---

## ✅ Instrucciones Simplificadas

1. **Prepara el entorno** (solo una vez):

   ```bash
   mvn clean package -DskipTests
   docker-compose up -d
   ```

2. **Toma las 10 capturas** siguiendo las instrucciones del informe

3. **Guarda cada imagen** con el nombre exacto indicado

4. **Ubica todas las capturas** en esta carpeta: `evidencias/`

**Total:** 10 capturas de pantalla ✨
