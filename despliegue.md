# Guía de Despliegue en la Nube - Bank Microservices Cloud

> Instrucciones detalladas para desplegar el sistema de microservicios bancarios en AWS EC2

---

## 📋 Tabla de Contenidos

1. [Requisitos Previos](#requisitos-previos)
2. [Preparación de la Instancia EC2](#preparación-de-la-instancia-ec2)
3. [Configuración del Entorno](#configuración-del-entorno)
4. [Transferencia del Proyecto](#transferencia-del-proyecto)
5. [Compilación en EC2](#compilación-en-ec2)
6. [Configuración de Security Groups](#configuración-de-security-groups)
7. [Despliegue con Docker Compose](#despliegue-con-docker-compose)
8. [Verificación del Despliegue](#verificación-del-despliegue)
9. [Pruebas del Sistema](#pruebas-del-sistema)
10. [Monitoreo y Gestión](#monitoreo-y-gestión)
11. [Troubleshooting](#troubleshooting)
12. [Optimización y Buenas Prácticas](#optimización-y-buenas-prácticas)

---

## Requisitos Previos

### 1. Cuenta de AWS

- Cuenta de AWS activa
- Permisos para crear instancias EC2
- Permisos para configurar Security Groups
- Acceso a AWS Console

### 2. Instancia EC2 Recomendada

| Especificación    | Valor Recomendado          |
| ----------------- | -------------------------- |
| Tipo de Instancia | t2.large o superior        |
| Sistema Operativo | Amazon Linux 2023          |
| CPU               | 2 vCPUs mínimo             |
| RAM               | 8 GB mínimo                |
| Almacenamiento    | 20 GB SSD (gp3)            |
| Región            | us-east-1 o la más cercana |

### 3. Herramientas Locales Necesarias

- **SSH Client**: Para conectarse a EC2
- **SCP/SFTP**: Para transferir archivos
- **AWS CLI** (opcional): Para automatización
- **Llave SSH**: Archivo .pem para autenticación

### 4. Verificar Conexión SSH

```bash
# Verificar que tienes la llave .pem
ls -l backend3.pem

# Configurar permisos correctos
chmod 400 backend3.pem

# Probar conexión
ssh -i backend3.pem ec2-user@<TU-IP-PUBLICA>
```

---

## Preparación de la Instancia EC2

### Paso 1: Crear Instancia EC2

#### Opción A: Usando AWS Console (Recomendado)

1. Ir a **AWS Console** → **EC2** → **Launch Instance**

2. **Configurar la instancia**:

   - **Name**: bank-microservices-prod
   - **AMI**: Amazon Linux 2023 (64-bit x86)
   - **Instance type**: t2.large
   - **Key pair**: Seleccionar o crear nueva (backend3.pem)
   - **Network settings**:
     - VPC: Default
     - Subnet: us-east-1a (o similar)
     - Auto-assign public IP: Enable
   - **Storage**: 20 GB gp3

3. **Launch instance**

4. **Obtener IP pública**:
   - Ir a **Instances**
   - Seleccionar tu instancia
   - Copiar **Public IPv4 address**

#### Opción B: Usando AWS CLI

```bash
# Crear instancia EC2
aws ec2 run-instances \
  --image-id ami-0c02fb55731490c00 \
  --instance-type t2.large \
  --key-name backend3 \
  --security-group-ids sg-xxxxxxxx \
  --subnet-id subnet-xxxxxxxx \
  --block-device-mappings '[{"DeviceName":"/dev/xvda","Ebs":{"VolumeSize":20,"VolumeType":"gp3"}}]' \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=bank-microservices-prod}]'
```

### Paso 2: Conectarse a la Instancia

```bash
# Conectarse vía SSH
ssh -i backend3.pem ec2-user@<TU-IP-PUBLICA>

# Verificar conectividad
whoami
hostname
uname -a
```

### Paso 3: Actualizar el Sistema

```bash
# Actualizar todos los paquetes
sudo dnf update -y

# Instalar utilidades básicas
sudo dnf install -y git vim htop wget curl unzip

# Verificar espacio en disco
df -h

# Verificar memoria
free -h
```

---

## Configuración del Entorno

### Paso 1: Instalar Docker

```bash
# Instalar Docker
sudo dnf install -y docker

# Habilitar Docker al inicio del sistema
sudo systemctl enable docker

# Iniciar Docker
sudo systemctl start docker

# Verificar que Docker está corriendo
sudo systemctl status docker

# Agregar usuario ec2-user al grupo docker
sudo usermod -aG docker ec2-user

# Verificar instalación
docker --version
```

**Salida esperada**:

```
Docker version 24.0.x, build xxxxxxx
```

### Paso 2: Instalar Docker Compose

```bash
# Descargar Docker Compose v2.31.0
sudo curl -L "https://github.com/docker/compose/releases/download/v2.31.0/docker-compose-linux-x86_64" \
  -o /usr/local/bin/docker-compose

# Dar permisos de ejecución
sudo chmod +x /usr/local/bin/docker-compose

# Crear symlink (opcional)
sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose

# Verificar instalación
docker-compose --version
```

**Salida esperada**:

```
Docker Compose version v2.31.0
```

### Paso 3: Instalar Java 21

```bash
# Instalar Amazon Corretto 21 (JDK)
sudo dnf install -y java-21-amazon-corretto java-21-amazon-corretto-devel

# Verificar instalación
java -version

# Configurar JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

# Verificar JAVA_HOME
echo $JAVA_HOME
```

**Salida esperada**:

```
openjdk version "21.0.x" 2024-xx-xx LTS
OpenJDK Runtime Environment Corretto-21.0.x.x
```

### Paso 4: Instalar Maven

```bash
# Instalar Maven
sudo dnf install -y maven

# Verificar instalación
mvn -version

# Configurar Maven settings (opcional)
mkdir -p ~/.m2
```

**Salida esperada**:

```
Apache Maven 3.8.x
Maven home: /usr/share/maven
Java version: 21.0.x
```

### Paso 5: Crear Estructura de Directorios

```bash
# Crear directorios para logs y datos
mkdir -p ~/logs
mkdir -p ~/data/postgres
mkdir -p ~/data/kafka
mkdir -p ~/backups

# Verificar estructura
tree -L 1 ~
```

### Paso 6: Configurar Swap (Opcional pero Recomendado)

```bash
# Crear archivo swap de 4GB
sudo dd if=/dev/zero of=/swapfile bs=1M count=4096

# Configurar permisos
sudo chmod 600 /swapfile

# Crear swap
sudo mkswap /swapfile

# Activar swap
sudo swapon /swapfile

# Hacer permanente (agregar a /etc/fstab)
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Verificar swap activo
free -h
swapon --show
```

### Paso 7: Desconectar y Reconectar SSH

```bash
# Salir de la sesión
exit

# Reconectar (para aplicar grupo docker)
ssh -i backend3.pem ec2-user@<TU-IP-PUBLICA>

# Verificar que docker funciona sin sudo
docker ps
```

---

## Transferencia del Proyecto

### Método 1: Transferencia desde Máquina Local (Recomendado)

#### Paso 1: Preparar el Proyecto Localmente

```bash
# En tu máquina local (macOS)
cd ~/ruta/a/tu/proyecto

# Crear archivo tar comprimido (excluyendo archivos innecesarios)
tar -czf bank-project.tar.gz \
  --exclude='bank-microservices-cloud/target' \
  --exclude='bank-microservices-cloud/*/target' \
  --exclude='bank-microservices-cloud/.git' \
  --exclude='bank-microservices-cloud/*.tar.gz' \
  --exclude='bank-microservices-cloud/evidencias' \
  --exclude='bank-microservices-cloud/*.pem' \
  --exclude='bank-microservices-cloud/*.ppk' \
  --exclude='bank-microservices-cloud/.idea' \
  --exclude='bank-microservices-cloud/.vscode' \
  bank-microservices-cloud/

# Verificar tamaño del archivo
ls -lh bank-project.tar.gz
```

#### Paso 2: Transferir a EC2

```bash
# Transferir con SCP
scp -i backend3.pem bank-project.tar.gz ec2-user@<TU-IP-PUBLICA>:~/

# Verificar transferencia
ssh -i backend3.pem ec2-user@<TU-IP-PUBLICA> "ls -lh ~/"
```

#### Paso 3: Desempaquetar en EC2

```bash
# Conectarse a EC2
ssh -i backend3.pem ec2-user@<TU-IP-PUBLICA>

# Descomprimir
cd ~
tar -xzf bank-project.tar.gz

# Verificar estructura
ls -la bank-microservices-cloud/

# Limpiar archivo tar
rm bank-project.tar.gz
```

### Método 2: Clonar desde GitHub

```bash
# Conectarse a EC2
ssh -i backend3.pem ec2-user@<TU-IP-PUBLICA>

# Clonar repositorio
cd ~
git clone https://github.com/RodrigoSanchezDev/bank-microservices-cloud.git

# Navegar al proyecto
cd bank-microservices-cloud

# Verificar rama
git branch
git status
```

---

## Compilación en EC2

### Paso 1: Configurar Variables de Entorno

```bash
# Navegar al proyecto
cd ~/bank-microservices-cloud

# Configurar Java 21
export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
export PATH=$JAVA_HOME/bin:$PATH

# Verificar versiones
java -version
mvn -version
```

### Paso 2: Compilar el Proyecto

```bash
# Compilar todos los módulos
mvn clean package -DskipTests

# Esto tomará 3-5 minutos
# Observar el progreso en la terminal
```

**Salida esperada al final**:

```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  03:45 min
[INFO] Finished at: 2025-XX-XXTXX:XX:XX-XX:00
[INFO] ------------------------------------------------------------------------
```

### Paso 3: Verificar JARs Generados

```bash
# Listar todos los JARs generados
find . -name "*.jar" -not -name "*original*" -type f

# O con formato legible
ls -lh */target/*.jar | grep -v '.original'
```

**Archivos esperados** (7 JARs):

- ✅ account-service/target/account-service-1.0.0.jar (~87 MB)
- ✅ api-gateway-bff/target/api-gateway-bff-1.0.0.jar (~55 MB)
- ✅ batch-service/target/batch-service-1.0.0.jar (~76 MB)
- ✅ config-server/target/config-server-1.0.0.jar (~43 MB)
- ✅ customer-service/target/customer-service-1.0.0.jar (~99 MB)
- ✅ eureka-server/target/eureka-server-1.0.0.jar (~56 MB)
- ✅ transaction-service/target/transaction-service-1.0.0.jar (~99 MB)

### Paso 4: Verificar Dockerfiles

```bash
# Verificar que todos los Dockerfiles existen
ls -l */Dockerfile

# Verificar contenido de un Dockerfile
cat customer-service/Dockerfile
```

---

## Configuración de Security Groups

### ⚠️ PASO CRÍTICO

Sin configurar correctamente los Security Groups, **NO podrás acceder** a los servicios desde internet.

### Opción A: AWS Console (Recomendado)

#### Paso 1: Localizar el Security Group

1. Ir a **AWS Console** → **EC2** → **Instances**
2. Seleccionar tu instancia **bank-microservices-prod**
3. Tab **"Security"**
4. Click en el **Security Group** (sg-xxxxxxxxx)

#### Paso 2: Editar Reglas de Entrada

1. Click **"Edit inbound rules"**
2. Click **"Add rule"** para cada puerto

#### Paso 3: Agregar Reglas

| Tipo       | Protocolo | Puerto | Origen    | Descripción             |
| ---------- | --------- | ------ | --------- | ----------------------- |
| SSH        | TCP       | 22     | Mi IP     | Acceso SSH seguro       |
| Custom TCP | TCP       | 8443   | 0.0.0.0/0 | API Gateway BFF (HTTPS) |
| Custom TCP | TCP       | 8081   | 0.0.0.0/0 | Account Service         |
| Custom TCP | TCP       | 8082   | 0.0.0.0/0 | Customer Service        |
| Custom TCP | TCP       | 8083   | 0.0.0.0/0 | Transaction Service     |
| Custom TCP | TCP       | 8084   | 0.0.0.0/0 | Batch Service           |
| Custom TCP | TCP       | 8761   | 0.0.0.0/0 | Eureka Server           |
| Custom TCP | TCP       | 8888   | 0.0.0.0/0 | Config Server           |
| Custom TCP | TCP       | 8090   | 0.0.0.0/0 | Kafka UI                |
| Custom TCP | TCP       | 5432   | 0.0.0.0/0 | PostgreSQL              |
| Custom TCP | TCP       | 9092   | 0.0.0.0/0 | Kafka                   |
| Custom TCP | TCP       | 2181   | 0.0.0.0/0 | Zookeeper               |

#### Paso 4: Guardar Reglas

1. Click **"Save rules"**
2. Verificar que las reglas se aplicaron correctamente

### Opción B: AWS CLI

```bash
# En tu máquina local con AWS CLI configurado
# Obtener el Security Group ID
INSTANCE_ID="i-xxxxxxxxxxxxxxxxx"  # Tu Instance ID
SG_ID=$(aws ec2 describe-instances \
  --instance-ids $INSTANCE_ID \
  --query 'Reservations[0].Instances[0].SecurityGroups[0].GroupId' \
  --output text)

echo "Security Group ID: $SG_ID"

# Agregar reglas de entrada
aws ec2 authorize-security-group-ingress \
  --group-id $SG_ID \
  --ip-permissions \
    IpProtocol=tcp,FromPort=8443,ToPort=8443,IpRanges='[{CidrIp=0.0.0.0/0,Description="API Gateway BFF"}]' \
    IpProtocol=tcp,FromPort=8081,ToPort=8084,IpRanges='[{CidrIp=0.0.0.0/0,Description="Microservices"}]' \
    IpProtocol=tcp,FromPort=8761,ToPort=8761,IpRanges='[{CidrIp=0.0.0.0/0,Description="Eureka"}]' \
    IpProtocol=tcp,FromPort=8888,ToPort=8888,IpRanges='[{CidrIp=0.0.0.0/0,Description="Config Server"}]' \
    IpProtocol=tcp,FromPort=8090,ToPort=8090,IpRanges='[{CidrIp=0.0.0.0/0,Description="Kafka UI"}]' \
    IpProtocol=tcp,FromPort=5432,ToPort=5432,IpRanges='[{CidrIp=0.0.0.0/0,Description="PostgreSQL"}]' \
    IpProtocol=tcp,FromPort=9092,ToPort=9092,IpRanges='[{CidrIp=0.0.0.0/0,Description="Kafka"}]' \
    IpProtocol=tcp,FromPort=2181,ToPort=2181,IpRanges='[{CidrIp=0.0.0.0/0,Description="Zookeeper"}]'
```

### Verificación de Security Groups

```bash
# Verificar reglas desde tu máquina local
aws ec2 describe-security-groups --group-ids $SG_ID

# O verificar en AWS Console
```

---

## Despliegue con Docker Compose

### Paso 1: Verificar docker-compose.yml

```bash
# Conectarse a EC2
ssh -i backend3.pem ec2-user@<TU-IP-PUBLICA>

# Navegar al proyecto
cd ~/bank-microservices-cloud

# Verificar archivo docker-compose.yml
cat docker-compose.yml

# Verificar sintaxis
docker-compose config
```

### Paso 2: Construir Imágenes Docker

```bash
# Construir todas las imágenes
docker-compose build

# Esto tomará 10-15 minutos la primera vez
# Ver progreso en terminal
```

**Imágenes esperadas** (6 imágenes):

- ✅ bank-microservices-cloud-config-server
- ✅ bank-microservices-cloud-eureka-server
- ✅ bank-microservices-cloud-api-gateway-bff
- ✅ bank-microservices-cloud-account-service
- ✅ bank-microservices-cloud-customer-service
- ✅ bank-microservices-cloud-transaction-service
- ✅ bank-microservices-cloud-batch-service

### Paso 3: Iniciar Todos los Servicios

```bash
# Iniciar todos los contenedores en modo detached
docker-compose up -d

# Ver estado de contenedores
docker-compose ps

# Ver logs en tiempo real
docker-compose logs -f
```

### Paso 4: Monitorear el Inicio

**Tiempos de inicio aproximados**:

1. **PostgreSQL** → 15-30 segundos
2. **Zookeeper** → 20-30 segundos
3. **Kafka** → 40-60 segundos
4. **Config Server** → 30-45 segundos
5. **Eureka Server** → 45-60 segundos
6. **Kafka UI** → 20-30 segundos
7. **Microservicios** → 60-90 segundos cada uno
8. **API Gateway BFF** → 60-90 segundos (último en iniciar)

**Tiempo total estimado**: 5-7 minutos

```bash
# Monitorear estado cada 30 segundos
watch -n 30 'docker-compose ps'

# O verificar manualmente
docker-compose ps

# Ver logs de un servicio específico
docker-compose logs -f customer-service
```

**Estado esperado final**:

```
NAME                           STATUS
bank-account-service           Up (healthy)
bank-api-gateway-bff           Up
bank-batch-service             Up (healthy)
bank-config-server             Up (healthy)
bank-customer-service          Up (healthy)
bank-eureka-server             Up (healthy)
bank-kafka                     Up (healthy)
bank-kafka-ui                  Up (healthy)
bank-postgres                  Up (healthy)
bank-transaction-service       Up (healthy)
bank-zookeeper                 Up (healthy)
```

---

## Verificación del Despliegue

### 1. Verificaciones Locales (dentro de EC2)

```bash
# Verificar que todos los contenedores están corriendo
docker-compose ps

# Verificar Config Server
curl -s http://localhost:8888/actuator/health | jq

# Verificar Eureka Server
curl -s http://localhost:8761/eureka/apps | grep '<app>'

# Verificar API Gateway BFF
curl -k -s https://localhost:8443/actuator/health | jq

# Verificar Kafka UI
curl -s http://localhost:8090 | grep -q "Kafka" && echo "Kafka UI OK"

# Contar servicios registrados en Eureka (debe ser 5)
curl -s http://localhost:8761/eureka/apps | grep '<app>' | wc -l
```

### 2. Verificaciones desde tu Navegador

**URLs Públicas** (reemplaza `<TU-IP-PUBLICA>` con tu IP de EC2):

#### Dashboards Web

```bash
# Eureka Server Dashboard
http://<TU-IP-PUBLICA>:8761

# Kafka UI Dashboard
http://<TU-IP-PUBLICA>:8090

# Config Server Health
http://<TU-IP-PUBLICA>:8888/actuator/health

# API Gateway BFF Health (HTTPS con advertencia de certificado)
https://<TU-IP-PUBLICA>:8443/actuator/health
```

#### Swagger UI de Microservicios

```bash
# Account Service
http://<TU-IP-PUBLICA>:8081/swagger-ui.html

# Customer Service
http://<TU-IP-PUBLICA>:8082/swagger-ui.html

# Transaction Service
http://<TU-IP-PUBLICA>:8083/swagger-ui.html

# Batch Service
http://<TU-IP-PUBLICA>:8084/swagger-ui.html
```

### 3. Verificación de Eureka Dashboard

1. Abrir `http://<TU-IP-PUBLICA>:8761` en navegador
2. Verificar que aparecen **5 servicios registrados**:
   - ✅ API-GATEWAY-BFF
   - ✅ ACCOUNT-SERVICE
   - ✅ CUSTOMER-SERVICE
   - ✅ TRANSACTION-SERVICE
   - ✅ BATCH-SERVICE

### 4. Verificación de Kafka UI

1. Abrir `http://<TU-IP-PUBLICA>:8090`
2. Verificar cluster **bank-cluster**
3. Verificar topic **customer-created-events**

---

## Pruebas del Sistema

### Ejecutar Suite de Tests Automatizada

```bash
# Conectarse a EC2
ssh -i backend3.pem ec2-user@<TU-IP-PUBLICA>

# Navegar al proyecto
cd ~/bank-microservices-cloud

# Dar permisos de ejecución al script
chmod +x test-evaluacion-final.sh

# Ejecutar TODAS las pruebas automáticamente
echo '8' | ./test-evaluacion-final.sh

# O ejecutar de forma interactiva
./test-evaluacion-final.sh
```

**El script valida**:

1. ✅ Migración de Procesos Batch (Spring Batch)
2. ✅ Patrón Backend for Frontend (BFF)
3. ✅ Microservicios Resilientes (Circuit Breaker, Retry)
4. ✅ Seguridad Distribuida (OAuth2/JWT)
5. ✅ Mensajería Asíncrona (Kafka)
6. ✅ Containerización (Docker)

### Pruebas Manuales desde tu Máquina Local

#### 1. Obtener Token JWT

```bash
# Definir la IP pública de EC2
EC2_IP="<TU-IP-PUBLICA>"

# Login y obtener token
curl -k -X POST https://$EC2_IP:8443/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Guardar token en variable
TOKEN=$(curl -k -s -X POST https://$EC2_IP:8443/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.token')

echo $TOKEN
```

#### 2. Probar Customer Service

```bash
# Crear un cliente
curl -k -X POST https://$EC2_IP:8443/api/customers \
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

# Listar clientes
curl -k -X GET https://$EC2_IP:8443/api/customers \
  -H "Authorization: Bearer $TOKEN"

# Buscar por RUT
curl -k -X GET https://$EC2_IP:8443/api/customers/rut/12345678-9 \
  -H "Authorization: Bearer $TOKEN"
```

#### 3. Probar Transaction Service

```bash
# Crear una transacción
curl -k -X POST https://$EC2_IP:8443/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": 1,
    "customerId": 1,
    "type": "DEPOSIT",
    "amount": 50000.00,
    "description": "Depósito inicial desde cloud",
    "status": "PENDING"
  }'

# Listar transacciones
curl -k -X GET https://$EC2_IP:8443/api/transactions \
  -H "Authorization: Bearer $TOKEN"
```

#### 4. Probar Account Service (Legacy)

```bash
# Obtener resumen general
curl -k -X GET https://$EC2_IP:8443/api/accounts/legacy/resumen-general \
  -H "Authorization: Bearer $TOKEN"

# Obtener transacciones
curl -k -X GET https://$EC2_IP:8443/api/accounts/legacy/transacciones \
  -H "Authorization: Bearer $TOKEN"
```

---

## Monitoreo y Gestión

### Comandos Útiles de Docker

```bash
# Ver todos los contenedores
docker-compose ps

# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f customer-service

# Ver últimas 100 líneas
docker-compose logs --tail=100 customer-service

# Reiniciar un servicio
docker-compose restart customer-service

# Reiniciar todos los servicios
docker-compose restart

# Ver uso de recursos
docker stats

# Ver espacio en disco usado por Docker
docker system df
```

### Monitoreo de Sistema

```bash
# Ver uso de CPU y memoria
htop

# Ver espacio en disco
df -h

# Ver uso de memoria
free -h

# Ver procesos
ps aux | grep java

# Ver puertos en uso
sudo netstat -tlnp

# Ver logs del sistema
sudo journalctl -u docker -f
```

### Health Checks

```bash
# Script de health check automático
cat > ~/health-check.sh << 'EOF'
#!/bin/bash
echo "=== Health Check ==="
echo "Config Server: $(curl -s http://localhost:8888/actuator/health | jq -r '.status')"
echo "Eureka Server: $(curl -s http://localhost:8761/actuator/health | jq -r '.status')"
echo "Account Service: $(curl -s http://localhost:8081/actuator/health | jq -r '.status')"
echo "Customer Service: $(curl -s http://localhost:8082/actuator/health | jq -r '.status')"
echo "Transaction Service: $(curl -s http://localhost:8083/actuator/health | jq -r '.status')"
echo "Batch Service: $(curl -s http://localhost:8084/actuator/health | jq -r '.status')"
echo "API Gateway BFF: $(curl -k -s https://localhost:8443/actuator/health | jq -r '.status')"
EOF

chmod +x ~/health-check.sh

# Ejecutar health check
~/health-check.sh
```

### Backup de Base de Datos

```bash
# Crear backup de PostgreSQL
docker exec bank-postgres pg_dumpall -U postgres > ~/backups/backup-$(date +%Y%m%d-%H%M%S).sql

# Listar backups
ls -lh ~/backups/

# Restaurar backup (si es necesario)
# cat ~/backups/backup-XXXXXXXX-XXXXXX.sql | docker exec -i bank-postgres psql -U postgres
```

---

## Troubleshooting

### Problema: Contenedor no inicia (unhealthy)

```bash
# Ver logs detallados
docker-compose logs -f <nombre-servicio>

# Inspeccionar contenedor
docker inspect <nombre-contenedor>

# Reiniciar el contenedor
docker-compose restart <nombre-servicio>

# Reiniciar todo el stack
docker-compose down
docker-compose up -d
```

### Problema: Puerto ya en uso

```bash
# Ver qué proceso usa el puerto
sudo netstat -tlnp | grep :8081

# Matar el proceso
sudo kill -9 <PID>

# O detener Docker Compose
docker-compose down
```

### Problema: No hay memoria suficiente

```bash
# Ver memoria disponible
free -h

# Si no hay swap, crear uno (ver sección anterior)
# Si ya existe swap, aumentar tamaño

# Limpiar Docker
docker system prune -f
docker volume prune -f
```

### Problema: Disco lleno

```bash
# Ver uso de disco
df -h

# Limpiar logs de Docker
sudo journalctl --vacuum-time=3d

# Limpiar imágenes y contenedores no usados
docker system prune -af

# Limpiar volúmenes
docker volume prune -f
```

### Problema: Servicios no se registran en Eureka

```bash
# Esperar 2-3 minutos más (los servicios tardan en registrarse)

# Verificar logs de Eureka
docker-compose logs -f eureka-server

# Verificar logs del microservicio
docker-compose logs -f customer-service

# Verificar conectividad de red
docker network inspect bank-microservices-cloud_bank-network

# Reiniciar Eureka
docker-compose restart eureka-server

# Reiniciar microservicio
docker-compose restart customer-service
```

### Problema: Kafka no funciona

```bash
# Verificar contenedores de Kafka
docker-compose ps zookeeper kafka kafka-ui

# Reiniciar stack de Kafka (en orden)
docker-compose restart zookeeper
sleep 10
docker-compose restart kafka
sleep 10
docker-compose restart kafka-ui

# Ver logs de Kafka
docker-compose logs -f kafka

# Verificar topics
docker exec bank-kafka kafka-topics \
  --bootstrap-server localhost:9092 --list
```

### Problema: No puedo acceder desde internet

```bash
# 1. Verificar Security Groups en AWS Console
# 2. Verificar que el servicio está corriendo
docker-compose ps

# 3. Verificar que el puerto está escuchando
sudo netstat -tlnp | grep :8443

# 4. Probar desde dentro de EC2
curl -k https://localhost:8443/actuator/health

# 5. Verificar firewall local (si existe)
sudo iptables -L -n
```

### Problema: Certificado SSL no confiable

```bash
# Esto es NORMAL con certificados autofirmados

# En curl: usar opción -k
curl -k https://<IP>:8443/...

# En navegador:
# Click en "Avanzado" → "Continuar de todos modos"

# En Postman:
# Settings → SSL Certificate Verification: OFF
```

---

## Optimización y Buenas Prácticas

### 1. Optimización de Recursos

```bash
# Ajustar límites de memoria en docker-compose.yml
# Ejemplo para customer-service:
#   deploy:
#     resources:
#       limits:
#         cpus: '1'
#         memory: 1G
#       reservations:
#         cpus: '0.5'
#         memory: 512M
```

### 2. Configurar Auto-Restart

```yaml
# En docker-compose.yml, agregar a cada servicio:
restart: unless-stopped
```

### 3. Logging Centralizado

```bash
# Configurar rotación de logs
sudo vi /etc/docker/daemon.json

# Agregar:
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}

# Reiniciar Docker
sudo systemctl restart docker
```

### 4. Monitoreo con CloudWatch (Opcional)

```bash
# Instalar CloudWatch Agent
wget https://s3.amazonaws.com/amazoncloudwatch-agent/amazon_linux/amd64/latest/amazon-cloudwatch-agent.rpm
sudo rpm -U ./amazon-cloudwatch-agent.rpm

# Configurar agente
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-config-wizard
```

### 5. Backup Automático

```bash
# Crear script de backup automático
cat > ~/backup-script.sh << 'EOF'
#!/bin/bash
BACKUP_DIR=~/backups
DATE=$(date +%Y%m%d-%H%M%S)
docker exec bank-postgres pg_dumpall -U postgres > $BACKUP_DIR/backup-$DATE.sql
find $BACKUP_DIR -name "backup-*.sql" -mtime +7 -delete
EOF

chmod +x ~/backup-script.sh

# Agregar a crontab (backup diario a las 2 AM)
(crontab -l 2>/dev/null; echo "0 2 * * * ~/backup-script.sh") | crontab -
```

### 6. Actualización del Sistema

```bash
# Script de actualización
cat > ~/update-system.sh << 'EOF'
#!/bin/bash
echo "Updating system packages..."
sudo dnf update -y

echo "Cleaning Docker..."
docker system prune -f

echo "System updated successfully!"
EOF

chmod +x ~/update-system.sh

# Ejecutar mensualmente
(crontab -l 2>/dev/null; echo "0 3 1 * * ~/update-system.sh") | crontab -
```

### 7. Seguridad

```bash
# Cambiar contraseñas por defecto
# Editar archivos de configuración y usar variables de entorno

# Configurar firewall UFW (opcional)
sudo dnf install -y ufw
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh
sudo ufw allow 8443/tcp
# ... agregar otros puertos necesarios
sudo ufw enable
```

---

## Resumen del Proceso de Despliegue

### Checklist Completo

| Paso      | Tarea                                    | Tiempo Estimado | Estado |
| --------- | ---------------------------------------- | --------------- | ------ |
| 1         | Crear y configurar instancia EC2         | 5-10 min        | ⬜     |
| 2         | Conectarse vía SSH                       | 1 min           | ⬜     |
| 3         | Actualizar sistema e instalar utilidades | 5 min           | ⬜     |
| 4         | Instalar Docker y Docker Compose         | 5 min           | ⬜     |
| 5         | Instalar Java 21 y Maven                 | 3 min           | ⬜     |
| 6         | Crear estructura de directorios          | 1 min           | ⬜     |
| 7         | Configurar swap                          | 2 min           | ⬜     |
| 8         | Transferir proyecto a EC2                | 3-5 min         | ⬜     |
| 9         | Compilar proyecto con Maven              | 3-5 min         | ⬜     |
| 10        | Configurar Security Groups en AWS        | 5 min           | ⬜     |
| 11        | Iniciar servicios con Docker Compose     | 7-10 min        | ⬜     |
| 12        | Verificar despliegue                     | 5 min           | ⬜     |
| 13        | Ejecutar pruebas del sistema             | 10 min          | ⬜     |
| **TOTAL** | **De cero a producción**                 | **55-75 min**   | ⬜     |

### URLs Finales de Acceso

Después del despliegue exitoso, tendrás acceso a:

```
Eureka Dashboard:     http://<IP-PUBLICA>:8761
Kafka UI:             http://<IP-PUBLICA>:8090
API Gateway BFF:      https://<IP-PUBLICA>:8443
Account Service:      http://<IP-PUBLICA>:8081
Customer Service:     http://<IP-PUBLICA>:8082
Transaction Service:  http://<IP-PUBLICA>:8083
Batch Service:        http://<IP-PUBLICA>:8084
Config Server:        http://<IP-PUBLICA>:8888
```

---

## Conclusión

Este documento proporciona una guía completa para desplegar el sistema de microservicios bancarios en AWS EC2.

**Puntos clave**:

- ✅ Configuración paso a paso
- ✅ Scripts de automatización
- ✅ Verificaciones y pruebas
- ✅ Troubleshooting común
- ✅ Optimización y mejores prácticas

Para instrucciones de ejecución local, consultar el archivo **`instrucciones.md`**.

**Desarrollado por Rodrigo Sanchez**  
Copyright © 2025 - Todos los derechos reservados
