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
          <router-link to="/user/mall" class="nav-item">{{ t('user.memberMall.navTitle') }}</router-link>
          <router-link to="/user/social" class="nav-item">{{ t('common.community') }}</router-link>
          <router-link to="/user/checkin" class="nav-item">{{ t('common.checkin') }}</router-link>
          <router-link to="/user/ranking" class="nav-item">{{ t('common.ranking') }}</router-link>
        </nav>
        <div class="user-info">
          <div class="language-switch">
            <button type="button" @click="switchLang('zh')" class="lang-btn" :class="{ active: currentLang === 'zh' }">
              {{ t('common.chinese') }}
            </button>
            <button type="button" @click="switchLang('en')" class="lang-btn" :class="{ active: currentLang === 'en' }">
              {{ t('common.english') }}
            </button>
          </div>
          <span class="points">🏆 {{ points }} {{ t('common.points') }}</span>
          <span class="username">{{ username || t('common.guestName') }}</span>
          <button type="button" class="logout-btn" @click="logout">{{ t('common.logout') }}</button>
        </div>
      </div>
    </header>

    <main class="main-content">
      <div class="mall-header">
        <div class="tabs">
          <button type="button" class="tab" :class="{ active: activeTab === 'normal' }" @click="activeTab = 'normal'">
            {{ t('user.memberMall.tabs.normal') }}
          </button>
          <button type="button" class="tab vip" :class="{ active: activeTab === 'vip' }" @click="activeTab = 'vip'">
            {{ t('user.memberMall.tabs.vip') }}
          </button>
        </div>
        <div class="search-bar">
          <input v-model="searchKeyword" type="text" class="search-input" :placeholder="t('user.memberMall.searchPlaceholder')">
          <select v-model="sortBy" class="sort-select">
            <option value="sales">{{ t('user.memberMall.sort.sales') }}</option>
            <option value="time">{{ t('user.memberMall.sort.time') }}</option>
            <option value="price-asc">{{ t('user.memberMall.sort.priceAsc') }}</option>
            <option value="price-desc">{{ t('user.memberMall.sort.priceDesc') }}</option>
          </select>
        </div>
        <div class="category-list">
          <button
            v-for="cat in categoryKeys"
            :key="cat"
            type="button"
            class="category-btn"
            :class="{ active: selectedCategory === cat }"
            @click="selectedCategory = cat"
          >
            {{ t(`user.memberMall.categories.${cat}`) }}
          </button>
        </div>
      </div>

      <div class="products-grid">
        <div v-for="product in filteredProducts" :key="product.id" class="product-card" :class="{ vip: product.isVip }">
          <div v-if="product.isVip" class="vip-badge">{{ t('user.memberMall.vipBadge') }}</div>
          <div class="product-image">{{ product.icon }}</div>
          <div class="product-info">
            <div class="product-name">{{ t(`user.memberMall.items.${product.i18nKey}.name`) }}</div>
            <div class="product-desc">{{ t(`user.memberMall.items.${product.i18nKey}.desc`) }}</div>
            <div class="product-price">¥{{ product.price }}</div>
            <div class="product-sales">{{ t('user.memberMall.sold', { n: product.sales }) }}</div>
          </div>
          <button type="button" class="add-cart-btn" @click="addToCart(product)">{{ t('user.memberMall.addToCart') }}</button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t, locale } = useI18n()
const currentLang = computed(() => locale.value)

const switchLang = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

const username = ref(localStorage.getItem('username') || '')
const points = ref(12580)
const activeTab = ref('normal')
const searchKeyword = ref('')
const sortBy = ref('sales')
const selectedCategory = ref('all')

const categoryKeys = ['all', 'gear', 'supplements', 'apparel', 'accessories']

const products = ref([
  { id: 1, i18nKey: 'p1', icon: '🏋️', price: 49, sales: 1256, category: 'gear', isVip: false },
  { id: 2, i18nKey: 'p2', icon: '🧘', price: 129, sales: 2341, category: 'gear', isVip: false },
  { id: 3, i18nKey: 'p3', icon: '💪', price: 299, sales: 3421, category: 'supplements', isVip: true },
  { id: 4, i18nKey: 'p4', icon: '👕', price: 89, sales: 1567, category: 'apparel', isVip: false },
  { id: 5, i18nKey: 'p5', icon: '⌚', price: 299, sales: 892, category: 'accessories', isVip: true },
  { id: 6, i18nKey: 'p6', icon: '🤝', price: 35, sales: 4532, category: 'accessories', isVip: false },
])

const filteredProducts = computed(() => {
  void locale.value
  let result = products.value

  if (activeTab.value === 'vip') {
    result = result.filter((p) => p.isVip)
  } else {
    result = result.filter((p) => !p.isVip)
  }

  if (selectedCategory.value !== 'all') {
    result = result.filter((p) => p.category === selectedCategory.value)
  }

  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.toLowerCase()
    result = result.filter((p) => {
      const name = t(`user.memberMall.items.${p.i18nKey}.name`).toLowerCase()
      return name.includes(kw)
    })
  }

  switch (sortBy.value) {
    case 'sales':
      result = [...result].sort((a, b) => b.sales - a.sales)
      break
    case 'time':
      result = [...result].sort((a, b) => b.id - a.id)
      break
    case 'price-asc':
      result = [...result].sort((a, b) => a.price - b.price)
      break
    case 'price-desc':
      result = [...result].sort((a, b) => b.price - a.price)
      break
    default:
      break
  }

  return result
})

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userType')
  localStorage.removeItem('user')
  localStorage.removeItem('coachId')
  router.push('/user/login')
}

const addToCart = (product) => {
  const name = t(`user.memberMall.items.${product.i18nKey}.name`)
  window.alert(t('user.memberMall.addedAlert', { name }))
}
</script>

<style scoped>
.user-dashboard {
  min-height: 100vh;
  background: #f8fafc;
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px;
}

.mall-header {
  margin-bottom: 30px;
}

.tabs {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.tab {
  padding: 12px 30px;
  background: white;
  border: none;
  border-radius: 20px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.tab.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.tab.vip.active {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.search-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
  max-width: 400px;
  padding: 14px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 15px;
  font-size: 16px;
}

.sort-select {
  padding: 14px 20px;
  border: 2px solid #e5e7eb;
  border-radius: 15px;
  font-size: 16px;
}

.category-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.category-btn {
  padding: 10px 24px;
  background: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 500;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.category-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
}

.product-card {
  background: white;
  border-radius: 20px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  position: relative;
}

.product-card.vip {
  border: 2px solid #f59e0b;
}

.vip-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.product-image {
  font-size: 50px;
  text-align: center;
  margin-bottom: 15px;
}

.product-name {
  font-weight: 600;
  font-size: 18px;
  margin-bottom: 8px;
}

.product-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.product-price {
  font-size: 24px;
  font-weight: 700;
  color: #667eea;
  margin-bottom: 5px;
}

.product-sales {
  font-size: 12px;
  color: #999;
  margin-bottom: 15px;
}

.add-cart-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
}
</style>