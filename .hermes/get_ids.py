import subprocess, json

with open("/c/Users/ADMIN/AppData/Local/Temp/admin.tok") as f:
    token = f.read().strip()

def curl_json(path):
    r = subprocess.run(
        ["curl", "-s", "-H", "Authorization: Bearer ${TKN}",
         "http://localhost:8080/api/v1/${path}"],
        capture_output=True, text=True, timeout=15,
        env={"TKN": token, "PATH": "/c/Windows/System32:/usr/bin:/bin"}
    )
    try:
        return json.loads(r.stdout)
    except Exception as e:
        return {"_error": str(e), "_raw": r.stdout[:200]}

ids = {}
for path in ["branches", "medicines", "customers"]:
    data = curl_json(path)
    if data.get("data"):
        ids[path] = data["data"][0]["id"]
    else:
        ids[path] = None
        print(f"WARN {path}: no data, response: {json.dumps(data)[:200]}")

print(json.dumps(ids, indent=2))

with open("/tmp/test_ids.json", "w") as f:
    json.dump(ids, f)