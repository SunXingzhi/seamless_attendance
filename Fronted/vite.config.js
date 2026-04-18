import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
	const env = loadEnv(mode, process.cwd())
	
	const apiBaseUrl = env.VITE_API_BASE_URL || 'http://localhost:8082'
	
	return {
		plugins: [
			vue(),
			vueDevTools(),
		],
                base: '/seamless_attendance/',
		resolve: {
			alias: {
					'@': fileURLToPath(new URL('./src', import.meta.url))
			},
		},
		define: {
			__APP_ENV__: JSON.stringify(env.VITE_APP_ENV || 'development'),
			__API_BASE_URL__: JSON.stringify(apiBaseUrl),
			global: 'window'
		},
		server: {
			proxy: {
					'/seamless_attendance/api': {
							target: apiBaseUrl,
							changeOrigin: true
					}
			}
		}
	}
})
