<template>
	<div class="studio-management">
		<div class="page-header">
			<h2>工作室管理</h2>
			<button @click="$router.push('/studios/create')" class="create-studio-btn">
				创建工作室
			</button>
		</div>

		<!-- 加载状态 -->
		<div v-if="loading" class="loading-container">
			<div class="loading-spinner">加载中...</div>
		</div>

		<!-- 错误提示 -->
		<div v-else-if="error" class="error-container">
			<div class="error-message">{{ error }}</div>
			<button @click="fetchStudios" class="retry-btn">重试</button>
		</div>

		<!-- 工作室列表 -->
		<div v-else class="studio-list">
			<div 
				v-for="studio in studioList" 
				:key="studio.id"
				class="studio-card"
				:class="{ 'new-studio': studio.isNew }"
			>
				<div class="studio-header">
					<h3 class="studio-name">{{ studio.name || studio.studioName }}</h3>
					<span class="studio-code">工作室代码: {{ studio.code || studio.studioCode }}</span>
				</div>

				<div class="studio-info">
					<div class="info-item">
						<span class="info-label">当前在勤人数:</span>
						<span class="info-value">{{ studio.presentCount }}/{{ studio.totalCount }}</span>
					</div>
					<div class="info-item">
						<span class="info-label">管理员姓名:</span>
						<span class="info-value">{{ studio.adminName }}</span>
					</div>
					<div class="info-item">
						<span class="info-label">管理员联系方式:</span>
						<span class="info-value">{{ studio.adminContact }}</span>
					</div>
				</div>

				<div class="studio-actions">
					<button @click="showAttendanceInfo(studio)" class="action-btn" :disabled="loadingAttendance">
						{{ loadingAttendance ? '加载中...' : '查看考勤信息' }}
					</button>
					<button @click="editStudio(studio)" class="action-btn">
						编辑工作室
					</button>
					<button @click="deleteStudio(studio)" class="action-btn delete-btn">
						删除工作室
					</button>
				</div>

				<!-- 二级菜单：考勤信息 -->
				<div v-if="activeStudio === studio.id" class="studio-attendance">
					<h4>考勤详情</h4>
					<div class="attendance-info">
						<div class="absent-list">
							<h5>缺勤人员</h5>
							<ul>
								<li v-for="person in studio.absentPersons" :key="person.id">
									{{ person.name }}
								</li>
								<li v-if="studio.absentPersons.length === 0">
									无缺勤人员
								</li>
							</ul>
						</div>
						<div class="present-list">
							<h5>在勤人员</h5>
							<ul>
								<li v-for="person in studio.presentPersons" :key="person.id">
									{{ person.name }}
								</li>
								<li v-if="studio.presentPersons.length === 0">
									无在勤人员
								</li>
							</ul>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 空状态 -->
		<div v-if="!loading && !error && studioList.length === 0" class="empty-state">
			<p>暂无工作室数据</p>
		</div>
	</div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStudioStore } from '../stores/studio'

const router = useRouter()
const studioStore = useStudioStore()

const studioList = computed(() => studioStore.studioList)
const loading = computed(() => studioStore.loading)
const error = computed(() => studioStore.error)

const activeStudio = ref(null)
const loadingAttendance = ref(false)

onMounted(() => {
	fetchStudios()
})

const fetchStudios = async () => {
	await studioStore.fetchStudioList()
}

const showAttendanceInfo = async (studio) => {
	if (activeStudio.value === studio.id) {
		activeStudio.value = null
	} else {
		activeStudio.value = studio.id
		loadingAttendance.value = true
		try {
			await studioStore.fetchStudioAttendance(studio.id)
		} catch (error) {
			alert('获取考勤信息失败: ' + error.message)
		} finally {
			loadingAttendance.value = false
		}
	}
}

const editStudio = (studio) => {
	router.push(`/studios/edit/${studio.id}`)
}

const deleteStudio = async (studio) => {
	if (confirm(`确定要删除工作室 "${studio.name}" 吗？此操作不可撤销。`)) {
		try {
			await studioStore.deleteStudio(studio.id)
			alert('工作室删除成功')
			// 不需要重新获取列表，store已经更新
		} catch (error) {
			alert('删除工作室失败: ' + error.message)
		}
	}
}
</script>

<style scoped>
.studio-management {
	padding: 20px;
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

.page-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30px;
}

.page-header h2 {
	margin: 0;
	color: #333;
}

.create-studio-btn {
	padding: 10px 24px;
	background: #67c23a;
	color: white;
	border: none;
	border-radius: 4px;
	font-size: 1rem;
	cursor: pointer;
	transition: background-color 0.2s;
}

.create-studio-btn:hover {
	background: #85ce61;
}

.studio-list {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
	gap: 20px;
}

.studio-card {
	background: white;
	border-radius: 8px;
	box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
	padding: 20px;
	transition: transform 0.2s, box-shadow 0.2s;
}

.studio-card:hover {
	transform: translateY(-5px);
	box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.studio-card.new-studio {
	border: 2px dashed #409eff;
	background: #f0f9ff;
}

.studio-header {
	margin-bottom: 15px;
	padding-bottom: 10px;
	border-bottom: 1px solid #e0e0e0;
}

.studio-name {
	margin: 0 0 8px 0;
	color: #333;
	font-size: 1.2rem;
}

.studio-code {
	display: inline-block;
	padding: 4px 12px;
	background: #f5f7fa;
	border-radius: 12px;
	font-size: 0.8rem;
	color: #606266;
}

.studio-info {
	margin-bottom: 20px;
}

.info-item {
	margin-bottom: 10px;
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.info-label {
	font-weight: 500;
	color: #606266;
}

.info-value {
	color: #303133;
}

.studio-actions {
	display: flex;
	gap: 10px;
	margin-bottom: 15px;
}

.action-btn {
	flex: 1;
	padding: 8px 16px;
	background: #409eff;
	color: white;
	border: none;
	border-radius: 4px;
	font-size: 0.9rem;
	cursor: pointer;
	transition: background-color 0.2s;
}

.action-btn:hover {
	background: #66b1ff;
}

.action-btn.delete-btn {
	background: #f56c6c;
}
.action-btn.delete-btn:hover {
	background: #f78989;
}

.studio-attendance {
	margin-top: 15px;
	padding-top: 15px;
	border-top: 1px solid #e0e0e0;
}

.studio-attendance h4 {
	margin: 0 0 15px 0;
	color: #333;
}

.attendance-info {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 15px;
}

.absent-list h5,
.present-list h5 {
	margin: 0 0 10px 0;
	color: #606266;
	font-size: 0.9rem;
}

.absent-list ul,
.present-list ul {
	margin: 0;
	padding-left: 20px;
}

.absent-list li,
.present-list li {
	color: #303133;
	margin-bottom: 5px;
}

@media (max-width: 768px) {
	.studio-list {
		grid-template-columns: 1fr;
	}

	.attendance-info {
		grid-template-columns: 1fr;
	}

	.page-header {
		flex-direction: column;
		align-items: flex-start;
		gap: 15px;
	}
}
</style>