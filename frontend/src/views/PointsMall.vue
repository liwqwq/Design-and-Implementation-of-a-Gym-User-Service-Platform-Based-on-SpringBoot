<template>
  <UserLayout>
    <div class="points-mall-container">
      <div class="points-header">
        <div class="points-info">
          <div class="points-icon">💰</div>
          <div class="points-detail">
            <div class="points-label">我的积分</div>
            <div class="points-value">{{ userPoints }}</div>
          </div>
        </div>
        <el-button type="primary" @click="goToCheckIn">每日打卡</el-button>
      </div>

      <div class="mall-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="🛒 积分商城" name="mall" />
          <el-tab-pane label="📜 兑换记录" name="records" />
        </el-tabs>
      </div>

      <div v-if="activeTab === 'mall'" class="mall-content">
        <div class="mall-header">
          <h3>积分兑换商品</h3>
          <el-select v-model="categoryFilter" placeholder="全部分类" size="small">
            <el-option label="全部分类" value="" />
            <el-option label="运动装备" value="equipment" />
            <el-option label="健身服饰" value="clothing" />
            <el-option label="营养补充" value="nutrition" />
            <el-option label="课程券" value="course" />
          </el-select>
        </div>
        <div class="product-grid">
          <div v-for="product in filteredProducts" :key="product.id" class="product-card">
            <div class="product-image">🎁</div>
            <div class="product-info">
              <div class="product-name">{{ product.name }}</div>
              <div class="product-points">
                <span class="points-tag">{{ product.points }} 积分</span>
                <span v-if="product.vipOnly" class="vip-tag">会员专享</span>
              </div>
              <el-button type="primary" size="small" :disabled="userPoints < product.points" @click="redeemProduct(product)">立即兑换</el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="records-content">
        <div v-if="exchangeRecords.length === 0" class="empty-state">暂无兑换记录</div>
        <div v-else class="records-list">
          <div v-for="record in exchangeRecords" :key="record.id" class="record-item">
            <div class="record-icon">🎁</div>
            <div class="record-info">
              <div class="record-name">{{ record.productName }}</div>
              <div class="record-time">{{ record.exchangeTime }}</div>
            </div>
            <div class="record-points">-{{ record.points }}积分</div>
          </div>
        </div>
      </div>

      <el-dialog v-model="showRedeemDialog" title="确认兑换" width="400px">
        <div v-if="selectedProduct">
          <div class="redeem-product">
            <div class="redeem-icon">🎁</div>
            <div class="redeem-info">
              <div class="redeem-name">{{ selectedProduct.name }}</div>
              <div class="redeem-points">{{ selectedProduct.points }} 积分</div>
            </div>
          </div>
        </div>
        <template #footer>
          <el-button @click="showRedeemDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmRedeem">确认兑换</el-button>
        </template>
      </el-dialog>
    </div>
  </UserLayout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import UserLayout from '../components/UserLayout.vue'
import { ElButton, ElDialog, ElSelect, ElTabPane } from 'element-plus'

const router = useRouter()
const userPoints = ref(0)
const activeTab = ref('mall')
const categoryFilter = ref('')
const showRedeemDialog = ref(false)
const selectedProduct = ref(null)

const products = ref([
  { id: 1, name: '运动水壶', points: 500, category: 'equipment' },
  { id: 2, name: '瑜伽垫', points: 1000, category: 'equipment' },
  { id: 3, name: '蛋白粉', points: 2000, category: 'nutrition' },
  { id: 4, name: '私教体验券', points: 5000, category: 'course', vipOnly: true },
  { id: 5, name: '运动T恤', points: 3000, category: 'clothing', vipOnly: true },
  { id: 6, name: '健身背包', points: 2500, category: 'equipment' }
])

const exchangeRecords = ref([])

const filteredProducts = computed(() => {
  return categoryFilter.value ? products.value.filter(p => p.category === categoryFilter.value) : products.value
})

const goToCheckIn = () => router.push('/user/checkin')
const redeemProduct = (product) => { selectedProduct.value = product; showRedeemDialog.value = true }
const confirmRedeem = () => {
  userPoints.value -= selectedProduct.value.points
  ElMessage.success('兑换成功！')
  showRedeemDialog.value = false
}
</script>

<style scoped>
.points-mall-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.points-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  color: white;
}

.points-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.points-icon {
  font-size: 48px;
}

.points-label {
  font-size: 14px;
  opacity: 0.9;
}

.points-value {
  font-size: 32px;
  font-weight: 700;
}

.mall-tabs {
  background: white;
  border-radius: 15px;
  padding: 15px;
  margin-bottom: 20px;
}

.mall-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.product-card {
  background: white;
  border-radius: 15px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.product-image {
  font-size: 48px;
  margin-bottom: 15px;
}

.product-name {
  font-weight: 600;
  margin-bottom: 10px;
}

.product-points {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 15px;
}

.points-tag {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 14px;
}

.vip-tag {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 14px;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.record-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: white;
  border-radius: 15px;
}

.record-icon {
  font-size: 32px;
}

.record-info {
  flex: 1;
}

.record-name {
  font-weight: 600;
}

.record-time {
  font-size: 14px;
  color: #666;
}

.record-points {
  color: #f5576c;
  font-weight: 600;
}

.empty-state {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 15px;
  color: #666;
}

.redeem-product {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 30px;
  background: #f8f9fa;
  border-radius: 10px;
}

.redeem-icon {
  font-size: 48px;
}

.redeem-name {
  font-weight: 600;
  font-size: 18px;
}

.redeem-points {
  color: #667eea;
  font-weight: 600;
}
</style>
