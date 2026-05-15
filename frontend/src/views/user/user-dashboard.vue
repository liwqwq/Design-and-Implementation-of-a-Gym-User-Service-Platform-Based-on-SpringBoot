<template>
  <div class="user-dashboard">
    <header class="user-header">
      <div class="header-content">
        <div class="logo" @click="router.push('/user/dashboard')">
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
          <span class="username">{{ username || t('common.guestName') }}</span>
          <button class="logout-btn" @click="logout">{{ t('common.logout') }}</button>
        </div>
      </div>
    </header>
    
    <main class="main-content">
      <section class="hero-section">
        <div class="hero-decoration hero-decoration-one"></div>
        <div class="hero-decoration hero-decoration-two"></div>
        <div class="hero-copy">
          <div class="hero-eyebrow">{{ t('user.dashboard.heroEyebrow') }}</div>
          <h1>{{ t('user.dashboard.welcomeLine', { name: username || t('common.guestName') }) }}</h1>
          <p>{{ t('user.dashboard.heroSubtitle') }}</p>
          <div class="quick-actions">
            <button class="action-btn primary" @click="goToCheckin">
              <span>🔥</span>{{ t('user.dashboard.checkInNow') }}
            </button>
            <button class="action-btn ghost" @click="goToClasses">
              <span>📅</span>{{ t('user.dashboard.bookClass') }}
            </button>
          </div>
        </div>

        <aside class="hero-panel">
          <div class="panel-topline">
            <span>{{ t('user.dashboard.todayPlan') }}</span>
            <strong>{{ todayLabel }}</strong>
          </div>
          <div class="next-class-card" v-if="firstCourse">
            <div class="next-class-icon">{{ getCourseIcon(firstCourse.name) }}</div>
            <div>
              <p class="next-class-kicker">{{ t('user.dashboard.nextRecommended') }}</p>
              <h3>{{ displayCourseName(firstCourse.name) }}</h3>
              <p>{{ formatCourseTime(firstCourse.startTime) }} · {{ displayInstructor(firstCourse.instructor) }}</p>
            </div>
          </div>
          <div class="next-class-card empty" v-else>
            <div class="next-class-icon">✨</div>
            <div>
              <p class="next-class-kicker">{{ t('user.dashboard.todayPlan') }}</p>
              <h3>{{ t('user.dashboard.ready') }}</h3>
              <p>{{ t('user.dashboard.emptyCourses') }}</p>
            </div>
          </div>
          <div class="goal-card">
            <div class="goal-header">
              <span>{{ t('user.dashboard.weeklyGoal') }}</span>
              <strong>{{ goalProgress }}%</strong>
            </div>
            <div class="goal-track">
              <div class="goal-fill" :style="{ width: goalProgress + '%' }"></div>
            </div>
            <p>{{ t('user.dashboard.goalHint') }}</p>
          </div>
        </aside>
      </section>

      <section class="vip-upgrade-section" :class="{ active: isVipMember }">
        <div class="vip-copy">
          <span class="vip-chip">👑 {{ isVipMember ? t('user.dashboard.vipActive') : t('user.dashboard.vipChip') }}</span>
          <h2>{{ isVipMember ? t('user.dashboard.vipActiveTitle') : t('user.dashboard.vipTitle') }}</h2>
          <p>{{ isVipMember ? t('user.dashboard.vipActiveText', { date: membershipEndDate || '-' }) : t('user.dashboard.vipText') }}</p>
        </div>
        <button class="vip-action" @click="goToVipPayment">
          {{ isVipMember ? t('user.dashboard.extendVip') : t('user.dashboard.buyVip') }}
        </button>
      </section>
      
      <section class="stats-section">
        <div class="stat-card emphasis">
          <div class="stat-icon">🏆</div>
          <div class="stat-content">
            <div class="stat-value">{{ points }}</div>
            <div class="stat-label">{{ t('user.dashboard.pointsBalance') }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">🔥</div>
          <div class="stat-content">
            <div class="stat-value">{{ streakDays }}</div>
            <div class="stat-label">{{ t('user.dashboard.consecutiveDays') }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">🎯</div>
          <div class="stat-content">
            <div class="stat-value">{{ bookedClasses }}</div>
            <div class="stat-label">{{ t('user.dashboard.bookedClasses') }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">🥇</div>
          <div class="stat-content">
            <div class="stat-value">{{ badges }}</div>
            <div class="stat-label">{{ t('user.dashboard.badges') }}</div>
          </div>
        </div>
      </section>

      <section class="feature-section">
        <button class="feature-card" @click="goToClasses">
          <span class="feature-icon">📅</span>
          <span>
            <strong>{{ t('user.dashboard.bookClass') }}</strong>
            <small>{{ t('user.dashboard.featureBook') }}</small>
          </span>
        </button>
        <button class="feature-card" @click="goToCheckin">
          <span class="feature-icon">✅</span>
          <span>
            <strong>{{ t('user.dashboard.checkInNow') }}</strong>
            <small>{{ t('user.dashboard.featureCheckin') }}</small>
          </span>
        </button>
        <button class="feature-card" @click="goToMall">
          <span class="feature-icon">🎁</span>
          <span>
            <strong>{{ t('common.pointsMall') }}</strong>
            <small>{{ t('user.dashboard.featureMall') }}</small>
          </span>
        </button>
        <button class="feature-card" @click="goToRanking">
          <span class="feature-icon">🏅</span>
          <span>
            <strong>{{ t('common.ranking') }}</strong>
            <small>{{ t('user.dashboard.featureRanking') }}</small>
          </span>
        </button>
      </section>
      
      <section class="section courses-section">
        <div class="section-header">
          <div>
            <span class="section-kicker">{{ t('user.dashboard.startJourney') }}</span>
            <h2>{{ t('user.dashboard.recommended') }}</h2>
          </div>
          <button class="text-link" @click="goToClasses">{{ t('user.dashboard.viewAll') }} →</button>
        </div>
        <div class="courses-grid" v-if="courses.length > 0">
          <article v-for="course in courses" :key="course.id" class="course-card">
            <div class="course-card-top">
              <div class="course-icon">{{ getCourseIcon(course.name) }}</div>
              <span class="status-pill" :class="{ booked: course.isBooked, full: spotsDisplay(course) <= 0 }">
                {{ course.isBooked ? t('user.dashboard.booked') : (spotsDisplay(course) <= 0 ? t('user.dashboard.full') : t('user.classes.available')) }}
              </span>
            </div>
            <div class="course-info">
              <div class="course-name">{{ displayCourseName(course.name) }}</div>
              <div class="course-meta">
                <span>👤 {{ displayInstructor(course.instructor) }}</span>
                <span>⏰ {{ formatCourseTime(course.startTime) }}</span>
                <span>🎟️ {{ t('user.dashboard.spotsLeft') }}: {{ spotsDisplay(course) }}</span>
              </div>
            </div>
            <button class="book-btn" 
                    :class="{ booked: course.isBooked, full: spotsDisplay(course) <= 0 }"
                    :disabled="course.isBooked || spotsDisplay(course) <= 0 || loading"
                    @click="bookClass(course)">
              {{ course.isBooked ? t('user.dashboard.booked') : (spotsDisplay(course) <= 0 ? t('user.dashboard.full') : t('user.dashboard.bookNow')) }}
            </button>
          </article>
        </div>
        <div v-else class="empty-state">
          <div>📭</div>
          <p>{{ loading ? t('common.loading') : t('user.dashboard.emptyCourses') }}</p>
        </div>
      </section>
      
      <section class="section community-section">
        <div class="section-header">
          <div>
            <span class="section-kicker">{{ t('user.dashboard.communityPreview') }}</span>
            <h2>{{ t('common.social') }}</h2>
          </div>
          <button class="text-link" @click="goToSocial">{{ t('user.dashboard.viewAll') }} →</button>
        </div>
        <div class="posts-grid">
          <article v-for="post in recentPosts" :key="post.id" class="post-card">
            <div class="post-author">
              <span class="author-avatar">{{ post.avatar }}</span>
              <div class="author-info">
                <div class="author-name">{{ post.author }}</div>
                <div class="post-time">{{ post.time }}</div>
              </div>
            </div>
            <div class="post-content">{{ post.content }}</div>
            <div class="post-actions">
              <span>👍 {{ post.likes }}</span>
              <span>💬 {{ post.comments }}</span>
            </div>
          </article>
        </div>
      </section>
    </main>
    
    <div v-if="toast.show" class="toast show" :class="{ success: toast.type === 'success', error: toast.type === 'error' }">
      {{ toast.message }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { formatCourseName } from '../../utils/courseName'
import { formatRelativeDateTime } from '../../utils/date'
import { courseIcon, displayInstructorName, remainingSeats } from '../../utils/courseMeta'
import { clearSession, getCurrentUsername } from '../../services/session'
import { getCheckinStatus } from '../../services/checkinService'
import { getUserMembership } from '../../services/userService'
import { createBooking, getClassList } from '../../services/classBookingService'

const router = useRouter()
const { t, te, locale } = useI18n()
const currentLang = computed(() => locale.value)
const username = ref(getCurrentUsername(t('common.guestName')))

const points = ref(0)
const streakDays = ref(0)
const badges = ref(0)
const bookedClasses = ref(0)
const courses = ref([])
const isVipMember = ref(false)
const membershipEndDate = ref('')
const loading = ref(false)
const toast = ref({ show: false, message: '', type: 'success' })

const displayCourseName = (name) => formatCourseName(name, t, te)
const todayLabel = computed(() => new Intl.DateTimeFormat(locale.value === 'zh' ? 'zh-CN' : 'en-US', {
  month: 'short', day: 'numeric', weekday: 'short'
}).format(new Date()))
const firstCourse = computed(() => courses.value?.[0] || null)
const goalProgress = computed(() => Math.min(100, Math.round((Number(streakDays.value || 0) / 7) * 100)))

const recentPosts = computed(() => [
  { id: 1, avatar: '💪', author: t('user.dashboard.demoPosts.p1.author'), time: t('user.dashboard.demoPosts.p1.time'), content: t('user.dashboard.demoPosts.p1.content'), likes: 128, comments: 15 },
  { id: 2, avatar: '🧘', author: t('user.dashboard.demoPosts.p2.author'), time: t('user.dashboard.demoPosts.p2.time'), content: t('user.dashboard.demoPosts.p2.content'), likes: 89, comments: 8 },
])

const switchLang = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

const showToast = (message, type = 'success') => {
  toast.value = { show: true, message, type }
  setTimeout(() => {
    toast.value = { show: false, message: '', type: 'success' }
  }, 3000)
}

const loadPoints = async () => {
  try {
    const result = await getCheckinStatus()
    if (!result.success) return
    const data = result.data || {}
    points.value = data.availablePoints || 0
    streakDays.value = data.consecutiveDays || 0
    badges.value = [1, 7, 30, 100].filter(goal => Number(streakDays.value || 0) >= goal).length
  } catch (error) {
    console.error('加载积分失败:', error)
  }
}

const loadMembership = async () => {
  try {
    const result = await getUserMembership()
    const membership = result?.data || {}
    const type = String(membership.membershipType || '').toLowerCase()
    const status = String(membership.status || '').toLowerCase()
    const endDate = membership.endDate || ''
    const notExpired = !endDate || new Date(endDate + 'T23:59:59').getTime() >= Date.now()
    isVipMember.value = type.includes('vip') && (status === 'active' || status === '有效') && notExpired
    membershipEndDate.value = endDate
  } catch (error) {
    console.error('加载会员信息失败:', error)
  }
}

const loadClasses = async () => {
  loading.value = true
  try {
    const result = await getClassList()
    const items = result.success && Array.isArray(result.data) ? result.data : []
    courses.value = items.slice(0, 3)
    bookedClasses.value = items.filter(course => course.isBooked).length
    if (items.length === 0) showToast(t('user.dashboard.toasts.noClassDataDemo'))
  } catch (error) {
    console.error('加载课程失败:', error)
    showToast(t('user.dashboard.toasts.loadClassFailedDemo'), 'error')
    courses.value = []
  } finally {
    loading.value = false
  }
}

const spotsDisplay = (course) => remainingSeats(course)
const getCourseIcon = (name) => courseIcon(name)
const displayInstructor = (name) => displayInstructorName(name, t)
const formatCourseTime = (timeStr) => formatRelativeDateTime(timeStr, { lang: locale.value, t })

const bookClass = async (course) => {
  if (course.isBooked || spotsDisplay(course) <= 0) return
  try {
    const result = await createBooking(course.id)
    if (result.success) {
      course.isBooked = true
      course.bookedCount = Number(course.bookedCount || 0) + 1
      course.spotsLeft = Math.max(0, spotsDisplay(course) - 1)
      bookedClasses.value += 1
      showToast(t('user.dashboard.toasts.bookSuccess'))
    } else {
      showToast(result.message || t('user.dashboard.toasts.bookFailed'), 'error')
    }
  } catch (error) {
    console.error('预约失败:', error)
    showToast(error.response?.data?.message || t('user.dashboard.toasts.bookFailed'), 'error')
  }
}

const logout = () => {
  clearSession()
  router.push('/user/login')
}

const goToCheckin = () => router.push('/user/checkin')
const goToClasses = () => router.push('/user/classes')
const goToMall = () => router.push('/user/points-mall')
const goToSocial = () => router.push('/user/social')
const goToRanking = () => router.push('/user/ranking')
const goToVipPayment = () => router.push('/user/vip-payment')

onMounted(() => {
  loadPoints()
  loadMembership()
  loadClasses()
})
</script>

<style scoped>
.user-dashboard {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(102, 126, 234, 0.16), transparent 34rem),
    radial-gradient(circle at 85% 8%, rgba(16, 185, 129, 0.13), transparent 28rem),
    linear-gradient(180deg, #f7f9ff 0%, #eef3f8 100%);
  color: #0f172a;
}

.logo {
  cursor: pointer;
}

.main-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 34px 28px 56px;
}

.hero-section {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.65fr);
  gap: 28px;
  min-height: 380px;
  padding: 46px;
  border-radius: 34px;
  color: white;
  background:
    linear-gradient(135deg, rgba(35, 46, 122, 0.92) 0%, rgba(102, 126, 234, 0.94) 48%, rgba(118, 75, 162, 0.96) 100%);
  box-shadow: 0 28px 70px rgba(76, 81, 191, 0.28);
}

.hero-decoration {
  position: absolute;
  border-radius: 999px;
  filter: blur(1px);
  opacity: 0.45;
  pointer-events: none;
}

.hero-decoration-one {
  width: 280px;
  height: 280px;
  right: -80px;
  top: -90px;
  background: rgba(255, 255, 255, 0.22);
}

.hero-decoration-two {
  width: 180px;
  height: 180px;
  left: 46%;
  bottom: -90px;
  background: rgba(16, 185, 129, 0.35);
}

.hero-copy,
.hero-panel {
  position: relative;
  z-index: 1;
}

.hero-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
  max-width: 760px;
}

.hero-eyebrow,
.section-kicker {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  margin-bottom: 12px;
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.hero-eyebrow {
  color: #dbeafe;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.16);
}

.hero-copy h1 {
  margin: 0;
  font-size: clamp(38px, 5vw, 64px);
  line-height: 1.04;
  letter-spacing: -0.045em;
  font-weight: 900;
}

.hero-copy p {
  max-width: 640px;
  margin: 20px 0 30px;
  color: rgba(255, 255, 255, 0.84);
  font-size: 18px;
  line-height: 1.8;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
}

.action-btn {
  min-height: 52px;
  padding: 0 24px;
  border: none;
  border-radius: 18px;
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.action-btn:hover,
.feature-card:hover,
.course-card:hover,
.post-card:hover {
  transform: translateY(-4px);
}

.action-btn.primary {
  color: #4338ca;
  background: #fff;
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.16);
}

.action-btn.ghost {
  color: #fff;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(10px);
}

.hero-panel {
  align-self: stretch;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 16px;
  padding: 22px;
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(18px);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.18);
}

.panel-topline,
.goal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  color: rgba(255, 255, 255, 0.78);
  font-size: 13px;
  font-weight: 700;
}

.panel-topline strong,
.goal-header strong {
  color: #fff;
}

.next-class-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.92);
  color: #111827;
  box-shadow: 0 16px 44px rgba(15, 23, 42, 0.16);
}

.next-class-card.empty {
  background: rgba(255, 255, 255, 0.82);
}

.next-class-icon {
  width: 62px;
  height: 62px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  font-size: 34px;
  background: linear-gradient(135deg, #eef2ff, #ddd6fe);
}

.next-class-kicker {
  margin: 0 0 2px !important;
  color: #6366f1 !important;
  font-size: 12px !important;
  line-height: 1.4 !important;
  font-weight: 800;
}

.next-class-card h3 {
  margin: 0 0 4px;
  font-size: 19px;
}

.next-class-card p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.goal-card {
  padding: 18px;
  border-radius: 22px;
  background: rgba(15, 23, 42, 0.20);
}

.goal-track {
  height: 10px;
  margin: 12px 0;
  border-radius: 999px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.2);
}

.goal-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #34d399, #facc15);
  box-shadow: 0 0 18px rgba(250, 204, 21, 0.45);
  transition: width 0.3s ease;
}

.goal-card p {
  margin: 0;
  color: rgba(255, 255, 255, 0.74);
  font-size: 13px;
}


.vip-upgrade-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin: 24px 0;
  padding: 26px 30px;
  border-radius: 28px;
  color: white;
  background: linear-gradient(135deg, #f59e0b 0%, #ef4444 100%);
  box-shadow: 0 20px 50px rgba(245, 158, 11, 0.22);
}

.vip-upgrade-section.active {
  background: linear-gradient(135deg, #1f2937 0%, #4f46e5 100%);
  box-shadow: 0 20px 50px rgba(79, 70, 229, 0.18);
}

.vip-chip {
  display: inline-flex;
  width: fit-content;
  padding: 7px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  font-weight: 900;
  font-size: 13px;
}

.vip-copy h2 {
  margin: 12px 0 8px;
  font-size: 28px;
}

.vip-copy p {
  margin: 0;
  color: rgba(255, 255, 255, 0.86);
  line-height: 1.7;
}

.vip-action {
  flex: 0 0 auto;
  min-width: 170px;
  min-height: 52px;
  border: none;
  border-radius: 18px;
  background: white;
  color: #c2410c;
  font-weight: 900;
  cursor: pointer;
  box-shadow: 0 16px 34px rgba(15, 23, 42, 0.14);
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
  margin: 24px 0;
}

.stat-card,
.feature-card,
.course-card,
.post-card,
.empty-state {
  border: 1px solid rgba(226, 232, 240, 0.86);
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.07);
  backdrop-filter: blur(14px);
}

.stat-card {
  min-height: 128px;
  border-radius: 26px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
}

.stat-card.emphasis {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(255, 247, 237, 0.92));
}

.stat-icon {
  width: 56px;
  height: 56px;
  flex: 0 0 auto;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  background: linear-gradient(135deg, #eef2ff, #f5d0fe);
}

.stat-value {
  font-size: 32px;
  font-weight: 900;
  color: #4f46e5;
  line-height: 1;
}

.stat-label {
  margin-top: 8px;
  font-size: 13px;
  color: #64748b;
  font-weight: 700;
}

.feature-section {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
  margin-bottom: 28px;
}

.feature-card {
  text-align: left;
  border-radius: 24px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.feature-card strong,
.feature-card small {
  display: block;
}

.feature-card strong {
  color: #0f172a;
  font-size: 16px;
  margin-bottom: 4px;
}

.feature-card small {
  color: #64748b;
  font-size: 12px;
  line-height: 1.45;
}

.feature-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  font-size: 24px;
  background: linear-gradient(135deg, #ede9fe, #dbeafe);
}

.section {
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
}

.section-kicker {
  margin-bottom: 8px;
  color: #4f46e5;
  background: rgba(99, 102, 241, 0.12);
}

.section h2 {
  margin: 0;
  font-size: 28px;
  letter-spacing: -0.03em;
}

.text-link {
  border: none;
  background: transparent;
  color: #4f46e5;
  font-weight: 900;
  cursor: pointer;
  white-space: nowrap;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.course-card {
  overflow: hidden;
  position: relative;
  border-radius: 28px;
  padding: 22px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.course-card::before {
  content: '';
  position: absolute;
  inset: 0 0 auto 0;
  height: 7px;
  background: linear-gradient(90deg, #667eea, #10b981);
}

.course-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 22px;
}

.course-icon {
  width: 68px;
  height: 68px;
  border-radius: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 38px;
  background: linear-gradient(135deg, #f8fafc, #eef2ff);
}

.status-pill {
  padding: 8px 12px;
  border-radius: 999px;
  color: #166534;
  background: #dcfce7;
  font-size: 12px;
  font-weight: 800;
}

.status-pill.booked {
  color: #4338ca;
  background: #e0e7ff;
}

.status-pill.full {
  color: #475569;
  background: #e2e8f0;
}

.course-name {
  font-weight: 900;
  font-size: 21px;
  margin-bottom: 12px;
  letter-spacing: -0.02em;
}

.course-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #64748b;
  font-size: 13px;
  margin-bottom: 20px;
}

.book-btn {
  width: 100%;
  min-height: 46px;
  padding: 0 18px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 16px;
  font-weight: 900;
  cursor: pointer;
  box-shadow: 0 12px 24px rgba(102, 126, 234, 0.25);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.book-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 16px 30px rgba(102, 126, 234, 0.32);
}

.book-btn:disabled {
  cursor: not-allowed;
  opacity: 0.8;
}

.book-btn.booked {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  box-shadow: none;
}

.book-btn.full {
  background: #94a3b8;
  box-shadow: none;
}

.empty-state {
  border-radius: 26px;
  padding: 44px;
  text-align: center;
  color: #64748b;
  font-weight: 700;
}

.empty-state div {
  font-size: 42px;
  margin-bottom: 10px;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.post-card {
  border-radius: 26px;
  padding: 24px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  background: #f1f5f9;
}

.author-name {
  font-weight: 900;
}

.post-time {
  font-size: 13px;
  color: #94a3b8;
}

.post-content {
  margin-bottom: 18px;
  color: #334155;
  line-height: 1.8;
}

.post-actions {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #64748b;
}

.toast {
  position: fixed;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%) translateY(100px);
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 15px 30px;
  border-radius: 14px;
  transition: all 0.3s;
  z-index: 2000;
  box-shadow: 0 16px 42px rgba(15, 23, 42, 0.24);
}

.toast.show {
  transform: translateX(-50%) translateY(0);
}

.toast.success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.toast.error {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

@media (max-width: 1180px) {
  .hero-section {
    grid-template-columns: 1fr;
  }

  .stats-section,
  .feature-section {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .courses-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .main-content {
    padding: 22px 14px 42px;
  }

  .hero-section {
    padding: 28px;
    border-radius: 26px;
  }

  .hero-copy h1 {
    font-size: 36px;
  }

  .stats-section,
  .feature-section,
  .courses-grid,
  .posts-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
