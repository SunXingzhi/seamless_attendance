package tech.xuexinglab.seamless_attendance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import tech.xuexinglab.seamless_attendance.DTO.deviceDTO;
import tech.xuexinglab.seamless_attendance.configuration.MqttProperties;
import tech.xuexinglab.seamless_attendance.entity.device;
import tech.xuexinglab.seamless_attendance.entity.user;
import tech.xuexinglab.seamless_attendance.mapper.deviceMapper;
import tech.xuexinglab.seamless_attendance.mapper.userMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

@Service
public class DeviceBitmapService {
	private static final Logger logger = LoggerFactory.getLogger(DeviceBitmapService.class);

	@Autowired
	private MqttMessageSender mqttMessageSender;

	@Autowired
	private MqttProperties mqttProperties;

	@Autowired
	private deviceMapper deviceMapper;

	@Autowired
	private userMapper userMapper;

	@Autowired
	private utilitySevice utilitySevice;

	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();

	// 新的 MQTT 主题
	private static final String SHOW_TOPIC = "ESP32C3/show";

	// 分片数量
	private static final int TOTAL_PARTS = 3;

	/**
	 * 处理设备人员字模生成和发送
	 *
	 * @param originDeviceName
	 * @param personnelNameOrNumber  人员工号（现在前端发送的是工号）
	 */
	public void processDevicePersonnelBitmap(String originDeviceName, String personnelNameOrNumber, int device_index) {
		try {
			logger.info("开始处理设备人员字模: originDeviceName={}, personnel={}", originDeviceName,
					personnelNameOrNumber);

			// 0. 判断传入的是工号还是姓名，并获取对应的姓名
			String personnelName = personnelNameOrNumber;
			user user = userMapper.getUserInfoByUserNumber(personnelNameOrNumber);
			if (user != null && user.getName() != null && !user.getName().isEmpty()) {
				// 传入的是工号，查询到对应的姓名
				personnelName = user.getName();
				logger.info("通过工号 {} 查询到对应姓名: {}", personnelNameOrNumber, personnelName);
			} else {
				// 传入的可能就是姓名，直接使用
				logger.info("直接使用姓名: {}", personnelName);
			}

			// 1. 计算 INDEX（使用工号来匹配）
			int index = calculatePersonnelIndex(originDeviceName, personnelNameOrNumber, device_index);
			if (index == -1) {
				logger.error("计算 INDEX 失败: originDeviceName={}, personnel={}", originDeviceName,
						personnelNameOrNumber);
				return;
			}

			// 2. 调用 Python 服务获取字模数据（使用姓名）
			String hexData = getBitmapFromPythonService(originDeviceName, personnelName);
			if (hexData == null || hexData.isEmpty()) {
				logger.warn("Python 服务不可用，使用模拟字模数据");
				// hexData = generateMockBitmapData(personnelName);

			}

			// 3. 分片字模数据
			List<String> dataParts = splitBitmapData(hexData, TOTAL_PARTS);
			if (dataParts.size() != TOTAL_PARTS) {
				logger.error("字模数据分片失败，期望分片数: {}, 实际分片数: {}", TOTAL_PARTS, dataParts.size());
				return;
			}

			// 4. 发送分片数据到 MQTT
			sendBitmapPartsToMqtt(originDeviceName, index, personnelName, dataParts);

			logger.info("设备人员字模处理完成: originDeviceName={}, personnel={}, index={}",
					originDeviceName, personnelName, index);

		} catch (Exception e) {
			logger.error("处理设备人员字模时发生错误", e);
		}
	}

	/**
	 * 计算人员在设备中的 INDEX
	 * 现在使用工号来匹配人员
	 */
	private int calculatePersonnelIndex(String originDeviceName, String personnelUserNumber, int device_index) {
		try {
			// 获取所有以该原始设备名称开头的设备
			List<device> devices = deviceMapper.getDevicesByOriginName(originDeviceName);
			if (devices == null || devices.isEmpty()) {
				logger.warn("未找到以 {} 开头的设备", originDeviceName);
				return -1;
			}

			// 按设备名称排序（A1, A2, A3...）
			devices.sort((d1, d2) -> {
				String index1 = utilitySevice.separateLettersAndNumbers(d1.getDeviceName())[1];
				String index2 = utilitySevice.separateLettersAndNumbers(d2.getDeviceName())[1];
				return Integer.compare(Integer.parseInt(index1), Integer.parseInt(index2));
			});

			int globalIndex = 1;

			// 遍历所有设备，查找目标人员
			for (device dev : devices) {
				String personnels = dev.getPersonnels();
				if (personnels != null && !personnels.isEmpty()) {
					// 解析人员列表
					List<String> personnelList = parsePersonnelList(personnels);

					// 在当前设备中查找目标人员（使用工号匹配）
					for (int i = 0; i < personnelList.size(); i++) {
						String currentPersonnel = personnelList.get(i);
						if (currentPersonnel.equals(personnelUserNumber)) {
							return globalIndex;
						}
						globalIndex++;
					}
				}
			}

			logger.warn("未找到工号 {} 在设备 {} 中的分配", personnelUserNumber, originDeviceName);
			return -1;

		} catch (Exception e) {
			logger.error("计算 INDEX 时发生错误", e);
			return -1;
		}
	}

	/**
	 * 解析人员列表字符串
	 */
	private List<String> parsePersonnelList(String personnels) {
		try {
			if (personnels.startsWith("[") && personnels.endsWith("]")) {
				// JSON 数组格式
				return objectMapper.readValue(personnels, new TypeReference<List<String>>() {
				});
			} else {
				// 逗号分隔格式
				return Arrays.asList(personnels.split(","));
			}
		} catch (Exception e) {
			logger.warn("解析人员列表失败，使用逗号分隔: {}", personnels);
			return Arrays.asList(personnels.split(","));
		}
	}

	/**
	 * 调用 Python 服务获取字模数据
	 */
	private String getBitmapFromPythonService(String originDeviceName, String personnelName) {
		try {
			String url = "http://127.0.0.1:5000/generate_bitmap";

			// 构建请求体
			Map<String, String> requestBody = new HashMap<>();
			requestBody.put("origin_device_name", originDeviceName);
			requestBody.put("personnel_name", personnelName);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

			ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

			if (response.getStatusCode() == HttpStatus.OK) {
				// 解析响应，提取 hex_data
				Map<String, Object> responseMap = objectMapper.readValue(response.getBody(),
						new TypeReference<Map<String, Object>>() {
						});

				if (responseMap.containsKey("hex_data")) {
					return responseMap.get("hex_data").toString();
				} else if (responseMap.containsKey("error")) {
					logger.error("Python 服务返回错误: {}", responseMap.get("error"));
				}
			} else {
				logger.error("Python 服务调用失败，状态码: {}", response.getStatusCode());
			}

		} catch (Exception e) {
			logger.error("调用 Python 服务时发生错误", e);
		}

		return null;
	}

	/**
	 * 分片字模数据
	 */
	private List<String> splitBitmapData(String hexData, int totalParts) {
		List<String> parts = new ArrayList<>();

		try {
			// 清理数据，移除换行和空格
			String cleanData = hexData.replace("\n", "").replace(" ", "");

			// 计算每部分长度
			int totalLength = cleanData.length();
			int partLength = (int) Math.ceil((double) totalLength / totalParts);

			// 分片
			for (int i = 0; i < totalParts; i++) {
				int start = i * partLength;
				int end = Math.min(start + partLength, totalLength);

				if (start < totalLength) {
					parts.add(cleanData.substring(start, end));
				} else {
					parts.add(""); // 空分片
				}
			}

		} catch (Exception e) {
			logger.error("分片字模数据时发生错误", e);
		}

		return parts;
	}

	/**
	 * 发送分片数据到 MQTT
	 */
	private void sendBitmapPartsToMqtt(String deviceId, int index, String personnelName, List<String> dataParts) {
		try {
			for (int i = 0; i < dataParts.size(); i++) {
				int partNum = i + 1;
				String data = dataParts.get(i);

				// 构建 MQTT 消息
				Map<String, Object> mqttMessage = new HashMap<>();
				mqttMessage.put("ID", deviceId);
				mqttMessage.put("INDEX", index);
				mqttMessage.put("TYPE", "name");
				mqttMessage.put("PART_NUM", partNum);
				mqttMessage.put("TOTAL_PARTS", TOTAL_PARTS);
				mqttMessage.put("DATA", data);

				String payload = objectMapper.writeValueAsString(mqttMessage);

				// 发送 MQTT 消息
				mqttMessageSender.sendMessage(SHOW_TOPIC, payload);

				logger.info("发送 MQTT 消息成功: partNum={}, index={}, deviceId={}",
						partNum, index, deviceId);

				// 添加短暂延迟，避免消息发送过快
				Thread.sleep(2000);
			}

		} catch (Exception e) {
			logger.error("发送 MQTT 消息时发生错误", e);
		}
	}
}