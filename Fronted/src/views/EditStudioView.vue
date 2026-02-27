<template>
	<div class="create-studio">
		<div class="page-header">
			<h2>编辑工作室</h2>
			<button @click="$router.push('/studios')" class="back-btn">
				返回列表
			</button>
		</div>

		<form @submit.prevent="submitForm" class="studio-form">
			<div class="form-section">
				<h3>工作室基本信息</h3>
				<div class="form-group">
					<label for="studio-name">工作室名称 *</label>
					<input id="studio-name" v-model="studioForm.studio_name" type="text"
						placeholder="输入工作室名称" required />
				</div>
				<div class="form-group">
					<label for="studio-code">工作室代码 *</label>
					<input id="studio-code" v-model="studioForm.studio_code" type="text"
						placeholder="输入工作室代码" required />
				</div>
				<div class="form-group">
					<label for="studio-description">工作室描述</label>
					<textarea id="studio-description" v-model="studioForm.description"
						placeholder="输入工作室描述（可选）" rows="3"></textarea>
				</div>
				<div class="form-group">
					<label for="studio-admin">工作室管理员 *</label>
					<select id="studio-admin" v-model="selectedAdmin" required>
	<option value="">请选择工作室管理员</option>
	<option v-for="person in personnel" :key="person.userId" :value="person.userNumber">
		{{ person.userName }} ({{ person.userNumber }})
	</option>
</select>
				</div>
			</div>

			<div class="form-section">
				<h3>选择人员</h3>
				<div class="personnel-selection">
					<div v-if="loadingPersonnel" class="loading">加载人员列表中...</div>
					<div v-else-if="personnel.length === 0" class="no-personnel">暂无可用人员</div>
					<div v-else class="personnel-list">
						<div v-for="person in personnel" :key="person.userId" class="personnel-item">
							<label :for="`person-${person.userId}`" class="personnel-checkbox">
								<input 
									type="checkbox" 
									:id="`person-${person.userId}`" 
									:value="person.userNumber"
									v-model="selectedPersonnel"
								/>
								<div class="personnel-info">
									<strong>{{ person.userName }}</strong>
									<span class="personnel-number">编号: {{ person.userNumber }}</span>
									<span v-if="person.attendanceStatus" class="personnel-status">状态: {{ person.attendanceStatus }}</span>
								</div>
							</label>
						</div>
						<div class="selected-personnel-info">
							已选择 {{ selectedPersonnel.length }} 个人员
						</div>
					</div>
				</div>
			</div>

			<div class="form-actions">
				<button type="button" @click="$router.push('/studios')" class="reset-btn">
					取消
				</button>
				<button type="submit" class="submit-btn" :disabled="submitting">
					{{ submitting ? '更新中...' : '更新工作室' }}
				</button>
			</div>
		</form>
	</div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStudioStore } from '../stores/studio'
import { usePersonnelStore } from '../stores/personnel'

const router = useRouter()
const route = useRoute()
const studioStore = useStudioStore()
const personnelStore = usePersonnelStore()

const studioId = computed(() => parseInt(route.params.id))
const submitting = ref(false)
const loadingPersonnel = ref(false)
const loadingStudio = ref(true)
const personnel = ref([])
const selectedPersonnel = ref([])
const selectedAdmin = ref('')

const studioForm = reactive({
	studio_name: '',
	studio_code: '',
	description: '',
	personnel: '',
	admin_name: '',
	admin_user_number: ''
})

onMounted(async () => {
	await Promise.all([
		loadStudioData(),
		loadPersonnel()
	])
})

const loadStudioData = async () => {
	loadingStudio.value = true
	try {
		// 先加载人员列表，以便后续根据用户ID查找学号
		await loadPersonnel()
		
		// 从store获取工作室详情
		const studio = studioStore.getStudioById(studioId.value)
		if (studio) {
			// 填充表单
			studioForm.studio_name = studio.name
			studioForm.studio_code = studio.code
			studioForm.description = studio.description
			studioForm.admin_name = studio.admin_name || ''
			studioForm.admin_user_number = studio.admin_user_number || ''
			selectedAdmin.value = studio.admin_user_number || ''
			
			// 初始化选中的人员
			if (studio.personnel && typeof studio.personnel === 'string') {
				const userIds = studio.personnel.split(',').map(p => p.trim())
				// 根据用户ID查找对应的学号
				selectedPersonnel.value = userIds.map(userId => {
					const person = personnel.value.find(p => p.userId == userId)
					return person ? person.userNumber : ''
				}).filter(Boolean)
			}
		} else {
			// 如果store中没有，尝试从后端获取
			try {
				const response = await fetch(`/api/studios/${studioId.value}`)
				const studioData = await response.json()
				// 填充表单
				studioForm.studio_name = studioData.studio_name || studioData.name
				studioForm.studio_code = studioData.studio_code || studioData.code
				studioForm.description = studioData.description || ''
				studioForm.admin_name = studioData.admin_name || ''
				studioForm.admin_user_number = studioData.admin_user_number || ''
				selectedAdmin.value = studioData.admin_user_number || ''
				
				// 初始化选中的人员
				if (studioData.personnels) {
					const userIds = studioData.personnels.split(',').map(p => p.trim())
					// 根据用户ID查找对应的学号
					selectedPersonnel.value = userIds.map(userId => {
						const person = personnel.value.find(p => p.userId == userId)
						return person ? person.userNumber : ''
					}).filter(Boolean)
				}
			} catch (error) {
				console.error('从后端获取工作室数据失败:', error)
			}
		}
	} catch (error) {
		console.error('加载工作室数据失败:', error)
		alert('加载工作室数据失败: ' + error.message)
	} finally {
		loadingStudio.value = false
	}
}

const loadPersonnel = async () => {
	loadingPersonnel.value = true
	try {
		await personnelStore.fetchPersonnelList()
		personnel.value = personnelStore.getPersonnelList || []
	} catch (error) {
		console.error('加载人员列表失败:', error)
	} finally {
		loadingPersonnel.value = false
	}
}

const submitForm = async () => {
	submitting.value = true
	try {
		if (selectedPersonnel.value.length === 0) {
			alert('请至少选择一个人员')
			return
		}
		
		if (!selectedAdmin.value) {
			alert('请选择工作室管理员')
			return
		}
		
		// 获取选中的管理员姓名
		const adminPerson = personnel.value.find(p => p.userNumber === selectedAdmin.value)
		const adminName = adminPerson ? adminPerson.userName : ''
		
		const submitData = {
			...studioForm,
			personnels: selectedPersonnel.value.join(','),
			admin_name: adminName,
			admin_user_number: selectedAdmin.value
		}

		console.log('提交工作室数据:', submitData)
		
		await studioStore.updateStudio(studioId.value, submitData)
		alert('工作室更新成功')
		router.push('/studios')
	} catch (error) {
		alert('更新工作室失败: ' + error.message)
	} finally {
		submitting.value = false
	}
}
</script>

<style scoped>
.create-studio {
	padding: 20px;
	max-width: 800px;
	margin: 0 auto;
}

.page-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30px;
	padding-bottom: 20px;
	border-bottom: 2px solid #e0e0e0;
}

.page-header h2 {
	margin: 0;
	color: #333;
	font-size: 1.8rem;
}

.back-btn {
	padding: 8px 16px;
	background: #909399;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.9rem;
	transition: background-color 0.2s;
}

.back-btn:hover {
	background: #a6a9ad;
}

.studio-form {
	background: white;
	border-radius: 8px;
	padding: 30px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-section {
	margin-bottom: 30px;
}

.form-section h3 {
	margin: 0 0 20px 0;
	color: #333;
	font-size: 1.3rem;
	padding-bottom: 10px;
	border-bottom: 2px solid #409eff;
}

.form-group {
	margin-bottom: 20px;
}

.form-group label {
	display: block;
	margin-bottom: 8px;
	font-weight: bold;
	color: #333;
	font-size: 0.95rem;
}

.form-group input,
.form-group textarea,
.form-group select {
	width: 100%;
	padding: 10px 12px;
	border: 1px solid #ddd;
	border-radius: 4px;
	font-size: 0.95rem;
	transition: border-color 0.2s;
	box-sizing: border-box;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
	outline: none;
	border-color: #409eff;
}

.form-group textarea {
	resize: vertical;
	min-height: 80px;
}

.form-actions {
	display: flex;
	justify-content: flex-end;
	gap: 15px;
	margin-top: 30px;
	padding-top: 20px;
	border-top: 1px solid #e0e0e0;
}

.reset-btn {
	padding: 10px 24px;
	background: #909399;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.95rem;
	transition: background-color 0.2s;
}

.reset-btn:hover {
	background: #a6a9ad;
}

.submit-btn {
	padding: 10px 24px;
	background: #67c23a;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.95rem;
	transition: background-color 0.2s;
}

.submit-btn:hover:not(:disabled) {
	background: #85ce61;
}

.submit-btn:disabled {
	background: #c0c4cc;
	cursor: not-allowed;
}

.personnel-selection {
	min-height: 200px;
}

.loading,
.no-personnel {
	text-align: center;
	color: #909399;
	padding: 40px 0;
}

.personnel-list {
	display: flex;
	flex-direction: column;
	gap: 12px;
}

.personnel-item {
	padding: 12px 16px;
	border: 1px solid #e0e0e0;
	border-radius: 6px;
	transition: all 0.2s;
}

.personnel-item:hover {
	border-color: #409eff;
	background: #f5f7fa;
}

.personnel-checkbox {
	display: flex;
	align-items: center;
	gap: 12px;
	cursor: pointer;
}

.personnel-checkbox input[type="checkbox"] {
	width: 18px;
	height: 18px;
	cursor: pointer;
}

.personnel-info {
	display: flex;
	align-items: center;
	gap: 12px;
	flex: 1;
}

.personnel-info strong {
	color: #333;
	font-size: 1rem;
}

.personnel-number {
	color: #909399;
	font-size: 0.85rem;
}

.personnel-status {
	color: #67c23a;
	font-size: 0.85rem;
	background: #f0f9ff;
	padding: 2px 8px;
	border-radius: 3px;
}

.selected-personnel-info {
	margin-top: 15px;
	padding: 12px;
	background-color: #ecf5ff;
	border: 1px solid #d9ecff;
	border-radius: 4px;
	font-size: 0.9rem;
	color: #409eff;
	text-align: center;
	font-weight: 500;
}

@media (max-width: 768px) {
	.create-studio {
		padding: 10px;
	}
	
	.studio-form {
		padding: 20px;
	}
	
	.page-header {
		flex-direction: column;
		align-items: flex-start;
		gap: 15px;
	}
	
	.form-actions {
		flex-direction: column;
	}
	
	.reset-btn,
	.submit-btn {
		width: 100%;
	}
}
</style>