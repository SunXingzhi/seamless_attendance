<template>
	<div class="assign-view">
	<h2>任务分配</h2>
	<div class="assign-actions">
		<button @click="showAssignModal = true" class="assign-btn">分配新任务</button>
	</div>
	
	<div class="task-list">
		<div v-for="task in tasks" :key="task.id" class="task-item">
		<div class="task-info">
			<h3>{{ task.title }}</h3>
			<p class="task-description">{{ task.description }}</p>
			<div class="task-meta">
			<span class="priority" :class="task.priority">
				{{ task.priority === 'high' ? '高' : task.priority === 'medium' ? '中' : '低' }}
			</span>
			<span class="deadline">截止日期: {{ task.deadline }}</span>
			</div>
		</div>
		<div class="task-assignee">
			<label>负责人:</label>
			<select v-model="task.assigneeId" @change="updateTaskAssignee(task)">
			<option value="">未分配</option>
			<option v-for="person in personnel" :key="person.userId" :value="person.userId">
				{{ person.userName }}
			</option>
			</select>
		</div>
		<div class="task-actions">
			<button @click="editTask(task)" class="action-btn edit-btn">编辑</button>
			<button @click="deleteTask(task.id)" class="action-btn delete-btn">删除</button>
		</div>
		</div>
	</div>
	
	<div v-if="showAssignModal" class="modal-overlay" @click.self="closeModal">
		<div class="modal-content">
		<div class="modal-header">
			<h3>{{ editingTaskId ? '编辑任务' : '分配新任务' }}</h3>
			<button @click="closeModal" class="close-btn">&times;</button>
		</div>
		<form @submit.prevent="submitTask" class="task-form">
			<div class="form-group">
			<label>任务标题:</label>
			<input type="text" v-model="taskForm.title" required />
			</div>
			<div class="form-group">
			<label>任务描述:</label>
			<textarea v-model="taskForm.description" rows="3" required></textarea>
			</div>
			<div class="form-row">
			<div class="form-group">
				<label>优先级:</label>
				<select v-model="taskForm.priority">
				<option value="high">高</option>
				<option value="medium">中</option>
				<option value="low">低</option>
				</select>
			</div>
			<div class="form-group">
				<label>截止日期:</label>
				<input type="date" v-model="taskForm.deadline" required />
			</div>
			</div>
			<div class="form-group">
			<label>负责人:</label>
			<select v-model="taskForm.assigneeId">
				<option value="">未分配</option>
				<option v-for="person in personnel" :key="person.userId" :value="person.userId">
				{{ person.userName }}
				</option>
			</select>
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
import { ref, computed } from 'vue'
import { usePersonnelStore } from '../stores/personnel'

const personnelStore = usePersonnelStore()
const personnel = computed(() => personnelStore.personnelList)

const showAssignModal = ref(false)
const editingTaskId = ref(null)
const taskForm = ref({
	title: '',
	description: '',
	priority: 'medium',
	deadline: '',
	assigneeId: ''
})

const tasks = ref([
	{
	id: 1,
	title: '硬件设计',
	description: '设计新的考勤设备硬件电路',
	priority: 'high',
	deadline: '2026-01-15',
	assigneeId: ''
	},
	{
	id: 2,
	title: '软件测试',
	description: '对考勤系统进行全面测试',
	priority: 'medium',
	deadline: '2026-01-20',
	assigneeId: 2
	},
	{
	id: 3,
	title: '文档编写',
	description: '编写系统使用手册和技术文档',
	priority: 'low',
	deadline: '2026-01-25',
	assigneeId: 3
	}
])

const closeModal = () => {
	showAssignModal.value = false
	editingTaskId.value = null
	resetForm()
}

const resetForm = () => {
	taskForm.value = {
	title: '',
	description: '',
	priority: 'medium',
	deadline: '',
	assigneeId: ''
	}
}

const editTask = (task) => {
	editingTaskId.value = task.id
	taskForm.value = { ...task }
	showAssignModal.value = true
}

const deleteTask = (id) => {
	if (confirm('确定要删除该任务吗？')) {
	tasks.value = tasks.value.filter(task => task.id !== id)
	}
}

const updateTaskAssignee = (task) => {
	console.log('更新任务负责人:', task.id, task.assigneeId)
}

const submitTask = () => {
	if (editingTaskId.value) {
	const index = tasks.value.findIndex(task => task.id === editingTaskId.value)
	if (index !== -1) {
		tasks.value[index] = { ...taskForm.value, id: editingTaskId.value }
	}
	} else {
	tasks.value.push({
		...taskForm.value,
		id: Date.now()
	})
	}
	closeModal()
}
</script>

<style scoped>
.assign-view {
	padding: 20px;
}

.assign-actions {
	margin-bottom: 20px;
}

.assign-btn {
	padding: 10px 20px;
	background: #67c23a;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.9rem;
	transition: background-color 0.2s;
}

.assign-btn:hover {
	background: #85ce61;
}

.task-list {
	display: flex;
	flex-direction: column;
	gap: 15px;
}

.task-item {
	background: white;
	border-radius: 8px;
	padding: 20px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
	display: flex;
	gap: 20px;
}

.task-info {
	flex: 1;
}

.task-info h3 {
	margin: 0 0 10px 0;
	color: #333;
	font-size: 1.1rem;
}

.task-description {
	margin: 5px 0 10px 0;
	color: #666;
	font-size: 0.9rem;
}

.task-meta {
	display: flex;
	gap: 15px;
	align-items: center;
}

.priority {
	padding: 4px 12px;
	border-radius: 12px;
	font-size: 0.8rem;
	font-weight: bold;
	color: white;
}

.priority.high {
	background: #f56c6c;
}

.priority.medium {
	background: #e6a23c;
}

.priority.low {
	background: #67c23a;
}

.deadline {
	color: #999;
	font-size: 0.85rem;
}

.task-assignee {
	display: flex;
	flex-direction: column;
	gap: 5px;
	min-width: 200px;
}

.task-assignee label {
	font-weight: bold;
	color: #333;
	font-size: 0.9rem;
}

.task-assignee select {
	padding: 8px 12px;
	border: 1px solid #ddd;
	border-radius: 4px;
	font-size: 0.9rem;
}

.task-actions {
	display: flex;
	flex-direction: column;
	gap: 10px;
}

.action-btn {
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

.task-form {
	padding: 20px;
}

.form-group {
	margin-bottom: 15px;
}

.form-row {
	display: flex;
	gap: 15px;
}

.form-row .form-group {
	flex: 1;
}

.form-group label {
	display: block;
	margin-bottom: 5px;
	font-weight: bold;
	color: #333;
	font-size: 0.9rem;
}

.form-group input,
.form-group select,
.form-group textarea {
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

@media (max-width: 768px) {
	.task-item {
	flex-direction: column;
	}
	
	.task-assignee {
	min-width: auto;
	}
	
	.task-actions {
	flex-direction: row;
	}
	
	.form-row {
	flex-direction: column;
	}
}
</style>
