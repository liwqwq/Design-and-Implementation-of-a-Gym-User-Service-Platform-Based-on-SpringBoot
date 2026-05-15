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
          <h2>{{ t('admin.classes.title') }}</h2>
          <button type="button" class="add-btn" @click="openCreateModal">{{ t('admin.classes.addClass') }}</button>
        </div>

        <p v-if="loadError" class="load-error">{{ loadError }}</p>

        <div class="tabs-container">
          <div class="tabs">
            <button type="button" class="tab" :class="{ active: activeTab === 'private' }" @click="activeTab = 'private'">
              {{ t('admin.classes.tabPrivate') }}
            </button>
            <button type="button" class="tab" :class="{ active: activeTab === 'group' }" @click="activeTab = 'group'">
              {{ t('admin.classes.tabGroup') }}
            </button>
          </div>

          <div class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>{{ t('admin.classes.id') }}</th>
                  <th>{{ t('admin.classes.courseName') }}</th>
                  <th>{{ t('admin.classes.coach') }}</th>
                  <th>{{ t('admin.classes.time') }}</th>
                  <th>{{ t('admin.classes.capacity') }}</th>
                  <th>{{ t('admin.classes.location') }}</th>
                  <th>{{ t('admin.classes.status') }}</th>
                  <th>{{ t('admin.classes.actions') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in filteredCourses" :key="row.id">
                  <td>{{ row.id }}</td>
                  <td>{{ displayCourseName(row.name) }}</td>
                  <td>{{ displayCoach(row.instructor) }}</td>
                  <td>{{ formatTimeRange(row) }}</td>
                  <td>{{ row.bookedCount ?? 0 }} / {{ row.capacity ?? 0 }}</td>
                  <td>{{ displayPlace(row.location) }}</td>
                  <td>
                    <span class="status-pill" :class="statusClass(row)">{{ displayStatus(row) }}</span>
                  </td>
                  <td>
                    <button type="button" class="action-btn edit" @click="openEditModal(row)">{{ t('admin.classes.edit') }}</button>
                    <button type="button" class="action-btn delete" @click="removeClass(row)">{{ t('admin.classes.delete') }}</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>

    <div v-if="showModal" class="modal-backdrop" @click.self="closeModal">
      <div class="modal">
        <h3>{{ form.id ? t('admin.classes.editClass') : t('admin.classes.addClass') }}</h3>
        <div class="form-row">
          <label>{{ t('admin.classes.formName') }}</label>
          <input v-model="form.name" type="text" class="input" />
        </div>
        <div class="form-row">
          <label>{{ t('admin.classes.formCoach') }}</label>
          <select v-model="form.coachId" class="input">
            <option value="">{{ t('admin.classes.selectCoach') }}</option>
            <option v-for="c in coaches" :key="c.id" :value="String(c.id)">{{ c.name }} ({{ c.username }})</option>
          </select>
        </div>
        <div class="form-row">
          <label>{{ t('admin.classes.formCapacity') }}</label>
          <input v-model.number="form.capacity" type="number" min="1" class="input" />
        </div>
        <div class="form-row">
          <label>{{ t('admin.classes.formStart') }}</label>
          <input v-model="form.startTime" type="datetime-local" class="input" />
        </div>
        <div class="form-row">
          <label>{{ t('admin.classes.formEnd') }}</label>
          <input v-model="form.endTime" type="datetime-local" class="input" />
        </div>
        <div class="form-row">
          <label>{{ t('admin.classes.formLocation') }}</label>
          <input v-model="form.location" type="text" class="input" />
        </div>
        <div class="form-row">
          <label>{{ t('admin.classes.formStatus') }}</label>
          <select v-model="form.status" class="input">
            <option value="ACTIVE">{{ t('admin.classes.active') }}</option>
            <option value="PENDING">{{ t('admin.classes.pending') }}</option>
            <option value="INACTIVE">{{ t('admin.classes.inactive') }}</option>
          </select>
        </div>
        <div class="modal-actions">
          <button type="button" class="btn-secondary" @click="closeModal">{{ t('admin.classes.cancel') }}</button>
          <button type="button" class="add-btn" :disabled="saving" @click="submitSave">{{ t('admin.classes.save') }}</button>
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
import { ElMessage } from 'element-plus'
import { formatCourseName } from '../../utils/courseName'

const router = useRouter()
const route = useRoute()
const { t, te, locale } = useI18n()
const currentLang = computed(() => locale.value)

const switchLang = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

const username = ref(localStorage.getItem('username') || 'Admin')
const activeTab = ref('group')
const courses = ref([])
const coaches = ref([])
const loadError = ref('')
const showModal = ref(false)
const saving = ref(false)

const form = ref({
  id: null,
  name: '',
  coachId: '',
  capacity: 20,
  startTime: '',
  endTime: '',
  location: 'A区教室',
  status: 'ACTIVE',
})

const privateCourses = computed(() => (courses.value || []).filter((c) => (c.capacity || 0) <= 10))
const groupCourses = computed(() => (courses.value || []).filter((c) => (c.capacity || 0) > 10))

const filteredCourses = computed(() => (activeTab.value === 'private' ? privateCourses.value : groupCourses.value))

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userType')
  localStorage.removeItem('user')
  localStorage.removeItem('coachId')
  router.push('/admin/login')
}

function pad(n) {
  return String(n).padStart(2, '0')
}

function defaultDatetimeLocal(d) {
  const y = d.getFullYear()
  const m = pad(d.getMonth() + 1)
  const day = pad(d.getDate())
  const h = pad(d.getHours())
  const min = pad(d.getMinutes())
  return `${y}-${m}-${day}T${h}:${min}`
}

function toLocalIso(s) {
  if (!s) return ''
  if (s.length === 16) return s + ':00'
  return s
}

function formatTimeRange(row) {
  if (!row.startTime) return '—'
  try {
    const a = new Date(row.startTime)
    const b = row.endTime ? new Date(row.endTime) : null
    const opt = { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }
    if (b && !Number.isNaN(b.getTime())) {
      return `${a.toLocaleString(undefined, opt)} – ${b.toLocaleString(undefined, opt)}`
    }
    return a.toLocaleString(undefined, opt)
  } catch {
    return String(row.startTime)
  }
}

function isFull(row) {
  const cap = row.capacity || 0
  const booked = row.bookedCount || 0
  return cap > 0 && booked >= cap
}

function statusClass(row) {
  const status = String(row.status || 'ACTIVE').toUpperCase()
  return {
    full: status === 'ACTIVE' && isFull(row),
    inactive: status === 'INACTIVE',
    pending: status === 'PENDING'
  }
}

function displayStatus(row) {
  const status = String(row.status || 'ACTIVE').toUpperCase()
  if (status === 'INACTIVE') return t('admin.classes.inactive')
  if (status === 'PENDING') return t('admin.classes.pending')
  if (isFull(row)) return t('admin.classes.full')
  return t('admin.classes.active') || t('admin.classes.stateActive')
}

function displayCourseName(name) {
  void locale.value
  return formatCourseName(name, t, te)
}

function displayPlace(loc) {
  void locale.value
  if (!loc) return '—'
  const key = 'admin.classes.places.' + loc
  return te(key) ? t(key) : loc
}

function displayCoach(name) {
  void locale.value
  if (!name) return '—'
  const key = 'admin.classes.coachNames.' + name
  return te(key) ? t(key) : name
}

async function loadCoaches() {
  try {
    const { data } = await axios.get('/api/admin/coaches')
    if (data.success) coaches.value = data.data || []
  } catch (e) {
    const status = e.response?.status
    if (status === 401 || status === 403) {
      loadError.value = t('admin.dashboard.unauthorized')
    }
  }
}

async function loadClasses() {
  loadError.value = ''
  try {
    const { data } = await axios.get('/api/admin/classes')
    if (data.success) {
      courses.value = data.data || []
    } else {
      loadError.value = data.message || t('admin.classes.loadFailed')
    }
  } catch (e) {
    const status = e.response?.status
    if (status === 401 || status === 403) {
      loadError.value = t('admin.dashboard.unauthorized')
    } else {
      loadError.value = t('admin.classes.loadFailed')
    }
  }
}

function openCreateModal() {
  const now = new Date()
  const end = new Date(now.getTime() + 60 * 60 * 1000)
  form.value = {
    id: null,
    name: '',
    coachId: coaches.value[0] ? String(coaches.value[0].id) : '',
    capacity: activeTab.value === 'private' ? 4 : 25,
    startTime: defaultDatetimeLocal(now),
    endTime: defaultDatetimeLocal(end),
    location: 'A区教室',
    status: 'ACTIVE',
  }
  showModal.value = true
}

function toDatetimeLocal(value) {
  if (!value) return ''
  const text = String(value)
  if (text.length >= 16) return text.substring(0, 16)
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return ''
  return defaultDatetimeLocal(d)
}

function openEditModal(row) {
  form.value = {
    id: row.id,
    name: row.name || '',
    coachId: row.coachId ? String(row.coachId) : '',
    capacity: Number(row.capacity || 20),
    startTime: toDatetimeLocal(row.startTime),
    endTime: toDatetimeLocal(row.endTime),
    location: row.location || '',
    status: String(row.status || 'ACTIVE').toUpperCase(),
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

async function submitSave() {
  if (!form.value.name.trim()) return
  saving.value = true
  try {
    const payload = {
      name: form.value.name.trim(),
      capacity: Number(form.value.capacity) || 20,
      startTime: toLocalIso(form.value.startTime),
      endTime: toLocalIso(form.value.endTime),
      location: form.value.location || '',
      status: form.value.status || 'ACTIVE',
      coachId: form.value.coachId ? Number(form.value.coachId) : null,
    }
    const request = form.value.id
      ? axios.put(`/api/admin/classes/${form.value.id}`, payload)
      : axios.post('/api/admin/classes', payload)
    const { data } = await request
    if (data.success) {
      ElMessage.success(form.value.id ? t('admin.classes.updateSuccess') : t('admin.classes.createSuccess'))
      closeModal()
      await loadClasses()
    } else {
      ElMessage.error(data.message || t('admin.classes.loadFailed'))
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || t('admin.classes.loadFailed'))
  } finally {
    saving.value = false
  }
}

async function removeClass(row) {
  if (!confirm(t('admin.classes.deleteConfirm'))) return
  try {
    const { data } = await axios.delete(`/api/admin/classes/${row.id}`)
    if (data.success) {
      ElMessage.success(t('admin.classes.deleteSuccess'))
      await loadClasses()
    } else {
      ElMessage.error(data.message || t('admin.classes.loadFailed'))
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || t('admin.classes.loadFailed'))
  }
}

onMounted(async () => {
  await loadCoaches()
  await loadClasses()
})
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

.add-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #1e3a5f, #2d5a87);
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
}

.add-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.load-error {
  color: #c0392b;
  margin-bottom: 12px;
}

.tabs-container {
  background: white;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px 14px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.data-table th {
  background: #e9ecef;
  color: #212529;
  font-weight: 600;
}

.action-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
}

.action-btn + .action-btn {
  margin-left: 8px;
}

.action-btn.edit {
  background: #2563eb;
  color: white;
}

.action-btn.delete {
  background: #dc3545;
  color: white;
}

.status-pill {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  background: #d4edda;
  color: #155724;
}

.status-pill.full {
  background: #f8d7da;
  color: #721c24;
}

.status-pill.inactive {
  background: #e5e7eb;
  color: #374151;
}

.status-pill.pending {
  background: #fef3c7;
  color: #92400e;
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal {
  background: white;
  border-radius: 16px;
  padding: 24px 28px;
  width: 100%;
  max-width: 440px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}

.modal h3 {
  margin: 0 0 16px;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 14px;
}

.form-row label {
  font-size: 13px;
  font-weight: 600;
  color: #444;
}

.input {
  padding: 10px 12px;
  border: 1px solid #ced4da;
  border-radius: 8px;
  font-size: 14px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
}

.btn-secondary {
  padding: 10px 18px;
  border-radius: 10px;
  border: 1px solid #ced4da;
  background: white;
  cursor: pointer;
}
</style>
