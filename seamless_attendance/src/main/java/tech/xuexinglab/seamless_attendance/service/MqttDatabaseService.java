package tech.xuexinglab.seamless_attendance.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tech.xuexinglab.seamless_attendance.configuration.MqttProperties;
import tech.xuexinglab.seamless_attendance.controller.WebSocketController;
// import tech.xuexinglab.seamless_attendance.service.interfaces.MqttMessageService;	 已经废弃
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

	// 消息处理器，负责处理接收到的MQTT消息 已经被废弃
	// private final MqttMessageService messageHandler;
	private mqttDataParse mqttDataParse;
	// 硬件消息处理器，负责处理硬件推送的状态消息
	@Autowired
	private MqttMessageHandlerService hardwareMessageHandler;

	@Autowired
	private MqttMessageSender messageSender;

	@Autowired
	private WebSocketController webSocketController;

	@Autowired
	private MqttPushCallback mqttPushCallback;

	/**
	 * 构造方法
	 * 
	 * @param messageHandler 消息处理器，处理接收到的MQTT消息
	 * @param mqttProperties MQTT配置信息
	 */
	public MqttDatabaseService(mqttDataParse mqttDataParse, MqttProperties mqttProperties) {
		this.mqttDataParse = mqttDataParse;
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
		if (!running)
			return;

		try {
			if (client != null && client.isConnected()) {
				return;
			}

			logger.info("Connecting to MQTT broker {} with clientId={}", mqttProperties.getBroker(),
					mqttProperties.getClientId());
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
				connOpts.setPassword(mqttProperties.getPassword() != null
						? mqttProperties.getPassword().toCharArray()
						: new char[0]);
			}

			// 使用注入的mqttPushCallback作为MQTT回调
			client.setCallback(mqttPushCallback);

			// 连接到MQTT broker
			client.connect(connOpts);
			// 订阅多个主题
			String[] topics = {
					mqttProperties.getSubscribeTopic(), // ESP32C3/status1
					mqttProperties.feedbackTopic, // ESP32C3/feedback
					mqttProperties.statusTopic // ESP32C3/status1 (重复，但保持一致性)
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