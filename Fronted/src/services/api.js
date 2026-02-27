import axios from 'axios'

const api = axios.create({
	baseURL: '/seamless_attendance/api',
	timeout: 10000,
	headers: {
		'Content-Type': 'application/json'
	}
})

api.interceptors.request.use(
	config => {
		const token = localStorage.getItem('token')
		if (token) {
			config.headers.Authorization = `Bearer ${token}`
		}
		return config
	},
	error => {
		return Promise.reject(error)
	}
)

api.interceptors.response.use(
	response => {
		return response.data
	},
	error => {
		console.error('API Error:', error)
		return Promise.reject(error)
	}
)

export default api
