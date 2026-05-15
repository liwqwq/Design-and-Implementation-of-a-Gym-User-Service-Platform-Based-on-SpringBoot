<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="logo-section">
        <div class="logo">
          <span class="logo-icon">🏋️</span>
          <h1 class="logo-text">FitLife Admin</h1>
        </div>
        <p class="tagline">健身房管理系统</p>
      </div>
      
      <h2>管理员登录</h2>
      <form @submit.prevent="login">
        <div class="form-group">
          <label for="username">用户名</label>
          <div class="input-container">
            <span class="input-icon">👤</span>
            <input type="text" id="username" v-model="form.username" @blur="validateUsername" required placeholder="请输入用户名">
          </div>
          <div v-if="errors.username" class="field-error">{{ errors.username }}</div>
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <div class="input-container">
            <span class="input-icon">🔒</span>
            <input type="password" id="password" v-model="form.password" @blur="validatePassword" required placeholder="请输入密码">
          </div>
          <div v-if="errors.password" class="field-error">{{ errors.password }}</div>
        </div>
        <div class="form-options">
          <div class="remember-me">
            <input type="checkbox" id="remember" v-model="form.remember">
            <label for="remember">记住我</label>
          </div>
          <a href="#" class="forgot-password">忘记密码？</a>
        </div>
        <button type="submit" class="primary-button">
          <span v-if="!isSubmitting">登录</span>
          <span v-else class="loading-spinner">⌛</span>
        </button>
        <div class="form-footer">
          <router-link to="/user/login">返回用户登录</router-link>
        </div>
      </form>
      <div v-if="error" class="error-message">{{ error }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const form = ref({
  username: '',
  password: '',
  remember: false
})
const error = ref('')
const isSubmitting = ref(false)
const errors = reactive({
  username: '',
  password: ''
})

const isFormValid = computed(() => {
  return !Object.values(errors).some(error => error !== '') && 
         Object.values(form.value).slice(0, 2).every(value => value.trim() !== '')
})

const validateUsername = () => {
  const username = form.value.username
  if (!username) {
    errors.username = '请输入用户名'
  } else {
    errors.username = ''
  }
}

const validatePassword = () => {
  const password = form.value.password
  if (!password) {
    errors.password = '请输入密码'
  } else {
    errors.password = ''
  }
}

const validateForm = () => {
  validateUsername()
  validatePassword()
  return isFormValid.value
}

const login = async () => {
  if (!validateForm()) {
    error.value = '请检查表单输入'
    return
  }
  
  try {
    localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userType')
  localStorage.removeItem('user')
  localStorage.removeItem('coachId')
    isSubmitting.value = true
    const credentials = {
      username: form.value.username,
      password: form.value.password
    }
    const response = await axios.post('/api/admin/auth/login', credentials)
    if (response.data.success) {
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('username', response.data.username)
      localStorage.setItem('userType', 'admin')
      router.push('/admin')
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    console.log('Login error:', err)
    if (err.response && err.response.data && err.response.data.message) {
      error.value = err.response.data.message
    } else {
      error.value = '登录失败，请检查用户名和密码'
    }
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e3a5f, #2d5a87);
  padding: 20px;
  position: relative;
  overflow: hidden;
}



.auth-card {
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  padding: 40px;
  width: 100%;
  max-width: 450px;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
  backdrop-filter: blur(10px);
}

.auth-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 30px 60px -15px rgba(0, 0, 0, 0.3);
}

.logo-section {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 10px;
}

.logo-icon {
  font-size: 40px;
}

.logo-text {
  font-size: 32px;
  font-weight: 800;
  background: linear-gradient(135deg, #1e3a5f, #2d5a87);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.tagline {
  font-size: 16px;
  color: #6b7280;
  margin: 0;
  font-weight: 500;
}

.auth-card h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #1f2937;
  font-size: 28px;
  font-weight: 700;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #4b5563;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.input-container {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 18px;
  color: #9ca3af;
  z-index: 1;
}

.input-container input {
  width: 100%;
  padding: 16px 50px 16px 48px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  font-size: 16px;
  transition: all 0.3s ease;
  background-color: #f9fafb;
  outline: none;
}

.input-container input:focus {
  outline: none;
  border-color: #2d5a87;
  box-shadow: 0 0 0 3px rgba(45, 90, 135, 0.1);
  background-color: white;
}

.input-container input::placeholder {
  color: #9ca3af;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
}

.remember-me input[type="checkbox"] {
  width: 18px;
  height: 18px;
  accent-color: #2d5a87;
  cursor: pointer;
}

.remember-me label {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
  cursor: pointer;
}

.forgot-password {
  font-size: 14px;
  color: #2d5a87;
  text-decoration: none;
  font-weight: 600;
}

.forgot-password:hover {
  color: #1e3a5f;
  text-decoration: underline;
}

.primary-button {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #1e3a5f, #2d5a87);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 10px;
}

.primary-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -5px rgba(45, 90, 135, 0.4);
}

.primary-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  display: inline-block;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.form-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #6b7280;
}

.form-footer a {
  color: #2d5a87;
  text-decoration: none;
  font-weight: 600;
}

.form-footer a:hover {
  color: #1e3a5f;
  text-decoration: underline;
}

.field-error {
  color: #ef4444;
  font-size: 14px;
  margin-top: 5px;
}

.error-message {
  color: #ef4444;
  font-size: 14px;
  margin-top: 15px;
  text-align: center;
  padding: 10px;
  background-color: #fee2e2;
  border-radius: 8px;
  border-left: 4px solid #ef4444;
}

@media (max-width: 768px) {
  .auth-card {
    padding: 30px;
  }
  
  .logo-text {
    font-size: 28px;
  }
  
  .auth-card h2 {
    font-size: 24px;
  }
  
  .input-container input {
    padding: 14px 45px 14px 44px;
  }
  
  .primary-button {
    padding: 14px;
    font-size: 16px;
  }
}
</style>