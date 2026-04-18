// 导入STOMP协议所需的库
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

// WebSocket服务（基于STOMP协议）
class WebSocketService {
	constructor() {
		this.stompClient = null
		this.listeners = {}
		this.reconnectAttempts = 0
		this.maxReconnectAttempts = 5
		this.reconnectDelay = 1000
	}

	// 连接WebSocket
	connect(url = '/seamless_attendance/ws') {
		return new Promise((resolve, reject) => {
			try {
				// 创建SockJS连接
				const socket = new SockJS(url)
				// 创建STOMP客户端
				this.stompClient = Stomp.over(socket)

				// 配置STOMP客户端
				this.stompClient.debug = (str) => {
					// 仅在开发环境打印调试信息
					if (import.meta.env.DEV) {
						console.log('STOMP调试:', str)
					}
				}

				// 连接成功回调
				this.stompClient.connect({}, () => {
					console.log('WebSocket连接已建立')
					this.reconnectAttempts = 0
					
					// 触发连接成功事件
					if (this.listeners['connect']) {
						this.listeners['connect'].forEach(callback => {
							try {
								callback()
							} catch (error) {
								console.error('执行连接成功回调失败:', error)
							}
						})
					}
					
					// 订阅后端消息
					this.subscribeToMessages()
					resolve()
				}, (error) => {
					console.error('WebSocket连接失败:', error)
					reject(error)
					this.attemptReconnect(url)
				})

				// 连接关闭回调
				this.stompClient.ws.onclose = () => {
					console.log('WebSocket连接已关闭')
					this.attemptReconnect(url)
				}

			} catch (error) {
				console.error('WebSocket连接失败:', error)
				reject(error)
			}
		})
	}

	// 订阅消息
	subscribeToMessages() {
		// 订阅配对消息
		this.stompClient.subscribe('/topic/pairing', (message) => {
			try {
				const parsedMessage = JSON.parse(message.body)
				console.log('【WebSocket服务】接收到配对消息:', parsedMessage)
				console.log('【WebSocket服务】当前监听器列表:', Object.keys(this.listeners))
				// 直接触发监听器，传递配对结果
				if (this.listeners['pairing']) {
					console.log('【WebSocket服务】找到pairing监听器，数量:', this.listeners['pairing'].length)
					this.listeners['pairing'].forEach(callback => {
						try {
							callback(parsedMessage)
						} catch (error) {
							console.error('执行配对消息回调失败:', error)
						}
					})
				} else {
					console.log('【WebSocket服务】没有找到pairing监听器')
				}
			} catch (error) {
				console.error('解析配对消息失败:', error)
			}
		})
		
		// 订阅状态更新消息
		this.stompClient.subscribe('/topic/status', (message) => {
			try {
				const parsedMessage = JSON.parse(message.body)
				console.log('【WebSocket服务】接收到状态更新消息:', parsedMessage)
				// 直接触发监听器，传递状态更新结果
				if (this.listeners['status']) {
					this.listeners['status'].forEach(callback => {
						try {
							callback(parsedMessage)
						} catch (error) {
							console.error('执行状态更新消息回调失败:', error)
						}
					})
				}
			} catch (error) {
				console.error('解析状态更新消息失败:', error)
			}
		})
	}

	// 尝试重连
	attemptReconnect(url) {
		if (this.reconnectAttempts < this.maxReconnectAttempts) {
			this.reconnectAttempts++
			console.log(`尝试重连WebSocket (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)
			setTimeout(() => {
				this.connect(url)
			}, this.reconnectDelay * this.reconnectAttempts)
		} else {
			console.error('WebSocket重连失败，已达到最大尝试次数')
			// 触发重连失败事件
			if (this.listeners['reconnectFailed']) {
				this.listeners['reconnectFailed'].forEach(callback => {
					try {
						callback()
					} catch (error) {
						console.error('执行重连失败回调失败:', error)
					}
				})
			}
		}
	}

	// 注册消息监听器
	on(event, callback) {
		if (!this.listeners[event]) {
			this.listeners[event] = []
		}
		this.listeners[event].push(callback)
	}

	// 移除消息监听器
	off(event, callback) {
		if (this.listeners[event]) {
			this.listeners[event] = this.listeners[event].filter(cb => cb !== callback)
		}
	}

	// 发送消息
	send(destination, message) {
		if (this.stompClient && this.stompClient.connected) {
			this.stompClient.send(destination, {}, JSON.stringify(message))
			return true
		} else {
			console.error('WebSocket未连接，无法发送消息')
			return false
		}
	}

	// 关闭连接
	close() {
		if (this.stompClient) {
			this.stompClient.disconnect()
			this.stompClient = null
		}
		this.listeners = {}
	}

	// 检查连接状态
	isConnected() {
		return this.stompClient && this.stompClient.connected
	}
}

// 导出单例实例
const webSocketService = new WebSocketService()
export default webSocketService
