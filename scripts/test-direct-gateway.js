// Direct gateway test - bypasses browser, no CORS
const http = require('http');

function request(method, path, body, token) {
  return new Promise((resolve, reject) => {
    const data = body ? JSON.stringify(body) : null;
    const opts = {
      hostname: 'localhost', port: 8080, path, method,
      headers: { 'Content-Type': 'application/json' }
    };
    if (token) opts.headers['Authorization'] = 'Bearer ' + token;
    if (data) opts.headers['Content-Length'] = Buffer.byteLength(data);
    const req = http.request(opts, (res) => {
      let buf = '';
      res.on('data', c => buf += c);
      res.on('end', () => {
        try { resolve({ status: res.statusCode, body: JSON.parse(buf) }); }
        catch { resolve({ status: res.statusCode, body: buf }); }
      });
    });
    req.on('error', reject);
    if (data) req.write(data);
    req.end();
  });
}

(async () => {
  console.log('=== Direct gateway integration test ===\n');

  // 1. Login
  const login = await request('POST', '/api/v1/auth/login',
    { email: 'admin@pcms.vn', password: 'admin123' });
  console.log('1. Login:', login.status, '- user:', login.body.user?.email);
  const token = login.body.accessToken;
  console.log('   Token issued by:', 'http://localhost:8080 (direct gateway)');
  console.log('   Token claims: role=' + JSON.parse(Buffer.from(token.split('.')[1], 'base64').toString()).role);
  console.log('');

  // 2. Test 12 endpoints
  const endpoints = [
    'users', 'branches', 'medicines', 'customers', 'categories', 'suppliers',
    'inventory', 'orders', 'payments', 'prescriptions', 'notifications', 'reports'
  ];
  console.log('2. Test 12 endpoints via DIRECT gateway (port 8080):');
  for (const ep of endpoints) {
    const r = await request('GET', `/api/v1/${ep}?size=1`, null, token);
    const total = r.body?.total ?? (Array.isArray(r.body) ? r.body.length : '?');
    console.log(`   ${ep.padEnd(15)} HTTP ${r.status}  total=${total}`);
  }
  console.log('');

  // 3. Verify customer UUID
  console.log('3. Verify customer UUID (real MySQL vs mock):');
  const cust = await request('GET', '/api/v1/customers?size=1', null, token);
  console.log('   Response:', JSON.stringify(cust.body).slice(0, 200));
  console.log('   First customer ID:', cust.body?.data?.[0]?.id || 'EMPTY');
  console.log('   Mock seed UUID was:    021fa652-c819-4fc4-95e2-93146d39e3c4');
  console.log('   Expected real UUID:   ee392a88-2c16-4688-82fc-b0c9881454d4');
  console.log('');

  // 4. Test orders
  console.log('4. Verify orders:');
  const ord = await request('GET', '/api/v1/orders?size=2', null, token);
  console.log('   Total:', ord.body?.total, 'first orderNumber:', ord.body?.data?.[0]?.orderNumber);
  console.log('');

  // 5. Test notifications + reports
  console.log('5. Test notifications (which returned 400 earlier):');
  const notif = await request('GET', '/api/v1/notifications?size=1', null, token);
  console.log('   Status:', notif.status, 'Body:', JSON.stringify(notif.body).slice(0, 200));
  console.log('');

  console.log('6. Test reports (which returned 404 earlier):');
  const rep = await request('GET', '/api/v1/reports?size=1', null, token);
  console.log('   Status:', rep.status, 'Body:', JSON.stringify(rep.body).slice(0, 200));
})();
