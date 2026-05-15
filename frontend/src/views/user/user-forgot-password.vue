<template>
  <div class="login-container">
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
      <div class="circle circle-4"></div>
    </div>
    
    <div class="login-wrapper">
      <div class="login-card">
        <div class="logo-section">
          <div class="logo-box">
            <span class="logo-icon">🔑</span>
          </div>
          <h1 class="logo-text">FitLife</h1>
          <p class="logo-subtitle">忘记密码</p>
        </div>
        
        <form @submit.prevent="submitForgotPassword" class="login-form">
          <h2 class="form-title">找回密码</h2>
          <p class="form-description">选择账户类型并输入注册邮箱，我们将发送密码重置链接</p>
          
          <!-- 用户类型选择 -->
          <div class="form-group">
            <label class="form-label">
              <span class="label-icon">👤</span>
              账户类型
            </label>
            <div class="user-type-selector">
              <button 
                type="button" 
                :class="['type-btn', { active: userType === 'USER' }]"
                @click="userType = 'USER'"
              >
                <span class="type-icon">👤</span>
                <span>普通用户</span>
              </button>
              <button 
                type="button" 
                :class="['type-btn', { active: userType === 'COACH' }]"
                @click="userType = 'COACH'"
              >
                <span class="type-icon">👨‍🏫</span>
                <span>教练</span>
              </button>
              <button 
                type="button" 
                :class="['type-btn', { active: userType === 'ADMIN' }]"
                @click="userType = 'ADMIN'"
              >
                <span class="type-icon">👔</span>
                <span>管理员</span>
              </button>
            </div>
          </div>
          
          <div class="form-group">
            <label class="form-label">
              <span class="label-icon">📧</span>
              邮箱地址
            </label>
            <input 
              type="email" 
              v-model="email" 
              class="form-input"
              placeholder="请输入您的邮箱地址"
              required
            >
          </div>
          
          <button type="submit" class="signin-button" :disabled="isSubmitting">
            <span v-if="!isSubmitting">发送重置链接</span>
            <span v-else class="loading">
              <span class="spinner"></span>
              发送中...
            </span>
          </button>
          
          <div v-if="error" class="error-box">
            <span class="error-icon">❌</span>
            {{ error }}
          </div>
          
          <div v-if="success" class="success-box">
            <span class="success-icon">✅</span>
            {{ success }}
          </div>
        </form>
        
        <div class="back-link">
          <router-link to="/user/login">← 返回登录页面</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const email = ref('')
const userType = ref('USER')
const error = ref('')
const success = ref('')
const isSubmitting = ref(false)

const submitForgotPassword = async () => {
  try {
    isSubmitting.value = true
    error.value = ''
    success.value = ''
    
    const response = await axios.post('/api/auth/forgot-password', { 
      email: email.value,
      userType: userType.value
    })
    if (response.data.success) {
      success.value = response.data.message
      email.value = ''
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = err.response?.data?.message || '发送失败，请稍后重试'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  padding: 20px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 400px;
  height: 400px;
  background: #fff;
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 300px;
  height: 300px;
  background: #fff;
  bottom: -50px;
  right: -50px;
  animation-delay: 2s;
}

.circle-3 {
  width: 200px;
  height: 200px;
  background: #fff;
  top: 50%;
  right: 20%;
  animation-delay: 4s;
}

.circle-4 {
  width: 150px;
  height: 150px;
  background: #fff;
  bottom: 30%;
  left: 10%;
  animation-delay: 1s;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(5deg); }
}

.login-wrapper {
  width: 100%;
  max-width: 420px;
  position: relative;
  z-index: 1;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.logo-section {
  text-align: center;
  margin-bottom: 32px;
}

.logo-box {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.4);
}

.logo-icon {
  font-size: 40px;
}

.logo-text {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px 0;
}

.logo-subtitle {
  font-size: 14px;
  color: #888;
  margin: 0;
}

.login-form {
  margin-bottom: 24px;
}

.form-title {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px 0;
  text-align: center;
}

.form-description {
  font-size: 14px;
  color: #666;
  text-align: center;
  margin: 0 0 24px 0;
}

.form-group {
  margin-bottom: 20px;
  position: relative;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.label-icon {
  font-size: 14px;
}

.user-type-selector {
  display: flex;
  gap: 8px;
}

.type-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 14px 12px;
  background: #f5f5f5;
  border: 2px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 13px;
  color: #666;
}

.type-btn:hover {
  background: #f0f0f0;
}

.type-btn.active {
  background: #f0f4ff;
  border-color: #667eea;
  color: #667eea;
}

.type-icon {
  font-size: 24px;
}

.form-input {
  width: 100%;
  padding: 14px 16px;
  background: #f5f5f5;
  border: 2px solid transparent;
  border-radius: 12px;
  font-size: 16px;
  color: #333;
  box-sizing: border-box;
  transition: all 0.3s ease;
}

.form-input:focus {
  outline: none;
  background: #fff;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-input::placeholder {
  color: #aaa;
}

.signin-button {
  width: 100%;
  padding: 14px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.signin-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.signin-button:active:not(:disabled) {
  transform: translateY(0);
}

.signin-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading {
  display: flex;
  align-items: center;
  gap: 8px;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-box {
  margin-top: 16px;
  padding: 14px 16px;
  background: #fff5f5;
  border: 1px solid #ffe5e5;
  border-radius: 10px;
  color: #d93026;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.error-icon {
  font-size: 16px;
}

.success-box {
  margin-top: 16px;
  padding: 14px 16px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 10px;
  color: #16a34a;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.success-icon {
  font-size: 16px;
}

.back-link {
  text-align: center;
}

.back-link a {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
}

.back-link a:hover {
  text-decoration: underline;
}
</style>
