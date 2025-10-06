#!/bin/bash

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuración BFF HTTPS
BFF_URL="https://localhost:8443"
USERNAME="admin"
PASSWORD="admin123"

# Contadores de tests
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

# Función para hacer GET request
make_get_request() {
    local endpoint=$1
    local token=$2
    local description=$3
    
    ((TOTAL_TESTS++))
    print_info "Testing: $description"
    
    local response=$(curl -s -k -w "\n%{http_code}" -X GET "${BFF_URL}${endpoint}" \
        -H "Authorization: Bearer ${token}" \
        -H "Content-Type: application/json")
    
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ]; then
        print_success "$description - HTTP $http_code"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
    else
        print_error "$description - HTTP $http_code"
        echo "Response: $body"
    fi
    echo ""
}

# Función para hacer POST request
make_post_request() {
    local endpoint=$1
    local token=$2
    local data=$3
    local description=$4
    
    ((TOTAL_TESTS++))
    print_info "Testing: $description"
    
    local response=$(curl -s -k -w "\n%{http_code}" -X POST "${BFF_URL}${endpoint}" \
        -H "Authorization: Bearer ${token}" \
        -H "Content-Type: application/json" \
        -d "$data")
    
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')
    
    if [[ "$http_code" =~ ^(200|201)$ ]]; then
        print_success "$description - HTTP $http_code"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
    else
        print_error "$description - HTTP $http_code"
        echo "Response: $body"
    fi
    echo ""
}

echo ""
echo "╔═══════════════════════════════════════════════════════╗"
echo "║   TEST COMPLETO DE ENDPOINTS - BFF HTTPS GATEWAY      ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""

# Obtener token
TOKEN=$(get_token)

# ============================================
# ACCOUNT SERVICE - LEGACY DATA ENDPOINTS
# ============================================
print_section "ACCOUNT SERVICE - LEGACY DATA (11 endpoints)"

make_get_request "/api/accounts/legacy/transacciones" "$TOKEN" "Todas las transacciones"
make_get_request "/api/accounts/legacy/transacciones/semana/1" "$TOKEN" "Transacciones semana 1"
make_get_request "/api/accounts/legacy/transacciones/validas" "$TOKEN" "Transacciones válidas"
make_get_request "/api/accounts/legacy/transacciones/invalidas" "$TOKEN" "Transacciones inválidas"
make_get_request "/api/accounts/legacy/intereses" "$TOKEN" "Todos los intereses"
make_get_request "/api/accounts/legacy/intereses/semana/2" "$TOKEN" "Intereses semana 2"
make_get_request "/api/accounts/legacy/intereses/validas" "$TOKEN" "Intereses válidos"
make_get_request "/api/accounts/legacy/cuentas-anuales" "$TOKEN" "Todas las cuentas anuales"
make_get_request "/api/accounts/legacy/cuentas-anuales/semana/3" "$TOKEN" "Cuentas anuales semana 3"
make_get_request "/api/accounts/legacy/cuentas-anuales/validas" "$TOKEN" "Cuentas anuales válidas"
make_get_request "/api/accounts/legacy/resumen" "$TOKEN" "Resumen general"

# ============================================
# CUSTOMER SERVICE - CRUD ENDPOINTS
# ============================================
print_section "CUSTOMER SERVICE - CRUD (8 endpoints)"

make_get_request "/api/customers" "$TOKEN" "Listar todos los clientes"
make_get_request "/api/customers/health" "$TOKEN" "Health check de customers"

# Crear un cliente de prueba
CUSTOMER_DATA='{
  "rut": "12345678-9",
  "firstName": "Test",
  "lastName": "Customer",
  "email": "test@bff.com",
  "phone": "+56912345678",
  "address": "Calle Test 123",
  "status": "ACTIVE"
}'
make_post_request "/api/customers" "$TOKEN" "$CUSTOMER_DATA" "Crear cliente de prueba"

# Nota: Los siguientes tests requieren IDs reales, se muestran como ejemplos
print_info "Nota: Los siguientes endpoints requieren IDs existentes en la BD"
echo "  - GET /api/customers/{id}"
echo "  - GET /api/customers/rut/{rut}"
echo "  - GET /api/customers/email/{email}"
echo "  - PUT /api/customers/{id}"
echo "  - DELETE /api/customers/{id}"
echo ""

# ============================================
# TRANSACTION SERVICE - CRUD ENDPOINTS
# ============================================
print_section "TRANSACTION SERVICE - CRUD (8 endpoints)"

make_get_request "/api/transactions" "$TOKEN" "Listar todas las transacciones"
make_get_request "/api/transactions/health" "$TOKEN" "Health check de transactions"

# Crear una transacción de prueba
TRANSACTION_DATA='{
  "accountId": 1,
  "customerId": 1,
  "type": "DEPOSIT",
  "amount": 50000.00,
  "description": "Test transaction via BFF",
  "status": "PENDING"
}'
make_post_request "/api/transactions" "$TOKEN" "$TRANSACTION_DATA" "Crear transacción de prueba"

# Nota: Los siguientes tests requieren IDs reales
print_info "Nota: Los siguientes endpoints requieren IDs existentes en la BD"
echo "  - GET /api/transactions/{id}"
echo "  - GET /api/transactions/account/{accountId}"
echo "  - GET /api/transactions/customer/{customerId}"
echo "  - PUT /api/transactions/{id}"
echo "  - DELETE /api/transactions/{id}"
echo ""

# ============================================
# RESUMEN FINAL
# ============================================
echo ""
echo "╔═══════════════════════════════════════════════════════╗"
echo "║                  RESUMEN DE TESTS                     ║"
echo "╚═══════════════════════════════════════════════════════╝"
echo ""
echo -e "${BLUE}Total de tests ejecutados:${NC} $TOTAL_TESTS"
echo -e "${GREEN}Tests exitosos:${NC} $PASSED_TESTS"
echo -e "${RED}Tests fallidos:${NC} $FAILED_TESTS"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    print_success "¡Todos los tests pasaron exitosamente!"
    echo -e "${GREEN}✓ BFF HTTPS funcionando correctamente${NC}"
    echo -e "${GREEN}✓ JWT authentication funcionando${NC}"
    echo -e "${GREEN}✓ Routing a microservicios funcionando${NC}"
else
    print_error "Algunos tests fallaron. Revisa los errores arriba."
fi

echo ""
print_info "ENDPOINTS TOTALES DISPONIBLES:"
echo "  - Account Service (Legacy): 11 endpoints"
echo "  - Customer Service (CRUD): 8 endpoints"
echo "  - Transaction Service (CRUD): 8 endpoints"
echo "  Total: 27 endpoints expuestos a través del BFF"
echo ""
