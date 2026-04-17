import { defineStore } from 'pinia'
import { deviceService } from '../services/device'

export const useDeviceStore = defineStore('device', {
	state: () => ({
		deviceList: [],
		loading: false,
		error: null
	}),

	// 计算方法
	getters: {
		getDeviceList: (state) => state.deviceList,
		getDeviceById: (state) => (id) => {
			return state.deviceList.find(device => device.id === id)
		},
		getActiveCount: (state) => {
			return state.deviceList.filter(device => device.activated && device.status === 'active').length
		},
		getInactiveCount: (state) => {
			return state.deviceList.filter(device => !device.activated).length
		},
		getabsentCount: (state) => {
			return state.deviceList.filter(device => device.activated && device.status === 'absent').length
		}
	},

	// stores api
	actions: {
		async fetchDeviceList() {
			this.loading = true
			this.error = null
			try {
				const response = await deviceService.getDeviceList()
				console.log('设备列表API响应:', response)
				
				let rawData = []
				if (Array.isArray(response)) {
					this.deviceList = response.map(item => this.transformDeviceData(item))
				} else if (response && Array.isArray(response.data)) {
					rawData = response.data
					this.deviceList = rawData.map(item => this.transformDeviceData(item))
				} else if (response && Array.isArray(response.list)) {
					rawData = response.list
					this.deviceList = rawData.map(item => this.transformDeviceData(item))
				} else {
					console.warn('设备列表响应数据格式不正确，使用空数组:', response)
					this.deviceList = []
					return
				}
			} catch (error) {
				this.error = error.message
				console.error('获取设备列表失败:', error)
				this.deviceList = []
			} finally {
				this.loading = false
			}
		},
		
		// 转换设备数据格式
		transformDeviceData(data) {
			const deviceId = data.id || Date.now()
			return {
				id: deviceId,
				deviceName: data.deviceName || data.name || '未命名设备',
				// deviceId字段已废弃，使用id作为设备主键
				// 保留deviceId字段用于兼容，但设置为与id相同
				deviceId: deviceId,
				ipAddress: data.ip_address || data.ipAddress || '-',
				deviceType: data.device_type || data.type || '其他',
				status: data.status === 'activated' ? 'active' : 'absent',
				activated: data.status === 'activated',
				studioCode: data.studio_code || data.studioId || '-',
				personnels: data.personnels || '',
				lastConnection: data.update_time || data.last_active_time || new Date().toLocaleString(),
				createTime: data.create_time || data.createTime || new Date().toLocaleString(),
				updateTime: data.update_time || data.updateTime || new Date().toLocaleString()
			}
		},
		
		// 添加设备
		async addDevice(device) {
			try {
				console.log('添加设备前的数据:', device)
				const backendData = this.transformToBackendFormat(device)
				console.log('转换后发送到后端的数据:', backendData)
				const response = await deviceService.addDevice(backendData)
				console.log('后端返回的数据:', response)
				const newDevice = this.transformDeviceData(response.data || response)
				console.log('转换后添加到列表的设备:', newDevice)
				this.deviceList.push(newDevice)
				return newDevice
			} catch (error) {
				console.error('添加设备失败:', error)
				throw error
			}
		},
		
		// 更新设备信息
		async updateDevice(id, updates) {
			try {
				const backendData = this.transformToBackendFormat(updates)
				const response = await deviceService.updateDevice(id, backendData)
				const index = this.deviceList.findIndex(device => device.id === id)
				if (index !== -1) {
					const currentDevice = this.deviceList[index]
					// 如果后端返回了完整数据，则使用后端数据
					if (response.data && response.data.id) {
						const updatedDevice = this.transformDeviceData(response.data)
						this.deviceList[index] = {
							...currentDevice,
							...updatedDevice,
							id: id
						}
					} else {
						// 如果后端没有返回完整数据，只更新前端提交的字段
						this.deviceList[index] = {
							...currentDevice,
							...updates
						}
					}
				}
				return this.deviceList[index]
			} catch (error) {
				console.error('更新设备失败:', error)
				throw error
			}
		},
		
		// 删除设备
		async deleteDevice(id) {
			try {
				await deviceService.deleteDevice(id)
				this.deviceList = this.deviceList.filter(device => device.id !== id)
			} catch (error) {
				console.error('删除设备失败:', error)
				throw error
			}
		},
		
		// 重新连接设备
		async reconnectDevice(id) {
			try {
				const response = await deviceService.reconnectDevice(id)
				const index = this.deviceList.findIndex(device => device.id === id)
				if (index !== -1) {
					this.deviceList[index].status = 'active'
					this.deviceList[index].lastConnection = new Date().toLocaleString()
				}
				return response
			} catch (error) {
				console.error('重新连接设备失败:', error)
				throw error
			}
		},
		
		// 激活设备
		async activateDevice(id) {
			try {
				const response = await deviceService.activateDevice(id)
				const index = this.deviceList.findIndex(device => device.id === id)
				if (index !== -1) {
					this.deviceList[index].activated = true
				}
				return response
			} catch (error) {
				console.error('激活设备失败:', error)
				throw error
			}
		},
		
		// 转换为后端数据格式
		transformToBackendFormat(data) {
			console.log('转换数据:', data)
			const deviceName = data.device_name || data.name || ''
			return {
				device_name: deviceName,
				// device_id字段设置为与device_name相同，保持兼容性
				device_id: deviceName,
				studio_code: data.studio_code || data.studioId || '',
				personnels: data.personnels || ''
			}
		},
	}
})
