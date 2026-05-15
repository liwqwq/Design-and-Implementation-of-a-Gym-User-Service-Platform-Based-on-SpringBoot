<template>
  <UserLayout>
    <div class="cart-container">
      <h2 class="page-title">🛒 购物车</h2>

      <div v-if="cartItems.length === 0" class="empty-cart">
        <div class="empty-icon">🛒</div>
        <div class="empty-text">购物车是空的</div>
        <el-button type="primary" @click="goToProducts">去购物</el-button>
      </div>

      <div v-else class="cart-content">
        <div class="cart-list">
          <div v-for="item in cartItems" :key="item.id" class="cart-item">
            <div class="item-image">{{ item.icon }}</div>
            <div class="item-info">
              <div class="item-name">{{ item.name }}</div>
              <div class="item-price">¥{{ item.price }}</div>
            </div>
            <div class="item-quantity">
              <el-button size="small" @click="decreaseQty(item)">-</el-button>
              <span>{{ item.quantity }}</span>
              <el-button size="small" @click="increaseQty(item)">+</el-button>
            </div>
            <div class="item-total">¥{{ item.price * item.quantity }}</div>
            <el-button type="danger" size="small" @click="removeItem(item)">删除</el-button>
          </div>
        </div>

        <div class="cart-summary">
          <div class="summary-row">
            <span>商品数量：</span>
            <span>{{ totalItems }} 件</span>
          </div>
          <div class="summary-row">
            <span>合计：</span>
            <span class="total-price">¥{{ totalPrice }}</span>
          </div>
          <el-button type="primary" size="large" @click="checkout">去结算</el-button>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import UserLayout from '../components/UserLayout.vue'
import { ElButton } from 'element-plus'

const router = useRouter()
const cartItems = ref([
  { id: 1, name: '运动水壶', icon: '🏋️', price: 49, quantity: 2 },
  { id: 2, name: '瑜伽垫', icon: '🧘', price: 129, quantity: 1 },
  { id: 3, name: '健身手套', icon: '🧤', price: 39, quantity: 1 }
])

const totalItems = computed(() => cartItems.value.reduce((sum, item) => sum + item.quantity, 0))
const totalPrice = computed(() => cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0))

const increaseQty = (item) => { item.quantity++ }
const decreaseQty = (item) => { if (item.quantity > 1) item.quantity-- }
const removeItem = (item) => { cartItems.value = cartItems.value.filter(i => i.id !== item.id) }

const goToProducts = () => router.push('/products')

const checkout = async () => {
  try {
    const token = localStorage.getItem('token')
    await axios.post('/api/orders/checkout', cartItems.value, { headers: { Authorization: `Bearer ${token}` } })
    ElMessage.success('下单成功！')
    cartItems.value = []
  } catch (err) {
    ElMessage.error('下单失败')
  }
}
</script>

<style scoped>
.cart-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  text-align: center;
  margin-bottom: 30px;
}

.empty-cart {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 15px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-text {
  font-size: 18px;
  color: #666;
  margin-bottom: 20px;
}

.cart-content {
  background: white;
  border-radius: 15px;
  padding: 25px;
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 10px;
}

.item-image {
  font-size: 40px;
}

.item-info {
  flex: 1;
}

.item-name {
  font-weight: 600;
  margin-bottom: 5px;
}

.item-price {
  color: #667eea;
  font-weight: 600;
}

.item-quantity {
  display: flex;
  align-items: center;
  gap: 10px;
}

.item-quantity span {
  min-width: 30px;
  text-align: center;
}

.item-total {
  font-weight: 600;
  font-size: 18px;
  color: #667eea;
  min-width: 80px;
  text-align: right;
}

.cart-summary {
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.total-price {
  font-size: 24px;
  font-weight: 700;
  color: #667eea;
}

.cart-summary .el-button {
  width: 100%;
  margin-top: 15px;
}
</style>
