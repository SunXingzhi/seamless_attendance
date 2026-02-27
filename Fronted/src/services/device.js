import api from './api'

// 设备管理服务
export const deviceService = {
	// 获取设备列表
	async getDeviceList() {
		try {
			const response = await api.get('/devices/all')
			return response
		} catch (error) {
			console.error('获取设备列表失败:', error)
			throw error
		}
	},

	// 获取设备详情
	async getDeviceById(id) {
		try {
			const response = await api.get(`/device/detail/${id}`)
			return response.data || response
		} catch (error) {
			console.error(`获取设备 ${id} 详情失败:`, error)
			throw error
		}
	},

	// 添加设备
	async addDevice(deviceData) {
		try {
			const response = await api.post('/devices', deviceData)
			return response.data || response
		} catch (error) {
			console.error('添加设备失败:', error)
			throw error
		}
	},

	// 更新设备
	async updateDevice(id, deviceData) {
		try {
			const response = await api.put(`/device/info/${id}`, deviceData)
			return response.data || response
		} catch (error) {
			console.error(`更新设备 ${id} 失败:`, error)
			throw error
		}
	},

	// 删除设备
	async deleteDevice(id) {
		try {
			const response = await api.delete(`/devices/${id}`)
			return response.data || response
		} catch (error) {
			console.error(`删除设备 ${id} 失败:`, error)
			throw error
		}
	},

	// 重新连接设备
	async reconnectDevice(id) {
		try {
			const response = await api.post(`/devices/${id}/reconnect`)
			return response.data || response
		} catch (error) {
			console.error(`重新连接设备 ${id} 失败:`, error)
			throw error
		}
	},

	// 激活设备
	async activateDevice(id) {
		try {
			const response = await api.post(`/devices/${id}/activate`)
			return response.data || response
		} catch (error) {
			console.error(`激活设备 ${id} 失败:`, error)
			throw error
		}
	},

	// 刷新设备状态
	async refreshDeviceStatus(id) {
		try {
			const response = await api.get(`/devices/${id}/status`)
			return response.data || response
		} catch (error) {
			console.error(`刷新设备 ${id} 状态失败:`, error)
			throw error
		}
	}
}
