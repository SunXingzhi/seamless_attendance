<template>
	<div class="permissions-view">
	<h2>权限管理</h2>
	<div class="permission-actions">
		<button @click="showAddUserModal = true" class="add-user-btn">添加用户</button>
	</div>
	
	<div class="permission-table-container">
		<table class="permission-table">
		<thead>
			<tr>
			<th>用户名</th>
			<th>角色</th>
			<th>权限</th>
			<th>创建时间</th>
			<th>操作</th>
			</tr>
		</thead>
		<tbody>
			<tr v-for="user in users" :key="user.id">
			<td>{{ user.userName }}</td>
			<td>
				<span class="role-badge" :class="user.role">
				{{ user.role === 'studio_admin' ? '工作室管理员' : '工作室员工' }}
				</span>
			</td>
			<td>
				<div class="permission-tags">
				<span v-for="perm in user.permissions" :key="perm" class="permission-tag">
					{{ getPermissionText(perm) }}
				</span>
				</div>
			</td>
			<td>{{ user.createdAt }}</td>
			<td>
				<button @click="editUser(user)" class="action-btn edit-btn">编辑</button>
				<button @click="deleteUser(user.id)" class="action-btn delete-btn">删除</button>
			</td>
			</tr>
		</tbody>
		</table>
	</div>
	
	<div class="permission-roles">
		<h3>角色权限配置</h3>
		<div class="role-config">
		<div class="role-card">
			<h4>工作室管理员</h4>
			<div class="role-permissions">
			<label v-for="perm in allPermissions" :key="perm.value">
				<input 
				type="checkbox" 
				:checked="adminPermissions.includes(perm.value)"
				@change="toggleAdminPermission(perm.value)"
				/>
				{{ perm.label }}
			</label>
			</div>
		</div>
		<div class="role-card">
			<h4>工作室员工</h4>
			<div class="role-permissions">
			<label v-for="perm in allPermissions" :key="perm.value">
				<input 
				type="checkbox" 
				:checked="memberPermissions.includes(perm.value)"
				@change="toggleMemberPermission(perm.value)"
				/>
				{{ perm.label }}
			</label>
			</div>
		</div>
		</div>
	</div>
	
	<div v-if="showAddUserModal" class="modal-overlay" @click.self="closeModal">
		<div class="modal-content">
		<div class="modal-header">
			<h3>{{ editingUserId ? '编辑用户' : '添加用户' }}</h3>
			<button @click="closeModal" class="close-btn">&times;</button>
		</div>
		<form @submit.prevent="submitUser" class="user-form">
			<div class="form-group">
			<label>用户名:</label>
			<input type="text" v-model="userForm.userName" required />
			</div>
			<div class="form-group">
			<label>角色:</label>
			<select v-model="userForm.role" required>
				<option value="studio_admin">工作室管理员</option>
				<option value="studio_member">工作室员工</option>
			</select>
			</div>
			<div class="form-group">
			<label>权限:</label>
			<div class="permission-checkboxes">
				<label v-for="perm in allPermissions" :key="perm.value">
				<input type="checkbox" v-model="userForm.permissions" :value="perm.value" />
				{{ perm.label }}
				</label>
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
import { ref } from 'vue'

const showAddUserModal = ref(false)
const editingUserId = ref(null)
const userForm = ref({
	userName: '',
	role: 'studio_member',
	permissions: []
})

const allPermissions = [
	{ value: 'view_personnel', label: '查看人员' },
	{ value: 'edit_personnel', label: '编辑人员' },
	{ value: 'delete_personnel', label: '删除人员' },
	{ value: 'view_attendance', label: '查看考勤' },
	{ value: 'edit_attendance', label: '编辑考勤' },
	{ value: 'view_statistics', label: '查看统计' },
	{ value: 'view_tasks', label: '查看任务' },
	{ value: 'edit_tasks', label: '编辑任务' },
	{ value: 'view_settings', label: '查看设置' },
	{ value: 'edit_settings', label: '编辑设置' }
]

const adminPermissions = ref([
	'view_personnel',
	'edit_personnel',
	'delete_personnel',
	'view_attendance',
	'edit_attendance',
	'view_statistics',
	'view_tasks',
	'edit_tasks',
	'view_settings',
	'edit_settings'
])

const memberPermissions = ref([
	'view_personnel',
	'view_attendance',
	'view_statistics',
	'view_tasks'
])

const users = ref([
	{
	id: 1,
	userName: '张三',
	role: 'studio_admin',
	permissions: [
		'view_personnel',
		'edit_personnel',
		'delete_personnel',
		'view_attendance',
		'edit_attendance',
		'view_statistics',
		'view_tasks',
		'edit_tasks',
		'view_settings',
		'edit_settings'
	],
	createdAt: '2026-01-01'
	},
	{
	id: 2,
	userName: '李四',
	role: 'studio_member',
	permissions: [
		'view_personnel',
		'view_attendance',
		'view_statistics',
		'view_tasks'
	],
	createdAt: '2026-01-05'
	},
	{
	id: 3,
	userName: '王五',
	role: 'studio_member',
	permissions: [
		'view_personnel',
		'view_attendance',
		'view_statistics',
		'view_tasks'
	],
	createdAt: '2026-01-08'
	}
])

const getPermissionText = (perm) => {
	const permission = allPermissions.find(p => p.value === perm)
	return permission ? permission.label : perm
}

const closeModal = () => {
	showAddUserModal.value = false
	editingUserId.value = null
	resetForm()
}

const resetForm = () => {
	userForm.value = {
	userName: '',
	role: 'studio_member',
	permissions: []
	}
}

const editUser = (user) => {
	editingUserId.value = user.id
	userForm.value = {
	userName: user.userName,
	role: user.role,
	permissions: [...user.permissions]
	}
	showAddUserModal.value = true
}

const deleteUser = (id) => {
	if (confirm('确定要删除该用户吗？')) {
	users.value = users.value.filter(user => user.id !== id)
	}
}

const toggleAdminPermission = (perm) => {
	const index = adminPermissions.value.indexOf(perm)
	if (index > -1) {
	adminPermissions.value.splice(index, 1)
	} else {
	adminPermissions.value.push(perm)
	}
}

const toggleMemberPermission = (perm) => {
	const index = memberPermissions.value.indexOf(perm)
	if (index > -1) {
	memberPermissions.value.splice(index, 1)
	} else {
	memberPermissions.value.push(perm)
	}
}

const submitUser = () => {
	if (editingUserId.value) {
	const index = users.value.findIndex(user => user.id === editingUserId.value)
	if (index !== -1) {
		users.value[index] = {
		...users.value[index],
		...userForm.value,
		createdAt: users.value[index].createdAt
		}
	}
	} else {
	users.value.push({
		id: Date.now(),
		...userForm.value,
		createdAt: new Date().toISOString().split('T')[0]
	})
	}
	closeModal()
}
</script>

<style scoped>
.permissions-view {
	padding: 20px;
}

.permission-actions {
	margin-bottom: 20px;
}

.add-user-btn {
	padding: 10px 20px;
	background: #67c23a;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.9rem;
	transition: background-color 0.2s;
}

.add-user-btn:hover {
	background: #85ce61;
}

.permission-table-container {
	background: white;
	border-radius: 8px;
	padding: 20px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
	margin-bottom: 30px;
}

.permission-table {
	width: 100%;
	border-collapse: collapse;
}

.permission-table th,
.permission-table td {
	padding: 12px;
	text-align: left;
	border-bottom: 1px solid #e0e0e0;
}

.permission-table th {
	background: #f5f7fa;
	font-weight: bold;
	color: #333;
}

.permission-table tbody tr:hover {
	background: #f9f9f9;
}

.role-badge {
	padding: 4px 12px;
	border-radius: 12px;
	font-size: 0.8rem;
	font-weight: bold;
}

.role-badge.studio_admin {
	background: #409eff;
	color: white;
}

.role-badge.studio_member {
	background: #67c23a;
	color: white;
}

.permission-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 5px;
}

.permission-tag {
	padding: 2px 8px;
	background: #e6f7ff;
	color: #1890ff;
	border-radius: 4px;
	font-size: 0.8rem;
}

.action-btn {
	padding: 6px 12px;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.85rem;
	transition: background-color 0.2s;
	margin-right: 5px;
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

.permission-roles {
	background: white;
	border-radius: 8px;
	padding: 20px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.permission-roles h3 {
	margin-top: 0;
	color: #333;
	font-size: 1.1rem;
	margin-bottom: 20px;
}

.role-config {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
	gap: 20px;
}

.role-card {
	background: #f5f7fa;
	border-radius: 8px;
	padding: 20px;
}

.role-card h4 {
	margin-top: 0;
	color: #333;
	margin-bottom: 15px;
	padding-bottom: 10px;
	border-bottom: 1px solid #e0e0e0;
}

.role-permissions {
	display: flex;
	flex-direction: column;
	gap: 10px;
}

.role-permissions label {
	display: flex;
	align-items: center;
	gap: 8px;
	cursor: pointer;
}

.role-permissions input[type="checkbox"] {
	width: auto;
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

.user-form {
	padding: 20px;
}

.form-group {
	margin-bottom: 15px;
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

.permission-checkboxes {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 10px;
	max-height: 200px;
	overflow-y: auto;
	padding: 10px;
	background: #f5f7fa;
	border-radius: 4px;
}

.permission-checkboxes label {
	display: flex;
	align-items: center;
	gap: 5px;
	cursor: pointer;
	font-size: 0.85rem;
}

.permission-checkboxes input[type="checkbox"] {
	width: auto;
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
	.role-config {
	grid-template-columns: 1fr;
	}
	
	.permission-checkboxes {
	grid-template-columns: 1fr;
	}
	
	.permission-table {
	font-size: 0.8rem;
	}
	
	.permission-table th,
	.permission-table td {
	padding: 8px;
	}
}
</style>
