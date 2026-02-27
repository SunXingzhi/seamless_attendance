package tech.xuexinglab.seamless_attendance.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {
	private String broker = "tcp://47.108.57.12:1883";
	private String clientId = "seamless_attendance_dev";
	public String subscribeTopic = "ESP32C3/status";
        public String commandTopic      = "ESP32C3/recognition";
        public String statusTopic       = "ESP32C3/status";
        public String feedbackTopic     = "ESP32C3/feedback";
	private String username;
	private String password;
	public int qos = 0;

	public String getBroker() {
		return broker;
	}

	public void setBroker(String broker) {
		this.broker = broker;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getSubscribeTopic() {
		return subscribeTopic;
	}

	public void setSubscribeTopic(String subscribeTopic) {
		this.subscribeTopic = subscribeTopic;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getQos() {
		return qos;
	}

	public void setQos(int qos) {
		this.qos = qos;
	}
}