package tech.xuexinglab.seamless_attendance.service;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class PushCallback implements MqttCallback {

	@Override
	public void connectionLost(Throwable cause) {
		System.out.println("Connection lost: " + (cause != null ? cause.getMessage() : "unknown"));
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.println("Message arrived. Topic: " + topic + "  Message: "
				+ new String(message.getPayload(), "UTF-8"));
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		System.out.println("Delivery complete");
	}
}