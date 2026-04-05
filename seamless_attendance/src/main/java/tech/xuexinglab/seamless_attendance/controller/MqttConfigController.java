package tech.xuexinglab.seamless_attendance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import tech.xuexinglab.seamless_attendance.DTO.MQTTMessageDTO;
import tech.xuexinglab.seamless_attendance.DTO.ResponseDTO;
import tech.xuexinglab.seamless_attendance.DTO.deviceDTO;
import tech.xuexinglab.seamless_attendance.configuration.MqttProperties;
import tech.xuexinglab.seamless_attendance.service.MqttMessageSender;
import tech.xuexinglab.seamless_attendance.service.interfaces.deviceService;
import tech.xuexinglab.seamless_attendance.mapper.deviceMapper;
import tech.xuexinglab.seamless_attendance.entity.device;

@RestController
@RequestMapping("seamless_attendance/api/mqtt")
public class MqttConfigController {

        private static final Logger logger = LoggerFactory.getLogger(MqttConfigController.class);

        @Autowired
        private MqttProperties mqttProperties;

        @Autowired
        private MqttMessageSender mqttMessageSender;

        @Autowired
        private deviceService deviceService;

        @Autowired
        private deviceMapper deviceMapper;

        @GetMapping("/config")
        public String getMqttConfig() {
                StringBuilder config = new StringBuilder();
                config.append("MQTT 配置信息:\n");
                config.append("================\n");
                config.append("Broker: " + mqttProperties.getBroker() + "\n");
                config.append("Client ID: " + mqttProperties.getClientId() + "\n");
                config.append("订阅主题: " + mqttProperties.getSubscribeTopic() + "\n");
                config.append("用户名: " + (mqttProperties.getUsername() != null ? mqttProperties.getUsername() : "未设置")
                                + "\n");
                config.append("密码: " + (mqttProperties.getPassword() != null ? "***" : "未设置") + "\n");
                config.append("QoS: " + mqttProperties.getQos() + "\n");
                return config.toString();
        }

        @GetMapping("/broker")
        public String getBroker() {
                return "MQTT Broker 地址: " + mqttProperties.getBroker();
        }

        @GetMapping("/topic")
        public String getTopic() {
                return "订阅主题: " + mqttProperties.getSubscribeTopic();
        }

        /**
         * 发送MQTT消息
         * 
         * @param topic   消息主题
         * @param payload 消息内容
         * @param qos     服务质量等级 (可选，默认使用配置中的QoS)
         * @return 发送结果
         */
        @PostMapping("/message")
        public ResponseDTO<String> sendMessage(
                        @RequestParam("topic") String topic,
                        @RequestParam("payload") String payload,
                        @RequestParam(value = "qos", required = false) Integer qos) {
                try {
                        if (qos != null) {
                                mqttMessageSender.sendMessage(topic, payload, qos);
                        } else {
                                mqttMessageSender.sendMessage(topic, payload);
                        }
                        return ResponseDTO.success("MQTT消息发送成功");
                } catch (Exception e) {
                        return ResponseDTO.error("MQTT消息发送失败: " + e.getMessage());
                }
        }

        // 控制设备开启蓝牙广播
        @PostMapping("/broadcast")
        public ResponseDTO<String> openBLEBroadcast(@RequestBody MQTTMessageDTO mqttMessageDTO) {
                try {
                        // 暂时写死, 后续根据需求修改
                        mqttMessageSender.sendMessage(mqttProperties.commandTopic,
                                        String.format("{\"ID\":\"%s\",\"CMD\":\"cmd\",\"cmd\":\"restart\"}",
                                                        mqttMessageDTO.getDevice_name()),
                                        mqttProperties.qos);
                        return ResponseDTO.success("广播成功");
                } catch (Exception e) {
                        return ResponseDTO.error("广播失败: " + e.getMessage());
                }
        }

        // 向设备发送分配完成信息
        @PostMapping("/assign/{device_personnel_number}")
        public ResponseDTO<String> alignUser(@PathVariable("device_personnel_number") String device_personnel_number,
                        @RequestBody MQTTMessageDTO mqttMessageDTO) {
                try {
                        String device_name = mqttMessageDTO.getDevice_name();
                        String user_number = mqttMessageDTO.getUser_number();
			Integer deviceDbId = mqttMessageDTO.getDevice_id();

			logger.info("接收到分配请求 - device_name: {}, device_personnel_number: {}, user_number: {}, device_db_id: {}",
					device_name, device_personnel_number, user_number, deviceDbId);
                        logger.info("mqttMessageDTO完整数据: {}", mqttMessageDTO);

			// 检查人员是否已与其他设备绑定
			boolean isPairedWithOtherDevice = deviceService.isPersonPairedWithOtherDevice(user_number,
					deviceDbId);
			if (isPairedWithOtherDevice) {
				return ResponseDTO
						.error(String.format("分配人员%s失败: 该人员已与其他设备绑定", device_personnel_number));
			}

			// 向设备发送分配指令
			mqttMessageSender.sendMessage(mqttProperties.commandTopic,
					String.format("{\"ID\":\"%s\",\"CMD\":\"cmd\",\"cmd\":\"assign %s\"}",
							device_name, device_personnel_number),
					mqttProperties.qos);

			// 更新数据库中的人员信息，设置为已配对状态
			boolean updateSuccess = deviceService.pairPersonWithDevice(user_number, deviceDbId);
                        if (!updateSuccess) {
                                return ResponseDTO.error(String.format("分配人员%s失败: 更新人员状态失败", device_personnel_number));
                        }

                        return ResponseDTO.success(String.format("分配人员%s成功", device_personnel_number));

                } catch (Exception e) {
                        return ResponseDTO
                                        .error(String.format("分配人员%s失败: %s", device_personnel_number, e.getMessage()));
                }
        }

        // 解除人员与设备的配对
        @PostMapping("/unassign")
        public ResponseDTO<String> unassignPerson(@RequestBody MQTTMessageDTO mqttMessageDTO) {
                try {
			String deviceName = mqttMessageDTO.getDevice_name();
			String user_number = mqttMessageDTO.getUser_number();

			logger.info("接收到解除配对请求 - device_name: {}, user_number: {}", deviceName, user_number);

			// 更新数据库中的人员信息，设置为未配对状态
			boolean updateSuccess = deviceService.unpairPersonByDeviceAndUser(deviceName, user_number);
                        if (!updateSuccess) {
                                return ResponseDTO.error("解除配对失败: 更新人员状态失败");
                        }

                        return ResponseDTO.success("解除配对成功");

                } catch (Exception e) {
                        return ResponseDTO
                                        .error(String.format("解除配对失败: %s", e.getMessage()));
                }
        }

        // 恢复设备出厂设置
        @PostMapping("/fullreset")
        public ResponseDTO<String> fullReset(@RequestBody deviceDTO dto) {
                // TODO: process POST request
                try{
                        deviceService.fullResetDevice(dto.getDevice_name());
                }
                catch(Exception e){
                        return ResponseDTO.error("设备恢复出厂设置失败: " + e.getMessage());
                }
                return ResponseDTO.success("设备已恢复出厂设置");
        }

        @PostMapping("/reset")
        public ResponseDTO<String> resetDevice(@RequestBody deviceDTO dto) {
                try {
                        // 根据设备名称获取设备
                        String deviceName = dto.getDevice_name();
                        if (deviceName == null || deviceName.trim().isEmpty()) {
                                return ResponseDTO.error("设备名称不能为空");
                        }
                        
                        // 查找设备ID
                        device device = deviceMapper.getDeviceByDeviceName(deviceName);
                        if (device == null) {
                                return ResponseDTO.error("设备不存在: " + deviceName);
                        }
                        
                        // 调用reset方法
                        boolean resetResult = deviceService.resetDevice(device.getId());
                        if (!resetResult) {
                                return ResponseDTO.error("设备复位失败: 无法找到设备或复位操作失败");
                        }
                        
                        return ResponseDTO.success("设备已恢复出厂设置");
                } catch (Exception e) {
                        return ResponseDTO.error("设备复位失败: " + e.getMessage());
                }
        }

        
}