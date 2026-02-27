<template>
	<div class="reports-view">
	<h2>报告生成</h2>
	<div class="report-filters">
		<div class="filter-group">
		<label>报告类型:</label>
		<select v-model="reportType">
			<option value="daily">日报</option>
			<option value="weekly">周报</option>
			<option value="monthly">月报</option>
			<option value="custom">自定义</option>
		</select>
		</div>
		<div class="filter-group">
		<label>日期范围:</label>
		<input type="date" v-model="startDate" />
		<span>至</span>
		<input type="date" v-model="endDate" />
		</div>
		<div class="filter-group">
		<label>包含内容:</label>
		<div class="checkbox-group">
			<label><input type="checkbox" v-model="includeAttendance" /> 考勤统计</label>
			<label><input type="checkbox" v-model="includeActivity" /> 活跃度分析</label>
			<label><input type="checkbox" v-model="includeTrend" /> 趋势分析</label>
			<label><input type="checkbox" v-model="includeDetails" /> 详细数据</label>
		</div>
		</div>
		<div class="filter-group">
		<label>输出格式:</label>
		<select v-model="outputFormat">
			<option value="pdf">PDF</option>
			<option value="excel">Excel</option>
			<option value="html">HTML</option>
		</select>
		</div>
		<button @click="generateReport" class="generate-btn">生成报告</button>
	</div>
	
	<div class="report-preview" v-if="reportGenerated">
		<div class="report-header">
		<h3>考勤报告预览</h3>
		<div class="report-actions">
			<button @click="downloadReport" class="download-btn">下载报告</button>
			<button @click="printReport" class="print-btn">打印报告</button>
		</div>
		</div>
		
		<div class="report-content">
		<div class="report-section">
			<h4>基本信息</h4>
			<div class="report-info">
			<p><strong>报告类型:</strong> {{ reportTypeText }}</p>
			<p><strong>统计时间:</strong> {{ startDate }} 至 {{ endDate }}</p>
			<p><strong>生成时间:</strong> {{ new Date().toLocaleString() }}</p>
			<p><strong>统计人数:</strong> {{ totalPeople }} 人</p>
			</div>
		</div>
		
		<div class="report-section" v-if="includeAttendance">
			<h4>考勤统计</h4>
			<div class="report-summary">
			<div class="summary-item">
				<span class="label">总出勤天数:</span>
				<span class="value">{{ totalAttendanceDays }}</span>
			</div>
			<div class="summary-item">
				<span class="label">平均出勤率:</span>
				<span class="value">{{ averageAttendanceRate }}%</span>
			</div>
			<div class="summary-item">
				<span class="label">迟到次数:</span>
				<span class="value">{{ totalLateCount }}</span>
			</div>
			<div class="summary-item">
				<span class="label">缺勤次数:</span>
				<span class="value">{{ totalAbsentCount }}</span>
			</div>
			</div>
		</div>
		
		<div class="report-section" v-if="includeDetails">
			<h4>详细数据</h4>
			<table class="report-table">
			<thead>
				<tr>
				<th>姓名</th>
				<th>出勤率</th>
				<th>迟到次数</th>
				<th>缺勤次数</th>
				<th>满勤天数</th>
				</tr>
			</thead>
			<tbody>
				<tr v-for="person in personnelData" :key="person.userId">
				<td>{{ person.userName }}</td>
				<td>{{ person.attendanceRate }}%</td>
				<td>{{ person.lateCount }}</td>
				<td>{{ person.absentCount }}</td>
				<td>{{ person.fullAttendanceDays }}</td>
				</tr>
			</tbody>
			</table>
		</div>
		</div>
	</div>
	</div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { usePersonnelStore } from '../stores/personnel'

const personnelStore = usePersonnelStore()
const personnel = computed(() => personnelStore.personnelList)

const reportType = ref('daily')
const startDate = ref(new Date().toISOString().split('T')[0])
const endDate = ref(new Date().toISOString().split('T')[0])
const includeAttendance = ref(true)
const includeActivity = ref(true)
const includeTrend = ref(true)
const includeDetails = ref(true)
const outputFormat = ref('pdf')
const reportGenerated = ref(false)

const reportTypeText = computed(() => {
	const types = {
	daily: '日报',
	weekly: '周报',
	monthly: '月报',
	custom: '自定义报告'
	}
	return types[reportType.value]
})

const totalPeople = ref(personnel.length)
const totalAttendanceDays = ref(120)
const averageAttendanceRate = ref(92)
const totalLateCount = ref(15)
const totalAbsentCount = ref(8)

const personnelData = ref([
	{
	userId: 1,
	userName: '张三',
	attendanceRate: 95,
	lateCount: 2,
	absentCount: 0,
	fullAttendanceDays: 18
	},
	{
	userId: 2,
	userName: '李四',
	attendanceRate: 88,
	lateCount: 5,
	absentCount: 2,
	fullAttendanceDays: 15
	},
	{
	userId: 3,
	userName: '王五',
	attendanceRate: 92,
	lateCount: 3,
	absentCount: 1,
	fullAttendanceDays: 17
	}
])

const generateReport = () => {
	console.log('生成报告:', {
	reportType: reportType.value,
	startDate: startDate.value,
	endDate: endDate.value,
	includeAttendance: includeAttendance.value,
	includeActivity: includeActivity.value,
	includeTrend: includeTrend.value,
	includeDetails: includeDetails.value,
	outputFormat: outputFormat.value
	})
	reportGenerated.value = true
}

const downloadReport = () => {
	console.log('下载报告')
}

const printReport = () => {
	window.print()
}
</script>

<style scoped>
.reports-view {
	padding: 20px;
}

.report-filters {
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

.checkbox-group {
	display: flex;
	flex-direction: column;
	gap: 5px;
}

.checkbox-group label {
	font-weight: normal;
	font-size: 0.9rem;
	display: flex;
	align-items: center;
	gap: 5px;
}

.checkbox-group input[type="checkbox"] {
	width: auto;
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

.report-preview {
	background: white;
	border-radius: 8px;
	padding: 20px;
	margin-top: 20px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.report-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20px;
	padding-bottom: 15px;
	border-bottom: 1px solid #e0e0e0;
}

.report-header h3 {
	margin: 0;
	color: #333;
}

.report-actions {
	display: flex;
	gap: 10px;
}

.download-btn,
.print-btn {
	padding: 8px 16px;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.9rem;
	transition: background-color 0.2s;
}

.download-btn {
	background: #67c23a;
	color: white;
}

.download-btn:hover {
	background: #85ce61;
}

.print-btn {
	background: #409eff;
	color: white;
}

.print-btn:hover {
	background: #66b1ff;
}

.report-content {
	margin-top: 20px;
}

.report-section {
	margin-bottom: 30px;
}

.report-section h4 {
	color: #333;
	margin-bottom: 15px;
	padding-bottom: 10px;
	border-bottom: 1px solid #e0e0e0;
}

.report-info p {
	margin: 8px 0;
	color: #666;
}

.report-summary {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
	gap: 15px;
}

.summary-item {
	background: #f5f7fa;
	padding: 15px;
	border-radius: 6px;
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.summary-item .label {
	color: #666;
	font-size: 0.9rem;
}

.summary-item .value {
	color: #409eff;
	font-size: 1.2rem;
	font-weight: bold;
}

.report-table {
	width: 100%;
	border-collapse: collapse;
}

.report-table th,
.report-table td {
	padding: 12px;
	text-align: left;
	border-bottom: 1px solid #e0e0e0;
}

.report-table th {
	background: #f5f7fa;
	font-weight: bold;
	color: #333;
}

.report-table tbody tr:hover {
	background: #f9f9f9;
}

@media (max-width: 768px) {
	.report-filters {
	flex-direction: column;
	align-items: stretch;
	}
	
	.report-header {
	flex-direction: column;
	align-items: flex-start;
	gap: 15px;
	}
	
	.report-summary {
	grid-template-columns: 1fr;
	}
	
	.report-table {
	font-size: 0.8rem;
	}
	
	.report-table th,
	.report-table td {
	padding: 8px;
	}
}
</style>
