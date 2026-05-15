<template>
  <UserLayout>
    <div class="products-container">
      <h2 class="page-title">🛍️ 在线商城</h2>

      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索商品..." @input="handleSearch" clearable />
        <el-select v-model="categoryFilter" placeholder="全部分类" @change="handleSearch">
          <el-option label="全部分类" value="" />
          <el-option label="运动装备" value="equipment" />
          <el-option label="健身服饰" value="clothing" />
          <el-option label="营养补剂" value="nutrition" />
        </el-select>
        <el-select v-model="sortBy" placeholder="排序方式" @change="handleSearch">
          <el-option label="默认排序" value="default" />
          <el-option label="销量优先" value="sales" />
          <el-option label="价格从低到高" value="price_asc" />
          <el-option label="价格从高到低" value="price_desc" />
        </el-select>
      </div>

      <div class="products-grid">
        <div v-for="product in filteredProducts" :key="product.id" class="product-card">
          <div class="product-image">{{ product.icon }}</div>
          <div class="product-name">{{ product.name }}</div>
          <div class="product-desc">{{ product.description }}</div>
          <div class="product-price">¥{{ product.price }}</div>
          <div class="product-sales">已售 {{ product.sales }}</div>
          <div class="product-actions">
            <el-button type="primary" size="small" @click="buyProduct(product)">立即购买</el-button>
            <el-button size="small" @click="addToCart(product)">加入购物车</el-button>
          </div>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<script setup>
import { ref, computed } from 'vue'
import axios from 'axios'
import UserLayout from '../components/UserLayout.vue'
import { ElButton, ElInput, ElSelect } from 'element-plus'

const searchKeyword = ref('')
const categoryFilter = ref('')
const sortBy = ref('default')

const products = ref([
  { id: 1, name: '运动水壶', icon: '🏋️', description: '便携式运动水壶', price: 49, sales: 1256, category: 'equipment' },
  { id: 2, name: '瑜伽垫', icon: '🧘', description: '加厚防滑瑜伽垫', price: 129, sales: 2341, category: 'equipment' },
  { id: 3, name: '健身手套', icon: '🧤', description: '透气防滑手套', price: 39, sales: 892, category: 'equipment' },
  { id: 4, name: '蛋白粉', icon: '💪', description: '高蛋白增肌粉', price: 299, sales: 3421, category: 'nutrition' },
  { id: 5, name: '运动T恤', icon: '👕', description: '透气速干T恤', price: 89, sales: 1567, category: 'clothing' },
  { id: 6, name: '运动短裤', icon: '🩳', description: '透气运动短裤', price: 79, sales: 1890, category: 'clothing' }
])

const filteredProducts = computed(() => {
  let result = products.value
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(p => p.name.toLowerCase().includes(keyword) || p.description.toLowerCase().includes(keyword))
  }
  if (categoryFilter.value) {
    result = result.filter(p => p.category === categoryFilter.value)
  }
  switch (sortBy.value) {
    case 'sales': result.sort((a, b) => b.sales - a.sales); break
    case 'price_asc': result.sort((a, b) => a.price - b.price); break
    case 'price_desc': result.sort((a, b) => b.price - a.price); break
  }
  return result
})

const handleSearch = () => {}

const buyProduct = async (product) => {
  try {
    const token = localStorage.getItem('token')
    await axios.post('/api/orders', { productId: product.id, quantity: 1 }, { headers: { Authorization: `Bearer ${token}` } })
    ElMessage.success('购买成功！')
  } catch (err) {
    ElMessage.error('购买失败')
  }
}

const addToCart = async (product) => {
  try {
    const token = localStorage.getItem('token')
    await axios.post('/api/cart', { productId: product.id, quantity: 1 }, { headers: { Authorization: `Bearer ${token}` } })
    ElMessage.success('已加入购物车')
  } catch (err) {
    ElMessage.success('已加入购物车')
  }
}
</script>

<style scoped>
.products-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  text-align: center;
  margin-bottom: 30px;
}

.search-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
}

.search-bar .el-input {
  flex: 1;
  max-width: 300px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.product-card {
  background: white;
  border-radius: 15px;
  padding: 25px;
  text-align: center;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.15);
}

.product-image {
  font-size: 56px;
  margin-bottom: 15px;
}

.product-name {
  font-weight: 600;
  font-size: 18px;
  margin-bottom: 10px;
}

.product-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
}

.product-price {
  font-size: 24px;
  font-weight: 700;
  color: #667eea;
  margin-bottom: 5px;
}

.product-sales {
  font-size: 13px;
  color: #999;
  margin-bottom: 15px;
}

.product-actions {
  display: flex;
  gap: 10px;
}

.product-actions .el-button {
  flex: 1;
}
</style>
