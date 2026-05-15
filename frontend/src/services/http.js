import axios from 'axios'
import { getToken } from './session'

function resolveApiBaseURL() {
  const raw = import.meta.env.VITE_API_BASE_URL
  if (raw != null && String(raw).trim() !== '') {
    const value = String(raw).trim().replace(/\/$/, '')
    return value.endsWith('/api') ? value : `${value}/api`
  }

  // 本地开发使用相对路径 /api，由 vite.config.js 代理到 Spring Boot 后端。
  if (import.meta.env.DEV) {
    return '/api'
  }

  // 如果前端被 Spring Boot 静态资源托管，直接访问同源 /api。
  if (typeof window === 'undefined') {
    return '/api'
  }

  const { protocol, hostname, port } = window.location
  if (!port || port === '8080') {
    return '/api'
  }

  // 如果用 vite preview 或其他端口预览前端，则自动连接本机后端 8080。
  return `${protocol}//${hostname}:8080/api`
}

export const http = axios.create({
  baseURL: resolveApiBaseURL(),
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

const unwrap = (response) => response?.data || { success: false, data: null }

export const apiGet = async (url, config = {}) => unwrap(await http.get(url, config))
export const apiPost = async (url, payload = {}, config = {}) => unwrap(await http.post(url, payload, config))
export const apiPut = async (url, payload = {}, config = {}) => unwrap(await http.put(url, payload, config))
export const apiDelete = async (url, config = {}) => unwrap(await http.delete(url, config))

export const serviceErrorMessage = (error, fallback) => error?.response?.data?.message || error?.message || fallback
