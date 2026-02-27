package tech.xuexinglab.seamless_attendance.service;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import tech.xuexinglab.seamless_attendance.configuration.MqttProperties;

/**
 * MqttMessageSender - MQTT消息发送服务
 * 功能：
 * 1. 负责连接到MQTT broker
 * 2. 发送MQTT消息到指定主题
 * 3. 处理消息发送结果
 */
@Service
public class MqttMessageSender {
	private static final Logger logger = LoggerFactory.getLogger(MqttMessageSender.class);

	// MQTT配置信息
	private final MqttProperties mqttProperties;
	// MQTT客户端实例
	private MqttClient client;
	// MQTT持久化方式（内存持久化）
	private final MemoryPersistence persistence = new MemoryPersistence();

	/**
	 * 构造方法
	 * @param mqttProperties MQTT配置信息
	 */
	public MqttMessageSender(MqttProperties mqttProperties) {
		this.mqttProperties = mqttProperties;
	}

	/**
	 * 连接到MQTT broker
	 * @throws MqttException MQTT异常
	 */
	private synchronized void connect() throws MqttException {
		if (client != null && client.isConnected()) {
			return;
		}

		logger.info("Connecting to MQTT broker for sending messages: {}", mqttProperties.getBroker());
		// 创建MQTT客户端实例，使用不同的客户端ID避免冲突
		String clientId = mqttProperties.getClientId() + "_sender";
		client = new MqttClient(mqttProperties.getBroker(), clientId, persistence);

		// 配置MQTT连接选项
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);
		connOpts.setAutomaticReconnect(true);
		connOpts.setConnectionTimeout(30);
		connOpts.setKeepAliveInterval(60);
		
		// 如果配置了用户名密码，则设置
		if (mqttProperties.getUsername() != null && !mqttProperties.getUsername().isEmpty()) {
			connOpts.setUserName(mqttProperties.getUsername());
			connOpts.setPassword(mqttProperties.getPassword() != null ? mqttProperties.getPassword().toCharArray() : new char[0]);
		}

		// 设置MQTT回调
		client.setCallback(new MqttCallback() {
			@Override
			public void connectionLost(Throwable cause) {
				logger.warn("MQTT connection lost for sender", cause);
			}

			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				// 发送模式下不需要处理
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
				logger.info("Message delivery completed: {}", token.getMessageId());
			}
		});

		// 连接到MQTT broker
		client.connect(connOpts);
		logger.info("Successfully connected to MQTT broker for sending messages");
	}

	/**
	 * 发送MQTT消息
	 * @param topic 消息主题
	 * @param payload 消息内容
	 * @param qos 服务质量等级 (0, 1, 2)
	 * @throws MqttException MQTT异常
	 */
	public void sendMessage(String topic, String payload, int qos) throws MqttException {
		// 确保客户端已连接
		connect();

		// 创建MQTT消息
		MqttMessage message = new MqttMessage(payload.getBytes());
		message.setQos(qos);
		message.setRetained(false);

		// 发送消息
		logger.info("Sending MQTT message. topic={}, payload={}, qos={}", topic, payload, qos);
		client.publish(topic, message);
		logger.info("MQTT message sent successfully");
	}

	/**
	 * 发送MQTT消息（使用默认QoS）
	 * @param topic 消息主题
	 * @param payload 消息内容
	 * @throws MqttException MQTT异常
	 */
	public void sendMessage(String topic, String payload) throws MqttException {
		sendMessage(topic, payload, mqttProperties.getQos());
	}

	/**
	 * 断开连接
	 */
	public void disconnect() {
		if (client != null && client.isConnected()) {
			try {
				client.disconnect();
				client.close();
				logger.info("Disconnected from MQTT broker");
			} catch (MqttException e) {
				logger.warn("Error while disconnecting MQTT client", e);
			}
		}
	}
}
