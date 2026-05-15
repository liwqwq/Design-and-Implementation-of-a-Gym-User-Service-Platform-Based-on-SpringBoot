<template>
  <UserLayout>
    <div class="profile-container">
      <h2 class="page-title">👤 个人中心</h2>

      <div class="profile-card">
        <div class="card-header">
          <span>个人信息</span>
          <el-button type="primary" @click="getProfile">刷新资料</el-button>
        </div>
        <div class="profile-content">
          <div v-if="profile" class="info-grid">
            <div class="info-item" v-for="item in profileItems" :key="item.key">
              <label>{{ item.label }}：</label>
              <span>{{ item.key === 'active' ? (profile.active ? '活跃' : '不活跃') : (profile[item.key] || '未设置') }}</span>
            </div>
          </div>
          <div v-else class="empty-profile">
            <el-button type="primary" @click="getProfile">获取个人资料</el-button>
          </div>
        </div>
      </div>

      <div class="profile-card">
        <div class="card-header">
          <span>功能菜单</span>
        </div>
        <div class="feature-grid">
          <div class="feature-card" @click="goToSchedule">
            <div class="feature-icon">📅</div>
            <div class="feature-title">课表查看</div>
            <div class="feature-desc">查看本周课程安排</div>
          </div>
          <div class="feature-card" @click="showComplaintModal = true">
            <div class="feature-icon">📝</div>
            <div class="feature-title">投诉建议</div>
            <div class="feature-desc">反馈问题与建议</div>
          </div>
          <div class="feature-card" @click="goToMembership">
            <div class="feature-icon">💳</div>
            <div class="feature-title">会员中心</div>
            <div class="feature-desc">查看会员状态</div>
          </div>
          <div class="feature-card" @click="goToSocial">
            <div class="feature-icon">💬</div>
            <div class="feature-title">社区互动</div>
            <div class="feature-desc">分享健身心得</div>
          </div>
          <div class="feature-card" @click="goToPointsMall">
            <div class="feature-icon">💰</div>
            <div class="feature-title">积分商城</div>
            <div class="feature-desc">积分兑换好礼</div>
          </div>
          <div class="feature-card" @click="goToRanking">
            <div class="feature-icon">🏆</div>
            <div class="feature-title">排行榜</div>
            <div class="feature-desc">查看健身达人</div>
          </div>
        </div>
      </div>

      <div class="profile-card">
        <div class="card-header">
          <span>更新个人资料</span>
        </div>
        <form @submit.prevent="updateProfile" class="update-form">
          <div class="form-item">
            <label>姓名</label>
            <el-input v-model="updateForm.name" placeholder="请输入姓名" />
          </div>
          <div class="form-item">
            <label>邮箱</label>
            <el-input v-model="updateForm.email" type="email" placeholder="请输入邮箱" />
          </div>
          <div class="form-item">
            <label>电话</label>
            <el-input v-model="updateForm.phone" placeholder="请输入电话" />
          </div>
          <div class="form-item">
            <label>生日</label>
            <el-date-picker v-model="updateForm.birthday" type="date" style="width: 100%" />
          </div>
          <div class="form-item">
            <label>性别</label>
            <el-select v-model="updateForm.gender" placeholder="请选择性别">
              <el-option label="男" value="男" />
              <el-option label="女" value="女" />
            </el-select>
          </div>
          <div class="form-item">
            <label>地址</label>
            <el-input v-model="updateForm.address" placeholder="请输入地址" />
          </div>
          <el-button type="primary" native-type="submit">更新资料</el-button>
        </form>
      </div>

      <el-dialog v-model="showComplaintModal" title="投诉建议" width="500px" center>
        <form @submit.prevent="submitComplaint">
          <div class="form-item">
            <label>投诉类型</label>
            <el-select v-model="complaintForm.type" placeholder="请选择投诉类型">
              <el-option label="服务态度" value="service" />
              <el-option label="设施问题" value="facility" />
              <el-option label="课程问题" value="course" />
              <el-option label="其他" value="other" />
            </el-select>
          </div>
          <div class="form-item">
            <label>投诉内容</label>
            <el-input v-model="complaintForm.content" type="textarea" rows="5" placeholder="请详细描述您的问题..." />
          </div>
          <div class="form-item">
            <label>联系方式</label>
            <el-input v-model="complaintForm.contact" placeholder="请留下您的联系方式" />
          </div>
          <div>
            <el-button @click="showComplaintModal = false">取消</el-button>
            <el-button type="primary" @click="submitComplaint">提交投诉</el-button>
          </div>
        </form>
      </el-dialog>
    </div>
  </UserLayout>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import UserLayout from '../components/UserLayout.vue'
import { ElButton, ElInput, ElSelect, ElDatePicker, ElDialog } from 'element-plus'

const router = useRouter()
const profile = ref(null)
const updateForm = reactive({ name: '', email: '', phone: '', birthday: '', gender: '', address: '' })
const showComplaintModal = ref(false)
const complaintForm = reactive({ type: '', content: '', contact: '' })

const profileItems = [
  { label: '用户名', key: 'username' },
  { label: '姓名', key: 'name' },
  { label: '邮箱', key: 'email' },
  { label: '电话', key: 'phone' },
  { label: '状态', key: 'active' },
  { label: '角色', key: 'role' }
]

const goToSchedule = () => router.push('/user/group-classes')
const goToMembership = () => router.push('/user/membership')
const goToSocial = () => router.push('/user/social')
const goToPointsMall = () => router.push('/user/points-mall')
const goToRanking = () => router.push('/user/ranking')

const getProfile = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/users/profile', { headers: { Authorization: `Bearer ${token}` } })
    if (response.data.success) {
      profile.value = response.data.data
      updateForm.name = profile.value.name || ''
      updateForm.email = profile.value.email || ''
      updateForm.phone = profile.value.phone || ''
      updateForm.birthday = profile.value.birthday || ''
      updateForm.gender = profile.value.gender || ''
      updateForm.address = profile.value.address || ''
    }
  } catch (err) {
    console.error('获取个人资料失败:', err)
  }
}

const updateProfile = async () => {
  try {
    const token = localStorage.getItem('token')
    await axios.put('/api/users/profile', updateForm, { headers: { Authorization: `Bearer ${token}` } })
    ElMessage.success('更新成功')
    getProfile()
  } catch (err) {
    ElMessage.error('更新失败')
  }
}

const submitComplaint = async () => {
  try {
    const token = localStorage.getItem('token')
    await axios.post('/api/complaints', complaintForm, { headers: { Authorization: `Bearer ${token}` } })
    ElMessage.success('投诉提交成功')
    showComplaintModal.value = false
    complaintForm.type = ''
    complaintForm.content = ''
    complaintForm.contact = ''
  } catch (err) {
    ElMessage.error('提交失败')
  }
}
</script>

<style scoped>
.profile-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  text-align: center;
  margin-bottom: 30px;
}

.profile-card {
  background: white;
  border-radius: 15px;
  padding: 25px;
  margin-bottom: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.card-header span {
  font-weight: 600;
  font-size: 18px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.info-item {
  display: flex;
  gap: 10px;
}

.info-item label {
  font-weight: 500;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
}

.feature-card {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.15);
}

.feature-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.feature-title {
  font-weight: 600;
  margin-bottom: 5px;
}

.feature-desc {
  font-size: 13px;
  color: #666;
}

.update-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item label {
  font-weight: 500;
}
</style>
