<template>
  <UserLayout>
    <div class="group-classes-container">
      <div class="page-header">
        <h2>👥 团课中心</h2>
        <p class="page-desc">查看未来7天可预约的团课课程</p>
      </div>

      <div class="week-tabs">
        <el-tabs v-model="selectedDay" @tab-change="fetchClasses">
          <el-tab-pane v-for="day in weekDays" :key="day.date" :label="day.label" :name="day.date" />
        </el-tabs>
      </div>

      <div class="classes-grid">
        <div v-for="cls in classes" :key="cls.id" class="class-card">
          <div class="class-time">{{ cls.time }}</div>
          <div class="class-content">
            <div class="class-name">{{ cls.name }}</div>
            <div class="class-info">
              <span class="info-item">👨‍🏫 {{ cls.trainer }}</span>
              <span class="info-item">👥 {{ cls.currentCount }}/{{ cls.maxCount }}</span>
            </div>
            <div class="class-desc">{{ cls.description }}</div>
          </div>
          <div class="class-actions">
            <div class="class-status" :class="{ available: cls.currentCount < cls.maxCount, full: cls.currentCount >= cls.maxCount }">
              {{ cls.currentCount >= cls.maxCount ? '已满' : '可预约' }}
            </div>
            <el-button
              type="primary"
              size="small"
              :disabled="cls.currentCount >= cls.maxCount"
              @click="bookClass(cls)"
            >
              立即预约
            </el-button>
          </div>
        </div>
      </div>

      <el-dialog v-model="showBookingDialog" title="确认预约" width="400px" center>
        <div class="booking-dialog" v-if="selectedClass">
          <div class="booking-class">
            <div class="booking-class-name">{{ selectedClass.name }}</div>
            <div class="booking-class-info">
              <div>📅 {{ selectedDay }} {{ selectedClass.time }}</div>
              <div>👨‍🏫 {{ selectedClass.trainer }}</div>
            </div>
          </div>
          <div class="booking-tip">确认预约此课程？</div>
        </div>
        <template #footer>
          <el-button @click="showBookingDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmBooking">确认预约</el-button>
        </template>
      </el-dialog>
    </div>
  </UserLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import UserLayout from '../components/UserLayout.vue'

const weekDays = ref([])
const selectedDay = ref('')
const classes = ref([])
const showBookingDialog = ref(false)
const selectedClass = ref(null)

const generateWeekDays = () => {
  const days = []
  const weekLabels = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const today = new Date()

  for (let i = 0; i < 7; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)
    const month = date.getMonth() + 1
    const day = date.getDate()
    const weekDay = weekLabels[date.getDay()]

    days.push({
      date: `${month}/${day}`,
      fullDate: date.toISOString().split('T')[0],
      label: i === 0 ? '今天' : `${weekDay} ${month}/${day}`
    })
  }

  weekDays.value = days
  selectedDay.value = days[0].fullDate
}

const classesData = [
  { name: '燃脂搏击', trainer: '张教练', time: '09:00 - 10:00', description: '高强度燃脂，快速消耗卡路里', maxCount: 30, currentCount: 18 },
  { name: '瑜伽基础', trainer: '李教练', time: '10:30 - 11:30', description: '适合初学者的瑜伽课程', maxCount: 25, currentCount: 12 },
  { name: '动感单车', trainer: '王教练', time: '14:00 - 15:00', description: '音乐配合骑行，燃脂效果极佳', maxCount: 20, currentCount: 20 },
  { name: '杠铃塑形', trainer: '刘教练', time: '15:30 - 16:30', description: '力量训练，塑造完美体型', maxCount: 15, currentCount: 8 },
  { name: '尊巴舞', trainer: '赵教练', time: '19:00 - 20:00', description: '拉丁风格舞蹈，欢乐燃脂', maxCount: 30, currentCount: 22 },
  { name: '普拉提', trainer: '李教练', time: '20:00 - 21:00', description: '核心力量训练，改善体态', maxCount: 20, currentCount: 15 }
]

const fetchClasses = async (day) => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/classes/group', {
      params: { date: day },
      headers: { Authorization: `Bearer ${token}` }
    })
    if (response.data.success) {
      classes.value = response.data.data
    } else {
      classes.value = classesData.map((c, i) => ({ ...c, id: i + 1 }))
    }
  } catch (error) {
    classes.value = classesData.map((c, i) => ({ ...c, id: i + 1 }))
  }
}

const bookClass = (cls) => {
  selectedClass.value = cls
  showBookingDialog.value = true
}

const confirmBooking = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.post('/api/bookings/group', {
      classId: selectedClass.value.id,
      date: selectedDay.value,
      time: selectedClass.value.time
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (response.data.success) {
      ElMessage.success('预约成功')
      selectedClass.value.currentCount++
      showBookingDialog.value = false
    }
  } catch (error) {
    ElMessage.error('预约失败，请稍后重试')
  }
}

onMounted(() => {
  generateWeekDays()
  fetchClasses(selectedDay.value)
})
</script>

<style scoped>
.group-classes-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: var(--spacing-lg);
}

.page-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.page-header h2 {
  font-size: 28px;
  margin-bottom: var(--spacing-xs);
}

.page-desc {
  color: var(--text-secondary);
}

.week-tabs {
  background: white;
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}

.classes-grid {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.class-card {
  background: white;
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  display: flex;
  gap: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
}

.class-card:hover {
  transform: translateX(5px);
  box-shadow: var(--shadow-md);
}

.class-time {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  min-width: 100px;
  text-align: center;
}

.class-content {
  flex: 1;
}

.class-name {
  font-size: var(--font-size-lg);
  font-weight: 700;
  margin-bottom: var(--spacing-xs);
}

.class-info {
  display: flex;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-sm);
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.class-desc {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.class-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
}

.class-status {
  font-size: var(--font-size-sm);
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 12px;
}

.class-status.available {
  background: #e8f5e9;
  color: #2e7d32;
}

.class-status.full {
  background: #ffebee;
  color: #c62828;
}

.booking-dialog {
  text-align: center;
}

.booking-class {
  background: var(--bg-primary);
  padding: var(--spacing-lg);
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-md);
}

.booking-class-name {
  font-size: var(--font-size-lg);
  font-weight: 700;
  margin-bottom: var(--spacing-sm);
}

.booking-class-info {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
  color: var(--text-secondary);
}

.booking-tip {
  color: var(--text-secondary);
}
</style>