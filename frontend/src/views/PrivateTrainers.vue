<template>
  <UserLayout>
    <div class="private-trainer-container">
      <div class="page-header">
        <h2>🏋️ 私教服务</h2>
        <p class="page-desc">专业教练一对一指导，定制化健身计划</p>
      </div>

      <div class="trainers-grid">
        <div v-for="trainer in trainers" :key="trainer.id" class="trainer-card">
          <div class="trainer-avatar">{{ trainer.avatar }}</div>
          <div class="trainer-info">
            <div class="trainer-name">{{ trainer.name }}</div>
            <div class="trainer-title">{{ trainer.title }}</div>
            <div class="trainer-rating">
              <span v-for="i in 5" :key="i" :class="{ filled: i <= trainer.rating }">★</span>
              <span class="rating-text">{{ trainer.rating }}.0</span>
            </div>
            <div class="trainer-specialties">
              <el-tag v-for="specialty in trainer.specialties" :key="specialty" size="small" type="info">
                {{ specialty }}
              </el-tag>
            </div>
            <div class="trainer-desc">{{ trainer.description }}</div>
          </div>
          <div class="trainer-actions">
            <div class="trainer-price">¥{{ trainer.price }}/课时</div>
            <el-button type="primary" @click="bookTrainer(trainer)">立即预约</el-button>
          </div>
        </div>
      </div>

      <el-dialog v-model="showBookingDialog" title="预约私教" width="500px" center>
        <div class="booking-form" v-if="selectedTrainer">
          <div class="booking-trainer">
            <div class="booking-avatar">{{ selectedTrainer.avatar }}</div>
            <div class="booking-info">
              <div class="booking-name">{{ selectedTrainer.name }}</div>
              <div class="booking-title">{{ selectedTrainer.title }}</div>
            </div>
          </div>
          <div class="form-item">
            <label>选择课程类型</label>
            <el-select v-model="bookingForm.courseType" placeholder="请选择课程">
              <el-option
                v-for="course in selectedTrainer.courses"
                :key="course.id"
                :label="`${course.name} - ¥${course.price}`"
                :value="course.id"
              />
            </el-select>
          </div>
          <div class="form-item">
            <label>预约日期</label>
            <el-date-picker
              v-model="bookingForm.date"
              type="date"
              placeholder="选择日期"
              :disabled-date="disabledDate"
              style="width: 100%"
            />
          </div>
          <div class="form-item">
            <label>预约时间</label>
            <el-select v-model="bookingForm.time" placeholder="选择时间">
              <el-option label="09:00 - 10:00" value="09:00" />
              <el-option label="10:00 - 11:00" value="10:00" />
              <el-option label="14:00 - 15:00" value="14:00" />
              <el-option label="15:00 - 16:00" value="15:00" />
              <el-option label="19:00 - 20:00" value="19:00" />
              <el-option label="20:00 - 21:00" value="20:00" />
            </el-select>
          </div>
          <div class="form-item">
            <label>备注</label>
            <el-input v-model="bookingForm.note" type="textarea" placeholder="有什么特殊需求？" />
          </div>
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

const trainers = ref([
  {
    id: 1,
    avatar: '👨‍🏫',
    name: '张教练',
    title: '高级私人教练',
    rating: 5,
    price: 300,
    specialties: ['增肌', '减脂', '体能训练'],
    description: '10年健身教学经验，擅长增肌减脂训练，曾帮助500+学员达成目标',
    courses: [
      { id: 1, name: '一对一私教', price: 300 },
      { id: 2, name: '10节套餐', price: 2700 },
      { id: 3, name: '30节套餐', price: 7500 }
    ]
  },
  {
    id: 2,
    avatar: '👩‍🏫',
    name: '李教练',
    title: '国家级健身教练',
    rating: 5,
    price: 350,
    specialties: ['瑜伽', '普拉提', '产后修复'],
    description: '国际认证瑜伽导师，专注女性健身和康复训练',
    courses: [
      { id: 1, name: '瑜伽私教', price: 350 },
      { id: 2, name: '10节套餐', price: 3150 },
      { id: 3, name: '30节套餐', price: 9000 }
    ]
  },
  {
    id: 3,
    avatar: '🏃',
    name: '王教练',
    title: '运动康复师',
    rating: 4,
    price: 400,
    specialties: ['运动康复', '体态矫正', '损伤恢复'],
    description: '医学背景出身，专业运动康复专家，为多名职业运动员提供康复服务',
    courses: [
      { id: 1, name: '康复评估', price: 400 },
      { id: 2, name: '10节康复课', price: 3600 }
    ]
  },
  {
    id: 4,
    avatar: '💪',
    name: '刘教练',
    title: '健美冠军',
    rating: 5,
    price: 500,
    specialties: ['健美', '力量训练', '竞技训练'],
    description: '多次获得全国健美比赛冠军，专注竞技健身训练',
    courses: [
      { id: 1, name: '冠军私教', price: 500 },
      { id: 2, name: '10节套餐', price: 4500 },
      { id: 3, name: '30节套餐', price: 12000 }
    ]
  }
])

const showBookingDialog = ref(false)
const selectedTrainer = ref(null)
const bookingForm = ref({
  courseType: '',
  date: '',
  time: '',
  note: ''
})

const disabledDate = (date) => {
  return date < new Date()
}

const bookTrainer = (trainer) => {
  selectedTrainer.value = trainer
  bookingForm.value = { courseType: '', date: '', time: '', note: '' }
  showBookingDialog.value = true
}

const confirmBooking = async () => {
  if (!bookingForm.value.courseType || !bookingForm.value.date || !bookingForm.value.time) {
    ElMessage.warning('请填写完整的预约信息')
    return
  }
  try {
    const token = localStorage.getItem('token')
    const response = await axios.post('/api/bookings', {
      trainerId: selectedTrainer.value.id,
      courseId: bookingForm.value.courseType,
      date: bookingForm.value.date,
      time: bookingForm.value.time,
      note: bookingForm.value.note
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (response.data.success) {
      ElMessage.success('预约成功')
      showBookingDialog.value = false
    }
  } catch (error) {
    ElMessage.error('预约失败，请稍后重试')
  }
}
</script>

<style scoped>
.private-trainer-container {
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

.trainers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(450px, 1fr));
  gap: var(--spacing-lg);
}

.trainer-card {
  background: white;
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  display: flex;
  gap: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
}

.trainer-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-md);
}

.trainer-avatar {
  font-size: 64px;
  flex-shrink: 0;
}

.trainer-info {
  flex: 1;
}

.trainer-name {
  font-size: var(--font-size-lg);
  font-weight: 700;
  margin-bottom: var(--spacing-xs);
}

.trainer-title {
  color: var(--text-secondary);
  margin-bottom: var(--spacing-sm);
}

.trainer-rating {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-bottom: var(--spacing-sm);
}

.trainer-rating span {
  opacity: 0.3;
}

.trainer-rating span.filled {
  opacity: 1;
}

.rating-text {
  margin-left: var(--spacing-xs);
  color: var(--text-secondary);
}

.trainer-specialties {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-sm);
}

.trainer-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.5;
}

.trainer-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-md);
}

.trainer-price {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: var(--fitness-primary);
}

.booking-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.booking-trainer {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--bg-primary);
  border-radius: var(--radius-md);
}

.booking-avatar {
  font-size: 48px;
}

.booking-name {
  font-weight: 600;
}

.booking-title {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.form-item label {
  display: block;
  margin-bottom: var(--spacing-xs);
  font-weight: 500;
}
</style>