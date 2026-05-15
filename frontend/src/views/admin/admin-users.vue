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
          <h2>{{ t('admin.users.title') }}</h2>
          <button type="button" class="add-btn" @click="goRegister">{{ t('admin.users.addUser') }}</button>
        </div>

        <p v-if="loadError" class="load-error">{{ loadError }}</p>

        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>{{ t('common.id') }}</th>
                <th>{{ t('common.username') }}</th>
                <th>{{ t('common.name') }}</th>
                <th>{{ t('common.phone') }}</th>
                <th>{{ t('common.email') }}</th>
                <th>{{ t('admin.users.roleLabel') }}</th>
                <th>{{ t('admin.users.status') }}</th>
                <th>{{ t('common.actions') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.id">
                <td>{{ user.id }}</td>
                <td>{{ user.username }}</td>
                <td>{{ user.name }}</td>
                <td>{{ user.phone }}</td>
                <td>{{ user.email }}</td>
                <td>
                  <span class="badge" :class="user.role">{{ roleLabel(user.role) }}</span>
                </td>
                <td>
                  <span class="status" :class="user.statusKey">{{ t('memberStatus.' + user.statusKey) }}</span>
                </td>
                <td>
                  <button type="button" class="action-btn edit" @click="openEdit(user)">{{ t('common.edit') }}</button>
                  <button type="button" class="action-btn delete" @click="removeUser(user)">{{ t('common.delete') }}</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </main>
    </div>

    <div v-if="addModalVisible" class="modal-backdrop" @click.self="closeAddModal">
      <div class="user-modal" @click.stop>
        <h3 class="modal-title">{{ t('admin.users.addUser') }}</h3>
        <div class="form-row">
          <label>{{ t('common.username') }}</label>
          <input v-model="addForm.username" type="text" class="form-input" autocomplete="username" />
        </div>
        <div class="form-row">
          <label>{{ t('common.password') }}</label>
          <input v-model="addForm.password" type="password" class="form-input" autocomplete="new-password" />
        </div>
        <div class="form-row">
          <label>{{ t('common.name') }}</label>
          <input v-model="addForm.name" type="text" class="form-input" autocomplete="name" />
        </div>
        <div class="form-row">
          <label>{{ t('common.email') }}</label>
          <input v-model="addForm.email" type="email" class="form-input" autocomplete="email" />
        </div>
        <div class="form-row">
          <label>{{ t('common.phone') }}</label>
          <input v-model="addForm.phone" type="text" class="form-input" autocomplete="tel" />
        </div>
        <div class="form-row">
          <label>{{ t('admin.dashboard.level') }}</label>
          <select v-model="addForm.membershipType" class="form-input">
            <option value="标准会员">{{ t('memberLevels.normal') }}</option>
            <option value="VIP年卡">{{ t('memberLevels.vip') }}</option>
          </select>
        </div>
        <label class="checkbox-row">
          <input v-model="addForm.active" type="checkbox" />
          <span>{{ t('admin.users.accountActive') }}</span>
        </label>
        <div class="modal-actions">
          <button type="button" class="btn-secondary" @click="closeAddModal">{{ t('common.cancel') }}</button>
          <button type="button" class="btn-primary" :disabled="saving" @click="saveNewUser">{{ t('common.save') }}</button>
        </div>
      </div>
    </div>

    <div v-if="editModalVisible" class="modal-backdrop" @click.self="closeEditModal">
      <div class="user-modal" @click.stop>
        <h3 class="modal-title">{{ t('admin.users.editUser') }}</h3>
        <p class="modal-username">{{ editForm.username }}</p>
        <div class="form-row">
          <label>{{ t('common.name') }}</label>
          <input v-model="editForm.name" type="text" class="form-input" autocomplete="name" />
        </div>
        <div class="form-row">
          <label>{{ t('common.email') }}</label>
          <input v-model="editForm.email" type="email" class="form-input" autocomplete="email" />
        </div>
        <div class="form-row">
          <label>{{ t('common.phone') }}</label>
          <input v-model="editForm.phone" type="text" class="form-input" autocomplete="tel" />
        </div>
        <div class="form-row">
          <label>{{ t('admin.users.role') }}</label>
          <select v-model="editForm.role" class="form-input">
            <option value="USER">{{ t('admin.users.roleMember') }}</option>
            <option value="ADMIN">{{ t('admin.users.roleAdminOption') }}</option>
          </select>
        </div>
        <div class="form-row">
          <label>{{ t('admin.users.optionalNewPassword') }}</label>
          <input v-model="editForm.newPassword" type="password" class="form-input" autocomplete="new-password" />
        </div>
        <label class="checkbox-row">
          <input v-model="editForm.active" type="checkbox" />
          <span>{{ t('admin.users.accountActive') }}</span>
        </label>
        <div class="modal-actions">
          <button type="button" class="btn-secondary" @click="closeEditModal">{{ t('common.cancel') }}</button>
          <button type="button" class="btn-primary" :disabled="saving" @click="saveUser">{{ t('common.save') }}</button>
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

const username = ref(localStorage.getItem('username') || '管理员')
const users = ref([])
const loadError = ref('')
const editModalVisible = ref(false)
const addModalVisible = ref(false)
const saving = ref(false)
const addForm = ref({
  username: '',
  password: '123456',
  name: '',
  email: '',
  phone: '',
  membershipType: '标准会员',
  active: true,
})
const editForm = ref({
  id: null,
  username: '',
  name: '',
  email: '',
  phone: '',
  role: 'USER',
  active: true,
  newPassword: '',
})

const roleLabel = (role) => {
  if (role === 'ADMIN') return t('common.admin')
  return t('admin.users.roleMember')
}

const loadUsers = async () => {
  loadError.value = ''
  try {
    const { data } = await axios.get('/api/admin/users')
    if (data.success) {
      users.value = data.data || []
    } else {
      loadError.value = data.message || t('admin.users.loadFailed')
    }
  } catch (e) {
    const status = e.response?.status
    if (status === 401 || status === 403) {
      loadError.value = t('admin.dashboard.unauthorized')
    } else {
      loadError.value = t('admin.users.loadFailed')
    }
  }
}

const goRegister = () => {
  addForm.value = {
    username: '',
    password: '123456',
    name: '',
    email: '',
    phone: '',
    membershipType: '标准会员',
    active: true,
  }
  addModalVisible.value = true
}

const closeAddModal = () => {
  addModalVisible.value = false
}

const saveNewUser = async () => {
  saving.value = true
  try {
    const { data } = await axios.post('/api/admin/users', addForm.value)
    if (data.success) {
      ElMessage.success(t('admin.users.userAdded'))
      closeAddModal()
      await loadUsers()
    } else {
      ElMessage.error(data.message || t('admin.users.saveFailed'))
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || t('admin.users.saveFailed'))
  } finally {
    saving.value = false
  }
}

const openEdit = (row) => {
  editForm.value = {
    id: row.id,
    username: row.username || '',
    name: row.name || '',
    email: row.email || '',
    phone: row.phone || '',
    role: row.role === 'ADMIN' ? 'ADMIN' : 'USER',
    active: row.active !== false,
    newPassword: '',
  }
  editModalVisible.value = true
}

const closeEditModal = () => {
  editModalVisible.value = false
}

const saveUser = async () => {
  if (!editForm.value.id) return
  saving.value = true
  try {
    const body = {
      name: editForm.value.name,
      email: editForm.value.email,
      phone: editForm.value.phone,
      active: editForm.value.active,
      role: editForm.value.role,
    }
    const pwd = editForm.value.newPassword?.trim()
    if (pwd) body.password = pwd

    const { data } = await axios.put(`/api/admin/users/${editForm.value.id}`, body)
    if (data.success) {
      ElMessage.success(t('admin.users.userUpdated'))
      closeEditModal()
      await loadUsers()
    } else {
      ElMessage.error(data.message || t('admin.users.saveFailed'))
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || t('admin.users.saveFailed'))
  } finally {
    saving.value = false
  }
}

const removeUser = async (row) => {
  try {
    await ElMessageBox.confirm(t('admin.users.confirmDeleteFull'), t('common.confirm'), {
      type: 'warning',
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
    })
  } catch {
    return
  }
  try {
    const { data } = await axios.delete(`/api/admin/users/${row.id}`)
    if (data.success) {
      ElMessage.success(t('admin.users.userDeleted'))
      await loadUsers()
    } else {
      ElMessage.error(data.message || t('admin.users.deleteFailed'))
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || t('admin.users.deleteFailed'))
  }
}

onMounted(loadUsers)

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userType')
  localStorage.removeItem('user')
  localStorage.removeItem('coachId')
  router.push('/admin/login')
}
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

.nav-item:hover, .nav-item.active {
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
  margin-bottom: 30px;
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

.table-container {
  background: white;
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th, .data-table td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.data-table th {
  background: #e9ecef;
  color: #212529;
  font-weight: 600;
}

.load-error {
  color: #c0392b;
  margin-bottom: 16px;
}

.badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.badge.ADMIN {
  background: #fff3cd;
  color: #856404;
}

.badge.USER {
  background: #d4edda;
  color: #155724;
}

.status.active {
  color: #28a745;
}

.status.inactive {
  color: #6c757d;
}

.status {
  font-size: 12px;
  font-weight: 600;
}

.action-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  margin-right: 8px;
  font-size: 13px;
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
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
}

.user-modal {
  background: white;
  border-radius: 14px;
  padding: 24px 28px;
  max-width: 440px;
  width: 100%;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
}

.modal-title {
  margin: 0 0 6px;
  font-size: 18px;
  color: #1e3a5f;
}

.modal-username {
  margin: 0 0 18px;
  font-size: 13px;
  color: #6c757d;
}

.form-row {
  margin-bottom: 14px;
}

.form-row label {
  display: block;
  font-size: 12px;
  color: #555;
  margin-bottom: 6px;
}

.form-input {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 12px;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  font-size: 14px;
}

.checkbox-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 16px 0 20px;
  font-size: 14px;
  cursor: pointer;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-secondary {
  padding: 8px 18px;
  border: 1px solid #ced4da;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  font-size: 14px;
}

.btn-primary {
  padding: 8px 18px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #1e3a5f, #2d5a87);
  color: white;
  cursor: pointer;
  font-size: 14px;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>