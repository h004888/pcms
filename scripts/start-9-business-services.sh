#!/bin/bash
# Start all 9 PCMS business services with correct MySQL env vars
# Each service runs in background, logs to logs/<service>.log

set -e
ROOT="C:/Users/ADMIN/Downloads/temp_v12/pcms"
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_USER=pcms_user
export MYSQL_PASSWORD=pcms_pass

# Services that need to start AFTER gateway is running
SERVICES=(
  "branch-service:8082:pcms_branch"
  "category-service:8084:pcms_category"
  "supplier-service:8085:pcms_supplier"
  "inventory-service:8086:pcms_inventory"
  "order-service:8088:pcms_order"
  "payment-service:8089:pcms_payment"
  "prescription-service:8090:pcms_prescription"
  "notification-service:8091:pcms_notification"
  "report-service:8092:pcms_report"
)

cd "$ROOT"

for entry in "${SERVICES[@]}"; do
  IFS=':' read -r name port db <<< "$entry"
  echo "=== Starting $name (port $port, db $db) ==="
  cd "$ROOT/$name"
  export MYSQL_DB=$db
  nohup java -jar target/${name}-1.0.0-SNAPSHOT.jar \
    > "$ROOT/logs/${name}.log" 2>&1 &
  PID=$!
  echo "  PID: $PID"
  unset MYSQL_DB
done

echo ""
echo "=== All 9 services launched. Waiting 60s for startup... ==="
sleep 60
echo ""
echo "=== Eureka registration status ==="
curl -s http://localhost:8761/eureka/apps 2>&1 | grep -oE '<name>[A-Z-]+</name>' | sort -u
