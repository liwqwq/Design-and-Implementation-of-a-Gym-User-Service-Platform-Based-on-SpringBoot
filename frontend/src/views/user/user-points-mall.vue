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
          <span class="points">🏆 {{ userPoints }} {{ t('common.points') }}</span>
          <span class="username">{{ username }}</span>
          <button class="logout-btn" @click="logout">{{ t('common.logout') }}</button>
        </div>
      </div>
    </header>
    
    <main class="main-content">
      <div class="points-header">
        <div class="points-banner">
          <div class="banner-icon">🎁</div>
          <div class="banner-content">
            <h2>{{ t('user.mall.myPoints') }}</h2>
            <p class="current-points">{{ userPoints }}</p>
            <p class="total-points">{{ t('user.mall.totalEarned') }} {{ totalPoints }} {{ t('common.points') }}</p>
          </div>
          <button class="go-checkin-btn" @click="goToCheckin">
            👆 {{ t('user.mall.goCheckin') }}
          </button>
        </div>
      </div>
      
      <div class="tabs-container">
        <div class="tabs">
          <button 
            class="tab" 
            :class="{ active: activeTab === 'all' }" 
            @click="switchTab('all')">
            {{ t('user.mall.allProducts') }}
          </button>
          <button 
            class="tab" 
            :class="{ active: activeTab === 'coupon' }" 
            @click="switchTab('coupon')">
            🎟️ {{ t('user.mall.coupons') }}
          </button>
          <button 
            class="tab" 
            :class="{ active: activeTab === 'product' }" 
            @click="switchTab('product')">
            🏋️ {{ t('user.mall.equipment') }}
          </button>
          <button 
            class="tab" 
            :class="{ active: activeTab === 'vip' }" 
            @click="switchTab('vip')">
            👑 {{ t('user.mall.vipExclusive') }}
          </button>
        </div>
      </div>
      
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>{{ t('common.loading') }}</p>
      </div>
      
      <div v-else class="products-grid">
        <div 
          v-for="product in filteredProducts" 
          :key="product.id" 
          class="product-card"
          :class="{ vip: product.isVip, outOfStock: product.stockQuantity === 0 }">
          <div v-if="product.isVip" class="vip-badge">{{ t('user.mall.vipExclusive') }}</div>
          <div class="product-icon">{{ product.icon }}</div>
          <div class="product-info">
            <div class="product-name">{{ displayProductName(product) }}</div>
            <div class="product-desc">{{ displayProductDescription(product) }}</div>
            <div class="product-stock" v-if="product.stockQuantity !== null">
              {{ t('user.mall.stock') }}: {{ product.stockQuantity }}
            </div>
            <div class="product-sold" v-if="product.soldCount > 0">
              {{ t('user.mall.sold') }}: {{ product.soldCount }}
            </div>
          </div>
          <div class="product-footer">
            <div class="product-cost">
              <span class="points-icon">💎</span>
              <span class="points-value">{{ product.pointsCost }}</span>
            </div>
            <button 
              class="exchange-btn"
              :disabled="product.stockQuantity === 0 || userPoints < product.pointsCost"
              @click="exchangeProduct(product)">
              {{ product.stockQuantity === 0 ? t('user.mall.soldOut') : t('user.mall.exchangeNow') }}
            </button>
          </div>
        </div>
      </div>
      
      <div class="history-section">
        <h3>📦 {{ t('user.mall.myHistory') }}</h3>
        <div v-if="exchangeHistory.length === 0" class="empty-history">
          <p>{{ t('user.mall.noHistory') }}</p>
        </div>
        <div v-else class="history-list">
          <div v-for="item in exchangeHistory" :key="item.id" class="history-item">
            <div class="history-icon">🎁</div>
            <div class="history-info">
              <div class="history-name">{{ displayHistoryProductName(item) }}</div>
              <div class="history-time">{{ formatTime(item.createdAt) }}</div>
            </div>
            <div class="history-status">
              <span class="status-badge" :class="item.status.toLowerCase()">
                {{ getStatusText(item.status) }}
              </span>
              <span class="history-points">-{{ item.pointsUsed }} {{ t('common.points') }}</span>
            </div>
          </div>
        </div>
      </div>
    </main>
    
    <div v-if="showToast" class="toast show" :class="{ error: toastType === 'error' }">
      <div class="toast-content">
        <span>{{ toastType === 'success' ? '🎉' : '❌' }}</span>
        <span>{{ toastMessage }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { clearSession, getCurrentUsername } from '../../services/session'
import { exchangePointProduct, getExchangeHistory, getMyPoints, getPointProducts, isCouponProduct, isEquipmentProduct, isVipProduct } from '../../services/pointsService'

const router = useRouter()
const { t, locale } = useI18n()
const currentLang = computed(() => locale.value)

const switchLang = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

const username = ref(getCurrentUsername('用户'))
const userPoints = ref(0)
const totalPoints = ref(0)
const activeTab = ref('all')
const loading = ref(true)
const products = ref([])
const exchangeHistory = ref([])
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('success')

const translateMallText = (text) => {
  if (text === null || text === undefined) return ''
  const raw = String(text).trim()
  if (!raw) return ''
  const key = `user.mall.products.${raw}`
  const translated = t(key)
  return translated === key ? raw : translated
}


const displayProductName = (product) => {
  if (!product) return ''
  if (locale.value === 'en' && product.nameEn) return product.nameEn
  return translateMallText(product.name || product.nameEn || '')
}

const displayProductDescription = (product) => {
  if (!product) return ''
  if (locale.value === 'en' && product.descriptionEn) return product.descriptionEn
  return translateMallText(product.description || product.descriptionEn || '')
}

const displayHistoryProductName = (item) => {
  if (!item) return ''
  if (locale.value === 'en' && item.productNameEn) return item.productNameEn
  return translateMallText(item.productName || item.productNameEn || '')
}

const filteredProducts = computed(() => {
  if (activeTab.value === 'coupon') {
    return products.value.filter(isCouponProduct)
  }
  if (activeTab.value === 'product') {
    return products.value.filter(isEquipmentProduct)
  }
  if (activeTab.value === 'vip') {
    return products.value.filter(isVipProduct)
  }
  return products.value
})

const loadPoints = async () => {
  try {
    const result = await getMyPoints()
    if (result.success) {
      userPoints.value = result.data?.availablePoints || 0
      totalPoints.value = result.data?.totalPoints || 0
    }
  } catch (error) {
    console.error('加载积分失败:', error)
  }
}

const loadProducts = async () => {
  try {
    const result = await getPointProducts()
    products.value = result.success && Array.isArray(result.data) ? result.data : []
  } catch (error) {
    console.error('加载商品失败:', error)
    products.value = []
  }
}

const loadHistory = async () => {
  try {
    const result = await getExchangeHistory()
    exchangeHistory.value = result.success && Array.isArray(result.data) ? result.data : []
  } catch (error) {
    console.error('加载兑换记录失败:', error)
    exchangeHistory.value = []
  }
}

const switchTab = (tab) => {
  // 所有商品只加载一次，分类在前端按照稳定规则筛选。
  // 这样可以兼容数据库里已有的中文分类（优惠券/运动装备/VIP专属）和英文/代码分类。
  activeTab.value = tab
}

const exchangeProduct = async (product) => {
  if (userPoints.value < product.pointsCost) {
    showToastMessage(t('user.mall.notEnoughPoints'), 'error')
    return
  }
  if (product.stockQuantity === 0) {
    showToastMessage(t('user.mall.outOfStock'), 'error')
    return
  }
  try {
    const result = await exchangePointProduct(product.id)
    if (result.success) {
      showToastMessage(t('user.mall.exchangeSuccess'), 'success')
      await Promise.all([loadPoints(), loadProducts(), loadHistory()])
    } else {
      showToastMessage(result.message || t('user.mall.exchangeFailed'), 'error')
    }
  } catch (error) {
    console.error('兑换出错:', error)
    showToastMessage(error.response?.data?.message || t('user.mall.exchangeFailed'), 'error')
  }
}

const showToastMessage = (message, type = 'success') => {
  toastMessage.value = message
  toastType.value = type
  showToast.value = true
  setTimeout(() => {
    showToast.value = false
  }, 2000)
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString(locale.value === 'en' ? 'en-US' : 'zh-CN')
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': t('user.mall.statusPending'),
    'SHIPPED': t('user.mall.statusShipped'),
    'DELIVERED': t('user.mall.statusDelivered'),
    'COMPLETED': t('user.mall.statusCompleted')
  }
  return statusMap[status] || status
}

const goToCheckin = () => {
  router.push('/user/checkin')
}

const logout = () => {
  clearSession()
  router.push('/user/login')
}

onMounted(async () => {
  await Promise.all([loadPoints(), loadProducts(), loadHistory()])
  loading.value = false
})
</script>

<style scoped>
.user-dashboard {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f4ff 0%, #f8fafc 100%);
  padding-bottom: 40px;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px;
}

.points-header {
  margin-bottom: 30px;
}

.points-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24px;
  padding: 40px;
  display: flex;
  align-items: center;
  gap: 30px;
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.25);
  position: relative;
  overflow: hidden;
}

.points-banner::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: shimmer 8s ease-in-out infinite;
}

@keyframes shimmer {
  0%, 100% { transform: translate(-20%, -20%); }
  50% { transform: translate(20%, 20%); }
}

.banner-icon {
  font-size: 64px;
  z-index: 1;
}

.banner-content {
  flex: 1;
  z-index: 1;
}

.banner-content h2 {
  margin: 0 0 10px 0;
  color: white;
  font-size: 24px;
  font-weight: 600;
}

.current-points {
  font-size: 56px;
  font-weight: 800;
  color: white;
  line-height: 1;
  margin: 10px 0;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.total-points {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  margin: 0;
}

.go-checkin-btn {
  padding: 16px 32px;
  background: white;
  color: #667eea;
  border: none;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  z-index: 1;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.go-checkin-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.tabs-container {
  background: white;
  border-radius: 16px;
  padding: 10px;
  margin-bottom: 30px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tab {
  padding: 12px 24px;
  background: transparent;
  border: none;
  border-radius: 12px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.tab:hover {
  background: #f0f4ff;
  color: #667eea;
}

.tab.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
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
  color: #666;
  margin: 0;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

.product-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  position: relative;
  display: flex;
  flex-direction: column;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.product-card.vip {
  border: 2px solid #f59e0b;
  background: linear-gradient(180deg, #fffbeb 0%, white 100%);
}

.product-card.outOfStock {
  opacity: 0.6;
}

.vip-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
}

.product-icon {
  font-size: 56px;
  text-align: center;
  margin-bottom: 16px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-weight: 600;
  font-size: 18px;
  color: #1f2937;
  margin-bottom: 8px;
}

.product-desc {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 12px;
  line-height: 1.5;
}

.product-stock,
.product-sold {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 4px;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.product-cost {
  display: flex;
  align-items: center;
  gap: 6px;
}

.points-icon {
  font-size: 18px;
}

.points-value {
  font-size: 24px;
  font-weight: 800;
  color: #f59e0b;
}

.exchange-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.exchange-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.35);
}

.exchange-btn:disabled {
  background: #e5e7eb;
  color: #9ca3af;
  cursor: not-allowed;
}

.history-section {
  background: white;
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.history-section h3 {
  margin: 0 0 24px 0;
  font-size: 20px;
  color: #1f2937;
}

.empty-history {
  text-align: center;
  padding: 40px 0;
  color: #9ca3af;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  transition: all 0.3s;
}

.history-item:hover {
  background: #f0f4ff;
}

.history-icon {
  font-size: 32px;
}

.history-info {
  flex: 1;
}

.history-name {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.history-time {
  font-size: 13px;
  color: #9ca3af;
}

.history-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.shipped {
  background: #dbeafe;
  color: #2563eb;
}

.status-badge.delivered,
.status-badge.completed {
  background: #dcfce7;
  color: #16a34a;
}

.history-points {
  font-size: 14px;
  font-weight: 600;
  color: #ef4444;
}

.toast {
  position: fixed;
  top: 100px;
  left: 50%;
  transform: translateX(-50%) translateY(-20px);
  background: white;
  padding: 16px 32px;
  border-radius: 16px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  opacity: 0;
  transition: all 0.3s;
  z-index: 9999;
}

.toast.show {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

.toast.error {
  border-left: 4px solid #ef4444;
}

.toast-content {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
}
</style>
