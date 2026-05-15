<template>
  <UserLayout>
    <div class="membership-container">
      <h2 class="page-title">💳 会员中心</h2>

      <div class="membership-card">
        <div class="card-header">
          <span>我的会员状态</span>
        </div>
        <div class="membership-info">
          <div class="info-item">
            <label>会员等级：</label>
            <span class="vip-badge">{{ userLevel }}</span>
          </div>
          <div class="info-item">
            <label>会员有效期：</label>
            <span>{{ expireDate }}</span>
          </div>
          <div class="info-item">
            <label>剩余天数：</label>
            <span>{{ remainingDays }} 天</span>
          </div>
          <div class="info-item">
            <label>累计消费：</label>
            <span>¥{{ totalSpent }}</span>
          </div>
        </div>
      </div>

      <div class="membership-card">
        <div class="card-header">
          <span>会员特权</span>
        </div>
        <div class="privileges-grid">
          <div class="privilege-item">
            <div class="privilege-icon">🎟️</div>
            <div class="privilege-name">专属课程</div>
            <div class="privilege-desc">免费参加会员专属课程</div>
          </div>
          <div class="privilege-item">
            <div class="privilege-icon">💰</div>
            <div class="privilege-name">消费折扣</div>
            <div class="privilege-desc">全场商品9折优惠</div>
          </div>
          <div class="privilege-item">
            <div class="privilege-icon">🎁</div>
            <div class="privilege-name">生日礼包</div>
            <div class="privilege-desc">生日当月领取专属礼包</div>
          </div>
          <div class="privilege-item">
            <div class="privilege-icon">⭐</div>
            <div class="privilege-name">双倍积分</div>
            <div class="privilege-desc">每次消费获得双倍积分</div>
          </div>
        </div>
      </div>

      <div class="membership-card">
        <div class="card-header">
          <span>升级会员</span>
        </div>
        <div class="plans-grid">
          <div class="plan-item" v-for="plan in plans" :key="plan.name">
            <div class="plan-name">{{ plan.name }}</div>
            <div class="plan-price">¥{{ plan.price }}</div>
            <div class="plan-period">{{ plan.period }}</div>
            <ul class="plan-features">
              <li v-for="feature in plan.features" :key="feature">{{ feature }}</li>
            </ul>
            <el-button type="primary" @click="buyPlan(plan)">立即购买</el-button>
          </div>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import UserLayout from '../components/UserLayout.vue'
import { ElButton } from 'element-plus'

const userLevel = ref('普通会员')
const expireDate = ref('2025-12-31')
const remainingDays = ref(234)
const totalSpent = ref(2680)

const plans = ref([
  {
    name: '月卡',
    price: 199,
    period: '30天',
    features: ['无限次入场', '基础课程', '会员折扣']
  },
  {
    name: '季卡',
    price: 499,
    period: '90天',
    features: ['无限次入场', '全部课程', '会员折扣', '双倍积分']
  },
  {
    name: '年卡',
    price: 1699,
    period: '365天',
    features: ['无限次入场', '全部课程', '会员折扣', '双倍积分', '生日礼包', '专属客服']
  }
])

const buyPlan = async (plan) => {
  try {
    const token = localStorage.getItem('token')
    await axios.post('/api/membership/buy', { planId: plan.name }, { headers: { Authorization: `Bearer ${token}` } })
    ElMessage.success(`成功购买${plan.name}！`)
  } catch (err) {
    ElMessage.error('购买失败')
  }
}
</script>

<style scoped>
.membership-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  text-align: center;
  margin-bottom: 30px;
}

.membership-card {
  background: white;
  border-radius: 15px;
  padding: 25px;
  margin-bottom: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.card-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.card-header span {
  font-weight: 600;
  font-size: 18px;
}

.membership-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.info-item {
  display: flex;
  gap: 10px;
  align-items: center;
}

.info-item label {
  font-weight: 500;
}

.vip-badge {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  padding: 5px 15px;
  border-radius: 20px;
  font-weight: 600;
}

.privileges-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.privilege-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 10px;
}

.privilege-icon {
  font-size: 36px;
  margin-bottom: 10px;
}

.privilege-name {
  font-weight: 600;
  margin-bottom: 5px;
}

.privilege-desc {
  font-size: 13px;
  color: #666;
}

.plans-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.plan-item {
  background: #f8f9fa;
  border-radius: 15px;
  padding: 25px;
  text-align: center;
  position: relative;
}

.plan-item:nth-child(3)::before {
  content: '推荐';
  position: absolute;
  top: -10px;
  right: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 3px 12px;
  border-radius: 10px;
  font-size: 12px;
}

.plan-name {
  font-weight: 600;
  font-size: 20px;
  margin-bottom: 10px;
}

.plan-price {
  font-size: 32px;
  font-weight: 700;
  color: #667eea;
  margin-bottom: 5px;
}

.plan-period {
  font-size: 14px;
  color: #666;
  margin-bottom: 20px;
}

.plan-features {
  list-style: none;
  padding: 0;
  margin-bottom: 20px;
  text-align: left;
}

.plan-features li {
  padding: 8px 0;
  border-bottom: 1px solid #eee;
  font-size: 14px;
}
</style>
