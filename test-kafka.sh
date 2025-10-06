#!/bin/bash

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# Configuración BFF HTTPS
BFF_URL="https://localhost:8443"
USERNAME="admin"
PASSWORD="admin123"

# Contadores
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Función para imprimir con color
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
    ((PASSED_TESTS++))
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
    ((FAILED_TESTS++))
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

print_section() {
    echo -e "\n${BLUE}═══════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════${NC}\n"
}

print_kafka_info() {
    echo -e "${CYAN}🔔 $1${NC}"
}

print_event() {
    echo -e "${MAGENTA}📨 $1${NC}"
}

# Función para obtener el token JWT del BFF
get_token() {
    print_info "Obteniendo token JWT del BFF..." >&2
    
    local token=$(curl -k -s -X POST "${BFF_URL}/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\"}" | jq -r '.token')
    
    if [ -z "$token" ] || [ "$token" = "null" ]; then
        print_error "No se pudo obtener el token JWT" >&2
        exit 1
    fi
    
    print_success "Token JWT obtenido del BFF" >&2
    echo "$token"
}

# Función para crear cliente y verificar evento Kafka
test_customer_creation_with_kafka() {
    local token=$1
    
    ((TOTAL_TESTS++))
    print_section "TEST: Crear Cliente y Verificar Evento Kafka"
    
    # Generar datos únicos
    local timestamp=$(date +%s)
    local rut="$((12000000 + RANDOM % 9999999))-$((RANDOM % 10))"
    local email="kafka.test.${timestamp}@example.com"
    
    print_info "Creando cliente de prueba con:"
    echo "  - RUT: $rut"
    echo "  - Email: $email"
    echo ""
    
    # Crear cliente
    local customer_data="{
        \"rut\": \"${rut}\",
        \"firstName\": \"Kafka\",
        \"lastName\": \"TestUser\",
        \"email\": \"${email}\",
        \"phone\": \"+56912345${RANDOM:0:3}\",
        \"address\": \"Calle Test 123\",
        \"status\": \"ACTIVE\"
    }"
    
    local response=$(curl -s -k -w "\n%{http_code}" -X POST "${BFF_URL}/api/customers" \
        -H "Authorization: Bearer ${token}" \
        -H "Content-Type: application/json" \
        -d "$customer_data")
    
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')
    
    if [[ "$http_code" =~ ^(200|201)$ ]]; then
        print_success "Cliente creado exitosamente - HTTP $http_code"
        echo "$body" | jq '.'
        
        local customer_id=$(echo "$body" | jq -r '.id')
        print_event "✅ Evento CustomerCreated debería estar en Kafka"
        print_kafka_info "📊 Verifica Kafka UI en: http://localhost:8090"
        print_kafka_info "   Topic: customer-created-events"
        print_kafka_info "   Customer ID: $customer_id"
        
        print_info "\n⏳ Esperando 3 segundos para que el consumer procese el evento..."
        sleep 3
        
        print_info "📋 Verifica los logs del transaction-service:"
        echo "   docker logs bank-transaction-service --tail 50"
        echo "   Deberías ver el mensaje: '📥 Evento CustomerCreated recibido'"
        
    else
        print_error "Error al crear cliente - HTTP $http_code"
        echo "Response: $body"
    fi
    echo ""
}

# Función para verificar Kafka UI
check_kafka_ui() {
    ((TOTAL_TESTS++))
    print_section "Verificar Kafka UI"
    
    print_info "Intentando conectar a Kafka UI..."
    
    local kafka_ui_status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8090)
    
    if [ "$kafka_ui_status" = "200" ]; then
        print_success "Kafka UI está disponible - HTTP $kafka_ui_status"
        print_kafka_info "🌐 Accede a: http://localhost:8090"
        print_kafka_info "📊 Cluster: bank-cluster"
        print_kafka_info "📋 Topics: customer-created-events"
    else
        print_error "Kafka UI no está disponible - HTTP $kafka_ui_status"
        print_info "Verifica que el contenedor esté corriendo: docker ps | grep kafka-ui"
    fi
    echo ""
}

# Función para verificar containers Kafka
check_kafka_containers() {
    ((TOTAL_TESTS++))
    print_section "Verificar Contenedores Kafka"
    
    print_info "Estado de los contenedores Kafka:"
    echo ""
    
    local zookeeper_status=$(docker ps --filter "name=bank-zookeeper" --format "{{.Status}}")
    local kafka_status=$(docker ps --filter "name=bank-kafka" --format "{{.Status}}" | grep -v "kafka-ui")
    local kafka_ui_status=$(docker ps --filter "name=bank-kafka-ui" --format "{{.Status}}")
    
    if [ -n "$zookeeper_status" ]; then
        print_success "Zookeeper: $zookeeper_status"
    else
        print_error "Zookeeper: No está corriendo"
    fi
    
    if [ -n "$kafka_status" ]; then
        print_success "Kafka: $kafka_status"
    else
        print_error "Kafka: No está corriendo"
    fi
    
    if [ -n "$kafka_ui_status" ]; then
        print_success "Kafka UI: $kafka_ui_status"
    else
        print_error "Kafka UI: No está corriendo"
    fi
    echo ""
}

# Función para mostrar información de topics
show_kafka_topics_info() {
    print_section "Información de Topics Kafka"
    
    print_info "Para ver los topics en Kafka UI:"
    echo "  1. Abre http://localhost:8090 en tu navegador"
    echo "  2. Selecciona 'bank-cluster'"
    echo "  3. Ve a la sección 'Topics'"
    echo "  4. Busca 'customer-created-events'"
    echo "  5. Haz click en el topic para ver los mensajes"
    echo ""
    
    print_kafka_info "También puedes ver los topics desde la terminal:"
    echo "  docker exec -it bank-kafka kafka-topics --bootstrap-server localhost:9092 --list"
    echo ""
}

echo ""
echo "╔═══════════════════════════════════════════════════════╗"
echo "║      TEST DE KAFKA - CUSTOMER EVENTS MESSAGING        ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""

# Verificar contenedores Kafka
check_kafka_containers

# Verificar Kafka UI
check_kafka_ui

# Mostrar info de topics
show_kafka_topics_info

# Obtener token
TOKEN=$(get_token)

# Test de creación de cliente con Kafka
test_customer_creation_with_kafka "$TOKEN"

# Crear más clientes para tener más eventos
print_section "Creando Clientes Adicionales para Eventos Kafka"

for i in {1..3}; do
    print_info "Creando cliente #$i..."
    
    timestamp=$(date +%s)$i
    rut="$((15000000 + RANDOM % 9999999))-$((RANDOM % 10))"
    email="test.kafka.${timestamp}@example.com"
    
    customer_data="{
        \"rut\": \"${rut}\",
        \"firstName\": \"Customer\",
        \"lastName\": \"Kafka${i}\",
        \"email\": \"${email}\",
        \"phone\": \"+56987654${RANDOM:0:3}\",
        \"address\": \"Avenida Kafka ${i}\",
        \"status\": \"ACTIVE\"
    }"
    
    response=$(curl -s -k -X POST "${BFF_URL}/api/customers" \
        -H "Authorization: Bearer ${TOKEN}" \
        -H "Content-Type: application/json" \
        -d "$customer_data")
    
    customer_id=$(echo "$response" | jq -r '.id')
    
    if [ "$customer_id" != "null" ] && [ -n "$customer_id" ]; then
        print_success "Cliente #$i creado - ID: $customer_id"
    else
        print_error "Error al crear cliente #$i"
    fi
    
    sleep 1
done

echo ""

# Resumen final
print_section "RESUMEN DE TESTS KAFKA"

echo -e "${BLUE}Total de tests ejecutados:${NC} $TOTAL_TESTS"
echo -e "${GREEN}Tests exitosos:${NC} $PASSED_TESTS"
echo -e "${RED}Tests fallidos:${NC} $FAILED_TESTS"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    print_success "¡Todos los tests pasaron exitosamente!"
else
    print_error "Algunos tests fallaron. Revisa los errores arriba."
fi

echo ""
print_section "VERIFICACIÓN FINAL"

print_kafka_info "🌐 Kafka UI: http://localhost:8090"
print_info "📊 Para ver los mensajes Kafka:"
echo "   1. Abre http://localhost:8090"
echo "   2. Ve a Topics > customer-created-events"
echo "   3. Haz click en 'Messages'"
echo "   4. Verás todos los eventos CustomerCreated"
echo ""

print_info "📋 Para ver los logs del consumer:"
echo "   docker logs bank-transaction-service --tail 100 | grep 'CustomerCreated'"
echo ""

print_info "🔍 Para ver estadísticas del topic:"
echo "   docker exec -it bank-kafka kafka-run-class kafka.tools.GetOffsetShell \\"
echo "     --broker-list localhost:9092 --topic customer-created-events"
echo ""

print_event "✅ Deberías haber visto 4 eventos CustomerCreated en total"
echo ""
