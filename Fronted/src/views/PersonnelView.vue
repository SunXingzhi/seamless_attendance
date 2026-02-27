<template>
        <div class="personnel-view">
                <h2>人员管理</h2>

                <!-- 搜索和操作栏 -->
                <div class="personnel-header">
                        <div class="search-filter">
                                <input type="text" v-model="searchQuery" placeholder="搜索姓名、学号/工号..."
                                        class="search-input" />
                                <select v-if="isSuperAdmin" v-model="roleFilter" class="filter-select">
                                        <option value="">全部角色</option>
                                        <option value="studio_admin">工作室管理员</option>
                                        <option value="studio_member">工作室员工</option>
                                </select>
                                <select v-if="isSuperAdmin" v-model="pairingFilter" class="filter-select">
                                        <option value="">全部配对状态</option>
                                        <option value="paired">已配对</option>
                                        <option value="unpaired">未配对</option>
                                </select>
                                <select v-if="isSuperAdmin" v-model="statusFilter" class="filter-select">
                                        <option value="">全部考勤状态</option>
                                        <option value="active">出勤</option>
                                        <option value="late">迟到</option>
                                        <option value="leave">请假</option>
                                        <option value="absent">缺勤</option>
                                        <option value="holiday">假期</option>
                                        <option value="excused">公假</option>
                                </select>
                        </div>
                        <button v-if="isSuperAdmin" @click="showAddModal = true" class="add-person-btn">添加人员</button>
                </div>

                <!-- 加载状态 -->
                <div v-if="loading" class="loading-container">
                        <div class="loading-spinner">加载中...</div>
                </div>

                <!-- 错误提示 -->
                <div v-else-if="error" class="error-container">
                        <div class="error-message">{{ error }}</div>
                        <button @click="personnelStore.fetchPersonnelList()" class="retry-btn">重试</button>
                </div>

                <!-- 人员列表 -->
                <div v-else class="personnel-grid">
                        <div v-for="person in filteredPersonnel" :key="person.userId" class="person-card">
                                <div class="person-info">
                                        <h3>{{ person.userName }}</h3>
                                        <div class="status-container">
                                                <div class="attendance-status" :class="person.attendanceStatus">
                                                        {{ getAttendanceStatusText(person.attendanceStatus) }}
                                                </div>
                                                <div class="pairing-status" :class="person.pairingStatus">
                                                        {{ person.pairingStatus === 'paired' ? '已配对' : '未配对' }}
                                                </div>
                                        </div>
                                </div>
                                <div class="person-details">
                                        <p><strong>学号/工号:</strong> {{ person.userNumber }}</p>
                                        <p><strong>职称:</strong> {{ person.role === 'studio_admin' ? '工作室管理员' : '工作室员工'
                                                }}</p>
                                        <p><strong>工作:</strong> {{ person.workTask }}</p>
                                        <p><strong>联系方式:</strong> {{ person.contactType === 'phone' ? '电话' : '邮箱' }}: {{
                                                person.contactNumber }}
                                        </p>
                                </div>
                                <div class="person-actions">
                                        <button @click="editPerson(person)" class="action-btn edit-btn">编辑</button>
                                        <button @click="deletePerson(person.userId)"
                                                class="action-btn delete-btn">删除</button>
                                </div>

                        </div>
                </div>

                <!-- 空状态 -->
                <div v-if="!loading && filteredPersonnel.length === 0" class="empty-state">
                        <p>暂无人员数据</p>
                </div>

                <!-- 添加人员模态框 -->
                <div v-if="showAddModal" class="modal-overlay" @click.self="closeModal">
                        <div class="modal-content">
                                <div class="modal-header">
                                        <h3>添加人员</h3>
                                        <button @click="closeModal" class="close-btn">&times;</button>
                                </div>
                                <form @submit.prevent="submitForm" class="person-form">
                                        <div class="form-group">
                                                <label>姓名:</label>
                                                <input type="text" v-model="formData.userName" required />
                                        </div>
                                        <!-- <div class="form-group">
	<label>系统账号名:</label>
	<input type="text" v-model="formData.user_name" required maxlength="10" placeholder="用于登录系统，不超过10字符" />
</div> -->
                                        <div class="form-group">
                                                <label>学号/工号:</label>
                                                <input type="text" v-model="formData.userNumber" required />
                                        </div>
                                        <div class="form-row">
                                                <div class="form-group">
                                                        <label>联系方式类型:</label>
                                                        <select v-model="formData.contactType">
                                                                <option value="phone">电话</option>
                                                                <option value="email">邮箱</option>
                                                        </select>
                                                </div>
                                                <div class="form-group">
                                                        <label>联系方式:</label>
                                                        <input type="text" v-model="formData.contactNumber" required
                                                                placeholder="请输入联系方式" />
                                                </div>
                                        </div>
                                        <div class="form-row">
                                                <div class="form-group">
                                                        <label>角色:</label>
                                                        <select v-model="formData.role">
                                                                <option value="studio_admin">工作室管理员</option>
                                                                <option value="studio_member">工作室员工</option>
                                                        </select>
                                                </div>
                                                <div class="form-group">
                                                        <label>工作任务:</label>
                                                        <input type="text" v-model="formData.workTask" required />
                                                </div>
                                        </div>

                                        <div class="form-actions">
                                                <button type="button" @click="closeModal" class="cancel-btn">取消</button>
                                                <button type="submit" class="save-btn">保存</button>
                                        </div>
                                </form>
                        </div>
                </div>
        </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { usePersonnelStore } from '../stores/personnel'
import { useAuthStore } from '../stores/auth'
import { personnelService } from '../services/personnel'
import { statisticsService } from "../services/statistics";
import webSocketService from '../services/websocket';

const personnelStore = usePersonnelStore()
const authStore = useAuthStore()
const personnel = computed(() => personnelStore.personnelList)
const loading = computed(() => personnelStore.loading)
const error = computed(() => personnelStore.error)

const userRole = computed(() => authStore.userRole)
const userStudioId = computed(() => authStore.userStudioId)

console.log("personnel", personnel.value)

// 更新人员状态
const updatePersonnelStatus = (userNumber, status) => {
        // 更新 personnelStore 中的人员状态
        personnelStore.updatePersonnelStatus(userNumber, status)
}

onMounted(async () => {
        await personnelStore.fetchPersonnelList()
        
        // 连接 WebSocket
        try {
                await webSocketService.connect()
                
                // 注册状态更新消息监听器
                webSocketService.on('status', (message) => {
                        console.log('接收到状态更新消息:', message)
                        if (message.userNumber && message.status) {
                                updatePersonnelStatus(message.userNumber, message.status)
                        }
                })
        } catch (error) {
                console.error('WebSocket 连接失败:', error)
        }
})

onUnmounted(() => {
        // 清理 WebSocket 监听器
        webSocketService.off('status')
})

const isSuperAdmin = computed(() => userRole.value === 'super_admin')
const isStudioAdmin = computed(() => userRole.value === 'studio_admin')
const isStudioMember = computed(() => userRole.value === 'studio_member')

// 搜索和筛选
const searchQuery = ref('')
const roleFilter = ref('')
const pairingFilter = ref('')
const statusFilter = ref('')

// 模态框
const showAddModal = ref(false)
const editingPersonId = ref(null)
const formData = ref({
        userName: '',
        userNumber: '',
        contactType: 'phone',
        contactNumber: '',
        role: 'studio_member',
        workTask: '',
        pairingStatus: 'unpaired'
})

// 计算属性：筛选后的人员列表
const filteredPersonnel = computed(() => {
        if (!personnel.value || !Array.isArray(personnel.value)) {
                return []
        }

        return personnel.value.filter(person => {
                if (!person) return false

                // 工作室管理员和工作室成员只能看到自己工作室的人员
                if (!isSuperAdmin.value && userStudioId.value) {
                        if (person.studioId !== userStudioId.value) {
                                return false
                        }
                }

                const userName = person.userName || ''
                const userNumber = person.userNumber || ''
                const searchLower = searchQuery.value.toLowerCase()

                const matchesSearch = userName.toLowerCase().includes(searchLower) ||
                        userNumber.toLowerCase().includes(searchLower)
                const matchesRole = !roleFilter.value || person.role === roleFilter.value
                const matchesPairing = !pairingFilter.value || person.pairingStatus === pairingFilter.value
                const matchesStatus = !statusFilter.value || person.attendanceStatus === statusFilter.value
                return matchesSearch && matchesRole && matchesPairing && matchesStatus
        })
})

// 表单操作
const closeModal = () => {
        showAddModal.value = false
        editingPersonId.value = null
        resetForm()
}

const resetForm = () => {
        formData.value = {
                userName: '',
                userNumber: '',
                contactType: 'phone',
                contactNumber: '',
                role: 'studio_member',
                workTask: '',
                pairingStatus: 'unpaired'
        }
}

const editPerson = (person) => {
        editingPersonId.value = person.userId
        formData.value = {
                userName: person.userName || '',
                userNumber: person.userNumber || '',
                contactType: person.contactType || 'phone',
                contactNumber: person.contactNumber || '',
                role: person.role || 'studio_member',
                workTask: person.workTask || '',
                pairingStatus: person.pairingStatus || 'unpaired'
        }
        showAddModal.value = true
}

const deletePerson = (id) => {
        if (confirm('确定要删除该人员吗？')) {
                personnelStore.deletePersonnel(id)
        }
}

const submitForm = () => {
        if (editingPersonId.value) {
                personnelStore.updatePersonnel(editingPersonId.value, formData.value)
        } else {
                personnelStore.addPersonnel(formData.value)
        }
        closeModal()
}

// 获取考勤状态文本
const getAttendanceStatusText = (status) => {
        switch (status) {
                case 'active':
                        return '出勤'
                case 'late':
                        return '迟到'
                case 'leave':
                        return '请假'
                case 'absent':
                        return '缺勤'
                case 'holiday':
                        return '假期'
                case 'excused':
                        return '公假'
                default:
                        return '未知'
        }
}
</script>

<style scoped>
.personnel-view {
        padding: 20px;
}

/* 头部搜索和操作栏 */
.personnel-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        flex-wrap: wrap;
        gap: 15px;
}

.search-filter {
        display: flex;
        gap: 10px;
        align-items: center;
        flex-wrap: wrap;
}

.search-input {
        padding: 8px 12px;
        border: 1px solid #ddd;
        border-radius: 4px;
        width: 250px;
        font-size: 0.9rem;
}

.filter-select {
        padding: 8px 12px;
        border: 1px solid #ddd;
        border-radius: 4px;
        font-size: 0.9rem;
        background: white;
}

.add-person-btn {
        padding: 8px 16px;
        background: #67c23a;
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 0.9rem;
        transition: background-color 0.2s;
}

.add-person-btn:hover {
        background: #85ce61;
}

/* 人员卡片网格 */
.personnel-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
        gap: 20px;
        margin-top: 20px;
}

.person-card {
        background: white;
        border-radius: 8px;
        padding: 20px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        transition: transform 0.2s;
        display: flex;
        flex-direction: column;
}

.person-card:hover {
        transform: translateY(-5px);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.person-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 15px;
}

.person-info h3 {
        margin: 0;
        font-size: 1.2rem;
        color: #333;
}

.status-container {
        display: flex;
        gap: 8px;
        flex-direction: column;
}

.attendance-status {
        padding: 4px 12px;
        border-radius: 12px;
        font-size: 0.8rem;
        font-weight: bold;
}

.attendance-status.active {
        background-color: #d4edda;
        color: #155724;
}

.attendance-status.late {
        background-color: #fff3cd;
        color: #856404;
}

.attendance-status.leave {
        background-color: #cce7ff;
        color: #004085;
}

.attendance-status.absent {
        background-color: #f8d7da;
        color: #721c24;
}

.attendance-status.holiday {
        background-color: #e2e3e5;
        color: #383d41;
}

.attendance-status.excused {
        background-color: #d1ecf1;
        color: #0c5460;
}

.pairing-status {
        padding: 4px 12px;
        border-radius: 12px;
        font-size: 0.8rem;
        font-weight: bold;
}

.pairing-status.paired {
        background-color: #d4edda;
        color: #155724;
}

.pairing-status.unpaired {
        background-color: #f5f5f5;
        color: #666;
        border: 1px dashed #999;
}

.person-details {
        flex: 1;
        margin-bottom: 15px;
}

.person-details p {
        margin: 8px 0;
        font-size: 0.9rem;
        color: #666;
}

/* 操作按钮 */
.person-actions {
        display: flex;
        gap: 10px;
        border-top: 1px solid #eee;
        padding-top: 15px;
}

.action-btn {
        flex: 1;
        padding: 6px 12px;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 0.85rem;
        transition: background-color 0.2s;
}

.edit-btn {
        background: #409eff;
        color: white;
}

.edit-btn:hover {
        background: #66b1ff;
}

.delete-btn {
        background: #f56c6c;
        color: white;
}

.delete-btn:hover {
        background: #f78989;
}

/* 模态框样式 */
.modal-overlay {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 1000;
}

.modal-content {
        background: white;
        border-radius: 8px;
        width: 90%;
        max-width: 500px;
        max-height: 90vh;
        overflow-y: auto;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 15px 20px;
        border-bottom: 1px solid #eee;
}

.modal-header h3 {
        margin: 0;
        color: #333;
}

.close-btn {
        background: none;
        border: none;
        font-size: 1.5rem;
        color: #999;
        cursor: pointer;
        padding: 0;
        line-height: 1;
}

.close-btn:hover {
        color: #333;
}

/* 表单样式 */
.person-form {
        padding: 20px;
}

.form-group {
        margin-bottom: 15px;
}

.form-row {
        display: flex;
        gap: 15px;
        flex-wrap: wrap;
}

.form-row .form-group {
        flex: 1;
        min-width: 200px;
}

.form-group label {
        display: block;
        margin-bottom: 5px;
        font-weight: bold;
        color: #333;
        font-size: 0.9rem;
}

.form-group input,
.form-group select {
        width: 100%;
        padding: 8px 12px;
        border: 1px solid #ddd;
        border-radius: 4px;
        font-size: 0.9rem;
}

.form-actions {
        display: flex;
        justify-content: flex-end;
        gap: 10px;
        margin-top: 20px;
        padding-top: 15px;
        border-top: 1px solid #eee;
}

.cancel-btn {
        padding: 8px 16px;
        background: #909399;
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 0.9rem;
        transition: background-color 0.2s;
}

.cancel-btn:hover {
        background: #a6a9ad;
}

.save-btn {
        padding: 8px 16px;
        background: #67c23a;
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 0.9rem;
        transition: background-color 0.2s;
}

.save-btn:hover {
        background: #85ce61;
}

/* 加载和错误状态样式 */
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

/* 响应式设计 */
@media (max-width: 768px) {
        .personnel-grid {
                grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
        }

        .search-input {
                width: 200px;
        }

        .personnel-header {
                flex-direction: column;
                align-items: stretch;
        }

        .search-filter {
                justify-content: stretch;
        }

        .search-input {
                flex: 1;
                min-width: 0;
        }
}

@media (max-width: 480px) {
        .personnel-grid {
                grid-template-columns: 1fr;
        }

        .form-row {
                flex-direction: column;
        }

        .form-row .form-group {
                min-width: auto;
        }
}
</style>