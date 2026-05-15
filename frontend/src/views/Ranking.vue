<template>
  <UserLayout>
    <div class="ranking-container">
      <div class="page-header">
        <h2>🏆 排行榜</h2>
        <p class="page-desc">按预约课程节数排名，看看谁是最努力的健身达人！</p>
      </div>

      <div class="ranking-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="🏅 总排行榜" name="all" />
          <el-tab-pane label="💪 本周排行" name="week" />
          <el-tab-pane label="🔥 本月排行" name="month" />
        </el-tabs>
      </div>

      <div class="ranking-list">
        <div v-for="(user, index) in rankingList" :key="user.id" class="ranking-item" :class="{ 'top-three': index < 3 }">
          <div class="rank-number">
            <span v-if="index === 0" class="medal gold">🥇</span>
            <span v-else-if="index === 1" class="medal silver">🥈</span>
            <span v-else-if="index === 2" class="medal bronze">🥉</span>
            <span v-else class="rank">{{ index + 1 }}</span>
          </div>
          <div class="user-avatar">{{ user.avatar }}</div>
          <div class="user-info">
            <div class="user-name">{{ user.name }}</div>
            <div class="user-detail">
            <span>{{ user.checkins }} 次打卡</span>
            <span>连续 {{ user.streak }} 天</span>
          </div>
          </div>
          <div class="user-stats">
            <div class="stat-value">{{ user.bookings }}</div>
            <div class="stat-label">预约节数</div>
          </div>
          <div class="user-badge" v-if="user.badges && user.badges.length > 0">
            <span v-for="badge in user.badges" :key="badge" class="badge">{{ badge }}</span>
          </div>
        </div>
      </div>

      <div class="my-ranking" v-if="currentUserRank">
        <div class="my-rank-title">我的排名</div>
        <div class="my-rank-info">
          <span class="rank">{{ currentUserRank.rank }}</span>
          <span class="name">{{ currentUserRank.name }}</span>
          <span class="bookings">{{ currentUserRank.bookings }} 节</span>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import UserLayout from '../components/UserLayout.vue'

const activeTab = ref('all')
const rankingList = ref([])
const currentUserRank = ref(null)

const fetchRanking = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/ranking', {
      params: { type: activeTab.value },
      headers: { Authorization: `Bearer ${token}` }
    })
    if (response.data.success) {
      rankingList.value = response.data.data.list || generateMockData()
      currentUserRank.value = response.data.data.myRank || null
    }
  } catch (error) {
    rankingList.value = generateMockData()
  }
}

const generateMockData = () => {
  return [
    { id: 1, avatar: '💪', name: '健身狂人', checkins: 365, streak: 30, bookings: 256, badges: ['🏆', '🔥'] },
    { id: 2, avatar: '🏋️', name: '力量小子', checkins: 300, streak: 25, bookings: 220, badges: ['🏆'] },
    { id: 3, avatar: '🧘', name: '瑜伽达人', checkins: 280, streak: 20, bookings: 198, badges: ['⭐'] },
    { id: 4, avatar: '🏃', name: '跑步爱好者', checkins: 250, streak: 15, bookings: 175, badges: [] },
    { id: 5, avatar: '💃', name: '舞动青春', checkins: 230, streak: 18, bookings: 160, badges: [] },
    { id: 6, avatar: '🤸', name: '柔术高手', checkins: 210, streak: 12, bookings: 145, badges: [] },
    { id: 7, avatar: '🏊', name: '游泳健将', checkins: 190, streak: 10, bookings: 130, badges: [] },
    { id: 8, avatar: '🚴', name: '骑行达人', checkins: 170, streak: 8, bookings: 115, badges: [] },
    { id: 9, avatar: '⚽', name: '运动健将', checkins: 150, streak: 7, bookings: 100, badges: [] },
    { id: 10, avatar: '🎾', name: '网球爱好者', checkins: 130, streak: 5, bookings: 85, badges: [] }
  ]
}

onMounted(() => {
  fetchRanking()
})
</script>

<style scoped>
.ranking-container {
  max-width: 900px;
  margin: 0 auto;
  padding: var(--spacing-lg);
}

.page-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.page-header h2 {
  font-size: 28px;
  margin-bottom: var(--spacing-xs);
}

.page-desc {
  color: var(--text-secondary);
}

.ranking-tabs {
  background: white;
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.ranking-item {
  background: white;
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
}

.ranking-item:hover {
  transform: translateX(5px);
  box-shadow: var(--shadow-md);
}

.ranking-item.top-three {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.rank-number {
  width: 50px;
  text-align: center;
}

.medal {
  font-size: 36px;
}

.rank {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-secondary);
}

.ranking-item.top-three .rank {
  color: white;
}

.user-avatar {
  font-size: 48px;
}

.user-info {
  flex: 1;
}

.user-name {
  font-weight: 700;
  font-size: var(--font-size-lg);
  margin-bottom: var(--spacing-xs);
}

.user-detail {
  display: flex;
  gap: var(--spacing-md);
  font-size: var(--font-size-sm);
  opacity: 0.9;
}

.user-stats {
  text-align: center;
  min-width: 100px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
}

.stat-label {
  font-size: var(--font-size-sm);
  opacity: 0.8;
}

.user-badge {
  display: flex;
  gap: var(--spacing-xs);
}

.badge {
  font-size: 24px;
}

.my-ranking {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  margin-top: var(--spacing-lg);
  text-align: center;
}

.my-rank-title {
  font-size: var(--font-size-sm);
  opacity: 0.9;
  margin-bottom: var(--spacing-sm);
}

.my-rank-info {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--spacing-lg);
}

.my-rank-info .rank {
  font-size: 32px;
  font-weight: 700;
  color: white;
}

.my-rank-info .name {
  font-size: var(--font-size-lg);
  font-weight: 600;
}

.my-rank-info .bookings {
  font-size: var(--font-size-lg);
}
</style>
