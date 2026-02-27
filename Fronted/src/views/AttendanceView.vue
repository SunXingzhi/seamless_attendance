<template>
        <div class="attendance-view">
                <h2>考勤记录</h2>
                <div class="attendance-filters">
                        <div class="filter-group">
                                <label>日期范围:</label>
                                <input type="date" v-model="startDate" />
                                <span>至</span>
                                <input type="date" v-model="endDate" />
                        </div>
                        <div class="filter-group">
                                <label>人员:</label>
                                <select v-model="selectedPerson">
                                        <option value="">全部人员</option>
                                        <option v-for="person in personnel" :key="person.userId" :value="person.userId">
                                                {{ person.userName }}
                                        </option>
                                </select>
                        </div>
                        <div class="filter-group">
                                <label>状态:</label>
                                <select v-model="statusFilter">
                                        <option value="">全部状态</option>
                                        <option value="active">到勤</option>
                                        <option value="late">迟到</option>
                                        <option value="leave">离开</option>
                                        <option value="absent">缺勤</option>
                                        <option value="holiday">假期</option>
                                        <option value="excused">请假</option>
                                </select>
                        </div>
                        <button @click="searchRecords" class="search-btn">查询</button>
                </div>

                <!-- 加载状态 -->
                <div v-if="loading" class="loading-container">
                        <div class="loading-spinner">加载中...</div>
                </div>

                <!-- 错误提示 -->
                <div v-else-if="error" class="error-container">
                        <div class="error-message">{{ error }}</div>
                        <button @click="fetchAllRecords" class="retry-btn">重试</button>
                </div>

                <!-- 考勤记录表格 -->
                <div v-else class="attendance-table-container">
                        <table class="attendance-table">
                                <thead>
                                        <tr>
                                                <!-- <th>日期</th> -->
                                                <th>姓名</th>
                                                <th>学号/工号</th>
                                                <th>签到时间</th>
                                                <th>签退时间</th>
                                                <th>工作时长</th>
                                                <th>状态</th>
                                                <!-- <th>备注</th> -->
                                        </tr>
                                </thead>
                                <tbody>
                                        <tr v-for="record in filteredRecords" :key="record.id">
                                                <!-- <td>{{ record.date }}</td> -->
                                                <td>{{ record.userName }}</td>
                                                <td>{{ record.userNumber }}</td>
                                                <td>{{ record.checkInTime }}</td>
                                                <td>{{ record.checkOutTime }}</td>
                                                <td>{{ record.workTime }}</td>
                                                <td>
                                                        <span class="status-badge" :class="record.status">
                                                                {{
                                                                        record.status === 'active' ? '到勤' :
                                                                                record.status === 'late' ? '迟到' :
                                                                record.status === 'leave' ? '离开' :
                                                                record.status === 'absent' ? '缺勤' :
                                                                record.status === 'holiday' ? '假期' :
                                                                record.status === 'excused' ? '请假' : '未知'
                                                                }}
                                                        </span>
                                                </td>
                                                <td>{{ record.remark }}</td>
                                        </tr>
                                </tbody>
                        </table>
                </div>

                <!-- 空状态 -->
                <div v-if="!loading && !error && filteredRecords.length === 0" class="empty-state">
                        <p>暂无考勤记录</p>
                </div>
        </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePersonnelStore } from '../stores/personnel'
import { personnelService } from '../services/personnel'

const personnelStore = usePersonnelStore()
const personnel = computed(() => personnelStore.personnelList)

const startDate = ref('')
const endDate = ref('')
const selectedPerson = ref('')
const statusFilter = ref('')

const loading = ref(false)
const error = ref(null)

const attendanceRecords = ref([])

const filteredRecords = computed(() => {
        return attendanceRecords.value.filter(record => {
                const matchesPerson = !selectedPerson.value || record.userId === selectedPerson.value
                const matchesStatus = !statusFilter.value || record.status === statusFilter.value
                return matchesPerson && matchesStatus
        })
})

const transformAttendanceRecord = (data) => {
        return {
                id: data.id,
                date: data.date,
                userId: data.user_id,
                userName: data.name,
                userNumber: data.user_number,
                checkInTime: data.check_in_time || '-',
                checkOutTime: data.check_out_time || '-',
                workTime: data.work_hours || '-',
                status: data.status,
                remark: data.remark || ''
        }
}

const fetchAllRecords = async () => {
        loading.value = true
        error.value = null
        try {
                const response = await personnelService.getAttendanceRecords()
                console.log('考勤记录数据:', response)

                let rawData = []
                if (Array.isArray(response)) {
                        rawData = response
                } else if (response && Array.isArray(response.data)) {
                        rawData = response.data
                } else if (response && Array.isArray(response.list)) {
                        rawData = response.list
                } else {
                        console.warn('响应数据格式不正确，使用空数组:', response)
                        attendanceRecords.value = []
                        return
                }

                attendanceRecords.value = rawData.map(item => transformAttendanceRecord(item))
        } catch (error) {
                console.error('获取考勤记录失败:', error)
                error.value = error.message
                attendanceRecords.value = []
        } finally {
                loading.value = false
        }
}

const searchRecords = async () => {
        loading.value = true
        error.value = null
        try {
                let response

                if (startDate.value && endDate.value) {
                        response = await personnelService.getAttendanceRecordsByDateRange(startDate.value, endDate.value)
                } else if (selectedPerson.value) {
                        response = await personnelService.getAttendanceRecordsByUserId(selectedPerson.value)
                } else {
                        await fetchAllRecords()
                        return
                }

                console.log('查询考勤记录:', response)

                let rawData = []
                if (Array.isArray(response)) {
                        rawData = response
                } else if (response && Array.isArray(response.data)) {
                        rawData = response.data
                } else if (response && Array.isArray(response.list)) {
                        rawData = response.list
                } else {
                        console.warn('响应数据格式不正确，使用空数组:', response)
                        attendanceRecords.value = []
                        return
                }

                attendanceRecords.value = rawData.map(item => transformAttendanceRecord(item))
        } catch (error) {
                console.error('查询考勤记录失败:', error)
                error.value = error.message
                attendanceRecords.value = []
        } finally {
                loading.value = false
        }
}

onMounted(() => {
        personnelStore.fetchPersonnelList()
        fetchAllRecords()
})
</script>

<style scoped>
.attendance-view {
        padding: 20px;
}

.attendance-filters {
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

.filter-group input,
.filter-group select {
        padding: 8px 12px;
        border: 1px solid #ddd;
        border-radius: 4px;
        font-size: 0.9rem;
}

.search-btn {
        padding: 8px 20px;
        background: #409eff;
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 0.9rem;
        transition: background-color 0.2s;
}

.search-btn:hover {
        background: #66b1ff;
}

.attendance-table-container {
        background: white;
        border-radius: 8px;
        padding: 20px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.attendance-table {
        width: 100%;
        border-collapse: collapse;
}

.attendance-table th,
.attendance-table td {
        padding: 12px;
        text-align: left;
        border-bottom: 1px solid #e0e0e0;
}

.attendance-table th {
        background: #f5f7fa;
        font-weight: bold;
        color: #333;
}

.attendance-table tbody tr:hover {
        background: #f9f9f9;
}

.status-badge {
        padding: 4px 12px;
        border-radius: 12px;
        font-size: 0.8rem;
        font-weight: bold;
}

.status-badge.active {
        background-color: #d4edda;
        color: #155724;
}

.status-badge.late {
        background-color: #fff3cd;
        color: #856404;
}

.status-badge.leave {
        background-color: #e2e3e5;
        color: #383d41;
}

.status-badge.absent {
        background-color: #f8d7da;
        color: #721c24;
}

.status-badge.holiday {
        background-color: #cce5ff;
        color: #004085;
}

.status-badge.excused {
        background-color: #d1ecf1;
        color: #0c5460;
}

.loading-container,
.error-container,
.empty-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 60px 20px;
        text-align: center;
}

.loading-spinner {
        font-size: 1.2rem;
        color: #409eff;
}

.error-message {
        color: #f56c6c;
        margin-bottom: 15px;
        font-size: 1rem;
}

.retry-btn {
        padding: 8px 16px;
        background: #409eff;
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 0.9rem;
        transition: background-color 0.2s;
}

.retry-btn:hover {
        background: #66b1ff;
}

.empty-state {
        color: #909399;
        font-size: 1rem;
}

@media (max-width: 768px) {
        .attendance-filters {
                flex-direction: column;
                align-items: stretch;
        }

        .attendance-table {
                font-size: 0.8rem;
        }

        .attendance-table th,
        .attendance-table td {
                padding: 8px;
        }
}
</style>
