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
      <div class="ranking-header">
        <h1>🏆 {{ t('user.ranking.title') }}</h1>
        <p>{{ t('user.ranking.subtitle') }}</p>
      </div>
      
      <div class="tabs">
        <button 
          class="tab" 
          :class="{ active: activeTab === 'total' }" 
          @click="switchTab('total')"
        >
          <span class="tab-icon">📊</span>
          <span>{{ t('user.ranking.total') }}</span>
        </button>
        <button 
          class="tab" 
          :class="{ active: activeTab === 'week' }" 
          @click="switchTab('week')"
        >
          <span class="tab-icon">📅</span>
          <span>{{ t('user.ranking.week') }}</span>
        </button>
        <button 
          class="tab" 
          :class="{ active: activeTab === 'month' }" 
          @click="switchTab('month')"
        >
          <span class="tab-icon">🗓️</span>
          <span>{{ t('user.ranking.month') }}</span>
        </button>
      </div>
      
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>{{ t('common.loading') }}...</p>
      </div>
      
      <div v-else class="ranking-content">
        <div class="podium">
          <div class="podium-item second" v-if="rankings[1]">
            <div class="podium-avatar">{{ rankings[1].avatar }}</div>
            <div class="podium-name">{{ rankings[1].name }}</div>
            <div class="podium-score">{{ rankings[1].score }}{{ t('user.ranking.classes') }}</div>
            <div class="podium-meta">{{ rankings[1].days }}{{ t('user.ranking.days') }} · {{ rankings[1].points }} {{ t('common.points') }}</div>
            <div class="podium-stand">2nd</div>
          </div>
          <div class="podium-item first" v-if="rankings[0]">
            <div class="crown">👑</div>
            <div class="podium-avatar">{{ rankings[0].avatar }}</div>
            <div class="podium-name">{{ rankings[0].name }}</div>
            <div class="podium-score">{{ rankings[0].score }}{{ t('user.ranking.classes') }}</div>
            <div class="podium-meta">{{ rankings[0].days }}{{ t('user.ranking.days') }} · {{ rankings[0].points }} {{ t('common.points') }}</div>
            <div class="podium-stand">1st</div>
          </div>
          <div class="podium-item third" v-if="rankings[2]">
            <div class="podium-avatar">{{ rankings[2].avatar }}</div>
            <div class="podium-name">{{ rankings[2].name }}</div>
            <div class="podium-score">{{ rankings[2].score }}{{ t('user.ranking.classes') }}</div>
            <div class="podium-meta">{{ rankings[2].days }}{{ t('user.ranking.days') }} · {{ rankings[2].points }} {{ t('common.points') }}</div>
            <div class="podium-stand">3rd</div>
          </div>
        </div>
        
        <div class="ranking-list">
          <div 
            v-for="(item, index) in rankings.slice(3)" 
            :key="item.id" 
            class="ranking-item"
            :class="'rank-' + (index + 4)"
          >
            <div class="rank-number">{{ index + 4 }}</div>
            <div class="rank-avatar">{{ item.avatar }}</div>
            <div class="rank-info">
              <div class="rank-name">{{ item.name }}</div>
              <div class="rank-details">{{ item.days }}{{ t('user.ranking.days') }} · {{ item.classes }}{{ t('user.ranking.classes') }} · {{ item.points }} {{ t('common.points') }}</div>
            </div>
            <div class="rank-score">{{ item.score }}{{ t('user.ranking.classes') }}</div>
          </div>
        </div>
        
        <div class="my-rank">
          <div class="my-rank-header">
            <span>{{ t('user.ranking.myRank') }}</span>
            <span class="my-rank-number">#{{ myRanking.rank }}</span>
          </div>
          <div class="my-rank-info">
            <div class="my-avatar">👤</div>
            <div class="my-details">
              <div class="my-name">{{ username }}</div>
              <div class="my-stats">{{ t('user.ranking.booked') }} {{ myRanking.score }} {{ t('user.ranking.classes') }}</div>
            </div>
            <div class="my-progress">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
              </div>
              <div class="progress-text" v-if="myRanking.isLeading">🎉 {{ t('user.ranking.leading') }}</div>
              <div class="progress-text" v-else-if="progressDiff > 0">{{ t('user.ranking.progress') }} {{ progressDiff }} {{ t('user.ranking.classes') }}</div>
              <div class="progress-text" v-else>{{ t('user.ranking.sameScoreTip') }}</div>
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
import { getMyPoints } from '../../services/pointsService'
import { getRankings, normaliseRankingItem } from '../../services/rankingService'

const router = useRouter()
const { t, locale } = useI18n()
const currentLang = computed(() => locale.value)

const switchLang = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

const username = ref(getCurrentUsername('用户'))
const points = ref(0)
const activeTab = ref('total')
const loading = ref(false)
const rankings = ref([])
const myRanking = ref({
  rank: null,
  score: 0,
  nextScore: 0,
  prevScore: 0,
  neededClassesToNext: 0,
  isLeading: false
})

const loadRankings = async () => {
  loading.value = true
  try {
    const result = await getRankings(activeTab.value)
    if (result.success) {
      rankings.value = Array.isArray(result.data) ? result.data.map(normaliseRankingItem) : []
      if (result.myRanking) myRanking.value = result.myRanking
    }
  } catch (error) {
    console.error('加载排行榜失败:', error)
  } finally {
    loading.value = false
  }
}

const switchTab = (tab) => {
  activeTab.value = tab
  loadRankings()
}

const progressDiff = computed(() => {
  if (myRanking.value.isLeading) return 0
  if (myRanking.value.neededClassesToNext !== undefined) {
    return Math.max(0, myRanking.value.neededClassesToNext)
  }
  return Math.max(0, myRanking.value.nextScore - myRanking.value.score + 1)
})

const progressPercent = computed(() => {
  if (myRanking.value.isLeading) return 100
  const nextScore = myRanking.value.nextScore || myRanking.value.score + 1
  const prevScore = myRanking.value.prevScore || 0
  if (nextScore === prevScore) return 50
  return Math.min(100, Math.max(0, ((myRanking.value.score - prevScore) / (nextScore - prevScore)) * 100))
})

const loadPointsData = async () => {
  try {
    const result = await getMyPoints()
    if (result.success && result.data) points.value = result.data.availablePoints || 0
  } catch (error) {
    console.error('加载积分失败:', error)
  }
}

const logout = () => {
  clearSession()
  router.push('/user/login')
}

onMounted(() => {
  loadRankings()
  loadPointsData()
})
</script>

<style scoped>
.user-dashboard {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f4ff 0%, #f8fafc 100%);
}

.main-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px;
}

.ranking-header {
  text-align: center;
  margin-bottom: 30px;
}

.ranking-header h1 {
  font-size: 36px;
  margin: 0 0 10px 0;
  background: linear-gradient(135deg, #1f2937 0%, #4b5563 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.ranking-header p {
  color: #6b7280;
  margin: 0;
  font-size: 16px;
}

.tabs {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 30px;
  background: white;
  padding: 8px;
  border-radius: 50px;
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.05);
  max-width: fit-content;
  margin-left: auto;
  margin-right: auto;
}

.tab {
  padding: 12px 28px;
  background: transparent;
  border: none;
  border-radius: 40px;
  font-weight: 600;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
}

.tab:hover {
  background: #f3f4f6;
}

.tab.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transform: translateY(-1px);
}

.tab-icon {
  font-size: 18px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  gap: 20px;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #e5e7eb;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-container p {
  color: #6b7280;
  font-size: 16px;
  margin: 0;
}

.ranking-content {
  animation: fadeInUp 0.5s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.podium {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 20px;
  margin-bottom: 40px;
  padding: 20px 0;
}

.podium-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: white;
  border-radius: 24px;
  padding: 24px 20px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.podium-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.12);
}

.podium-item.first {
  order: 2;
  transform: scale(1.08);
  z-index: 10;
}

.podium-item.first:hover {
  transform: scale(1.1);
}

.podium-item.second {
  order: 1;
}

.podium-item.third {
  order: 3;
}

.crown {
  font-size: 36px;
  position: absolute;
  top: -20px;
  animation: bounce 1.5s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.podium-avatar {
  font-size: 56px;
  margin-bottom: 12px;
  position: relative;
}

.podium-name {
  font-weight: 700;
  margin-bottom: 6px;
  font-size: 17px;
  color: #1f2937;
}

.podium-score {
  font-size: 20px;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 18px;
}

.podium-meta {
  color: #6b7280;
  font-size: 12px;
  margin: -10px 0 14px 0;
  white-space: nowrap;
}

.podium-stand {
  padding: 12px 28px;
  border-radius: 16px;
  font-weight: 800;
  color: white;
  font-size: 16px;
}

.first .podium-stand {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 50%, #d97706 100%);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
}

.second .podium-stand {
  background: linear-gradient(135deg, #d1d5db 0%, #9ca3af 50%, #6b7280 100%);
  box-shadow: 0 4px 12px rgba(156, 163, 175, 0.3);
}

.third .podium-stand {
  background: linear-gradient(135deg, #fcd34d 0%, #f59e0b 50%, #b45309 100%);
  box-shadow: 0 4px 12px rgba(217, 119, 6, 0.3);
}

.ranking-list {
  background: white;
  border-radius: 24px;
  padding: 10px 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  margin-bottom: 30px;
  overflow: hidden;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 12px;
  border-bottom: 1px solid #f3f4f6;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 12px;
}

.ranking-item:hover {
  background: #f9fafb;
  transform: translateX(4px);
}

.ranking-item:last-child {
  border-bottom: none;
}

.rank-number {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 15px;
  color: #4b5563;
}

.rank-avatar {
  font-size: 36px;
}

.rank-info {
  flex: 1;
}

.rank-name {
  font-weight: 700;
  color: #1f2937;
  font-size: 16px;
}

.rank-details {
  font-size: 14px;
  color: #6b7280;
  margin-top: 2px;
}

.rank-score {
  font-weight: 800;
  font-size: 18px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.my-rank {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24px;
  padding: 28px;
  color: white;
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.25);
  position: relative;
  overflow: hidden;
}

.my-rank::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: shimmer 3s ease-in-out infinite;
}

@keyframes shimmer {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(20%, 20%); }
}

.my-rank-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

.my-rank-header span:first-child {
  font-weight: 600;
  font-size: 18px;
  opacity: 0.9;
}

.my-rank-number {
  font-size: 28px;
  font-weight: 900;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.my-rank-info {
  display: flex;
  align-items: center;
  gap: 24px;
  position: relative;
  z-index: 1;
}

.my-avatar {
  font-size: 48px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
  width: 72px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.my-details {
  flex: 1;
}

.my-name {
  font-weight: 700;
  font-size: 20px;
  margin-bottom: 4px;
}

.my-stats {
  font-size: 15px;
  opacity: 0.85;
}

.my-progress {
  width: 220px;
}

.progress-bar {
  height: 10px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50px;
  overflow: hidden;
  margin-bottom: 10px;
  box-shadow: inset 0 2px 6px rgba(0, 0, 0, 0.1);
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #fff 0%, #f0f0ff 100%);
  border-radius: 50px;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.4);
}

.progress-text {
  font-size: 13px;
  opacity: 0.9;
  font-weight: 500;
}
</style>