import api from './api'

export const statisticsService = {
	// ==================== 用户统计 ====================

	// 获取用户每日统计数据
	getUserDailyStats: (userNumber, limit = 7) => {
		return api.get(`/statistics/user/daily/${userNumber}`, { params: { limit } })
	},

	// 获取用户指定日期的每日统计
	getUserDailyStatsByDate: (userNumber, date) => {
		return api.get(`/statistics/user/daily/${userNumber}/${date}`)
	},

	// 获取用户每周统计数据
	getUserWeeklyStats: (userNumber, limit = 4) => {
		return api.get(`/statistics/user/weekly/${userNumber}`, { params: { limit } })
	},

	// 获取用户指定周的每周统计
	getUserWeeklyStatsByWeek: (userNumber, weekStartDate) => {
		return api.get(`/statistics/user/weekly/${userNumber}/${weekStartDate}`)
	},

	// 获取用户每月统计数据
	getUserMonthlyStats: (userNumber, limit = 12) => {
		return api.get(`/statistics/user/monthly/${userNumber}`, { params: { limit } })
	},

	// 获取用户指定月份的每月统计
	getUserMonthlyStatsByMonth: (userNumber, year, month) => {
		return api.get(`/statistics/user/monthly/${userNumber}/${year}/${month}`)
	},

	// 获取用户每年统计数据
	getUserYearlyStats: (userNumber, limit = 5) => {
		return api.get(`/statistics/user/yearly/${userNumber}`, { params: { limit } })
	},

	// 获取用户指定年份的每年统计
	getUserYearlyStatsByYear: (userNumber, year) => {
		return api.get(`/statistics/user/yearly/${userNumber}/${year}`)
	},

	// ==================== 工作室统计 ====================

	// 获取工作室每日统计数据
	getStudioDailyStats: (studioId, limit = 7) => {
		return api.get(`/statistics/studio/daily/${studioId}`, { params: { limit } })
	},

	// 获取工作室指定日期的每日统计
	getStudioDailyStatsByDate: (studioId, date) => {
		return api.get(`/statistics/studio/daily/${studioId}/${date}`)
	},

	// 获取工作室每周统计数据
	getStudioWeeklyStats: (studioId, limit = 4) => {
		return api.get(`/statistics/studio/weekly/${studioId}`, { params: { limit } })
	},

	// 获取工作室指定周的每周统计
	getStudioWeeklyStatsByWeek: (studioId, weekStartDate) => {
		return api.get(`/statistics/studio/weekly/${studioId}/${weekStartDate}`)
	},

	// 获取工作室每月统计数据
	getStudioMonthlyStats: (studioId, limit = 12) => {
		return api.get(`/statistics/studio/monthly/${studioId}`, { params: { limit } })
	},

	// 获取工作室指定月份的每月统计
	getStudioMonthlyStatsByMonth: (studioId, year, month) => {
		return api.get(`/statistics/studio/monthly/${studioId}/${year}/${month}`)
	},

	// 获取工作室每年统计数据
	getStudioYearlyStats: (studioId, limit = 5) => {
		return api.get(`/statistics/studio/yearly/${studioId}`, { params: { limit } })
	},

	// 获取工作室指定年份的每年统计
	getStudioYearlyStatsByYear: (studioId, year) => {
		return api.get(`/statistics/studio/yearly/${studioId}/${year}`)
	},

	// ==================== 综合统计查询 ====================

	// 获取工作室成员的每日统计数据
	getStudioUserDailyStats: (studioId, date) => {
		return api.get(`/statistics/studio/${studioId}/members/daily/${date}`)
	},

	// 获取工作室成员的月度统计数据
	getStudioUserMonthlyStats: (studioId, year, month) => {
		return api.get(`/statistics/studio/${studioId}/members/monthly/${year}/${month}`)
	},

	// ==================== 统计概览 ====================

	// 获取统计概览数据
	getStatisticsOverview: (date) => {
		return api.get('/statistics/overview', { params: { date } })
	},

	// ==================== 兼容性API (保留旧接口) ====================

	// 获取统计数据 (兼容旧接口)
	getStatistics: (params) => {
		return api.get('/statistics', { params })
	},

	// 获取活跃度统计 (兼容旧接口)
	getActivityStatistics: (params) => {
		return api.get('/statistics/activity', { params })
	},

	// 获取趋势分析 (兼容旧接口)
	getTrendAnalysis: (params) => {
		return api.get('/statistics/trend', { params })
	},

	// 获取出勤率统计 (兼容旧接口)
	getAttendanceRate: (params) => {
		return api.get('/statistics/attendance-rate', { params })
	},

	// 获取满勤次数 (兼容旧接口)
	getFullAttendanceCount: (params) => {
		return api.get('/statistics/full-attendance', { params })
	},

	// 生成统计报告 (兼容旧接口)
	generateReport: (params) => {
		return api.post('/statistics/report', params)
	}
}