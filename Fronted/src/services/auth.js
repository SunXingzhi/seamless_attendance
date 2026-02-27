import api from './api'

export const authService = {
	login: (credentials) => {
		return api.post('/auth/login', credentials)
	},
	
	logout: () => {
		return api.post('/auth/logout')
	},
	
	getCurrentUser: () => {
		return api.get('/auth/user')
	},
	
	refreshToken: () => {
		return api.post('/auth/refresh')
	}
}
