#!/bin/bash
# =====================================================
# PCMS - Start all services in order (local dev)
# Requires: MySQL running, config-server started first
# Use this when running directly with java -jar (not Docker)
# =====================================================

set -e
BASE_DIR=$(cd "$(dirname "$0")/.." && pwd)
LOG_DIR="$BASE_DIR/logs"
mkdir -p "$LOG_DIR"

start_service() {
  local name=$1
  local jar=$(ls "$BASE_DIR/$name/target/"*.jar 2>/dev/null | head -1)
  if [ -z "$jar" ]; then
    echo "❌ $name JAR not found. Run ./scripts/build-all.sh first."
    exit 1
  fi
  # Config-server needs --spring.profiles.active=native
  local extra_args=""
  if [ "$name" = "config-server" ]; then
    extra_args="--spring.profiles.active=native"
  fi
  echo "▶ Starting $name..."
  nohup java $extra_args -jar "$jar" > "$LOG_DIR/$name.log" 2>&1 &
  echo "  PID: $!  |  Log: $LOG_DIR/$name.log"
  sleep 5
}

echo "======================================"
echo "  PCMS - Starting services in order..."
echo "======================================"

start_service config-server
echo "⏳ Waiting 15s for config-server..."
sleep 15

start_service discovery-server
echo "⏳ Waiting 10s for discovery-server..."
sleep 10

start_service api-gateway

# Start all business services
for svc in user-service branch-service catalog-service category-service supplier-service \
           inventory-service customer-service order-service payment-service \
           prescription-service notification-service report-service; do
  start_service "$svc"
done

echo ""
echo "======================================"
echo "  ✅ All 15 services started!"
echo "======================================"
echo "  Eureka Dashboard:  http://localhost:8761"
echo "  API Gateway:       http://localhost:8080"
echo "  Config Server:     http://localhost:8888"
echo "  Logs:              $LOG_DIR/"
echo ""
echo "Stop with: ./scripts/stop-all.sh"
