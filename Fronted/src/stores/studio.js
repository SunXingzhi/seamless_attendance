import { defineStore } from 'pinia'
import { studioService } from '../services/studio'

export const useStudioStore = defineStore('studio', {
	state: () => ({
		studioList: [],
		loading: false,
		error: null
	}),

	// 计算方法
	getters: {
		getStudioList: (state) => state.studioList,
		getStudioById: (state) => (id) => {
			return state.studioList.find(studio => studio.id === id)
		},
		getActiveCount: (state) => {
			return state.studioList.reduce((total, studio) => total + (studio.presentCount || 0), 0)
		}
	},

	// stores api
	actions: {
		async fetchStudioList() {
			this.loading = true
			this.error = null
			try {
				const response = await studioService.getStudioList()
				console.log('工作室列表API响应:', response)
				
				let rawData = []
				if (Array.isArray(response)) {
					this.studioList = response.map(item => this.transformStudioData(item))
				} else if (response && Array.isArray(response.data)) {
					rawData = response.data
					this.studioList = rawData.map(item => this.transformStudioData(item))
				} else if (response && Array.isArray(response.list)) {
					rawData = response.list
					this.studioList = rawData.map(item => this.transformStudioData(item))
				} else {
					console.warn('工作室列表响应数据格式不正确，使用空数组:', response)
					this.studioList = []
					return
				}
			} catch (error) {
				this.error = error.message
				console.error('获取工作室列表失败:', error)
				this.studioList = []
			} finally {
				this.loading = false
			}
		},
		
		// 转换工作室数据格式
		transformStudioData(data) {
			return {
				id: data.id,
				name: data.studio_name || data.name,
				code: data.studio_code || data.code,
				description: data.description || '',
				personnels: data.personnels || data.personnel || '',
				presentCount: data.member_count || data.presentCount || 0,
				totalCount: data.max_member_count || data.totalCount || 0,
				adminName: data.admin_name || data.adminName,
				adminUserNumber: data.admin_user_number || data.adminUserNumber,
				adminContact: data.admin_contact || data.adminContact,
				isNew: false,
				absentPersons: data.absent_persons || data.absentPersons || [],
				presentPersons: data.present_persons || data.presentPersons || []
			}
		},
		
		// 获取工作室考勤信息
		async fetchStudioAttendance(id) {
			try {
				const response = await studioService.getStudioAttendance(id)
				console.log(`工作室 ${id} 考勤信息API响应:`, response)
				
				const index = this.studioList.findIndex(studio => studio.id === id)
				if (index !== -1) {
					const attendanceData = response.data || response
					this.studioList[index].absentPersons = attendanceData.absent_persons || attendanceData.absentPersons || []
					this.studioList[index].presentPersons = attendanceData.present_persons || attendanceData.presentPersons || []
					this.studioList[index].presentCount = attendanceData.present_count || attendanceData.presentCount || 0
					this.studioList[index].totalCount = attendanceData.total_count || attendanceData.totalCount || 0
				}
				return response
			} catch (error) {
				console.error('获取工作室考勤信息失败:', error)
				throw error
			}
		},
		
		// 添加工作室
		async addStudio(studio) {
			try {
				const backendData = this.transformToBackendFormat(studio)
				const response = await studioService.createStudio(backendData)
				const newStudio = this.transformStudioData(response.data || response)
				this.studioList.push(newStudio)
				return newStudio
			} catch (error) {
				console.error('添加工作室失败:', error)
				throw error
			}
		},
		
		// 更新工作室信息
		async updateStudio(id, updates) {
			try {
				const backendData = this.transformToBackendFormat(updates)
				const response = await studioService.updateStudio(id, backendData)
				const index = this.studioList.findIndex(studio => studio.id === id)
				if (index !== -1) {
					const currentStudio = this.studioList[index]
					// 如果后端返回了完整数据，则使用后端数据
					if (response.data && response.data.id) {
						const updatedStudio = this.transformStudioData(response.data)
						this.studioList[index] = {
							...currentStudio,
							...updatedStudio,
							id: id
						}
					} else {
						// 如果后端没有返回完整数据，只更新前端提交的字段
						this.studioList[index] = {
							...currentStudio,
							...updates
						}
					}
				}
				return this.studioList[index]
			} catch (error) {
				console.error('更新工作室失败:', error)
				throw error
			}
		},
		
		// 删除工作室
		async deleteStudio(id) {
			try {
				await studioService.deleteStudio(id)
				this.studioList = this.studioList.filter(studio => studio.id !== id)
			} catch (error) {
				console.error('删除工作室失败:', error)
				throw error
			}
		},
		
		// 转换为后端数据格式
		transformToBackendFormat(data) {
                        console.log('转换depersonnel数据:'+ data.personnels)
			return {
				studio_name: data.studio_name || data.name || '',
				studio_code: data.studio_code || data.code || '',
				description: data.description || '',
				personnels: data.personnels || data.personnel || '',
				admin_name: data.admin_name || '',
				admin_user_number: data.admin_user_number || ''
			}
		},
	}
})
