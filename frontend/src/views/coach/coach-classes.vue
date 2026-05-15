<template>
  <div class="coach-classes">
    <!-- 顶部导航 -->
    <header class="dashboard-header">
      <div class="header-content">
        <div class="logo">
          <span class="logo-icon">👨‍🏫</span>
          <span class="logo-text">FitLife {{ t('coach.login.title') }}</span>
        </div>
        <nav class="nav">
          <router-link to="/coach/dashboard" class="nav-item" :class="{ active: currentPage === 'dashboard' }">{{ t('common.dashboard') }}</router-link>
          <router-link to="/coach/profile" class="nav-item" :class="{ active: currentPage === 'profile' }">{{ t('common.myProfile') }}</router-link>
          <router-link to="/coach/classes" class="nav-item" :class="{ active: currentPage === 'classes' }">{{ t('common.classes') }}</router-link>
          <router-link to="/coach/mailbox" class="nav-item" :class="{ active: currentPage === 'mailbox' }">{{ t('coach.social.mailbox') }}</router-link>
        </nav>
        <div class="user-info">
          <div class="language-switch">
            <button 
              @click="switchLang('zh')" 
              class="lang-btn" 
              :class="{ active: currentLang === 'zh' }"
            >
              {{ t('common.chinese') }}
            </button>
            <button 
              @click="switchLang('en')" 
              class="lang-btn" 
              :class="{ active: currentLang === 'en' }"
            >
              {{ t('common.english') }}
            </button>
          </div>
          <span class="username">{{ username }}</span>
          <button class="logout-btn" @click="logout">{{ t('common.logout') }}</button>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="classes-content">
      <div class="section-title">
        <h1>📚 {{ t('coach.classes.title') }}</h1>
        <p>{{ t('coach.classes.subtitle') }}</p>
      </div>

      <!-- 标签切换 -->
      <div class="tabs-wrapper">
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'my-classes' }"
          @click="activeTab = 'my-classes'"
        >
          <span>📋 {{ t('coach.classes.myClasses') }}</span>
        </div>
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'add-class' }"
          @click="activeTab = 'add-class'"
        >
          <span>➕ {{ t('coach.classes.addClass') }}</span>
        </div>
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'facility' }"
          @click="activeTab = 'facility'"
        >
          <span>🏋️ {{ t('coach.classes.facility') }}</span>
        </div>
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'students' }"
          @click="activeTab = 'students'"
        >
          <span>👥 {{ t('coach.classes.students') }}</span>
        </div>
      </div>

      <!-- 我的课程 -->
      <div v-show="activeTab === 'my-classes'" class="tab-panel">
        <div class="filter-bar">
          <select v-model="classFilter" class="filter-select">
            <option value="all">{{ t('coach.classes.all') }}</option>
            <option value="upcoming">{{ t('coach.classes.upcoming') }}</option>
            <option value="ongoing">{{ t('coach.classes.ongoing') }}</option>
            <option value="completed">{{ t('coach.classes.completed') }}</option>
          </select>
        </div>
        
        <div v-if="myClasses.length === 0" class="empty-state">
          <div class="empty-icon">📚</div>
          <div class="empty-text">{{ t('coach.classes.noClasses') }}</div>
        </div>
        <div v-else class="classes-list">
          <div v-for="cls in filteredMyClasses" :key="cls.id" class="class-card">
            <div class="class-header">
              <div class="class-time">{{ cls.time }}</div>
              <div class="class-status" :class="cls.status">{{ displayClassStatus(cls) }}</div>
            </div>
            <div class="class-body">
              <h3 class="class-name">{{ displayCourseName(cls.name) }}</h3>
              <div class="class-meta">
                <span>📍 {{ displayVenue(cls.location) }}</span>
                <span>👥 {{ cls.participants }}/{{ cls.capacity }}{{ t('common.people') }}</span>
              </div>
            </div>
            <div class="class-actions">
              <button class="action-btn edit" @click="editClass(cls)">{{ t('common.edit') }}</button>
              <button class="action-btn delete" @click="deleteClass(cls.id)">{{ t('common.delete') }}</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 上架课程 -->
      <div v-show="activeTab === 'add-class'" class="tab-panel">
        <form @submit.prevent="addClass" class="class-form">
          <div class="form-row">
            <div class="form-group">
              <label>{{ t('coach.classes.name') }}</label>
              <input type="text" v-model="newClass.name" :placeholder="t('coach.classes.namePlaceholder')" required />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>{{ t('coach.classes.date') }}</label>
              <input type="date" v-model="newClass.date" required />
            </div>
            <div class="form-group">
              <label>{{ t('coach.classes.startTime') }}</label>
              <input type="time" v-model="newClass.startTime" required />
            </div>
            <div class="form-group">
              <label>{{ t('coach.classes.endTime') }}</label>
              <input type="time" v-model="newClass.endTime" required />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="label-with-note">
                {{ t('coach.classes.location') }}
                <span>{{ t('coach.classes.bookFacilityFirst') }}</span>
              </label>
              <select v-model="newClass.reservedBookingId" required @change="applyReservedFacilityBooking">
                <option value="" disabled>{{ t('coach.classes.selectBookedFacility') }}</option>
                <option
                  v-for="booking in bookableFacilityBookings"
                  :key="booking.id"
                  :value="booking.id"
                >
                  {{ displayBookedFacilityLabel(booking) }}
                </option>
              </select>
              <p class="field-hint">{{ t('coach.classes.bookFacilityHint') }}</p>
            </div>
            <div class="form-group">
              <label>{{ t('coach.classes.capacity') }}</label>
              <input type="number" v-model="newClass.capacity" :placeholder="t('coach.classes.capacityPlaceholder')" min="1" required />
            </div>
          </div>
          <div class="form-group full-width">
            <label>{{ t('coach.classes.description') }}</label>
            <textarea v-model="newClass.description" :placeholder="t('coach.classes.descPlaceholder')" rows="3"></textarea>
          </div>
          <button type="submit" class="submit-btn">{{ t('coach.classes.submit') }}</button>
        </form>
      </div>

      <!-- 预约场地 -->
      <div v-show="activeTab === 'facility'" class="tab-panel">
        <div v-if="facilities.length === 0" class="empty-state">
          <div class="empty-icon">🏋️</div>
          <div class="empty-text">{{ t('coach.classes.noFacilities') }}</div>
        </div>
        <div v-else class="facilities-grid">
          <div v-for="facility in facilities" :key="facility.id" class="facility-card">
            <div class="facility-header">
              <h3>{{ displayVenue(facility.name) }}</h3>
              <div class="facility-status" :class="facility.available ? 'available' : 'occupied'">
                {{ facility.available ? t('coach.classes.available') : t('coach.classes.occupied') }}
              </div>
            </div>
            <p class="facility-desc">{{ displayFacilityDescription(facility) }}</p>
            <div class="facility-equipment">
              <span class="equipment-label">{{ t('coach.classes.equipment') }}:</span>
              <span>{{ displayFacilityEquipment(facility) }}</span>
            </div>
            <div v-if="facility.available" class="facility-schedule">
              <div class="schedule-head">
                <h4>{{ t('coach.classes.timeSlots') }}</h4>
                <p class="schedule-hint">{{ t('coach.classes.slotPickHint') }}</p>
              </div>
              <div
                class="slot-chips"
                role="group"
                :aria-label="displayVenue(facility.name) + ' ' + t('coach.classes.timeSlots')"
              >
                <button
                  v-for="(slot, slotIdx) in facility.timeSlots"
                  :key="`${facility.id}-${slotIdx}-${slot}`"
                  type="button"
                  class="slot-chip"
                  :class="{ 'slot-chip--active': isSlotSelectedForFacility(facility.id, slot) }"
                  @click="selectTimeSlot(facility, slot)"
                >
                  {{ formatSlotDisplay(slot) }}
                </button>
              </div>
            </div>
            <button
              v-if="facility.available && isBookingReadyFor(facility.id)"
              type="button"
              class="book-btn"
              @click="bookFacility(facility)"
            >
              {{ t('coach.classes.confirmBooking') }}
            </button>
          </div>
        </div>


        <div class="reserved-facility-panel">
          <div class="reserved-head">
            <h3>{{ t('coach.classes.myReservedFacilities') }}</h3>
            <button type="button" class="mini-refresh" @click="loadMyFacilityBookings">{{ t('coach.classes.refresh') }}</button>
          </div>
          <div v-if="reservedFacilityBookings.length === 0" class="reserved-empty">
            {{ t('coach.classes.noReservedFacilities') }}
          </div>
          <div v-else class="reserved-list">
            <div v-for="booking in reservedFacilityBookings" :key="booking.id" class="reserved-item">
              <div class="reserved-info">
                <strong>{{ displayBookedFacilityLabel(booking) }}</strong>
                <span>{{ booking.usedByClass ? t('coach.classes.usedByClass') : t('coach.classes.usableForClass') }}</span>
              </div>
              <button
                type="button"
                class="cancel-reserved-btn"
                :disabled="booking.usedByClass"
                @click="cancelFacilityBooking(booking)"
              >
                {{ booking.usedByClass ? t('coach.classes.cannotDeleteUsedFacility') : t('coach.classes.cancelFacility') }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 学员信息 -->
      <div v-show="activeTab === 'students'" class="tab-panel">
        <div class="search-bar">
          <input type="text" v-model="studentSearch" :placeholder="t('coach.classes.searchPlaceholder')" class="search-input" />
        </div>
        
        <div v-if="students.length === 0" class="empty-state">
          <div class="empty-icon">👥</div>
          <div class="empty-text">{{ t('coach.classes.noStudents') }}</div>
        </div>
        <div v-else class="students-list">
          <div v-for="student in filteredStudents" :key="student.id" class="student-card" @click="showStudentDetail(student)">
            <div class="student-avatar">👤</div>
            <div class="student-info">
              <div class="student-name">{{ student.name }}</div>
              <div class="student-meta">
                <span>{{ displayMemberType(student.memberType) }}</span>
                <span>{{ student.phone }}</span>
                <span v-if="student.hasDisease" class="disease-tag">{{ t('coach.classes.hasDisease') }}</span>
                <span v-if="student.hasAttended" class="attended-tag">{{ t('coach.classes.attended') }}</span>
              </div>
            </div>
            <div class="student-status">
              {{ student.hasAttended ? t('coach.classes.attended') : t('coach.classes.notAttended') }}
            </div>
          </div>
        </div>

        <!-- 学员详情弹窗 -->
        <div v-if="selectedStudent" class="modal-overlay" @click="selectedStudent = null">
          <div class="modal-content" @click.stop>
            <div class="modal-header">
              <h3>{{ t('coach.classes.studentDetail') }}</h3>
              <button type="button" class="close-btn" :aria-label="t('common.close')" @click="selectedStudent = null">×</button>
            </div>
            <div class="modal-body">
              <div class="detail-row">
                <label>{{ t('coach.classes.name') }}</label>
                <span>{{ selectedStudent.name }}</span>
              </div>
              <div class="detail-row">
                <label>{{ t('coach.classes.phone') }}</label>
                <span>{{ selectedStudent.phone }}</span>
              </div>
              <div class="detail-row">
                <label>{{ t('coach.classes.memberType') }}</label>
                <span>{{ displayMemberType(selectedStudent.memberType) }}</span>
              </div>
              <div class="detail-row">
                <label>{{ t('coach.classes.joinDate') }}</label>
                <span>{{ selectedStudent.joinDate }}</span>
              </div>
              <div class="detail-row">
                <label>{{ t('coach.classes.health') }}</label>
                <span :class="selectedStudent.hasDisease ? 'warning' : 'normal'">
                  {{ selectedStudent.hasDisease ? t('coach.classes.disease') + ' - ' + selectedStudent.diseaseInfo : t('coach.classes.healthy') }}
                </span>
              </div>
              <div class="detail-row">
                <label>{{ t('coach.classes.attendance') }}</label>
                <span>{{ selectedStudent.hasAttended ? t('coach.classes.hasAttended') : t('coach.classes.notAttended') }}</span>
              </div>
              <div class="detail-row">
                <label>{{ t('coach.classes.classCount') }}</label>
                <span>{{ selectedStudent.classCount }}{{ t('coach.classes.times') }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 编辑课程弹窗：放在各标签面板外，避免在「我的课程」下被 v-show 隐藏 -->
      <div v-if="showEditModal" class="modal-overlay" @click="cancelEdit">
        <div class="modal-content edit-modal" @click.stop>
          <div class="modal-header">
            <h3>{{ t('coach.classes.editClass') }}</h3>
            <button type="button" class="close-btn" :aria-label="t('common.close')" @click="cancelEdit">×</button>
          </div>
          <div class="modal-body">
            <div class="form-row">
              <div class="form-group">
                <label>{{ t('coach.classes.name') }}</label>
                <input type="text" v-model="editClassData.name" :placeholder="t('coach.classes.namePlaceholder')" required />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>{{ t('coach.classes.date') }}</label>
                <input type="date" v-model="editClassData.date" required />
              </div>
              <div class="form-group">
                <label>{{ t('coach.classes.startTime') }}</label>
                <input type="time" v-model="editClassData.startTime" required />
              </div>
              <div class="form-group">
                <label>{{ t('coach.classes.endTime') }}</label>
                <input type="time" v-model="editClassData.endTime" required />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>{{ t('coach.classes.location') }}</label>
                <select v-model="editClassData.location">
                  <option value="训练区A">{{ t('locations.areaA') }}</option>
                  <option value="训练区B">{{ t('locations.areaB') }}</option>
                  <option value="瑜伽室">{{ t('locations.yoga') }}</option>
                  <option value="操课室">{{ t('locations.aerobics') }}</option>
                  <option value="动感单车室">{{ t('locations.spinning') }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>{{ t('coach.classes.capacity') }}</label>
                <input type="number" v-model="editClassData.capacity" :placeholder="t('coach.classes.capacityPlaceholder')" min="1" required />
              </div>
            </div>
            <div class="modal-footer">
              <button class="cancel-btn" @click="cancelEdit">{{ t('common.cancel') }}</button>
              <button class="submit-btn" @click="saveEditClass">{{ t('common.save') }}</button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { formatCourseName } from '../../utils/courseName'

const { t, te, locale } = useI18n()
const currentLang = computed(() => locale.value)

const displayCourseName = (name) => formatCourseName(name, t, te)

const displayVenue = (name) => {
  if (!name) return ''
  const key = 'venue.' + name
  return te(key) ? t(key) : name
}

const pickBilingual = (primary, alt) => {
  const primaryOk = primary != null && String(primary).trim() !== ''
  const altOk = alt != null && String(alt).trim() !== ''
  if (locale.value === 'en') return altOk ? String(alt) : (primaryOk ? String(primary) : '')
  return primaryOk ? String(primary) : (altOk ? String(alt) : '')
}

const displayFacilityDescription = (facility) => pickBilingual(facility?.description, facility?.descriptionEn)
const displayFacilityEquipment = (facility) => pickBilingual(facility?.equipment, facility?.equipmentEn)

const displayClassStatus = (cls) => {
  if (!cls) return ''
  const key = 'classStatus.' + cls.status
  return te(key) ? t(key) : (cls.statusText || cls.status || '')
}

const displayMemberType = (mt) => {
  if (!mt) return ''
  const key = 'memberTypeLabels.' + mt
  return te(key) ? t(key) : mt
}

const switchLang = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

const username = computed(() => localStorage.getItem('username') || t('common.coach'))
const currentPage = ref('classes')
const activeTab = ref('my-classes')
const classFilter = ref('all')
const studentSearch = ref('')
/** 场地预约：按「场地 id + 时段」选中，避免多卡共用同一时段字符串时全部高亮 */
const facilityBookingSelection = ref({ facilityId: null, slot: null })
const selectedStudent = ref(null)
const editingClass = ref(null)
const showEditModal = ref(false)

const myClasses = ref([])
const facilities = ref([])
const reservedFacilityBookings = ref([])
const students = ref([])

const newClass = ref({
  name: '',
  type: 'group',
  date: '',
  startTime: '',
  endTime: '',
  location: '',
  reservedBookingId: '',
  capacity: 20,
  description: ''
})

const editClassData = ref({
  id: '',
  name: '',
  date: '',
  startTime: '',
  endTime: '',
  location: '训练区A',
  capacity: 20
})

const filteredStudents = computed(() => {
  if (!studentSearch.value) return students.value
  const search = studentSearch.value.toLowerCase()
  return students.value.filter(s => 
    s.name.toLowerCase().includes(search) || 
    s.phone.includes(search)
  )
})

const filteredMyClasses = computed(() => {
  const list = myClasses.value || []
  const f = classFilter.value
  if (f === 'all') return list
  return list.filter((c) => {
    const s = c.status
    if (f === 'upcoming') return s === 'upcoming'
    if (f === 'ongoing') return s === 'starting'
    if (f === 'completed') return s === 'completed'
    return true
  })
})

const bookableFacilityBookings = computed(() => {
  return (reservedFacilityBookings.value || []).filter((booking) => !booking.usedByClass)
})

const getAuthHeader = () => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

const loadMyClasses = async () => {
  try {
    const response = await axios.get('/api/coach/classes', { headers: getAuthHeader() })
    if (response.data.success) {
      myClasses.value = response.data.data
    }
  } catch (error) {
    console.error('加载课程失败:', error)
    myClasses.value = []
    ElMessage.warning(t('coach.classes.loadClassesFailed'))
  }
}

const loadFacilities = async () => {
  try {
    const response = await axios.get('/api/coach/facilities', { headers: getAuthHeader() })
    if (response.data.success) {
      facilities.value = response.data.data
    }
  } catch (error) {
    console.error('加载场地失败:', error)
    facilities.value = []
    ElMessage.warning(t('coach.classes.loadFacilitiesFailed'))
  }
}


const loadMyFacilityBookings = async () => {
  try {
    const response = await axios.get('/api/coach/facilities/my-bookings', { headers: getAuthHeader() })
    if (response.data.success) {
      reservedFacilityBookings.value = (response.data.data || []).sort((a, b) => {
        const ak = `${a.date || ''} ${a.startTime || ''} ${a.facilityName || ''}`
        const bk = `${b.date || ''} ${b.startTime || ''} ${b.facilityName || ''}`
        return ak.localeCompare(bk)
      })
    }
  } catch (error) {
    console.error('加载已预约场地失败:', error)
    reservedFacilityBookings.value = []
  }
}

const displayBookedFacilityLabel = (booking) => {
  if (!booking) return ''
  const venue = locale.value === 'en' ? (booking.facilityNameEn || displayVenue(booking.facilityName)) : displayVenue(booking.facilityName)
  return `${venue} · ${booking.date} ${booking.startTime}–${booking.endTime}`
}

const applyReservedFacilityBooking = () => {
  const id = Number(newClass.value.reservedBookingId)
  const booking = reservedFacilityBookings.value.find((item) => Number(item.id) === id)
  if (!booking) return
  newClass.value.location = booking.facilityName
  newClass.value.date = booking.date
  newClass.value.startTime = booking.startTime
  newClass.value.endTime = booking.endTime
}

const cancelFacilityBooking = async (booking) => {
  if (!booking?.id) return
  try {
    await ElMessageBox.confirm(
      t('coach.classes.cancelFacilityConfirm'),
      t('coach.classes.cancelFacility'),
      { type: 'warning', confirmButtonText: t('common.confirm'), cancelButtonText: t('common.cancel') }
    )
  } catch {
    return
  }

  try {
    const response = await axios.delete(`/api/coach/facilities/bookings/${booking.id}`, { headers: getAuthHeader() })
    const data = response.data
    if (data && data.success) {
      ElMessage.success(t('coach.classes.cancelFacilitySuccess'))
      if (Number(newClass.value.reservedBookingId) === Number(booking.id)) {
        newClass.value.reservedBookingId = ''
        newClass.value.location = ''
        newClass.value.date = ''
        newClass.value.startTime = ''
        newClass.value.endTime = ''
      }
      loadFacilities()
      loadMyFacilityBookings()
    } else {
      const msg = data && data.message ? String(data.message) : ''
      ElMessage.error(msg ? t('coach.classes.cancelFacilityFailedDetail', { detail: msg }) : t('coach.classes.cancelFacilityFailedGeneric'))
    }
  } catch (error) {
    const serverMsg = error.response?.data?.message
    const detail = serverMsg != null && serverMsg !== '' ? String(serverMsg) : (error.message || '')
    ElMessage.error(detail ? t('coach.classes.cancelFacilityFailedDetail', { detail }) : t('coach.classes.networkHint'))
  }
}

const loadStudents = async () => {
  try {
    const response = await axios.get('/api/coach/students', { headers: getAuthHeader() })
    if (response.data.success) {
      students.value = response.data.data
    }
  } catch (error) {
    console.error('加载学员失败:', error)
    students.value = []
    ElMessage.warning(t('coach.classes.loadStudentsFailed'))
  }
}

const addClass = async () => {
  try {
    const cap = parseInt(String(newClass.value.capacity ?? '').trim(), 10)
    if (!newClass.value.reservedBookingId || !newClass.value.location) {
      ElMessage.warning(t('coach.classes.selectBookedFacility'))
      return
    }
    const payload = {
      name: String(newClass.value.name ?? '').trim(),
      type: newClass.value.type,
      date: newClass.value.date,
      startTime: newClass.value.startTime,
      endTime: newClass.value.endTime,
      location: newClass.value.location,
      reservedBookingId: newClass.value.reservedBookingId,
      capacity: Number.isNaN(cap) ? 0 : cap,
      description: newClass.value.description || ''
    }
    const response = await axios.post('/api/coach/classes', payload, { headers: getAuthHeader() })
    const data = response.data
    if (data && data.success) {
      ElMessage.success(t('coach.classes.addSuccess'))
      newClass.value = { name: '', type: 'group', date: '', startTime: '', endTime: '', location: '', reservedBookingId: '', capacity: 20, description: '' }
      activeTab.value = 'my-classes'
      loadMyClasses()
      loadMyFacilityBookings()
    } else {
      const msg = data && data.message ? String(data.message) : ''
      ElMessage.error(msg ? t('coach.classes.addFailedDetail', { detail: msg }) : t('coach.classes.addFailedGeneric'))
    }
  } catch (error) {
    const serverMsg = error.response?.data?.message
    const detail = serverMsg != null && serverMsg !== '' ? String(serverMsg) : (error.message || '')
    ElMessage.error(detail ? t('coach.classes.addFailedDetail', { detail }) : t('coach.classes.networkHint'))
  }
}

/** 解析后端 formatTime 生成的 "HH:mm-HH:mm"，不能对 '-' 简单 split（会拆坏 "09:00-10:00"） */
const parseClassTimeRange = (timeStr) => {
  if (!timeStr || typeof timeStr !== 'string') return { start: '', end: '' }
  const m = timeStr.trim().match(/^(\d{1,2}:\d{2})-(\d{1,2}:\d{2})$/)
  if (m) return { start: m[1], end: m[2] }
  const idx = timeStr.indexOf('-')
  if (idx > 0) {
    return { start: timeStr.slice(0, idx).trim(), end: timeStr.slice(idx + 1).trim() }
  }
  return { start: '', end: '' }
}

const editClass = (cls) => {
  const { start, end } = parseClassTimeRange(cls.time)
  const today = new Date()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

  editClassData.value = {
    id: cls.id,
    name: cls.name,
    date: cls.classDate || todayStr,
    startTime: cls.startSlot || start,
    endTime: cls.endSlot || end,
    location: cls.location,
    capacity: cls.capacity
  }
  showEditModal.value = true
}

const saveEditClass = async () => {
  try {
    const response = await axios.put(`/api/coach/classes/${editClassData.value.id}`, editClassData.value, { headers: getAuthHeader() })
    const data = response.data
    if (data && data.success) {
      ElMessage.success(t('coach.classes.editSuccess'))
      showEditModal.value = false
      loadMyClasses()
    } else {
      const msg = data && data.message ? String(data.message) : ''
      ElMessage.error(msg ? t('coach.classes.updateFailedDetail', { detail: msg }) : t('coach.classes.updateFailedGeneric'))
    }
  } catch (error) {
    const serverMsg = error.response?.data?.message
    const detail = serverMsg != null && serverMsg !== '' ? String(serverMsg) : (error.message || '')
    ElMessage.error(detail ? t('coach.classes.updateFailedDetail', { detail }) : t('coach.classes.networkHint'))
  }
}

const cancelEdit = () => {
  showEditModal.value = false
  editClassData.value = {
    id: '',
    name: '',
    date: '',
    startTime: '',
    endTime: '',
    location: '训练区A',
    capacity: 20
  }
}

const deleteClass = async (id) => {
  try {
    await ElMessageBox.confirm(
      t('coach.classes.deleteConfirm'),
      t('common.delete'),
      { type: 'warning', confirmButtonText: t('common.confirm'), cancelButtonText: t('common.cancel') }
    )
  } catch {
    return
  }
  try {
    const response = await axios.delete(`/api/coach/classes/${id}`, { headers: getAuthHeader() })
    const data = response.data
    if (data && data.success) {
      ElMessage.success(t('coach.classes.deleteSuccess'))
      loadMyClasses()
    } else {
      const msg = data && data.message ? String(data.message) : ''
      ElMessage.error(msg ? t('coach.classes.deleteFailedDetail', { detail: msg }) : t('coach.classes.deleteFailedGeneric'))
    }
  } catch (error) {
    const serverMsg = error.response?.data?.message
    const detail = serverMsg != null && serverMsg !== '' ? String(serverMsg) : (error.message || '')
    ElMessage.error(detail ? t('coach.classes.deleteFailedDetail', { detail }) : t('coach.classes.networkHint'))
  }
}

const sameFacility = (a, b) => Number(a) === Number(b)

const isSlotSelectedForFacility = (facilityId, slot) => {
  const s = facilityBookingSelection.value
  return s && s.slot === slot && sameFacility(s.facilityId, facilityId)
}

const isBookingReadyFor = (facilityId) => {
  const s = facilityBookingSelection.value
  return !!(s && s.slot && sameFacility(s.facilityId, facilityId))
}

const formatSlotDisplay = (slot) => {
  if (!slot || typeof slot !== 'string') return ''
  return slot.replace(/-/g, ' – ')
}

const selectTimeSlot = (facility, slot) => {
  const cur = facilityBookingSelection.value
  if (cur && sameFacility(cur.facilityId, facility.id) && cur.slot === slot) {
    facilityBookingSelection.value = { facilityId: null, slot: null }
    return
  }
  facilityBookingSelection.value = { facilityId: facility.id, slot }
}

const bookFacility = async (facility) => {
  const sel = facilityBookingSelection.value
  if (!sel?.slot || !sameFacility(sel.facilityId, facility.id)) {
    ElMessage.warning(t('coach.classes.selectSlotFirst'))
    return
  }
  try {
    const response = await axios.post('/api/coach/facilities/book', {
      facilityId: facility.id,
      timeSlot: sel.slot
    }, { headers: getAuthHeader() })
    const data = response.data
    if (data && data.success) {
      ElMessage.success(t('coach.facilities.bookSuccess'))
      if (data.data && data.data.id && !reservedFacilityBookings.value.some((b) => Number(b.id) === Number(data.data.id))) {
        reservedFacilityBookings.value.unshift(data.data)
      }
      facilityBookingSelection.value = { facilityId: null, slot: null }
      loadFacilities()
      loadMyFacilityBookings()
    } else {
      const msg = data && data.message ? String(data.message) : ''
      ElMessage.error(msg ? t('coach.classes.bookingFailedDetail', { detail: msg }) : t('coach.classes.bookingFailedGeneric'))
    }
  } catch (error) {
    const serverMsg = error.response?.data?.message
    const detail = serverMsg != null && serverMsg !== '' ? String(serverMsg) : (error.message || '')
    ElMessage.error(detail ? t('coach.classes.bookingFailedDetail', { detail }) : t('coach.classes.networkHint'))
  }
}

const showStudentDetail = (student) => {
  selectedStudent.value = student
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userType')
  localStorage.removeItem('coachId')
  window.location.href = '/coach/login'
}

onMounted(() => {
  loadMyClasses()
  loadFacilities()
  loadMyFacilityBookings()
  loadStudents()
})
</script>

<style scoped>
.coach-classes {
  min-height: 100vh;
  background: #f5f7fa;
}

.dashboard-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  box-sizing: border-box;
}

.header-content {
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  box-sizing: border-box;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  color: white;
  flex-shrink: 0;
}

.logo-icon {
  font-size: 30px;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  font-size: 19px;
  font-weight: 700;
  line-height: 1.35;
}

.nav {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  flex-wrap: wrap;
  gap: 10px;
  flex: 1;
  padding-top: 20px;
}

.nav-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.95);
  text-decoration: none;
  padding: 12px 18px;
  border-radius: 999px;
  line-height: 1.35;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  box-sizing: border-box;
  transition: background 0.2s, color 0.2s, box-shadow 0.2s;
}

.nav-item:hover,
.nav-item.active {
  background: rgba(255, 255, 255, 0.22);
  color: white;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
}

.language-switch {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.28);
  border: 2px solid rgba(255, 255, 255, 0.65);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.18),
    0 2px 10px rgba(0, 0, 0, 0.22);
}

.lang-btn {
  margin: 0;
  padding: 10px 18px;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.02em;
  line-height: 1.3;
  color: rgba(255, 255, 255, 0.96);
  background: transparent;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  transition: color 0.15s, background 0.15s, box-shadow 0.15s;
}

.lang-btn:not(.active) {
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}

.lang-btn:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.14);
}

.lang-btn.active {
  color: #312e81;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.username {
  color: white;
  font-weight: 600;
  font-size: 14px;
  line-height: 1.35;
  white-space: nowrap;
}

.logout-btn {
  margin: 0;
  padding: 12px 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 999px;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  line-height: 1.35;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
  transition: background 0.2s, border-color 0.2s, box-shadow 0.2s;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.28);
  border-color: rgba(255, 255, 255, 0.75);
}

.classes-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

.section-title {
  margin-bottom: 24px;
}

.section-title h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}

.section-title p {
  color: #666;
}

.tabs-wrapper {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.tab-item {
  padding: 12px 24px;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

.tab-item:hover {
  background: #f0f0f0;
}

.tab-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.tab-panel {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.filter-bar {
  margin-bottom: 20px;
}

.filter-select {
  padding: 8px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px;
  color: #999;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 18px;
}

.classes-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.class-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
}

.class-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.class-time {
  font-weight: 600;
  color: #333;
}

.class-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.class-status.upcoming {
  background: #dbeafe;
  color: #1d4ed8;
}

.class-status.pending {
  background: #fef3c7;
  color: #d97706;
}

.class-status.completed {
  background: #d1fae5;
  color: #059669;
}

.class-body {
  margin-bottom: 16px;
}

.class-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.class-meta {
  display: flex;
  gap: 16px;
  color: #666;
  font-size: 14px;
}

.class-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.3s;
}

.action-btn.edit {
  background: #e0e7ff;
  color: #4338ca;
}

.action-btn.delete {
  background: #fee2e2;
  color: #dc2626;
}

.action-btn:hover {
  opacity: 0.8;
}

.class-form {
  max-width: 800px;
  margin: 0 auto;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  font-weight: 600;
  color: #333;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.3s;
}

.submit-btn:hover {
  opacity: 0.9;
}

.facilities-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.facility-card {
  background: #fff;
  border-radius: 14px;
  padding: 22px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
}

.facility-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.facility-header h3 {
  font-size: 18px;
  color: #333;
}

.facility-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.facility-status.available {
  background: #d1fae5;
  color: #059669;
}

.facility-status.occupied {
  background: #fee2e2;
  color: #dc2626;
}

.facility-desc {
  color: #666;
  margin-bottom: 12px;
}

.facility-equipment {
  display: flex;
  gap: 8px;
  color: #666;
  font-size: 14px;
  margin-bottom: 16px;
}

.equipment-label {
  font-weight: 600;
}

.facility-schedule {
  margin-bottom: 16px;
  padding: 14px 14px 16px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.schedule-head {
  margin-bottom: 12px;
}

.facility-schedule h4 {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 6px 0;
}

.schedule-hint {
  margin: 0;
  font-size: 12px;
  line-height: 1.4;
  color: #64748b;
}

.slot-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.slot-chip {
  appearance: none;
  -webkit-appearance: none;
  margin: 0;
  padding: 10px 16px;
  min-height: 44px;
  min-width: 132px;
  box-sizing: border-box;
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  color: #334155;
  background: #f1f5f9;
  border: 2px solid #cbd5e1;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s, color 0.2s, box-shadow 0.2s;
  text-align: center;
  line-height: 1.3;
}

.slot-chip:hover {
  border-color: #667eea;
  background: #eef2ff;
  color: #4338ca;
}

.slot-chip:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.35);
}

.slot-chip--active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.35);
}

.slot-chip--active:hover {
  color: #fff;
  border-color: transparent;
  filter: brightness(1.03);
}

.book-btn {
  width: 100%;
  margin-top: 14px;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.3s;
}

.book-btn:hover {
  opacity: 0.9;
}

.search-bar {
  margin-bottom: 20px;
}

.search-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.3s;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
}

.students-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.student-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.3s;
}

.student-card:hover {
  background: #f0f0f0;
}

.student-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.student-info {
  flex: 1;
}

.student-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.student-meta {
  display: flex;
  gap: 12px;
  color: #666;
  font-size: 14px;
}

.disease-tag {
  background: #fee2e2;
  color: #dc2626;
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 12px;
}

.attended-tag {
  background: #d1fae5;
  color: #059669;
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 12px;
}

.student-status {
  color: #666;
  font-size: 14px;
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
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 16px;
  padding: 24px;
  width: 90%;
  max-width: 400px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.modal-header h3 {
  font-size: 20px;
  color: #333;
}

.close-btn {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  margin: 0;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: #e2e8f0;
  color: #334155;
  font-size: 20px;
  line-height: 1;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, color 0.2s, transform 0.15s;
}

.close-btn:hover {
  background: #cbd5e1;
  color: #0f172a;
}

.close-btn:active {
  transform: scale(0.96);
}

.close-btn:focus-visible {
  outline: none;
  box-shadow: 0 0 0 2px #fff, 0 0 0 4px #667eea;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-row label {
  color: #666;
}

.detail-row span {
  color: #333;
  font-weight: 500;
}

.detail-row span.warning {
  color: #dc2626;
}

.detail-row span.normal {
  color: #059669;
}

.edit-modal {
  max-width: 500px;
}

.modal-footer {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.cancel-btn {
  flex: 1;
  padding: 12px;
  background: #f0f0f0;
  color: #666;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s;
}

.cancel-btn:hover {
  background: #e0e0e0;
}

.modal-footer .submit-btn {
  flex: 2;
}

.label-with-note {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.label-with-note span {
  color: #ef8f00;
  font-size: 12px;
  font-weight: 600;
}

.field-hint {
  margin: 8px 0 0;
  color: #8a94a6;
  font-size: 12px;
  line-height: 1.5;
}

.reserved-facility-panel {
  margin-top: 24px;
  padding: 18px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.reserved-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.reserved-head h3 {
  margin: 0;
  color: #1f2937;
}

.mini-refresh {
  border: none;
  border-radius: 999px;
  padding: 8px 14px;
  background: #eef2ff;
  color: #5b5ce2;
  font-weight: 700;
  cursor: pointer;
}

.reserved-empty {
  color: #8a94a6;
  padding: 10px 0;
}

.reserved-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px;
}

.reserved-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  background: white;
  border: 1px solid #dbeafe;
}

.reserved-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.reserved-item span {
  color: #10b981;
  font-size: 12px;
  font-weight: 700;
}

.cancel-reserved-btn {
  border: none;
  border-radius: 999px;
  padding: 8px 12px;
  background: #fee2e2;
  color: #dc2626;
  font-size: 12px;
  font-weight: 800;
  cursor: pointer;
}

.cancel-reserved-btn:hover:not(:disabled) {
  background: #fecaca;
}

.cancel-reserved-btn:disabled {
  background: #e5e7eb;
  color: #6b7280;
  cursor: not-allowed;
}

</style>