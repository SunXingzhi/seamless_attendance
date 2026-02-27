<template>
	<div class="settings-view">
	<h2>设置</h2>
	<div class="settings-container">
		<div class="settings-section">
		<h3>系统设置</h3>
		<div class="setting-item">
			<label>工作室名称:</label>
			<input type="text" v-model="studioName" placeholder="输入工作室名称" />
		</div>
		<div class="setting-item">
			<label>管理员邮箱:</label>
			<input type="email" v-model="adminEmail" placeholder="输入管理员邮箱" />
		</div>
		<div class="setting-item">
			<label>考勤时间:</label>
			<div class="time-settings">
			<div>
				<span>上班时间:</span>
				<input type="time" v-model="workStartTime" />
			</div>
			<div>
				<span>下班时间:</span>
				<input type="time" v-model="workEndTime" />
			</div>
			</div>
		</div>
		</div>
		
		<div class="settings-section">
		<h3>设备管理</h3>
		<div class="device-list">
			<div class="device-item">
			<div class="device-info">
				<h4>考勤设备 #001</h4>
				<p>IP地址: 192.168.1.100</p>
				<p>状态: <span class="status active">在线</span></p>
			</div>
			<button class="device-btn">重新连接</button>
			</div>
		</div>
		<button class="add-device-btn">添加设备</button>
		</div>
		
		<div class="settings-section">
		<h3>用户权限</h3>
		<table class="permission-table">
			<thead>
			<tr>
				<th>用户名</th>
				<th>角色</th>
				<th>权限</th>
				<th>操作</th>
			</tr>
			</thead>
			<tbody>
			<tr>
				<td>张三</td>
				<td>工作室管理员</td>
				<td>全部权限</td>
				<td><button class="edit-btn">编辑</button></td>
			</tr>
			<tr>
				<td>李四</td>
				<td>工作室员工</td>
				<td>查看权限</td>
				<td><button class="edit-btn">编辑</button></td>
			</tr>
			</tbody>
		</table>
		</div>
		
		<button class="save-settings-btn">保存设置</button>
	</div>
	</div>
</template>

<script setup>
import { ref } from 'vue'

const studioName = ref('示例工作室')
const adminEmail = ref('admin@example.com')
const workStartTime = ref('09:00')
const workEndTime = ref('18:00')
</script>

<style scoped>
.settings-view {
	padding: 20px;
}

.settings-container {
	margin-top: 20px;
}

.settings-section {
	background: white;
	border-radius: 8px;
	padding: 20px;
	margin-bottom: 20px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.settings-section h3 {
	margin-top: 0;
	color: #333;
	font-size: 1.1rem;
	border-bottom: 1px solid #e0e0e0;
	padding-bottom: 10px;
	margin-bottom: 20px;
}

.setting-item {
	margin-bottom: 15px;
}

.setting-item label {
	display: block;
	margin-bottom: 5px;
	font-weight: bold;
	color: #666;
}

.setting-item input {
	width: 100%;
	padding: 8px 12px;
	border: 1px solid #ddd;
	border-radius: 4px;
	font-size: 0.9rem;
}

.time-settings {
	display: flex;
	gap: 20px;
	flex-wrap: wrap;
}

.time-settings div {
	display: flex;
	align-items: center;
	gap: 10px;
}

.device-list {
	margin-bottom: 15px;
}

.device-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 15px;
	background: #f9f9f9;
	border-radius: 6px;
	margin-bottom: 10px;
}

.device-info h4 {
	margin: 0 0 5px 0;
	color: #333;
}

.device-info p {
	margin: 3px 0;
	color: #666;
	font-size: 0.9rem;
}

.status {
	padding: 2px 8px;
	border-radius: 10px;
	font-size: 0.8rem;
	font-weight: bold;
}

.status.active {
	background: #d4edda;
	color: #155724;
}

.status.absent {
	background: #f8d7da;
	color: #721c24;
}

.device-btn {
	padding: 6px 12px;
	background: #409eff;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.8rem;
}

.add-device-btn {
	padding: 8px 16px;
	background: #67c23a;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.9rem;
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

.edit-btn {
	padding: 6px 12px;
	background: #e6a23c;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.8rem;
}

.save-settings-btn {
	padding: 10px 24px;
	background: #409eff;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 1rem;
	margin-top: 20px;
	width: 100%;
}

@media (max-width: 768px) {
	.time-settings {
	flex-direction: column;
	gap: 10px;
	}
	
	.device-item {
	flex-direction: column;
	align-items: flex-start;
	gap: 10px;
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