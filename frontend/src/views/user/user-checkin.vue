<template>
  <div class="user-dashboard">
    <header class="user-header">
      <div class="header-content">
        <div class="logo">
          <span class="logo-icon">💪</span>
          <span class="logo-text">FitLife</span>
        </div>
        <nav class="nav">
          <router-link to="/user/dashboard" class="nav-item">{{ t('common.dashboard') }}</router-link>
          <router-link to="/user/classes" class="nav-item">{{ t('common.classBooking') }}</router-link>
          <router-link to="/user/profile" class="nav-item">{{ t('common.myProfile') }}</router-link>
          <router-link to="/user/points-mall" class="nav-item">{{ t('common.pointsMall') }}</router-link>
          <router-link to="/user/social" class="nav-item">{{ t('common.community') }}</router-link>
          <router-link to="/user/checkin" class="nav-item">{{ t('common.checkin') }}</router-link>
          <router-link to="/user/ranking" class="nav-item">{{ t('common.ranking') }}</router-link>
        </nav>
        <div class="user-info">
          <div class="language-switch">
            <button @click="switchLang('zh')" class="lang-btn" :class="{ active: currentLang === 'zh' }">
              {{ t('common.chinese') }}
            </button>
            <button @click="switchLang('en')" class="lang-btn" :class="{ active: currentLang === 'en' }">
              {{ t('common.english') }}
            </button>
          </div>
          <span class="points">🏆 {{ points }} {{ t('common.points') }}</span>
          <span class="username">{{ username }}</span>
          <button class="logout-btn" @click="logout">{{ t('common.logout') }}</button>
        </div>
      </div>
    </header>
    
    <main class="main-content">
      <div class="checkin-card">
        <div class="checkin-header">
          <div class="date-info">
            <div class="current-date">{{ currentDate }}</div>
            <div class="weekday">{{ currentWeekday }}</div>
            <div class="streak-info">🔥 {{ t('user.checkin.streak') }} {{ streakDays }} {{ t('user.checkin.days') }}</div>
          </div>
          <button v-if="!hasCheckedInToday" class="checkin-btn" @click="checkin">
            <span class="checkin-icon">✅</span>
            <span>{{ t('user.checkin.checkin') }}</span>
          </button>
          <div v-else class="checked-in-badge">
            <span class="checkin-icon">🎉</span>
            <span>{{ t('user.checkin.checkedIn') }}</span>
          </div>
        </div>
        
        <div class="calendar-section">
          <h3>📅 {{ t('user.checkin.monthRecord') }}</h3>
          <div class="calendar-grid">
            <div v-for="day in calendarDays" :key="day.date" class="calendar-day" :class="{ checked: day.checked, today: day.isToday, future: day.isFuture }">
              <div class="day-number">{{ day.day }}</div>
              <div v-if="day.checked" class="check-mark">✅</div>
            </div>
          </div>
        </div>
        
        <div class="badges-section">
          <h3>🏆 {{ t('user.checkin.myBadges') }}</h3>
          <div class="badges-grid">
            <div v-for="badge in badges" :key="badge.id" class="badge-card" :class="{ locked: !badge.unlocked, unlocked: badge.unlocked }">
              <div class="badge-icon">{{ badge.icon }}</div>
              <div class="badge-name">{{ t(`user.checkin.badges.${badge.nameKey}`, badge.nameKey) }}</div>
              <div class="badge-desc">{{ t(`user.checkin.badges.${badge.descKey}`, badge.descKey) }}</div>
            </div>
          </div>
        </div>
        
        <div class="rewards-section">
          <h3>🎁 {{ t('user.checkin.rewards') }}</h3>
          <div class="rewards-list">
            <div v-for="reward in rewards" :key="reward.id" class="reward-item">
              <div class="reward-info">
                <div class="reward-name">{{ t(`user.checkin.rewardItems.${reward.nameKey}`, reward.nameKey) }}</div>
                <div class="reward-desc">{{ t(`user.checkin.rewardItems.${reward.descKey}`, reward.descKey) }}</div>
              </div>
              <div class="reward-value">+{{ reward.points }} {{ t('common.points') }}</div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { clearSession, getCurrentUsername } from '../../services/session'
import { checkInToday, getCheckinStatus, getMonthlyCheckinRecords } from '../../services/checkinService'

const router = useRouter()
const { t, locale, tm } = useI18n()
const currentLang = computed(() => locale.value)

const switchLang = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

const username = ref(getCurrentUsername('用户'))
const points = ref(0)
const streakDays = ref(0)
const hasCheckedInToday = ref(false)
const loading = ref(true)
const checkinRecords = ref([])

const currentDate = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}${t('user.checkin.year')}${now.getMonth() + 1}${t('user.checkin.month')}${now.getDate()}${t('user.checkin.day')}`
})

const currentWeekday = computed(() => {
  const now = new Date()
  const weekdays = tm('user.checkin.weekdays')
  return Array.isArray(weekdays) ? weekdays[now.getDay()] : ''
})

const calendarDays = ref([])

const badges = ref([
  { id: 1, icon: '🥉', nameKey: '初露锋芒', descKey: '完成首次打卡', unlocked: false },
  { id: 2, icon: '🥈', nameKey: '坚持不懈', descKey: '连续打卡7天', unlocked: false },
  { id: 3, icon: '🥇', nameKey: '持之以恒', descKey: '连续打卡30天', unlocked: false },
  { id: 4, icon: '💎', nameKey: '钻石会员', descKey: '连续打卡100天', unlocked: false },
])

const rewards = ref([
  { id: 1, nameKey: '每日打卡', descKey: '每日首次打卡奖励', points: 10 },
  { id: 2, nameKey: '连续7天', descKey: '连续打卡7天奖励', points: 50 },
  { id: 3, nameKey: '连续30天', descKey: '连续打卡30天奖励', points: 200 },
])

const loadCheckinStatus = async () => {
  try {
    const result = await getCheckinStatus()
    if (result.success) {
      const data = result.data || {}
      hasCheckedInToday.value = Boolean(data.hasCheckedInToday)
      streakDays.value = data.consecutiveDays || 0
      points.value = data.availablePoints || 0
      updateBadges()
    }
  } catch (error) {
    console.error('加载打卡状态失败:', error)
  }
}

const loadMonthCheckinRecords = async () => {
  try {
    const now = new Date()
    const result = await getMonthlyCheckinRecords(now.getFullYear(), now.getMonth() + 1)
    if (result.success) {
      checkinRecords.value = result.records || result.data || []
    }
  } catch (error) {
    console.error('加载打卡记录失败:', error)
  } finally {
    generateCalendarDays()
    loading.value = false
  }
}

const updateBadges = () => {
  badges.value[0].unlocked = streakDays.value >= 1
  badges.value[1].unlocked = streakDays.value >= 7
  badges.value[2].unlocked = streakDays.value >= 30
  badges.value[3].unlocked = streakDays.value >= 100
}

const logout = () => {
  clearSession()
  router.push('/user/login')
}

const checkin = async () => {
  try {
    const result = await checkInToday()
    if (result.success) {
      const earnedPoints = result.record?.pointsEarned || result.data?.pointsEarned || 10
      hasCheckedInToday.value = true
      const todayItem = calendarDays.value.find(day => day.day === new Date().getDate())
      if (todayItem) todayItem.checked = true
      await Promise.all([loadCheckinStatus(), loadMonthCheckinRecords()])
      showSuccessToast(earnedPoints)
    } else {
      showErrorToast(result.message || t('user.checkin.checkinFailed'))
    }
  } catch (error) {
    showErrorToast(error.response?.data?.message || t('user.checkin.checkinFailed'))
  }
}

const showSuccessToast = (earnedPoints = 10) => {
  const toast = document.createElement('div')
  toast.className = 'toast'
  toast.innerHTML = `
    <div class="toast-content">
      <span>🎉</span>
      <span>${t('user.checkin.checkinSuccess')} +${earnedPoints} ${t('common.points')}</span>
    </div>
  `
  document.body.appendChild(toast)
  
  setTimeout(() => {
    toast.classList.add('show')
  }, 10)
  
  setTimeout(() => {
    toast.classList.remove('show')
    setTimeout(() => {
      document.body.removeChild(toast)
    }, 300)
  }, 2000)
}

const showErrorToast = (message) => {
  const toast = document.createElement('div')
  toast.className = 'toast error'
  toast.innerHTML = `
    <div class="toast-content">
      <span>❌</span>
      <span>${message}</span>
    </div>
  `
  document.body.appendChild(toast)
  
  setTimeout(() => {
    toast.classList.add('show')
  }, 10)
  
  setTimeout(() => {
    toast.classList.remove('show')
    setTimeout(() => {
      document.body.removeChild(toast)
    }, 300)
  }, 3000)
}

const generateCalendarDays = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth()
  const today = now.getDate()
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const checkedDates = new Set(
    checkinRecords.value.map(record => new Date(record.checkinDate).getDate())
  )

  calendarDays.value = Array.from({ length: daysInMonth }, (_, index) => {
    const day = index + 1
    return {
      date: day,
      day,
      checked: checkedDates.has(day),
      isToday: day === today,
      isFuture: day > today
    }
  })
}

onMounted(async () => {
  await Promise.all([
    loadCheckinStatus(),
    loadMonthCheckinRecords()
  ])
})
</script>

<style scoped>
.user-dashboard {
  min-height: 100vh;
  background: #f8fafc;
}

.main-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px;
}

.checkin-card {
  background: white;
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.checkin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 2px solid #f1f5f9;
}

.date-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.current-date {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
}

.weekday {
  font-size: 16px;
  color: #6b7280;
  font-weight: 500;
}

.streak-info {
  font-size: 18px;
  color: #f59e0b;
  font-weight: 600;
}

.checkin-btn {
  padding: 16px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 16px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.checkin-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.checkin-btn:active {
  transform: translateY(0);
}

.checkin-icon {
  font-size: 24px;
}

.checked-in-badge {
  padding: 16px 32px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border-radius: 16px;
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.8;
  }
}

.calendar-section {
  margin-bottom: 40px;
}

.calendar-section h3 {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 24px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 12px;
}

.calendar-day {
  aspect-ratio: 1;
  background: #f8fafc;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.3s;
  position: relative;
  border: 2px solid transparent;
}

.calendar-day:hover:not(.future) {
  background: #f1f5f9;
  transform: translateY(-2px);
}

.calendar-day.checked {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  border-color: #10b981;
}

.calendar-day.today {
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
  border-color: #667eea;
  transform: scale(1.05);
}

.calendar-day.future {
  opacity: 0.4;
  cursor: not-allowed;
}

.day-number {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
}

.calendar-day.checked .day-number {
  color: #065f46;
}

.calendar-day.today .day-number {
  color: #3730a3;
}

.check-mark {
  font-size: 14px;
  animation: checkIn 0.3s ease-out;
}

@keyframes checkIn {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

.badges-section {
  margin-bottom: 40px;
}

.badges-section h3 {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 24px;
}

.badges-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.badge-card {
  background: #f8fafc;
  border-radius: 20px;
  padding: 24px 16px;
  text-align: center;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.badge-card:hover {
  transform: translateY(-4px);
}

.badge-card.unlocked {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-color: #f59e0b;
  box-shadow: 0 4px 15px rgba(245, 158, 11, 0.2);
}

.badge-card.locked {
  filter: grayscale(0.8);
  opacity: 0.6;
}

.badge-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.badge-name {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 6px;
}

.badge-desc {
  font-size: 12px;
  color: #6b7280;
}

.rewards-section h3 {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 24px;
}

.rewards-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.reward-item {
  background: #f8fafc;
  border-radius: 16px;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.reward-item:hover {
  background: #f1f5f9;
  border-color: #e2e8f0;
  transform: translateX(4px);
}

.reward-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.reward-name {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
}

.reward-desc {
  font-size: 14px;
  color: #6b7280;
}

.reward-value {
  font-size: 18px;
  font-weight: 700;
  color: #f59e0b;
}

.toast {
  position: fixed;
  top: 100px;
  left: 50%;
  transform: translateX(-50%) translateY(-20px);
  background: white;
  padding: 20px 40px;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  z-index: 9999;
  opacity: 0;
  transition: all 0.3s;
}

.toast.show {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

.toast.error {
  background: #fef2f2;
  border: 1px solid #fecaca;
}

.toast.error .toast-content {
  color: #dc2626;
}

.toast-content {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}
</style>

