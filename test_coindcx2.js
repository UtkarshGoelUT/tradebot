const https = require('https');

const data = JSON.stringify([{
    side: "buy",
    order_type: "market_order",
    market: "DOGEINR",
    total_quantity: 100,
    timestamp: 1772532828291,
    ecode: "I",
    client_order_id: "b04ea3df9612464d869ed2ea262ea402"
  }]);

const options = {
  hostname: 'api.coindcx.com',
  port: 443,
  path: '/exchange/v1/orders/create_multiple',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': data.length
  }
};

const req = https.request(options, res => {
  console.log(`statusCode: ${res.statusCode}`);
  let out = '';
  res.on('data', d => { out += d; });
  res.on('end', () => { console.log(out); });
});

req.on('error', error => { console.error(error); });
req.write(data);
req.end();
