import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import i18n from './i18n'
import './assets/styles/global.css'
import './assets/styles/user-top-header.css'
import './assets/styles/admin-shell.css'

/**
 * 开发模式（vite）：走相对路径 /api，由 vite.config 代理到后端。
 * 生产静态页单独跑在 3001 等端口时：直连后端 8080（vite preview 无 proxy）。
 * 也可在项目根创建 .env.production 设置 VITE_API_BASE_URL 覆盖。
 */
function resolveApiBaseURL() {
  const raw = import.meta.env.VITE_API_BASE_URL
  if (raw != null && String(raw).trim() !== '') {
    return String(raw).replace(/\/$/, '')
  }
  if (import.meta.env.DEV) {
    return ''
  }
  if (typeof window === 'undefined') {
    return ''
  }
  const { protocol, hostname, port } = window.location
  if (!port || port === '8080') {
    return ''
  }
  return `${protocol}//${hostname}:8080`
}

axios.defaults.baseURL = resolveApiBaseURL()

// 请求拦截器，添加token
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

const app = createApp(App)
app.use(router)
app.use(ElementPlus)
app.use(i18n)
app.mount('#app')