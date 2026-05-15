<template>
  <div class="coach-profile">
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
          <span class="username">{{ username }}</span>
          <button class="logout-btn" @click="logout">{{ t('common.logout') }}</button>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="profile-content">
      <div class="section-title">
        <h1>👤 {{ t('coach.profile.title') }}</h1>
        <p>{{ t('coach.profile.subtitle') }}</p>
      </div>

      <div class="profile-grid">
        <!-- 个人信息卡片 -->
        <div class="profile-card">
          <h2>{{ t('coach.profile.info') }}</h2>
          <div class="profile-info">
            <div class="avatar-section">
              <div class="avatar">👨‍🏫</div>
              <div class="avatar-info">
                <h3>{{ profile.name }}</h3>
                <p class="title">{{ t('coach.profile.certified') }}</p>
              </div>
            </div>
            
            <div class="info-row">
              <label>{{ t('coach.profile.username') }}</label>
              <span>{{ profile.username }}</span>
            </div>
            <div class="info-row">
              <label>{{ t('coach.profile.email') }}</label>
              <span>{{ profile.email }}</span>
            </div>
            <div class="info-row">
              <label>{{ t('coach.profile.phone') }}</label>
              <span>{{ profile.phone }}</span>
            </div>
            <div class="info-row">
              <label>{{ t('coach.profile.experience') }}</label>
              <span>{{ profile.experience }}{{ t('coach.profile.years') }}</span>
            </div>
            <div class="info-row">
              <label>{{ t('coach.profile.certifications') }}</label>
              <span>{{ displayCertifications(profile.certifications) }}</span>
            </div>
            <div class="info-row">
              <label>{{ t('coach.profile.specialty') }}</label>
              <span>{{ displaySpecialty(profile.specialty) }}</span>
            </div>
            <div class="info-row">
              <label>{{ t('coach.profile.hourlyRate') }}</label>
              <span>¥{{ profile.hourlyRate }}/{{ t('coach.profile.hour') }}</span>
            </div>
            <div class="info-row">
              <label>{{ t('coach.profile.rating') }}</label>
              <span class="rating">⭐ {{ profile.rating }}</span>
            </div>
          </div>
        </div>

        <!-- 上课提醒设置 -->
        <div class="profile-card">
          <h2>{{ t('coach.profile.notifications') }}</h2>
          <div class="notification-settings">
            <div class="setting-item">
              <div class="setting-info">
                <h4>{{ t('coach.profile.classReminder') }}</h4>
                <p>{{ t('coach.profile.classReminderDesc') }}</p>
              </div>
              <div class="toggle" :class="{ active: notificationSettings.classReminder }" @click="notificationSettings.classReminder = !notificationSettings.classReminder">
                <div class="toggle-knob"></div>
              </div>
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>{{ t('coach.profile.bookingNotification') }}</h4>
                <p>{{ t('coach.profile.bookingNotificationDesc') }}</p>
              </div>
              <div class="toggle" :class="{ active: notificationSettings.bookingNotification }" @click="notificationSettings.bookingNotification = !notificationSettings.bookingNotification">
                <div class="toggle-knob"></div>
              </div>
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>{{ t('coach.profile.reviewNotification') }}</h4>
                <p>{{ t('coach.profile.reviewNotificationDesc') }}</p>
              </div>
              <div class="toggle" :class="{ active: notificationSettings.reviewNotification }" @click="notificationSettings.reviewNotification = !notificationSettings.reviewNotification">
                <div class="toggle-knob"></div>
              </div>
            </div>
            <div class="setting-item">
              <div class="setting-info">
                <h4>{{ t('coach.profile.complaintNotification') }}</h4>
                <p>{{ t('coach.profile.complaintNotificationDesc') }}</p>
              </div>
              <div class="toggle" :class="{ active: notificationSettings.complaintNotification }" @click="notificationSettings.complaintNotification = !notificationSettings.complaintNotification">
                <div class="toggle-knob"></div>
              </div>
            </div>
          </div>
          <button class="save-btn" @click="saveSettings">{{ t('coach.profile.save') }}</button>
        </div>

        <!-- 人流统计说明（入场码在会员个人中心） -->
        <div class="profile-card qr-card">
          <h2>{{ t('coach.profile.crowdNoteTitle') }}</h2>
          <p class="qr-desc">{{ t('coach.profile.crowdNoteDesc') }}</p>
          <p class="qr-desc">
            <a href="/gym-scan" target="_blank" rel="noopener" class="staff-link">{{ t('coach.profile.staffScanLink') }}</a>
          </p>
        </div>

        <!-- 统计信息 -->
        <div class="stats-card">
          <h2>📊 {{ t('coach.profile.stats') }}</h2>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-num">{{ stats.totalClasses }}</div>
              <div class="stat-name">{{ t('coach.profile.totalClasses') }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-num">{{ stats.totalStudents }}</div>
              <div class="stat-name">{{ t('coach.profile.totalStudents') }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-num">{{ stats.avgRating }}</div>
              <div class="stat-name">{{ t('coach.profile.avgRating') }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-num">{{ stats.thisMonthClasses }}</div>
              <div class="stat-name">{{ t('coach.profile.thisMonthClasses') }}</div>
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
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()
const currentLang = computed(() => locale.value)

const switchLang = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

const translateListText = (text, map) => {
  const raw = String(text || '')
  if (locale.value !== 'en') return raw
  return raw
    .split(/[、,，]/)
    .map((part) => map[part.trim()] || part.trim())
    .filter(Boolean)
    .join(', ')
}

const certificationMap = {
  '国家级健身教练认证': 'National Fitness Coach Certification',
  '高级私教证书': 'Advanced Personal Trainer Certificate'
}

const specialtyMap = {
  '力量训练': 'Strength Training',
  '增肌塑形': 'Muscle Gain & Body Shaping',
  '功能性训练': 'Functional Training',
  '瑜伽放松': 'Yoga Relaxation',
  '普拉提': 'Pilates',
  '柔韧性训练': 'Flexibility Training',
  '动感单车': 'Spinning',
  '有氧训练': 'Cardio Training',
  '减脂': 'Fat Loss',
  '搏击训练': 'Boxing Training',
  '体能训练': 'Conditioning',
  '爆发力': 'Power Training'
}

const displayCertifications = (text) => translateListText(text, certificationMap)
const displaySpecialty = (text) => translateListText(text, specialtyMap)

const username = computed(() => localStorage.getItem('username') || t('common.coach'))
const currentPage = ref('profile')

const profile = ref({
  name: '',
  username: '',
  email: '',
  phone: '',
  experience: 0,
  certifications: '',
  specialty: '',
  hourlyRate: 0,
  rating: 0
})

const notificationSettings = ref({
  classReminder: true,
  bookingNotification: true,
  reviewNotification: true,
  complaintNotification: true
})

const stats = ref({
  totalClasses: 0,
  totalStudents: 0,
  avgRating: 0,
  thisMonthClasses: 0
})

const getAuthHeader = () => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

const loadProfile = async () => {
  try {
    const response = await axios.get('/api/coach/profile', { headers: getAuthHeader() })
    if (response.data.success) {
      profile.value = response.data.data
      stats.value = {
        totalClasses: response.data.data.totalClasses || 0,
        totalStudents: response.data.data.totalStudents || 0,
        avgRating: response.data.data.avgRating || response.data.data.rating || 0,
        thisMonthClasses: response.data.data.thisMonthClasses || 0
      }
      if (response.data.data.notificationSettings) {
        notificationSettings.value = {
          ...notificationSettings.value,
          ...response.data.data.notificationSettings
        }
      }
    }
  } catch (error) {
    console.error('加载个人信息失败:', error)
  }
}

const saveSettings = async () => {
  try {
    const response = await axios.post('/api/coach/notification-settings', notificationSettings.value, { headers: getAuthHeader() })
    if (response.data.success) {
      ElMessage.success(t('coach.profile.saveSuccess'))
    }
  } catch (error) {
    ElMessage.error(t('coach.profile.saveFailed'))
  }
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userType')
  localStorage.removeItem('coachId')
  window.location.href = '/coach/login'
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.coach-profile {
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

.profile-content {
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

.profile-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.profile-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.profile-card h2 {
  font-size: 20px;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
}

.avatar-info h3 {
  font-size: 24px;
  color: #333;
  margin-bottom: 4px;
}

.avatar-info .title {
  color: #667eea;
  font-weight: 600;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.info-row label {
  color: #666;
}

.info-row span {
  color: #333;
  font-weight: 500;
}

.rating {
  color: #f59e0b;
}

.notification-settings {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
}

.setting-info h4 {
  color: #333;
  margin-bottom: 4px;
}

.setting-info p {
  color: #999;
  font-size: 14px;
}

.toggle {
  width: 50px;
  height: 28px;
  background: #e5e7eb;
  border-radius: 14px;
  position: relative;
  cursor: pointer;
  transition: background 0.3s;
}

.toggle.active {
  background: #667eea;
}

.toggle-knob {
  width: 24px;
  height: 24px;
  background: white;
  border-radius: 50%;
  position: absolute;
  top: 2px;
  left: 2px;
  transition: left 0.3s;
}

.toggle.active .toggle-knob {
  left: 24px;
}

.save-btn {
  margin-top: 20px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.3s;
}

.save-btn:hover {
  opacity: 0.9;
}

.staff-link {
  color: #667eea;
  font-weight: 600;
}

.qr-content {
  text-align: center;
}

.qr-placeholder {
  width: 200px;
  height: 200px;
  margin: 0 auto 20px;
  background: #f8fafc;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.qr-icon {
  font-size: 64px;
}

.qr-text {
  color: #999;
}

.qr-desc {
  color: #666;
  margin-bottom: 20px;
}

.qr-info {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  background: #f8fafc;
  border-radius: 20px;
}

.badge {
  background: #667eea;
  color: white;
  padding: 4px 12px;
  border-radius: 10px;
  font-size: 12px;
}

.code {
  font-family: monospace;
  font-weight: bold;
  color: #333;
}

.stats-card {
  grid-column: span 2;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
}

.stat-num {
  font-size: 32px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 8px;
}

.stat-name {
  color: #666;
  font-size: 14px;
}
</style>