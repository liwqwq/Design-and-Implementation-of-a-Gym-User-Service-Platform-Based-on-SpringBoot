<template>
  <div class="github-login-container">
    <div class="login-wrapper">
      <div class="logo-section">
        <div class="logo-icon">💪</div>
        <h1 class="logo-text">FitLife</h1>
      </div>
      
      <div class="login-box">
        <h2 class="login-title">Create your account</h2>
        
        <form @submit.prevent="register">
          <div class="form-group">
            <label for="username">Username</label>
            <input 
              type="text" 
              id="username" 
              v-model="form.username" 
              class="form-input"
              autocomplete="username"
              autofocus
            >
          </div>
          
          <div class="form-group">
            <label for="name">Full Name</label>
            <input 
              type="text" 
              id="name" 
              v-model="form.name" 
              class="form-input"
              autocomplete="name"
            >
          </div>
          
          <div class="form-group">
            <label for="email">Email</label>
            <input 
              type="email" 
              id="email" 
              v-model="form.email" 
              class="form-input"
              autocomplete="email"
            >
          </div>
          
          <div class="form-group">
            <label for="password">Password</label>
            <input 
              type="password" 
              id="password" 
              v-model="form.password" 
              class="form-input"
              autocomplete="new-password"
            >
          </div>
          
          <button type="submit" class="signin-button" :disabled="isSubmitting">
            <span v-if="!isSubmitting">Create account</span>
            <span v-else>Creating...</span>
          </button>
        </form>
        
        <div v-if="error" class="error-box">
          {{ error }}
        </div>
      </div>
      
      <div class="signup-box">
        <span>Already have an account?</span>
        <router-link to="/user/login" class="signup-link">Sign in</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const form = ref({ username: '', name: '', email: '', password: '' })
const error = ref('')
const isSubmitting = ref(false)

const register = async () => {
  try {
    isSubmitting.value = true
    error.value = ''
    const response = await axios.post('/api/auth/register', form.value)
    if (response.data.success) {
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('username', response.data.username)
      localStorage.setItem('userType', 'user')
      router.push('/user/dashboard')
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Registration failed'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.github-login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f6f8fa;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Noto Sans', Helvetica, Arial, sans-serif;
}

.login-wrapper {
  width: 100%;
  max-width: 340px;
  text-align: center;
}

.logo-section {
  margin-bottom: 24px;
}

.logo-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.logo-text {
  font-size: 32px;
  font-weight: 300;
  color: #1f2328;
  margin: 0;
  letter-spacing: -0.5px;
}

.login-box {
  background: #ffffff;
  border: 1px solid #d1d9e0;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  font-size: 24px;
  font-weight: 300;
  color: #1f2328;
  margin: 0 0 20px 0;
  letter-spacing: -0.5px;
}

.form-group {
  margin-bottom: 16px;
  text-align: left;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 400;
  color: #1f2328;
  line-height: 1.5;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  background: #ffffff;
  border: 1px solid #d1d9e0;
  border-radius: 6px;
  color: #1f2328;
  font-size: 14px;
  line-height: 20px;
  box-sizing: border-box;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.form-input:focus {
  border-color: #0969da;
  box-shadow: 0 0 0 3px rgba(9, 105, 218, 0.3);
  outline: none;
}

.signin-button {
  width: 100%;
  padding: 8px 16px;
  background: #1a7f37;
  color: white;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease;
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.1);
}

.signin-button:hover:not(:disabled) {
  background: #166a2f;
}

.signin-button:active:not(:disabled) {
  background: #1a7f37;
}

.signin-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-box {
  margin-top: 16px;
  padding: 16px;
  background: #fff8f5;
  border: 1px solid #ffe4d0;
  border-radius: 6px;
  color: #d1242f;
  font-size: 14px;
  text-align: left;
}

.signup-box {
  padding: 20px;
  border: 1px solid #d1d9e0;
  border-radius: 8px;
  margin-bottom: 40px;
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.signup-box span {
  color: #1f2328;
  font-size: 14px;
}

.signup-link {
  color: #0969da;
  text-decoration: none;
  font-size: 14px;
  margin-left: 4px;
}

.signup-link:hover {
  text-decoration: underline;
}
</style>
