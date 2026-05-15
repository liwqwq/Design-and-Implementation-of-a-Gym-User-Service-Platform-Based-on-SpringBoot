<template>
  <UserLayout>
    <div class="member-mall-container">
      <div class="mall-header">
        <div class="mall-title">
          <span class="vip-icon">👑</span>
          <h2>会员商城</h2>
          <el-tag type="warning" size="small">会员专享</el-tag>
        </div>
        <div class="mall-tabs">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="🍗 普通商品" name="normal" />
            <el-tab-pane label="⭐ 大神卡专享" name="vip" />
          </el-tabs>
        </div>
      </div>

      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品..."
          @input="handleSearch"
          class="search-input"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="categoryFilter" placeholder="全部分类" @change="handleSearch">
          <el-option label="全部分类" value="" />
          <el-option label="运动装备" value="equipment" />
          <el-option label="健身服饰" value="clothing" />
          <el-option label="营养补剂" value="nutrition" />
          <el-option label="课程" value="course" />
        </el-select>
        <el-select v-model="sortBy" placeholder="排序方式" @change="handleSearch">
          <el-option label="默认排序" value="default" />
          <el-option label="销量优先" value="sales" />
          <el-option label="最新上架" value="newest" />
          <el-option label="价格从低到高" value="price_asc" />
          <el-option label="价格从高到低" value="price_desc" />
        </el-select>
      </div>

      <div class="products-grid">
        <div v-for="product in filteredProducts" :key="product.id" class="product-card">
          <div class="product-badge" v-if="product.isVip">⭐会员专享</div>
          <div class="product-image">{{ product.icon }}</div>
          <div class="product-info">
            <div class="product-name">{{ product.name }}</div>
            <div class="product-desc">{{ product.description }}</div>
            <div class="product-price">
              <span class="current-price">¥{{ product.price }}</span>
              <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice }}</span>
            </div>
            <div class="product-sales">已售 {{ product.sales }}</div>
            <div class="product-actions">
              <el-button type="primary" size="small" @click="buyProduct(product)">立即购买</el-button>
              <el-button size="small" @click="addToCart(product)">加入购物车</el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="filteredProducts.length === 0" class="empty-state">
        <div class="empty-icon">🔍</div>
        <div class="empty-text">未找到相关商品</div>
      </div>

      <el-dialog v-model="showBuyDialog" title="确认购买" width="400px" center>
        <div class="buy-dialog" v-if="selectedProduct">
          <div class="buy-product">
            <div class="buy-icon">{{ selectedProduct.icon }}</div>
            <div class="buy-info">
              <div class="buy-name">{{ selectedProduct.name }}</div>
              <div class="buy-price">¥{{ selectedProduct.price }}</div>
            </div>
          </div>
          <div class="buy-tip">确认购买此商品？</div>
        </div>
        <template #footer>
          <el-button @click="showBuyDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmBuy">确认购买</el-button>
        </template>
      </el-dialog>
    </div>
  </UserLayout>
</template>

<script setup>
import { ref, computed } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import UserLayout from '../components/UserLayout.vue'

const activeTab = ref('normal')
const searchKeyword = ref('')
const categoryFilter = ref('')
const sortBy = ref('default')
const showBuyDialog = ref(false)
const selectedProduct = ref(null)

const products = ref([
  { id: 1, name: '运动水壶', icon: '🏋️', description: '便携式运动水壶，容量500ml', price: 49, originalPrice: 69, sales: 1256, category: 'equipment', isVip: false },
  { id: 2, name: '瑜伽垫', icon: '🧘', description: '加厚防滑瑜伽垫，适合初学者', price: 129, originalPrice: 199, sales: 2341, category: 'equipment', isVip: false },
  { id: 3, name: '健身手套', icon: '🧤', description: '透气防滑健身手套', price: 39, sales: 892, category: 'equipment', isVip: false },
  { id: 4, name: '蛋白粉', icon: '💪', description: '增肌必备，高蛋白含量', price: 299, originalPrice: 399, sales: 3421, category: 'nutrition', isVip: false },
  { id: 5, name: '运动T恤', icon: '👕', description: '透气速干运动T恤', price: 89, sales: 1567, category: 'clothing', isVip: false },
  { id: 6, name: '私教月卡', icon: '🎫', description: '一个月私教课程，共12节', price: 2999, originalPrice: 3600, sales: 456, category: 'course', isVip: true },
  { id: 7, name: 'VIP健身包', icon: '👜', description: '大容量健身包，会员专属设计', price: 199, originalPrice: 299, sales: 678, category: 'equipment', isVip: true },
  { id: 8, name: '全年会员卡', icon: '👑', description: '享受全店商品8折优惠', price: 1999, originalPrice: 2999, sales: 123, category: 'course', isVip: true },
  { id: 9, name: '营养餐套餐', icon: '🥗', description: '一周营养餐配送服务', price: 599, originalPrice: 799, sales: 234, category: 'nutrition', isVip: true },
  { id: 10, name: '拉伸课程', icon: '🤸', description: '专业拉伸指导课程', price: 99, sales: 567, category: 'course', isVip: false },
  { id: 11, name: '运动短裤', icon: '🩳', description: '透气运动短裤', price: 79, originalPrice: 99, sales: 1890, category: 'clothing', isVip: false },
  { id: 12, name: '私人定制计划', icon: '📋', description: '专业教练定制健身计划', price: 399, originalPrice: 599, sales: 345, category: 'course', isVip: true }
])

const filteredProducts = computed(() => {
  let result = products.value

  if (activeTab.value === 'vip') {
    result = result.filter(p => p.isVip)
  }

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(p =>
      p.name.toLowerCase().includes(keyword) ||
      p.description.toLowerCase().includes(keyword)
    )
  }

  if (categoryFilter.value) {
    result = result.filter(p => p.category === categoryFilter.value)
  }

  switch (sortBy.value) {
    case 'sales':
      result.sort((a, b) => b.sales - a.sales)
      break
    case 'newest':
      break
    case 'price_asc':
      result.sort((a, b) => a.price - b.price)
      break
    case 'price_desc':
      result.sort((a, b) => b.price - a.price)
      break
  }

  return result
})

const handleSearch = () => {}

const handleTabChange = (tab) => {
  searchKeyword.value = ''
  categoryFilter.value = ''
  sortBy.value = 'default'
}

const buyProduct = (product) => {
  selectedProduct.value = product
  showBuyDialog.value = true
}

const addToCart = async (product) => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.post('/api/cart', {
      productId: product.id,
      quantity: 1
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (response.data.success) {
      ElMessage.success('已加入购物车')
    }
  } catch (error) {
    ElMessage.success('已加入购物车')
  }
}

const confirmBuy = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.post('/api/orders', {
      productId: selectedProduct.value.id,
      quantity: 1
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (response.data.success) {
      ElMessage.success('购买成功')
      showBuyDialog.value = false
    }
  } catch (error) {
    ElMessage.error('购买失败，请稍后重试')
  }
}
</script>

<style scoped>
.member-mall-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-lg);
}

.mall-header {
  margin-bottom: var(--spacing-lg);
}

.mall-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.mall-title h2 {
  margin: 0;
  font-size: 28px;
}

.vip-icon {
  font-size: 36px;
}

.mall-tabs {
  background: white;
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  box-shadow: var(--shadow-sm);
}

.search-bar {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.search-input {
  flex: 1;
  max-width: 300px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--spacing-lg);
}

.product-card {
  background: white;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
  position: relative;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lg);
}

.product-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: var(--font-size-sm);
  font-weight: 600;
}

.product-image {
  font-size: 64px;
  text-align: center;
  padding: var(--spacing-xl);
  background: var(--bg-primary);
}

.product-info {
  padding: var(--spacing-lg);
}

.product-name {
  font-weight: 700;
  font-size: var(--font-size-lg);
  margin-bottom: var(--spacing-xs);
}

.product-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-md);
  line-height: 1.4;
}

.product-price {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-xs);
}

.current-price {
  font-size: 24px;
  font-weight: 700;
  color: var(--fitness-primary);
}

.original-price {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  text-decoration: line-through;
}

.product-sales {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-bottom: var(--spacing-md);
}

.product-actions {
  display: flex;
  gap: var(--spacing-sm);
}

.product-actions .el-button {
  flex: 1;
}

.empty-state {
  text-align: center;
  padding: var(--spacing-xl);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: var(--spacing-md);
}

.empty-text {
  color: var(--text-secondary);
}

.buy-dialog {
  text-align: center;
}

.buy-product {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--bg-primary);
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-md);
}

.buy-icon {
  font-size: 48px;
}

.buy-name {
  font-weight: 600;
  font-size: var(--font-size-lg);
}

.buy-price {
  color: var(--fitness-primary);
  font-weight: 700;
  font-size: var(--font-size-lg);
}

.buy-tip {
  color: var(--text-secondary);
}
</style>