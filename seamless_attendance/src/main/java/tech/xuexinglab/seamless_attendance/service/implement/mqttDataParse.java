package tech.xuexinglab.seamless_attendance.service.implement;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.xuexinglab.seamless_attendance.service.interfaces.MqttMessageService;
import tech.xuexinglab.seamless_attendance.mapper.LOTMapper;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * mqttDataParse - MQTT消息解析服务
 * 功能：
 * 1. 实现MqttMessageService接口，处理接收到的MQTT消息
 * 2. 解析设备发送的员工状态数据
 * 3. 计算员工状态变化
 * 4. 将员工考勤记录保存到数据库
 * 说明：此类被标注为 @Service，Spring 会自动注册为一个 Bean
 */
@Service
public class mqttDataParse implements MqttMessageService{
	// 数据库操作映射器
	@Autowired
	private LOTMapper lotMapper;
	
	// 日志记录器
	private static final Logger logger = LoggerFactory.getLogger(mqttDataParse.class);

	// 员工状态列表，初始化为缺勤状态
	// 索引0: 员工1状态
	// 索引1: 员工2状态
	// 索引2: 员工3状态
	private List<String> personnel_status_list	= new ArrayList<>();
	
	/**
	 * 构造方法
	 * 初始化员工状态列表为缺勤状态
	 */
	public mqttDataParse() {
		personnel_status_list.add("absent"); // 员工1初始状态：缺勤
		personnel_status_list.add("absent"); // 员工2初始状态：缺勤  
		personnel_status_list.add("absent"); // 员工3初始状态：缺勤
	}

	/**
	 * 计算员工状态文本
	 * @param currentStatus 当前状态值 (1=在岗, 0=离岗)
	 * @param lastActive 上一次是否在岗
	 * @return 状态文本 (active=在岗, leave=离岗, absent=缺勤)
	 */
	private String computeStatusText(Integer currentStatus, boolean lastActive) {
		// currentStatus: 1=在岗(active), 0=离岗/未在岗
		if (currentStatus == null) {
			return "absent"; // 状态为空时返回缺勤
		}
		if (currentStatus == 1 && !lastActive) {
			return "active"; // 从离岗变为在岗
		}
		if (currentStatus == 0 && lastActive) {
			return "leave"; // 从在岗变为离岗
		}
		return currentStatus == 1 ? "active" : "absent"; // 保持当前状态
	}

	/**
	 * 处理接收到的MQTT消息
	 * @param topic 消息主题
	 * @param payload 消息内容
	 */
	@Override
	public void personnel_status_analysis(String topic, String payload){
		logger.info("MQTT message received. topic='{}' payload='{}'", topic, payload);

		// 检查消息内容是否为空
		if (payload == null || payload.trim().isEmpty()) {
			logger.warn("Empty payload, skip processing");
			return;
		}

		try {
			// 解析JSON数据为Device_message对象
			Device_message deviceMessage = JSON.parseObject(payload, Device_message.class);
			if (deviceMessage == null) {
				logger.warn("Parsed Device_message is null");
				return;
			}

			// 检查设备ID是否有效
			if (deviceMessage.ID == null || deviceMessage.ID.trim().isEmpty()) {
				logger.warn("Missing device ID, skip processing. payload='{}'", payload);
				return;
			}
			logger.info("Device ID: {}", deviceMessage.ID);

			// 获取当前状态列表
			// 当前 mqtt client 发送数据示例：{"ID":"A1","P1":1,"P2":1,"P3":1}
			List<Integer> currentStatuses = deviceMessage.toStatusList();
			if (currentStatuses.size() < 3) {
				logger.warn("Invalid status list length={}, payload='{}'", currentStatuses.size(), payload);
				return;
			}

			// 分析每个员工的状态变化
			for (int i = 0; i < 3; i++) {
				Integer currentStatus = currentStatuses.get(i);
				boolean lastActive = "active".equals(personnel_status_list.get(i));
				String statusText = computeStatusText(currentStatus, lastActive);
				personnel_status_list.set(i, statusText);
				logger.info("Person: P{} Status: {} ({})", i + 1, currentStatus, statusText);
			}

			// 数据库操作：每条消息写入一次考勤记录
			saveAttendanceRecord(deviceMessage.ID, personnel_status_list);

		} catch (Exception e) {
			logger.error("Failed to parse or handle mqtt payload", e);
		}
	}

	/**
	 * 保存考勤记录到数据库
	 * @param device_id 设备ID
	 * @param personnel_status_list 员工状态列表
	 */
	public void saveAttendanceRecord(String device_id, List<String> personnel_status_list){
		try{
			// 验证参数有效性
			if (device_id == null || device_id.trim().isEmpty()) {
				logger.warn("device_id is empty, skip saving");
				return;
			}
			if (personnel_status_list == null || personnel_status_list.size() < 3) {
				logger.warn("personnel_status_list is invalid, skip saving");
				return;
			}
			// 检查状态列表中的每个状态是否有效
			for(String personnel_status : personnel_status_list){
				if(personnel_status==null || personnel_status.isEmpty()){
					logger.warn("the personnel_status is invalid");
				}
			}
			// 使用小型设备(每台设备支持3个人的录入)
			lotMapper.smallSaveAttendanceRecord(device_id, 
									personnel_status_list.get(0), // 员工1状态
									personnel_status_list.get(1), // 员工2状态
									personnel_status_list.get(2)); // 员工3状态

		} catch(Exception e){
			logger.error("Failed to save the attendance record"+e);
		}
	}

	/**
	 * Device_message - 设备消息数据模型
	 * 用于解析MQTT消息中的设备数据
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Device_message{
		public String ID; // 设备ID
		public Integer P1; // 员工1状态 (1=在岗, 0=离岗)
		public Integer P2; // 员工2状态
		public Integer P3; // 员工3状态

		/**
		 * 将设备消息转换为状态列表
		 * @return 状态列表 [P1, P2, P3]
		 */
		public List<Integer> toStatusList() {
			List<Integer> list = new ArrayList<>();
			list.add(P1);
			list.add(P2);
			list.add(P3);
			return list;
		}
	}
}
