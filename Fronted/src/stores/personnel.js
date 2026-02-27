import { defineStore } from 'pinia'
import { personnelService } from '../services/personnel'

export const usePersonnelStore = defineStore('personnel', {
	state: () => ({
		personnelList: [],
		loading: false,
		error: null
	}),

	// 计算方法
	getters: {
		getPersonnelList: (state) => state.personnelList,
		getPersonnelById: (state) => (id) => {
			return state.personnelList.find(person => person.userId === id)
		},
		getActiveCount: (state) => {
			return state.personnelList.filter(person => person.attendanceStatus === 'active').length
		},
		getLateCount: (state) => {
			return state.personnelList.filter(person => person.attendanceStatus === 'late').length
		},
		getLeaveCount: (state) => {
			return state.personnelList.filter(person => person.attendanceStatus === 'leave').length
		},
		getAbsentCount: (state) => {
			return state.personnelList.filter(person => person.attendanceStatus === 'absent').length
		},
		getHolidayCount: (state) => {
			return state.personnelList.filter(person => person.attendanceStatus === 'holiday').length
		},
		getExcusedCount: (state) => {
			return state.personnelList.filter(person => person.attendanceStatus === 'excused').length
		},
		getPairedCount: (state) => {
			return state.personnelList.filter(person => person.pairingStatus === 'paired').length
		},
		getUnpairedCount: (state) => {
			return state.personnelList.filter(person => person.pairingStatus === 'unpaired').length
		}
	},

	// stores api
	actions: {
		async fetchPersonnelList() {
			this.loading = true
			this.error = null
			try {
				const response = await personnelService.getPersonnelList()
				console.log('API 响应数据:', response)

				let rawData = []
				if (Array.isArray(response)) {
					this.personnelList = response
				} else if (response && Array.isArray(response.data)) {
					rawData = response.data
				} else if (response && Array.isArray(response.list)) {
					rawData = response.list
				} else {
					console.warn('响应数据格式不正确，使用空数组:', response)
					this.personnelList = []
					return
				}

				this.personnelList = rawData.map(item => this.transformPersonnelData(item))
			} catch (error) {
				this.error = error.message
				console.error('获取人员列表失败:', error)
				this.personnelList = []
			} finally {
				this.loading = false
			}
		},
		// 转换人员数据格式
		transformPersonnelData(data) {
			if (!data) {
				return {}
			}
			return {
			userId: data.id || data.user_id || data.userId,
			name: data.name,
			user_name: data.user_name,
			userName: data.name || data.userName || data.user_name,
			userNumber: data.user_number || data.userNumber,
			contactType: data.contact_type || data.contactType,
			contactNumber: data.contact_value || data.contactNumber,
			role: data.role,
			workTask: data.job_title || data.workTask,
			workContent: data.work_content || data.workContent,
			studioId: data.studio_id || data.studioId || '',
			avatar: data.avatar,
			attendanceStatus: data.status || data.attendanceStatus,
			pairingStatus: data.pairing_status || data.pairingStatus || 'unpaired',
			joinDate: data.join_date || data.joinDate,
			createTime: data.create_time || data.createTime,
			updateTime: data.update_time || data.updateTime
		}
		},
		// 添加人员
		async addPersonnel(person) {
			try {
				const backendData = this.transformToBackendFormat(person)
				const response = await personnelService.addPersonnel(backendData)
				console.log('添加人员响应:', response)
				// 由于 api 拦截器已经返回了 response.data，所以直接使用 response
				const newPerson = this.transformPersonnelData(response)
				// 确保新人员对象包含所有必要字段
				if (newPerson.userName) {
					this.personnelList.push(newPerson)
				} else {
					console.error('添加人员失败: 响应数据不完整', response)
					// 如果后端返回的数据不完整，使用前端提交的数据
					const fallbackPerson = {
						...person,
						userId: response.id || response.user_id || Date.now() // 使用后端返回的 ID 或生成临时 ID
					}
					this.personnelList.push(fallbackPerson)
				}
			} catch (error) {
				console.error('添加人员失败:', error)
				throw error
			}
		},
		// 更新人员信息
		async updatePersonnel(id, updates) {
			try {
				const backendData = this.transformToBackendFormat(updates)
				const response = await personnelService.updatePersonnel(id, backendData)
				console.log('更新人员响应:', response)
				const index = this.personnelList.findIndex(person => person.userId === id)
				if (index !== -1) {
					const currentPerson = this.personnelList[index]
					// 由于 api 拦截器已经返回了 response.data，所以直接使用 response
					const updatedPerson = this.transformPersonnelData(response)
					this.personnelList[index] = {
						...currentPerson,
						...updatedPerson,
						userId: id
					}
				}
			} catch (error) {
				console.error('更新人员失败:', error)
				throw error
			}
		},
		// 删除人员
		async deletePersonnel(id) {
			try {
				await personnelService.deletePersonnel(id)
				this.personnelList = this.personnelList.filter(person => person.userId !== id)
			} catch (error) {
				console.error('删除人员失败:', error)
				throw error
			}
		},
		// 更新考勤状态
		async updateAttendanceStatus(id, status) {
			try {
				await personnelService.updateAttendanceStatus(id, status)
				const index = this.personnelList.findIndex(person => person.userId === id)
				if (index !== -1) {
					this.personnelList[index].attendanceStatus = status
				}
			} catch (error) {
				console.error('更新考勤状态失败:', error)
				throw error
			}
		},
		
		// 根据userNumber更新人员状态（WebSocket消息处理）
		updatePersonnelStatus(userNumber, status) {
			const index = this.personnelList.findIndex(person => person.userNumber === userNumber)
			if (index !== -1) {
				this.personnelList[index].attendanceStatus = status
				console.log(`已更新人员 ${userNumber} 的状态为: ${status}`)
			} else {
				console.warn(`未找到用户编号为 ${userNumber} 的人员`)
			}
		},
		// 转换为后端数据格式
		transformToBackendFormat(data) {
			return {
			id: data.userId || data.id,
			name: data.userName,
			user_name: data.user_name,
			user_number: data.userNumber,
			contact_type: data.contactType,
			contact_value: data.contactNumber,
			role: data.role,
			job_title: data.workTask,
			work_content: data.workContent,
			studio_id: data.studio_id || data.studioId || '',
			avatar: data.avatar,
			status: data.attendanceStatus,
			pairing_status: data.pairingStatus,
			join_date: data.joinDate
		}
		},
	}
})