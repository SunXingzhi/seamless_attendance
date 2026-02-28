import requests
import json

# 测试新的请求格式
url = "http://127.0.0.1:5000/generate_bitmap"

# 测试新格式
print("测试新格式请求...")
data_new = {
	"origin_device_name": "A",
	"personnel_name": "孙浩翔"
}

try:
	response = requests.post(url, json=data_new)
	print(f"状态码: {response.status_code}")
	print(f"响应内容: {response.text}")
	
	if response.status_code == 200:
		result = response.json()
		print(f"生成文本: {result.get('generated_text')}")
		print(f"原始设备名称: {result.get('origin_device_name')}")
		print(f"人员姓名: {result.get('personnel_name')}")
		print(f"字体大小: {result.get('font_size')}")
		print(f"十六进制数据长度: {len(result.get('hex_data', ''))}")
		
		# 显示部分十六进制数据
		hex_data = result.get('hex_data', '')
		if hex_data:
			print(f"前100个字符: {hex_data[:100]}...")
	else:
		print(f"错误: {response.json()}")
		
except Exception as e:
	print(f"请求失败: {e}")

print("\n" + "="*50)

# 测试旧格式（保持兼容性）
print("测试旧格式请求...")
data_old = {
	"text": "周南全"
}

try:
	response = requests.post(url, json=data_old)
	print(f"状态码: {response.status_code}")
	print(f"响应内容: {response.text}")
	
	if response.status_code == 200:
		result = response.json()
		print(f"生成文本: {result.get('generated_text')}")
		print(f"字体大小: {result.get('font_size')}")
		print(f"十六进制数据长度: {len(result.get('hex_data', ''))}")
		
		# 显示部分十六进制数据
		hex_data = result.get('hex_data', '')
		if hex_data:
			print(f"前100个字符: {hex_data[:100]}...")
	else:
		print(f"错误: {response.json()}")
		
except Exception as e:
	print(f"请求失败: {e}")