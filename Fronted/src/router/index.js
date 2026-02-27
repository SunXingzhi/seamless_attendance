import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const baseURL = '/seamless_attendance'

const router = createRouter({
	history: createWebHistory(baseURL),
        routes: [
                {
                        path: '/',
                        redirect: '/login'
                },
                {
                        path: '/login',
                        name: 'Login',
                        component: () => import('../views/LoginView.vue'),
                        meta: { title: '登录', requiresAuth: false }
                },
                {
                        path: '/personnel',
                        name: 'Personnel',
                        component: () => import('../views/PersonnelView.vue'),
                        meta: { title: '人员管理', requiresAuth: true }
                },
                {
                        path: '/attendance',
                        name: 'Attendance',
                        component: () => import('../views/AttendanceView.vue'),
                        meta: { title: '考勤记录', requiresAuth: true }
                },
                {
                        path: '/statistics',
                        name: 'Statistics',
                        component: () => import('../views/StatisticsView.vue'),
                        meta: { title: '统计分析', requiresAuth: true }
                },
                {
                        path: '/analysis',
                        name: 'Analysis',
                        component: () => import('../views/AnalysisView.vue'),
                        meta: { title: '出勤率分析', requiresAuth: true }
                },
                {
                        path: '/reports',
                        name: 'Reports',
                        component: () => import('../views/ReportsView.vue'),
                        meta: { title: '报告生成', requiresAuth: true }
                },
                {
                        path: '/work',
                        name: 'Work',
                        component: () => import('../views/WorkView.vue'),
                        meta: { title: '工作任务', requiresAuth: true }
                },
                {
                        path: '/assign',
                        name: 'Assign',
                        component: () => import('../views/AssignView.vue'),
                        meta: { title: '任务分配', requiresAuth: true }
                },
                {
                        path: '/settings',
                        name: 'Settings',
                        component: () => import('../views/SettingsView.vue'),
                        meta: { title: '设置', requiresAuth: true }
                },
                {
                        path: '/devices',
                        name: 'Devices',
                        component: () => import('../views/DevicesView.vue'),
                        meta: { title: '设备管理', requiresAuth: true }
                },
                {
                        path: '/permissions',
                        name: 'Permissions',
                        component: () => import('../views/PermissionsView.vue'),
                        meta: { title: '权限管理', requiresAuth: true }
                },
                {
                        path: '/studios',
                        name: 'Studios',
                        component: () => import('../views/StudioManagementView.vue'),
                        meta: { title: '工作室管理', requiresAuth: true }
                },
                {
                        path: '/studios/create',
                        name: 'CreateStudio',
                        component: () => import('../views/CreateStudioView.vue'),
                        meta: { title: '创建工作室', requiresAuth: true }
                },
                {
                        path: '/studios/edit/:id',
                        name: 'EditStudio',
                        component: () => import('../views/EditStudioView.vue'),
                        meta: { title: '编辑工作室', requiresAuth: true }
                }
        ]
})

router.beforeEach((to, from, next) => {
        document.title = `${to.meta.title} - Seamless Attendance`
        
        const authStore = useAuthStore()
        authStore.initializeAuth()
        
        if (to.meta.requiresAuth && !authStore.isAuthenticated) {
                next('/login')
        } else if (to.path === '/login' && authStore.isAuthenticated) {
                next('/personnel')
        } else {
                next()
        }
})

export default router
