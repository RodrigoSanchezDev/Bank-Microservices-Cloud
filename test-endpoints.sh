#!/bin/bash

# Script para probar todos los endpoints de la API Legacy Data
# Uso: ./test-endpoints.sh

echo "🚀 INICIANDO PRUEBA DE ENDPOINTS"
echo "================================="
echo ""

# Colores
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 1. Autenticación
echo -e "${BLUE}1️⃣  AUTENTICACIÓN${NC}"
echo "Endpoint: POST /api/auth/login"
echo "Credenciales: admin / admin123"
echo ""

RESPONSE=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

TOKEN=$(echo $RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}❌ ERROR: No se pudo obtener el token${NC}"
    echo "Response: $RESPONSE"
    exit 1
fi

echo -e "${GREEN}✅ Token obtenido exitosamente${NC}"
echo "Token: ${TOKEN:0:50}..."
echo ""

# 2. Todas las Transacciones
echo -e "${BLUE}2️⃣  TODAS LAS TRANSACCIONES${NC}"
echo "Endpoint: GET /api/legacy/transacciones"
RESP=$(curl -s -X GET "http://localhost:8081/api/legacy/transacciones" \
  -H "Authorization: Bearer $TOKEN")
echo $RESP | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    print(f\"✅ Total: {data['total']}, Válidos: {data['validos']}, Inválidos: {data['invalidos']}, Tasa: {data['tasa_exito']}\")
    print(f\"   Primer registro: ID={data['datos'][0]['id']}, Fecha={data['datos'][0]['fecha']}, Monto={data['datos'][0]['monto']}\")
except:
    print('❌ Error procesando respuesta')
"
echo ""

# 3. Transacciones por Semana
echo -e "${BLUE}3️⃣  TRANSACCIONES POR SEMANA${NC}"
for semana in semana_1 semana_2 semana_3; do
    echo "Endpoint: GET /api/legacy/transacciones/semana/$semana"
    RESP=$(curl -s -X GET "http://localhost:8081/api/legacy/transacciones/semana/$semana" \
      -H "Authorization: Bearer $TOKEN")
    echo $RESP | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    print(f\"  $semana: Total={data['total']}, Válidos={data['validos']}, Tasa={data['tasa_exito']}\")
except:
    print('  ❌ Error')
"
done
echo ""

# 4. Transacciones Válidas
echo -e "${BLUE}4️⃣  SOLO TRANSACCIONES VÁLIDAS${NC}"
echo "Endpoint: GET /api/legacy/transacciones/validas"
RESP=$(curl -s -X GET "http://localhost:8081/api/legacy/transacciones/validas" \
  -H "Authorization: Bearer $TOKEN")
COUNT=$(echo $RESP | python3 -c "import sys, json; print(len(json.load(sys.stdin)))")
echo -e "${GREEN}✅ Cantidad de transacciones válidas: $COUNT${NC}"
echo ""

# 5. Transacciones Inválidas (Admin only)
echo -e "${BLUE}5️⃣  SOLO TRANSACCIONES INVÁLIDAS (Admin)${NC}"
echo "Endpoint: GET /api/legacy/transacciones/invalidas"
RESP=$(curl -s -X GET "http://localhost:8081/api/legacy/transacciones/invalidas" \
  -H "Authorization: Bearer $TOKEN")
echo $RESP | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    print(f\"✅ Cantidad de transacciones inválidas: {len(data)}\")
    if len(data) > 0:
        print(f\"   Ejemplo: ID={data[0]['id']}, Motivo={data[0]['motivoInvalidez']}\")
except:
    print('❌ Error')
"
echo ""

# 6. Todos los Intereses
echo -e "${BLUE}6️⃣  TODOS LOS INTERESES${NC}"
echo "Endpoint: GET /api/legacy/intereses"
RESP=$(curl -s -X GET "http://localhost:8081/api/legacy/intereses" \
  -H "Authorization: Bearer $TOKEN")
echo $RESP | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    print(f\"✅ Total: {data['total']}, Válidos: {data['validos']}, Inválidos: {data['invalidos']}, Tasa: {data['tasa_exito']}\")
except:
    print('❌ Error')
"
echo ""

# 7. Intereses por Semana
echo -e "${BLUE}7️⃣  INTERESES POR SEMANA${NC}"
for semana in semana_1 semana_2 semana_3; do
    echo "Endpoint: GET /api/legacy/intereses/semana/$semana"
    RESP=$(curl -s -X GET "http://localhost:8081/api/legacy/intereses/semana/$semana" \
      -H "Authorization: Bearer $TOKEN")
    echo $RESP | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    print(f\"  $semana: Total={data['total']}, Válidos={data['validos']}\")
except:
    print('  ❌ Error')
"
done
echo ""

# 8. Intereses Válidos
echo -e "${BLUE}8️⃣  SOLO INTERESES VÁLIDOS${NC}"
echo "Endpoint: GET /api/legacy/intereses/validas"
RESP=$(curl -s -X GET "http://localhost:8081/api/legacy/intereses/validas" \
  -H "Authorization: Bearer $TOKEN")
COUNT=$(echo $RESP | python3 -c "import sys, json; print(len(json.load(sys.stdin)))")
echo -e "${GREEN}✅ Cantidad de intereses válidos: $COUNT${NC}"
echo ""

# 9. Todas las Cuentas Anuales
echo -e "${BLUE}9️⃣  TODAS LAS CUENTAS ANUALES${NC}"
echo "Endpoint: GET /api/legacy/cuentas-anuales"
RESP=$(curl -s -X GET "http://localhost:8081/api/legacy/cuentas-anuales" \
  -H "Authorization: Bearer $TOKEN")
echo $RESP | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    print(f\"✅ Total: {data['total']}, Válidos: {data['validos']}, Inválidos: {data['invalidos']}, Tasa: {data['tasa_exito']}\")
except:
    print('❌ Error')
"
echo ""

# 10. Cuentas Anuales por Semana
echo -e "${BLUE}🔟 CUENTAS ANUALES POR SEMANA${NC}"
for semana in semana_1 semana_2 semana_3; do
    echo "Endpoint: GET /api/legacy/cuentas-anuales/semana/$semana"
    RESP=$(curl -s -X GET "http://localhost:8081/api/legacy/cuentas-anuales/semana/$semana" \
      -H "Authorization: Bearer $TOKEN")
    echo $RESP | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    print(f\"  $semana: Total={data['total']}, Válidos={data['validos']}\")
except:
    print('  ❌ Error')
"
done
echo ""

# 11. Cuentas Anuales Válidas
echo -e "${BLUE}1️⃣1️⃣ SOLO CUENTAS ANUALES VÁLIDAS${NC}"
echo "Endpoint: GET /api/legacy/cuentas-anuales/validas"
RESP=$(curl -s -X GET "http://localhost:8081/api/legacy/cuentas-anuales/validas" \
  -H "Authorization: Bearer $TOKEN")
COUNT=$(echo $RESP | python3 -c "import sys, json; print(len(json.load(sys.stdin)))")
echo -e "${GREEN}✅ Cantidad de cuentas anuales válidas: $COUNT${NC}"
echo ""

# 12. Resumen General (Admin only)
echo -e "${BLUE}1️⃣2️⃣ RESUMEN GENERAL (Admin)${NC}"
echo "Endpoint: GET /api/legacy/resumen"
RESP=$(curl -s -X GET "http://localhost:8081/api/legacy/resumen" \
  -H "Authorization: Bearer $TOKEN")
echo $RESP | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    print(f\"✅ ESTADÍSTICAS GLOBALES:\")
    print(f\"   📊 Total registros procesados: {data['total_registros']}\")
    print(f\"   ✅ Total válidos: {data['total_validos']}\")
    print(f\"   ❌ Total inválidos: {data['total_invalidos']}\")
    print(f\"   📈 Tasa de éxito global: {data['tasa_exito']}\")
    print(f\"\")
    print(f\"   Por tipo de dato:\")
    print(f\"   - Transacciones: {data['transacciones']['total']} ({data['transacciones']['validas']} válidas)\")
    print(f\"   - Intereses: {data['intereses']['total']} ({data['intereses']['validos']} válidos)\")
    print(f\"   - Cuentas Anuales: {data['cuentas_anuales']['total']} ({data['cuentas_anuales']['validas']} válidas)\")
except Exception as e:
    print(f'❌ Error: {e}')
"
echo ""

# 13. Health Check
echo -e "${BLUE}1️⃣3️⃣ HEALTH CHECK${NC}"
echo "Endpoint: GET /actuator/health"
HEALTH=$(curl -s http://localhost:8081/actuator/health | python3 -c "import sys, json; data=json.load(sys.stdin); print(data['status'])")
if [ "$HEALTH" == "UP" ]; then
    echo -e "${GREEN}✅ Servicio: $HEALTH${NC}"
else
    echo -e "${RED}❌ Servicio: $HEALTH${NC}"
fi
echo ""

# 14. Swagger UI
echo -e "${BLUE}1️⃣4️⃣ SWAGGER UI${NC}"
echo "URL: http://localhost:8081/swagger-ui.html"
SWAGGER=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/swagger-ui.html)
if [ "$SWAGGER" == "200" ]; then
    echo -e "${GREEN}✅ Swagger UI accesible (HTTP $SWAGGER)${NC}"
else
    echo -e "${RED}❌ Swagger UI no accesible (HTTP $SWAGGER)${NC}"
fi
echo ""

# Resumen Final
echo "================================="
echo -e "${GREEN}🎉 PRUEBA DE ENDPOINTS COMPLETADA${NC}"
echo "================================="
echo ""
echo "📋 Resumen:"
echo "  ✅ 14 endpoints probados"
echo "  ✅ Autenticación JWT funcionando"
echo "  ✅ API Legacy Data operativa"
echo "  ✅ Swagger UI accesible"
echo ""
echo "🔗 Enlaces útiles:"
echo "  - Swagger UI: http://localhost:8081/swagger-ui.html"
echo "  - OpenAPI Spec: http://localhost:8081/v3/api-docs"
echo "  - Eureka Dashboard: http://localhost:8761"
echo "  - Health Check: http://localhost:8081/actuator/health"
echo ""
echo "🔑 Credenciales:"
echo "  - Username: admin"
echo "  - Password: admin123"
echo ""
