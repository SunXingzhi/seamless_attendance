import { defineStore } from 'pinia'
import { authService } from '../services/auth'

export const useAuthStore = defineStore('auth', {
	state: () => ({
		user: null,
		token: localStorage.getItem('token') || null,
		isAuthenticated: false,
		loading: false,
		error: null
	}),

	getters: {
		isLoggedIn: (state) => !!state.user,
		userRole: (state) => state.user?.role || null,
		userStudioId: (state) => state.user?.studioId || null,
		isSuperAdmin: (state) => state.user?.role === 'super_admin',
		isStudioAdmin: (state) => state.user?.role === 'studio_admin',
		isStudioMember: (state) => state.user?.role === 'studio_member'
	},

	actions: {
		async login(credentials) {
			this.loading = true
			this.error = null
			
			try {
				const response = await authService.login(credentials)
				
				let data = response
				
				if (data.code && data.code !== 200) {
					throw new Error(data.msg || data.message || '登录失败')
				}
				
				// 检查是否有data字段，如果有则使用data.data
				const userData = data.data || data
				
				this.user = {
					id: userData.id,
					name: userData.name,
					username: userData.username,
					role: userData.role,
					studioId: userData.studioId,
					studioAdminId: userData.studioAdminId
				}
				
				this.isAuthenticated = true
				localStorage.setItem('user', JSON.stringify(this.user))
			} catch (error) {
				this.error = error.response?.data?.message || error.message || '登录失败'
				this.isAuthenticated = false
				this.user = null
				this.token = null
				localStorage.removeItem('token')
				localStorage.removeItem('user')
				throw new Error(this.error)
			} finally {
				this.loading = false
			}
		},

		async logout() {
			try {
				await authService.logout()
			} catch (error) {
				console.error('登出失败:', error)
			} finally {
				this.user = null
				this.token = null
				this.isAuthenticated = false
				localStorage.removeItem('token')
				localStorage.removeItem('user')
			}
		},

		initializeAuth() {
			const userStr = localStorage.getItem('user')
			if (userStr) {
				try {
					this.user = JSON.parse(userStr)
					this.isAuthenticated = true
				} catch (e) {
					localStorage.removeItem('user')
				}
			}
		}
	}
})