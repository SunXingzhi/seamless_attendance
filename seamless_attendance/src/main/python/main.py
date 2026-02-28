from flask import Flask, request, jsonify
from PIL import Image, ImageDraw, ImageFont
import numpy as np

app = Flask(__name__)

# 图片尺寸200×70
image_width = 200
image_height = 70
font_path = "C:/Windows/Fonts/STKAITI.TTF"  # 字体路径有效
font_size = 50
bytes_per_line = 24
indent = "  "
max_char = 3  # 最多支持的汉字数

def generate_bitmap(text):
	# 限制为3个汉字
	if len(text) > max_char:
		text = text[:max_char]
		print(f"文本截断为3字：{text}")
	
	# 固定200×70的1位黑白位图
	image = Image.new("1", (image_width, image_height), 1)
	draw = ImageDraw.Draw(image)
	
	# 加载字体
	try:
		font = ImageFont.truetype(font_path, font_size)
	except Exception as e:
		print(f"字体加载失败：{e}")
		return None
	
	# 计算文本宽高
	bbox = draw.textbbox((0, 0), text, font=font)
	text_width = bbox[2] - bbox[0]
	text_height = bbox[3] - bbox[1]
	
	# 关键：强制压缩文字位置，确保3字在200宽度内
	# 左偏移2像素，减少左右边距，适配200宽度
	position_x = max(0, (image_width - text_width) // 2 - 2)
	position_y = (image_height - text_height) // 2 - 1
	
	# 紧凑加粗：仅右偏移1像素，模拟加粗
	draw.text((position_x + 1, position_y), text, font=font, fill=0)  # 加粗层
	draw.text((position_x, position_y), text, font=font, fill=0)	 # 主文字层
	
	# 转换为数组
	bitmap = np.array(image)
	print(f"已生成「{text}」字模（200×70，字号{font_size}，加粗，3字适配）")
	return bitmap

def bitmap_to_custom_hex(bitmap):
	"""转换为指定样式的十六进制字模"""
	hex_bytes = []
	for row in bitmap:
		for i in range(0, len(row), 8):
			byte = 0
			for j in range(8):
				if i + j < len(row) and row[i + j] == 0:
					byte |= (1 << (7 - j))
			hex_bytes.append(f"0x{byte:02X}")
	
	# 按每行24个拼接格式
	custom_hex_lines = []
	for i in range(0, len(hex_bytes), bytes_per_line):
		line_bytes = hex_bytes[i:i+bytes_per_line]
		line_str = ",".join(line_bytes)
		if i + bytes_per_line < len(hex_bytes):
			line_str += ","
		custom_hex_lines.append(f"{indent}{line_str}")
	
	return "\n".join(custom_hex_lines)

@app.route('/generate_bitmap', methods=['POST'])
def generate_bitmap_data():
	data = request.get_json()
	
	# 支持两种请求格式：
	# 1. 新格式: {"origin_device_name": "A", "personnel_name": "孙浩翔"}
	# 2. 旧格式: {"text": "周南全"}
	
	text = ''
	
	# 优先使用新格式
	if 'personnel_name' in data:
		text = data.get('personnel_name', '').strip()
		origin_device_name = data.get('origin_device_name', '').strip()
		print(f"处理新格式请求: origin_device_name={origin_device_name}, personnel_name={text}")
	elif 'text' in data:
		text = data.get('text', '').strip()
		print(f"处理旧格式请求: text={text}")
	else:
		return jsonify({"error": "请传入正确的参数格式，比如{\"origin_device_name\": \"A\", \"personnel_name\": \"孙浩翔\"} 或 {\"text\": \"周南全\"}"}), 400
	
	if not text:
		return jsonify({"error": "人员姓名或文本内容不能为空"}), 400
	
	bitmap = generate_bitmap(text)
	if bitmap is None:
		return jsonify({"error": "字模生成失败（检查字体路径）"}), 500

	custom_hex = bitmap_to_custom_hex(bitmap)
	
	# 构建响应，保持向后兼容
	response_data = {
		"generated_text": text,
		"font_size": font_size,
		"hex_data": custom_hex
	}
	
	# 如果是新格式请求，添加额外信息
	if 'personnel_name' in data:
		response_data["origin_device_name"] = data.get('origin_device_name', '')
		response_data["personnel_name"] = text
		print(f"字模生成成功: origin_device_name={data.get('origin_device_name', '')}, personnel_name={text}")
	else:
		print(f"字模生成成功: text={text}")
	
	return jsonify(response_data)

if __name__ == '__main__':
	app.run(debug=True)