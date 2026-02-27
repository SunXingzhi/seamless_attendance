<template>
	<div class="login-container">
		<div class="login-box">
			<div class="login-header">
				<h1>工作室管理系统</h1>
				<p>管理员登录</p>
			</div>
			
			<form @submit.prevent="handleLogin" class="login-form">
				<div class="form-group">
					<label for="username">用户名</label>
					<input 
						type="text" 
						id="username" 
						v-model="loginForm.username" 
						placeholder="请输入用户名"
						:disabled="loading"
						required
						maxlength="10"
					/>
				</div>
				
				<div class="form-group">
					<label for="password">密码</label>
					<input 
						type="password" 
						id="password" 
						v-model="loginForm.password" 
						placeholder="请输入密码"
						:disabled="loading"
						required
					/>
				</div>
				
				<div v-if="error" class="error-message">
					{{ error }}
				</div>
				
				<button type="submit" class="login-btn" :disabled="loading">
					{{ loading ? '登录中...' : '登录' }}
				</button>
			</form>
			
			<div class="login-footer">
				<p>忘记密码？请联系系统管理员</p>
			</div>
		</div>
	</div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const loginForm = ref({
	username: '',
	password: ''
})

const loading = ref(false)
const error = ref('')

const handleLogin = async () => {
	error.value = ''
	
	if (loginForm.value.username.length > 10) {
		error.value = '用户名不能超过10个字符'
		return
	}
	
	if (loginForm.value.password.includes(' ') || loginForm.value.password.includes('\t')) {
		error.value = '密码不能包含空白字符'
		return
	}
	
	loading.value = true
	
	try {
		await authStore.login({
			username: loginForm.value.username,
			password: loginForm.value.password
		})
		
		router.push('/personnel')
	} catch (err) {
		error.value = err.message || '登录失败，请检查用户名和密码'
	} finally {
		loading.value = false
	}
}
</script>

<style scoped>
.login-container {
	min-height: 100vh;
	display: flex;
	justify-content: center;
	align-items: center;
	background: linear-gradient(135deg, #667eea 0%, #4b78a2 100%);
	padding: 20px;
}

.login-box {
	background: white;
	border-radius: 12px;
	box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
	width: 100%;
	max-width: 400px;
	padding: 40px;
}

.login-header {
	text-align: center;
	margin-bottom: 40px;
}

.login-header h1 {
	margin: 0 0 10px 0;
	font-size: 28px;
	color: #333;
}

.login-header p {
	margin: 0;
	font-size: 14px;
	color: #666;
}

.login-form {
	display: flex;
	flex-direction: column;
	gap: 20px;
}

.form-group {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.form-group label {
	font-size: 14px;
	font-weight: 600;
	color: #333;
}

.form-group input {
	padding: 12px 16px;
	border: 2px solid #e0e0e0;
	border-radius: 8px;
	font-size: 14px;
	transition: all 0.3s;
}

.form-group input:focus {
	outline: none;
	border-color: #667eea;
	box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-group input:disabled {
	background-color: #f5f5f5;
	cursor: not-allowed;
}

.error-message {
	background-color: #fee;
	color: #c33;
	padding: 12px;
	border-radius: 8px;
	font-size: 14px;
	text-align: center;
	border: 1px solid #fcc;
}

.login-btn {
	padding: 14px 24px;
	background: linear-gradient(135deg, #667eea 0%, #777eeb 100%);
	color: white;
	border: none;
	border-radius: 8px;
	font-size: 16px;
	font-weight: 600;
	cursor: pointer;
	transition: all 0.3s;
	margin-top: 10px;
}

.login-btn:hover:not(:disabled) {
	transform: translateY(-2px);
	box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
}

.login-btn:active:not(:disabled) {
	transform: translateY(0);
}

.login-btn:disabled {
	opacity: 0.6;
	cursor: not-allowed;
}

.login-footer {
	text-align: center;
	margin-top: 30px;
	padding-top: 20px;
	border-top: 1px solid #e0e0e0;
}

.login-footer p {
	margin: 0;
	font-size: 13px;
	color: #999;
}

@media (max-width: 480px) {
	.login-box {
		padding: 30px 20px;
	}
	
	.login-header h1 {
		font-size: 24px;
	}
}
</style>
