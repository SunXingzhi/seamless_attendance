import api from './api'

export const statisticsService = {
	// 获取统计数据
	getStatistics: (params) => {
	return api.get('/statistics', { params })
	},
	
	// 获取活跃度统计
	getActivityStatistics: (params) => {
	return api.get('/statistics/activity', { params })
	},
	
	// 获取趋势分析
	getTrendAnalysis: (params) => {
	return api.get('/statistics/trend', { params })
	},
	
	// 获取出勤率统计
	getAttendanceRate: (params) => {
	return api.get('/statistics/attendance-rate', { params })
	},
	
	// 获取满勤次数
	getFullAttendanceCount: (params) => {
	return api.get('/statistics/full-attendance', { params })
	},
	
	// 生成统计报告
	generateReport: (params) => {
	return api.post('/statistics/report', params)
	}
}