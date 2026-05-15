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
          <span class="points">🏆 {{ points }} {{ t('common.points') }}</span>
          <span class="username">{{ username }}</span>
          <button class="logout-btn" @click="logout">{{ t('common.logout') }}</button>
        </div>
      </div>
    </header>
    
    <main class="main-content">
      <div class="profile-header">
        <div class="avatar-section">
          <div class="avatar">👤</div>
          <div class="user-details">
            <h1>{{ username }}</h1>
            <div class="member-badge">VIP {{ t('user.profile.member') }}</div>
            <div class="member-expire">{{ t('user.profile.expire') }} 2025-12-31</div>
          </div>
        </div>
        <div class="stats-row">
          <div class="mini-stat">
            <div class="mini-value">{{ streakDays }}</div>
            <div class="mini-label">{{ t('user.dashboard.consecutiveDays') }}</div>
          </div>
          <div class="mini-stat">
            <div class="mini-value">{{ badges }}</div>
            <div class="mini-label">{{ t('user.dashboard.badges') }}</div>
          </div>
          <div class="mini-stat">
            <div class="mini-value">{{ totalCheckins }}</div>
            <div class="mini-label">{{ t('user.profile.totalCheckins') }}</div>
          </div>
        </div>
      </div>
      
      <div class="tabs-container">
        <div class="tabs">
          <button 
            class="tab" 
            :class="{ active: activeTab === 'profile' }" 
            @click="activeTab = 'profile'"
          >
            {{ t('user.profile.basicInfo') }}
          </button>
          <button 
            class="tab" 
            :class="{ active: activeTab === 'schedule' }" 
            @click="activeTab = 'schedule'"
          >
            {{ t('user.profile.myBookings') }}
          </button>
          <button 
            class="tab" 
            :class="{ active: activeTab === 'complaint' }" 
            @click="activeTab = 'complaint'"
          >
            {{ t('user.profile.complaint') }}
          </button>
          <button 
            class="tab" 
            :class="{ active: activeTab === 'points' }" 
            @click="activeTab = 'points'"
          >
            {{ t('common.points') }}
          </button>
        </div>
        
        <div v-if="activeTab === 'profile'" class="tab-content">
          <div class="form-section">
            <h3>{{ t('user.profile.basicInfo') }}</h3>
            <div class="form-row">
              <div class="form-item">
                <label>{{ t('common.username') }}</label>
                <input type="text" v-model="profile.username" class="form-input" disabled>
              </div>
              <div class="form-item">
                <label>{{ t('common.name') }}</label>
                <input type="text" v-model="profile.name" class="form-input">
              </div>
            </div>
            <div class="form-row">
              <div class="form-item">
                <label>{{ t('common.email') }}</label>
                <input type="email" v-model="profile.email" class="form-input">
              </div>
              <div class="form-item">
                <label>{{ t('common.phone') }}</label>
                <input type="tel" v-model="profile.phone" class="form-input">
              </div>
            </div>
            <div class="form-row">
              <div class="form-item">
                <label>{{ t('user.profile.gender') }}</label>
                <select v-model="profile.gender" class="form-input">
                  <option value="male">{{ t('user.profile.male') }}</option>
                  <option value="female">{{ t('user.profile.female') }}</option>
                </select>
              </div>
              <div class="form-item">
                <label>{{ t('user.profile.birthday') }}</label>
                <input type="date" v-model="profile.birthday" class="form-input">
              </div>
            </div>
            <div class="form-row">
              <div class="form-item full-width">
                <label>{{ t('user.profile.address') }}</label>
                <input type="text" v-model="profile.address" class="form-input">
              </div>
            </div>
            <div class="form-row">
              <div class="form-item full-width">
                <label>{{ t('user.profile.medicalNotes') }}</label>
                <textarea v-model="profile.medicalNotes" class="form-input" rows="2" :placeholder="t('user.profile.medicalNotesPlaceholder')"></textarea>
              </div>
            </div>
            <div class="gym-qr-section">
              <h3>{{ t('user.profile.gymQrTitle') }}</h3>
              <p class="gym-qr-desc">{{ t('user.profile.gymQrDesc') }}</p>
              <div v-if="checkinQrImageUrl" class="gym-qr-wrap">
                <img :src="checkinQrImageUrl" alt="Check-in QR" class="gym-qr-img" />
              </div>
              <p v-else class="gym-qr-loading">{{ t('user.profile.gymQrLoading') }}</p>
              <p class="gym-qr-staff">
                <router-link to="/gym-scan" target="_blank">{{ t('user.profile.staffScanLink') }}</router-link>
              </p>
            </div>
            <button class="save-btn" @click="saveProfile">{{ t('user.profile.saveChanges') }}</button>
          </div>
        </div>

        <div v-else-if="activeTab === 'schedule'" class="tab-content">
          <div class="schedule-section">
            <div class="section-title-row">
              <div>
                <h3>{{ t('user.profile.myBookings') }}</h3>
                <p>{{ t('user.profile.manageBookingsHint') }}</p>
              </div>
              <button class="outline-btn" @click="goToClasses">{{ t('user.profile.goBookMore') }}</button>
            </div>
            <div class="week-tabs">
              <button v-for="day in scheduleWeekDays" :key="day.date" class="week-tab" :class="{ active: selectedScheduleDay === day.date }" @click="selectScheduleDay(day.date)">{{ day.label }}</button>
            </div>
            <div class="schedule-list">
              <div v-if="filteredScheduleClasses.length === 0" class="empty-state">{{ t('user.classes.noCourses') }}</div>
              <div v-for="classItem in filteredScheduleClasses" :key="classItem.id" class="schedule-item">
                <div class="class-time">{{ classItem.time }}</div>
                <div class="class-info">
                  <div class="class-name">{{ displayCourseName(classItem.name) }}</div>
                  <div class="class-trainer">{{ t('common.trainer') }}: {{ classItem.trainer }}</div>
                </div>
                <div class="schedule-actions">
                  <div class="class-status">{{ getScheduleStatusName(classItem.status) }}</div>
                  <button class="cancel-booking-btn" @click="cancelMyBooking(classItem)">{{ t('user.profile.cancelBooking') }}</button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else-if="activeTab === 'complaint'" class="tab-content">
          <div class="complaint-section">
            <h3>{{ t('user.profile.complaint') }}</h3>
            <div class="form-row">
              <div class="form-item">
                <label>{{ t('user.profile.complaintType') }}</label>
                <select v-model="complaintType" class="form-input">
                  <option value="course">{{ t('user.profile.complaintCourse') }}</option>
                  <option value="coach">{{ t('user.profile.complaintCoach') }}</option>
                  <option value="facility">{{ t('user.profile.complaintFacility') }}</option>
                  <option value="other">{{ t('user.profile.complaintOther') }}</option>
                </select>
              </div>
            </div>
            <textarea v-model="complaintContent" :placeholder="t('user.profile.complaintPlaceholder')" class="complaint-textarea"></textarea>
            <button class="submit-btn" @click="submitComplaint">{{ t('user.profile.submitComplaint') }}</button>
          </div>
          
          <!-- 投诉记录 -->
          <div class="complaints-history">
            <h3>{{ t('user.profile.complaintHistory') }}</h3>
            <div v-if="complaints.length === 0" class="empty-state">
              {{ t('user.profile.noComplaints') }}
            </div>
            <div v-else class="complaints-list">
              <div v-for="complaint in complaints" :key="complaint.id" class="complaint-item">
                <div class="complaint-header">
                  <span class="complaint-type">{{ getComplaintTypeName(complaint.type) }}</span>
                  <span class="complaint-status" :class="complaint.status?.toLowerCase()">
                    {{ getComplaintStatusName(complaint.status) }}
                  </span>
                </div>
                <div class="complaint-content">{{ complaint.content }}</div>
                <div class="complaint-footer">
                  <span class="complaint-time">{{ formatComplaintTime(complaint.createdAt) }}</span>
                  <div v-if="complaint.response" class="complaint-response">
                    <strong>{{ t('user.profile.response') }}：</strong>{{ complaint.response }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else class="tab-content">
          <div class="points-section">
            <h3>{{ t('user.profile.myPoints') }}</h3>
            <div class="points-display">
              <div class="points-value">{{ points }}</div>
              <div class="points-label">{{ t('common.points') }}</div>
            </div>
            <button class="go-mall-btn" @click="goToPointsMall">🎁 {{ t('user.profile.goToMall') }}</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watchEffect, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { formatCourseName } from '../../utils/courseName'
import { buildUpcomingDays, formatTimeRange, toDateKey } from '../../utils/date'
import { clearSession, getCurrentUsername } from '../../services/session'
import { getCheckinStatus } from '../../services/checkinService'
import { cancelBookingByClass, getClassList, getMyBookings } from '../../services/classBookingService'
import { getCheckinQrToken, getUserComplaints, getUserProfile, submitComplaint as submitComplaintRequest, updateUserProfile } from '../../services/userService'

const router = useRouter()
const route = useRoute()
const { t, te, locale } = useI18n()
const currentLang = computed(() => locale.value)
const username = ref(getCurrentUsername('用户'))

const points = ref(0)
const totalPoints = ref(0)
const streakDays = ref(0)
const badges = ref(0)
const totalCheckins = ref(0)
const activeTab = ref('profile')
const complaintContent = ref('')
const complaintType = ref('course')
const complaints = ref([])
const checkinQrToken = ref('')
const scheduleClasses = ref([])
const scheduleWeekDays = ref([])
const selectedScheduleDay = ref('')

const profile = reactive({
  username: username.value,
  name: '',
  gender: 'male',
  email: '',
  phone: '',
  birthday: '',
  address: '',
  medicalNotes: ''
})

const displayCourseName = (name) => formatCourseName(name, t, te)
const checkinQrImageUrl = computed(() => checkinQrToken.value
  ? `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(checkinQrToken.value)}`
  : '')

const filteredScheduleClasses = computed(() => {
  if (!selectedScheduleDay.value) return scheduleClasses.value
  return scheduleClasses.value.filter(item => item.date === selectedScheduleDay.value)
})

const refreshDateTabLabels = () => {
  const previousScheduleDay = selectedScheduleDay.value
  scheduleWeekDays.value = buildUpcomingDays({ lang: locale.value, t, includeDateInLabel: true })
  selectedScheduleDay.value = previousScheduleDay || scheduleWeekDays.value[0]?.date || ''
}

const switchLang = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
  refreshDateTabLabels()
}

const selectScheduleDay = (date) => {
  selectedScheduleDay.value = date
}

const loadUserProfile = async () => {
  try {
    const result = await getUserProfile()
    if (!result.success) return
    const userData = result.data || {}
    Object.assign(profile, {
      username: userData.username || username.value,
      name: userData.name || '',
      email: userData.email || '',
      phone: userData.phone || '',
      gender: userData.gender || 'male',
      birthday: userData.birthday || '',
      address: userData.address || '',
      medicalNotes: userData.medicalNotes || ''
    })
  } catch (error) {
    console.error('加载用户资料失败:', error)
  }
}

const loadCheckinQrToken = async () => {
  try {
    const result = await getCheckinQrToken()
    if (result.success && result.data?.token) checkinQrToken.value = result.data.token
  } catch (error) {
    console.error('加载入场码失败:', error)
  }
}

const loadPointsData = async () => {
  try {
    const result = await getCheckinStatus()
    if (!result.success) return
    const data = result.data || {}
    points.value = data.availablePoints || 0
    totalPoints.value = data.totalPoints || data.availablePoints || 0
    streakDays.value = data.consecutiveDays || 0
    totalCheckins.value = data.totalCheckins || 0
    badges.value = [1, 7, 30, 100].filter(goal => Number(streakDays.value || 0) >= goal).length
  } catch (error) {
    console.error('加载积分数据失败:', error)
  }
}

const loadScheduleClasses = async () => {
  try {
    const bookingResult = await getMyBookings()
    const bookings = bookingResult.success && Array.isArray(bookingResult.data) ? bookingResult.data : []
    let classMap = {}

    if (bookings.some(booking => !booking.classes && booking.classId)) {
      try {
        const classResult = await getClassList()
        const classes = classResult.success && Array.isArray(classResult.data) ? classResult.data : []
        classMap = classes.reduce((map, item) => ({ ...map, [item.id]: item }), {})
      } catch (classError) {
        console.error('加载课程详情失败:', classError)
      }
    }

    const uniqueByClassId = new Map()
    bookings
      .filter(booking => booking.status === 'CONFIRMED' || booking.status === 'PENDING')
      .forEach(booking => {
        const course = booking.classes || classMap[booking.classId] || {}
        const classId = booking.classId || course.id
        const startTime = course.startTime || booking.startTime
        if (!classId || !startTime || uniqueByClassId.has(classId)) return

        uniqueByClassId.set(classId, {
          id: booking.id,
          classId,
          date: toDateKey(startTime),
          time: formatTimeRange(startTime, course.endTime || booking.endTime),
          name: course.name || booking.className || t('common.unknownCourse'),
          trainer: course.instructor?.name || course.instructor || course.coachName || booking.coachName || t('common.trainerPending'),
          status: booking.status
        })
      })

    scheduleClasses.value = Array.from(uniqueByClassId.values())
      .sort((a, b) => (a.date + a.time).localeCompare(b.date + b.time))
  } catch (error) {
    console.error('加载课表失败:', error)
    scheduleClasses.value = []
  }
}

const saveProfile = async () => {
  try {
    const result = await updateUserProfile(profile)
    if (result.success) {
      ElMessage.success(t('common.success'))
    } else {
      ElMessage.error(result.message || t('common.error'))
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(t('common.error'))
  }
}

const submitComplaint = async () => {
  if (!complaintContent.value.trim()) {
    ElMessage.warning(t('user.profile.complaintPlaceholder'))
    return
  }
  try {
    const result = await submitComplaintRequest({
      type: complaintType.value,
      content: complaintContent.value,
      contact: profile.phone
    })
    if (result.success) {
      ElMessage.success(t('user.profile.complaintSubmitted'))
      complaintContent.value = ''
      await loadComplaints()
    } else {
      ElMessage.error(result.message || t('common.error'))
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.response?.data?.message || t('common.error'))
  }
}

const loadComplaints = async () => {
  try {
    const result = await getUserComplaints()
    complaints.value = result.success && Array.isArray(result.data) ? result.data : []
  } catch (error) {
    console.error('加载投诉记录失败:', error)
    complaints.value = []
  }
}

const getComplaintTypeName = (type) => ({
  course: t('user.profile.complaintCourse'),
  coach: t('user.profile.complaintCoach'),
  facility: t('user.profile.complaintFacility'),
  other: t('user.profile.complaintOther')
}[type] || type)

const getComplaintStatusName = (status) => ({
  pending: t('user.profile.statusPending'),
  processing: t('user.profile.statusProcessing'),
  resolved: t('user.profile.statusResolved'),
  rejected: t('user.profile.statusRejected'),
  processed: t('user.profile.statusResolved'),
  PROCESSED: t('user.profile.statusResolved'),
  PENDING: t('user.profile.statusPending'),
  PROCESSING: t('user.profile.statusProcessing'),
  RESOLVED: t('user.profile.statusResolved'),
  REJECTED: t('user.profile.statusRejected')
}[status] || status)

const getScheduleStatusName = (status) => ({
  CONFIRMED: t('user.profile.statusConfirmed'),
  CANCELLED: t('user.profile.statusCancelled'),
  PENDING: t('user.profile.statusPending')
}[status] || status)

const formatComplaintTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  if (Number.isNaN(date.getTime())) return timeStr
  const pad = value => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

const cancelMyBooking = async (classItem) => {
  if (!classItem?.classId) return
  try {
    const result = await cancelBookingByClass(classItem.classId)
    if (result.success) {
      ElMessage.success(t('user.profile.bookingCancelled'))
      await Promise.all([loadScheduleClasses(), loadPointsData()])
    } else {
      ElMessage.error(result.message || t('common.error'))
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || t('common.error'))
  }
}

const logout = () => {
  clearSession()
  router.push('/user/login')
}

const goToPointsMall = () => router.push('/user/points-mall')
const goToClasses = () => router.push('/user/classes')

onMounted(() => {
  refreshDateTabLabels()
  loadUserProfile()
  loadCheckinQrToken()
  loadPointsData()
  loadScheduleClasses()
})

watchEffect(() => {
  if (route.query.tab === 'booking' || route.query.tab === 'schedule') activeTab.value = 'schedule'
})

watch(activeTab, (newTab) => {
  if (newTab === 'complaint') loadComplaints()
  if (newTab === 'schedule') loadScheduleClasses()
})

watch(locale, refreshDateTabLabels)
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

.profile-header {
  background: white;
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 25px;
  margin-bottom: 30px;
}

.avatar {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
}

.user-details h1 {
  font-size: 28px;
  margin: 0 0 10px 0;
}

.member-badge {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  padding: 4px 16px;
  border-radius: 12px;
  color: white;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 5px;
}

.member-expire {
  font-size: 14px;
  color: #666;
}

.stats-row {
  display: flex;
  gap: 30px;
}

.mini-stat {
  text-align: center;
}

.mini-value {
  font-size: 24px;
  font-weight: 700;
  color: #667eea;
}

.mini-label {
  font-size: 12px;
  color: #666;
}

.tabs-container {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.tabs {
  display: flex;
  border-bottom: 1px solid #eee;
}

.tab {
  padding: 15px 30px;
  background: transparent;
  border: none;
  cursor: pointer;
  font-weight: 500;
  color: #666;
  transition: all 0.3s;
  position: relative;
}

.tab:hover {
  color: #667eea;
}

.tab.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.tab-content {
  padding: 30px;
}

.form-section h3 {
  margin: 0 0 25px 0;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 25px;
  margin-bottom: 25px;
}

.form-row .form-item.full-width {
  grid-column: 1 / -1;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item label {
  font-weight: 600;
  font-size: 14px;
}

.form-input {
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  font-size: 16px;
}

.form-input:focus {
  border-color: #667eea;
  outline: none;
}

.form-input:disabled {
  background: #f8fafc;
  color: #999;
}

.save-btn {
  padding: 12px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
}

.schedule-section h3 {
  margin: 0 0 20px 0;
}

.week-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 25px;
}

.week-tab {
  padding: 10px 20px;
  background: #f8fafc;
  border: none;
  border-radius: 10px;
  cursor: pointer;
}

.week-tab.active {
  background: #667eea;
  color: white;
}

.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.schedule-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 15px;
}

.class-time {
  font-weight: 600;
  font-size: 18px;
  color: #667eea;
}

.class-name {
  font-weight: 600;
}

.class-trainer {
  font-size: 14px;
  color: #666;
}

.class-status {
  margin-left: auto;
  padding: 4px 12px;
  background: #d4edda;
  color: #155724;
  border-radius: 12px;
  font-size: 12px;
}

.complaint-section h3 {
  margin: 0 0 20px 0;
}

.complaint-textarea {
  width: 100%;
  height: 200px;
  padding: 16px;
  border: 2px solid #e5e7eb;
  border-radius: 15px;
  font-size: 16px;
  resize: none;
  margin-bottom: 20px;
  box-sizing: border-box;
}

.submit-btn {
  padding: 12px 30px;
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
}

.points-section h3 {
  margin: 0 0 25px 0;
}

.points-display {
  text-align: center;
  margin-bottom: 30px;
}

.points-value {
  font-size: 64px;
  font-weight: 700;
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.points-label {
  font-size: 18px;
  color: #666;
}

.go-mall-btn {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
  border: none;
  border-radius: 15px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
}

/* 预约课程样式 */
.booking-section h3 {
  margin: 0 0 20px 0;
}

.booking-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.booking-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 15px;
}

.booking-time {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 10px 20px;
  border-radius: 10px;
  font-weight: 600;
  min-width: 120px;
  text-align: center;
}

.booking-info {
  flex: 1;
}

.booking-name {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 5px;
}

.booking-trainer {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.booking-desc {
  font-size: 13px;
  color: #999;
}

.booking-status {
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 14px;
}

.booking-status.available {
  background: #d4edda;
  color: #155724;
}

.booking-status.full {
  background: #f8d7da;
  color: #721c24;
}

.booking-item .book-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.booking-item .book-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%);
}

.booking-item .book-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.booking-item .book-btn.booked {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  cursor: pointer;
}

/* 投诉记录样式 */
.complaints-history {
  margin-top: 30px;
  padding-top: 30px;
  border-top: 1px solid #eee;
}

.complaints-history h3 {
  margin: 0 0 20px 0;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
  background: #f8fafc;
  border-radius: 15px;
}

.complaints-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.complaint-item {
  background: #f8fafc;
  border-radius: 15px;
  padding: 20px;
}

.complaint-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.complaint-type {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.complaint-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.complaint-status.pending {
  background: #fff3cd;
  color: #856404;
}

.complaint-status.processing {
  background: #cce5ff;
  color: #004085;
}

.complaint-status.resolved {
  background: #d4edda;
  color: #155724;
}

.complaint-status.rejected {
  background: #f8d7da;
  color: #721c24;
}

.complaint-content {
  font-size: 15px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 15px;
}

.complaint-footer {
  font-size: 14px;
  color: #666;
}

.complaint-time {
  font-size: 13px;
  color: #999;
}

.complaint-response {
  margin-top: 10px;
  padding: 12px;
  background: #e8f5e9;
  border-radius: 10px;
  font-size: 14px;
  color: #155724;
}

.gym-qr-section {
  margin-top: 24px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}
.gym-qr-section h3 {
  margin: 0 0 8px;
  font-size: 16px;
  color: #334155;
}
.gym-qr-desc {
  margin: 0 0 12px;
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}
.gym-qr-wrap {
  display: flex;
  justify-content: center;
}
.gym-qr-img {
  width: 200px;
  height: 200px;
  border-radius: 8px;
  background: #fff;
}
.gym-qr-loading {
  text-align: center;
  color: #94a3b8;
  font-size: 14px;
}
.gym-qr-staff {
  margin-top: 12px;
  text-align: center;
  font-size: 14px;
}
.gym-qr-staff a {
  color: #4f46e5;
  font-weight: 600;
}
</style>


<style scoped>
/* Unified profile center polish */
.user-dashboard{background:radial-gradient(circle at top right,rgba(118,75,162,.12),transparent 32%),linear-gradient(180deg,#f7f8ff 0%,#f8fafc 52%,#eef2f7 100%)}.main-content{max-width:1080px;padding:34px 24px 72px}.profile-header{position:relative;overflow:hidden;display:grid;grid-template-columns:minmax(0,1.2fr) minmax(360px,.8fr);align-items:center;gap:28px;padding:34px;border-radius:30px;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);box-shadow:0 24px 70px rgba(102,126,234,.28);color:#fff}.profile-header:after{content:'';position:absolute;right:-70px;top:-90px;width:240px;height:240px;border-radius:50%;background:rgba(255,255,255,.14)}.avatar-section,.stats-row{position:relative;z-index:1;margin:0}.avatar{background:rgba(255,255,255,.18);box-shadow:inset 0 0 0 1px rgba(255,255,255,.25),0 18px 36px rgba(31,41,55,.16)}.user-details h1{color:#fff}.member-badge{display:inline-flex;background:rgba(245,158,11,.95)}.member-expire{color:rgba(255,255,255,.82)}.stats-row{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:12px}.mini-stat{padding:18px 12px;border-radius:22px;background:rgba(255,255,255,.14);border:1px solid rgba(255,255,255,.2);backdrop-filter:blur(10px)}.mini-value{color:#fff}.mini-label{color:rgba(255,255,255,.82)}.tabs-container{border-radius:28px;box-shadow:0 18px 50px rgba(15,23,42,.08);border:1px solid rgba(226,232,240,.8)}.tabs{padding:10px;gap:8px;border-bottom:1px solid #eef2f7;flex-wrap:wrap}.tab{border-radius:999px;padding:12px 20px}.tab.active{box-shadow:0 12px 26px rgba(102,126,234,.28)}.section-title-row{display:flex;justify-content:space-between;align-items:flex-start;gap:18px;margin-bottom:22px}.section-title-row h3{margin:0 0 8px;font-size:22px}.section-title-row p{margin:0;max-width:680px;color:#64748b;line-height:1.7}.outline-btn{flex-shrink:0;border:1px solid #c7d2fe;background:#eef2ff;color:#4f46e5;padding:11px 16px;border-radius:999px;font-weight:800;cursor:pointer}.week-tabs{overflow-x:auto;padding-bottom:8px}.week-tab{min-width:92px;border:1px solid #e2e8f0;color:#64748b;font-weight:700}.week-tab.active{background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);box-shadow:0 10px 24px rgba(102,126,234,.24)}.schedule-item{background:#f8fafc;border:1px solid #e2e8f0;box-shadow:0 10px 26px rgba(15,23,42,.05)}.class-info{flex:1}.schedule-actions{margin-left:auto;display:flex;align-items:center;gap:10px}.class-status{margin-left:0;white-space:nowrap}.cancel-booking-btn{border:none;border-radius:999px;padding:9px 14px;background:#fee2e2;color:#dc2626;font-weight:800;cursor:pointer}.cancel-booking-btn:hover{background:#fecaca}.form-input,.complaint-textarea,.gym-qr-section,.complaint-item,.empty-state{border:1px solid #e2e8f0}.save-btn,.submit-btn,.go-mall-btn{box-shadow:0 12px 26px rgba(102,126,234,.18)}@media(max-width:900px){.profile-header{grid-template-columns:1fr}.stats-row{grid-template-columns:1fr}.section-title-row{flex-direction:column}.schedule-item{align-items:flex-start;flex-direction:column}.schedule-actions{margin-left:0;width:100%;justify-content:space-between}}
</style>
