<template>
  <UserLayout>
    <div class="dashboard-content">
      <div class="welcome-card">
        <h2>欢迎回来，{{ username }}！</h2>
        <p>这里是您的个人仪表板，您可以在这里管理您的账户和会员信息。</p>
        <div class="stats-grid">
          <div class="stat-card" v-for="(stat, index) in stats" :key="index" :style="{ animationDelay: `${index * 0.1}s` }">
            <div class="stat-icon">{{ stat.icon }}</div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="recent-activities">
        <h3>最近活动</h3>
        <div class="activity-list">
          <div class="activity-item" v-for="(activity, index) in activities" :key="index">
            <div class="activity-icon">{{ activity.icon }}</div>
            <div class="activity-content">
              <div class="activity-title">{{ activity.title }}</div>
              <div class="activity-time">{{ activity.time }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="fitness-plans">
        <h3>健身计划推荐</h3>
        <div class="plans-grid">
          <div class="plan-card" v-for="plan in plans" :key="plan.title" @click="showPlanDetail(plan)">
            <div class="plan-icon">{{ plan.icon }}</div>
            <div class="plan-title">{{ plan.title }}</div>
            <div class="plan-desc">{{ plan.description }}</div>
            <el-button type="primary" size="small">查看详情</el-button>
          </div>
        </div>
      </div>

      <el-dialog v-model="showPlanModal" title="计划详情" width="600px">
        <div class="plan-detail" v-if="selectedPlan">
          <div class="detail-header">
            <span class="detail-icon">{{ selectedPlan.icon }}</span>
            <div class="detail-info">
              <h4>{{ selectedPlan.title }}</h4>
              <p>{{ selectedPlan.description }}</p>
            </div>
          </div>
          <div class="detail-content">
            <div class="detail-item">
              <label>计划类型：</label>
              <span>{{ selectedPlan.type }}</span>
            </div>
            <div class="detail-item">
              <label>适合人群：</label>
              <span>{{ selectedPlan.targetAudience }}</span>
            </div>
            <div class="detail-item">
              <label>训练频率：</label>
              <span>{{ selectedPlan.frequency }}</span>
            </div>
            <div class="detail-item">
              <label>训练时长：</label>
              <span>{{ selectedPlan.duration }}</span>
            </div>
            <div class="detail-item">
              <label>训练目标：</label>
              <span>{{ selectedPlan.goal }}</span>
            </div>
            <div class="detail-item">
              <label>训练内容：</label>
              <ul>
                <li v-for="(exercise, index) in selectedPlan.exercises" :key="index">{{ exercise }}</li>
              </ul>
            </div>
            <div class="detail-item">
              <label>注意事项：</label>
              <p>{{ selectedPlan.notes }}</p>
            </div>
          </div>
        </div>
        <template #footer>
          <el-button @click="closePlanModal">关闭</el-button>
        </template>
      </el-dialog>
    </div>
  </UserLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import UserLayout from '../components/UserLayout.vue'
import { ElButton, ElDialog } from 'element-plus'

const username = ref(localStorage.getItem('username') || '用户')
const showPlanModal = ref(false)
const selectedPlan = ref({})

const stats = ref([
  { icon: '👤', value: 128, label: '总用户数' },
  { icon: '📊', value: 86, label: '会员数' },
  { icon: '🏋️', value: 45, label: '活跃用户' }
])

const activities = ref([
  { icon: '🏃', title: '完成了30分钟跑步', time: '今天 14:30' },
  { icon: '💪', title: '完成了力量训练', time: '昨天 16:45' },
  { icon: '🧘', title: '完成了瑜伽练习', time: '3天前' }
])

const plans = ref([
  {
    icon: '🔥',
    title: '初学者训练计划',
    description: '适合健身新手的基础训练计划',
    type: '基础训练',
    targetAudience: '健身新手',
    frequency: '每周3次',
    duration: '每次45分钟',
    goal: '建立基础体能，掌握正确动作姿势',
    exercises: ['热身：5-10分钟', '俯卧撑：3组 × 10次', '深蹲：3组 × 15次', '平板支撑：3组 × 30秒'],
    notes: '初学者应该注意动作的正确性，不要追求重量。'
  },
  {
    icon: '🏋️',
    title: '力量增长计划',
    description: '针对肌肉增长的训练计划',
    type: '力量训练',
    targetAudience: '有一定基础的健身者',
    frequency: '每周4次',
    duration: '每次60分钟',
    goal: '增加肌肉质量，提高力量水平',
    exercises: ['热身：10分钟', '卧推：4组 × 8-12次', '硬拉：4组 × 6-8次', '深蹲：4组 × 8-10次'],
    notes: '注意循序渐进，逐渐增加训练重量。'
  },
  {
    icon: '💨',
    title: '减脂塑形计划',
    description: '高效燃脂，塑造完美身材',
    type: '有氧训练',
    targetAudience: '希望减脂的人群',
    frequency: '每周5次',
    duration: '每次40分钟',
    goal: '减少体脂率，塑造身材线条',
    exercises: ['热身：5分钟', 'HIIT训练：20分钟', '有氧运动：15分钟', '拉伸：5分钟'],
    notes: '配合合理饮食，效果更佳。'
  }
])

const showPlanDetail = (plan) => {
  selectedPlan.value = plan
  showPlanModal.value = true
}

const closePlanModal = () => {
  showPlanModal.value = false
}
</script>

<style scoped>
.dashboard-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 40px;
  color: white;
  margin-bottom: 30px;
}

.welcome-card h2 {
  margin: 0 0 10px 0;
  font-size: 28px;
}

.welcome-card p {
  margin: 0 0 30px 0;
  opacity: 0.9;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 15px;
  padding: 20px;
  text-align: center;
}

.stat-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.recent-activities,
.fitness-plans {
  background: white;
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.recent-activities h3,
.fitness-plans h3 {
  margin: 0 0 20px 0;
  font-size: 20px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 10px;
}

.activity-icon {
  font-size: 28px;
}

.activity-title {
  font-weight: 600;
}

.activity-time {
  font-size: 14px;
  color: #666;
}

.plans-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.plan-card {
  background: #f8f9fa;
  border-radius: 15px;
  padding: 25px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.plan-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.2);
}

.plan-icon {
  font-size: 40px;
  margin-bottom: 15px;
}

.plan-title {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 10px;
}

.plan-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
}

.plan-detail {
  padding: 20px;
}

.detail-header {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.detail-icon {
  font-size: 50px;
}

.detail-info h4 {
  margin: 0 0 10px 0;
  font-size: 20px;
}

.detail-info p {
  margin: 0;
  color: #666;
}

.detail-content {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 20px;
}

.detail-item {
  margin-bottom: 15px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-item label {
  font-weight: 600;
  color: #333;
}

.detail-item ul {
  margin: 0;
  padding-left: 20px;
}

.detail-item li {
  margin-bottom: 5px;
}
</style>
