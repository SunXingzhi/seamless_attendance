package tech.xuexinglab.seamless_attendance.controller;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocketController - WebSocket消息控制器
 * 功能：
 * 1. 发送配对完成消息到前端
 */
@Controller
public class WebSocketController {

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	/**
	 * 发送配对完成消息
	 */
	public void sendPairingCompleteMessage(String deviceId) {
		String message = "{\"success\":true, \"message\":\"配对成功\", \"deviceId\":\"" + deviceId + "\"}";
		messagingTemplate.convertAndSend("/topic/pairing", message);
	}

	/**
	 * 发送配对完成消息（兼容旧版本）
	 */
	public void sendPairingCompleteMessage() {
		sendPairingCompleteMessage("");
	}

	/**
	 * 发送消息给所有连接的客户端
	 */
	public void sendMessageToAll(String message) {
		messagingTemplate.convertAndSend("/topic/status", message);
	}

	/**
	 * 发送完全复位完成消息
	 */
	public void sendFullResetCompleteMessage(String deviceId) {
		String message = "{\"success\":true, \"message\":\"恢复初始设置成功\", \"deviceId\":\"" + deviceId + "\"}";
		messagingTemplate.convertAndSend("/topic/reset", message);
	}

	/**
	 * 发送复位完成消息(蓝牙初始化)
	 */
	public void sendResetCompleteMessage(String deviceId) {
		String message = "{\"success\":true, \"message\":\"蓝牙初始化成功\", \"deviceId\":\"" + deviceId + "\"}";
		messagingTemplate.convertAndSend("/topic/reset", message);
	}
}
