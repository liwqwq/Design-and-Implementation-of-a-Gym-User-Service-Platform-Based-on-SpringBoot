<template>
  <div class="dashboard">
    <header class="header">
      <div class="header-content">
        <div class="logo">
          <span class="logo-icon">🏋️‍♂️</span>
          <span class="logo-text">FitLife</span>
        </div>
        <div class="user-info">
          <span class="username">{{ username }}</span>
          <el-button type="primary" @click="logout" class="logout-button">
            退出登录
          </el-button>
        </div>
      </div>
    </header>
    
    <div class="dashboard-container">
      <aside class="sidebar">
        <nav class="nav">
          <router-link to="/admin" class="nav-item">
            <span class="nav-icon">👥</span>
            <span class="nav-text">会员管理</span>
          </router-link>
          <router-link to="/admin/users" class="nav-item">
            <span class="nav-icon">⚙️</span>
            <span class="nav-text">用户管理</span>
          </router-link>
          <router-link to="/admin/classes" class="nav-item">
            <span class="nav-icon">📅</span>
            <span class="nav-text">课程管理</span>
          </router-link>
          <router-link to="/admin/products" class="nav-item">
            <span class="nav-icon">🛍️</span>
            <span class="nav-text">在线商城</span>
          </router-link>
          <router-link to="/admin/community" class="nav-item">
            <span class="nav-icon">💬</span>
            <span class="nav-text">社区互动</span>
          </router-link>
        </nav>
      </aside>
      
      <main class="main">
        <slot></slot>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElButton } from 'element-plus'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '用户')

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userType')
  router.push('/admin/login')
}

onMounted(() => {
  // 可以从localStorage中获取用户名
  const user = localStorage.getItem('user')
  if (user) {
    try {
      const userData = JSON.parse(user)
      username.value = userData.username || '用户'
    } catch (e) {
      console.error('解析用户数据失败', e)
    }
  }
})
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-secondary);
  margin: 0;
  padding: 0;
  position: relative;
}

.header {
  background: linear-gradient(135deg, var(--fitness-primary) 0%, var(--fitness-secondary) 100%);
  color: white;
  padding: 16px 0;
  box-shadow: var(--shadow-md);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  transition: all var(--transition-normal);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 var(--spacing-xl);
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  transition: all var(--transition-normal);
}

.logo-icon {
  font-size: 32px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.logo-text {
  font-size: var(--font-size-xl);
  font-weight: 700;
  letter-spacing: 1px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.username {
  font-weight: 500;
  font-size: var(--font-size-lg);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.logout-button {
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.logout-button:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.dashboard-container {
  display: flex;
  margin-top: 72px;
  min-height: calc(100vh - 72px);
}

.sidebar {
  width: 220px;
  background: linear-gradient(135deg, var(--bg-primary) 0%, #f8fafc 100%);
  border-right: 1px solid var(--border-color);
  padding: 20px 0;
  position: fixed;
  left: 0;
  top: 72px;
  bottom: 0;
  z-index: 900;
  box-shadow: var(--shadow-md);
  overflow-y: auto;
  transition: all var(--transition-normal);
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 0 10px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  text-decoration: none;
  color: var(--text-primary);
  border-radius: var(--radius-lg);
  transition: all var(--transition-fast);
  font-weight: 500;
  position: relative;
  overflow: hidden;
}

.nav-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 4px;
  height: 100%;
  background-color: transparent;
  transition: all var(--transition-fast);
  border-radius: var(--radius-lg) 0 0 var(--radius-lg);
}

.nav-item:hover {
  background-color: var(--bg-tertiary);
  color: var(--fitness-primary);
  transform: translateX(4px);
  box-shadow: var(--shadow-sm);
}

.nav-item:hover::before {
  background-color: var(--fitness-primary);
}

.nav-item.router-link-active {
  background-color: var(--fitness-primary);
  color: white;
  box-shadow: var(--shadow-md);
}

.nav-item.router-link-active::before {
  background-color: #1d4ed8;
}

.nav-icon {
  font-size: 18px;
  width: 24px;
  text-align: center;
  transition: all var(--transition-fast);
}

.nav-item:hover .nav-icon {
  transform: scale(1.1);
}

.main {
  flex: 1;
  margin-left: 220px;
  padding: var(--spacing-xl);
  overflow-y: auto;
  min-height: calc(100vh - 72px);
  background-color: var(--bg-secondary);
  transition: all var(--transition-normal);
}

/* 滚动条样式 */
.sidebar::-webkit-scrollbar,
.main::-webkit-scrollbar {
  width: 8px;
}

.sidebar::-webkit-scrollbar-track,
.main::-webkit-scrollbar-track {
  background: var(--border-light);
  border-radius: var(--radius-sm);
}

.sidebar::-webkit-scrollbar-thumb,
.main::-webkit-scrollbar-thumb {
  background: var(--text-tertiary);
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.sidebar::-webkit-scrollbar-thumb:hover,
.main::-webkit-scrollbar-thumb:hover {
  background: var(--text-secondary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    position: static;
    top: auto;
    bottom: auto;
    border-right: none;
    border-bottom: 1px solid var(--border-color);
    box-shadow: var(--shadow-md);
  }
  
  .main {
    margin-left: 0;
    margin-top: 0;
    min-height: calc(100vh - 72px);
    padding: var(--spacing-md);
  }
  
  .dashboard-container {
    flex-direction: column;
  }
  
  .nav {
    flex-direction: row;
    overflow-x: auto;
    padding: 0 10px;
    gap: 8px;
  }
  
  .nav-item {
    white-space: nowrap;
    border-radius: var(--radius-lg);
    padding: 10px 16px;
  }
  
  .nav-item:hover {
    transform: none;
  }
  
  .header-content {
    padding: 0 var(--spacing-md);
  }
  
  .logo-text {
    font-size: var(--font-size-lg);
  }
  
  .username {
    font-size: var(--font-size-base);
  }
}

@media (max-width: 480px) {
  .header {
    padding: 12px 0;
  }
  
  .logo-icon {
    font-size: 24px;
  }
  
  .logo-text {
    font-size: var(--font-size-base);
  }
  
  .user-info {
    gap: var(--spacing-md);
  }
  
  .nav-item {
    padding: 8px 12px;
    font-size: var(--font-size-sm);
  }
  
  .nav-icon {
    font-size: var(--font-size-base);
  }
}
</style>