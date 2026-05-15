<template>
  <UserLayout>
    <div class="checkin-container">
      <h2 class="page-title">✅ 每日打卡</h2>

      <div class="checkin-card">
        <div class="checkin-header">
          <div class="current-date">{{ currentDate }}</div>
          <div v-if="todayChecked" class="checked-badge">今日已打卡</div>
        </div>
        <div v-if="!todayChecked" class="checkin-btn-wrap">
          <el-button type="primary" size="large" @click="doCheckIn" class="checkin-btn">
            🎯 立即打卡
          </el-button>
          <div class="checkin-reward">打卡获得 {{ checkInPoints }} 积分</div>
        </div>
      </div>

      <div class="stats-card">
        <div class="stat-item">
          <div class="stat-value">{{ checkInDays }}</div>
          <div class="stat-label">累计打卡天数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ totalPoints }}</div>
          <div class="stat-label">获得积分</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ streakDays }}</div>
          <div class="stat-label">连续打卡</div>
        </div>
      </div>

      <div class="badges-section">
        <h3>🏅 成就勋章</h3>
        <div class="badges-grid">
          <div v-for="badge in badges" :key="badge.id" class="badge-item" :class="{ unlocked: badge.unlocked }">
            <div class="badge-icon">{{ badge.icon }}</div>
            <div class="badge-name">{{ badge.name }}</div>
            <div class="badge-desc">{{ badge.desc }}</div>
          </div>
        </div>
      </div>

      <div class="calendar-section">
        <h3>📆 打卡日历</h3>
        <div class="calendar-grid">
          <div v-for="day in calendarDays" :key="day.date" class="calendar-day" :class="{ checked: day.checked, future: day.future }">
            <div class="day-number">{{ day.day }}</div>
            <div class="day-status">{{ day.checked ? '✅' : '' }}</div>
          </div>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import UserLayout from '../components/UserLayout.vue'
import { ElButton } from 'element-plus'

const todayChecked = ref(false)
const checkInDays = ref(0)
const totalPoints = ref(0)
const streakDays = ref(0)
const checkInPoints = ref(10)

const currentDate = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
})

const badges = ref([
  { id: 1, name: '初出茅庐', icon: '🌱', desc: '累计打卡7天', unlocked: false },
  { id: 2, name: '坚持不懈', icon: '🔥', desc: '累计打卡30天', unlocked: false },
  { id: 3, name: '健身达人', icon: '💪', desc: '累计打卡100天', unlocked: false },
  { id: 4, name: '连续7天', icon: '📅', desc: '连续打卡7天', unlocked: false },
  { id: 5, name: '健身传奇', icon: '👑', desc: '累计打卡365天', unlocked: false }
])

const calendarDays = ref([])

const generateCalendar = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth()
  const firstDay = new Date(year, month, 1).getDay()
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const today = now.getDate()
  const days = []
  for (let i = 0; i < firstDay; i++) days.push({ date: `empty-${i}`, day: '', checked: false, future: false })
  for (let i = 1; i <= daysInMonth; i++) days.push({ date: `${year}-${month + 1}-${i}`, day: i, checked: i < today && Math.random() > 0.3, future: i > today })
  calendarDays.value = days
}

const doCheckIn = async () => {
  try {
    const token = localStorage.getItem('token')
    await axios.post('/api/checkin', {}, { headers: { Authorization: `Bearer ${token}` } })
    todayChecked.value = true
    checkInDays.value++
    totalPoints.value += checkInPoints.value
    ElMessage.success('打卡成功！')
  } catch (err) {
    ElMessage.error('打卡失败')
  }
}

onMounted(() => {
  generateCalendar()
})
</script>

<style scoped>
.checkin-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  text-align: center;
  margin-bottom: 30px;
}

.checkin-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 40px;
  text-align: center;
  color: white;
  margin-bottom: 25px;
}

.checkin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.current-date {
  font-size: 18px;
}

.checked-badge {
  background: rgba(255, 255, 255, 0.3);
  padding: 8px 20px;
  border-radius: 20px;
  font-weight: 600;
}

.checkin-btn {
  width: 200px;
  height: 60px;
  font-size: 18px;
  border-radius: 30px;
}

.checkin-reward {
  margin-top: 15px;
  opacity: 0.9;
}

.stats-card {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  margin-bottom: 25px;
}

.stat-item {
  background: white;
  border-radius: 15px;
  padding: 25px;
  text-align: center;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #667eea;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.badges-section,
.calendar-section {
  background: white;
  border-radius: 15px;
  padding: 25px;
  margin-bottom: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.badges-section h3,
.calendar-section h3 {
  margin-bottom: 20px;
}

.badges-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
}

.badge-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 10px;
  opacity: 0.5;
}

.badge-item.unlocked {
  opacity: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.badge-icon {
  font-size: 36px;
  margin-bottom: 10px;
}

.badge-name {
  font-weight: 600;
  margin-bottom: 5px;
}

.badge-desc {
  font-size: 13px;
  opacity: 0.8;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.calendar-day {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: #f8f9fa;
  font-size: 14px;
}

.calendar-day.checked {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.calendar-day.future {
  opacity: 0.3;
}

.day-number {
  font-weight: 600;
}

.day-status {
  font-size: 12px;
}
</style>
