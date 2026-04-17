package tech.xuexinglab.seamless_attendance.service;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
// import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;


import org.eclipse.paho.client.mqttv3.MqttCallback;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import tech.xuexinglab.seamless_attendance.configuration.MqttProperties;
import tech.xuexinglab.seamless_attendance.controller.WebSocketController;
// import tech.xuexinglab.seamless_attendance.service.interfaces.MqttMessageService;	 已经废弃
import tech.xuexinglab.seamless_attendance.service.implement.MqttMessageHandlerService;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

@Service
public class MqttPushCallback implements MqttCallback {

	private static final Logger logger = LoggerFactory.getLogger(MqttPushCallback.class);

	@Autowired
	private MqttProperties mqttProperties;

	@Autowired
	private MqttMessageHandlerService hardwareMessageHandler;

	@Autowired
	private WebSocketController webSocketController;

	@Autowired
	private mqttDataParse mqttDataParse;

	@Autowired
	private MqttMessageSender messageSender;

	/**
	 * 连接丢失回调
	 * 
	 * @param cause 连接丢失的原因
	 */
	@Override
	public void connectionLost(Throwable cause) {
		logger.warn("MQTT connection lost", cause);
		if (cause != null) {
			logger.error("Connection lost cause: {}", cause.getMessage());
		}
		// 仅依赖内置的自动重连机制
	}

	/**
         * 消息到达回调
         * 
         * @param topic   消息主题
         * @param message 消息内容
         * @throws Exception 处理异常
         */
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
                String  payload;
                String  deviceId;
		String	feedBackStatus;
		String	feedBackConfirmString;	// 设备反馈确认指令字符串

                payload			= new String(message.getPayload(), "UTF-8");
		JSONObject jsonObject	= JSON.parseObject(payload);
		deviceId		= null; // 默认设备ID
		feedBackConfirmString	= null; // 默认反馈确认指令字符串
		feedBackStatus		= null; // 默认反馈状态

                logger.info("Received message. topic={}, payload={}", topic, payload);

                try {
                        // 获取设备ID(设备名称)
                        deviceId = jsonObject.getString("ID");
                        if ((deviceId == null || deviceId.isEmpty()) || payload.isEmpty()) {
                                logger.warn("Invalid message format. Skipping processing.");
                                return;
                        }

                        // 根据不同主题解析
                        if (topic.equals(mqttProperties.subscribeTopic)) {
                                
				
                                // 调用硬件消息处理器处理状态消息
                                if (hardwareMessageHandler != null) {
                                        hardwareMessageHandler.handleStatusMessage(deviceId,
                                                        payload);
                                } else {
                                        logger.error("Hardware message handler is not initialized");
                                }
                        } else if (topic.equals(mqttProperties.feedbackTopic)) {
                                // 处理反馈消息
                                // 当接收到feedbackTopic的消息时，向前端推送配对完成消息
                                try {
                                        
                                        // 反馈消息格式: {"ID":"A1","status":"OK"}
                                        logger.info("Processing feedback message. Payload: {}",
                                                        payload);
                                        if (payload != null && !payload.isEmpty()
                                                        && payload.contains("ID")
							&& payload.contains("status")) {

						// 获取feedback状态
						feedBackStatus = jsonObject.getString("status");
						logger.info("Parsed device ID: {}, feedback status: {}",
								deviceId, feedBackStatus);
						// 配对完成反馈
						if(feedBackStatus.equals("OK")){
							// 向前端推送配对完成消息
							logger.info("Pairing completed for device: {}",
									deviceId);
							webSocketController
									.sendPairingCompleteMessage(deviceId);
							logger.info("Sent pairing complete message to frontend");
							// 这里不需要发送MQTT指令，由前端在接收到通知后调用align API
						}

						// 系统fullreset复位反馈(设备恢复初始设置)
						else if(feedBackStatus.equals("APPLAY")){
							// 向前端推送复位完成消息
							logger.info("Reset completed for device: {}", deviceId);
							webSocketController
									.sendFullResetCompleteMessage(deviceId);
							logger.info("Sent reset complete message to frontend");
							
							// 发送 确认指令 给设备，确认复位完成
							feedBackConfirmString	= String.format("{\"ID\":\"%s\",\"CMD\":\"cmd\",\"cmd\":\"confirm\"}", deviceId);
							messageSender.sendMessage(mqttProperties.commandTopic, feedBackConfirmString, 1);
						} 
						// 系统reset复位反馈(蓝牙初始化)
						else if(feedBackStatus.equals("RESET")){
							// 向前端推送复位完成消息
							logger.info("Reset completed for device: {}", deviceId);
							webSocketController
									.sendResetCompleteMessage(deviceId);
							logger.info("Sent reset complete message to frontend");
						}
						else {
							logger.error("Invalid feedback status: {}, payload='{}'", feedBackStatus, payload);
							return; // 配对失败时不继续处理
						}
                                        }

                                } catch (Exception e) {
                                        logger.error("Error processing feedback message", e);
                                }

                        } else if (topic.equals(mqttProperties.statusTopic)) {
                                // 处理状态消息
                                // 这里可以添加额外的状态消息处理逻辑
                        }
                        // 调用消息处理器处理消息
                        mqttDataParse.personnel_status_analysis(topic, payload);
                } catch (Exception e) {
                        logger.error("Error processing MQTT message", e);
                }
        }

	/**
	 * 消息发送完成回调
	 * 
	 * @param token 消息发送令牌
	 */
	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// 订阅模式下不需要处理
	}
}
