import requests

url = "http://127.0.0.1:5000/generate_bitmap"
data = {"text": "周南全"}  # 替换为你要生成的汉字

response = requests.post(url, json=data)
result = response.json()

# 打印格式化后的字模（直接复制可用）
print("生成的字模数据：")
print(result["hex_data"])