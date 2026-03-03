import requests

url = "https://api.coindcx.com/exchange/v1/orders/create_multiple"
headers = {
    'Content-Type': 'application/json'
}
data = '{"orders":[{"side":"buy","order_type":"market_order","market":"DOGEINR","total_quantity":100,"timestamp":1772532828291,"ecode":"I","client_order_id":"b04ea3df9612464d869ed2ea262ea402"}]}'

response = requests.post(url, data=data, headers=headers)
print(response.status_code)
print(response.text)
