import api from './api'

// 工作室管理服务
export const studioService = {
	// 获取工作室列表
	async getStudioList() {
		try {
			const response = await api.get('/studios/all')
			return response.data || response
		} catch (error) {
			console.error('获取工作室列表失败:', error)
			throw error
		}
	},

	// 获取工作室详情
	async getStudioById(id) {
		try {
			const response = await api.get(`/studios/${id}`)
			return response.data || response
		} catch (error) {
			console.error(`获取工作室 ${id} 详情失败:`, error)
			throw error
		}
	},

	// 获取工作室考勤信息
	async getStudioAttendance(id) {
		try {
			const response = await api.get(`/studios/${id}/attendance`)
			return response.data || response
		} catch (error) {
			console.error(`获取工作室 ${id} 考勤信息失败:`, error)
			throw error
		}
	},

	// 创建工作室
	async createStudio(studioData) {
		try {
			const response = await api.post('/studios', studioData)
			return response.data || response
		} catch (error) {
			console.error('创建工作室失败:', error)
			throw error
		}
	},

	// 更新工作室
	async updateStudio(id, studioData) {
		try {
			const response = await api.put(`/studios/${id}`, studioData)
			return response.data || response
		} catch (error) {
			console.error(`更新工作室 ${id} 失败:`, error)
			throw error
		}
	},

	// 删除工作室
	async deleteStudio(id) {
		try {
			const response = await api.delete(`/studios/${id}`)
			return response.data || response
		} catch (error) {
			console.error(`删除工作室 ${id} 失败:`, error)
			throw error
		}
	}
}
