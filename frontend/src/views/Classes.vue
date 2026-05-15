<template>
  <UserLayout>
    <div class="classes-container">
      <h2 class="page-title">📅 课程管理</h2>

      <div class="tabs">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="👤 私教课程" name="private" />
          <el-tab-pane label="👥 团课课程" name="group" />
        </el-tabs>
      </div>

      <div v-if="activeTab === 'private'" class="trainers-grid">
        <div v-for="trainer in trainers" :key="trainer.id" class="trainer-card">
          <div class="trainer-avatar">{{ trainer.avatar }}</div>
          <div class="trainer-info">
            <div class="trainer-name">{{ trainer.name }}</div>
            <div class="trainer-title">{{ trainer.title }}</div>
            <div class="trainer-specialties">
              <el-tag v-for="specialty in trainer.specialties" :key="specialty" size="small">{{ specialty }}</el-tag>
            </div>
          </div>
          <el-button type="primary" @click="goToPrivateTrainers">查看详情</el-button>
        </div>
      </div>

      <div v-else class="classes-list">
        <div v-for="cls in groupClasses" :key="cls.id" class="class-card">
          <div class="class-time">{{ cls.time }}</div>
          <div class="class-info">
            <div class="class-name">{{ cls.name }}</div>
            <div class="class-trainer">{{ cls.trainer }}</div>
          </div>
          <div class="class-status">{{ cls.currentCount }}/{{ cls.maxCount }}</div>
          <el-button type="primary" size="small" :disabled="cls.currentCount >= cls.maxCount">预约</el-button>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import UserLayout from '../components/UserLayout.vue'
import { ElButton, ElTabs, ElTag } from 'element-plus'

const router = useRouter()
const activeTab = ref('private')

const trainers = ref([
  { id: 1, avatar: '👨‍🏫', name: '张教练', title: '高级私人教练', specialties: ['增肌', '减脂'] },
  { id: 2, avatar: '👩‍🏫', name: '李教练', title: '瑜伽导师', specialties: ['瑜伽', '普拉提'] },
  { id: 3, avatar: '🏋️', name: '王教练', title: '力量训练专家', specialties: ['力量举', '健美'] }
])

const groupClasses = ref([
  { id: 1, name: '燃脂搏击', time: '09:00 - 10:00', trainer: '张教练', currentCount: 18, maxCount: 30 },
  { id: 2, name: '瑜伽基础', time: '10:30 - 11:30', trainer: '李教练', currentCount: 12, maxCount: 25 },
  { id: 3, name: '动感单车', time: '14:00 - 15:00', trainer: '王教练', currentCount: 20, maxCount: 20 }
])

const handleTabChange = () => {}

const goToPrivateTrainers = () => router.push('/private-trainers')
</script>

<style scoped>
.classes-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  text-align: center;
  margin-bottom: 30px;
}

.tabs {
  background: white;
  border-radius: 15px;
  padding: 20px;
  margin-bottom: 30px;
}

.trainers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.trainer-card {
  background: white;
  border-radius: 15px;
  padding: 25px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.trainer-avatar {
  font-size: 56px;
}

.trainer-info {
  flex: 1;
}

.trainer-name {
  font-weight: 600;
  font-size: 18px;
  margin-bottom: 5px;
}

.trainer-title {
  color: #666;
  margin-bottom: 10px;
}

.trainer-specialties {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.classes-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.class-card {
  background: white;
  border-radius: 15px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.class-time {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 10px 20px;
  border-radius: 10px;
  font-weight: 600;
}

.class-info {
  flex: 1;
}

.class-name {
  font-weight: 600;
  margin-bottom: 5px;
}

.class-trainer {
  color: #666;
}

.class-status {
  font-weight: 600;
  color: #667eea;
}
</style>
