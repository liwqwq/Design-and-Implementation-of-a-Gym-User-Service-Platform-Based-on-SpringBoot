<template>
  <div class="coach-dashboard">
    <!-- 顶部导航 -->
    <header class="dashboard-header">
      <div class="header-content">
        <div class="logo">
          <span class="logo-icon">👨‍🏫</span>
          <span class="logo-text">FitLife {{ t('coach.login.title') }}</span>
        </div>
        <nav class="nav">
          <router-link to="/coach/dashboard" class="nav-item" :class="{ active: currentPage === 'dashboard' }">{{ t('common.dashboard') }}</router-link>
          <router-link to="/coach/profile" class="nav-item" :class="{ active: currentPage === 'profile' }">{{ t('common.myProfile') }}</router-link>
          <router-link to="/coach/classes" class="nav-item" :class="{ active: currentPage === 'classes' }">{{ t('common.classes') }}</router-link>
          <router-link to="/coach/mailbox" class="nav-item" :class="{ active: currentPage === 'mailbox' }">{{ t('coach.social.mailbox') }}</router-link>
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
          <button class="logout-btn" @click="logout">{{ t('common.logout') }}</button>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="dashboard-content">
      <div class="section-title">
        <h1>🏠 {{ t('coach.dashboard.title') }}</h1>
        <p>{{ t('coach.dashboard.welcome') }}, {{ username || t('common.guestName') }}!</p>
      </div>

      <!-- 人流控制卡片 -->
      <div class="stats-cards">
        <div class="stat-card active-users">
          <div class="stat-icon">👥</div>
          <div class="stat-content">
            <div class="stat-value">{{ activeUsers }}</div>
            <div class="stat-label">{{ t('coach.dashboard.activeUsers') }}</div>
          </div>
        </div>
        
        <div class="stat-card today-classes">
          <div class="stat-icon">📅</div>
          <div class="stat-content">
            <div class="stat-value">{{ todayClasses }}</div>
            <div class="stat-label">{{ t('coach.dashboard.todayClasses') }}</div>
          </div>
        </div>
        
        <div class="stat-card pending-requests">
          <div class="stat-icon">🔔</div>
          <div class="stat-content">
            <div class="stat-value">{{ pendingRequests }}</div>
            <div class="stat-label">{{ t('coach.dashboard.pendingRequests') }}</div>
          </div>
        </div>
      </div>

      <!-- 今日课程安排 -->
      <div class="section">
        <h2>📋 {{ t('coach.dashboard.schedule') }}</h2>
        <div v-if="todaySchedule.length === 0" class="empty-state">
          <div class="empty-icon">📅</div>
          <div class="empty-text">{{ t('coach.dashboard.noSchedule') }}</div>
        </div>
        <div v-else class="schedule-list">
          <div v-for="classItem in todaySchedule" :key="classItem.id" class="schedule-item">
            <div class="schedule-time">{{ classItem.time }}</div>
            <div class="schedule-info">
              <div class="schedule-name">{{ displayScheduleName(classItem) }}</div>
              <div class="schedule-meta">
                <span>👥 {{ classItem.participants }}{{ t('common.people') }}</span>
                <span>📍 {{ displayScheduleLocation(classItem) }}</span>
              </div>
            </div>
            <div class="schedule-status" :class="classItem.status">
              {{ displaySessionStatus(classItem.status) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 通知提醒 -->
      <div class="section">
        <h2>🔔 {{ t('coach.dashboard.notifications') }}</h2>
        <div v-if="notifications.length === 0" class="empty-state">
          <div class="empty-icon">🔕</div>
          <div class="empty-text">{{ t('coach.dashboard.noNotifications') }}</div>
        </div>
        <div v-else class="notifications-list">
          <div v-for="notification in notifications" :key="notification.id" class="notification-item">
            <div class="notification-icon">{{ notification.icon }}</div>
            <div class="notification-content">
              <div class="notification-title">{{ displayNotificationTitle(notification) }}</div>
              <div class="notification-time">{{ displayNotificationTime(notification) }}</div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { useI18n } from 'vue-i18n'
import { formatCourseName } from '../../utils/courseName'

const { t, te, locale } = useI18n()
const currentLang = computed(() => locale.value)

const switchLang = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

const username = ref(localStorage.getItem('username') || '')
const activeUsers = ref(0)
const todayClasses = ref(0)
const pendingRequests = ref(0)
const todaySchedule = ref([])
const notifications = ref([])
const currentPage = ref('dashboard')

const getAuthHeader = () => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

const loadDashboardData = async () => {
  try {
    const [usersResponse, scheduleResponse, notificationsResponse] = await Promise.all([
      axios.get('/api/coach/active-users', { headers: getAuthHeader() }),
      axios.get('/api/coach/today-schedule', { headers: getAuthHeader() }),
      axios.get('/api/coach/notifications', { headers: getAuthHeader() })
    ])

    if (usersResponse.data.success) {
      activeUsers.value = usersResponse.data.data
    }

    if (scheduleResponse.data.success) {
      todaySchedule.value = scheduleResponse.data.data
      todayClasses.value = todaySchedule.value.length
    }

    if (notificationsResponse.data.success) {
      notifications.value = notificationsResponse.data.data
      pendingRequests.value = notifications.value.filter((n) => n.type !== 'system').length
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    activeUsers.value = 0
    todayClasses.value = 0
    pendingRequests.value = 0
    todaySchedule.value = []
    notifications.value = []
  }
}

const displayScheduleName = (row) => {
  if (row?.sk) return t(`coach.dashboard.demoSchedule.${row.sk}.name`)
  return formatCourseName(row?.name, t, te)
}

const displayScheduleLocation = (row) => {
  if (row?.sk) return t(`coach.dashboard.demoSchedule.${row.sk}.location`)
  if (!row?.location) return ''
  const key = 'venue.' + row.location
  return te(key) ? t(key) : row.location
}

const displaySessionStatus = (status) => {
  const path = 'classStatus.' + status
  return te(path) ? t(path) : status
}

const pickBilingual = (primary, alt) => {
  const primaryOk = primary != null && String(primary).trim() !== ''
  const altOk = alt != null && String(alt).trim() !== ''
  if (locale.value === 'en') return altOk ? String(alt) : (primaryOk ? String(primary) : '')
  return primaryOk ? String(primary) : (altOk ? String(alt) : '')
}

const displayNotificationTitle = (n) => {
  if (n?.sk) return t(`coach.dashboard.demoNotifications.${n.sk}.title`)
  return pickBilingual(n?.title, n?.titleEn)
}

const displayNotificationTime = (n) => {
  if (n?.sk) return t(`coach.dashboard.demoNotifications.${n.sk}.time`)
  return pickBilingual(n?.time, n?.timeEn)
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userType')
  localStorage.removeItem('coachId')
  window.location.href = '/coach/login'
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.coach-dashboard {
  min-height: 100vh;
  background: #f5f7fa;
}

.dashboard-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  box-sizing: border-box;
}

.header-content {
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  box-sizing: border-box;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  color: white;
  flex-shrink: 0;
}

.logo-icon {
  font-size: 30px;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  font-size: 19px;
  font-weight: 700;
  line-height: 1.35;
}

.nav {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  flex-wrap: wrap;
  gap: 10px;
  flex: 1;
  padding-top: 20px;
}

.nav-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.95);
  text-decoration: none;
  padding: 12px 18px;
  border-radius: 999px;
  line-height: 1.35;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  box-sizing: border-box;
  transition: background 0.2s, color 0.2s, box-shadow 0.2s;
}

.nav-item:hover,
.nav-item.active {
  background: rgba(255, 255, 255, 0.22);
  color: white;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
}

.language-switch {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.28);
  border: 2px solid rgba(255, 255, 255, 0.65);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.18),
    0 2px 10px rgba(0, 0, 0, 0.22);
}

.lang-btn {
  margin: 0;
  padding: 10px 18px;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.02em;
  line-height: 1.3;
  color: rgba(255, 255, 255, 0.96);
  background: transparent;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  transition: color 0.15s, background 0.15s, box-shadow 0.15s;
}

.lang-btn:not(.active) {
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}

.lang-btn:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.14);
}

.lang-btn.active {
  color: #312e81;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.username {
  color: white;
  font-weight: 600;
  font-size: 14px;
  line-height: 1.35;
  white-space: nowrap;
}

.logout-btn {
  margin: 0;
  padding: 12px 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 999px;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  line-height: 1.35;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
  transition: background 0.2s, border-color 0.2s, box-shadow 0.2s;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.28);
  border-color: rgba(255, 255, 255, 0.75);
}

.dashboard-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

.section-title {
  margin-bottom: 24px;
}

.section-title h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}

.section-title p {
  color: #666;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.active-users .stat-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.today-classes .stat-icon {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.pending-requests .stat-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.section h2 {
  font-size: 20px;
  color: #333;
  margin-bottom: 20px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
}

.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.schedule-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
}

.schedule-time {
  font-weight: 600;
  color: #333;
  min-width: 120px;
}

.schedule-info {
  flex: 1;
}

.schedule-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.schedule-meta {
  display: flex;
  gap: 16px;
  color: #666;
  font-size: 14px;
}

.schedule-status {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.schedule-status.upcoming {
  background: #dbeafe;
  color: #1d4ed8;
}

.schedule-status.pending {
  background: #fef3c7;
  color: #d97706;
}

.schedule-status.completed {
  background: #d1fae5;
  color: #059669;
}

.notifications-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
}

.notification-icon {
  font-size: 24px;
}

.notification-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.notification-time {
  color: #999;
  font-size: 14px;
}
</style>