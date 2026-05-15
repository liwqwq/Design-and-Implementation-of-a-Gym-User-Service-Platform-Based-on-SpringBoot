<template>
  <div class="github-login-container">
    <div class="login-wrapper">
      <div class="github-logo">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#24292f" width="48" height="48">
          <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
        </svg>
      </div>
      
      <div class="login-card">
        <h1>Sign in to FitLife</h1>
        
        <form @submit.prevent="login">
          <div class="form-group">
            <label for="username">Username or email address</label>
            <input type="text" id="username" v-model="form.username" @blur="validateUsername" required placeholder="Username or email">
            <div v-if="errors.username" class="field-error">{{ errors.username }}</div>
          </div>
          
          <div class="form-group">
            <div class="label-wrapper">
              <label for="password">Password</label>
              <a href="#" class="forgot-link">Forgot password?</a>
            </div>
            <input type="password" id="password" v-model="form.password" @blur="validatePassword" required placeholder="Password">
            <div v-if="errors.password" class="field-error">{{ errors.password }}</div>
          </div>
          
          <button type="submit" class="primary-button" :disabled="isSubmitting">
            <span v-if="!isSubmitting">Sign in</span>
            <span v-else class="loading">Signing in...</span>
          </button>
        </form>
        
        <div v-if="error" class="error-message">{{ error }}</div>
        
        <div class="divider">
          <span>or</span>
        </div>
        
        <div class="admin-switch">
          <span>Are you an administrator?</span>
          <router-link to="/admin/login" class="switch-link">Sign in to admin panel</router-link>
        </div>
        
        <div class="signup-link">
          <span>New to FitLife?</span>
          <router-link to="/user/register">Create an account</router-link>
        </div>
      </div>
    </div>
    
    <footer class="login-footer">
      <ul>
        <li><a href="#">Terms</a></li>
        <li><a href="#">Privacy</a></li>
        <li><a href="#">Security</a></li>
        <li><a href="#">Contact FitLife</a></li>
      </ul>
    </footer>
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
    errors.username = 'Please enter your username or email'
  } else {
    errors.username = ''
  }
}

const validatePassword = () => {
  const password = form.value.password
  if (!password) {
    errors.password = 'Please enter your password'
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
    error.value = 'Please check your input'
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
    const response = await axios.post('/api/auth/login', credentials)
    if (response.data.success) {
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('username', response.data.username)
      localStorage.setItem('userType', 'user')
      router.push('/user/dashboard')
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    console.log('Login error:', err)
    if (err.response && err.response.data && err.response.data.message) {
      error.value = err.response.data.message
    } else {
      error.value = 'Login failed. Please check your username and password.'
    }
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.github-login-container {
  min-height: 100vh;
  background-color: #f6f8fa;
  display: flex;
  flex-direction: column;
}

.login-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.github-logo {
  margin-bottom: 24px;
}

.login-card {
  background-color: white;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  padding: 24px;
  width: 100%;
  max-width: 425px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.login-card h1 {
  font-size: 24px;
  font-weight: 300;
  letter-spacing: -0.5px;
  color: #24292f;
  text-align: center;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-weight: 500;
  color: #24292f;
  font-size: 14px;
  margin-bottom: 8px;
}

.label-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.forgot-link {
  font-size: 12px;
  color: #0969da;
  text-decoration: none;
}

.forgot-link:hover {
  text-decoration: underline;
}

.form-group input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  font-size: 14px;
  background-color: white;
  box-sizing: border-box;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.form-group input:focus {
  outline: none;
  border-color: #0969da;
  box-shadow: 0 0 0 3px rgba(9, 105, 218, 0.15);
}

.form-group input::placeholder {
  color: #656d76;
}

.primary-button {
  width: 100%;
  padding: 12px;
  background-color: #2da44e;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease;
  margin-top: 8px;
}

.primary-button:hover:not(:disabled) {
  background-color: #2c974b;
}

.primary-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading {
  font-size: 13px;
}

.error-message {
  background-color: #fef2f2;
  border: 1px solid #ffccc7;
  border-radius: 6px;
  padding: 12px;
  margin-top: 16px;
  color: #dc2626;
  font-size: 14px;
  text-align: center;
}

.field-error {
  color: #dc2626;
  font-size: 12px;
  margin-top: 4px;
}

.divider {
  display: flex;
  align-items: center;
  margin: 16px 0;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background-color: #d0d7de;
}

.divider span {
  padding: 0 10px;
  font-size: 12px;
  color: #656d76;
}

.admin-switch {
  text-align: center;
  padding: 16px;
  background-color: #f6f8fa;
  border-radius: 6px;
  margin-bottom: 16px;
}

.admin-switch span {
  font-size: 14px;
  color: #24292f;
}

.switch-link {
  display: block;
  margin-top: 8px;
  font-size: 14px;
  color: #0969da;
  text-decoration: none;
}

.switch-link:hover {
  text-decoration: underline;
}

.signup-link {
  text-align: center;
  padding: 16px;
  border: 1px solid #d0d7de;
  border-radius: 6px;
}

.signup-link span {
  font-size: 14px;
  color: #24292f;
}

.signup-link a {
  display: block;
  margin-top: 8px;
  font-size: 14px;
  color: #0969da;
  text-decoration: none;
}

.signup-link a:hover {
  text-decoration: underline;
}

.login-footer {
  padding: 20px;
  border-top: 1px solid #d0d7de;
  background-color: white;
}

.login-footer ul {
  display: flex;
  justify-content: center;
  list-style: none;
  padding: 0;
  margin: 0;
  gap: 24px;
}

.login-footer li a {
  font-size: 12px;
  color: #57606a;
  text-decoration: none;
}

.login-footer li a:hover {
  color: #0969da;
  text-decoration: underline;
}

@media (max-width: 768px) {
  .login-card {
    padding: 16px;
  }
  
  .login-card h1 {
    font-size: 20px;
  }
}
</style>