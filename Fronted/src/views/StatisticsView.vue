<template>
	<div class="statistics-view">
		<h2>统计分析</h2>

		<!-- 错误信息 -->
		<div v-if="error" class="error-message">
			{{ error }}
		</div>

		<!-- 加载状态 -->
		<div v-if="loading" class="loading-overlay">
			<div class="loading-spinner"></div>
			<p>加载中...</p>
		</div>

		<div class="statistics-filters">
			<div class="filter-group">
				<label>统计对象:</label>
				<select v-model="statisticsTarget">
					<option value="user">个人统计</option>
					<option value="studio">工作室统计</option>
				</select>
			</div>

			<div v-if="statisticsTarget === 'user'" class="filter-group">
				<label>选择人员:</label>
				<select v-model="selectedUserNumber">
					<option value="">请选择人员</option>
					<option v-for="person in personnel" :key="person.userNumber" :value="person.userNumber">
						{{ person.userName }} ({{ person.userNumber }})
					</option>
				</select>
			</div>

			<div v-if="statisticsTarget === 'studio'" class="filter-group">
				<label>选择工作室:</label>
				<select v-model="selectedStudioId">
					<option value="">请选择工作室</option>
					<option v-for="studio in studios" :key="studio.id" :value="studio.id">
						{{ studio.name }}
					</option>
				</select>
			</div>

			<div class="filter-group">
				<label>时间周期:</label>
				<select v-model="timePeriod">
					<option value="daily">每日</option>
					<option value="weekly">每周</option>
					<option value="monthly">每月</option>
					<option value="yearly">每年</option>
				</select>
			</div>

			<div class="filter-group">
				<label>数据范围:</label>
				<select v-model="dataLimit">
					<option :value="7">最近7天</option>
					<option :value="30">最近30天</option>
					<option :value="90">最近90天</option>
					<option :value="365">最近一年</option>
				</select>
			</div>

			<button @click="fetchStatisticsData" class="generate-btn">刷新数据</button>
		</div>

		<!-- 统计概览 -->
		<div v-if="overviewData" class="statistics-summary">
			<div class="summary-card">
				<h4>总人数</h4>
				<p class="summary-value">{{ overviewData.totalUsers }}</p>
			</div>
			<div class="summary-card">
				<h4>今日到勤</h4>
				<p class="summary-value">{{ overviewData.todayAttendance }}</p>
			</div>
			<div class="summary-card">
				<h4>今日迟到</h4>
				<p class="summary-value">{{ overviewData.todayLate }}</p>
			</div>
			<div class="summary-card">
				<h4>今日早退</h4>
				<p class="summary-value">{{ overviewData.todayEarlyLeave }}</p>
			</div>
			<div class="summary-card">
				<h4>今日缺勤</h4>
				<p class="summary-value">{{ overviewData.todayAbsent }}</p>
			</div>
			<div class="summary-card">
				<h4>出勤率</h4>
				<p class="summary-value">{{ overviewData.attendanceRate }}%</p>
			</div>
		</div>

		<!-- 用户统计详情 -->
		<div v-if="statisticsTarget === 'user' && selectedUserNumber && userStats.length > 0" class="user-stats-section">
			<h3>{{ selectedUserName }} 的统计数据</h3>
			<div class="stats-table">
				<table>
					<thead>
						<tr>
							<th>日期</th>
							<th>到勤次数</th>
							<th>迟到次数</th>
							<th>早退次数</th>
							<th>缺勤次数</th>
							<th>工作时长(小时)</th>
							<th>出勤率(%)</th>
						</tr>
					</thead>
					<tbody>
						<tr v-for="stat in userStats" :key="stat.date || stat.weekStartDate || stat.month || stat.year">
							<td>{{ formatDate(stat) }}</td>
							<td>{{ stat.attendanceCount || stat.totalAttendance }}</td>
							<td>{{ stat.lateCount || stat.totalLate }}</td>
							<td>{{ stat.earlyLeaveCount || stat.totalEarlyLeave }}</td>
							<td>{{ stat.absentCount || stat.totalAbsent }}</td>
							<td>{{ stat.totalWorkHours ? stat.totalWorkHours.toFixed(1) : '-' }}</td>
							<td>{{ stat.attendanceRate ? stat.attendanceRate.toFixed(1) : '-' }}</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 工作室统计详情 -->
		<div v-if="statisticsTarget === 'studio' && selectedStudioId && studioStats.length > 0" class="studio-stats-section">
			<h3>{{ selectedStudioName }} 的统计数据</h3>
			<div class="stats-table">
				<table>
					<thead>
						<tr>
							<th>日期</th>
							<th>总人数</th>
							<th>到勤人数</th>
							<th>迟到人数</th>
							<th>早退人数</th>
							<th>缺勤人数</th>
							<th>出勤率(%)</th>
						</tr>
					</thead>
					<tbody>
						<tr v-for="stat in studioStats" :key="stat.date || stat.weekStartDate || stat.month || stat.year">
							<td>{{ formatDate(stat) }}</td>
							<td>{{ stat.totalUsers }}</td>
							<td>{{ stat.attendanceCount }}</td>
							<td>{{ stat.lateCount }}</td>
							<td>{{ stat.earlyLeaveCount }}</td>
							<td>{{ stat.absentCount }}</td>
							<td>{{ stat.attendanceRate ? stat.attendanceRate.toFixed(1) : '-' }}</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 图表区域 -->
		<div class="statistics-charts">
			<div class="chart-container">
				<h3>趋势分析</h3>
				<div class="trend-chart">
					<canvas id="trendCanvas" width="600" height="300"></canvas>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { usePersonnelStore } from '../stores/personnel'
import { useStudioStore } from '../stores/studio'
import { statisticsService } from '../services/statistics'

const personnelStore = usePersonnelStore()
const studioStore = useStudioStore()

const personnel = computed(() => personnelStore.personnelList)
const studios = computed(() => studioStore.studioList)

// 过滤器状态
const statisticsTarget = ref('user') // 'user' 或 'studio'
const selectedUserNumber = ref('')
const selectedStudioId = ref('')
const timePeriod = ref('daily') // 'daily', 'weekly', 'monthly', 'yearly'
const dataLimit = ref(7)

// 加载状态和错误处理
const loading = ref(false)
const error = ref(null)

// 数据状态
const overviewData = ref(null)
const userStats = ref([])
const studioStats = ref([])

// 计算属性
const selectedUserName = computed(() => {
	const user = personnel.value.find(p => p.userNumber === selectedUserNumber.value)
	return user ? user.userName : ''
})

const selectedStudioName = computed(() => {
	const studio = studios.value.find(s => s.id === selectedStudioId.value)
	return studio ? studio.name : ''
})

// 获取统计概览数据
const fetchOverviewData = async () => {
	try {
		const today = new Date().toISOString().split('T')[0]
		const response = await statisticsService.getStatisticsOverview(today)
		overviewData.value = response.data
	} catch (err) {
		console.error('获取统计概览失败:', err)
		// 设置默认数据
		overviewData.value = {
			totalUsers: personnel.value.length,
			todayAttendance: 0,
			todayLate: 0,
			todayEarlyLeave: 0,
			todayAbsent: 0,
			attendanceRate: 0
		}
	}
}

// 获取用户统计数据
const fetchUserStats = async () => {
	if (!selectedUserNumber.value) return

	try {
		let response
		const limit = parseInt(dataLimit.value)

		switch (timePeriod.value) {
			case 'daily':
				response = await statisticsService.getUserDailyStats(selectedUserNumber.value, limit)
				break
			case 'weekly':
				response = await statisticsService.getUserWeeklyStats(selectedUserNumber.value, limit)
				break
			case 'monthly':
				response = await statisticsService.getUserMonthlyStats(selectedUserNumber.value, limit)
				break
			case 'yearly':
				response = await statisticsService.getUserYearlyStats(selectedUserNumber.value, limit)
				break
		}

		userStats.value = Array.isArray(response.data) ? response.data : [response.data]
		drawTrendChart(userStats.value)
	} catch (err) {
		console.error('获取用户统计失败:', err)
		userStats.value = []
		error.value = '获取用户统计数据失败'
	}
}

// 获取工作室统计数据
const fetchStudioStats = async () => {
	if (!selectedStudioId.value) return

	try {
		let response
		const limit = parseInt(dataLimit.value)

		switch (timePeriod.value) {
			case 'daily':
				response = await statisticsService.getStudioDailyStats(selectedStudioId.value, limit)
				break
			case 'weekly':
				response = await statisticsService.getStudioWeeklyStats(selectedStudioId.value, limit)
				break
			case 'monthly':
				response = await statisticsService.getStudioMonthlyStats(selectedStudioId.value, limit)
				break
			case 'yearly':
				response = await statisticsService.getStudioYearlyStats(selectedStudioId.value, limit)
				break
		}

		studioStats.value = Array.isArray(response.data) ? response.data : [response.data]
		drawTrendChart(studioStats.value)
	} catch (err) {
		console.error('获取工作室统计失败:', err)
		studioStats.value = []
		error.value = '获取工作室统计数据失败'
	}
}

// 统一的获取数据方法
const fetchStatisticsData = async () => {
	loading.value = true
	error.value = null

	try {
		await fetchOverviewData()

		if (statisticsTarget.value === 'user') {
			await fetchUserStats()
		} else {
			await fetchStudioStats()
		}
	} catch (err) {
		error.value = '获取统计数据失败，请检查后端服务是否运行'
		console.error('获取统计数据失败:', err)
	} finally {
		loading.value = false
	}
}

// 格式化日期显示
const formatDate = (stat) => {
	if (stat.date) return stat.date
	if (stat.weekStartDate) return `${stat.weekStartDate} (周)`
	if (stat.month) return `${stat.month}月`
	if (stat.year) return `${stat.year}年`
	return '-'
}

// 绘制趋势图表
const drawTrendChart = (data) => {
	const canvas = document.getElementById('trendCanvas')
	if (!canvas || !data || data.length === 0) return

	const ctx = canvas.getContext('2d')
	if (!ctx) return

	// 清空画布
	ctx.clearRect(0, 0, canvas.width, canvas.height)

	const width = canvas.width - 80
	const height = canvas.height - 60
	const padding = 40

	// 获取数据值
	const values = data.map(item => {
		// 根据不同时间周期获取对应的值
		if (timePeriod.value === 'daily') {
			return item.attendanceCount || item.totalAttendance || 0
		} else if (timePeriod.value === 'weekly' || timePeriod.value === 'monthly' || timePeriod.value === 'yearly') {
			return item.attendanceRate || 0
		}
		return 0
	})

	if (values.length === 0) return

	const maxValue = Math.max(...values)
	const minValue = Math.min(...values)
	const range = maxValue - minValue || 1

	const stepX = width / (values.length - 1 || 1)
	const stepY = height / range

	// 绘制坐标轴
	ctx.strokeStyle = '#ddd'
	ctx.lineWidth = 1
	ctx.beginPath()
	ctx.moveTo(padding, padding)
	ctx.lineTo(padding, height + padding)
	ctx.lineTo(width + padding, height + padding)
	ctx.stroke()

	// 绘制网格线
	ctx.strokeStyle = '#f0f0f0'
	ctx.lineWidth = 1
	for (let i = 0; i <= 5; i++) {
		const y = padding + (height / 5) * i
		ctx.beginPath()
		ctx.moveTo(padding, y)
		ctx.lineTo(width + padding, y)
		ctx.stroke()
	}

	// 绘制数据线
	ctx.strokeStyle = '#409eff'
	ctx.lineWidth = 3
	ctx.beginPath()

	values.forEach((value, index) => {
		const x = padding + index * stepX
		const y = height + padding - ((value - minValue) / range) * height

		if (index === 0) {
			ctx.moveTo(x, y)
		} else {
			ctx.lineTo(x, y)
		}
	})

	ctx.stroke()

	// 绘制数据点
	ctx.fillStyle = '#409eff'
	values.forEach((value, index) => {
		const x = padding + index * stepX
		const y = height + padding - ((value - minValue) / range) * height

		ctx.beginPath()
		ctx.arc(x, y, 4, 0, 2 * Math.PI)
		ctx.fill()
	})

	// 添加数值标签
	ctx.fillStyle = '#666'
	ctx.font = '12px Arial'
	ctx.textAlign = 'center'
	values.forEach((value, index) => {
		if (index % Math.ceil(values.length / 10) === 0) { // 每隔几个点显示标签
			const x = padding + index * stepX
			const y = height + padding - ((value - minValue) / range) * height - 15
			ctx.fillText(value.toString(), x, y)
		}
	})
}

// 初始化
onMounted(async () => {
	await personnelStore.fetchPersonnelList()
	await studioStore.fetchStudioList()
	await fetchStatisticsData()
})

// 监听筛选条件变化
watch(
	[statisticsTarget, selectedUserNumber, selectedStudioId, timePeriod, dataLimit],
	async () => {
		await fetchStatisticsData()
	},
	{ deep: true }
)
</script>

<style scoped>
.statistics-view {
	padding: 20px;
	position: relative;
}

/* 错误信息 */
.error-message {
	background-color: #f8d7da;
	color: #721c24;
	padding: 10px 15px;
	border-radius: 4px;
	margin-bottom: 20px;
	border-left: 4px solid #dc3545;
}

/* 加载状态 */
.loading-overlay {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background-color: rgba(255, 255, 255, 0.8);
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	z-index: 1000;
}

.loading-spinner {
	width: 40px;
	height: 40px;
	border: 4px solid #f3f3f3;
	border-top: 4px solid #409eff;
	border-radius: 50%;
	animation: spin 1s linear infinite;
	margin-bottom: 10px;
}

@keyframes spin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(360deg); }
}

.loading-overlay p {
	margin: 0;
	color: #666;
	font-size: 1rem;
}

.statistics-filters {
	display: flex;
	flex-wrap: wrap;
	gap: 15px;
	margin: 20px 0;
	padding: 20px;
	background: white;
	border-radius: 8px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.filter-group {
	display: flex;
	align-items: center;
	gap: 8px;
}

.filter-group label {
	font-weight: bold;
	color: #333;
}

.filter-group select,
.filter-group input {
	padding: 8px 12px;
	border: 1px solid #ddd;
	border-radius: 4px;
	font-size: 0.9rem;
}

.date-picker {
	display: flex;
	align-items: center;
	gap: 8px;
}

.generate-btn {
	padding: 8px 20px;
	background: #409eff;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.9rem;
	transition: background-color 0.2s;
}

.generate-btn:hover {
	background: #66b1ff;
}

/* 统计摘要卡片 */
.statistics-summary {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
	gap: 15px;
	margin: 20px 0;
}

.summary-card {
	background: white;
	padding: 20px;
	border-radius: 8px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
	text-align: center;
}

.summary-card h4 {
	margin: 0 0 10px 0;
	color: #666;
	font-size: 0.9rem;
	font-weight: normal;
}

.summary-value {
	margin: 0;
	font-size: 2rem;
	font-weight: bold;
	color: #409eff;
}

/* 统计表格样式 */
.user-stats-section, .studio-stats-section {
	margin: 20px 0;
	background: white;
	border-radius: 8px;
	padding: 20px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-stats-section h3, .studio-stats-section h3 {
	margin-top: 0;
	color: #333;
	font-size: 1.2rem;
	margin-bottom: 20px;
}

.stats-table {
	overflow-x: auto;
}

.stats-table table {
	width: 100%;
	border-collapse: collapse;
	font-size: 0.9rem;
}

.stats-table th, .stats-table td {
	padding: 12px 15px;
	text-align: left;
	border-bottom: 1px solid #eee;
}

.stats-table th {
	background-color: #f8f9fa;
	font-weight: 600;
	color: #333;
}

.stats-table tr:hover {
	background-color: #f8f9fa;
}

.stats-table td {
	color: #666;
}

/* 图表容器 */
.statistics-charts {
	display: grid;
	grid-template-columns: 1fr;
	gap: 20px;
	margin-top: 20px;
}

.chart-container {
	background: white;
	border-radius: 8px;
	padding: 20px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chart-container h3 {
	margin-top: 0;
	color: #333;
	font-size: 1.1rem;
	margin-bottom: 20px;
}

/* 趋势图表 */
.trend-chart {
	background: white;
	border-radius: 4px;
	display: flex;
	justify-content: center;
	align-items: center;
	padding: 10px;
}

.trend-chart canvas {
	max-width: 100%;
	height: auto;
}

/* 响应式设计 */
@media (max-width: 1200px) {
	.activity-grid {
	grid-template-columns: repeat(26, 1fr);
	grid-template-rows: repeat(14, 1fr);
	height: 240px;
	}
}

@media (max-width: 768px) {
	.statistics-filters {
	flex-direction: column;
	align-items: stretch;
	}
	
	.statistics-charts {
	grid-template-columns: 1fr;
	}
	
	.statistics-summary {
	grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
	}
	
	.activity-grid {
	grid-template-columns: repeat(13, 1fr);
	grid-template-rows: repeat(28, 1fr);
	height: 480px;
	}
	
	.activity-legend {
	flex-wrap: wrap;
	gap: 10px;
	}
}

@media (max-width: 480px) {
	.statistics-summary {
	grid-template-columns: repeat(2, 1fr);
	}
	
	.activity-grid {
	grid-template-columns: repeat(10, 1fr);
	grid-template-rows: repeat(37, 1fr);
	height: 500px;
	}
}
</style>