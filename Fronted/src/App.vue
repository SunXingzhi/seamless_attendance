<template>
	<div class="app-container" :class="{ 'login-mode': isLoginPage }">
	<!-- 登录页面 -->
	<router-view v-if="isLoginPage" />
	
	<!-- 侧边树形菜单 -->
	<aside class="sidebar" v-if="!isLoginPage">
		<div class="sidebar-header">
		<h1 class="logo">无感考勤系统 </h1>
		<h2 class="logo">By Sun Haoxiang</h2>
		</div>
		<nav class="tree-nav">
		<ul class="tree-menu">
			<li 
				v-for="menu in menuItems" 
				:key="menu.id"
				class="tree-node"
			>
				<div 
					class="tree-node-header"
					@click="toggleMenu(menu.id)"
				>
					<span class="menu-text">{{ menu.name }}</span>
					<span 
						class="menu-toggle"
						v-if="menu.children && menu.children.length > 0"
					>
						{{ menu.open ? '▼' : '▶' }}
					</span>
				</div>
				<ul 
					class="tree-children"
					v-if="menu.children && menu.children.length > 0"
					:class="{ 'menu-open': menu.open }"
				>
					<li 
						v-for="child in menu.children" 
						:key="child.id"
						class="tree-child"
					>
						<router-link 
							:to="child.path"
							class="child-link"
							:class="{ active: $route.path === child.path }"
						>
							<span class="menu-text">{{ child.name }}</span>
						</router-link>
					</li>
				</ul>
			</li>
		</ul>
		</nav>
	</aside>
	
	<!-- 主内容区 -->
	<div class="main-content" v-if="!isLoginPage">
		<!-- 顶部标题栏 -->
		<header class="top-header">
			<h2 class="page-title">{{ currentPageTitle }}</h2>
			<div class="header-right">
				<div class="user-info" v-if="authStore.isAuthenticated">
					<span class="user-name">{{ authStore.user?.name || authStore.user?.username }}</span>
					<button @click="handleLogout" class="logout-btn">退出</button>
				</div>
			</div>
		</header>
		
		<!-- 页面内容 -->
		<main class="content-area">
			<router-view />
		</main>
		
		<!-- 页脚 -->
		<footer class="app-footer">
			<p>© 2026 Seamless Attendance System. All rights reserved.</p>
		</footer>
	</div>
	</div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onActivated} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 存储菜单展开状态
const menuOpenState = ref({
	1: true, // 人员管理默认展开
	2: false, // 统计分析默认收起
	3: false, // 工作任务默认收起
	4: false // 设置默认收起
})

onMounted(() => {
	authStore.initializeAuth()
})

const isLoginPage = computed(() => route.path === '/login')

const handleLogout = async () => {
	await authStore.logout()
	router.push('/login')
}

const baseURL = '/seamless_attendance'

const menuItems = computed(() => {
	const role = authStore.userRole
	
	const allMenus = [
		{
			id: 1,
			name: '人员管理',
			open: menuOpenState.value[1],
			children: [
				{ id: 11, name: '人员列表', path: '/personnel' },
				{ id: 12, name: '考勤记录', path: '/attendance' }
			],
			requiredRoles: ['super_admin', 'studio_admin', 'studio_member']
		}, 
		{
			id: 2,
			name: '统计分析',
			open: menuOpenState.value[2],
			children: [
				{ id: 21, name: '活跃度统计', path: '/statistics' },
				{ id: 22, name: '出勤率分析', path: '/analysis' },
				{ id: 23, name: '报告生成', path: '/reports' }
			],
			requiredRoles: ['super_admin', 'studio_admin']
		},
		{
			id: 3,
			name: '工作任务',
			open: menuOpenState.value[3],
			children: [
				{ id: 31, name: '任务看板', path: '/work' },
				{ id: 32, name: '任务分配', path: '/assign' }
			],
			requiredRoles: ['super_admin', 'studio_admin', 'studio_member']
		},
		{
			id: 4,
			name: '设置',
			open: menuOpenState.value[4],
			children: [
				{ id: 41, name: '系统设置', path: '/settings', requiredRoles: ['super_admin'] },
				{ id: 42, name: '设备管理', path: '/devices', requiredRoles: ['super_admin'] },
				{ id: 43, name: '权限管理', path: '/permissions', requiredRoles: ['super_admin'] },
				{ id: 44, name: '工作室管理', path: '/studios', requiredRoles: ['super_admin'] }
			],
			requiredRoles: ['super_admin']
		}
	]
	
	if (!role) {
		return [
			{
				id: 1,
				name: '人员管理',
				open: true,
				children: [
					{ id: 11, name: '人员列表', path: '/personnel' }
				],
				requiredRoles: null
			}
		]
	}
	
	return allMenus.map(menu => {
		if (menu.requiredRoles && !menu.requiredRoles.includes(role)) {
			return null
		}
		
		const filteredMenu = { ...menu }
		
		if (filteredMenu.children) {
			filteredMenu.children = filteredMenu.children.filter(child => {
				return !child.requiredRoles || child.requiredRoles.includes(role)
			})
		}
		
		return filteredMenu
	}).filter(menu => menu !== null && (!menu.children || menu.children.length > 0))
})

// 切换菜单展开/收起
const toggleMenu = (menuId) => {
	if (menuOpenState.value.hasOwnProperty(menuId)) {
		menuOpenState.value[menuId] = !menuOpenState.value[menuId]
	}
}

// 当前页面标题
const currentPageTitle = computed(() => {
	// 根据当前路由匹配对应的菜单名称
	let title = '首页'
	
	menuItems.value.forEach(menu => {
		if (menu.children) {
			const match = menu.children.find(child => child.path === route.path)
			if (match) {
				title = match.name
			}
		}
	})
	
	return title
})
</script>

<style>
* {
	margin: 0;
	padding: 0;
	box-sizing: border-box;
}

body {
	font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
	background-color: #f5f7fa;
	color: #333;
	line-height: 1.6;
}

/* 主容器 */
.app-container {
	min-height: 100vh;
	display: flex;
	flex-direction: row;
	overflow: hidden;
}

.app-container.login-mode {
	display: block;
}

/* 侧边栏 */
.sidebar {
	width: 250px;
	background: #304156;
	color: white;
	display: flex;
	flex-direction: column;
	box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
	transition: width 0.3s ease;
	z-index: 100;
}

/* 侧边栏头部 */
.sidebar-header {
	padding: 20px;
	background: #263445;
	border-bottom: 1px solid #1d2838;
}

.sidebar-header .logo {
	font-size: 1.2rem;
	font-weight: 600;
	color: #409eff;
	text-align: center;
}

/* 树形导航 */
.tree-nav {
	flex: 1;
	overflow-y: auto;
	padding: 10px 0;
}

.tree-menu {
	list-style: none;
}

.tree-node {
	margin-bottom: 2px;
}

/* 树形节点头部 */
.tree-node-header {
	display: flex;
	align-items: center;
	padding: 12px 20px;
	cursor: pointer;
	transition: background-color 0.2s;
	user-select: none;
}

.tree-node-header:hover {
	background-color: #263445;
}

/* 菜单文本 */
.menu-text {
	flex: 1;
	font-size: 0.95rem;
}

/* 菜单展开/收起图标 */
.menu-toggle {
	font-size: 0.7rem;
	color: #909399;
	transition: transform 0.2s;
}

/* 子菜单 */
.tree-children {
	list-style: none;
	max-height: 0;
	overflow: hidden;
	transition: max-height 0.3s ease;
}

.tree-children.menu-open {
	max-height: 500px;
}

/* 子菜单节点 */
.tree-child {
	margin: 0;
}

/* 子菜单链接 */
.child-link {
	display: flex;
	align-items: center;
	padding: 10px 20px 10px 45px;
	color: #c0c4cc;
	text-decoration: none;
	transition: all 0.2s;
	font-size: 0.9rem;
}

.child-link:hover {
	background-color: #263445;
	color: white;
	padding-left: 50px;
}

.child-link.active {
	background-color: #409eff;
	color: white;
	border-right: 3px solid #66b1ff;
	padding-left: 50px;
}

/* 主内容区 */
.main-content {
	flex: 1;
	display: flex;
	flex-direction: column;
	overflow: hidden;
	background: #f5f7fa;
}

/* 顶部标题栏 */
.top-header {
	background: white;
	padding: 15px 20px;
	border-bottom: 1px solid #e4e7ed;
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.page-title {
	font-size: 1.2rem;
	font-weight: 600;
	color: #303133;
	margin: 0;
}

.header-right {
	display: flex;
	align-items: center;
	gap: 20px;
}

.user-info {
	display: flex;
	align-items: center;
	gap: 15px;
}

.user-name {
	font-size: 0.9rem;
	color: #606266;
	font-weight: 500;
}

.logout-btn {
	padding: 6px 16px;
	background: #f56c6c;
	color: white;
	border: none;
	border-radius: 4px;
	font-size: 0.85rem;
	cursor: pointer;
	transition: background-color 0.2s;
}

.logout-btn:hover {
	background: #f78989;
}

/* 内容区域 */
.content-area {
	flex: 1;
	overflow-y: auto;
	padding: 20px;
	max-width: 100%;
}

/* 页脚 */
.app-footer {
	background: white;
	color: #909399;
	text-align: center;
	padding: 15px 0;
	font-size: 0.85rem;
	border-top: 1px solid #e4e7ed;
	margin-top: auto;
}

/* 滚动条样式 */
.tree-nav::-webkit-scrollbar {
	width: 6px;
}

.tree-nav::-webkit-scrollbar-track {
	background: #1d2838;
}

.tree-nav::-webkit-scrollbar-thumb {
	background: #404e63;
	border-radius: 3px;
}

.tree-nav::-webkit-scrollbar-thumb:hover {
	background: #4a5a73;
}

/* 响应式设计 */
@media (max-width: 768px) {
	.sidebar {
	width: 200px;
	}
	
	.content-area {
	padding: 10px;
	}
	
	.tree-node-header {
	padding: 10px 15px;
	}
	
	.child-link {
	padding: 8px 15px 8px 35px;
	}
	
	.child-link:hover,
	.child-link.active {
	padding-left: 40px;
	}
}

@media (max-width: 480px) {
	.app-container {
	flex-direction: column;
	}
	
	.sidebar {
	width: 100%;
	height: 200px;
	border-right: none;
		border-bottom: 2px solid #1d2838;
	}
	
	.main-content {
	height: calc(100vh - 200px);
	}
}
</style>
