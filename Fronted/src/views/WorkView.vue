<template>
	<div class="work-view">
	<h2>工作任务</h2>
	<div class="work-container">
		<div class="task-board">
		<div class="task-column">
			<h3>待分配</h3>
			<div class="task-card">
			<div class="task-header">
				<h4>硬件设计</h4>
				<span class="task-priority high">高</span>
			</div>
			<p class="task-description">设计新的考勤设备硬件电路</p>
			<div class="task-meta">
				<span>截止日期: 2026-01-15</span>
				<span>负责人: 无</span>
			</div>
			</div>
		</div>
		<div class="task-column">
			<h3>进行中</h3>
			<div class="task-card">
			<div class="task-header">
				<h4>软件设计</h4>
				<span class="task-priority medium">中</span>
			</div>
			<p class="task-description">开发考勤系统前端界面</p>
			<div class="task-meta">
				<span>截止日期: 2026-01-20</span>
				<span>负责人: 张三</span>
			</div>
			</div>
		</div>
		<div class="task-column">
			<h3>已完成</h3>
			<div class="task-card">
			<div class="task-header">
				<h4>数据库设计</h4>
				<span class="task-priority low">低</span>
			</div>
			<p class="task-description">设计考勤系统数据库表结构</p>
			<div class="task-meta">
				<span>截止日期: 2026-01-10</span>
				<span>负责人: 李四</span>
			</div>
			</div>
		</div>
		</div>
	</div>
	</div>
</template>

<script setup>
</script>

<style scoped>
.work-view {
	padding: 20px;
}

.work-container {
	margin-top: 20px;
}

.task-board {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 20px;
	background: #f5f5f5;
	padding: 20px;
	border-radius: 8px;
}

.task-column {
	background: white;
	border-radius: 8px;
	padding: 15px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.task-column h3 {
	margin-top: 0;
	color: #333;
	font-size: 1.1rem;
	border-bottom: 2px solid #e0e0e0;
	padding-bottom: 10px;
}

.task-card {
	background: #f9f9f9;
	border-radius: 6px;
	padding: 15px;
	margin: 10px 0;
	box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
	cursor: pointer;
	transition: transform 0.2s;
}

.task-card:hover {
	transform: translateY(-2px);
	box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
}

.task-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 8px;
}

.task-header h4 {
	margin: 0;
	font-size: 1rem;
	color: #333;
}

.task-priority {
	padding: 3px 8px;
	border-radius: 12px;
	font-size: 0.7rem;
	font-weight: bold;
	color: white;
}

.task-priority.high {
	background: #f56c6c;
}

.task-priority.medium {
	background: #e6a23c;
}

.task-priority.low {
	background: #67c23a;
}

.task-description {
	margin: 8px 0;
	color: #666;
	font-size: 0.9rem;
	line-height: 1.4;
}

.task-meta {
	display: flex;
	flex-direction: column;
	gap: 4px;
	margin-top: 10px;
	font-size: 0.8rem;
	color: #999;
}

@media (max-width: 768px) {
	.task-board {
	grid-template-columns: 1fr;
	}
}
</style>