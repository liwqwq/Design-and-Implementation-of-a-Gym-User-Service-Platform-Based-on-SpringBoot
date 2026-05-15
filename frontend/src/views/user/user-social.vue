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
          <span class="points">🏆 {{ points }} {{ t('common.points') }}</span>
          <span class="username">{{ username || t('common.guestName') }}</span>
          <button class="logout-btn" @click="logout">{{ t('common.logout') }}</button>
        </div>
      </div>
    </header>
    
    <main class="main-content">
      <div class="social-header">
        <div class="tabs">
          <button type="button" class="tab" :class="{ active: sortBy === 'hot' }" @click="applySort('hot')">🔥 {{ t('user.social.hot') }}</button>
          <button type="button" class="tab" :class="{ active: sortBy === 'latest' }" @click="applySort('latest')">🕐 {{ t('user.social.latest') }}</button>
        </div>
        <div class="category-filter">
          <button
            v-for="cat in categories"
            :key="cat.value"
            type="button"
            class="category-btn"
            :class="{ active: selectedCategory === cat.value }"
            @click="applyCategory(cat.value)"
          >{{ t(`user.social.categories.${cat.labelKey}`) }}</button>
        </div>
        <div class="header-actions">
          <button type="button" class="team-btn" @click="showTeamModal = true">👥 {{ t('user.social.createTeam') }}</button>
          <button type="button" class="post-btn" @click="showPostModal = true">📝 {{ t('user.social.post') }}</button>
        </div>
      </div>
      
      <div class="advertisements" v-if="advertisements.length > 0">
        <div v-for="ad in advertisements" :key="ad.id" class="ad-card">
          <div class="ad-title">{{ displayAdTitle(ad) }}</div>
          <div class="ad-desc">{{ displayAdDesc(ad) }}</div>
        </div>
      </div>
      
      <div class="section-title">📢 {{ t('user.social.teams') }}</div>
      <div class="teams-list">
        <div v-for="team in teams" :key="team.id" class="team-card">
          <div class="team-header">
            <div class="team-title">{{ displayTeamTitle(team) }}</div>
            <div class="team-category">{{ displayTeamCategory(team) }}</div>
          </div>
          <div class="team-desc">{{ displayTeamDesc(team) }}</div>
          <div class="team-info">
            <span>📍 {{ displayTeamLocation(team) }}</span>
            <span>🕐 {{ displayTeamMeetTime(team) }}</span>
            <span>👥 {{ team.currentMembers }}/{{ team.maxMembers }}</span>
          </div>
          <div class="team-creator">{{ t('user.social.creator') }}: {{ team.creatorName }}</div>
          <button class="join-btn"
                  :class="teamButtonClass(team)"
                  @click="toggleTeamMembership(team)"
                  :disabled="teamButtonDisabled(team)">
            {{ teamButtonLabel(team) }}
          </button>
        </div>
      </div>
      
      <div class="section-title">💬 {{ t('user.social.posts') }}</div>
      <div class="feed-toolbar">
        <span class="feed-line">{{ feedLineText }}</span>
      </div>
      <div v-if="!loading && posts.length === 0" class="feed-empty">{{ t('user.social.feedEmpty') }}</div>
      <div v-show="posts.length > 0" class="posts-list" :key="listRenderKey">
        <div v-for="post in posts" :key="post.id" class="post-card" :class="{ reported: post.reported }">
          <div class="post-header">
            <div class="post-author">
              <span class="author-avatar">👤</span>
              <div class="author-info">
                <div class="author-name">{{ post.username }}</div>
                <div class="post-time">{{ formatTime(post.createdAt) }}</div>
              </div>
            </div>
            <div class="post-actions-header">
              <span class="category-tag">{{ getCategoryLabel(post.category) }}</span>
              <button v-if="!post.reported" class="report-btn" @click="openReportModal(post)">🚩</button>
              <span v-else class="reported-tag">{{ t('user.social.reported') }}</span>
            </div>
          </div>
          <div class="post-content">{{ displayPostContent(post) }}</div>
          <div class="post-actions">
            <button class="action-btn" :class="{ liked: post.liked }" @click="likePost(post)">
              👍 {{ post.likes }}
            </button>
            <button class="action-btn" @click="toggleComments(post)">
              💬 {{ post.comments }}
            </button>
          </div>
          <div v-if="post.showComments" class="comments-section">
            <div v-for="comment in post.commentsData" :key="comment.id" class="comment-item">
              <div class="comment-header">
                <span class="comment-author">{{ comment.username }}</span>
                <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
              </div>
              <div class="comment-content">{{ displayCommentContent(comment) }}</div>
            </div>
            <div v-if="post.loadingComments" class="loading-text">{{ t('common.loading') }}...</div>
            <div class="comment-input-wrapper">
              <input type="text" v-model="post.newComment" :placeholder="t('user.social.commentPlaceholder')" class="comment-input" @keyup.enter="addComment(post)">
              <button class="send-btn" @click="addComment(post)">{{ t('user.social.send') }}</button>
            </div>
          </div>
        </div>
      </div>
      
      <div v-if="loading" class="loading-text">{{ t('common.loading') }}...</div>
      
      <!-- 发布动态弹窗 -->
      <div v-if="showPostModal" class="modal-overlay">
        <div class="modal-content">
          <div class="modal-header">
            <h3>{{ t('user.social.post') }}</h3>
            <button class="close-btn" @click="showPostModal = false">✕</button>
          </div>
          <select v-model="newPostCategory" class="category-select">
            <option v-for="cat in categories.filter(c => c.value !== 'all')" :key="cat.value" :value="cat.value">{{ t(`user.social.categories.${cat.labelKey}`) }}</option>
          </select>
          <textarea v-model="newPostContent" :placeholder="t('user.social.postPlaceholder')" class="post-textarea"></textarea>
          <div class="modal-actions">
            <button class="cancel-btn" @click="showPostModal = false">{{ t('common.cancel') }}</button>
            <button class="submit-btn" @click="submitPost" :disabled="!newPostContent.trim()">{{ t('user.social.submit') }}</button>
          </div>
        </div>
      </div>
      
      <!-- 创建组队弹窗 -->
      <div v-if="showTeamModal" class="modal-overlay">
        <div class="modal-content">
          <div class="modal-header">
            <h3>{{ t('user.social.createTeam') }}</h3>
            <button class="close-btn" @click="showTeamModal = false">✕</button>
          </div>
          <input v-model="newTeam.title" :placeholder="t('user.social.teamTitle')" class="team-input">
          <textarea v-model="newTeam.description" :placeholder="t('user.social.teamDesc')" class="team-textarea"></textarea>
          <input v-model="newTeam.location" :placeholder="t('user.social.teamLocation')" class="team-input">
          <input v-model="newTeam.meetTime" :placeholder="t('user.social.teamTime')" class="team-input">
          <select v-model="newTeam.category" class="category-select">
            <option value="running">🏃 {{ t('user.social.teamFormCategories.running') }}</option>
            <option value="fitness">🏋️ {{ t('user.social.teamFormCategories.fitness') }}</option>
            <option value="swimming">🏊 {{ t('user.social.teamFormCategories.swimming') }}</option>
            <option value="yoga">🧘 {{ t('user.social.teamFormCategories.yoga') }}</option>
            <option value="other">🎯 {{ t('user.social.teamFormCategories.other') }}</option>
          </select>
          <input type="number" v-model="newTeam.maxMembers" :placeholder="t('user.social.teamMaxMembers')" class="team-input">
          <div class="modal-actions">
            <button class="cancel-btn" @click="showTeamModal = false">{{ t('common.cancel') }}</button>
            <button class="submit-btn" @click="submitTeam" :disabled="!newTeam.title || !newTeam.description">{{ t('user.social.create') }}</button>
          </div>
        </div>
      </div>
      
      <!-- 举报弹窗 -->
      <div v-if="reportModalVisible" class="modal-overlay">
        <div class="modal-content">
          <div class="modal-header">
            <h3>{{ t('user.social.report') }}</h3>
            <button class="close-btn" @click="reportModalVisible = false">✕</button>
          </div>
          <select v-model="reportReason" class="category-select">
            <option value="spam">{{ t('user.social.reportSpam') }}</option>
            <option value="inappropriate">{{ t('user.social.reportInappropriate') }}</option>
            <option value="other">{{ t('user.social.reportOther') }}</option>
          </select>
          <div class="modal-actions">
            <button class="cancel-btn" @click="reportModalVisible = false">{{ t('common.cancel') }}</button>
            <button class="submit-btn" @click="submitReport">{{ t('user.social.submitReport') }}</button>
          </div>
        </div>
      </div>
      
      <!-- Toast提示 -->
      <div v-if="toast.show" class="toast" :class="{ show: toast.show, success: toast.type === 'success', error: toast.type === 'error' }">
        {{ toast.message }}
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import axios from 'axios'

const router = useRouter()
const { t, locale } = useI18n()
const currentLang = computed(() => locale.value)

const switchLang = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

const username = ref(localStorage.getItem('username') || '')
const points = ref(0)
const sortBy = ref('hot')
const selectedCategory = ref('all')
const lastMeta = ref({ sortBy: 'hot', category: 'all', total: 0 })
const listRenderKey = ref(0)
const showPostModal = ref(false)
const showTeamModal = ref(false)
const newPostContent = ref('')
const newPostCategory = ref('share')
const reportModalVisible = ref(false)
const reportReason = ref('spam')
const currentReportPost = ref(null)
const posts = ref([])
const teams = ref([])
const advertisements = ref([])
const loading = ref(false)
const reportedPostIds = ref(new Set())
const likedPostIds = ref(new Set())

const newTeam = ref({
  title: '',
  description: '',
  category: 'running',
  location: '',
  meetTime: '',
  maxMembers: 10
})

const TEAM_TITLE_SLUG = {
  '周末晨跑小队': 'weekendRun',
  '瑜伽爱好者群': 'yogaGroup',
  '力量训练打卡': 'strengthGroup',
  '游泳健身队': 'swimTeam'
}


const DEMO_POST_SLUG = {
  '今天的训练感觉特别好，状态满满！给大家分享一下我今天的训练计划。首先是热身15分钟，然后是力量训练45分钟，最后是拉伸放松。坚持就是胜利！💪': 'trainingPlan',
  '这是我坚持了半年的健身计划，每周三练，每次一小时。效果真的很明显，分享给大家！希望能帮助到想要健身的朋友。': 'halfYearPlan',
  '刚开始健身不久，很多东西都不太懂，希望大佬们多多指教！有什么好的建议吗？': 'beginnerHelp',
  '太开心了！今天终于能举起以前不敢想的重量了！感谢教练的指导，也感谢自己的坚持。继续加油！🎉': 'newPr',
  '健身一年，从最初的跑两步都累，到现在可以跑5公里不喘气。变化真的很大，分享一下我的心得。': 'oneYearRun',
  '用了很多健身装备，给大家推荐几款性价比很高的！首先是...': 'equipmentTips',
  '健身餐总是吃不下，有没有什么好吃又健康的做法推荐？': 'mealHelp',
  '今日打卡完成！连续打卡第30天，继续保持！': 'checkin30',
  '周末晨跑缺人，有没有伙伴一起？集合地点：中央公园东门，配速友好～': 'weekendRunPost',
  '想组一个小班练核心，每天晚上7点健身房见，上限6人。': 'coreTeamPost',
  '大家今天练了什么？来评论区互相打卡鼓励一下！': 'chatTraining',
  '新人报道，求推荐适合入门的团课～': 'newbieClassHelp'
}

const DEMO_COMMENT_SLUG = {
  '说得太好了！': 'greatPoint',
  '学习了，感谢分享！': 'learnedThanks',
  '太有帮助了！': 'helpful',
  '我也这么觉得！': 'sameHere',
  '加油！一起努力！': 'keepGoing',
  '666！': 'awesome',
  '学到了！': 'learned',
  '太强了！': 'strong'
}

const displayAdTitle = (ad) => {
  if (!ad) return ''
  if (locale.value === 'zh') return ad.title
  if (ad.title === '新会员优惠') return t('user.social.demoAds.newMemberTitle')
  if (ad.title === '私教课特惠') return t('user.social.demoAds.ptSaleTitle')
  return ad.title
}

const displayAdDesc = (ad) => {
  if (!ad) return ''
  if (locale.value === 'zh') return ad.description
  if (ad.title === '新会员优惠') return t('user.social.demoAds.newMemberDesc')
  if (ad.title === '私教课特惠') return t('user.social.demoAds.ptSaleDesc')
  return ad.description
}

const displayTeamTitle = (team) => {
  const slug = TEAM_TITLE_SLUG[team.title]
  if (locale.value === 'zh' || !slug) return team.title
  return t(`user.social.demoTeams.${slug}.title`)
}

const displayTeamDesc = (team) => {
  const slug = TEAM_TITLE_SLUG[team.title]
  if (locale.value === 'zh' || !slug) return team.description
  return t(`user.social.demoTeams.${slug}.description`)
}

const displayTeamLocation = (team) => {
  const slug = TEAM_TITLE_SLUG[team.title]
  if (locale.value === 'zh' || !slug) return team.location
  return t(`user.social.demoTeams.${slug}.location`)
}

const displayTeamMeetTime = (team) => {
  const slug = TEAM_TITLE_SLUG[team.title]
  if (locale.value === 'zh' || !slug) return team.meetTime
  return t(`user.social.demoTeams.${slug}.meetTime`)
}

const displayTeamCategory = (team) => {
  const key = team.category
  if (!key) return ''
  const path = `user.social.teamDbCategory.${key}`
  const translated = t(path)
  if (translated !== path) return translated
  return key
}


const displayPostContent = (post) => {
  if (!post) return ''
  const content = post.content || ''
  const slug = DEMO_POST_SLUG[content]
  if (locale.value === 'zh' || !slug) return content
  const key = `user.social.demoPosts.${slug}`
  const translated = t(key)
  return translated !== key ? translated : content
}

const displayCommentContent = (comment) => {
  if (!comment) return ''
  const content = comment.content || ''
  const slug = DEMO_COMMENT_SLUG[content]
  if (locale.value === 'zh' || !slug) return content
  const key = `user.social.demoComments.${slug}`
  const translated = t(key)
  return translated !== key ? translated : content
}

const categories = [
  { value: 'all', labelKey: 'all' },
  { value: 'share', labelKey: 'share' },
  { value: 'help', labelKey: 'help' },
  { value: 'team', labelKey: 'team' },
  { value: 'chat', labelKey: 'chat' },
]

const feedLineText = computed(() => {
  void locale.value
  void sortBy.value
  void selectedCategory.value
  const m = lastMeta.value
  const sortKey = m.sortBy === 'hot' ? 'user.social.feedSortHot' : 'user.social.feedSortLatest'
  let catLabel = t('user.social.categories.all')
  if (m.category && m.category !== 'all') {
    const entry = categories.find(c => c.value === m.category)
    catLabel = entry ? t(`user.social.categories.${entry.labelKey}`) : m.category
  }
  return t('user.social.feedLine', { sort: t(sortKey), category: catLabel, n: m.total })
})

const getAuthHeader = () => {
  const token = localStorage.getItem('token')
  if (token) {
    return { Authorization: `Bearer ${token}` }
  }
  return {}
}

/** 兼容 ISO 字符串、时间戳、以及 Jackson 可能输出的 LocalDateTime 数组 */
const parseServerDate = (raw) => {
  if (raw == null || raw === '') return null
  if (typeof raw === 'number' && Number.isFinite(raw)) {
    const d = new Date(raw)
    return Number.isNaN(d.getTime()) ? null : d
  }
  if (Array.isArray(raw)) {
    const y = raw[0]
    const mo = raw[1]
    const d = raw[2]
    if (y == null || mo == null || d == null) return null
    const h = raw.length > 3 ? raw[3] : 0
    const mi = raw.length > 4 ? raw[4] : 0
    const s = raw.length > 5 ? raw[5] : 0
    const dt = new Date(y, mo - 1, d, h, mi, s)
    return Number.isNaN(dt.getTime()) ? null : dt
  }
  const d = new Date(raw)
  return Number.isNaN(d.getTime()) ? null : d
}

const formatTime = (dateInput) => {
  const date = parseServerDate(dateInput)
  if (!date) return ''
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const localeTag = locale.value === 'zh' ? 'zh-CN' : 'en-US'

  if (diffMs < 0) {
    return new Intl.DateTimeFormat(localeTag, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date)
  }

  const minutes = Math.floor(diffMs / 60000)
  const hours = Math.floor(diffMs / 3600000)
  const days = Math.floor(diffMs / 86400000)

  if (minutes < 1) return t('user.social.time.justNow')
  if (minutes < 60) return t('user.social.time.minutesAgo', { n: minutes })
  if (hours < 24) return t('user.social.time.hoursAgo', { n: hours })
  if (days < 30) return t('user.social.time.daysAgo', { n: days })
  return new Intl.DateTimeFormat(localeTag, { year: 'numeric', month: 'short', day: 'numeric' }).format(date)
}

const getCategoryLabel = (category) => {
  const raw = category != null && String(category).trim() !== ''
    ? String(category).trim().toLowerCase()
    : 'share'
  const cat = categories.find(c => c.value === raw)
  if (!cat) return t('user.social.categories.other')
  return t(`user.social.categories.${cat.labelKey}`)
}

const toast = ref({ show: false, message: '', type: 'success' })

const showToast = (message, type = 'success') => {
  toast.value = { show: true, message, type }
  setTimeout(() => {
    toast.value = { show: false, message: '', type: 'success' }
  }, 3000)
}

const loadPoints = async () => {
  try {
    const response = await axios.get('/api/points/my', { headers: getAuthHeader() })
    if (response.data.success) {
      points.value = response.data.data.availablePoints || 0
    }
  } catch (error) {
    console.error('加载积分失败:', error)
  }
}

const loadPosts = async () => {
  loading.value = true
  try {
    const categoryParam = selectedCategory.value === 'all' ? 'all' : selectedCategory.value
    console.log('加载帖子，sortBy:', sortBy.value, 'category:', categoryParam)
    const response = await axios.get('/api/posts', {
      params: {
        sortBy: sortBy.value,
        category: categoryParam
      }
    })
    if (response.data.success) {
      const list = Array.isArray(response.data.data) ? response.data.data : []
      posts.value = list.map(p => ({
        ...p,
        showComments: false,
        newComment: '',
        loadingComments: false,
        commentsData: [],
        reported: Boolean(p.reported) || reportedPostIds.value.has(p.id),
        liked: Boolean(p.liked) || likedPostIds.value.has(p.id)
      }))
      const meta = response.data.meta
      if (meta && typeof meta === 'object') {
        lastMeta.value = {
          sortBy: meta.sortBy || sortBy.value,
          category: meta.category != null ? meta.category : selectedCategory.value,
          total: Number(meta.total) >= 0 ? Number(meta.total) : list.length
        }
      } else {
        lastMeta.value = {
          sortBy: sortBy.value,
          category: selectedCategory.value,
          total: list.length
        }
      }
      listRenderKey.value += 1
      console.log('帖子加载成功，数量:', posts.value.length, 'meta:', lastMeta.value)
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
    showToast(t('user.social.toasts.postsLoadFailed'), 'error')
  } finally {
    loading.value = false
  }
}

const applySort = (mode) => {
  sortBy.value = mode
  loadPosts()
}

const applyCategory = (cat) => {
  selectedCategory.value = cat
  loadPosts()
}

const loadTeams = async () => {
  try {
    const response = await axios.get('/api/teams', { headers: getAuthHeader() })
    if (response.data.success) {
      teams.value = response.data.data
    }
  } catch (error) {
    console.error('加载组队失败:', error)
  }
}

const loadAdvertisements = async () => {
  try {
    const response = await axios.get('/api/advertisements')
    if (response.data.success) {
      advertisements.value = response.data.data
    }
  } catch (error) {
    console.error('加载广告失败:', error)
  }
}

const likePost = async (post) => {
  if (post.liked) {
    showToast(t('user.social.toasts.likedAlready'), 'error')
    return
  }
  
  try {
    const response = await axios.post(`/api/posts/${post.id}/like`, {}, { headers: getAuthHeader() })
    if (response.data.success) {
      post.likes = response.data.likes
      post.liked = true
      likedPostIds.value.add(post.id)
      showToast(response.data.alreadyLiked ? t('user.social.toasts.likedAlready') : t('user.social.toasts.likeSuccess'))
    }
  } catch (error) {
    console.error('点赞失败:', error)
    showToast(t('user.social.toasts.likeFailed'), 'error')
  }
}

const toggleComments = async (post) => {
  post.showComments = !post.showComments
  if (post.showComments && post.commentsData.length === 0) {
    await loadComments(post)
  }
}

const loadComments = async (post) => {
  post.loadingComments = true
  try {
    const response = await axios.get(`/api/posts/${post.id}/comments`)
    if (response.data.success) {
      post.commentsData = response.data.data
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  } finally {
    post.loadingComments = false
  }
}

const addComment = async (post) => {
  if (!post.newComment.trim()) {
    showToast(t('user.social.toasts.commentEmpty'), 'error')
    return
  }
  
  try {
    const response = await axios.post(`/api/posts/${post.id}/comments`, 
      { content: post.newComment },
      { headers: getAuthHeader() }
    )
    if (response.data.success) {
      post.comments = response.data.comments ?? (post.comments + 1)
      post.commentsData.unshift(response.data.data)
      post.newComment = ''
      showToast(t('user.social.toasts.commentSuccess'))
    }
  } catch (error) {
    console.error('评论失败:', error)
    showToast(t('user.social.toasts.commentFailed'), 'error')
  }
}

const submitPost = async () => {
  if (!newPostContent.value.trim()) {
    showToast(t('user.social.toasts.postEmpty'), 'error')
    return
  }
  
  try {
    const response = await axios.post('/api/posts', 
      { 
        content: newPostContent.value,
        category: newPostCategory.value
      },
      { headers: getAuthHeader() }
    )
    if (response.data.success) {
      showToast(t('user.social.toasts.postSuccess'))
      newPostContent.value = ''
      showPostModal.value = false
      loadPosts()
    }
  } catch (error) {
    console.error('发布失败:', error)
    showToast(t('user.social.toasts.postFailed'), 'error')
  }
}

const submitTeam = async () => {
  if (!newTeam.value.title || !newTeam.value.description) {
    showToast(t('user.social.toasts.teamIncomplete'), 'error')
    return
  }
  
  try {
    const response = await axios.post('/api/teams', 
      { 
        title: newTeam.value.title,
        description: newTeam.value.description,
        category: newTeam.value.category,
        location: newTeam.value.location,
        meetTime: newTeam.value.meetTime,
        maxMembers: Number(newTeam.value.maxMembers) || 10
      },
      { headers: getAuthHeader() }
    )
    if (response.data.success) {
      showToast(t('user.social.toasts.teamCreated'))
      newTeam.value = {
        title: '',
        description: '',
        category: 'running',
        location: '',
        meetTime: '',
        maxMembers: 10
      }
      showTeamModal.value = false
      loadTeams()
    }
  } catch (error) {
    console.error('创建失败:', error)
    showToast(error.response?.data?.message || t('user.social.toasts.teamCreateFailed'), 'error')
  }
}

const openReportModal = (post) => {
  currentReportPost.value = post
  reportModalVisible.value = true
}

const submitReport = async () => {
  try {
    const response = await axios.post(`/api/posts/${currentReportPost.value.id}/report`, {
      type: reportReason.value,
      reason: reportReason.value
    }, { headers: getAuthHeader() })
    currentReportPost.value.reported = true
    reportedPostIds.value.add(currentReportPost.value.id)
    showToast(response.data.alreadyReported ? t('user.social.toasts.reportAlreadySubmitted') : t('user.social.toasts.reportSubmitted'))
    reportModalVisible.value = false
  } catch (error) {
    console.error('举报失败:', error)
    showToast(t('user.social.toasts.reportFailed'), 'error')
  }
}

const teamButtonDisabled = (team) => {
  if (!team) return true
  if (team.createdByCurrentUser) return true
  if (!team.joined && team.currentMembers >= team.maxMembers) return true
  return false
}

const teamButtonClass = (team) => ({
  joined: team.joined,
  leave: team.joined && !team.createdByCurrentUser,
  full: !team.joined && team.currentMembers >= team.maxMembers
})

const teamButtonLabel = (team) => {
  if (team.createdByCurrentUser) return t('user.social.createdByMe')
  if (team.joined) return t('user.social.leaveTeam')
  if (team.currentMembers >= team.maxMembers) return t('user.social.full')
  return t('user.social.join')
}

const applyTeamPayload = (team, payload) => {
  if (!payload || !payload.id) return
  team.joined = Boolean(payload.joined)
  team.createdByCurrentUser = Boolean(payload.createdByCurrentUser)
  team.currentMembers = payload.currentMembers
  team.maxMembers = payload.maxMembers
}

const joinTeam = async (team) => {
  try {
    const response = await axios.post(`/api/teams/${team.id}/join`, {}, { headers: getAuthHeader() })
    if (response.data.success) {
      applyTeamPayload(team, response.data.data)
      showToast(t('user.social.toasts.joinSuccess'))
    }
  } catch (error) {
    console.error('加入失败:', error)
    showToast(error.response?.data?.message || t('user.social.toasts.joinFailed'), 'error')
  }
}

const leaveTeam = async (team) => {
  try {
    const response = await axios.post(`/api/teams/${team.id}/leave`, {}, { headers: getAuthHeader() })
    if (response.data.success) {
      applyTeamPayload(team, response.data.data)
      showToast(t('user.social.toasts.leaveSuccess'))
    }
  } catch (error) {
    console.error('退出失败:', error)
    showToast(error.response?.data?.message || t('user.social.toasts.leaveFailed'), 'error')
  }
}

const toggleTeamMembership = async (team) => {
  if (teamButtonDisabled(team)) return
  if (team.joined) {
    await leaveTeam(team)
  } else {
    await joinTeam(team)
  }
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userType')
  localStorage.removeItem('user')
  localStorage.removeItem('coachId')
  router.push('/user/login')
}

onMounted(() => {
  loadPoints()
  loadPosts()
  loadTeams()
  loadAdvertisements()
})
</script>

<style scoped>
.user-dashboard {
  min-height: 100vh;
  background: #f8fafc;
}

.main-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px;
}

.social-header {
  margin-bottom: 30px;
}

.tabs {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.tab {
  padding: 10px 24px;
  background: #f0f0f0;
  color: #666;
  border: none;
  border-radius: 20px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.tab:hover {
  background: #e2e8f0;
}

.tab.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.category-filter {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.category-btn {
  padding: 8px 20px;
  background: #f0f0f0;
  color: #666;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.category-btn:hover {
  background: #e2e8f0;
}

.category-btn.active {
  background: #667eea;
  color: white;
}

.header-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.team-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  border-radius: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.team-btn:hover {
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
}

.post-btn {
  padding: 12px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.post-btn:hover {
  background: linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%);
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  margin: 30px 0 20px;
  color: #333;
}

.advertisements {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.ad-card {
  flex: 1;
  min-width: 250px;
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  padding: 20px;
  border-radius: 15px;
  color: white;
}

.ad-title {
  font-weight: 700;
  margin-bottom: 8px;
}

.ad-desc {
  font-size: 14px;
  opacity: 0.9;
}

.teams-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.team-card {
  background: white;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.team-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.team-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.team-title {
  font-weight: 700;
  font-size: 16px;
}

.team-category {
  background: #f0f0f0;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

.team-desc {
  color: #666;
  margin-bottom: 15px;
  line-height: 1.5;
}

.team-info {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #888;
  margin-bottom: 10px;
}

.team-creator {
  font-size: 13px;
  color: #888;
  margin-bottom: 15px;
}

.join-btn {
  width: 100%;
  padding: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.join-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%);
}

.join-btn:disabled {
  cursor: not-allowed;
}

.join-btn.joined {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.join-btn.leave {
  background: linear-gradient(135deg, #f97316 0%, #dc2626 100%);
}

.join-btn.leave:hover:not(:disabled) {
  background: linear-gradient(135deg, #ea580c 0%, #b91c1c 100%);
}

.join-btn.full {
  background: #9ca3af;
}

.feed-toolbar {
  margin: 8px 0 16px;
  padding: 10px 14px;
  background: #eef2ff;
  border-radius: 12px;
  border: 1px solid #e0e7ff;
}

.feed-line {
  font-size: 14px;
  color: #4338ca;
  font-weight: 600;
}

.feed-empty {
  text-align: center;
  padding: 28px 16px;
  color: #64748b;
  font-size: 15px;
  background: #f8fafc;
  border-radius: 16px;
  margin-bottom: 16px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.post-card {
  background: white;
  border-radius: 20px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.post-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.post-card.reported {
  opacity: 0.7;
  background: #fef2f2;
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

.post-actions-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.report-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
  opacity: 0.6;
  transition: all 0.3s;
}

.report-btn:hover {
  opacity: 1;
}

.reported-tag {
  font-size: 12px;
  color: #ef4444;
  background: #fee2e2;
  padding: 4px 8px;
  border-radius: 8px;
}

.category-tag {
  padding: 4px 12px;
  background: #f0f0f0;
  border-radius: 12px;
  font-size: 12px;
}

.post-content {
  margin-bottom: 15px;
  line-height: 1.6;
}

.post-actions {
  display: flex;
  gap: 20px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  background: #f8fafc;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn:hover {
  background: #e2e8f0;
}

.action-btn.liked {
  background: #dbeafe;
  color: #3b82f6;
}

.comments-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.comment-item {
  margin-bottom: 15px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 12px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 600;
  font-size: 14px;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  font-size: 14px;
  color: #333;
}

.comment-input-wrapper {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.comment-input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 15px;
  font-size: 14px;
  transition: all 0.3s;
}

.comment-input:focus {
  outline: none;
  border-color: #667eea;
}

.send-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.send-btn:hover {
  background: linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%);
}

.loading-text {
  text-align: center;
  color: #888;
  padding: 20px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1001;
}

.modal-content {
  background: white;
  border-radius: 20px;
  padding: 30px;
  width: 90%;
  max-width: 500px;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    transform: translateY(-20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.modal-header h3 {
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  transition: all 0.3s;
}

.close-btn:hover {
  color: #333;
}

.category-select {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 15px;
  font-size: 14px;
  margin-bottom: 15px;
  transition: all 0.3s;
}

.category-select:focus {
  outline: none;
  border-color: #667eea;
}

.post-textarea {
  width: 100%;
  height: 150px;
  padding: 16px;
  border: 2px solid #e5e7eb;
  border-radius: 15px;
  font-size: 16px;
  resize: none;
  margin-bottom: 20px;
  transition: all 0.3s;
}

.post-textarea:focus {
  outline: none;
  border-color: #667eea;
}

.team-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 15px;
  font-size: 14px;
  margin-bottom: 12px;
  transition: all 0.3s;
  box-sizing: border-box;
}

.team-input:focus {
  outline: none;
  border-color: #667eea;
}

.team-textarea {
  width: 100%;
  height: 100px;
  padding: 16px;
  border: 2px solid #e5e7eb;
  border-radius: 15px;
  font-size: 14px;
  resize: none;
  margin-bottom: 12px;
  transition: all 0.3s;
  box-sizing: border-box;
}

.team-textarea:focus {
  outline: none;
  border-color: #667eea;
}

.modal-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
}

.cancel-btn {
  padding: 12px 24px;
  background: #f1f5f9;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.cancel-btn:hover {
  background: #e2e8f0;
}

.submit-btn {
  padding: 12px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.submit-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.toast {
  position: fixed;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%) translateY(100px);
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 15px 30px;
  border-radius: 10px;
  transition: all 0.3s;
  z-index: 2000;
}

.toast.show {
  transform: translateX(-50%) translateY(0);
}

.toast.success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.toast.error {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}
</style>
