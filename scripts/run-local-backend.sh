#!/usr/bin/env bash
# PCMS Backend - Local run script (no Docker required)
# Builds + starts config-server, discovery, gateway + all business services

set -e
M2_HOME="/c/Users/ADMIN/Downloads/temp_v12/pcms/apache-maven-3.9.9"
export PATH="$M2_HOME/bin:$PATH"

ROOT="C:/Users/ADMIN/Downloads/temp_v12/pcms"
LOG_DIR="$ROOT/logs/local"
mkdir -p "$LOG_DIR"

# === Step 1: Build all modules (skip tests) ===
echo "=== Building all modules (skip tests) ==="
cd "$ROOT"
mvn clean install -DskipTests -q 2>&1 | tail -30 || {
  echo "Build FAILED - check errors above"
  exit 1
}
echo "BUILD OK"
echo ""

# === Step 2: Apply seed admin user ===
echo "=== Applying seed admin user ==="
/c/Program\ Files/MySQL/MySQL\ Server\ 9.5/bin/mysql.exe -u pcms_user -ppcms_pass pcms_user < "$ROOT/scripts/seed-admin-user.sql" 2>&1 | grep -v Warning || true
echo "SEED OK"
echo ""

# === Step 3: Start config-server (port 8888) ===
echo "=== Starting config-server (port 8888) ==="
cd "$ROOT/config-server"
nohup mvn spring-boot:run -q > "$LOG_DIR/config-server.log" 2>&1 &
CONFIG_PID=$!
echo "config-server PID: $CONFIG_PID"
sleep 25
echo ""

# === Step 4: Start discovery-server (port 8761) ===
echo "=== Starting discovery-server (port 8761) ==="
cd "$ROOT/discovery-server"
nohup mvn spring-boot:run -q > "$LOG_DIR/discovery-server.log" 2>&1 &
DISCOVERY_PID=$!
echo "discovery-server PID: $DISCOVERY_PID"
sleep 25
echo ""

# === Step 5: Start api-gateway (port 8080) ===
echo "=== Starting api-gateway (port 8080) ==="
cd "$ROOT/api-gateway"
nohup mvn spring-boot:run -q > "$LOG_DIR/api-gateway.log" 2>&1 &
GATEWAY_PID=$!
echo "api-gateway PID: $GATEWAY_PID"
sleep 30
echo ""

echo "=== INFRASTRUCTURE READY ==="
echo "  config-server:    http://localhost:8888 (PID $CONFIG_PID)"
echo "  discovery-server: http://localhost:8761 (PID $DISCOVERY_PID)"
echo "  api-gateway:      http://localhost:8080 (PID $GATEWAY_PID)"
echo ""
echo "  Logs: $LOG_DIR"
echo ""
echo "=== Now start business services manually (recommended to do one at a time) ==="
echo ""
echo "To start all business services in background:"
for svc in user-service branch-service catalog-service category-service supplier-service inventory-service customer-service order-service payment-service prescription-service notification-service report-service; do
  echo "  cd $ROOT/$svc && nohup mvn spring-boot:run -q > $LOG_DIR/${svc}.log 2>&1 &"
done
