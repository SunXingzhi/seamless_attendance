package tech.xuexinglab.seamless_attendance.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tech.xuexinglab.seamless_attendance.configuration.MqttProperties;
import tech.xuexinglab.seamless_attendance.controller.WebSocketController;
import tech.xuexinglab.seamless_attendance.service.interfaces.MqttMessageService;
import tech.xuexinglab.seamless_attendance.service.implement.MqttMessageHandlerService;

/**
 * MqttDatabaseService - MQTT消息接收服务
 * 功能：
 * 1. 负责连接到MQTT broker
 * 2. 订阅指定主题的消息
 * 3. 接收并转发MQTT消息到消息处理器
 * 4. 处理连接断开后的自动重连
 */
@Service
public class MqttDatabaseService {
	private static final Logger logger = LoggerFactory.getLogger(MqttDatabaseService.class);

	// MQTT配置信息
	private final MqttProperties mqttProperties;

	// MQTT持久化方式（内存持久化）
	private final MemoryPersistence persistence = new MemoryPersistence();
	// MQTT客户端实例
	private MqttClient client;
	// 服务运行状态
	private volatile boolean running = false;

	// 消息处理器，负责处理接收到的MQTT消息
	private final MqttMessageService messageHandler;

	// 硬件消息处理器，负责处理硬件推送的状态消息
	@Autowired
	private MqttMessageHandlerService hardwareMessageHandler;

	@Autowired
	private MqttMessageSender messageSender;
	
	@Autowired
	private WebSocketController webSocketController;
	/**
	 * 构造方法
	 * @param messageHandler 消息处理器，处理接收到的MQTT消息
	 * @param mqttProperties MQTT配置信息
	 */
	public MqttDatabaseService(MqttMessageService messageHandler, MqttProperties mqttProperties) {
		this.messageHandler = messageHandler;
		this.mqttProperties = mqttProperties;
	}

	/**
	 * 服务启动方法
	 * 在Spring容器初始化时自动调用
	 */
	@PostConstruct
	public void start() {
		running = true;
		connectAndSubscribe();
	}

	/**
	 * 服务停止方法
	 * 在Spring容器销毁时自动调用
	 */
	@PreDestroy
	public void stop() {
		running = false;
		if (client != null && client.isConnected()) {
			try {
				client.disconnect();
				client.close();
			} catch (MqttException e) {
				logger.warn("Error while disconnecting MQTT client", e);
			}
		}
	}

	/**
	 * 连接到MQTT broker并订阅主题
	 * 线程安全方法
	 */
	private synchronized void connectAndSubscribe() {
		if (!running) return;

		try {
			if (client != null && client.isConnected()) {
				return;
			}

			logger.info("Connecting to MQTT broker {} with clientId={}", mqttProperties.getBroker(), mqttProperties.getClientId());
			// 创建MQTT客户端实例
			client = new MqttClient(mqttProperties.getBroker(), mqttProperties.getClientId(), persistence);

			// 配置MQTT连接选项
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true); // 清除会话
			connOpts.setAutomaticReconnect(true); // 自动重连
			connOpts.setConnectionTimeout(30); // 连接超时时间（秒）
			connOpts.setKeepAliveInterval(60); // 心跳间隔（秒）
			
			// 如果配置了用户名密码，则设置
			if (mqttProperties.getUsername() != null && !mqttProperties.getUsername().isEmpty()) {
				connOpts.setUserName(mqttProperties.getUsername());
				connOpts.setPassword(mqttProperties.getPassword() != null ? mqttProperties.getPassword().toCharArray() : new char[0]);
			}

			// 设置MQTT回调
			client.setCallback(new MqttCallback() {

				/**
				 * 连接丢失回调
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
				 * @param topic 消息主题
				 * @param message 消息内容
				 * @throws Exception 处理异常
				 */
				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					String payload = new String(message.getPayload(), "UTF-8");
					logger.info("Received message. topic={}, payload={}", topic, payload);
					try {
						// 根据不同主题解析
						if(topic.equals(mqttProperties.subscribeTopic)){
							// 处理状态更新消息
							String deviceId = null; // 默认设备ID
							// 解析payload获取设备ID
							if (payload != null && !payload.isEmpty() && payload.contains("ID")) {
								int startIndex = payload.indexOf("ID") + 4;
								int endIndex = payload.indexOf(",", startIndex);
								if (endIndex == -1) endIndex = payload.indexOf("}", startIndex);
								if (startIndex > 3 && endIndex > startIndex) {
									deviceId = payload.substring(startIndex, endIndex).replaceAll("\\\"", "").trim();
									logger.info("Parsed device ID: {}", deviceId);
								}
							}
							// 调用硬件消息处理器处理状态消息
							if (hardwareMessageHandler != null) {
								hardwareMessageHandler.handleStatusMessage(deviceId, payload);
							} else {
								logger.error("Hardware message handler is not initialized");
							}
						}
						else if(topic.equals(mqttProperties.feedbackTopic)){
							// 处理反馈消息
							// 当接收到feedbackTopic的消息时，向前端推送配对完成消息
							try {
								// 解析payload获取设备ID
								// 反馈消息格式: {"ID":"A1","status":"OK"}
								String deviceId = "A1"; // 默认设备ID
								logger.info("Processing feedback message. Payload: {}", payload);
								if (payload != null && !payload.isEmpty() && payload.contains("ID")) {
									int startIndex = payload.indexOf("ID") + 4;
									int endIndex = payload.indexOf(",", startIndex);
									if (endIndex == -1) endIndex = payload.indexOf("}", startIndex);
									if (startIndex > 3 && endIndex > startIndex) {
										deviceId = payload.substring(startIndex, endIndex).replaceAll("\\\"", "").trim();
										logger.info("Parsed device ID: {}", deviceId);
									}
								}
								 
								// 向前端推送配对完成消息
								logger.info("Pairing completed for device: {}", deviceId);
								webSocketController.sendPairingCompleteMessage(deviceId);
								logger.info("Sent pairing complete message to frontend");
								// 这里不需要发送MQTT指令，由前端在接收到通知后调用align API
							} catch (Exception e) {
								logger.error("Error processing feedback message", e);
							}

						}
						else if(topic.equals(mqttProperties.statusTopic)){
							// 处理状态消息
							// 这里可以添加额外的状态消息处理逻辑
						}
						// 调用消息处理器处理消息
						messageHandler.personnel_status_analysis(topic, payload);
					} catch (Exception e) {
						logger.error("Error processing MQTT message", e);
					}
				}

				/**
				 * 消息发送完成回调
				 * @param token 消息发送令牌
				 */
				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {
					// 订阅模式下不需要处理
				}
			});

			// 连接到MQTT broker
			client.connect(connOpts);
			// 订阅多个主题
			String[] topics = {
					mqttProperties.getSubscribeTopic(),  // ESP32C3/status1
					mqttProperties.feedbackTopic,       // ESP32C3/feedback
					mqttProperties.statusTopic          // ESP32C3/status1 (重复，但保持一致性)
			};
			int[] qos = {
					mqttProperties.getQos(),
					mqttProperties.getQos(),
					mqttProperties.getQos()
			};
			client.subscribe(topics, qos);

			logger.info("Successfully connected and subscribed to topics: {}", String.join(", ", topics));

		} catch (MqttException e) {
			logger.error("Failed to connect/subscribe to MQTT broker", e);
			if (running) {
				// 启动重连线程
				new Thread(() -> reconnectWithBackoff()).start();
			}
		}
	}

	/**
	 * 带退避策略的重连方法
	 * 重连间隔随尝试次数指数增加，最大60秒
	 */
	private void reconnectWithBackoff() {
		int attempt_times = 0;
		while (running && (client == null || !client.isConnected())) {
						attempt_times++;
			// 计算重连等待时间（指数退避）
			int waitSec = Math.min(60, (1 << Math.min(attempt_times, 6)));
			logger.info("Attempting MQTT reconnect, attempt={}, waiting {}s", attempt_times, waitSec);
			try {
				Thread.sleep(waitSec * 1000L);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			try {
				// 尝试重新连接
				connectAndSubscribe();
			} catch (Exception e) {
				logger.warn("Reconnect attempt {} failed", attempt_times, e);
			}
		}
	}

}