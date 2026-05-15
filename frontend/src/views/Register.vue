<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="logo-section">
        <div class="logo">
          <span class="logo-icon">🏋️</span>
          <h1 class="logo-text">FitLife</h1>
        </div>
        <p class="tagline">解锁你的健身潜能</p>
      </div>
      
      <h2>用户注册</h2>
      <form @submit.prevent="register">
        <div class="form-group">
          <label for="username">用户名</label>
          <div class="input-container">
            <span class="input-icon">👤</span>
            <input type="text" id="username" v-model="form.username" @blur="validateUsername" required placeholder="请输入用户名">
          </div>
          <div v-if="errors.username" class="error-message">{{ errors.username }}</div>
        </div>
        
        <div class="form-group">
          <label for="password">密码</label>
          <div class="input-container">
            <span class="input-icon">🔐</span>
            <input type="password" id="password" v-model="form.password" @blur="validatePassword" required placeholder="请输入密码">
          </div>
          <div v-if="errors.password" class="error-message">{{ errors.password }}</div>
        </div>
        
        <div class="form-group">
          <label for="confirmPassword">确认密码</label>
          <div class="input-container">
            <span class="input-icon">🔐</span>
            <input type="password" id="confirmPassword" v-model="form.confirmPassword" @blur="validateConfirmPassword" required placeholder="请确认密码">
          </div>
          <div v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</div>
        </div>
        
        <div class="form-group">
          <label for="name">姓名</label>
          <div class="input-container">
            <span class="input-icon">👤</span>
            <input type="text" id="name" v-model="form.name" required placeholder="请输入姓名">
          </div>
        </div>
        
        <div class="form-group">
          <label for="email">邮箱</label>
          <div class="input-container">
            <span class="input-icon">📧</span>
            <input type="email" id="email" v-model="form.email" required placeholder="请输入邮箱">
          </div>
        </div>
        
        <div class="form-group">
          <label for="phone">电话</label>
          <div class="input-container">
            <span class="input-icon">📱</span>
            <input type="tel" id="phone" v-model="form.phone" required placeholder="请输入电话">
          </div>
        </div>
        
        <button type="submit" class="submit-btn" :disabled="isSubmitting">
          {{ isSubmitting ? '注册中...' : '立即注册' }}
        </button>
        
        <div class="error-message" v-if="error">{{ error }}</div>
        <div class="success-message" v-if="success">{{ success }}</div>
      </form>
      
      <p class="login-link">
        已有账号？<a href="/login">立即登录</a>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import axios from 'axios'

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  email: '',
  phone: ''
})

const errors = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const isSubmitting = ref(false)
const error = ref('')
const success = ref('')

const validateUsername = () => {
  if (!form.username) {
    errors.username = '请输入用户名'
  } else if (form.username.length < 3) {
    errors.username = '用户名至少需要3个字符'
  } else {
    errors.username = ''
  }
}

const validatePassword = () => {
  if (!form.password) {
    errors.password = '请输入密码'
  } else if (form.password.length < 6) {
    errors.password = '密码至少需要6个字符'
  } else {
    errors.password = ''
  }
}

const validateConfirmPassword = () => {
  if (!form.confirmPassword) {
    errors.confirmPassword = '请确认密码'
  } else if (form.confirmPassword !== form.password) {
    errors.confirmPassword = '两次输入的密码不一致'
  } else {
    errors.confirmPassword = ''
  }
}

const register = async () => {
  error.value = ''
  success.value = ''
  
  validateUsername()
  validatePassword()
  validateConfirmPassword()
  
  if (errors.username || errors.password || errors.confirmPassword) {
    return
  }
  
  isSubmitting.value = true
  
  try {
    const response = await axios.post('/api/auth/register', {
      username: form.username,
      password: form.password,
      name: form.name,
      email: form.email,
      phone: form.phone
    })
    
    if (response.data.success) {
      success.value = '注册成功！即将跳转到登录页面...'
      setTimeout(() => {
        window.location.href = '/login'
      }, 2000)
    } else {
      error.value = response.data.message || '注册失败'
    }
  } catch (err) {
    error.value = err.response?.data?.message || '注册失败，请稍后重试'
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.auth-card {
  background: white;
  border-radius: 20px;
  padding: 40px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.logo-section {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 10px;
}

.logo-icon {
  font-size: 40px;
}

.logo-text {
  font-size: 28px;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.tagline {
  color: #666;
  font-size: 14px;
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.input-container {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 18px;
}

.input-container input {
  width: 100%;
  padding: 12px 15px 12px 45px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 16px;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.input-container input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.input-container input::placeholder {
  color: #999;
}

.error-message {
  color: #e74c3c;
  font-size: 13px;
  margin-top: 5px;
}

.success-message {
  color: #27ae60;
  font-size: 14px;
  margin-top: 10px;
  text-align: center;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 10px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.login-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>
