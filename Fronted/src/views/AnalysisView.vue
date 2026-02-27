<template>
        <div class="analysis-view">
                <h2>出勤率分析</h2>
                <div class="analysis-filters">
                        <div class="filter-group">
                                <label>时间范围:</label>
                                <select v-model="timeRange">
                                        <option value="week">近一周</option>
                                        <option value="month">近一个月</option>
                                        <option value="quarter">近一季度</option>
                                        <option value="year">近一年</option>
                                </select>
                        </div>
                        <div class="filter-group">
                                <label>分析对象:</label>
                                <select v-model="analysisType">
                                        <option value="all">总体分析</option>
                                        <option value="department">部门分析</option>
                                        <option value="individual">个人分析</option>
                                </select>
                        </div>
                        <button @click="generateAnalysis" class="generate-btn">生成分析</button>
                </div>

                <div class="analysis-summary">
                        <div class="summary-card">
                                <h4>平均出勤率</h4>
                                <p class="summary-value">{{ averageAttendanceRate }}%</p>
                        </div>
                        <div class="summary-card">
                                <h4>最高出勤率</h4>
                                <p class="summary-value">{{ maxAttendanceRate }}%</p>
                        </div>
                        <div class="summary-card">
                                <h4>最低出勤率</h4>
                                <p class="summary-value">{{ minAttendanceRate }}%</p>
                        </div>
                        <div class="summary-card">
                                <h4>迟到次数</h4>
                                <p class="summary-value">{{ lateCount }}</p>
                        </div>
                        <div class="summary-card">
                                <h4>缺勤次数</h4>
                                <p class="summary-value">{{ absentCount }}</p>
                        </div>
                </div>

                <div class="analysis-charts">
                        <div class="chart-container">
                                <h3>出勤率趋势</h3>
                                <div class="trend-chart">
                                        <canvas id="attendanceTrendCanvas" width="400" height="300"></canvas>
                                </div>
                        </div>
                        <div class="chart-container">
                                <h3>出勤率分布</h3>
                                <div class="distribution-chart">
                                        <canvas id="distributionCanvas" width="400" height="300"></canvas>
                                </div>
                        </div>
                </div>

                <div class="analysis-details">
                        <h3>详细数据</h3>
                        <table class="analysis-table">
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
                                        <tr v-for="person in personnelAnalysis" :key="person.userId">
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
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePersonnelStore } from '../stores/personnel'

const personnelStore = usePersonnelStore()
const personnel = computed(() => personnelStore.personnelList)

const timeRange = ref('week')
const analysisType = ref('all')

const averageAttendanceRate = ref(92)
const maxAttendanceRate = ref(100)
const minAttendanceRate = ref(85)
const lateCount = ref(12)
const absentCount = ref(5)

const personnelAnalysis = ref([
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

onMounted(() => {
        drawTrendChart()
        drawDistributionChart()
})

const drawTrendChart = () => {
        const canvas = document.getElementById('attendanceTrendCanvas')
        if (!canvas) return

        const ctx = canvas.getContext('2d')
        if (!ctx) return

        ctx.clearRect(0, 0, canvas.width, canvas.height)

        const data = [85, 88, 90, 87, 92, 95, 93]
        const width = canvas.width - 40
        const height = canvas.height - 40
        const stepX = width / (data.length - 1)
        const stepY = height / 100

        ctx.beginPath()
        ctx.moveTo(20, 20 + height - data[0] * stepY)

        for (let i = 1; i < data.length; i++) {
                ctx.lineTo(20 + i * stepX, 20 + height - data[i] * stepY)
        }

        ctx.strokeStyle = '#67c23a'
        ctx.lineWidth = 2
        ctx.stroke()

        ctx.beginPath()
        ctx.moveTo(20, 20)
        ctx.lineTo(20, 20 + height)
        ctx.lineTo(20 + width, 20 + height)
        ctx.strokeStyle = '#ddd'
        ctx.lineWidth = 1
        ctx.stroke()
}

const drawDistributionChart = () => {
        const canvas = document.getElementById('distributionCanvas')
        if (!canvas) return

        const ctx = canvas.getContext('2d')
        if (!ctx) return

        ctx.clearRect(0, 0, canvas.width, canvas.height)

        const data = [30, 45, 20, 5]
        const colors = ['#67c23a', '#e6a23c', '#f56c6c', '#909399']
        const labels = ['90-100%', '80-89%', '70-79%', '<70%']

        const centerX = canvas.width / 2
        const centerY = canvas.height / 2
        const radius = 100

        let startAngle = 0

        data.forEach((value, index) => {
                const sliceAngle = (value / 100) * 2 * Math.PI

                ctx.beginPath()
                ctx.moveTo(centerX, centerY)
                ctx.arc(centerX, centerY, radius, startAngle, startAngle + sliceAngle)
                ctx.closePath()

                ctx.fillStyle = colors[index]
                ctx.fill()

                startAngle += sliceAngle
        })

        ctx.beginPath()
        ctx.arc(centerX, centerY, 60, 0, 2 * Math.PI)
        ctx.fillStyle = 'white'
        ctx.fill()

        ctx.fillStyle = '#333'
        ctx.font = '14px Arial'
        ctx.textAlign = 'center'
        ctx.fillText('出勤率分布', centerX, centerY + 5)
}

const generateAnalysis = () => {
        console.log('生成出勤率分析:', {
                timeRange: timeRange.value,
                analysisType: analysisType.value
        })
}
</script>

<style scoped>
.analysis-view {
        padding: 20px;
}

.analysis-filters {
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

.filter-group select {
        padding: 8px 12px;
        border: 1px solid #ddd;
        border-radius: 4px;
        font-size: 0.9rem;
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

.analysis-summary {
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
        color: #67c23a;
}

.analysis-charts {
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

.trend-chart,
.distribution-chart {
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 10px;
}

.trend-chart canvas,
.distribution-chart canvas {
        max-width: 100%;
        height: auto;
}

.analysis-details {
        background: white;
        border-radius: 8px;
        padding: 20px;
        margin-top: 20px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.analysis-details h3 {
        margin-top: 0;
        color: #333;
        font-size: 1.1rem;
        margin-bottom: 20px;
}

.analysis-table {
        width: 100%;
        border-collapse: collapse;
}

.analysis-table th,
.analysis-table td {
        padding: 12px;
        text-align: left;
        border-bottom: 1px solid #e0e0e0;
}

.analysis-table th {
        background: #f5f7fa;
        font-weight: bold;
        color: #333;
}

.analysis-table tbody tr:hover {
        background: #f9f9f9;
}

@media (max-width: 768px) {
        .analysis-filters {
                flex-direction: column;
                align-items: stretch;
        }

        .analysis-charts {
                grid-template-columns: 1fr;
        }

        .analysis-summary {
                grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
        }

        .analysis-table {
                font-size: 0.8rem;
        }

        .analysis-table th,
        .analysis-table td {
                padding: 8px;
        }
}
</style>
