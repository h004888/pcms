#!/bin/bash
# =====================================================
# PCMS - Start all services in order (local dev)
# Requires: MySQL running, build artifacts ready
# Use this when running directly with java -jar (not Docker)
# =====================================================
# Thứ tự khởi động (khớp docker-compose.yml):
#   1. config-server (8888)   - BẮT BUỘC đầu tiên
#   2. discovery-server (8761)- phụ thuộc config-server
#   3. api-gateway (8080)     - phụ thuộc cả 2 trên
#   4. 17 business services   - phụ thuộc tất cả infra
# =====================================================

set -e
BASE_DIR=$(cd "$(dirname "$0")/.." && pwd)
LOG_DIR="$BASE_DIR/logs"
mkdir -p "$LOG_DIR"

# Health check: đợi service UP (max 60s) thay vì sleep cố định
wait_for_health() {
  local name=$1
  local url=$2
  local max_wait=${3:-60}
  local waited=0
  echo -n "  ⏳ Waiting for $name"
  while [ $waited -lt $max_wait ]; do
    if curl -fsS -o /dev/null --max-time 2 "$url" 2>/dev/null; then
      echo " ✓ (${waited}s)"
      return 0
    fi
    echo -n "."
    sleep 2
    waited=$((waited + 2))
  done
  echo " ✗ TIMEOUT after ${max_wait}s"
  return 1
}

start_service() {
  local name=$1
  local jar=$(ls "$BASE_DIR/$name/target/"*.jar 2>/dev/null | head -1)
  if [ -z "$jar" ]; then
    echo "❌ $name JAR not found. Run ./scripts/build-all.sh first."
    exit 1
  fi
  # Config-server cần --spring.profiles.active=native để load từ classpath:/config
  local extra_args=""
  if [ "$name" = "config-server" ]; then
    extra_args="--spring.profiles.active=native"
  fi
  echo "▶ Starting $name..."
  # extra_args đặt SAU -jar để JVM không interpret là option của java
  nohup java -jar "$jar" $extra_args > "$LOG_DIR/$name.log" 2>&1 &
  echo "  PID: $!  |  Log: $LOG_DIR/$name.log"
}

echo "======================================"
echo "  PCMS - Starting services in order..."
echo "======================================"

# --- 1. config-server (BẮT BUỘC đầu tiên) ---
start_service config-server
wait_for_health "config-server (8888)" "http://localhost:8888/actuator/health" 60 || {
  echo "❌ config-server failed to start. Check $LOG_DIR/config-server.log"
  exit 1
}

# --- 2. discovery-server (Eureka) ---
start_service discovery-server
wait_for_health "discovery-server (8761)" "http://localhost:8761/actuator/health" 60 || {
  echo "❌ discovery-server failed to start. Check $LOG_DIR/discovery-server.log"
  exit 1
}

# --- 3. api-gateway ---
start_service api-gateway
wait_for_health "api-gateway (8080)" "http://localhost:8080/actuator/health" 60 || {
  echo "❌ api-gateway failed to start. Check $LOG_DIR/api-gateway.log"
  exit 1
}

# --- 4. Business services (17 modules từ parent pom) ---
# Thứ tự trong vòng for KHÔNG quan trọng — các service độc lập.
# Sắp xếp theo alphabet để dễ đọc.
BUSINESS_SERVICES=(
  branch-service
  catalog-service
  category-service
  customer-portal-service
  customer-service
  ecom-ops-service
  health-tools-service
  inventory-service
  mobile-bff
  notification-service
  order-service
  payment-service
  pharmacist-workbench-service
  prescription-service
  report-service
  supplier-service
  user-service
)

for svc in "${BUSINESS_SERVICES[@]}"; do
  start_service "$svc"
  # Mỗi service cần ~10s để fetch config + register Eureka. Không cần đợi 100% UP
  # vì script cuối sẽ tổng hợp status.
  sleep 8
done

echo ""
echo "⏳ Waiting 30s for all services to fully register with Eureka..."
sleep 30

echo ""
echo "======================================"
echo "  ✅ All 20 services launched!"
echo "======================================"
echo "  Eureka Dashboard:  http://localhost:8761"
echo "  API Gateway:       http://localhost:8080"
echo "  Config Server:     http://localhost:8888"
echo "  Logs:              $LOG_DIR/"
echo ""
echo "Stop with: ./scripts/stop-all.sh"
echo ""
echo "=== Eureka registration status ==="
curl -s "http://localhost:8761/eureka/apps" 2>/dev/null \
  | grep -oE '<name>[A-Z-]+</name>|<status>[A-Z]+</status>' \
  | paste -d' ' - - \
  | sort -u