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
		<label>时间范围:</label>
		<select v-model="timeRange">
			<option value="week">近一周</option>
			<option value="month">近一个月</option>
			<option value="half-year">近半年</option>
			<option value="year">近一年</option>
			<option value="custom">自定义</option>
		</select>
		<div v-if="timeRange === 'custom'" class="date-picker">
			<input type="date" v-model="startDate" />
			<span>至</span>
			<input type="date" v-model="endDate" />
		</div>
		</div>
		<div class="filter-group">
		<label>统计对象:</label>
		<select v-model="statisticsType">
			<option value="all">总体</option>
			<option value="individual">个人</option>
		</select>
		<select v-if="statisticsType === 'individual'" v-model="selectedPerson">
			<option v-for="person in personnel" :key="person.userId" :value="person.userId">
			{{ person.userName }}
			</option>
		</select>
		</div>
		<div class="filter-group">
		<label>统计内容:</label>
		<select v-model="statisticsContent">
			<option value="activity">活跃度</option>
			<option value="attendance">出勤率</option>
			<option value="full-attendance">满勤次数</option>
		</select>
		</div>
		<button @click="generateReport" class="generate-btn">生成报告</button>
	</div>
	
	<div class="statistics-summary">
		<div class="summary-card">
		<h4>总人数</h4>
		<p class="summary-value">{{ personnel.length }}</p>
		</div>
		<div class="summary-card">
		<h4>今日到勤</h4>
		<p class="summary-value">{{ activeCount }}</p>
		</div>
		<div class="summary-card">
		<h4>今日迟到</h4>
		<p class="summary-value">{{ lateCount }}</p>
		</div>
		<div class="summary-card">
		<h4>今日离开</h4>
		<p class="summary-value">{{ leaveCount }}</p>
		</div>
		<div class="summary-card">
		<h4>今日缺勤</h4>
		<p class="summary-value">{{ absentCount }}</p>
		</div>
		<div class="summary-card">
		<h4>假期人数</h4>
		<p class="summary-value">{{ holidayCount }}</p>
		</div>
		<div class="summary-card">
		<h4>请假人数</h4>
		<p class="summary-value">{{ excusedCount }}</p>
		</div>
		<div class="summary-card">
		<h4>出勤率</h4>
		<p class="summary-value">{{ attendanceRate }}%</p>
		</div>
	</div>
	
	<div class="statistics-charts">
		<div class="chart-container">
		<h3>活跃度统计</h3>
		<div class="activity-grid">
			<div 
			v-for="(day, index) in 365" 
			:key="index"
			class="activity-day"
			:class="getActivityLevel(index)"
			:title="getDayTitle(index)"
			></div>
		</div>
		<div class="activity-legend">
			<span>活跃度:</span>
			<div class="legend-item">
			<div class="legend-color high"></div>
			<span>高</span>
			</div>
			<div class="legend-item">
			<div class="legend-color medium"></div>
			<span>中</span>
			</div>
			<div class="legend-item">
			<div class="legend-color low"></div>
			<span>低</span>
			</div>
			<div class="legend-item">
			<div class="legend-color none"></div>
			<span>无</span>
			</div>
		</div>
		</div>
		<div class="chart-container">
		<h3>趋势分析</h3>
		<div class="trend-chart">
			<canvas id="trendCanvas" width="400" height="300"></canvas>
		</div>
		</div>
	</div>
	</div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { usePersonnelStore } from '../stores/personnel'
import { statisticsService } from '../services/statistics'

const personnelStore = usePersonnelStore()
const personnel = computed(() => personnelStore.personnelList)

const timeRange = ref('week')
const startDate = ref('')
const endDate = ref('')
const statisticsType = ref('all')
const selectedPerson = ref('')
const statisticsContent = ref('activity')

// 加载状态和错误处理
const loading = ref(false)
const error = ref(null)

// 统计数据
const statisticsData = ref({
	activeCount: 0,
	lateCount: 0,
	leaveCount: 0,
	absentCount: 0,
	holidayCount: 0,
	excusedCount: 0,
	attendanceRate: 0
})

// 活跃度数据
const activityData = ref([])

// 趋势数据
const trendData = ref([])

// 统计数据计算
const activeCount = computed(() => statisticsData.value.activeCount)
const lateCount = computed(() => statisticsData.value.lateCount)
const leaveCount = computed(() => statisticsData.value.leaveCount)
const absentCount = computed(() => statisticsData.value.absentCount)
const holidayCount = computed(() => statisticsData.value.holidayCount)
const excusedCount = computed(() => statisticsData.value.excusedCount)
const attendanceRate = computed(() => statisticsData.value.attendanceRate)

// 获取统计数据
const fetchStatistics = async () => {
	loading.value = true
	error.value = null
	try {
		const params = {
			timeRange: timeRange.value,
			startDate: startDate.value,
			endDate: endDate.value,
			statisticsType: statisticsType.value,
			selectedPerson: selectedPerson.value,
			statisticsContent: statisticsContent.value
		}
		const response = await statisticsService.getStatistics(params)
		statisticsData.value = response.data || response
	} catch (err) {
		error.value = '获取统计数据失败，请检查后端服务是否运行'
		console.error('获取统计数据失败:', err)
		// 使用默认数据，避免页面显示异常
		statisticsData.value = {
			activeCount: 0,
			lateCount: 0,
			leaveCount: 0,
			absentCount: 0,
			holidayCount: 0,
			excusedCount: 0,
			attendanceRate: 0
		}
	} finally {
		loading.value = false
	}
}

// 获取活跃度统计
const fetchActivityStatistics = async () => {
	loading.value = true
	error.value = null
	try {
		const params = {
			timeRange: timeRange.value,
			startDate: startDate.value,
			endDate: endDate.value
		}
		const response = await statisticsService.getActivityStatistics(params)
		activityData.value = response.data || response
	} catch (err) {
		error.value = '获取活跃度统计失败，请检查后端服务是否运行'
		console.error('获取活跃度统计失败:', err)
		// 清空活跃度数据，避免显示模拟数据
		activityData.value = []
	} finally {
		loading.value = false
	}
}

// 获取趋势分析
const fetchTrendAnalysis = async () => {
	loading.value = true
	error.value = null
	try {
		const params = {
			timeRange: timeRange.value,
			startDate: startDate.value,
			endDate: endDate.value,
			statisticsContent: statisticsContent.value
		}
		const response = await statisticsService.getTrendAnalysis(params)
		trendData.value = response.data || response
		drawTrendChart(response.data || response)
	} catch (err) {
		error.value = '获取趋势分析失败，请检查后端服务是否运行'
		console.error('获取趋势分析失败:', err)
		// 清空趋势数据，使用默认数据绘制图表
		trendData.value = []
		drawTrendChart([])
	} finally {
		loading.value = false
	}
}

// 活跃度计算
const getActivityLevel = (index) => {
	if (activityData.value[index]) {
		return activityData.value[index].level || 'none'
	}
	return 'none'
}

const getDayTitle = (index) => {
	if (activityData.value[index]) {
		return activityData.value[index].date || ''
	}
	const date = new Date()
	date.setDate(date.getDate() - (365 - index))
	return date.toLocaleDateString()
}

// 图表初始化
onMounted(async () => {
	await personnelStore.fetchPersonnelList()
	await fetchStatistics()
	await fetchActivityStatistics()
	await fetchTrendAnalysis()
})

// 监听筛选条件变化
watch(
	[timeRange, startDate, endDate, statisticsType, selectedPerson, statisticsContent],
	async () => {
		await fetchStatistics()
		await fetchActivityStatistics()
		await fetchTrendAnalysis()
	},
	{ deep: true }
)

const drawTrendChart = (data) => {
	const canvas = document.getElementById('trendCanvas')
	if (!canvas) return
	
	const ctx = canvas.getContext('2d')
	if (!ctx) return
	
	// 清空画布
	ctx.clearRect(0, 0, canvas.width, canvas.height)
	
	// 使用后端数据或默认数据
	const trendData = data && data.length > 0 ? data : [65, 59, 80, 81, 56, 55, 40, 60, 75, 85, 60, 50]
	const width = canvas.width - 40
	const height = canvas.height - 40
	const stepX = width / (trendData.length - 1)
	const stepY = height / 100
	
	// 绘制简单的趋势线
	ctx.beginPath()
	ctx.moveTo(20, 20 + height - trendData[0] * stepY)
	
	for (let i = 1; i < trendData.length; i++) {
	ctx.lineTo(20 + i * stepX, 20 + height - trendData[i] * stepY)
	}
	
	ctx.strokeStyle = '#409eff'
	ctx.lineWidth = 2
	ctx.stroke()
	
	// 绘制坐标轴
	ctx.beginPath()
	ctx.moveTo(20, 20)
	ctx.lineTo(20, 20 + height)
	ctx.lineTo(20 + width, 20 + height)
	ctx.strokeStyle = '#ddd'
	ctx.lineWidth = 1
	ctx.stroke()
}

const generateReport = async () => {
	loading.value = true
	error.value = null
	try {
		const params = {
			timeRange: timeRange.value,
			startDate: startDate.value,
			endDate: endDate.value,
			statisticsType: statisticsType.value,
			selectedPerson: selectedPerson.value,
			statisticsContent: statisticsContent.value
		}
		const response = await statisticsService.generateReport(params)
		console.log('生成报告响应:', response)
		// 处理报告生成结果，例如下载报告或显示报告链接
		if (response.url) {
			window.open(response.url, '_blank')
		} else {
			alert('报告生成成功')
		}
	} catch (err) {
		error.value = '生成报告失败'
		console.error('生成报告失败:', err)
	} finally {
		loading.value = false
	}
}
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

/* 图表容器 */
.statistics-charts {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
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

/* 活跃度网格 */
.activity-grid {
	display: grid;
	grid-template-columns: repeat(53, 1fr);
	grid-template-rows: repeat(7, 1fr);
	gap: 2px;
	height: 120px;
	background: #f5f5f5;
	padding: 10px;
	border-radius: 4px;
	margin-bottom: 15px;
}

.activity-day {
	border-radius: 2px;
	transition: transform 0.2s;
}

.activity-day:hover {
	transform: scale(1.5);
	z-index: 10;
}

.activity-day.high {
	background-color: #1989fa;
}

.activity-day.medium {
	background-color: #67c23a;
}

.activity-day.low {
	background-color: #e6a23c;
}

.activity-day.none {
	background-color: #ebedf0;
}

/* 活跃度图例 */
.activity-legend {
	display: flex;
	align-items: center;
	gap: 15px;
	justify-content: center;
	font-size: 0.8rem;
	color: #666;
}

.legend-item {
	display: flex;
	align-items: center;
	gap: 5px;
}

.legend-color {
	width: 12px;
	height: 12px;
	border-radius: 2px;
}

.legend-color.high {
	background-color: #1989fa;
}

.legend-color.medium {
	background-color: #67c23a;
}

.legend-color.low {
	background-color: #e6a23c;
}

.legend-color.none {
	background-color: #ebedf0;
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