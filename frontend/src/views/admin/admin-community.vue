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
          <h2>{{ t('admin.community.title') }}</h2>
          <div class="tabs">
            <button type="button" class="tab" :class="{ active: activeTab === 'posts' }" @click="activeTab = 'posts'">
              {{ t('admin.community.tabPosts') }}
            </button>
            <button type="button" class="tab" :class="{ active: activeTab === 'reports' }" @click="activeTab = 'reports'">
              {{ t('admin.community.tabReports') }}
            </button>
          </div>
        </div>

        <div v-if="activeTab === 'posts'" class="posts-list">
          <p v-if="postsLoadError" class="load-error">{{ postsLoadError }}</p>
          <p v-if="!postsLoadError && !postsLoading && postsList.length === 0" class="empty-hint">
            {{ t('admin.community.noPosts') }}
          </p>
          <div v-for="post in postsList" :key="post.id" class="post-card">
            <div class="post-header">
              <div class="post-author">
                <span class="author-avatar">💬</span>
                <div class="author-info">
                  <div class="author-name">{{ post.username || t('common.guestName') }}</div>
                  <div class="post-meta">
                    <span class="post-time">{{ formatDateTime(post.createdAt) }}</span>
                    <span v-if="post.active === false" class="badge-hidden">{{ t('admin.community.hiddenBadge') }}</span>
                  </div>
                </div>
              </div>
              <span class="category">{{ postCategoryLabel(post.category) }}</span>
            </div>
            <div class="post-content">{{ post.content }}</div>
            <div class="post-stats">👍 {{ post.likes }} 💬 {{ post.comments }}</div>
            <div class="post-actions">
              <button type="button" class="action-btn edit" @click="openEditPost(post)">{{ t('common.edit') }}</button>
              <button
                type="button"
                class="action-btn delete"
                :disabled="postActingId === post.id"
                @click="removePost(post)"
              >
                {{ t('common.delete') }}
              </button>
            </div>
          </div>
        </div>

        <div v-else class="reports-list">
          <p v-if="reportsLoadError" class="load-error">{{ reportsLoadError }}</p>
          <p v-if="!reportsLoadError && !reportsLoading && reportsList.length === 0" class="empty-hint">
            {{ t('admin.community.noReports') }}
          </p>
          <div v-for="report in reportsList" :key="report.id" class="report-card">
            <div class="report-header">
              <div class="report-info">
                <div class="report-title">{{ t('admin.community.reportNumber', { n: report.id }) }}</div>
                <div class="report-time">{{ formatDateTime(report.createdAt) }}</div>
              </div>
              <span class="report-status" :class="reportStatusClass(report.status)">{{ reportStatusLabel(report.status) }}</span>
            </div>
            <div class="report-content">
              <div class="report-item">
                <label>{{ t('admin.community.reporterLabel') }}</label>
                <span>{{ report.reporter }}</span>
              </div>
              <div class="report-item">
                <label>{{ t('admin.community.postLabel') }}</label>
                <span>{{ report.postContent }}</span>
              </div>
              <div class="report-item">
                <label>{{ t('admin.community.reasonLabel') }}</label>
                <span class="reason">{{ displayReason(report) }}</span>
              </div>
            </div>
            <div class="report-actions">
              <button
                type="button"
                class="action-btn accept"
                :disabled="!isReportPending(report) || actingId === report.id"
                @click="processReport(report)"
              >
                {{ t('admin.community.process') }}
              </button>
              <button
                type="button"
                class="action-btn reject"
                :disabled="!isReportPending(report) || actingId === report.id"
                @click="ignoreReport(report)"
              >
                {{ t('admin.community.dismiss') }}
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>

    <div v-if="postEditVisible" class="modal-backdrop" @click.self="closePostEditModal">
      <div class="post-edit-modal" @click.stop>
        <h3 class="modal-title">{{ t('admin.community.editPostTitle') }}</h3>
        <p class="modal-sub">ID: {{ postEditForm.id }}</p>
        <div class="form-row">
          <label>{{ t('admin.community.categoryLabel') }}</label>
          <select v-model="postEditForm.category" class="form-input">
            <option v-for="c in postCategoryValues" :key="c" :value="c">{{ postCategoryLabel(c) }}</option>
          </select>
        </div>
        <div class="form-row">
          <label>{{ t('admin.community.postContentLabel') }}</label>
          <textarea v-model="postEditForm.content" class="form-textarea" rows="5"></textarea>
        </div>
        <label class="checkbox-row">
          <input v-model="postEditForm.active" type="checkbox" />
          <span>{{ t('admin.community.postActiveLabel') }}</span>
        </label>
        <div class="modal-actions">
          <button type="button" class="btn-secondary" @click="closePostEditModal">{{ t('common.cancel') }}</button>
          <button type="button" class="btn-primary" :disabled="postEditSaving" @click="savePost">{{ t('common.save') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const { t, te, locale } = useI18n()
const currentLang = computed(() => locale.value)

const switchLang = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

const username = ref(localStorage.getItem('username') || 'Admin')
const activeTab = ref('posts')

const postsList = ref([])
const postsLoading = ref(false)
const postsLoadError = ref('')
const postEditVisible = ref(false)
const postEditSaving = ref(false)
const postActingId = ref(null)
const postEditForm = ref({
  id: null,
  content: '',
  category: 'share',
  active: true,
})
const postCategoryValues = ['share', 'help', 'team', 'chat']

const reportsList = ref([])
const reportsLoading = ref(false)
const reportsLoadError = ref('')
const actingId = ref(null)

const postCategoryLabel = (cat) => {
  const key = 'user.social.categories.' + (cat || 'share')
  if (te(key)) return t(key)
  return cat || '—'
}

const formatDateTime = (iso) => {
  if (!iso) return '—'
  try {
    const d = new Date(iso)
    return d.toLocaleString(currentLang.value === 'zh' ? 'zh-CN' : 'en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch {
    return String(iso)
  }
}

const displayReason = (row) => {
  const type = row.type ? String(row.type) : ''
  const reason = row.reason ? String(row.reason) : ''
  if (type && reason) return `${type} · ${reason}`
  return reason || type || '—'
}

const isReportPending = (row) => String(row.status || '').toUpperCase() === 'PENDING'

const reportStatusClass = (status) => {
  const s = String(status || '').toUpperCase()
  if (s === 'PENDING') return 'pending'
  if (s === 'IGNORED') return 'ignored'
  return 'resolved'
}

const reportStatusLabel = (status) => {
  const s = String(status || '').toUpperCase()
  if (s === 'PENDING') return t('admin.community.statusPending')
  if (s === 'IGNORED') return t('admin.community.statusIgnored')
  return t('admin.community.statusResolved')
}

const loadPosts = async () => {
  postsLoadError.value = ''
  postsLoading.value = true
  try {
    const res = await axios.get('/api/admin/posts', { validateStatus: () => true })
    const status = res.status
    if (status === 401 || status === 403) {
      postsLoadError.value = t('admin.dashboard.unauthorized')
      return
    }
    if (status === 404) {
      postsLoadError.value = t('admin.community.postsApiNotFound')
      return
    }
    const data = res.data
    if (data && data.success) {
      postsList.value = data.data || []
    } else {
      postsLoadError.value = (data && (data.message || data.error)) || t('admin.community.loadPostsFailed')
    }
  } catch (e) {
    const status = e.response?.status
    if (status === 401 || status === 403) {
      postsLoadError.value = t('admin.dashboard.unauthorized')
    } else if (status === 404) {
      postsLoadError.value = t('admin.community.postsApiNotFound')
    } else {
      const body = e.response?.data
      const msg =
        (typeof body === 'object' && body != null && (body.message || body.error)) ||
        (typeof body === 'string' ? body : null)
      postsLoadError.value = msg || e.message || t('admin.community.loadPostsFailed')
    }
  } finally {
    postsLoading.value = false
  }
}

const openEditPost = (row) => {
  postEditForm.value = {
    id: row.id,
    content: row.content || '',
    category: postCategoryValues.includes(row.category) ? row.category : 'share',
    active: row.active !== false,
  }
  postEditVisible.value = true
}

const closePostEditModal = () => {
  postEditVisible.value = false
}

const savePost = async () => {
  if (!postEditForm.value.id) return
  const content = postEditForm.value.content?.trim()
  if (!content) {
    ElMessage.warning(t('admin.community.postContentRequired'))
    return
  }
  postEditSaving.value = true
  try {
    const { data } = await axios.put(`/api/admin/posts/${postEditForm.value.id}`, {
      content,
      category: postEditForm.value.category,
      active: postEditForm.value.active,
    })
    if (data.success) {
      ElMessage.success(t('admin.community.postUpdated'))
      closePostEditModal()
      await loadPosts()
    } else {
      ElMessage.error(data.message || t('admin.community.loadPostsFailed'))
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || t('admin.community.loadPostsFailed'))
  } finally {
    postEditSaving.value = false
  }
}

const removePost = async (row) => {
  try {
    await ElMessageBox.confirm(t('admin.community.confirmDeletePost'), t('common.confirm'), {
      type: 'warning',
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
    })
  } catch {
    return
  }
  postActingId.value = row.id
  try {
    const { data } = await axios.delete(`/api/admin/posts/${row.id}`)
    if (data.success) {
      ElMessage.success(t('admin.community.postDeleted'))
      await loadPosts()
    } else {
      ElMessage.error(data.message || t('admin.community.loadPostsFailed'))
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || t('admin.community.loadPostsFailed'))
  } finally {
    postActingId.value = null
  }
}

const loadReports = async () => {
  reportsLoadError.value = ''
  reportsLoading.value = true
  try {
    const { data } = await axios.get('/api/admin/reports')
    if (data.success) {
      reportsList.value = data.data || []
    } else {
      reportsLoadError.value = data.message || t('admin.community.loadReportsFailed')
    }
  } catch (e) {
    reportsLoadError.value = e.response?.data?.message || t('admin.community.loadReportsFailed')
  } finally {
    reportsLoading.value = false
  }
}

const processReport = async (row) => {
  actingId.value = row.id
  try {
    const { data } = await axios.post(`/api/admin/reports/${row.id}/process`)
    if (data.success) {
      ElMessage.success(t('admin.community.processSuccess'))
      await loadReports()
    } else {
      ElMessage.error(data.message === 'NOT_PENDING' ? t('admin.community.alreadyHandled') : data.message || t('admin.community.loadReportsFailed'))
    }
  } catch (e) {
    const msg = e.response?.data?.message
    ElMessage.error(msg === 'NOT_PENDING' ? t('admin.community.alreadyHandled') : msg || t('admin.community.loadReportsFailed'))
  } finally {
    actingId.value = null
  }
}

const ignoreReport = async (row) => {
  actingId.value = row.id
  try {
    const { data } = await axios.post(`/api/admin/reports/${row.id}/ignore`)
    if (data.success) {
      ElMessage.success(t('admin.community.ignoreSuccess'))
      await loadReports()
    } else {
      ElMessage.error(data.message === 'NOT_PENDING' ? t('admin.community.alreadyHandled') : data.message || t('admin.community.loadReportsFailed'))
    }
  } catch (e) {
    const msg = e.response?.data?.message
    ElMessage.error(msg === 'NOT_PENDING' ? t('admin.community.alreadyHandled') : msg || t('admin.community.loadReportsFailed'))
  } finally {
    actingId.value = null
  }
}

watch(activeTab, (tab) => {
  if (tab === 'reports') loadReports()
  if (tab === 'posts') loadPosts()
})

onMounted(() => {
  if (activeTab.value === 'reports') loadReports()
  if (activeTab.value === 'posts') loadPosts()
})

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
  margin-bottom: 25px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
}

.tabs {
  display: flex;
  gap: 10px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-card {
  background: white;
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 15px;
}

.author-avatar {
  font-size: 36px;
}

.author-name {
  font-weight: 600;
}

.post-time {
  font-size: 14px;
  color: #999;
}

.category {
  padding: 4px 12px;
  background: #f0f0f0;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.post-content {
  margin-bottom: 15px;
  line-height: 1.6;
}

.post-stats {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
}

.post-actions,
.report-actions {
  display: flex;
  gap: 10px;
}

.load-error {
  color: #c0392b;
  margin-bottom: 12px;
}

.empty-hint {
  color: #6c757d;
  margin-bottom: 12px;
}

.action-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.action-btn.edit {
  background: #1e3a5f;
  color: white;
}

.action-btn.delete {
  background: #dc3545;
  color: white;
}

.action-btn.accept {
  background: #28a745;
  color: white;
}

.action-btn.reject {
  background: #6c757d;
  color: white;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.reports-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.report-card {
  background: white;
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.report-title {
  font-weight: 600;
  font-size: 16px;
}

.report-time {
  font-size: 14px;
  color: #999;
}

.report-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.report-status.pending {
  background: #fff3cd;
  color: #856404;
}

.report-status.resolved {
  background: #d4edda;
  color: #155724;
}

.report-status.ignored {
  background: #e9ecef;
  color: #495057;
}

.report-content {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 15px;
}

.report-item {
  margin-bottom: 10px;
}

.report-item:last-child {
  margin-bottom: 0;
}

.report-item label {
  font-weight: 600;
}

.report-item .reason {
  color: #dc3545;
  font-weight: 500;
}

.post-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.badge-hidden {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 8px;
  background: #f8d7da;
  color: #721c24;
  font-weight: 600;
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

.post-edit-modal {
  background: white;
  border-radius: 14px;
  padding: 24px 28px;
  max-width: 520px;
  width: 100%;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
}

.modal-title {
  margin: 0 0 6px;
  font-size: 18px;
  color: #1e3a5f;
}

.modal-sub {
  margin: 0 0 16px;
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

.form-input,
.form-textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 12px;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  font-size: 14px;
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
  font-family: inherit;
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
