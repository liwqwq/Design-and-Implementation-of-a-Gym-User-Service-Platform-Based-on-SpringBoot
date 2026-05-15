<template>
  <div class="admin-dashboard admin-shell">
    <header class="admin-header">
      <div class="header-content">
        <div class="logo">
          <span class="logo-icon">🏋️</span>
          <span class="logo-text">FitLife {{ t('admin.login.title') }}</span>
        </div>
        <div class="user-info">
          <div class="language-switch">
            <button type="button" class="lang-btn" :class="{ active: currentLang === 'zh' }" @click="switchLang('zh')">
              {{ t('common.chinese') }}
            </button>
            <button type="button" class="lang-btn" :class="{ active: currentLang === 'en' }" @click="switchLang('en')">
              {{ t('common.english') }}
            </button>
          </div>
          <span class="username">{{ username }}</span>
          <button type="button" class="logout-btn" @click="logout">{{ t('common.logout') }}</button>
        </div>
      </div>
    </header>

    <div class="dashboard-container">
      <aside class="sidebar">
        <nav class="nav">
          <router-link to="/admin" end class="nav-item" :class="{ active: route.path === '/admin' }">
            <span class="nav-icon">👥</span>
            <span class="nav-text">{{ t('common.membership') }}</span>
          </router-link>
          <router-link to="/admin/users" class="nav-item" :class="{ active: route.path === '/admin/users' }">
            <span class="nav-icon">⚙️</span>
            <span class="nav-text">{{ t('common.users') }}</span>
          </router-link>
          <router-link to="/admin/classes" class="nav-item" :class="{ active: route.path === '/admin/classes' }">
            <span class="nav-icon">📅</span>
            <span class="nav-text">{{ t('common.classes') }}</span>
          </router-link>
          <router-link to="/admin/products" class="nav-item" :class="{ active: route.path === '/admin/products' }">
            <span class="nav-icon">🛍️</span>
            <span class="nav-text">{{ t('common.products') }}</span>
          </router-link>
          <router-link to="/admin/community" class="nav-item" :class="{ active: route.path === '/admin/community' }">
            <span class="nav-icon">💬</span>
            <span class="nav-text">{{ t('common.social') }}</span>
          </router-link>
        </nav>
      </aside>

      <main class="main-content">
        <div class="page-header">
          <div>
            <h2>{{ t('admin.products.title') }}</h2>
            <p class="page-subtitle">{{ t('admin.products.subtitle', { n: products.length }) }}</p>
          </div>
          <button type="button" class="add-btn" @click="openCreateModal">{{ t('admin.products.addProduct') }}</button>
        </div>

        <p v-if="loadError" class="load-error">{{ loadError }}</p>

        <div class="search-bar">
          <input
            v-model="searchKeyword"
            type="text"
            class="search-input"
            :placeholder="t('admin.products.searchPlaceholder')"
          />
          <select v-model="categoryFilter" class="category-select">
            <option value="">{{ t('admin.products.categoryAll') }}</option>
            <option v-for="cat in categoryOptions" :key="cat.value" :value="cat.value">{{ cat.label }}</option>
          </select>
          <button type="button" class="refresh-btn" :disabled="loading" @click="loadProducts">
            {{ loading ? t('common.loading') : t('admin.products.refresh') }}
          </button>
        </div>

        <div v-if="loading" class="empty-state">{{ t('common.loading') }}</div>
        <div v-else-if="filteredProducts.length === 0" class="empty-state">{{ t('admin.products.noProducts') }}</div>
        <div v-else class="products-grid">
          <div v-for="product in filteredProducts" :key="product.id" class="product-card" :class="{ inactive: product.isActive === false }">
            <div class="product-image">
              <span>{{ productIcon(product) }}</span>
            </div>
            <div class="product-info">
              <div class="product-name">{{ displayProductName(product) }}</div>
              <div class="product-desc">{{ displayProductDescription(product) || t('admin.products.noDescription') }}</div>
              <div class="product-meta">
                <span>{{ categoryLabel(product.category) }}</span>
                <span :class="['status-pill', product.isActive === false ? 'inactive' : 'active']">
                  {{ product.isActive === false ? t('admin.products.inactive') : t('admin.products.active') }}
                </span>
              </div>
              <div class="product-price">💎 {{ product.pointsCost }} {{ t('common.points') }}</div>
              <div class="product-sales">{{ t('admin.products.stockCount', { n: product.stockQuantity ?? 0 }) }}</div>
            </div>
            <div class="product-actions">
              <button type="button" class="action-btn edit" @click="openEditModal(product)">{{ t('admin.products.edit') }}</button>
              <button type="button" class="action-btn delete" @click="removeProduct(product)">{{ t('admin.products.delete') }}</button>
            </div>
          </div>
        </div>
      </main>
    </div>

    <div v-if="modalVisible" class="modal-backdrop" @click.self="closeModal">
      <div class="product-modal" @click.stop>
        <h3 class="modal-title">{{ isEditing ? t('admin.products.editProduct') : t('admin.products.addProduct') }}</h3>
        <div class="form-grid">
          <label class="form-row">
            <span>{{ t('admin.products.formName') }}</span>
            <input v-model="form.name" type="text" class="form-input" />
          </label>
          <label class="form-row">
            <span>{{ t('admin.products.formNameEn') }}</span>
            <input v-model="form.nameEn" type="text" class="form-input" :placeholder="t('admin.products.formNameEnPlaceholder')" />
          </label>
          <label class="form-row">
            <span>{{ t('admin.products.formCategory') }}</span>
            <select v-model="form.category" class="form-input">
              <option v-for="cat in categoryOptions" :key="cat.value" :value="cat.value">{{ cat.label }}</option>
            </select>
          </label>
          <label class="form-row">
            <span>{{ t('admin.products.formPointsCost') }}</span>
            <input v-model.number="form.pointsCost" type="number" min="0" step="1" class="form-input" />
          </label>
          <label class="form-row">
            <span>{{ t('admin.products.formStock') }}</span>
            <input v-model.number="form.stockQuantity" type="number" min="0" step="1" class="form-input" />
          </label>
          <label class="form-row full">
            <span>{{ t('admin.products.formDescription') }}</span>
            <textarea v-model="form.description" rows="3" class="form-input"></textarea>
          </label>
          <label class="form-row full">
            <span>{{ t('admin.products.formDescriptionEn') }}</span>
            <textarea v-model="form.descriptionEn" rows="3" class="form-input" :placeholder="t('admin.products.formDescriptionEnPlaceholder')"></textarea>
          </label>
          <label class="form-row full">
            <span>{{ t('admin.products.formIcon') }}</span>
            <input v-model="form.icon" type="text" class="form-input" :placeholder="t('admin.products.iconPlaceholder')" />
          </label>
          <label class="checkbox-row full">
            <input v-model="form.isActive" type="checkbox" />
            <span>{{ t('admin.products.formActive') }}</span>
          </label>
        </div>
        <div class="modal-actions">
          <button type="button" class="btn-secondary" @click="closeModal">{{ t('common.cancel') }}</button>
          <button type="button" class="btn-primary" :disabled="saving" @click="saveProduct">
            {{ saving ? t('common.saving') : t('common.save') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const { t, locale } = useI18n()
const currentLang = computed(() => locale.value)

const switchLang = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

const username = ref(localStorage.getItem('username') || 'Admin')
const searchKeyword = ref('')
const categoryFilter = ref('')
const products = ref([])
const loading = ref(false)
const saving = ref(false)
const loadError = ref('')
const modalVisible = ref(false)
const isEditing = ref(false)

const categoryValues = ['优惠券', '运动装备', '训练辅助', '营养补剂', '健身服饰', '运动配件', 'VIP专属', '会员服务', '会员特权', '其他']
const categoryKeyMap = {
  优惠券: 'categoryCoupon',
  运动装备: 'categoryEquipment',
  训练辅助: 'categoryTraining',
  营养补剂: 'categoryNutrition',
  健身服饰: 'categoryClothing',
  运动配件: 'categoryAccessories',
  VIP专属: 'categoryVip',
  会员服务: 'categoryService',
  会员特权: 'categoryPrivilege',
  其他: 'categoryOther',
}

const emptyForm = () => ({
  id: null,
  name: '',
  nameEn: '',
  description: '',
  descriptionEn: '',
  category: '运动装备',
  pointsCost: 100,
  stockQuantity: 100,
  icon: '🎁',
  isActive: true,
  isVip: false,
})

const form = ref(emptyForm())

const categoryOptions = computed(() => categoryValues.map((value) => ({
  value,
  label: categoryKeyMap[value] ? t(`admin.products.${categoryKeyMap[value]}`) : value,
})))

const filteredProducts = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase()
  return products.value.filter((p) => {
    const name = String(p.name || '').toLowerCase()
    const nameEn = String(p.nameEn || '').toLowerCase()
    const desc = String(p.description || '').toLowerCase()
    const descEn = String(p.descriptionEn || '').toLowerCase()
    const category = String(p.category || '').toLowerCase()
    const matchKeyword = !kw || name.includes(kw) || nameEn.includes(kw) || desc.includes(kw) || descEn.includes(kw) || category.includes(kw)
    const matchCategory = !categoryFilter.value || p.category === categoryFilter.value
    return matchKeyword && matchCategory
  })
})

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userType')
  localStorage.removeItem('user')
  localStorage.removeItem('coachId')
  router.push('/admin/login')
}


function displayProductName(product) {
  if (locale.value === 'en' && product.nameEn) return product.nameEn
  return product.name || product.nameEn || ''
}

function displayProductDescription(product) {
  if (locale.value === 'en' && product.descriptionEn) return product.descriptionEn
  return product.description || product.descriptionEn || ''
}

function defaultDiscountType(category) {
  if (category === '优惠券') return 'COUPON'
  if (category === '会员服务') return 'SERVICE'
  if (category === '会员特权') return 'PRIVILEGE'
  return 'PRODUCT'
}

function categoryLabel(category) {
  const key = categoryKeyMap[category]
  return key ? t(`admin.products.${key}`) : (category || t('admin.products.categoryOther'))
}

function formatPrice(price) {
  const n = Number(price)
  if (!Number.isFinite(n)) return '0.00'
  return n.toFixed(2).replace(/\.00$/, '')
}

function isImageUrl(url) {
  if (!url) return false
  return /^https?:\/\//i.test(url) || String(url).startsWith('data:image/')
}

function productIcon(product) {
  if (product.icon) return product.icon
  const text = `${product.name || ''}${product.nameEn || ''}${product.category || ''}`
  if (/水|杯|壶|bottle/i.test(text)) return '🥤'
  if (/瑜伽|yoga/i.test(text)) return '🧘'
  if (/蛋白|营养|protein/i.test(text)) return '💪'
  if (/T恤|衣|服|shirt|apparel/i.test(text)) return '👕'
  if (/跳绳|绳|rope/i.test(text)) return '🪢'
  if (/护膝|手套|带|轴/i.test(text)) return '🏋️'
  return '🛍️'
}

function normalizeProduct(raw) {
  return {
    id: raw.id,
    name: raw.name || '',
    nameEn: raw.nameEn || '',
    description: raw.description || '',
    descriptionEn: raw.descriptionEn || '',
    category: raw.category || '其他',
    pointsCost: raw.pointsCost ?? 0,
    stockQuantity: raw.stockQuantity ?? 0,
    soldCount: raw.soldCount ?? 0,
    icon: raw.icon || '',
    isActive: raw.isActive !== false,
    isVip: raw.isVip === true,
    discountType: raw.discountType || '',
    minSpend: raw.minSpend ?? null,
    discountAmount: raw.discountAmount ?? null,
    expireDays: raw.expireDays ?? null,
  }
}

async function loadProducts() {
  loading.value = true
  loadError.value = ''
  try {
    const { data } = await axios.get('/api/points/admin/products')
    if (data.success) {
      products.value = (data.data || []).map(normalizeProduct)
    } else {
      loadError.value = data.message || t('admin.products.loadFailed')
      ElMessage.error(loadError.value)
    }
  } catch (e) {
    loadError.value = e.response?.data?.message || t('admin.products.loadFailed')
    ElMessage.error(loadError.value)
  } finally {
    loading.value = false
  }
}

function openCreateModal() {
  isEditing.value = false
  form.value = emptyForm()
  modalVisible.value = true
}

function openEditModal(product) {
  isEditing.value = true
  form.value = { ...normalizeProduct(product) }
  if (!categoryValues.includes(form.value.category)) {
    categoryValues.push(form.value.category)
  }
  modalVisible.value = true
}

function closeModal() {
  modalVisible.value = false
}

function buildPayload() {
  const category = form.value.category || '其他'
  return {
    name: form.value.name.trim(),
    nameEn: form.value.nameEn.trim(),
    description: form.value.description.trim(),
    descriptionEn: form.value.descriptionEn.trim(),
    category,
    pointsCost: Math.max(0, Math.floor(Number(form.value.pointsCost) || 0)),
    stockQuantity: Math.max(0, Math.floor(Number(form.value.stockQuantity) || 0)),
    icon: form.value.icon.trim(),
    isActive: form.value.isActive !== false,
    isVip: category === 'VIP专属',
    discountType: defaultDiscountType(category),
  }
}

function validateForm(payload) {
  if (!payload.name) {
    ElMessage.warning(t('admin.products.nameRequired'))
    return false
  }
  if (payload.pointsCost < 0) {
    ElMessage.warning(t('admin.products.priceInvalid'))
    return false
  }
  return true
}

async function saveProduct() {
  const payload = buildPayload()
  if (!validateForm(payload)) return
  saving.value = true
  try {
    const request = isEditing.value && form.value.id
      ? axios.put(`/api/points/admin/products/${form.value.id}`, payload)
      : axios.post('/api/points/admin/products', payload)
    const { data } = await request
    if (data.success) {
      ElMessage.success(isEditing.value ? t('admin.products.updateSuccess') : t('admin.products.createSuccess'))
      closeModal()
      await loadProducts()
    } else {
      ElMessage.error(data.message || t('admin.products.saveFailed'))
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || t('admin.products.saveFailed'))
  } finally {
    saving.value = false
  }
}

async function removeProduct(product) {
  try {
    await ElMessageBox.confirm(
      t('admin.products.deleteConfirm', { name: displayProductName(product) }),
      t('common.confirm'),
      { type: 'warning', confirmButtonText: t('common.delete'), cancelButtonText: t('common.cancel') }
    )
  } catch {
    return
  }

  try {
    const { data } = await axios.delete(`/api/points/admin/products/${product.id}`)
    if (data.success) {
      ElMessage.success(t('admin.products.deleteSuccess'))
      await loadProducts()
    } else {
      ElMessage.error(data.message || t('admin.products.deleteFailed'))
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || t('admin.products.deleteFailed'))
  }
}

onMounted(loadProducts)
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.admin-header {
  background: linear-gradient(135deg, #1e3a5f, #2d5a87);
  color: white;
  padding: 16px 0;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  font-size: 32px;
}

.logo-text {
  font-size: 24px;
  font-weight: 700;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username {
  font-weight: 600;
}

.logout-btn {
  padding: 8px 20px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 20px;
  color: white;
  cursor: pointer;
}

.dashboard-container {
  display: flex;
  margin-top: 72px;
  min-height: calc(100vh - 72px);
}

.sidebar {
  width: 220px;
  background: white;
  padding: 20px 0;
  position: fixed;
  left: 0;
  top: 72px;
  bottom: 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 0 10px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  text-decoration: none;
  color: #333;
  border-radius: 10px;
  transition: all 0.3s;
}

.nav-item:hover,
.nav-item.active {
  background: linear-gradient(135deg, #1e3a5f, #2d5a87);
  color: white;
}

.nav-icon {
  font-size: 18px;
}

.main-content {
  flex: 1;
  margin-left: 220px;
  padding: 30px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
}

.page-subtitle {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.add-btn,
.refresh-btn,
.btn-primary {
  padding: 10px 24px;
  background: linear-gradient(135deg, #1e3a5f, #2d5a87);
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
}

.refresh-btn:disabled,
.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 25px;
}

.search-input {
  flex: 1;
  max-width: 320px;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 10px;
  font-size: 14px;
}

.category-select {
  padding: 12px 20px;
  border: 1px solid #ddd;
  border-radius: 10px;
  font-size: 14px;
}

.load-error {
  color: #dc3545;
  background: #fff1f2;
  border: 1px solid #fecdd3;
  border-radius: 10px;
  padding: 10px 14px;
}

.empty-state {
  padding: 50px 20px;
  text-align: center;
  color: #6b7280;
  background: white;
  border-radius: 15px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.product-card {
  background: white;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
}

.product-card.inactive {
  opacity: 0.65;
}

.product-image {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 50px;
  text-align: center;
  margin-bottom: 15px;
}

.product-image img {
  max-width: 72px;
  max-height: 72px;
  border-radius: 12px;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-name {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 8px;
}

.product-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  min-height: 40px;
}

.product-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 10px;
  color: #6b7280;
  font-size: 12px;
}

.status-pill {
  padding: 3px 8px;
  border-radius: 999px;
  font-size: 12px;
}

.status-pill.active {
  color: #047857;
  background: #d1fae5;
}

.status-pill.inactive {
  color: #991b1b;
  background: #fee2e2;
}

.product-price {
  font-size: 20px;
  font-weight: 700;
  color: #1e3a5f;
  margin-bottom: 5px;
}

.product-sales {
  font-size: 12px;
  color: #999;
  margin-bottom: 15px;
}

.product-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  flex: 1;
  padding: 8px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
}

.action-btn.edit {
  background: #1e3a5f;
  color: white;
}

.action-btn.delete {
  background: #dc3545;
  color: white;
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
}

.product-modal {
  width: min(680px, 100%);
  background: white;
  border-radius: 18px;
  box-shadow: 0 20px 45px rgba(0, 0, 0, 0.2);
  padding: 24px;
}

.modal-title {
  margin: 0 0 18px;
  font-size: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.form-row.full,
.checkbox-row.full {
  grid-column: 1 / -1;
}

.form-input {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 14px;
  outline: none;
}

.form-input:focus {
  border-color: #2d5a87;
  box-shadow: 0 0 0 3px rgba(45, 90, 135, 0.12);
}

.checkbox-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 22px;
}

.btn-secondary {
  padding: 10px 22px;
  border-radius: 10px;
  border: none;
  background: #e5e7eb;
  color: #374151;
  cursor: pointer;
}
</style>
