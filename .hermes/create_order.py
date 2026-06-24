import subprocess, json

# Login
r = subprocess.run(
    ["curl", "-s", "-X", "POST", "-H", "Content-Type: application/json",
     "-d", '{"email":"admin@pcms.vn","password":"admin123"}',
     "http://localhost:8080/api/v1/auth/login"],
    capture_output=True, text=True, timeout=15
)
token = json.loads(r.stdout).get("accessToken")
AUTH=*** + "Bearer " + token

def curl_json(method, path, data=None):
    args = ["curl", "-s", "-X", method, "-H", AUTH,
            "-H", "Content-Type: application/json",
            "http://localhost:8080/api/v1/" + path]
    if data is not None:
        args += ["-d", json.dumps(data)]
    r = subprocess.run(args, capture_output=True, text=True, timeout=30)
    try:
        return json.loads(r.stdout)
    except Exception as e:
        return {"_error": str(e), "_raw": r.stdout[:300]}

# Find non-prescription medicine
data = curl_json("GET", "medicines?size=20")
non_rx_med = None
for m in data.get("data", []):
    if not m.get("prescriptionRequired"):
        non_rx_med = m
        break

print("Non-RX med:", non_rx_med.get("id"), non_rx_med.get("name"))

branch = curl_json("GET", "branches")["data"][0]
cust = curl_json("GET", "customers")["data"][0]

order_payload = {
    "branchId": branch["id"],
    "customerId": cust["id"],
    "items": [{"medicineId": non_rx_med["id"], "quantity": 2, "unitPrice": non_rx_med["price"]}],
    "staffId": "00000000-0000-0000-0000-000000000001"
}
print("\n=== Creating order ===")
order_resp = curl_json("POST", "orders", order_payload)
print(json.dumps(order_resp, indent=2)[:600])
order_id = order_resp.get("data", {}).get("id") or order_resp.get("id")
print("\nORDER_ID=", order_id)

with open("C:/Users/ADMIN/AppData/Local/Temp/test_state.json", "w") as f:
    json.dump({"order_id": order_id, "ids": {"med": non_rx_med["id"], "branch": branch["id"], "cust": cust["id"]}, "token": token}, f)
print("Saved test_state.json")