#!/bin/bash
set -e
cd "C:/Users/ADMIN/Downloads/temp_v12/pcms"

TMPLOGIN="C:/Users/ADMIN/AppData/Local/Temp/login.json"

curl -s -X POST -H "Content-Type: application/json" \
    -d '{"email":"admin@pcms.vn","password":"admin123"}' \
    http://localhost:8080/api/v1/auth/login > "$TMPLOGIN"

python <<'PYEOF'
import json
with open(r"C:\Users\ADMIN\AppData\Local\Temp\login.json") as f:
    t = json.load(f).get('accessToken','')
with open(r"C:\Users\ADMIN\Downloads\temp_v12\pcms\.hermes\saga_setup.sh") as f:
    src = f.read()
src2 = src.replace('eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJ0eXBlIjoiYWNjZXNzIiwiZW1haWwiOiJhZG1pbkBwY21zLnZuIiwic3ViIjoiMTFmMTZjYzEtMGIxNi00NmRjLWIwZDEtYjgxZWE0YjkxZTNjIiwianRpIjoiMGVkNmM2ZTQtNDMwNy00ZTZhLTg4NDctMWFjMzk3NzRhYTZmIiwiaWF0IjoxNzgyMDkxNTc5LCJleHAiOjE3ODIwOTI0Nzl9.0JJV58iP8hIJM780KiVq4aHEV7id_CIM7lRB5WiPBqU', t)
with open(r"C:\Users\ADMIN\Downloads\temp_v12\pcms\.hermes\saga_setup.sh", "w") as f:
    f.write(src2)
print("Token len:", len(t))
PYEOF

set -e
TMPLOGIN="C:/Users/ADMIN/AppData/Local/Temp/login.json"
TOKEN=$(python -c "import json;print(json.load(open(r'C:\Users\ADMIN\AppData\Local\Temp\login.json'))['accessToken'])")
HDR="Authorization: Bearer *** __TOKEN_PLACEHOLDER__ replaced already, recompose)
HDR_REPLACED=${HDR/__TOKEN_PLACEHOLDER__/"$TOKEN"}
HDR="Authorization: Bearer ${TOKEN}"
echo "Token len: ${#TOKEN}"

NON_RX=$(curl -s -H "$HDR" "http://localhost:8080/api/v1/medicines?size=20" \
  | python -c "import sys,json;d=json.load(sys.stdin)['data'];print([m['id'] for m in d if not m.get('prescriptionRequired')][0])")

BRANCH=$(curl -s -H "$HDR" "http://localhost:8080/api/v1/branches" \
  | python -c "import sys,json;print(json.load(sys.stdin)['data'][0]['id'])")

CUST=$(curl -s -H "$HDR" "http://localhost:8080/api/v1/customers" \
  | python -c "import sys,json;print(json.load(sys.stdin)['data'][0]['id'])")

MED_PRICE=$(curl -s -H "$HDR" "http://localhost:8080/api/v1/medicines?size=20" \
  | python -c "import sys,json;d=json.load(sys.stdin)['data'];print([m['price'] for m in d if m['id']=='$NON_RX'][0])")

echo "NON_RX=$NON_RX"
echo "BRANCH=$BRANCH"
echo "CUST=$CUST"
echo "MED_PRICE=$MED_PRICE"

echo
echo "=== Creating order ==="
RESP=$(curl -s -X POST -H "$HDR" -H "Content-Type: application/json" \
  -d "{\"branchId\":\"$BRANCH\",\"customerId\":\"$CUST\",\"items\":[{\"medicineId\":\"$NON_RX\",\"quantity\":2,\"unitPrice\":$MED_PRICE}],\"staffId\":\"00000000-0000-0000-0000-000000000001\"}" \
  http://localhost:8080/api/v1/orders)

echo "$RESP" | python -m json.tool 2>/dev/null | head -25

ORDER_ID=$(echo "$RESP" | python -c "import sys,json;d=json.load(sys.stdin);print(d.get('data',{}).get('id') or d.get('id') or '')")
echo
echo "ORDER_ID=$ORDER_ID"

# Save env to a file bash can re-source (no token in content)
cat > /tmp/saga_test.env <<ENVEOF
export TOKEN='***...export ORDER_ID='$ORDER_ID'
export BRANCH='$BRANCH'
export CUST='$CUST'
export NON_RX='$NON_RX'
ENVEOF
# Replace token placeholder from real value at write-time
sed -i.bak "s|export TOKEN=.*|export TOKEN=*** "$TOKEN"\"|" /tmp/saga_test.env && rm /tmp/saga_test.env.bak
echo "Saved /tmp/saga_test.env"
