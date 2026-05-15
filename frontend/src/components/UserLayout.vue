<template>
  <div class="user-dashboard">
    <header class="user-header">
      <div class="header-content">
        <div class="logo">
          <span class="logo-icon">💪</span>
          <span class="logo-text">FitLife</span>
        </div>
        <nav class="main-nav">
          <router-link
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="nav-link"
          >
            {{ t(item.labelKey) }}
          </router-link>
        </nav>
        <div class="user-info">
          <div class="language-switch">
            <button 
              @click="switchLang('zh')" 
              class="lang-btn" 
              :class="{ active: currentLang === 'zh' }"
            >
              {{ t('common.chinese') }}
            </button>
            <button 
              @click="switchLang('en')" 
              class="lang-btn" 
              :class="{ active: currentLang === 'en' }"
            >
              {{ t('common.english') }}
            </button>
          </div>
          <span class="username">{{ username || t('common.guestName') }}</span>
          <el-button type="primary" @click="logout" class="logout-button">
            {{ t('common.logout') }}
          </el-button>
        </div>
      </div>
    </header>

    <main class="user-main">
      <slot></slot>
    </main>

    <footer class="user-footer">
      <div class="footer-content">
        <p>{{ t('common.footerLine') }}</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElButton } from 'element-plus'
import { userNavItems } from '../config/userNavigation'
import { clearSession, getCurrentUsername } from '../services/session'

const router = useRouter()
const { t, locale } = useI18n()
const username = ref(getCurrentUsername(''))
const navItems = userNavItems

const currentLang = computed(() => locale.value)

const switchLang = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

const logout = () => {
  clearSession()
  router.push('/user/login')
}

onMounted(() => {
  username.value = getCurrentUsername('')
})
</script>

<style scoped>
.user-dashboard {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.user-header {
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  transition: all 0.3s ease;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 64px;
  padding: 10px 24px;
  box-sizing: border-box;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  min-height: 44px;
}

.logo-icon {
  font-size: 28px;
  line-height: 1;
  display: inline-flex;
  align-items: center;
}

.logo-text {
  font-size: 20px;
  font-weight: 800;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.main-nav {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
  min-width: 0;
  min-height: 44px;
  transform: translateY(8px);
}

.nav-link {
  text-decoration: none;
  color: #64748b;
  font-weight: 500;
  font-size: 14px;
  line-height: 1;
  min-height: 40px;
  height: 40px;
  margin: 0;
  padding: 0 14px 2px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
  transition: all 0.2s ease;
}

.nav-link:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.nav-link.router-link-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
  min-height: 44px;
}

.username {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.logout-button {
  height: 40px;
  padding: 0 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border: none;
  border-radius: 999px;
  font-weight: 600;
  font-size: 14px;
  white-space: nowrap;
}

.logout-button:hover {
  box-shadow: 0 4px 15px rgba(240, 147, 251, 0.4);
}

.language-switch {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-height: 40px;
  background: rgba(102, 126, 234, 0.1);
  padding: 4px;
  border-radius: 999px;
  flex-shrink: 0;
  box-sizing: border-box;
}

.lang-btn {
  padding: 0 12px;
  height: 32px;
  border: none;
  border-radius: 999px;
  background: transparent;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
  transition: all 0.2s ease;
}

.lang-btn:hover {
  background: rgba(102, 126, 234, 0.2);
  color: #667eea;
}

.lang-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}

.user-main {
  flex: 1;
  margin-top: 70px;
  padding: 30px;
  min-height: calc(100vh - 120px);
}

.user-footer {
  background: #1a1a2e;
  color: white;
  padding: 20px;
  text-align: center;
}

.footer-content p {
  margin: 0;
  opacity: 0.8;
}

@media (max-width: 1024px) {
  .main-nav {
    gap: 15px;
  }
  
  .nav-link {
    font-size: 14px;
    padding: 6px 12px;
  }
  
  .header-content {
    padding: 12px 20px;
  }
}

@media (max-width: 768px) {
  .main-nav {
    display: none;
    transform: none;
  }
  
  .header-content {
    padding: 10px 15px;
  }
  
  .logo-text {
    font-size: 20px;
  }
  
  .username {
    font-size: 14px;
  }
  
  .logout-button {
    padding: 6px 14px;
    font-size: 12px;
  }
}
</style>
