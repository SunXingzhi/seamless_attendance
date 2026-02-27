import api from './api'

const mqttService = {
	// 开启设备广播（restart）
	async broadcastRestart(deviceId) {
		try {
			const response = await api.post('/mqtt/broadcast', {
				device_name: deviceId,
				command: 'restart'
			})
			return response
		} catch (error) {
			console.error('开启广播失败:', error)
			throw error
		}
	},

	// 向设备发送分配人员 ID 信息
	async alignPerson(deviceId, userId, personIndex, personInfo) {
		try {
                        const response = await api.post(`/mqtt/assign/${personIndex}`, {
				device_id: deviceId,
				person_index: personIndex,
				device_personnel_number: personIndex,
				user_number: personInfo.userNumber,
				user_name: personInfo.name
			})
			return response
		} catch (error) {
			console.error('分配人员 ID 失败:', error)
			throw error
		}
	},

	// 发送配对完成消息
	async sendPairingComplete(deviceId, userId) {
		try {
			const response = await api.post('/mqtt/complete', {
				device_id: deviceId,
				user_id: userId
			})
			return response
		} catch (error) {
			console.error('发送配对完成消息失败:', error)
			throw error
		}
	},

	// 向设备分配人员
	async assignPerson(deviceId, deviceName, devicePersonnelNumber, personInfo) {
		try {
                        console.log('向设备分配人员:', deviceId, deviceName, devicePersonnelNumber, personInfo)
			const response = await api.post(`/mqtt/assign/${devicePersonnelNumber}`, {
				device_id: deviceId,
				device_name: deviceName,
				device_personnel_number: devicePersonnelNumber,
				user_number: personInfo.userNumber,
				user_name: personInfo.name
			})
			return response
		} catch (error) {
			console.error('分配人员失败:', error)
			throw error
		}
	},

	// 解除人员与设备的配对
	async unassignPerson(deviceId, userNumber) {
		try {
			console.log('解除人员与设备的配对:', deviceId, userNumber)
			const response = await api.post('/mqtt/unassign', {
				device_id: deviceId,
				user_number: userNumber
			})
			return response
		} catch (error) {
			console.error('解除人员配对失败:', error)
			throw error
		}
	}
}

export default mqttService
