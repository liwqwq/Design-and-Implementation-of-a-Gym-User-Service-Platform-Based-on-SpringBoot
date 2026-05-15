<template>
  <div class="classes-page">
    <!-- 顶部导航栏 -->
    <header class="page-header">
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

    <!-- 主内容区 -->
    <main class="main-content">
      <section class="booking-hero">
        <div class="booking-hero-copy">
          <span class="hero-kicker">{{ t('common.classBooking') }}</span>
          <h1>{{ t('user.classes.hubTitle') }}</h1>
          <p>{{ t('user.classes.hubSubtitle') }}</p>
          <div class="hero-actions">
            <button class="hero-action primary" @click="switchTab('group')">👥 {{ t('user.classes.group') }}</button>
            <button class="hero-action" @click="switchTab('private')">👨‍🏫 {{ t('user.classes.private') }}</button>
          </div>
        </div>
        <div class="booking-hero-panel">
          <div class="guide-title">{{ t('user.classes.bookingGuideTitle') }}</div>
          <p>{{ t('user.classes.bookingGuideText') }}</p>
          <div class="guide-chip">🏆 {{ points }} {{ t('common.points') }}</div>
        </div>
      </section>

      <section class="booking-stats-grid">
        <div class="booking-stat-card"><span>📌</span><div><strong>{{ bookedCourseCount }}</strong><small>{{ t('user.classes.myBookedCount') }}</small></div></div>
        <div class="booking-stat-card"><span>👥</span><div><strong>{{ groupClasses.length }}</strong><small>{{ t('user.classes.groupCount') }}</small></div></div>
        <div class="booking-stat-card"><span>👨‍🏫</span><div><strong>{{ trainers.length }}</strong><small>{{ t('user.classes.trainerCount') }}</small></div></div>
      </section>

      <section class="booking-workspace">
      <!-- Tab切换 -->
      <div class="tabs-wrapper">
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'group' }"
          @click="switchTab('group')"
        >
          <span class="tab-icon">👥</span>
          <span class="tab-text">{{ t('user.classes.group') }}</span>
        </div>
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'private' }"
          @click="switchTab('private')"
        >
          <span class="tab-icon">👨‍🏫</span>
          <span class="tab-text">{{ t('user.classes.private') }}</span>
        </div>
      </div>

      <!-- 团课界面 -->
      <div v-show="activeTab === 'group'" class="tab-panel">
        <!-- 日期选择栏 -->
        <div class="date-tabs">
          <div 
            v-for="day in groupDateList" 
            :key="day.date"
            class="date-tab"
            :class="{ active: selectedGroupDate === day.date }"
            @click="selectGroupDate(day.date)"
          >
            <div class="date-weekday">{{ day.label }}</div>
            <div class="date-number">{{ day.day }}</div>
          </div>
        </div>

        <!-- 课程列表 -->
        <div class="courses-section">
          <div v-if="groupClasses.length === 0" class="empty-state">
            <div class="empty-icon">📅</div>
            <div class="empty-text">{{ t('user.classes.noCourses') }}</div>
          </div>
          <div v-else class="courses-grid">
            <div v-for="course in groupClasses" :key="course.id" class="course-card">
              <div class="course-header">
                <div class="course-time-badge">{{ course.time }}</div>
                <div class="course-status-tag" :class="getStatusClass(course)">
                  {{ course.isBooked ? t('user.classes.booked') : (course.bookedCount >= course.capacity ? t('user.classes.full') : t('user.classes.available')) }}
                </div>
              </div>
              <div class="course-body">
                <h3 class="course-name">{{ displayCourseTitle(course) }}</h3>
                <div class="course-meta">
                  <span class="trainer-info">👤 {{ t('common.trainer') }}: {{ course.trainer }}</span>
                  <span class="capacity-info">👥 {{ course.bookedCount }}/{{ course.capacity }}</span>
                </div>
              </div>
              <button 
                class="book-button"
                :class="{ booked: course.isBooked, full: course.bookedCount >= course.capacity }"
                :disabled="!course.isBooked && course.bookedCount >= course.capacity"
                @click="toggleGroupClassBooking(course)"
              >
                {{ course.isBooked ? t('user.classes.bookCancel') : (course.bookedCount >= course.capacity ? t('user.classes.full') : t('user.classes.bookNow')) }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 私教界面 -->
      <div v-show="activeTab === 'private'" class="tab-panel">
        <!-- 教练列表 -->
        <div v-if="trainers.length === 0" class="empty-state">
          <div class="empty-icon">👨‍🏫</div>
          <div class="empty-text">{{ t('user.classes.noTrainers') }}</div>
        </div>
        <div v-else class="trainers-grid">
          <div v-for="trainer in trainers" :key="trainer.id" class="trainer-card" @click="showTrainerDetail(trainer)">
            <div class="trainer-avatar-wrapper">
              <div class="trainer-avatar">{{ trainer.avatar || '👤' }}</div>
            </div>
            <div class="trainer-info">
              <h3 class="trainer-name">{{ trainer.name }}</h3>
              <div class="specialties-container">
                <span v-for="(specialty, index) in trainer.specialties" :key="index" class="specialty-tag">
                  {{ displaySpecialty(specialty) }}
                </span>
              </div>
              <div class="trainer-stats">
                <span class="stat-item">⭐ {{ trainer.rating || 5.0 }}</span>
                <span class="stat-item">📊 {{ trainer.experienceYears || 5 }}{{ t('user.classes.yearsExp') }}</span>
              </div>
            </div>
            <div class="trainer-action">
              <button class="view-courses-btn">{{ t('user.classes.viewCourses') }}</button>
            </div>
          </div>
        </div>
      </div>
      </section>
    </main>

    <!-- 私教详情弹窗 -->
    <div v-if="showTrainerModal" class="modal-overlay" @click.self="closeTrainerModal">
      <div class="trainer-modal">
        <div class="modal-header">
          <div class="trainer-header-info">
            <div class="modal-avatar">{{ selectedTrainer?.avatar || '👤' }}</div>
            <div class="modal-trainer-info">
              <h3>{{ selectedTrainer?.name }}</h3>
              <div class="specialties-row">
                <span v-for="(specialty, index) in selectedTrainer?.specialties" :key="index" class="specialty-tag">
                  {{ displaySpecialty(specialty) }}
                </span>
              </div>
              <div class="trainer-meta">
                <span>⭐ {{ selectedTrainer?.rating || 5.0 }}</span>
                <span>📊 {{ selectedTrainer?.experienceYears || 5 }}{{ t('user.classes.yearsExp') }}</span>
                <span>💰 ¥{{ selectedTrainer?.hourlyRate || 200 }}/{{ t('user.classes.perHour') }}</span>
              </div>
            </div>
          </div>
          <button class="close-btn" @click="closeTrainerModal">✕</button>
        </div>
        <div class="modal-content">
          <h4>{{ t('user.classes.availableCourses') }}</h4>
          <div v-if="trainerCourses.length === 0" class="empty-state small">
            <div class="empty-icon">📅</div>
            <div class="empty-text">{{ t('user.classes.noCourses') }}</div>
          </div>
          <div v-else class="modal-courses">
            <div v-for="course in trainerCourses" :key="course.id" class="modal-course-item">
              <div class="course-detail">
                <div class="course-time">{{ course.time }}</div>
                <div class="course-info">
                  <div class="course-name">{{ displayCourseTitle(course) }}</div>
                  <div class="course-duration">{{ displayDuration(course) }}</div>
                </div>
              </div>
              <button 
                class="book-btn"
                :class="{ booked: course.isBooked, full: course.isFull }"
                :disabled="!course.isBooked && course.isFull"
                @click="togglePrivateClassBooking(course)"
              >
                {{ course.isBooked ? t('user.classes.bookCancel') : (course.isFull ? t('user.classes.full') : t('user.classes.book')) }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { formatCourseName } from '../../utils/courseName'
import { buildUpcomingDays } from '../../utils/date'
import { clearSession, getCurrentUsername } from '../../services/session'
import { getCheckinStatus } from '../../services/checkinService'
import {
  applyBookingState,
  bookingCount,
  cancelBookingByClass,
  createBooking,
  getClassList,
  getMyBookings,
  getTrainerCourses,
  getTrainers,
  normaliseClassCard,
  normalisePrivateCourse,
  normaliseTrainer
} from '../../services/classBookingService'

const router = useRouter()
const { t, te, locale } = useI18n()
const currentLang = computed(() => locale.value)
const username = ref(getCurrentUsername('用户'))
const points = ref(0)

const activeTab = ref('group')
const selectedGroupDate = ref('')
const groupDateList = ref([])
const groupClasses = ref([])
const trainers = ref([])
const showTrainerModal = ref(false)
const selectedTrainer = ref(null)
const trainerCourses = ref([])
const bookedCourseTotal = ref(0)

const bookedCourseCount = computed(() => bookedCourseTotal.value)

const displayCourseName = (name) => formatCourseName(name, t, te)
const displayCourseTitle = (course) => {
  if (!course) return ''
  if (locale.value === 'en' && course.nameEn) return course.nameEn
  return displayCourseName(course.name)
}

const displaySpecialty = (specialty) => {
  const raw = String(specialty || '').trim().replace(/^specialties\./, '')
  if (!raw) return ''
  const key = `specialties.${raw}`
  return te(key) ? t(key) : raw
}

const displayDuration = (course) => {
  const minutes = Number(course?.durationMinutes || String(course?.duration || '').match(/\d+/)?.[0] || 60)
  return locale.value === 'en' ? `${minutes} min` : `${minutes}分钟`
}

const refreshDateTabs = () => {
  const previousDate = selectedGroupDate.value
  groupDateList.value = buildUpcomingDays({ lang: locale.value, t })
  selectedGroupDate.value = previousDate || groupDateList.value[0]?.date || ''
}

const switchLang = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
  refreshDateTabs()
}

const loadPoints = async () => {
  try {
    const result = await getCheckinStatus()
    if (result.success) points.value = result.data?.availablePoints || 0
  } catch (error) {
    console.error('加载积分失败:', error)
  }
}

const refreshBookingState = async () => {
  const result = await getMyBookings()
  const bookings = result.success && Array.isArray(result.data) ? result.data : []
  bookedCourseTotal.value = bookingCount(bookings)
  groupClasses.value = applyBookingState(groupClasses.value, bookings)
  trainerCourses.value = applyBookingState(trainerCourses.value, bookings)
}

const loadGroupClasses = async () => {
  try {
    const result = await getClassList(selectedGroupDate.value)
    const items = result.success && Array.isArray(result.data) ? result.data : []
    groupClasses.value = items.map(item => normaliseClassCard(item, t('common.trainer')))
  } catch (error) {
    console.error('加载团课失败:', error)
    groupClasses.value = []
  } finally {
    await refreshBookingState()
  }
}

const loadTrainers = async () => {
  try {
    const result = await getTrainers()
    trainers.value = result.success && Array.isArray(result.data)
      ? result.data.map(normaliseTrainer)
      : []
  } catch (error) {
    console.error('加载教练失败:', error)
    trainers.value = []
  }
}

const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'group') loadGroupClasses()
  if (tab === 'private') loadTrainers()
}

const getStatusClass = (course) => {
  if (course.isBooked) return 'booked'
  if (Number(course.bookedCount || 0) >= Number(course.capacity || 0)) return 'full'
  return 'available'
}

const selectGroupDate = (date) => {
  selectedGroupDate.value = date
  loadGroupClasses()
}

const handleBookingChange = async (course, { privateCourse = false } = {}) => {
  try {
    const result = course.isBooked
      ? await cancelBookingByClass(course.id)
      : await createBooking(course.id)

    if (!result.success) {
      ElMessage.error(result.message || (course.isBooked ? t('user.classes.cancelFailed') : t('user.classes.bookFailed')))
      return
    }

    const shouldBook = !course.isBooked
    course.isBooked = shouldBook
    course.bookingId = shouldBook ? result.data?.id || course.bookingId || null : null
    course.isFull = false
    if (!privateCourse) {
      course.bookedCount = Math.max(0, Number(course.bookedCount || 0) + (shouldBook ? 1 : -1))
    }
    bookedCourseTotal.value = Math.max(0, bookedCourseTotal.value + (shouldBook ? 1 : -1))
    ElMessage.success(shouldBook ? t('user.classes.bookSuccess') : t('user.classes.cancelSuccess'))
  } catch (error) {
    ElMessage.error(error.response?.data?.message || (course.isBooked ? t('user.classes.cancelFailed') : t('user.classes.bookFailed')))
  }
}

const toggleGroupClassBooking = (course) => handleBookingChange(course)
const togglePrivateClassBooking = (course) => handleBookingChange(course, { privateCourse: true })

const showTrainerDetail = (trainer) => {
  selectedTrainer.value = trainer
  showTrainerModal.value = true
  loadTrainerCourses(trainer.id)
}

const loadTrainerCourses = async (trainerId) => {
  try {
    const result = await getTrainerCourses(trainerId)
    trainerCourses.value = result.success && Array.isArray(result.data)
      ? result.data.map(normalisePrivateCourse)
      : []
  } catch (error) {
    console.error('加载教练课程失败:', error)
    trainerCourses.value = []
  } finally {
    await refreshBookingState()
  }
}

const closeTrainerModal = () => {
  showTrainerModal.value = false
  selectedTrainer.value = null
  trainerCourses.value = []
}

const logout = () => {
  clearSession()
  router.push('/user/login')
}

onMounted(async () => {
  refreshDateTabs()
  await Promise.all([loadPoints(), loadGroupClasses(), loadTrainers()])
})
</script>

<style scoped>
.classes-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

/* 主内容区 */
.main-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 20px;
}

.page-title {
  text-align: center;
  margin-bottom: 30px;
}

.page-title h1 {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 10px 0;
}

.page-title p {
  color: #666;
  margin: 0;
}

/* Tab切换 */
.tabs-wrapper {
  display: flex;
  background: white;
  border-radius: 15px;
  padding: 6px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 600;
  color: #666;
}

.tab-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.tab-icon {
  font-size: 20px;
}

.tab-text {
  font-size: 16px;
}

/* 日期选择 */
.date-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 30px;
  overflow-x: auto;
  padding-bottom: 10px;
}

.date-tab {
  flex-shrink: 0;
  padding: 15px 22px;
  background: white;
  border-radius: 14px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 80px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.date-tab:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.date-tab.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.date-weekday {
  font-size: 13px;
  margin-bottom: 6px;
  opacity: 0.8;
}

.date-number {
  font-size: 18px;
  font-weight: 700;
}

/* 课程列表 */
.courses-section {
  margin-top: 20px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.empty-state.small {
  padding: 40px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.empty-text {
  color: #999;
  font-size: 16px;
}

.courses-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.course-card {
  background: white;
  border-radius: 20px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.course-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.course-time-badge {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 8px 16px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 14px;
}

.course-status-tag {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.course-status-tag.available {
  background: #d1fae5;
  color: #065f46;
}

.course-status-tag.booked {
  background: #e0e7ff;
  color: #4338ca;
}

.course-status-tag.full {
  background: #fee2e2;
  color: #991b1b;
}

.course-body {
  margin-bottom: 20px;
}

.course-name {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 10px 0;
}

.course-meta {
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

.book-button {
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
}

.book-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.4);
}

.book-button:disabled {
  cursor: not-allowed;
}

.book-button.booked {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  cursor: pointer;
}

.book-button.full {
  background: #9ca3af;
}

/* 私教列表 */
.trainers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.trainer-card {
  background: white;
  border-radius: 20px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s;
}

.trainer-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12);
}

.trainer-avatar-wrapper {
  text-align: center;
  margin-bottom: 15px;
}

.trainer-avatar {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  margin: 0 auto;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.trainer-info {
  text-align: center;
  margin-bottom: 20px;
}

.trainer-name {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 12px 0;
}

.specialties-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 6px;
  margin-bottom: 12px;
}

.specialty-tag {
  padding: 4px 10px;
  background: #e0e7ff;
  color: #6366f1;
  border-radius: 10px;
  font-size: 12px;
}

.trainer-stats {
  display: flex;
  justify-content: center;
  gap: 15px;
  font-size: 13px;
  color: #666;
}

.stat-item {
  display: flex;
  align-items: center;
}

.view-courses-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.view-courses-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(16, 185, 129, 0.4);
}

/* 弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
}

.trainer-modal {
  background: white;
  border-radius: 24px;
  width: 100%;
  max-width: 500px;
  max-height: 85vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 25px;
  border-bottom: 1px solid #eee;
}

.trainer-header-info {
  display: flex;
  gap: 20px;
}

.modal-avatar {
  width: 70px;
  height: 70px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  flex-shrink: 0;
}

.modal-trainer-info {
  flex: 1;
}

.modal-trainer-info h3 {
  margin: 0 0 10px 0;
  font-size: 20px;
  font-weight: 700;
}

.specialties-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}

.trainer-meta {
  display: flex;
  gap: 15px;
  font-size: 13px;
  color: #666;
}

.close-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #f1f5f9;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  color: #666;
  transition: all 0.3s;
}

.close-btn:hover {
  background: #e2e8f0;
}

.modal-content {
  padding: 25px;
  max-height: 60vh;
  overflow-y: auto;
}

.modal-content h4 {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
}

.modal-courses {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.modal-course-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8fafc;
  border-radius: 15px;
}

.course-detail {
  flex: 1;
}

.course-time {
  font-weight: 600;
  color: #667eea;
  margin-bottom: 5px;
}

.course-duration {
  font-size: 13px;
  color: #999;
  margin-top: 5px;
}

.modal-course-item .book-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.modal-course-item .book-btn:hover:not(:disabled) {
  transform: translateY(-2px);
}

.modal-course-item .book-btn:disabled {
  background: #9ca3af;
}

.modal-course-item .book-btn.booked {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  cursor: pointer;
}
</style>


<style scoped>
/* Unified polished booking center overrides */
.classes-page { min-height: 100vh; background: radial-gradient(circle at top left, rgba(102,126,234,.14), transparent 32%), linear-gradient(180deg,#f5f7ff 0%,#f8fafc 42%,#eef2f7 100%); }
.main-content { max-width: 1180px; padding: 34px 24px 72px; }
.booking-hero { position: relative; overflow: hidden; display: grid; grid-template-columns: minmax(0,1.6fr) minmax(300px,.8fr); gap: 24px; margin-bottom: 22px; padding: 34px; border-radius: 28px; background: linear-gradient(135deg,#667eea 0%,#764ba2 100%); color:#fff; box-shadow: 0 24px 70px rgba(102,126,234,.28); }
.booking-hero:before,.booking-hero:after{content:'';position:absolute;border-radius:999px;background:rgba(255,255,255,.12)}.booking-hero:before{width:220px;height:220px;right:-72px;top:-96px}.booking-hero:after{width:160px;height:160px;left:42%;bottom:-96px}.booking-hero-copy,.booking-hero-panel{position:relative;z-index:1}.hero-kicker{display:inline-flex;align-items:center;gap:8px;padding:7px 12px;margin-bottom:14px;border-radius:999px;background:rgba(255,255,255,.18);font-size:13px;font-weight:700}.booking-hero h1{margin:0 0 12px;font-size:clamp(30px,4vw,46px);line-height:1.08;letter-spacing:-.04em}.booking-hero p{margin:0;max-width:660px;color:rgba(255,255,255,.86);line-height:1.8}.hero-actions{display:flex;flex-wrap:wrap;gap:12px;margin-top:24px}.hero-action{border:none;border-radius:999px;padding:12px 18px;color:#4338ca;background:rgba(255,255,255,.92);font-weight:800;cursor:pointer;box-shadow:0 12px 25px rgba(31,41,55,.12)}.hero-action.primary{color:#fff;background:linear-gradient(135deg,#10b981,#059669)}.booking-hero-panel{align-self:stretch;padding:22px;border-radius:24px;background:rgba(255,255,255,.14);border:1px solid rgba(255,255,255,.22);backdrop-filter:blur(12px)}.guide-title{font-size:18px;font-weight:900;margin-bottom:10px}.guide-chip{display:inline-flex;align-items:center;padding:10px 14px;margin-top:20px;border-radius:999px;background:rgba(255,255,255,.95);color:#d97706;font-weight:900}.booking-stats-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px;margin-bottom:22px}.booking-stat-card{display:flex;align-items:center;gap:14px;padding:18px 20px;border-radius:22px;background:rgba(255,255,255,.92);box-shadow:0 16px 40px rgba(15,23,42,.08);border:1px solid rgba(226,232,240,.8)}.booking-stat-card span{display:inline-flex;align-items:center;justify-content:center;width:48px;height:48px;border-radius:16px;background:#eef2ff;font-size:22px}.booking-stat-card strong{display:block;font-size:26px;color:#111827;line-height:1}.booking-stat-card small{display:block;margin-top:6px;color:#64748b;font-weight:700}.booking-workspace{padding:22px;border-radius:28px;background:rgba(255,255,255,.88);box-shadow:0 18px 50px rgba(15,23,42,.08);border:1px solid rgba(226,232,240,.8)}.tabs-wrapper{margin:0 0 24px;box-shadow:none;background:#f1f5f9}.tab-item{border-radius:16px}.date-tab,.course-card,.trainer-card,.empty-state{box-shadow:0 14px 36px rgba(15,23,42,.08);border:1px solid rgba(226,232,240,.72)}.course-card{display:grid;grid-template-columns:minmax(0,1fr) 200px;gap:18px;align-items:center}.course-header,.course-body{grid-column:1}.book-button{grid-column:2;grid-row:1/span 2;min-height:54px}.trainers-grid{grid-template-columns:repeat(auto-fit,minmax(260px,1fr))}.trainer-card{border:1px solid rgba(226,232,240,.86)}.trainer-modal{border-radius:28px}@media(max-width:900px){.booking-hero{grid-template-columns:1fr;padding:28px}.booking-stats-grid{grid-template-columns:1fr}.course-card{grid-template-columns:1fr}.book-button{grid-column:1;grid-row:auto}}
</style>
