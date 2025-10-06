#!/bin/bash

# Script de inicio rápido para Bank Microservices Cloud
# Autor: DUOC UC - Desarrollo Backend III

set -e

echo "🏦 Bank Microservices Cloud - Inicio Rápido"
echo "==========================================="
echo ""

# Colores
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función para mostrar mensajes
print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

# Verificar Docker
print_step "Verificando Docker..."
if ! command -v docker &> /dev/null; then
    echo "❌ Docker no está instalado. Por favor instala Docker Desktop."
    exit 1
fi
print_success "Docker está instalado"

# Verificar Maven
print_step "Verificando Maven..."
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven no está instalado."
    print_warning "Puedes instalar Maven con: brew install maven"
    exit 1
fi
print_success "Maven está instalado"

# Compilar proyecto
print_step "Compilando proyecto..."
mvn clean package -DskipTests
print_success "Compilación exitosa"

# Levantar servicios con Docker Compose
print_step "Levantando servicios con Docker Compose..."
docker-compose up -d

echo ""
echo "⏳ Esperando a que los servicios estén listos..."
sleep 30

# Verificar servicios
echo ""
echo "📊 Estado de los Servicios:"
echo "============================"
docker-compose ps

echo ""
echo "✅ ¡Servicios levantados exitosamente!"
echo ""
echo "📍 Acceso a los servicios:"
echo "  - Eureka Dashboard:    http://localhost:8761"
echo "  - Config Server:       http://localhost:8888"
echo "  - Account Service API: http://localhost:8081"
echo "  - PostgreSQL:          localhost:5432"
echo ""
echo "🔐 Credenciales por defecto:"
echo "  - Usuario: admin"
echo "  - Password: admin123"
echo ""
echo "📝 Ver logs:"
echo "  docker-compose logs -f account-service"
echo ""
echo "🛑 Detener servicios:"
echo "  docker-compose down"
echo ""
