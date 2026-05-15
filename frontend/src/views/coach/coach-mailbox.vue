<template>
  <div class="coach-mailbox">
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
    <main class="mailbox-content">
      <div class="section-title">
        <h1>📬 {{ t('coach.social.mailbox') }}</h1>
        <p>{{ t('coach.social.subtitle') }}</p>
      </div>

      <!-- 标签切换 -->
      <div class="tabs-wrapper">
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'complaints' }"
          @click="activeTab = 'complaints'"
        >
          <span>⚠️ {{ t('coach.social.complaints') }}</span>
          <span v-if="complaintCount > 0" class="badge">{{ complaintCount }}</span>
        </div>
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'reviews' }"
          @click="activeTab = 'reviews'"
        >
          <span>⭐ {{ t('coach.social.reviews') }}</span>
        </div>
      </div>

      <!-- 投诉信息 -->
      <div v-show="activeTab === 'complaints'" class="tab-panel">
        <div v-if="complaints.length === 0" class="empty-state">
          <div class="empty-icon">✅</div>
          <div class="empty-text">{{ t('coach.social.noComplaints') }}</div>
          <div class="empty-subtext">{{ t('coach.social.noComplaintsDesc') }}</div>
        </div>
        <div v-else class="complaints-list">
          <div v-for="complaint in complaints" :key="complaint.id" class="complaint-card">
            <div class="complaint-header">
              <div class="complaint-user">
                <span class="user-avatar">👤</span>
                <span class="user-name">{{ complaint.userName }}</span>
              </div>
              <div class="complaint-status" :class="normalizeStatus(complaint.status)">
                {{ statusLabel(complaint.status) }}
              </div>
            </div>
            <div class="complaint-body">
              <h4 class="complaint-title">{{ pickBilingual(complaint.title, complaint.titleEn) }}</h4>
              <p class="complaint-content">{{ pickBilingual(complaint.content, complaint.contentEn) }}</p>
            </div>
            <div v-if="pickBilingual(complaint.response, complaint.responseEn).trim()" class="complaint-reply-box">
              <div class="reply-label">{{ t('coach.social.reply') }}</div>
              <p class="complaint-reply-text">{{ pickBilingual(complaint.response, complaint.responseEn) }}</p>
            </div>
            <div class="complaint-footer">
              <span class="complaint-time">{{ complaint.createdAt }}</span>
              <div class="complaint-actions">
                <button v-if="normalizeStatus(complaint.status) === 'pending'" class="action-btn respond" @click="respondComplaint(complaint)">
                  {{ t('coach.social.handle') }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 评价信息 -->
      <div v-show="activeTab === 'reviews'" class="tab-panel">
        <div class="review-stats">
          <div class="stat-item">
            <div class="stat-value">{{ reviewStats.averageRating }}</div>
            <div class="stat-label">{{ t('coach.social.avgRating') }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ reviewStats.totalReviews }}</div>
            <div class="stat-label">{{ t('coach.social.totalReviews') }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ reviewStats.positiveCount }}</div>
            <div class="stat-label">{{ t('coach.social.positiveCount') }}</div>
          </div>
        </div>

        <div v-if="reviews.length === 0" class="empty-state">
          <div class="empty-icon">⭐</div>
          <div class="empty-text">{{ t('coach.social.noReviews') }}</div>
          <div class="empty-subtext">{{ t('coach.social.noReviewsDesc') }}</div>
        </div>
        <div v-else class="reviews-list">
          <div v-for="review in reviews" :key="review.id" class="review-card">
            <div class="review-header">
              <div class="review-user">
                <span class="user-avatar">👤</span>
                <span class="user-name">{{ review.userName }}</span>
              </div>
              <div class="review-rating">
                <span v-for="i in 5" :key="i" class="star">
                  {{ i <= review.rating ? '⭐' : '☆' }}
                </span>
              </div>
            </div>
            <div class="review-body">
              <p class="review-content">{{ pickBilingual(review.content, review.contentEn) }}</p>
              <div v-if="review.images && review.images.length > 0" class="review-images">
                <img v-for="(img, index) in review.images" :key="index" :src="img" class="review-image" />
              </div>
            </div>
            <div class="review-footer">
              <span class="review-time">{{ review.createdAt }}</span>
              <span class="review-class">{{ pickBilingual(review.className, review.classNameEn) }}</span>
            </div>
            <div class="review-response">
              <textarea v-if="review.responded" readonly class="response-text">{{ pickBilingual(review.response, review.responseEn) }}</textarea>
              <div v-else class="response-form">
                <textarea v-model="review.responseText" :placeholder="t('coach.social.responsePlaceholder')" rows="2"></textarea>
                <button class="respond-btn" @click="respondReview(review)">{{ t('coach.social.respond') }}</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t, locale, te } = useI18n()
const currentLang = computed(() => locale.value)

const switchLang = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

const username = computed(() => {
  const u = localStorage.getItem('username')
  return u || t('common.coach')
})
const currentPage = ref('mailbox')
const activeTab = ref('complaints')

const complaints = ref([])
const reviews = ref([])

const reviewStats = ref({
  averageRating: 0,
  totalReviews: 0,
  positiveCount: 0
})

function normalizeStatus(status) {
  return String(status || '').toLowerCase()
}

function pickBilingual(primary, alt) {
  const primaryOk = primary != null && String(primary).trim() !== ''
  const altOk = alt != null && String(alt).trim() !== ''
  if (locale.value === 'en') {
    return altOk ? String(alt) : (primaryOk ? String(primary) : '')
  }
  return primaryOk ? String(primary) : (altOk ? String(alt) : '')
}

function statusLabel(status) {
  const key = normalizeStatus(status)
  if (key && te(`complaintStatus.${key}`)) {
    return t(`complaintStatus.${key}`)
  }
  return status || ''
}

const complaintCount = computed(() => {
  return complaints.value.filter((c) => normalizeStatus(c.status) === 'pending').length
})

const getAuthHeader = () => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

const loadComplaints = async () => {
  try {
    const response = await axios.get('/api/coach/complaints', { headers: getAuthHeader() })
    if (response.data.success) {
      complaints.value = Array.isArray(response.data.data) ? response.data.data : []
    } else {
      complaints.value = []
      ElMessage.warning(response.data.message || t('coach.social.loadMailboxFailed'))
    }
  } catch (error) {
    console.error(error)
    complaints.value = []
    ElMessage.warning(t('coach.social.loadMailboxFailed'))
  }
}

const loadReviews = async () => {
  try {
    const response = await axios.get('/api/coach/reviews', { headers: getAuthHeader() })
    if (response.data.success) {
      const list = Array.isArray(response.data.data) ? response.data.data : []
      reviews.value = list.map((r) => ({ ...r, responseText: '' }))
      calculateStats()
    } else {
      reviews.value = []
      reviewStats.value = { averageRating: 0, totalReviews: 0, positiveCount: 0 }
      ElMessage.warning(response.data.message || t('coach.social.loadMailboxFailed'))
    }
  } catch (error) {
    console.error(error)
    reviews.value = []
    reviewStats.value = { averageRating: 0, totalReviews: 0, positiveCount: 0 }
    ElMessage.warning(t('coach.social.loadMailboxFailed'))
  }
}

const calculateStats = () => {
  if (reviews.value.length === 0) {
    reviewStats.value = { averageRating: 0, totalReviews: 0, positiveCount: 0 }
    return
  }

  const totalRating = reviews.value.reduce((sum, r) => sum + r.rating, 0)
  reviewStats.value = {
    averageRating: (totalRating / reviews.value.length).toFixed(1),
    totalReviews: reviews.value.length,
    positiveCount: reviews.value.filter((r) => r.rating >= 4).length
  }
}

const respondComplaint = async (complaint) => {
  try {
    const response = await axios.post(
      `/api/coach/complaints/${complaint.id}/respond`,
      {
        response: t('coach.social.defaultComplaintAckZh'),
        responseEn: t('coach.social.defaultComplaintAckEn')
      },
      { headers: getAuthHeader() }
    )
    if (response.data.success) {
      ElMessage.success(t('coach.social.complaintHandleSuccess'))
      complaint.status = 'processed'
      complaint.response = t('coach.social.defaultComplaintAckZh')
      complaint.responseEn = t('coach.social.defaultComplaintAckEn')
    }
  } catch (error) {
    ElMessage.error(t('coach.social.complaintHandleFail'))
  }
}

const respondReview = async (review) => {
  if (!review.responseText.trim()) {
    ElMessage.warning(t('coach.social.reviewReplyEmpty'))
    return
  }

  const text = review.responseText.trim()
  const body =
    locale.value === 'en'
      ? { response: text, responseEn: text }
      : { response: text, responseEn: '' }

  try {
    const response = await axios.post(`/api/coach/reviews/${review.id}/respond`, body, { headers: getAuthHeader() })
    if (response.data.success) {
      ElMessage.success(t('coach.social.reviewReplySuccess'))
      review.responded = true
      review.response = body.response
      review.responseEn = body.responseEn || ''
      review.responseText = ''
    }
  } catch (error) {
    ElMessage.error(t('coach.social.reviewReplyFail'))
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
  loadComplaints()
  loadReviews()
})
</script>

<style scoped>
.coach-mailbox {
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

.mailbox-content {
  max-width: 1000px;
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

.tabs-wrapper {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

.tab-item:hover {
  background: #f0f0f0;
}

.tab-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.badge {
  background: #dc2626;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.tab-panel {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px;
  color: #999;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 18px;
  margin-bottom: 8px;
}

.empty-subtext {
  font-size: 14px;
}

.complaints-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.complaint-card {
  background: #fff5f5;
  border: 1px solid #ffccc7;
  border-radius: 12px;
  padding: 20px;
}

.complaint-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.complaint-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #fca5a5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.user-name {
  font-weight: 600;
  color: #333;
}

.complaint-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.complaint-status.pending {
  background: #fee2e2;
  color: #dc2626;
}

.complaint-status.processed {
  background: #d1fae5;
  color: #059669;
}

.complaint-status.resolved {
  background: #dbeafe;
  color: #1d4ed8;
}

.complaint-status.rejected {
  background: #fee2e2;
  color: #b91c1c;
}

.complaint-status.processing {
  background: #fef3c7;
  color: #b45309;
}

.complaint-body {
  margin-bottom: 12px;
}

.complaint-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.complaint-content {
  color: #666;
  line-height: 1.6;
}

.complaint-reply-box {
  margin-bottom: 12px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.reply-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}

.complaint-reply-text {
  margin: 0;
  color: #334155;
  line-height: 1.5;
}

.complaint-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.complaint-time {
  color: #999;
  font-size: 14px;
}

.complaint-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.3s;
}

.action-btn.respond {
  background: #667eea;
  color: white;
}

.action-btn:hover {
  opacity: 0.8;
}

.review-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 8px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.review-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.review-rating {
  display: flex;
  gap: 2px;
}

.star {
  font-size: 18px;
}

.review-body {
  margin-bottom: 12px;
}

.review-content {
  color: #333;
  line-height: 1.6;
}

.review-images {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

.review-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
}

.review-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.review-time {
  color: #999;
  font-size: 14px;
}

.review-class {
  background: #e0e7ff;
  color: #4338ca;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

.review-response {
  border-top: 1px solid #e0e0e0;
  padding-top: 12px;
}

.response-text {
  width: 100%;
  padding: 12px;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  color: #333;
  margin-bottom: 12px;
}

.response-form {
  display: flex;
  gap: 12px;
}

.response-form textarea {
  flex: 1;
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.respond-btn {
  padding: 12px 24px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.3s;
}

.respond-btn:hover {
  opacity: 0.8;
}
</style>