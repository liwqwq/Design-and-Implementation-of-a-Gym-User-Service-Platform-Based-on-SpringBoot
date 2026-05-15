<template>
  <UserLayout>
    <div class="community-container">
      <h2 class="page-title">💬 社区互动</h2>

      <div class="community-cards">
        <div class="community-card" @click="goToSocial">
          <div class="card-icon">📝</div>
          <div class="card-title">社区帖子</div>
          <div class="card-desc">浏览和发布健身相关的帖子</div>
        </div>
        <div class="community-card" @click="goToCheckIn">
          <div class="card-icon">✅</div>
          <div class="card-title">每日打卡</div>
          <div class="card-desc">记录每天的健身成果</div>
        </div>
        <div class="community-card" @click="goToRanking">
          <div class="card-icon">🏆</div>
          <div class="card-title">排行榜</div>
          <div class="card-desc">查看健身达人排名</div>
        </div>
        <div class="community-card" @click="goToTeam">
          <div class="card-icon">👥</div>
          <div class="card-title">健身约搭子</div>
          <div class="card-desc">组队一起健身</div>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<script setup>
import { useRouter } from 'vue-router'
import UserLayout from '../components/UserLayout.vue'

const router = useRouter()

const goToSocial = () => router.push('/social')
const goToCheckIn = () => router.push('/checkin')
const goToRanking = () => router.push('/ranking')
const goToTeam = () => router.push('/social')
</script>

<style scoped>
.community-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  text-align: center;
  margin-bottom: 30px;
}

.community-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.community-card {
  background: white;
  border-radius: 15px;
  padding: 30px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.community-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.15);
}

.card-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.card-title {
  font-weight: 600;
  font-size: 18px;
  margin-bottom: 10px;
}

.card-desc {
  font-size: 14px;
  color: #666;
}
</style>
