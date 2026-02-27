package tech.xuexinglab.seamless_attendance.service.interfaces;


public interface MqttMessageService {
	/**
	 * 处理接收到的 MQTT 消息。
	 *
	 * @param topic   消息主题
	 * @param payload 消息内容（字符串）
	 */

        void personnel_status_analysis(String topic, String payload);
}
